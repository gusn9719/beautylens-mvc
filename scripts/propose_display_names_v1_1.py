"""
propose_display_names_v1_1.py  — displayName 정제 v1.1
BL_PRODUCTS 전체 상품명 displayName 정제 후보 생성
DB 수정 없음 / API 수정 없음 / dry-run only

v1.0 대비 변경사항:
  - [SET] 보존 (REVIEW_LEADING_KW에서 제거)
  - 괄호 없는 끝 증정품 패턴 감지 (Rule 2-C)
  - (증정: xxx) 형식 감지 (Rule 2-D)
  - 마케팅 클레임 대괄호 감지 (Rule 1-C)
  - riskLevel 기준 강화
"""
import sys, re, csv, os
sys.stdout.reconfigure(encoding='utf-8')
import oracledb

VERSION = "v1.1"

# ─── 설정 ────────────────────────────────────────────────────────
DB_DSN  = "localhost:1521/xepdb1"
DB_USER = "hr"
DB_PASS = "hr"

PROJECT_ROOT = r"D:\Lecture\spring-workspace\beautylens-mvc"
OUT_CSV  = os.path.join(PROJECT_ROOT, "docs", "display_name_candidates_v1_1.csv")

# ─── 정제 규칙 상수 v1.1 ─────────────────────────────────────────

# Rule 1-A: 앞 대괄호 — safe 제거
SAFE_LEADING_KW = [
    '올영픽', '하루특가', '타임어택', '타임 어택', '무신사 단독', '무신사단독',
    '사은품', '포켓몬', '콜라보', '런칭', '에디션', '한정판', '시간한정',
    '올영', '올리브영픽', '위클리픽', '오늘의픽',
]

# Rule 1-B: 앞 대괄호 — review 제거
# v1.1 변경: SET 제거 (보존으로 변경)
REVIEW_LEADING_KW = [
    'PICK', 'NEW', '단독', '기획', '증정', '1+1', '세일', '특가',
    '한정', '할인', '이벤트', '프로모션', '쿠폰', '무료배송', '리뉴얼',
]

# Rule 1-C (신규): 마케팅 클레임 대괄호 — review 제거
MARKETING_CLAIM_KW = [
    '1위', '연속', '수상', '판매량', '재구매율', '랭킹',
]

# [SET] 보존 — 대소문자 무관하게 보존 (v1.1 핵심 변경)
SET_PRESERVE_RE = re.compile(r'^\[set\]$', re.IGNORECASE)

# 앞 대괄호에 수량/용량 → 제거 금지
PRODUCT_IN_BRACKET_RE = re.compile(
    r'\d+\s*(ml|g|mg|L|oz|매|개|입|팩|pack|PACK|pcs)',
    re.IGNORECASE
)

# Rule 2: 뒤쪽 증정품 키워드
GIFT_WORDS = [
    '키링', '스티커', '파우치', '거울', '타올', '타월', '케이스', '아크릴',
    '포스트잇', '마켓백', '브러쉬', '굿즈', '미니어처', '액정클리너',
    '머리핀', '헤어핀', '클리너', '틴케이스', '이어폰', '볼펜', '부채',
    '에코백', '집게', '집게핀', '클립', '쿠션', '샘플',
]

# 볼륨/용량 패턴
VOLUME_RE = re.compile(r'\d+\s*(ml|ML|g|mg|L|oz|매|개|입|팩)', re.IGNORECASE)


# ─── 정제 함수 v1.1 ──────────────────────────────────────────────

def _risk_max(a: str, b: str) -> str:
    order = {'safe': 0, 'review': 1, 'unsafe': 2}
    return a if order[a] >= order[b] else b


def _is_marketing_claim(content: str) -> bool:
    """
    대괄호 내용이 마케팅 클레임인지 판단 (Rule 1-C).
    숫자+위/연속/수상/랭킹 등 순위 클레임 감지.
    """
    for kw in MARKETING_CLAIM_KW:
        if kw in content:
            return True
    # e.g. "7년 연속 1위", "판매량 1위", "수상" 단독
    if re.search(r'\d+\s*년', content) and ('위' in content or '연속' in content):
        return True
    return False


def process_leading_brackets(name: str):
    """
    앞 대괄호 블록 처리 (v1.1)
    Returns (new_name, removed_list, change_types, risk)
    """
    removed, changes, risk = [], [], 'safe'

    while True:
        m = re.match(r'^\[([^\]]+)\]\s*', name)
        if not m:
            break
        content = m.group(1)

        # [SET] 보존 (v1.1 핵심 변경)
        if SET_PRESERVE_RE.match(content):
            break

        # 수량/용량 정보 → 중단 (제거 불가)
        if PRODUCT_IN_BRACKET_RE.search(content):
            break

        # Rule 1-A: Safe 제거
        if any(kw.lower() in content.lower() for kw in SAFE_LEADING_KW):
            removed.append(f'[{content}]')
            changes.append('REMOVE_LEADING_PROMO_BRACKET')
            name = name[m.end():].strip()
            continue

        # Rule 1-B: Review 제거
        if any(kw.upper() in content.upper() for kw in REVIEW_LEADING_KW):
            removed.append(f'[{content}]')
            changes.append('REMOVE_LEADING_PROMO_BRACKET')
            risk = _risk_max(risk, 'review')
            name = name[m.end():].strip()
            continue

        # Rule 1-C (신규): 마케팅 클레임 → review 제거
        if _is_marketing_claim(content):
            removed.append(f'[{content}]')
            changes.append('REMOVE_MARKETING_CLAIM_BRACKET')
            risk = _risk_max(risk, 'review')
            name = name[m.end():].strip()
            continue

        # 알 수 없는 대괄호 → 중단
        break

    return name, removed, changes, risk


def process_trailing_gifts(name: str):
    """
    뒤쪽 증정품 문구 제거 (v1.1 — 패턴 2-C, 2-D 추가)
    Returns (new_name, removed_list, change_types, risk)
    """
    removed, changes, risk = [], [], 'safe'

    # 패턴 A: +(증정) xxx at end
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
        has_gift = any(gw in inner for gw in GIFT_WORDS)
        has_증정 = '증정' in inner
        has_vol  = bool(VOLUME_RE.search(inner))

        if has_gift or has_증정:
            suffix = m.group(0).strip()
            removed.append(suffix)
            changes.append('REMOVE_GIFT_SUFFIX')
            risk = _risk_max(risk, 'review' if (has_vol and not has_gift) else 'safe')
            name = name[:m.start()].strip()
        else:
            break

    # 패턴 C (신규): +gift_word 괄호 없는 끝 증정품
    # e.g. "바디로션 200ml+헤어핀 굿즈", "세럼 50ml+키링"
    # 본품 볼륨 정보가 앞에 있어야 제거 후보
    while True:
        # 명확한 증정품 키워드로 끝나는 패턴
        gift_pattern = '|'.join(re.escape(g) for g in GIFT_WORDS)
        m = re.search(
            r'\s*\+\s*[가-힣A-Za-z0-9\s]*?(' + gift_pattern + r')\s*$',
            name
        )
        if not m:
            break
        suffix = m.group(0).strip()
        # 본품 볼륨이 남아 있는지 확인
        remaining = name[:m.start()].strip()
        has_main_vol = bool(VOLUME_RE.search(remaining))
        if not has_main_vol:
            break  # 본품 볼륨 없으면 건드리지 않음
        removed.append(suffix)
        changes.append('REMOVE_BARE_GIFT_SUFFIX')
        risk = _risk_max(risk, 'review')  # 괄호 없는 패턴은 항상 review
        name = remaining
        break  # 한 번만 적용

    # 패턴 D (신규): (증정: xxx) 형식
    # e.g. "(증정: 멀티 히알루론산 마스크 5매)"
    while True:
        m = re.search(r'\s*\(증정\s*:\s*[^)]+\)\s*$', name)
        if not m:
            break
        suffix = m.group(0).strip()
        has_vol = bool(VOLUME_RE.search(suffix))
        removed.append(suffix)
        changes.append('REMOVE_COLON_GIFT_SUFFIX')
        risk = _risk_max(risk, 'review')  # 항상 review (본품 구성일 가능성)
        name = name[:m.start()].strip()

    return name, removed, changes, risk


def clean_product_name(orig: str):
    """
    원본 productName → displayName 후보 생성 (v1.1)
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

    unique_ch = list(dict.fromkeys(all_changes))
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

print(f"[{VERSION}] BL_PRODUCTS 조회 중...")
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
        'version':              VERSION,
        '_score':               score or 0,
        '_bst':                 bst or '',
    })

cur.close()
conn.close()

# ─── 통계 ─────────────────────────────────────────────────────────
total     = len(results)
changed_r = [r for r in results if r['changed']]
no_change = [r for r in results if not r['changed']]
safe_r    = [r for r in changed_r if r['riskLevel'] == 'safe']
review_r  = [r for r in changed_r if r['riskLevel'] == 'review']
unsafe_r  = [r for r in results   if r['riskLevel'] == 'unsafe']

SEP = "=" * 72
print(SEP)
print(f"정제 결과 통계 [{VERSION}]")
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
    print(f"  {ct:50s}: {cnt}")

# ─── [SET] 보존 확인 ────────────────────────────────────────────────
set_products = [r for r in results if re.search(r'\[set\]', r['productName'], re.IGNORECASE)]
set_changed  = [r for r in set_products if r['changed']]
print(f"\n{SEP}")
print(f"[SET] 포함 상품: {len(set_products)}개 (변경된 것: {len(set_changed)}개, 보존된 것: {len(set_products)-len(set_changed)}개)")
print(SEP)
for r in set_products[:30]:
    flag = '⚠️ 변경됨' if r['changed'] else '✅ 보존됨'
    print(f"  {flag} id={r['productId']} score={r['_score']:.1f}")
    print(f"     원본: {r['productName'][:80]}")
    if r['changed']:
        print(f"     정제: {r['displayNameCandidate'][:80]}")

# ─── 마케팅 클레임 감지 결과 ─────────────────────────────────────────
marketing_r = [r for r in changed_r if r['changeType'] in ('REMOVE_MARKETING_CLAIM_BRACKET', 'MULTI_RULE')
               and 'REMOVE_MARKETING_CLAIM_BRACKET' in (r['reason'] or '')]
# 더 정확하게: reason에 마케팅 클레임 키워드 포함
marketing_r2 = [r for r in changed_r if 'REMOVE_MARKETING_CLAIM_BRACKET' in r.get('changeType','')]
# changeType에서 직접 확인하기 어려우므로 reason 검색
mc_changed = [r for r in changed_r
              if any(kw in (r['reason'] or '') for kw in ['년 연속', '1위', '수상', '판매량', '재구매율', '랭킹'])]
print(f"\n{SEP}")
print(f"마케팅 클레임 감지 결과: {len(mc_changed)}건")
print(SEP)
for r in mc_changed[:30]:
    print(f"  id={r['productId']} score={r['_score']:.1f} [{r['riskLevel']}]")
    print(f"     원본: {r['productName'][:80]}")
    print(f"     정제: {r['displayNameCandidate'][:80]}")
    print(f"     사유: {r['reason'][:80]}")

# ─── 새 패턴 감지 결과 ─────────────────────────────────────────────
bare_gift_r = [r for r in changed_r if 'REMOVE_BARE_GIFT_SUFFIX' in r['changeType'] or
               'REMOVE_BARE_GIFT_SUFFIX' in (r['reason'] or '')]
colon_gift_r = [r for r in changed_r if 'REMOVE_COLON_GIFT_SUFFIX' in r['changeType'] or
                'REMOVE_COLON_GIFT_SUFFIX' in (r['reason'] or '')]

print(f"\n{SEP}")
print(f"괄호 없는 끝 증정품(Rule 2-C): {len(bare_gift_r)}건  /  (증정: xxx)(Rule 2-D): {len(colon_gift_r)}건")
print(SEP)
print("▶ Rule 2-C (괄호 없는 끝 증정품):")
for r in bare_gift_r[:20]:
    print(f"  id={r['productId']} [{r['riskLevel']}]")
    print(f"     원본: {r['productName'][:80]}")
    print(f"     정제: {r['displayNameCandidate'][:80]}")
    print(f"     사유: {r['reason'][:80]}")
print("▶ Rule 2-D (증정: xxx):")
for r in colon_gift_r[:20]:
    print(f"  id={r['productId']} [{r['riskLevel']}]")
    print(f"     원본: {r['productName'][:80]}")
    print(f"     정제: {r['displayNameCandidate'][:80]}")
    print(f"     사유: {r['reason'][:80]}")

# ─── 변경 후보 상위 50개 ─────────────────────────────────────────────
print(f"\n{SEP}")
print("변경 후보 상위 50개 (추천 점수 내림차순)")
print(SEP)
for i, r in enumerate(changed_r[:50], 1):
    flag = {'safe': '✅', 'review': '⚠️', 'unsafe': '❌'}.get(r['riskLevel'], '?')
    print(f"{i:3d}. {flag}[{r['changeType']}] id={r['productId']} {r['platform']} score={r['_score']:.1f}")
    print(f"     원본: {r['productName'][:78]}")
    print(f"     정제: {r['displayNameCandidate'][:78]}")
    if r['reason']:
        print(f"     사유: {r['reason'][:78]}")

# ─── review 상위 50개 ─────────────────────────────────────────────
print(f"\n{SEP}")
print(f"review 후보 상위 50개 (score 내림차순)")
print(SEP)
for i, r in enumerate(review_r[:50], 1):
    print(f"{i:3d}. id={r['productId']} score={r['_score']:.1f} {r['platform']}")
    print(f"     원본: {r['productName'][:78]}")
    print(f"     정제: {r['displayNameCandidate'][:78]}")
    print(f"     사유: {r['reason'][:78]}")

# ─── unsafe 전체 ────────────────────────────────────────────────
print(f"\n{SEP}")
print(f"unsafe 전체 ({len(unsafe_r)}건)")
print(SEP)
for r in unsafe_r:
    print(f"  id={r['productId']} | {r['productName'][:70]}")
    print(f"  → {r['reason']}")

# ─── safe 상위 50개 ────────────────────────────────────────────────
print(f"\n{SEP}")
print(f"safe 후보 상위 50개 (score 내림차순)")
print(SEP)
for i, r in enumerate(safe_r[:50], 1):
    print(f"{i:3d}. id={r['productId']} score={r['_score']:.1f} {r['platform']}")
    print(f"     원본: {r['productName'][:78]}")
    print(f"     정제: {r['displayNameCandidate'][:78]}")
    if r['reason']:
        print(f"     사유: {r['reason'][:78]}")

# ─── 아이디얼포맨 중복 확인 ──────────────────────────────────────────
print(f"\n{SEP}")
print("아이디얼포맨 중복 후보")
print(SEP)
idealformen = [r for r in results if '아이디얼포맨' in r['productName'] or '아이디얼포맨' in r['brand']]
for r in idealformen:
    print(f"  id={r['productId']} {r['platform']} | {r['productName'][:70]}")
    print(f"  → [{r['riskLevel']}] 정제: {r['displayNameCandidate'][:70]}")

# ─── CSV 저장 ─────────────────────────────────────────────────────
with open(OUT_CSV, 'w', newline='', encoding='utf-8-sig') as f:
    fieldnames = ['productId', 'platform', 'brand', 'productName',
                  'displayNameCandidate', 'changed', 'changeType',
                  'riskLevel', 'reason', 'version']
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    for r in results:
        writer.writerow({k: r[k] for k in fieldnames})

print(f"\n{SEP}")
print(f"CSV 저장 완료: {OUT_CSV}")
print(f"총 {total}행 (헤더 제외) — version={VERSION}")
print(SEP)
