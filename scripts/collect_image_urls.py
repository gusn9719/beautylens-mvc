"""
scripts/collect_image_urls.py
BL_PRODUCTS 전체 상품 대표 이미지 URL 수집

Usage:
  python scripts/collect_image_urls.py --dry-run --limit 20
  python scripts/collect_image_urls.py --actual --sleep 2.5
  python scripts/collect_image_urls.py --actual --sleep 2.5 --force
  python scripts/collect_image_urls.py --actual --limit 50 --sleep 2.0
"""

import argparse
import csv
import os
import re
import sys
import time

import cloudscraper
import oracledb as cx_Oracle
cx_Oracle.init_oracle_client = lambda **kw: None  # thin mode — no Oracle client needed

# ─── 설정 ─────────────────────────────────────────────────────────────────────

DB_DSN  = "localhost:1521/xepdb1"
DB_USER = "hr"
DB_PASS = "hr"

RESULTS_CSV = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs", "image_collection_results.csv"
)

COMMIT_EVERY    = 50
MAX_RETRY       = 2
TIMEOUT_SEC     = 15
CONSECUTIVE_FAIL_LIMIT = 10  # 연속 실패 시 중단

IMAGE_STATUS_FOUND      = "found"
IMAGE_STATUS_NOT_FOUND  = "not_found"
IMAGE_STATUS_FETCH_FAIL = "fetch_failed"
IMAGE_STATUS_BLOCKED    = "blocked"
IMAGE_STATUS_INVALID    = "invalid_url"
IMAGE_STATUS_PENDING    = "pending"

# ─── URL 구성 ─────────────────────────────────────────────────────────────────

def make_product_url(platform, platform_product_id):
    if platform == "oliveyoung":
        return (
            "https://www.oliveyoung.co.kr/store/goods/getGoodsDetail.do"
            f"?goodsNo={platform_product_id}"
        )
    if platform == "musinsa":
        return f"https://www.musinsa.com/products/{platform_product_id}"
    return None


# ─── 이미지 추출 ─────────────────────────────────────────────────────────────

def extract_image_url(html):
    # 1. og:image (content first)
    m = re.search(
        r'<meta[^>]+property=["\']og:image["\'][^>]+content=["\']([^"\']+)["\']',
        html, re.I
    )
    if not m:
        m = re.search(
            r'<meta[^>]+content=["\']([^"\']+)["\'][^>]+property=["\']og:image["\']',
            html, re.I
        )
    if m:
        return m.group(1).strip(), "og:image"

    # 2. twitter:image
    m = re.search(
        r'<meta[^>]+name=["\']twitter:image["\'][^>]+content=["\']([^"\']+)["\']',
        html, re.I
    )
    if not m:
        m = re.search(
            r'<meta[^>]+content=["\']([^"\']+)["\'][^>]+name=["\']twitter:image["\']',
            html, re.I
        )
    if m:
        return m.group(1).strip(), "twitter:image"

    # 3. JSON-LD image
    m = re.search(r'"image"\s*:\s*"(https?://[^"]+)"', html)
    if m:
        return m.group(1), "json-ld"

    # 4. oliveyoung CDN img
    m = re.search(
        r'(https?://image\.oliveyoung\.co\.kr/[^\s"\'<>]+\.(?:jpg|jpeg|png|webp))',
        html, re.I
    )
    if m:
        return m.group(1), "img-oliveyoung"

    # 5. musinsa CDN img
    m = re.search(
        r'(https?://image\.msscdn\.net/[^\s"\'<>]+\.(?:jpg|jpeg|png|webp))',
        html, re.I
    )
    if m:
        return m.group(1), "img-musinsa"

    return None, None


def is_valid_image_url(url):
    if not url:
        return False
    if not url.startswith(("http://", "https://")):
        return False
    if len(url) > 1000:
        return False
    return True


# ─── 수집 함수 ───────────────────────────────────────────────────────────────

def fetch_with_retry(scraper, product_url, max_retry=MAX_RETRY, timeout=TIMEOUT_SEC):
    last_status = None
    last_err = None
    for attempt in range(max_retry + 1):
        try:
            resp = scraper.get(product_url, timeout=timeout, allow_redirects=True)
            last_status = resp.status_code
            if resp.status_code == 200:
                return resp.status_code, resp.text, None
            if resp.status_code in (403, 429, 503):
                last_err = f"HTTP_{resp.status_code}"
                if attempt < max_retry:
                    time.sleep(5)
                    continue
                return resp.status_code, None, last_err
            return resp.status_code, None, f"HTTP_{resp.status_code}"
        except Exception as e:
            last_err = str(e)[:80]
            if attempt < max_retry:
                time.sleep(3)
    return last_status, None, last_err


def determine_status(http_status, image_url, err):
    if err:
        if "403" in str(err) or "blocked" in str(err).lower():
            return IMAGE_STATUS_BLOCKED
        return IMAGE_STATUS_FETCH_FAIL
    if http_status and http_status in (403, 429):
        return IMAGE_STATUS_BLOCKED
    if image_url and is_valid_image_url(image_url):
        return IMAGE_STATUS_FOUND
    if http_status == 200:
        return IMAGE_STATUS_NOT_FOUND
    return IMAGE_STATUS_FETCH_FAIL


# ─── DB 함수 ────────────────────────────────────────────────────────────────

def get_connection():
    return cx_Oracle.connect(user=DB_USER, password=DB_PASS, dsn=DB_DSN)


def load_products(conn, force=False):
    """수집 대상 상품 조회. force=False이면 found 스킵."""
    with conn.cursor() as cur:
        if force:
            cur.execute(
                """SELECT PRODUCT_ID, PLATFORM, PLATFORM_PRODUCT_ID, PRODUCT_KEY,
                          PRODUCT_NAME, BRAND
                     FROM BL_PRODUCTS
                    ORDER BY RECOMMENDATION_SCORE DESC NULLS LAST"""
            )
        else:
            cur.execute(
                """SELECT PRODUCT_ID, PLATFORM, PLATFORM_PRODUCT_ID, PRODUCT_KEY,
                          PRODUCT_NAME, BRAND
                     FROM BL_PRODUCTS
                    WHERE IMAGE_STATUS IS NULL
                       OR IMAGE_STATUS != 'found'
                       OR IMAGE_URL IS NULL
                    ORDER BY RECOMMENDATION_SCORE DESC NULLS LAST"""
            )
        cols = [d[0].lower() for d in cur.description]
        return [dict(zip(cols, row)) for row in cur.fetchall()]


def update_product(conn, cur, product_id, product_url, image_url, image_status):
    cur.execute(
        """UPDATE BL_PRODUCTS
              SET PRODUCT_URL   = :1,
                  IMAGE_URL     = :2,
                  IMAGE_STATUS  = :3
            WHERE PRODUCT_ID    = :4""",
        (product_url, image_url, image_status, product_id)
    )


# ─── 메인 ───────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--dry-run",  action="store_true")
    mode.add_argument("--actual",   action="store_true")
    parser.add_argument("--limit",  type=int, default=0, help="처리 상한 (0=전체)")
    parser.add_argument("--sleep",  type=float, default=2.5, help="요청 간 sleep 초")
    parser.add_argument("--force",  action="store_true", help="found 상품도 재수집")
    args = parser.parse_args()

    dry_run = args.dry_run
    mode_label = "DRY-RUN" if dry_run else "ACTUAL"
    print(f"[collect_image_urls] mode={mode_label} limit={args.limit or '전체'} "
          f"sleep={args.sleep}s force={args.force}")

    conn = get_connection()
    products = load_products(conn, force=args.force)
    total_all = len(products)

    if args.limit > 0:
        products = products[:args.limit]

    print(f"대상 상품: {len(products)}건 (DB 전체 미수집: {total_all}건)")

    scraper = cloudscraper.create_scraper(
        browser={"browser": "chrome", "platform": "windows", "mobile": False}
    )

    results = []
    ok = 0
    fail = 0
    consecutive_fail = 0

    cur = conn.cursor()

    for i, p in enumerate(products):
        pid   = p["product_id"]
        plat  = p["platform"]
        ppid  = p["platform_product_id"]
        pname = p["product_name"]
        brand = p["brand"] or ""

        product_url = make_product_url(plat, ppid)
        if not product_url:
            row = {
                "productId": pid, "platform": plat, "platformProductId": ppid,
                "brand": brand, "productName": pname, "displayName": pname,
                "productUrl": "", "imageUrl": "", "imageStatus": IMAGE_STATUS_INVALID,
                "extractMethod": "", "httpStatus": "", "errorMessage": "no_url_pattern"
            }
            results.append(row)
            fail += 1
            continue

        # 진행률 출력
        pct = (i + 1) / len(products) * 100
        print(f"[{i+1:04d}/{len(products)}] {pct:5.1f}% id={pid} {plat}", end=" → ")

        if dry_run:
            print(f"(dry-run) url={product_url[:60]}")
            row = {
                "productId": pid, "platform": plat, "platformProductId": ppid,
                "brand": brand, "productName": pname, "displayName": pname,
                "productUrl": product_url, "imageUrl": "", "imageStatus": IMAGE_STATUS_PENDING,
                "extractMethod": "dry-run", "httpStatus": "", "errorMessage": ""
            }
            results.append(row)
            continue

        # 실제 수집
        http_status, html, err = fetch_with_retry(scraper, product_url)

        image_url   = None
        extract_method = None

        if html:
            image_url, extract_method = extract_image_url(html)

        image_status = determine_status(http_status, image_url, err)

        if image_status == IMAGE_STATUS_FOUND:
            ok += 1
            consecutive_fail = 0
            print(f"✅ {extract_method} {(image_url or '')[:55]}")
        else:
            fail += 1
            consecutive_fail += 1
            print(f"❌ {image_status} http={http_status} err={err or ''}")

        row = {
            "productId": pid, "platform": plat, "platformProductId": ppid,
            "brand": brand, "productName": pname, "displayName": pname,
            "productUrl": product_url,
            "imageUrl": image_url or "",
            "imageStatus": image_status,
            "extractMethod": extract_method or "",
            "httpStatus": str(http_status or ""),
            "errorMessage": err or ""
        }
        results.append(row)

        update_product(conn, cur, pid, product_url, image_url, image_status)

        # 50건마다 commit
        if (i + 1) % COMMIT_EVERY == 0:
            conn.commit()
            print(f"  >>> COMMIT ({i+1}건)")

        # 연속 실패 중단
        if consecutive_fail >= CONSECUTIVE_FAIL_LIMIT:
            print(f"\n⚠️  연속 {CONSECUTIVE_FAIL_LIMIT}건 실패 → 중단")
            break

        if i < len(products) - 1:
            time.sleep(args.sleep)

    if not dry_run:
        conn.commit()
        print(f"\n최종 COMMIT 완료")

    cur.close()
    conn.close()

    # ─── 결과 CSV 저장 ─────────────────────────────────────────────────────
    fieldnames = [
        "productId", "platform", "platformProductId", "brand", "productName",
        "displayName", "productUrl", "imageUrl", "imageStatus",
        "extractMethod", "httpStatus", "errorMessage"
    ]
    os.makedirs(os.path.dirname(RESULTS_CSV), exist_ok=True)
    with open(RESULTS_CSV, "w", newline="", encoding="utf-8-sig") as f:
        w = csv.DictWriter(f, fieldnames=fieldnames)
        w.writeheader()
        w.writerows(results)

    # ─── 요약 ──────────────────────────────────────────────────────────────
    print(f"\n{'='*60}")
    print(f"수집 결과 [{mode_label}]")
    print(f"  처리: {len(results)}건")
    if not dry_run:
        print(f"  ✅ found: {ok}건 ({ok/max(len(results),1)*100:.1f}%)")
        print(f"  ❌ 실패: {fail}건")
        from collections import Counter
        status_dist = Counter(r["imageStatus"] for r in results)
        print(f"  status 분포: {dict(status_dist)}")
    print(f"  CSV: {RESULTS_CSV}")


if __name__ == "__main__":
    main()
