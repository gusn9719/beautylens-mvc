"""
BeautyLens - Full Review Import Script
Source: service_reviews.parquet (preprocessed_v3)
Target: BL_REVIEWS

Usage:
  python import_reviews_full.py --dry-run
  python import_reviews_full.py --dry-run --limit 5000
  python import_reviews_full.py           # actual import
"""

import sys
import argparse
import datetime
import collections
import math

import pyarrow.parquet as pq
import pandas as pd

PARQUET_PATH = r"D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\service_reviews.parquet"

REQUIRED_COLS = ["platform", "product_id", "review_id", "review_text", "sentiment_label", "product_key"]

BATCH_SIZE = 5000
COMMIT_EVERY = 500  # commit after every N successful inserts (actual import only)

# ============================================================
# Helpers
# ============================================================

def safe_float(v):
    if v is None:
        return None
    try:
        f = float(v)
        return None if math.isnan(f) else f
    except (TypeError, ValueError):
        return None

def safe_int(v):
    if v is None:
        return None
    try:
        f = float(v)
        if math.isnan(f):
            return None
        return int(f)
    except (TypeError, ValueError):
        return None

def safe_str(v, max_len=None):
    if v is None:
        return None
    if isinstance(v, float) and math.isnan(v):
        return None
    s = str(v).strip()
    if s in ("", "nan", "NaN", "None"):
        return None
    return s[:max_len] if max_len else s

def safe_date(v):
    if v is None:
        return None
    if isinstance(v, float) and math.isnan(v):
        return None
    s = str(v).strip()
    if not s or s in ("nan", "NaN", "None"):
        return None
    for fmt in ("%Y-%m-%d", "%Y/%m/%d", "%Y.%m.%d"):
        try:
            return datetime.datetime.strptime(s, fmt).date()
        except ValueError:
            continue
    return None

INSERT_SQL = """
    INSERT INTO BL_REVIEWS (
        REVIEW_ID, PRODUCT_ID, PLATFORM_REVIEW_ID, RATING,
        REVIEW_TEXT, REVIEW_DATE,
        REVIEWER_SKIN_TYPE, REVIEWER_SKIN_CONCERN,
        SENTIMENT_LABEL, SENTIMENT_ID
    ) VALUES (
        SEQ_BL_REVIEWS.NEXTVAL, :1, :2, :3,
        :4, :5,
        :6, :7,
        :8, :9
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
# Load product_key map (for dry-run: from parquet; actual: from DB)
# ============================================================

def load_product_keys_from_parquet():
    """For dry-run: build product_key set from scores parquet."""
    scores_path = r"D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\product_recommendation_scores.parquet"
    pf = pq.ParquetFile(scores_path)
    keys = set()
    for batch in pf.iter_batches(batch_size=5000, columns=["product_key"]):
        df = batch.to_pandas()
        keys.update(df["product_key"].dropna().tolist())
    return keys

def load_product_keys_from_db():
    """For actual import: load from BL_PRODUCTS."""
    import oracledb
    conn = oracledb.connect(user="hr", password="hr", dsn="localhost:1521/xepdb1")
    cur = conn.cursor()
    cur.execute("SELECT PRODUCT_KEY, PRODUCT_ID FROM BL_PRODUCTS")
    result = {row[0]: row[1] for row in cur.fetchall()}
    cur.close()
    conn.close()
    return result

# ============================================================
# Main
# ============================================================

def main():
    parser = argparse.ArgumentParser(description="Full Review Import")
    parser.add_argument("--dry-run", action="store_true", help="Analyze only, no DB writes")
    parser.add_argument("--limit", type=int, default=None, help="Max rows to process")
    args = parser.parse_args()

    SEP = "=" * 60
    print(SEP)
    print("BeautyLens Full Review Import")
    print(f"Mode: {'DRY-RUN' if args.dry_run else 'ACTUAL IMPORT'}")
    print(f"Start: {datetime.datetime.now()}")
    print(SEP)

    # --------------------------------------------------------
    # Load product key set
    # --------------------------------------------------------
    print("\n[STEP 1] Loading product key map...")
    if args.dry_run:
        print("  (dry-run: loading from product_recommendation_scores.parquet)")
        product_key_set = load_product_keys_from_parquet()
        product_map = None
        print(f"  Product keys available: {len(product_key_set):,}")
    else:
        print("  (actual: loading from BL_PRODUCTS)")
        product_map = load_product_keys_from_db()
        product_key_set = set(product_map.keys())
        if not product_key_set:
            print("[ERROR] BL_PRODUCTS is empty. Run import_products_full.py first.")
            sys.exit(1)
        print(f"  Product keys in DB: {len(product_key_set):,}")

    # --------------------------------------------------------
    # Validate source file
    # --------------------------------------------------------
    print(f"\n[STEP 2] Validating {PARQUET_PATH}")
    pf = pq.ParquetFile(PARQUET_PATH)
    all_cols = pf.schema_arrow.names
    print(f"  Columns ({len(all_cols)}): {all_cols}")

    missing = [c for c in REQUIRED_COLS if c not in all_cols]
    if missing:
        print(f"[ERROR] Missing required columns: {missing}")
        sys.exit(1)
    print(f"  Required columns verified.")

    # --------------------------------------------------------
    # Process in batches
    # --------------------------------------------------------
    print(f"\n[STEP 3] Processing reviews (batch_size={BATCH_SIZE:,})...")

    total_rows           = 0
    skip_no_product      = 0
    skip_no_review_text  = 0
    skip_other           = 0
    platform_counts      = collections.Counter()
    sentiment_counts     = collections.Counter()
    matched_rows         = 0
    fail_samples         = []
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

            product_key = safe_str(row.get("product_key"), 80)
            review_text = safe_str(row.get("review_text"))
            platform    = safe_str(row.get("platform"), 20)
            sentiment   = safe_str(row.get("sentiment_label"), 10)

            if platform:
                platform_counts[platform] += 1
            if sentiment:
                sentiment_counts[sentiment] += 1

            if not product_key or product_key not in product_key_set:
                skip_no_product += 1
                continue

            if not review_text:
                skip_no_review_text += 1
                continue

            matched_rows += 1

            if not args.dry_run:
                internal_pid = product_map.get(product_key)
                review_id_raw = safe_str(row.get("review_id"), 50)
                rating        = safe_float(row.get("rating"))
                review_date   = safe_date(row.get("review_date"))
                skin_type     = safe_str(row.get("skin_type"), 100)
                skin_concern  = safe_str(row.get("skin_concern"), 200)
                sentiment_id  = safe_int(row.get("sentiment_id"))
                try:
                    cursor.execute(INSERT_SQL, (
                        internal_pid, review_id_raw, rating,
                        review_text, review_date,
                        skin_type, skin_concern,
                        sentiment, sentiment_id,
                    ))
                    success += 1
                    if success % COMMIT_EVERY == 0:
                        conn.commit()
                except Exception as e:
                    import oracledb as _odb
                    if isinstance(e, _odb.IntegrityError):
                        skip_other += 1
                    else:
                        fail += 1
                        if len(fail_samples) < 5:
                            fail_samples.append(f"key={product_key}: {e}")

            if args.limit and total_rows >= args.limit:
                break

        if args.limit and total_rows >= args.limit:
            break

    skipped_total = skip_no_product + skip_no_review_text + skip_other
    match_rate = matched_rows / total_rows * 100 if total_rows > 0 else 0

    print(f"\n{'--- DRY-RUN REPORT ---' if args.dry_run else '--- IMPORT COMPLETE ---'}")
    print(f"  Total rows processed:        {total_rows:,}")
    print(f"  Matched (product found):     {matched_rows:,}  ({match_rate:.1f}%)")
    print(f"  Skip (no product match):     {skip_no_product:,}")
    print(f"  Skip (no review_text):       {skip_no_review_text:,}")
    print(f"  Skip (duplicate/other):      {skip_other:,}")
    print(f"\n  Platform distribution:")
    for plat, cnt in sorted(platform_counts.items(), key=lambda x: -x[1]):
        print(f"    {plat}: {cnt:,}")
    print(f"\n  Sentiment distribution (all rows):")
    for sent, cnt in sorted(sentiment_counts.items(), key=lambda x: -x[1]):
        print(f"    {sent}: {cnt:,}")

    if not args.dry_run:
        print(f"\n  Inserted: {success:,}  Failed: {fail:,}  Skipped(dup): {skip_other:,}")
        if fail_samples:
            print(f"  Fail samples: {fail_samples}")
        conn.commit()  # final commit for remaining rows
        status = "success" if fail == 0 else ("partial" if success > 0 else "failed")
        log_import(cursor, conn,
                   "service_reviews.parquet (full)",
                   total_rows, success, fail, skipped_total,
                   status, "; ".join(fail_samples[:30]), start_imp)
        cursor.close()
        conn.close()
    else:
        print(f"\n  [DRY-RUN] No data was written to DB.")
        if args.limit:
            total_full = pq.ParquetFile(PARQUET_PATH).metadata.num_rows
            ratio = matched_rows / total_rows if total_rows > 0 else 0
            est_match = int(total_full * ratio)
            print(f"  Note: processed only {total_rows:,} of {total_full:,} rows (--limit)")
            print(f"  Estimated full match if no limit: ~{est_match:,}")

    print(f"\nDone: {datetime.datetime.now()}")
    print(SEP)

if __name__ == "__main__":
    main()
