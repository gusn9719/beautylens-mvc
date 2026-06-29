"""
propose_display_names.py
BL_PRODUCTS 전체 상품명 displayName 정제 후보 생성
DB 수정 없음 / API 수정 없음 / dry-run only
"""
import sys, re, csv, os
sys.stdout.reconfigure(encoding='utf-8')
import oracledb

# ─── 설정 ────────────────────────────────────────────────────────
DB_DSN  = "localhost:1521/xepdb1"
DB_USER = "hr"
DB_PASS = "hr"

PROJECT_ROOT = r"D:\Lecture\spring-workspace\beautylens-mvc"
OUT_CSV  = os.path.join(PROJECT_ROOT, "docs", "display_name_candidates.csv")

# ─── 정제 규칙 상수 ──────────────────────────────────────────────

# Rule 1-A: 앞 대괄호에 이 키워드가 있으면 → safe 제거
SAFE_LEADING_KW = [
    '올영픽', '하루특가', '타임어택', '타임 어택', '무신사 단독', '무신사단독',
    '사은품', '포켓몬', '콜라보', '런칭', '에디션', '한정판', '시간한정',
    '올영', '올리브영픽', '위클리픽', '오늘의픽',
]

# Rule 1-B: 앞 대괄호에 이 키워드 → review (조건부 제거)
REVIEW_LEADING_KW = [
    'PICK', 'NEW', '단독', '기획', '증정', '1+1', 'SET', '세일', '특가',
    '한정', '할인', '이벤트', '프로모션', '쿠폰', '무료배송',
]

# 앞 대괄호에 수량/용량 정보가 있으면 제거 금지 (제품 정보)
PRODUCT_IN_BRACKET_RE = re.compile(
    r'\d+\s*(ml|g|mg|L|oz|매|개|입|팩|pack|PACK|pcs)',
    re.IGNORECASE
)

# Rule 2: 뒤쪽 증정품 키워드
GIFT_WORDS = [
    '키링', '스티커', '파우치', '거울', '타올', '타월', '케이스', '아크릴',
    '포스트잇', '마켓백', '브러쉬', '굿즈', '미니어처', '액정클리너',
    '머리핀', '헤어핀', '클리너', '틴케이스', '이어폰', '볼펜', '부채',
    '에코백', '집게', '집게핀', '클립', '쿠션',
]

# 볼륨/용량 패턴 (정제 전후 비교용)
VOLUME_RE = re.compile(r'\d+\s*(ml|ML|g|mg|L|oz|매|개|입|팩)', re.IGNORECASE)


# ─── 정제 함수 ────────────────────────────────────────────────────

def _risk_max(a: str, b: str) -> str:
    order = {'safe': 0, 'review': 1, 'unsafe': 2}
    return a if order[a] >= order[b] else b


def process_leading_brackets(name: str):
    """
    앞 대괄호 블록 처리
    Returns (new_name, removed_list, change_types, risk)
    """
    removed, changes, risk = [], [], 'safe'

    while True:
        m = re.match(r'^\[([^\]]+)\]\s*', name)
        if not m:
            break
        content = m.group(1)

        # 수량/용량 정보 → 중단 (제거 불가)
        if PRODUCT_IN_BRACKET_RE.search(content):
            break

        # Safe 제거
        if any(kw.lower() in content.lower() for kw in SAFE_LEADING_KW):
            removed.append(f'[{content}]')
            changes.append('REMOVE_LEADING_PROMO_BRACKET')
            name = name[m.end():].strip()
            continue

        # Review 제거
        if any(kw.upper() in content.upper() for kw in REVIEW_LEADING_KW):
            removed.append(f'[{content}]')
            changes.append('REMOVE_LEADING_PROMO_BRACKET')
            risk = _risk_max(risk, 'review')
            name = name[m.end():].strip()
            continue

        # 알 수 없는 대괄호 → 중단
        break

    return name, removed, changes, risk


def process_trailing_gifts(name: str):
    """
    뒤쪽 증정품 문구 제거
    Returns (new_name, removed_list, change_types, risk)
    """
    removed, changes, risk = [], [], 'safe'

    # 패턴 A: +(증정) xxx at end
    # e.g., "+(증정) 고체향수키링 레몬비누향", "+(증정품)포함"
    m = re.search(
        r'\s*\+\s*\(?증정[품]?\)?\s+[가-힣A-Za-z0-9\s()]+$',
        name
    )
    if m:
        suffix = m.group(0).strip()
        has_vol = bool(VOLUME_RE.search(suffix))
        removed.append(suffix)
        changes.append('REMOVE_GIFT_SUFFIX')
        risk = _risk_max(risk, 'review' if has_vol else 'safe')
        name = name[:m.start()].strip()

    # 패턴 B: (+xxx) at end where xxx contains gift keywords OR 증정
    while True:
        m = re.search(r'\s*\(\s*\+([^)]+)\)\s*$', name)
        if not m:
            break
        inner = m.group(1).strip()
        has_gift  = any(gw in inner for gw in GIFT_WORDS)
        has_증정  = '증정' in inner
        has_vol   = bool(VOLUME_RE.search(inner))

        if has_gift or has_증정:
            suffix = m.group(0).strip()
            removed.append(suffix)
            changes.append('REMOVE_GIFT_SUFFIX')
            # 볼륨 정보가 섞여 있으면 주의
            risk = _risk_max(risk, 'review' if (has_vol and not has_gift) else 'safe')
            name = name[:m.start()].strip()
        else:
            break  # 증정품 아닌 괄호는 건드리지 않음

    return name, removed, changes, risk


def clean_product_name(orig: str):
    """
    원본 productName → displayName 후보 생성
    Returns: (display_name, changed, change_type, risk_level, reason)
    """
    name = orig.strip()
    all_removed, all_changes = [], []
    risk = 'safe'

    # Rule 1: 앞 대괄호
    name, rem1, chg1, r1 = process_leading_brackets(name)
    all_removed.extend(rem1); all_changes.extend(chg1)
    risk = _risk_max(risk, r1)

    # Rule 2: 뒤쪽 증정품
    name, rem2, chg2, r2 = process_trailing_gifts(name)
    all_removed.extend(rem2); all_changes.extend(chg2)
    risk = _risk_max(risk, r2)

    # Rule 3: 공백 정규화
    norm = re.sub(r'  +', ' ', name).strip()
    if norm != name:
        all_changes.append('NORMALIZE_SPACING')
        name = norm

    # Post-check: 8자 미만이면 원본 유지
    if len(name) < 8:
        return (orig.strip(), False, 'NEEDS_REVIEW', 'unsafe',
                f"정제 후 {len(name)}자 → 원본 유지")

    # Post-check: 볼륨 정보 소실 여부
    orig_vols = VOLUME_RE.findall(orig)
    new_vols  = VOLUME_RE.findall(name)
    if orig_vols and not new_vols:
        risk = _risk_max(risk, 'review')
        all_removed.append('[볼륨 소실 가능성]')

    # 변경 없음
    if name == orig.strip():
        return name, False, 'NO_CHANGE', 'safe', ''

    # changeType
    unique_ch = list(dict.fromkeys(all_changes))  # order-preserving dedup
    if not unique_ch:
        change_type = 'NO_CHANGE'
    elif len(unique_ch) == 1:
        change_type = unique_ch[0]
    else:
        change_type = 'MULTI_RULE'

    reason = '; '.join(all_removed) if all_removed else ''
    return name, True, change_type, risk, reason


# ─── 메인 ────────────────────────────────────────────────────────

conn = oracledb.connect(user=DB_USER, password=DB_PASS, dsn=DB_DSN)
cur  = conn.cursor()

print("BL_PRODUCTS 조회 중...")
cur.execute("""
    SELECT PRODUCT_ID, PLATFORM, BRAND, PRODUCT_NAME,
           RECOMMENDATION_SCORE, BASE_SKIN_TYPE
    FROM BL_PRODUCTS
    ORDER BY RECOMMENDATION_SCORE DESC NULLS LAST, PRODUCT_ID
""")
rows = cur.fetchall()
print(f"총 {len(rows)}개 상품 조회 완료\n")

results = []
for pid, plat, brand, pname_raw, score, bst in rows:
    pname = str(pname_raw) if pname_raw else ''
    display, changed, chtype, risk, reason = clean_product_name(pname)
    results.append({
        'productId':            pid,
        'platform':             plat or '',
        'brand':                brand or '',
        'productName':          pname,
        'displayNameCandidate': display,
        'changed':              changed,
        'changeType':           chtype,
        'riskLevel':            risk,
        'reason':               reason,
        '_score':               score or 0,
        '_bst':                 bst or '',
    })

cur.close()
conn.close()

# ─── 통계 ─────────────────────────────────────────────────────────
total      = len(results)
changed_r  = [r for r in results if r['changed']]
no_change  = [r for r in results if not r['changed']]
safe_r     = [r for r in changed_r if r['riskLevel'] == 'safe']
review_r   = [r for r in changed_r if r['riskLevel'] == 'review']
unsafe_r   = [r for r in results   if r['riskLevel'] == 'unsafe']

SEP = "=" * 70
print(SEP)
print("정제 결과 통계")
print(SEP)
print(f"전체 상품:           {total}")
print(f"변경 없음(NO_CHANGE):{len(no_change)}")
print(f"변경 후보:           {len(changed_r)}")
print(f"  safe:   {len(safe_r)}")
print(f"  review: {len(review_r)}")
print(f"unsafe:   {len(unsafe_r)}")

by_type = {}
for r in changed_r:
    ct = r['changeType']
    by_type[ct] = by_type.get(ct, 0) + 1
print("\n변경 유형별:")
for ct, cnt in sorted(by_type.items(), key=lambda x: -x[1]):
    print(f"  {ct:45s}: {cnt}")

# ─── 샘플 출력 1: 전체 변경 후보 상위 50개 (score 순) ─────────────
print(f"\n{SEP}")
print("변경 후보 상위 50개 (추천 점수 내림차순)")
print(SEP)
for i, r in enumerate(changed_r[:50], 1):
    flag = {'safe':'✅','review':'⚠️','unsafe':'❌'}.get(r['riskLevel'],'?')
    print(f"{i:3d}. {flag}[{r['changeType']}] id={r['productId']} {r['platform']} score={r['_score']:.1f}")
    print(f"     원본: {r['productName'][:78]}")
    print(f"     정제: {r['displayNameCandidate'][:78]}")
    if r['reason']:
        print(f"     사유: {r['reason'][:78]}")

# ─── 샘플 출력 2: unsafe 전체 ────────────────────────────────────
print(f"\n{SEP}")
print(f"unsafe 전체 ({len(unsafe_r)}건)")
print(SEP)
for r in unsafe_r:
    print(f"  id={r['productId']} | {r['productName'][:70]}")
    print(f"  → {r['reason']}")

# ─── 샘플 출력 3: review 상위 50개 ──────────────────────────────
print(f"\n{SEP}")
print(f"review 후보 상위 50개 (score 내림차순)")
print(SEP)
for i, r in enumerate(review_r[:50], 1):
    print(f"{i:3d}. id={r['productId']} score={r['_score']:.1f} {r['platform']}")
    print(f"     원본: {r['productName'][:78]}")
    print(f"     정제: {r['displayNameCandidate'][:78]}")
    print(f"     사유: {r['reason'][:78]}")

# ─── 샘플 출력 4: 상품명 길이 상위 30개 ─────────────────────────
print(f"\n{SEP}")
print("상품명 길이 상위 30개")
print(SEP)
by_len = sorted(results, key=lambda r: len(r['productName']), reverse=True)[:30]
for i, r in enumerate(by_len, 1):
    print(f"{i:3d}. [{r['riskLevel']}] id={r['productId']} len={len(r['productName'])}")
    print(f"     원본: {r['productName'][:78]}")
    print(f"     정제: {r['displayNameCandidate'][:78]}")

# ─── 샘플 출력 5: 유사도 1.0 후보 (아이디얼포맨) ──────────────────
print(f"\n{SEP}")
print("유사도 1.0 동일 상품 후보 (아이디얼포맨 검색)")
print(SEP)
idealformen = [r for r in results if '아이디얼포맨' in r['productName'] or '아이디얼포맨' in r['brand']]
for r in idealformen:
    print(f"  id={r['productId']} {r['platform']} | {r['productName'][:70]}")
    print(f"  → 정제: {r['displayNameCandidate'][:70]}")

# ─── CSV 저장 ─────────────────────────────────────────────────────
with open(OUT_CSV, 'w', newline='', encoding='utf-8-sig') as f:
    fieldnames = ['productId','platform','brand','productName',
                  'displayNameCandidate','changed','changeType','riskLevel','reason']
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    for r in results:
        writer.writerow({k: r[k] for k in fieldnames})

print(f"\n{SEP}")
print(f"CSV 저장 완료: {OUT_CSV}")
print(f"총 {total}행 (헤더 제외)")
print(SEP)
