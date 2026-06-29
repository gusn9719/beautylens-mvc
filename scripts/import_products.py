"""
BeautyLens - Product Import Script
Sources: product_recommendation_scores_preview.csv (95 rows)
         product_skin_aggregates_preview.csv (200 rows)
Target:  BL_PRODUCTS (Oracle XE, hr/hr, xepdb1)
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

CSV_SCORES = r"D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\product_recommendation_scores_preview.csv"
CSV_AGG    = r"D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\product_skin_aggregates_preview.csv"

REQUIRED_COLS = ["product_key", "product_name", "platform", "product_id"]

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
        return None if (f != f) else f   # NaN check
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

def row_to_params(row, has_rec_cols=True):
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
        safe_str(row.get("caution_level"), 40) if has_rec_cols else "normal",
        safe_float(row.get("recommendation_score")) if has_rec_cols else None,
        safe_str(row.get("recommendation_tier"), 40) if has_rec_cols else None,
        safe_str(row.get("evidence_level"), 40) if has_rec_cols else None,
    )

# ============================================================
# Main
# ============================================================
def main():
    start_all = datetime.datetime.now()
    print("=" * 60)
    print("BeautyLens Product Import")
    print(f"Start: {start_all}")
    print("=" * 60)

    conn = oracledb.connect(user=DB_USER, password=DB_PASSWORD, dsn=DB_DSN)
    cursor = conn.cursor()

    # --------------------------------------------------------
    # 1. product_recommendation_scores_preview.csv
    # --------------------------------------------------------
    print("\n[STEP 1] product_recommendation_scores_preview.csv")
    df_scores = read_csv(CSV_SCORES)
    validate_columns(df_scores, REQUIRED_COLS, "scores")

    total_s = len(df_scores)
    success_s = fail_s = skipped_s = 0
    fail_reasons_s = []
    start_s = datetime.datetime.now()

    existing_keys = set()

    for i, row in df_scores.iterrows():
        pk = safe_str(row.get("product_key"), 80)
        pn = safe_str(row.get("product_name"), 500)
        if not pk:
            skipped_s += 1
            fail_reasons_s.append(f"row {i}: product_key missing")
            continue
        if not pn:
            skipped_s += 1
            fail_reasons_s.append(f"row {i}: product_name missing (key={pk})")
            continue
        try:
            cursor.execute(INSERT_SQL, row_to_params(row, has_rec_cols=True))
            conn.commit()
            existing_keys.add(pk)
            success_s += 1
        except oracledb.IntegrityError:
            skipped_s += 1
            fail_reasons_s.append(f"row {i}: duplicate key={pk}")
        except Exception as e:
            fail_s += 1
            fail_reasons_s.append(f"row {i}: {e}")

    status_s = "success" if fail_s == 0 else ("partial" if success_s > 0 else "failed")
    log_import(cursor, conn, "product_recommendation_scores_preview.csv",
               total_s, success_s, fail_s, skipped_s, status_s,
               "; ".join(fail_reasons_s[:50]), start_s)

    print(f"  total={total_s}  success={success_s}  fail={fail_s}  skipped={skipped_s}")
    if fail_reasons_s[:5]:
        print(f"  sample reasons: {fail_reasons_s[:5]}")

    # --------------------------------------------------------
    # 2. product_skin_aggregates_preview.csv
    # --------------------------------------------------------
    print("\n[STEP 2] product_skin_aggregates_preview.csv")
    df_agg = read_csv(CSV_AGG)
    validate_columns(df_agg, REQUIRED_COLS, "aggregates")

    total_a = len(df_agg)
    success_a = fail_a = skipped_a = 0
    fail_reasons_a = []
    start_a = datetime.datetime.now()

    for i, row in df_agg.iterrows():
        pk = safe_str(row.get("product_key"), 80)
        pn = safe_str(row.get("product_name"), 500)
        if not pk:
            skipped_a += 1
            fail_reasons_a.append(f"row {i}: product_key missing")
            continue
        if not pn:
            skipped_a += 1
            fail_reasons_a.append(f"row {i}: product_name missing (key={pk})")
            continue
        if pk in existing_keys:
            skipped_a += 1
            continue
        try:
            cursor.execute(INSERT_SQL, row_to_params(row, has_rec_cols=False))
            conn.commit()
            existing_keys.add(pk)
            success_a += 1
        except oracledb.IntegrityError:
            skipped_a += 1
            fail_reasons_a.append(f"row {i}: duplicate key={pk}")
        except Exception as e:
            fail_a += 1
            fail_reasons_a.append(f"row {i}: {e}")

    status_a = "success" if fail_a == 0 else ("partial" if success_a > 0 else "failed")
    log_import(cursor, conn, "product_skin_aggregates_preview.csv",
               total_a, success_a, fail_a, skipped_a, status_a,
               "; ".join(fail_reasons_a[:50]), start_a)

    print(f"  total={total_a}  success={success_a}  fail={fail_a}  skipped={skipped_a}")
    if fail_reasons_a[:5]:
        print(f"  sample reasons: {fail_reasons_a[:5]}")

    # --------------------------------------------------------
    # Final summary
    # --------------------------------------------------------
    cursor.execute("SELECT COUNT(*) FROM BL_PRODUCTS")
    total_in_db = cursor.fetchone()[0]

    cursor.close()
    conn.close()

    print("\n" + "=" * 60)
    print(f"[DONE] BL_PRODUCTS total in DB: {total_in_db}")
    print(f"  scores: {success_s} inserted, {skipped_s} skipped, {fail_s} failed")
    print(f"  agg:    {success_a} inserted, {skipped_a} skipped, {fail_a} failed")
    print("=" * 60)

if __name__ == "__main__":
    main()
