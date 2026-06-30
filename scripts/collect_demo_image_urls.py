"""
Collect representative image URLs only for demo-visible products.

Usage:
  python scripts/collect_demo_image_urls.py --dry-run --limit 30
  python scripts/collect_demo_image_urls.py --actual --limit 30 --sleep 1.5
"""

import argparse
import csv
import io
import os
import sys
import time

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import cloudscraper
import oracledb

from collect_image_urls import (
    DB_DSN,
    DB_PASS,
    DB_USER,
    determine_status,
    extract_image_url,
    fetch_with_retry,
    make_product_url,
)

oracledb.init_oracle_client = lambda **kw: None

RESULTS_CSV = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs",
    "demo_image_collection_results.csv",
)


def connect():
    return oracledb.connect(user=DB_USER, password=DB_PASS, dsn=DB_DSN)


def load_targets(conn, limit):
    sql = """
        SELECT *
          FROM (
            SELECT PRODUCT_ID,
                   PLATFORM,
                   PLATFORM_PRODUCT_ID,
                   PRODUCT_NAME,
                   BRAND,
                   PRODUCT_URL,
                   IMAGE_URL,
                   IMAGE_STATUS,
                   RECOMMENDATION_SCORE,
                   TOTAL_REVIEW_COUNT
              FROM BL_PRODUCTS
             WHERE (IMAGE_URL IS NULL OR IMAGE_STATUS IS NULL OR IMAGE_STATUS != 'found')
             ORDER BY RECOMMENDATION_SCORE DESC NULLS LAST,
                      TOTAL_REVIEW_COUNT DESC NULLS LAST,
                      PRODUCT_ID DESC
          )
         WHERE ROWNUM <= :limit
    """
    with conn.cursor() as cur:
        cur.execute(sql, limit=limit)
        cols = [d[0].lower() for d in cur.description]
        return [dict(zip(cols, row)) for row in cur.fetchall()]


def update_product(conn, product_id, product_url, image_url, image_status):
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE BL_PRODUCTS
               SET PRODUCT_URL = :product_url,
                   IMAGE_URL = :image_url,
                   IMAGE_STATUS = :image_status
             WHERE PRODUCT_ID = :product_id
            """,
            product_url=product_url,
            image_url=image_url,
            image_status=image_status,
            product_id=product_id,
        )


def write_results(rows):
    os.makedirs(os.path.dirname(RESULTS_CSV), exist_ok=True)
    fields = [
        "productId",
        "platform",
        "brand",
        "productName",
        "productUrl",
        "imageUrl",
        "imageStatus",
        "extractMethod",
        "httpStatus",
        "errorMessage",
    ]
    with open(RESULTS_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--dry-run", action="store_true")
    group.add_argument("--actual", action="store_true")
    parser.add_argument("--limit", type=int, default=30)
    parser.add_argument("--sleep", type=float, default=1.5)
    parser.add_argument("--timeout", type=int, default=6)
    parser.add_argument("--retry", type=int, default=0)
    args = parser.parse_args()

    conn = connect()
    targets = load_targets(conn, args.limit)
    print(f"[collect_demo_image_urls] targets={len(targets)} mode={'DRY-RUN' if args.dry_run else 'ACTUAL'}")

    if args.dry_run:
        for item in targets[:10]:
            url = item.get("product_url") or make_product_url(item["platform"], item["platform_product_id"])
            print(f"  id={item['product_id']} platform={item['platform']} url={url}")
        conn.close()
        return

    scraper = cloudscraper.create_scraper(
        browser={"browser": "chrome", "platform": "windows", "mobile": False}
    )
    results = []
    found = 0
    failed = 0

    for idx, item in enumerate(targets, 1):
        product_id = item["product_id"]
        product_url = item.get("product_url") or make_product_url(item["platform"], item["platform_product_id"])
        image_url = None
        method = None
        http_status = None
        err = None

        if not product_url:
            status = "invalid_url"
            err = "product url unavailable"
        else:
            http_status, html, err = fetch_with_retry(
                scraper,
                product_url,
                max_retry=args.retry,
                timeout=args.timeout,
            )
            if html:
                image_url, method = extract_image_url(html)
            status = determine_status(http_status, image_url, err)

        if status == "found":
            found += 1
        else:
            failed += 1

        update_product(conn, product_id, product_url, image_url, status)
        conn.commit()
        print(f"[{idx}/{len(targets)}] id={product_id} status={status} image={image_url or '-'}")

        results.append({
            "productId": product_id,
            "platform": item.get("platform"),
            "brand": item.get("brand"),
            "productName": item.get("product_name"),
            "productUrl": product_url,
            "imageUrl": image_url,
            "imageStatus": status,
            "extractMethod": method or "",
            "httpStatus": http_status or "",
            "errorMessage": err or "",
        })
        time.sleep(args.sleep)

    write_results(results)
    conn.close()
    print(f"summary found={found} failed={failed} results={RESULTS_CSV}")
    if found:
        print("sample image urls:")
        for row in [r for r in results if r["imageStatus"] == "found"][:20]:
            print(f"  {row['productId']}: {row['imageUrl']}")


if __name__ == "__main__":
    main()
