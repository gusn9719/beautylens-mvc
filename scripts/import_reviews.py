"""
BeautyLens - Review Import Script
Source:  train_preview.csv (2000 rows)
Target:  BL_REVIEWS (Oracle XE, hr/hr, xepdb1)
Note:    product_key = platform + '::' + str(product_id)
         Matches to internal PRODUCT_ID via BL_PRODUCTS lookup.
"""

import sys
import datetime
import oracledb
import pandas as pd

# ============================================================
# Constants
# ============================================================
DB_USER     = "hr"
DB_PASSWORD = "hr"
DB_DSN      = "localhost:1521/xepdb1"

CSV_TRAIN = r"D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\train_preview.csv"

REQUIRED_COLS = ["review_id", "product_id", "platform", "review_text", "sentiment_label"]

# ============================================================
# Helpers
# ============================================================
def read_csv(path):
    for enc in ("utf-8-sig", "utf-8", "cp949"):
        try:
            df = pd.read_csv(path, encoding=enc)
            print(f"  [OK] {path} ({enc}, {len(df)} rows)")
            return df
        except Exception as e:
            print(f"  [WARN] {path} ({enc}): {e}")
    print(f"[ERROR] Cannot read {path}")
    sys.exit(1)

def validate_columns(df, required, filename):
    missing = [c for c in required if c not in df.columns]
    if missing:
        print(f"[ERROR] {filename}: missing required columns: {missing}")
        sys.exit(1)
    print(f"  [OK] Required columns verified: {required}")

def safe_float(v):
    if v is None:
        return None
    try:
        f = float(v)
        return None if (f != f) else f
    except (TypeError, ValueError):
        return None

def safe_int(v):
    if v is None:
        return None
    try:
        f = float(v)
        if f != f:
            return None
        return int(f)
    except (TypeError, ValueError):
        return None

def safe_str(v, max_len=None):
    if v is None:
        return None
    if isinstance(v, float) and v != v:
        return None
    s = str(v).strip()
    if s in ("", "nan", "NaN", "None"):
        return None
    return s[:max_len] if max_len else s

def safe_date(v):
    if v is None:
        return None
    if isinstance(v, float) and v != v:
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

def log_import(cursor, conn, filename, total, success, fail, skipped, status, error_msg, start):
    end = datetime.datetime.now()
    cursor.execute("""
        INSERT INTO BL_IMPORT_LOGS
            (LOG_ID, FILE_NAME, TOTAL_COUNT, SUCCESS_COUNT, FAIL_COUNT,
             SKIPPED_COUNT, STATUS, ERROR_MSG, START_TIME, END_TIME)
        VALUES
            (SEQ_BL_IMPORT_LOGS.NEXTVAL, :1, :2, :3, :4, :5, :6, :7, :8, :9)
    """, (filename, total, success, fail, skipped,
          status, error_msg[:3900] if error_msg else None,
          start, end))
    conn.commit()

# ============================================================
# INSERT SQL
# ============================================================
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

# ============================================================
# Main
# ============================================================
def main():
    start_all = datetime.datetime.now()
    print("=" * 60)
    print("BeautyLens Review Import")
    print(f"Start: {start_all}")
    print("=" * 60)

    conn = oracledb.connect(user=DB_USER, password=DB_PASSWORD, dsn=DB_DSN)
    cursor = conn.cursor()

    # --------------------------------------------------------
    # Build product_key → internal PRODUCT_ID map
    # --------------------------------------------------------
    print("\n[STEP 1] Loading product key map from BL_PRODUCTS...")
    cursor.execute("SELECT PRODUCT_KEY, PRODUCT_ID FROM BL_PRODUCTS")
    product_map = {row[0]: row[1] for row in cursor.fetchall()}
    print(f"  Loaded {len(product_map)} product keys")

    if not product_map:
        print("[ERROR] BL_PRODUCTS is empty. Run import_products.py first.")
        cursor.close()
        conn.close()
        sys.exit(1)

    # --------------------------------------------------------
    # Read train_preview.csv
    # --------------------------------------------------------
    print("\n[STEP 2] Reading train_preview.csv...")
    df = read_csv(CSV_TRAIN)
    validate_columns(df, REQUIRED_COLS, "train_preview.csv")

    total = len(df)
    success = fail = skipped_no_product = skipped_dup = skipped_other = 0
    fail_reasons = []
    start_imp = datetime.datetime.now()

    # --------------------------------------------------------
    # Insert reviews
    # --------------------------------------------------------
    print("\n[STEP 3] Inserting reviews...")
    for i, row in df.iterrows():
        # Build product_key
        platform   = safe_str(row.get("platform"), 20)
        product_id_raw = safe_str(row.get("product_id"), 50)

        # If product_key column exists in CSV, use it; otherwise construct
        if "product_key" in df.columns and safe_str(row.get("product_key")):
            product_key = safe_str(row.get("product_key"), 80)
        else:
            if not platform or not product_id_raw:
                skipped_other += 1
                fail_reasons.append(f"row {i}: platform or product_id missing")
                continue
            product_key = f"{platform}::{product_id_raw}"

        # Lookup internal PRODUCT_ID
        internal_product_id = product_map.get(product_key)
        if internal_product_id is None:
            skipped_no_product += 1
            if skipped_no_product <= 5:
                fail_reasons.append(f"row {i}: no product match for key={product_key}")
            continue

        # Build params
        review_id_raw = safe_str(row.get("review_id"), 50)
        rating        = safe_float(row.get("rating"))
        review_text   = safe_str(row.get("review_text"))
        review_date   = safe_date(row.get("review_date"))
        skin_type     = safe_str(row.get("skin_type"), 100)
        skin_concern  = safe_str(row.get("skin_concern"), 200)
        sentiment_label = safe_str(row.get("sentiment_label"), 10)
        sentiment_id    = safe_int(row.get("sentiment_id"))

        try:
            cursor.execute(INSERT_SQL, (
                internal_product_id,
                review_id_raw,
                rating,
                review_text,
                review_date,
                skin_type,
                skin_concern,
                sentiment_label,
                sentiment_id,
            ))
            conn.commit()
            success += 1
        except oracledb.IntegrityError:
            skipped_dup += 1
        except Exception as e:
            fail += 1
            fail_reasons.append(f"row {i}: {e}")

    skipped_total = skipped_no_product + skipped_dup + skipped_other
    status = "success" if fail == 0 else ("partial" if success > 0 else "failed")
    error_summary = (
        f"no_product={skipped_no_product}, dup={skipped_dup}, other={skipped_other}; "
        + "; ".join(fail_reasons[:30])
    )
    log_import(cursor, conn, "train_preview.csv",
               total, success, fail, skipped_total, status,
               error_summary, start_imp)

    # --------------------------------------------------------
    # Final summary
    # --------------------------------------------------------
    cursor.execute("SELECT COUNT(*) FROM BL_REVIEWS")
    total_in_db = cursor.fetchone()[0]

    cursor.execute("""
        SELECT SENTIMENT_LABEL, COUNT(*) CNT
        FROM BL_REVIEWS
        GROUP BY SENTIMENT_LABEL
        ORDER BY SENTIMENT_LABEL
    """)
    dist = cursor.fetchall()

    cursor.close()
    conn.close()

    print("\n" + "=" * 60)
    print(f"[DONE] BL_REVIEWS total in DB: {total_in_db}")
    print(f"  total rows processed : {total}")
    print(f"  success              : {success}")
    print(f"  skipped (no product) : {skipped_no_product}")
    print(f"  skipped (duplicate)  : {skipped_dup}")
    print(f"  skipped (other)      : {skipped_other}")
    print(f"  failed               : {fail}")
    print(f"  sentiment distribution:")
    for label, cnt in dist:
        print(f"    {label}: {cnt}")
    if fail_reasons:
        print(f"  sample fail reasons: {fail_reasons[:5]}")
    print("=" * 60)

if __name__ == "__main__":
    main()
