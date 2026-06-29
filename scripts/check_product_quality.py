"""
BeautyLens - Product Quality Check Script
BL_PRODUCTS import 후 상품명 품질 검수 및 중복 의심 후보 탐지.

Usage:
  python check_product_quality.py

Output:
  - Console (full report)
  - docs/06_product_quality_report.md
"""

import sys
import re
import difflib
import datetime
import collections
from pathlib import Path

import oracledb
import pandas as pd

DB_DSN  = "localhost:1521/xepdb1"
DB_USER = "hr"
DB_PASS = "hr"

DOCS_DIR = Path(__file__).resolve().parent.parent / "docs"
REPORT_PATH = DOCS_DIR / "06_product_quality_report.md"

PROMO_KEYWORDS = [
    '증정', '기획', '세트', '1+1', '2개', '3개', '4개', '5개',
    '단독', '한정', '특별', '대용량', '리필', '올리브영', '무신사',
    '쿠폰', '무료배송', '특가', '할인', '이벤트', '프로모션', '묶음',
    '번들', '패키지', '기프트', '사은품',
]

SIMILARITY_THRESHOLD_SAME_BRAND   = 0.65
SIMILARITY_THRESHOLD_NO_BRAND     = 0.80
MAX_PAIRS_PER_SECTION = 30
MAX_PAIRS_PER_BRAND   = 10  # 동일 브랜드 내 상위 N쌍만 기록

SEP = "=" * 70

# ============================================================
# DB
# ============================================================

def load_products():
    conn = oracledb.connect(user=DB_USER, password=DB_PASS, dsn=DB_DSN)
    cur  = conn.cursor()
    cur.execute("""
        SELECT
            PRODUCT_ID,
            PLATFORM,
            PRODUCT_KEY,
            PRODUCT_NAME,
            BRAND,
            CATEGORY,
            AVG_RATING,
            TOTAL_REVIEW_COUNT,
            RECOMMENDATION_SCORE,
            CAUTION_LEVEL,
            BASE_SKIN_TYPE
        FROM BL_PRODUCTS
        ORDER BY PRODUCT_ID
    """)
    cols = [d[0] for d in cur.description]
    rows = cur.fetchall()
    cur.close()
    conn.close()
    df = pd.DataFrame(rows, columns=cols)
    return df

# ============================================================
# Checks
# ============================================================

def check_platform_dist(df):
    dist = df['PLATFORM'].value_counts().to_dict()
    return dist

def check_null_fields(df):
    return {
        'brand_null':        int(df['BRAND'].isna().sum()),
        'product_name_null': int(df['PRODUCT_NAME'].isna().sum()),
        'category_null':     int(df['CATEGORY'].isna().sum()),
        'score_null':        int(df['RECOMMENDATION_SCORE'].isna().sum()),
        'skin_type_null':    int(df['BASE_SKIN_TYPE'].isna().sum()),
    }

def check_name_lengths(df):
    df2 = df.copy()
    df2['name_len'] = df2['PRODUCT_NAME'].fillna('').str.len()
    top30 = df2.nlargest(30, 'name_len')[
        ['PRODUCT_ID','PLATFORM','BRAND','PRODUCT_NAME','name_len']
    ].values.tolist()
    stats = {
        'min':    int(df2['name_len'].min()),
        'max':    int(df2['name_len'].max()),
        'mean':   float(df2['name_len'].mean()),
        'median': float(df2['name_len'].median()),
        '>100':   int((df2['name_len'] > 100).sum()),
        '>200':   int((df2['name_len'] > 200).sum()),
    }
    return stats, top30

def check_promo_keywords(df):
    hits = collections.defaultdict(list)
    flagged_ids = set()

    for _, row in df.iterrows():
        name = str(row['PRODUCT_NAME'] or '')
        matched_kw = [kw for kw in PROMO_KEYWORDS if kw in name]
        if matched_kw:
            flagged_ids.add(row['PRODUCT_ID'])
            for kw in matched_kw:
                hits[kw].append(row['PRODUCT_ID'])

    flagged_df = df[df['PRODUCT_ID'].isin(flagged_ids)].copy()
    flagged_df['matched_keywords'] = flagged_df.apply(
        lambda r: [kw for kw in PROMO_KEYWORDS if kw in str(r['PRODUCT_NAME'] or '')],
        axis=1
    )
    keyword_counts = {kw: len(ids) for kw, ids in hits.items()}
    return len(flagged_ids), keyword_counts, flagged_df

def find_similar_pairs(df):
    """
    브랜드별 그룹핑 후 difflib.SequenceMatcher로 유사 상품 탐지.
    - cross_pairs: 플랫폼이 다른 유사 쌍
    - same_pairs:  같은 플랫폼 내 유사 쌍
    """
    cross_pairs = []
    same_pairs  = []

    records = df[['PRODUCT_ID','PLATFORM','BRAND','PRODUCT_NAME','PRODUCT_KEY']].to_dict('records')

    # brand별 그룹화
    brand_groups = collections.defaultdict(list)
    for r in records:
        key = (r['BRAND'] or '').strip() or '__NOBRAND__'
        brand_groups[key].append(r)

    for brand, group in brand_groups.items():
        if len(group) < 2:
            continue

        threshold = SIMILARITY_THRESHOLD_NO_BRAND if brand == '__NOBRAND__' else SIMILARITY_THRESHOLD_SAME_BRAND
        brand_cross = []
        brand_same  = []

        for i in range(len(group)):
            for j in range(i + 1, len(group)):
                name_i = str(group[i]['PRODUCT_NAME'] or '')[:200]
                name_j = str(group[j]['PRODUCT_NAME'] or '')[:200]
                if len(name_i) < 5 or len(name_j) < 5:
                    continue

                # quick_ratio 먼저 (fast O(n))
                sm = difflib.SequenceMatcher(None, name_i, name_j)
                if sm.quick_ratio() < threshold:
                    continue
                ratio = sm.ratio()
                if ratio < threshold:
                    continue

                pair = {
                    'ratio':    round(ratio, 3),
                    'brand':    brand,
                    'plat1':    group[i]['PLATFORM'],
                    'name1':    name_i[:80],
                    'key1':     group[i]['PRODUCT_KEY'],
                    'plat2':    group[j]['PLATFORM'],
                    'name2':    name_j[:80],
                    'key2':     group[j]['PRODUCT_KEY'],
                }
                if group[i]['PLATFORM'] != group[j]['PLATFORM']:
                    brand_cross.append(pair)
                else:
                    brand_same.append(pair)

        # 브랜드별 상위 N쌍만
        brand_cross.sort(key=lambda x: -x['ratio'])
        brand_same.sort(key=lambda x: -x['ratio'])
        cross_pairs.extend(brand_cross[:MAX_PAIRS_PER_BRAND])
        same_pairs.extend(brand_same[:MAX_PAIRS_PER_BRAND])

    cross_pairs.sort(key=lambda x: -x['ratio'])
    same_pairs.sort(key=lambda x: -x['ratio'])
    return cross_pairs, same_pairs

# ============================================================
# Report generation
# ============================================================

def build_report(df, run_at):
    lines = []

    def h1(t):   lines.append(f"\n# {t}\n")
    def h2(t):   lines.append(f"\n## {t}\n")
    def h3(t):   lines.append(f"\n### {t}\n")
    def row(s):  lines.append(s)
    def blank(): lines.append("")

    h1("06. 상품명 품질 검수 보고서 (Product Quality Report)")
    row(f"생성 일시: {run_at}")
    row(f"검수 대상: BL_PRODUCTS (전체 {len(df):,}개)")
    row("")
    row("---")

    # ── 1. 전체 상품 수 & platform 분포 ──────────────────────
    h2("1. 전체 상품 수 및 Platform 분포")
    row(f"**총 상품 수**: {len(df):,}")
    blank()
    row("| Platform | 상품 수 | 비율 |")
    row("|---|---|---|")
    for plat, cnt in sorted(check_platform_dist(df).items(), key=lambda x: -x[1]):
        pct = cnt / len(df) * 100
        row(f"| {plat} | {cnt:,} | {pct:.1f}% |")
    blank()
    row("---")

    # ── 2. NULL 필드 현황 ─────────────────────────────────────
    h2("2. NULL 필드 현황")
    nulls = check_null_fields(df)
    row("| 필드 | NULL 수 | NULL 비율 |")
    row("|---|---|---|")
    fields = [
        ('BRAND', nulls['brand_null']),
        ('PRODUCT_NAME', nulls['product_name_null']),
        ('CATEGORY', nulls['category_null']),
        ('RECOMMENDATION_SCORE', nulls['score_null']),
        ('BASE_SKIN_TYPE', nulls['skin_type_null']),
    ]
    for fname, cnt in fields:
        pct = cnt / len(df) * 100
        row(f"| {fname} | {cnt:,} | {pct:.1f}% |")
    blank()
    row("---")

    # ── 3. 상품명 길이 분포 ──────────────────────────────────
    h2("3. 상품명 길이 분포")
    name_stats, top30_names = check_name_lengths(df)
    row("| 항목 | 값 |")
    row("|---|---|")
    row(f"| 최소 | {name_stats['min']} |")
    row(f"| 최대 | {name_stats['max']} |")
    row(f"| 평균 | {name_stats['mean']:.1f} |")
    row(f"| 중앙값 | {name_stats['median']:.1f} |")
    row(f"| 100자 초과 | {name_stats['>100']:,} |")
    row(f"| 200자 초과 | {name_stats['>200']:,} |")
    blank()

    h3("상품명 길이 상위 30개")
    row("| # | PRODUCT_ID | Platform | Brand | 상품명 (앞 80자) | 길이 |")
    row("|---|---|---|---|---|---|")
    for idx, (pid, plat, brand, name, nlen) in enumerate(top30_names, 1):
        brand_s = str(brand or '')[:20]
        name_s  = str(name  or '')[:80]
        row(f"| {idx} | {pid} | {plat} | {brand_s} | {name_s} | {int(nlen)} |")
    blank()
    row("---")

    # ── 4. 프로모션/이벤트 의심 상품명 ──────────────────────
    h2("4. 프로모션/이벤트 의심 상품명")
    n_flagged, kw_counts, flagged_df = check_promo_keywords(df)
    row(f"**검수 키워드**: {', '.join(PROMO_KEYWORDS)}")
    blank()
    row(f"**프로모션 의심 상품 수**: {n_flagged:,} / {len(df):,} ({n_flagged/len(df)*100:.1f}%)")
    blank()

    h3("키워드별 상품 수")
    row("| 키워드 | 상품 수 |")
    row("|---|---|")
    for kw, cnt in sorted(kw_counts.items(), key=lambda x: -x[1]):
        row(f"| {kw} | {cnt} |")
    blank()

    h3("프로모션 의심 상품 샘플 (최대 30개)")
    row("| # | PRODUCT_ID | Platform | Brand | 상품명 (앞 100자) | 매칭 키워드 |")
    row("|---|---|---|---|---|---|")
    for idx, (_, r) in enumerate(flagged_df.head(30).iterrows(), 1):
        name_s = str(r['PRODUCT_NAME'] or '')[:100]
        brand_s = str(r['BRAND'] or '')[:20]
        kws_s = ', '.join(r['matched_keywords'][:5])
        row(f"| {idx} | {r['PRODUCT_ID']} | {r['PLATFORM']} | {brand_s} | {name_s} | {kws_s} |")
    blank()
    row("---")

    # ── 5. 중복 의심 후보 ────────────────────────────────────
    h2("5. 중복 의심 상품 후보")
    row("*(자동 병합 없음. 사람이 검토 후 판단 필요.)*")
    blank()
    row(f"- 플랫폼 간 중복 기준: 동일 브랜드 내 상품명 유사도 ≥ {SIMILARITY_THRESHOLD_SAME_BRAND}")
    row(f"- 브랜드 미상(NULL) 기준: 상품명 유사도 ≥ {SIMILARITY_THRESHOLD_NO_BRAND}")
    blank()

    cross_pairs, same_pairs = find_similar_pairs(df)

    h3("5-1. 플랫폼 간 중복 의심 후보 (최대 30쌍)")
    row(f"총 발견: {len(cross_pairs)}쌍 (표시: {min(len(cross_pairs), MAX_PAIRS_PER_SECTION)}쌍)")
    blank()
    if cross_pairs:
        row("| # | 유사도 | 브랜드 | Platform1 | 상품명1 (앞 60자) | Platform2 | 상품명2 (앞 60자) |")
        row("|---|---|---|---|---|---|---|")
        for idx, p in enumerate(cross_pairs[:MAX_PAIRS_PER_SECTION], 1):
            brand_s = str(p['brand'] or '')[:20]
            row(f"| {idx} | {p['ratio']:.3f} | {brand_s} | {p['plat1']} | {p['name1'][:60]} | {p['plat2']} | {p['name2'][:60]} |")
    else:
        row("*플랫폼 간 중복 의심 후보 없음*")
    blank()

    h3("5-2. 같은 플랫폼 내 중복 의심 후보 (최대 30쌍)")
    row(f"총 발견: {len(same_pairs)}쌍 (표시: {min(len(same_pairs), MAX_PAIRS_PER_SECTION)}쌍)")
    blank()
    if same_pairs:
        row("| # | 유사도 | 브랜드 | Platform | 상품명1 (앞 60자) | 상품명2 (앞 60자) |")
        row("|---|---|---|---|---|---|")
        for idx, p in enumerate(same_pairs[:MAX_PAIRS_PER_SECTION], 1):
            brand_s = str(p['brand'] or '')[:20]
            row(f"| {idx} | {p['ratio']:.3f} | {brand_s} | {p['plat1']} | {p['name1'][:60]} | {p['name2'][:60]} |")
    else:
        row("*같은 플랫폼 내 중복 의심 후보 없음*")
    blank()
    row("---")

    # ── 6. 종합 판단 ─────────────────────────────────────────
    h2("6. 종합 판단 및 권고")

    issues = []
    if nulls['brand_null'] > 0:
        issues.append(f"brand NULL: {nulls['brand_null']:,}개 ({nulls['brand_null']/len(df)*100:.1f}%) — 검색/필터 API에서 브랜드 필터 사용 시 누락")
    if nulls['product_name_null'] > 0:
        issues.append(f"product_name NULL: {nulls['product_name_null']:,}개 — UI 표시 오류 발생 가능")
    if n_flagged > 0:
        issues.append(f"프로모션 의심 상품: {n_flagged:,}개 ({n_flagged/len(df)*100:.1f}%) — UI 표시 전 정제 필요 여부 검토 권장")
    if name_stats['>100'] > 0:
        issues.append(f"100자 초과 상품명: {name_stats['>100']:,}개 — UI 줄임 처리(ellipsis) 필요")
    if cross_pairs:
        issues.append(f"플랫폼 간 중복 의심: {len(cross_pairs)}쌍 — 동일 상품 여부 수동 확인 후 병합 여부 결정")
    if same_pairs:
        issues.append(f"같은 플랫폼 내 중복 의심: {len(same_pairs)}쌍 — 컬러/용량 변형 vs 실제 중복 판별 필요")

    if issues:
        row("**발견된 품질 이슈**:")
        for iss in issues:
            row(f"- {iss}")
    else:
        row("**품질 이슈 없음**")
    blank()

    need_clean = n_flagged > 0 or name_stats['>100'] > 0 or nulls['product_name_null'] > 0
    row(f"**자동 정제 필요 여부**: {'⚠️ 필요 — 다음 단계(7차)에서 displayName 정제 검토 권장' if need_clean else '✅ 불필요'}")
    blank()
    row("**현재 단계 처리 범위**: 보고서 생성만. DB PRODUCT_NAME 직접 수정 없음. 중복 자동 병합 없음.")
    blank()
    row("---")
    row(f"*생성: {run_at}*")

    return "\n".join(lines)

# ============================================================
# Main
# ============================================================

def main():
    run_at = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(SEP)
    print("BeautyLens Product Quality Check")
    print(f"Start: {run_at}")
    print(SEP)

    print("\n[STEP 1] Loading BL_PRODUCTS from DB...")
    df = load_products()
    print(f"  Loaded {len(df):,} products.")

    print("\n[STEP 2] Running quality checks (similarity analysis may take ~30s)...")
    report_md = build_report(df, run_at)

    print("\n[STEP 3] Writing report...")
    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
    REPORT_PATH.write_text(report_md, encoding="utf-8")
    print(f"  Saved: {REPORT_PATH}")

    # Console summary (key numbers only)
    nulls         = check_null_fields(df)
    platform_dist = check_platform_dist(df)
    name_stats, _ = check_name_lengths(df)
    n_flagged, kw_counts, _ = check_promo_keywords(df)
    cross_pairs, same_pairs = find_similar_pairs(df)

    print(f"\n{'─'*50}")
    print("SUMMARY")
    print(f"{'─'*50}")
    print(f"  Total products:          {len(df):,}")
    for plat, cnt in sorted(platform_dist.items(), key=lambda x: -x[1]):
        print(f"    {plat}: {cnt:,}")
    print(f"  brand NULL:              {nulls['brand_null']:,} ({nulls['brand_null']/len(df)*100:.1f}%)")
    print(f"  product_name NULL:       {nulls['product_name_null']:,}")
    print(f"  name len > 100:          {name_stats['>100']:,}")
    print(f"  promo keyword flagged:   {n_flagged:,} ({n_flagged/len(df)*100:.1f}%)")
    print(f"  cross-platform similar:  {len(cross_pairs)} pairs")
    print(f"  same-platform similar:   {len(same_pairs)} pairs")
    print(f"{'─'*50}")
    print(f"\nReport written to: {REPORT_PATH}")
    print(f"Done: {datetime.datetime.now()}")
    print(SEP)

if __name__ == "__main__":
    main()
