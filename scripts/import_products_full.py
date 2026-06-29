"""
BeautyLens - Full Product Import Script
Source: product_recommendation_scores.parquet (preprocessed_v3)
Target: BL_PRODUCTS

Usage:
  python import_products_full.py --dry-run
  python import_products_full.py --dry-run --limit 100
  python import_products_full.py           # actual import
"""

import sys
import argparse
import datetime
import collections

import pyarrow.parquet as pq
import pandas as pd

PARQUET_PATH = r"D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\product_recommendation_scores.parquet"

REQUIRED_COLS = ["product_key", "platform", "product_id", "product_name"]

BATCH_SIZE = 2000

# ============================================================
# Helpers
# ============================================================

def safe_float(v):
    if v is None:
        return None
    try:
        import math
        f = float(v)
        return None if math.isnan(f) else f
    except (TypeError, ValueError):
        return None

def safe_int(v):
    if v is None:
        return None
    try:
        import math
        f = float(v)
        if math.isnan(f):
            return None
        return int(f)
    except (TypeError, ValueError):
        return None

def safe_str(v, max_len=None):
    if v is None:
        return None
    import math
    if isinstance(v, float) and math.isnan(v):
        return None
    s = str(v).strip()
    if s in ("", "nan", "NaN", "None"):
        return None
    return s[:max_len] if max_len else s

def row_to_params(row):
    return (
        safe_str(row.get("platform"), 20),
        safe_str(row.get("product_id"), 50),
        safe_str(row.get("product_key"), 80),
        safe_str(row.get("product_name"), 500),
        safe_str(row.get("brand"), 100),
        safe_str(row.get("category"), 50),
        safe_float(row.get("price")),
        safe_float(row.get("avg_rating")),
        safe_int(row.get("total_review_count")),
        safe_float(row.get("overall_positive_rate")),
        safe_float(row.get("overall_neutral_rate")),
        safe_float(row.get("overall_negative_rate")),
        safe_str(row.get("base_skin_type"), 20),
        safe_int(row.get("skin_review_count")),
        safe_float(row.get("skin_positive_rate")),
        safe_float(row.get("skin_neutral_rate")),
        safe_float(row.get("skin_negative_rate")),
        safe_str(row.get("top_skin_need_tags"), 300),
        safe_str(row.get("top_skin_concern_tags"), 300),
        safe_str(row.get("caution_level"), 40) or "normal",
        safe_float(row.get("recommendation_score")),
        safe_str(row.get("recommendation_tier"), 40),
        safe_str(row.get("evidence_level"), 40),
    )

INSERT_SQL = """
    INSERT INTO BL_PRODUCTS (
        PRODUCT_ID, PLATFORM, PLATFORM_PRODUCT_ID, PRODUCT_KEY, PRODUCT_NAME,
        BRAND, CATEGORY, PRICE, AVG_RATING, TOTAL_REVIEW_COUNT,
        OVERALL_POS_RATE, OVERALL_NEU_RATE, OVERALL_NEG_RATE,
        BASE_SKIN_TYPE, SKIN_REVIEW_COUNT,
        SKIN_POS_RATE, SKIN_NEU_RATE, SKIN_NEG_RATE,
        TOP_NEED_TAGS, TOP_CONCERN_TAGS, CAUTION_LEVEL,
        RECOMMENDATION_SCORE, RECOMMENDATION_TIER, EVIDENCE_LEVEL
    ) VALUES (
        SEQ_BL_PRODUCTS.NEXTVAL, :1, :2, :3, :4,
        :5, :6, :7, :8, :9,
        :10, :11, :12,
        :13, :14,
        :15, :16, :17,
        :18, :19, :20,
        :21, :22, :23
    )
"""

def log_import(cursor, conn, filename, total, success, fail, skipped, status, error_msg, start):
    end = datetime.datetime.now()
    cursor.execute("""
        INSERT INTO BL_IMPORT_LOGS
            (LOG_ID, FILE_NAME, TOTAL_COUNT, SUCCESS_COUNT, FAIL_COUNT,
             SKIPPED_COUNT, STATUS, ERROR_MSG, START_TIME, END_TIME)
        VALUES (SEQ_BL_IMPORT_LOGS.NEXTVAL, :1, :2, :3, :4, :5, :6, :7, :8, :9)
    """, (filename, total, success, fail, skipped,
          status, error_msg[:3900] if error_msg else None, start, end))
    conn.commit()

# ============================================================
# Main
# ============================================================

def main():
    parser = argparse.ArgumentParser(description="Full Product Import")
    parser.add_argument("--dry-run", action="store_true", help="Analyze only, no DB writes")
    parser.add_argument("--limit", type=int, default=None, help="Max unique products to process")
    args = parser.parse_args()

    SEP = "=" * 60
    print(SEP)
    print("BeautyLens Full Product Import")
    print(f"Mode: {'DRY-RUN' if args.dry_run else 'ACTUAL IMPORT'}")
    print(f"Start: {datetime.datetime.now()}")
    print(SEP)

    # --------------------------------------------------------
    # Read parquet in batches
    # --------------------------------------------------------
    print(f"\n[FILE] {PARQUET_PATH}")
    pf = pq.ParquetFile(PARQUET_PATH)
    all_cols = pf.schema_arrow.names
    print(f"  Schema columns ({len(all_cols)}): {all_cols}")

    missing = [c for c in REQUIRED_COLS if c not in all_cols]
    if missing:
        print(f"[ERROR] Missing required columns: {missing}")
        sys.exit(1)
    print(f"  Required columns verified.")

    # Dry-run stats
    total_rows      = 0
    seen_keys       = set()
    skip_no_key     = 0
    skip_no_name    = 0
    skip_dup_key    = 0
    platform_counts = collections.Counter()
    caution_counts  = collections.Counter()
    fail_samples    = []

    # Actual import stats (only used if not dry-run)
    success = fail = 0

    if not args.dry_run:
        import oracledb
        conn = oracledb.connect(user="hr", password="hr", dsn="localhost:1521/xepdb1")
        cursor = conn.cursor()
        start_imp = datetime.datetime.now()
    else:
        conn = cursor = start_imp = None

    for batch in pf.iter_batches(batch_size=BATCH_SIZE):
        df = batch.to_pandas()
        for _, row in df.iterrows():
            total_rows += 1

            pk   = safe_str(row.get("product_key"), 80)
            pn   = safe_str(row.get("product_name"), 500)
            plat = safe_str(row.get("platform"), 20)

            if not pk:
                skip_no_key += 1
                continue
            if not pn:
                skip_no_name += 1
                continue
            if pk in seen_keys:
                skip_dup_key += 1
                continue

            seen_keys.add(pk)
            if plat:
                platform_counts[plat] += 1
            caution = safe_str(row.get("caution_level"), 40) or "normal"
            caution_counts[caution] += 1

            if args.limit and len(seen_keys) > args.limit:
                break

            if not args.dry_run:
                try:
                    cursor.execute(INSERT_SQL, row_to_params(row))
                    conn.commit()
                    success += 1
                except Exception as e:
                    fail += 1
                    if len(fail_samples) < 5:
                        fail_samples.append(f"key={pk}: {e}")

        if args.limit and len(seen_keys) > args.limit:
            break

    unique_products = len(seen_keys)
    skipped_total   = skip_no_key + skip_no_name + skip_dup_key

    print(f"\n{'--- DRY-RUN REPORT ---' if args.dry_run else '--- IMPORT COMPLETE ---'}")
    print(f"  Total rows read:          {total_rows:,}")
    print(f"  Unique products (insertable): {unique_products:,}")
    print(f"  Skip (no product_key):    {skip_no_key:,}")
    print(f"  Skip (no product_name):   {skip_no_name:,}")
    print(f"  Skip (dup product_key):   {skip_dup_key:,}")
    print(f"  Platform distribution:")
    for plat, cnt in sorted(platform_counts.items(), key=lambda x: -x[1]):
        print(f"    {plat}: {cnt:,}")
    print(f"  Caution level distribution:")
    for caution, cnt in sorted(caution_counts.items(), key=lambda x: -x[1]):
        print(f"    {caution}: {cnt:,}")

    if not args.dry_run:
        print(f"\n  Inserted: {success:,}  Failed: {fail:,}")
        if fail_samples:
            print(f"  Fail samples: {fail_samples}")
        status = "success" if fail == 0 else ("partial" if success > 0 else "failed")
        log_import(cursor, conn,
                   "product_recommendation_scores.parquet (full)",
                   total_rows, success, fail, skipped_total,
                   status, "; ".join(fail_samples[:30]), start_imp)
        cursor.close()
        conn.close()
    else:
        print(f"\n  [DRY-RUN] No data was written to DB.")
        print(f"  Expected insert count: {unique_products:,}")

    print(f"\nDone: {datetime.datetime.now()}")
    print(SEP)

if __name__ == "__main__":
    main()
