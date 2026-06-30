"""
collect_seed_images.py
피부 타입별 추천 상위 상품 대상 이미지 URL 수집 (Lite 모드)
대상: 피부 타입별 상위 15개 × 5타입 = 최대 70개 (중복 제거)

Usage:
  python scripts/collect_seed_images.py --dry-run
  python scripts/collect_seed_images.py --actual [--sleep 3.0] [--force]
"""

import sys
import io
import csv
import time
import re
import argparse

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

import oracledb
oracledb.init_oracle_client = lambda **kw: None

import cloudscraper
from bs4 import BeautifulSoup

DB_DSN  = "localhost:1521/xepdb1"
DB_USER = "hr"
DB_PASS = "hr"
TIMEOUT_SEC = 15
MAX_RETRY   = 2
SKIN_TYPES  = ['건성', '지성', '복합성', '민감성', '중성']
TOP_N       = 15
OUT_CSV     = r"D:\Lecture\spring-workspace\beautylens-mvc\docs\seed_image_collection_results.csv"


def make_product_url(platform, ppid):
    if platform == "oliveyoung":
        return f"https://www.oliveyoung.co.kr/store/goods/getGoodsDetail.do?goodsNo={ppid}"
    if platform == "musinsa":
        return f"https://www.musinsa.com/products/{ppid}"
    return None


def extract_image_url(html):
    soup = BeautifulSoup(html, "html.parser")

    # 1. og:image
    tag = soup.find("meta", property="og:image")
    if tag and tag.get("content", "").strip().startswith("http"):
        return tag["content"].strip(), "og:image"

    # 2. twitter:image
    tag = soup.find("meta", attrs={"name": "twitter:image"})
    if tag and tag.get("content", "").strip().startswith("http"):
        return tag["content"].strip(), "twitter:image"

    # 3. oliveyoung CDN img
    for img in soup.find_all("img", src=True):
        s = img["src"]
        if "image.oliveyoung.co.kr" in s and s.startswith("http"):
            return s, "oy_cdn_img"

    # 4. musinsa CDN img
    for img in soup.find_all("img", src=True):
        s = img["src"]
        if "image.msscdn.net" in s and s.startswith("http"):
            return s, "ms_cdn_img"

    return None, None


def fetch_with_retry(scraper, url, max_retry=2, timeout=15):
    for attempt in range(max_retry + 1):
        try:
            resp = scraper.get(url, timeout=timeout)
            if resp.status_code == 200:
                return 200, resp.text, None
            if resp.status_code in (403, 429, 503):
                if attempt < max_retry:
                    time.sleep(5)
                    continue
            return resp.status_code, None, f"HTTP {resp.status_code}"
        except Exception as e:
            if attempt < max_retry:
                time.sleep(3)
                continue
            return 0, None, str(e)
    return 0, None, "max retry exceeded"


def load_targets(conn):
    cur = conn.cursor()
    sql = """
        SELECT product_id, platform, platform_product_id, product_name,
               base_skin_type, recommendation_score, image_status
        FROM (
          SELECT product_id, platform, platform_product_id, product_name,
                 base_skin_type, recommendation_score, image_status,
                 ROW_NUMBER() OVER (
                   PARTITION BY base_skin_type
                   ORDER BY RECOMMENDATION_SCORE DESC NULLS LAST
                 ) AS rn
          FROM BL_PRODUCTS
          WHERE base_skin_type IN ('건성','지성','복합성','민감성','중성')
        )
        WHERE rn <= :top_n
        ORDER BY base_skin_type, rn
    """
    cur.execute(sql, top_n=TOP_N)
    rows = cur.fetchall()
    cur.close()
    # 중복 productId 제거 (같은 상품이 여러 피부타입에 해당하는 edge case 방어)
    seen = set()
    targets = []
    for r in rows:
        pid = r[0]
        if pid not in seen:
            seen.add(pid)
            targets.append({
                "productId":          r[0],
                "platform":           r[1],
                "platformProductId":  r[2],
                "productName":        r[3],
                "baseSkinType":       r[4],
                "recommendationScore": r[5],
                "imageStatus":        r[6],
            })
    return targets


def main():
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--dry-run",  action="store_true")
    group.add_argument("--actual",   action="store_true")
    parser.add_argument("--sleep",   type=float, default=3.0)
    parser.add_argument("--force",   action="store_true", help="이미 found인 항목도 재수집")
    args = parser.parse_args()

    conn = oracledb.connect(user=DB_USER, password=DB_PASS, dsn=DB_DSN)
    targets = load_targets(conn)

    skin_counts = {}
    for t in targets:
        sk = t["baseSkinType"]
        skin_counts[sk] = skin_counts.get(sk, 0) + 1

    print(f"=== collect_seed_images.py ({'DRY-RUN' if args.dry_run else 'ACTUAL'}) ===")
    print(f"대상 상품 수: {len(targets)}")
    for sk, cnt in sorted(skin_counts.items()):
        print(f"  {sk}: {cnt}개")

    already_found = [t for t in targets if t["imageStatus"] == "found"]
    to_collect    = [t for t in targets if t["imageStatus"] != "found"] if not args.force else targets

    print(f"\n이미 found: {len(already_found)}건 (스킵)")
    print(f"수집 대상:  {len(to_collect)}건")

    if args.dry_run:
        print("\n[DRY-RUN] DB 변경 없음.")
        for t in to_collect[:5]:
            url = make_product_url(t["platform"], t["platformProductId"])
            print(f"  [{t['baseSkinType']}] id={t['productId']} {t['productName'][:30]}")
            print(f"    url={url}")
        if len(to_collect) > 5:
            print(f"  ... 외 {len(to_collect)-5}건")
        conn.close()
        return

    # --- ACTUAL ---
    scraper = cloudscraper.create_scraper()
    cur = conn.cursor()

    results = []
    success = 0
    not_found = 0
    failed = 0

    for i, t in enumerate(to_collect, 1):
        pid      = t["productId"]
        platform = t["platform"]
        ppid     = t["platformProductId"]
        prod_url = make_product_url(platform, ppid)

        print(f"[{i:3d}/{len(to_collect)}] id={pid} [{t['baseSkinType']}] {t['productName'][:35]}")

        if not prod_url:
            status = "blocked"
            img_url = None
            method  = None
            print(f"  → URL 구성 불가")
        else:
            status_code, html, err = fetch_with_retry(scraper, prod_url, MAX_RETRY, TIMEOUT_SEC)

            if status_code == 200 and html:
                img_url, method = extract_image_url(html)
                if img_url:
                    status = "found"
                    success += 1
                    print(f"  → found  [{method}] {img_url[:60]}")
                else:
                    status = "not_found"
                    img_url = None
                    not_found += 1
                    print(f"  → not_found (og:image 없음)")
            elif status_code == 403:
                status = "blocked"
                img_url = None
                failed += 1
                print(f"  → blocked (HTTP 403)")
            else:
                status = "fetch_failed"
                img_url = None
                failed += 1
                print(f"  → fetch_failed ({err})")

        results.append({
            "productId":   pid,
            "platform":    platform,
            "baseSkinType": t["baseSkinType"],
            "score":       t["recommendationScore"],
            "productName": t["productName"],
            "productUrl":  prod_url or "",
            "imageUrl":    img_url or "",
            "imageStatus": status,
            "method":      method or "",
        })

        cur.execute(
            "UPDATE BL_PRODUCTS SET PRODUCT_URL=:1, IMAGE_URL=:2, IMAGE_STATUS=:3 WHERE PRODUCT_ID=:4",
            (prod_url, img_url, status, pid)
        )
        conn.commit()

        if i < len(to_collect):
            time.sleep(args.sleep)

    cur.close()
    conn.close()

    # CSV 출력
    with open(OUT_CSV, "w", newline="", encoding="utf-8-sig") as f:
        w = csv.DictWriter(f, fieldnames=["productId","platform","baseSkinType","score","productName","productUrl","imageUrl","imageStatus","method"])
        w.writeheader()
        w.writerows(results)

    print(f"\n=== 완료 ===")
    print(f"성공(found):    {success}")
    print(f"not_found:      {not_found}")
    print(f"실패:           {failed}")
    print(f"CSV: {OUT_CSV}")


if __name__ == "__main__":
    main()
