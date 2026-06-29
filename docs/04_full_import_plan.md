# 04. 전체 데이터 Import 계획 (Full Import Plan)

작성일: 2026-06-30  
단계: 6차-1 보완 (3개 플랫폼 전체 재조사 완료)  
상태: 사용자 전략 선택 대기

---

## 0. 문제 인식 (6차-1 보완 이유)

기존 계획은 `product_recommendation_scores.parquet` 기준으로만 작성되어 **oliveyoung + musinsa** 상품만 포함하고 있었다.  
`service_reviews.parquet`에는 **coupang 리뷰 36,185건**이 있으나, coupang 상품이 BL_PRODUCTS에 없으면 FK 매칭 실패로 전량 스킵된다.

→ 6차-1 보완 조사로 3개 플랫폼 전체를 재검토한다.

---

## 1. D:\_WebCrawling 전체 디렉토리 구조

```
D:\_WebCrawling\
├── oliveyoung_crawler\
│   ├── output\                        # 올리브영 raw 리뷰 JSONL
│   │   ├── skincare_reviews.jsonl     (73,194 lines)
│   │   ├── cleansing_reviews.jsonl    (51,955 lines)
│   │   ├── maskpack_reviews.jsonl     (58,906 lines)
│   │   └── suncare_reviews.jsonl      (42,639 lines)
│   ├── output_external\               # 외부 플랫폼 raw 리뷰 JSONL
│   │   ├── musinsa_reviews.jsonl      (228,851 lines, ~146MB)
│   │   └── coupang_reviews.jsonl      (raw, ~119MB)
│   ├── preprocessed_v3\               ★ 최신 전처리 결과
│   │   ├── service_reviews.parquet    (402,438행, 111MB) ← 3플랫폼 리뷰+감성
│   │   ├── product_recommendation_scores.parquet (6,008행, oliveyoung+musinsa only)
│   │   ├── product_skin_aggregates.parquet       (6,008행, oliveyoung+musinsa only)
│   │   ├── train.parquet              (321,950행, product_key 없음)
│   │   └── ...
│   ├── scripts\
│   │   ├── build_recommendation_scores.py
│   │   ├── build_product_skin_aggregates.py
│   │   └── build_service_reviews.py
│   └── recommendation\
└── 쿠팡\                              # 쿠팡 raw 크롤링 CSV
    ├── coupang_reviews_20260514_001626.csv (~83MB, 컬럼: review_id/product_name/product_id/rating/review_content)
    └── coupang_reviews_partial_*.csv  (3개)
```

---

## 2. 플랫폼별 데이터 현황

### 2-1. oliveyoung

| 항목 | 값 |
|---|---|
| 상품 (product_recommendation_scores) | 830 unique products |
| 리뷰 (service_reviews) | 172,109건 |
| 리뷰 중 상품 매칭 | 172,048 (100.0%) |
| sentiment_label 보유 | 172,109 (100%) |
| base_skin_type 보유 | 67,606 (39.3%) |
| brand 정보 | 있음 |
| recommendation_score | 있음 |
| caution_level | 있음 |
| product_key 형식 | `oliveyoung::A000000197743` |
| **BL_PRODUCTS import 가능** | ✅ |
| **BL_REVIEWS import 가능** | ✅ |

---

### 2-2. musinsa

| 항목 | 값 |
|---|---|
| 상품 (product_recommendation_scores) | 691 unique products |
| 리뷰 (service_reviews) | 194,144건 |
| 리뷰 중 상품 매칭 | 151,526 (78.0%) |
| 미매칭 musinsa 리뷰 | 42,618 (22.0%) — musinsa 상품이 scores에 없음 |
| sentiment_label 보유 | 194,144 (100%) |
| base_skin_type 보유 | 122,059 (62.9%) |
| brand 정보 | 있음 |
| recommendation_score | 있음 |
| caution_level | 있음 |
| product_key 형식 | `musinsa::2637406` |
| **BL_PRODUCTS import 가능** | ✅ |
| **BL_REVIEWS import 가능** | ✅ (scores 상품 기준 78%) |

**musinsa 추가 참고**: review 있는 1,012개 상품 중 691개만 scores에 존재. 나머지 321개는 피부 타입 리뷰 수 부족 등으로 scoring 파이프라인에서 제외된 상품.

---

### 2-3. coupang

| 항목 | 값 |
|---|---|
| 상품 (product_recommendation_scores) | **0개** — 파이프라인 설계상 제외 |
| 리뷰 (service_reviews) | 36,185건 |
| 리뷰 중 상품 매칭 | **0 (0%)** — BL_PRODUCTS에 coupang 상품 없음 |
| sentiment_label 보유 | 36,185 (100%, 전처리 완료) |
| base_skin_type 보유 | **0 (0%)** — coupang 리뷰에 피부 타입 정보 없음 |
| brand 정보 | **전무 (100% NULL)** |
| category | **'beauty' 단일값** (세부 구분 없음) |
| recommendation_score | **없음** (scoring 불가) |
| caution_level | **없음** |
| product_key 형식 | `coupang::coupang_9488566` (product_id에 이미 'coupang_' 포함) |
| **BL_PRODUCTS import 가능** | 조건부 — 하단 B안 참고 |
| **BL_REVIEWS import 가능** | B안 전제 시 가능 |

---

### 2-4. coupang 상품 데이터 품질 상세 (B안 전제 시)

service_reviews.parquet에서 coupang 상품 정보를 추출하면:
- 373 unique 상품
- **product_name: 89.5%가 가격/프로모션 텍스트 포함** (e.g., `닥터지 레드 블레미쉬 포 맨 올인원 플루이드, 150ml, 1개32,000원46%17,040원`)
- 정제 가능성: 가격 패턴 (`숫자,숫자원`, `숫자%`) 제거 시 이름 복구 가능, 단 2개 상품은 정제 후 이름이 공백이 됨
- **brand: 100% NULL** — coupang 크롤러가 brand 정보를 수집하지 못함
- **category: 100% 'beauty'** — 세부 카테고리 없음 (skincare/maskpack 구분 불가)

정제 전/후 예시:
```
BEFORE: 닥터지 레드 블레미쉬 포 맨 올인원 플루이드, 150ml, 1개32,000원46%17,040원
AFTER:  닥터지 레드 블레미쉬 포 맨 올인원 플루이드, 150ml, 1개

BEFORE: 미샤 듀이 루즈 립스틱, 1개, 슬로잉코랄16,600원50%8,160원
AFTER:  미샤 듀이 루즈 립스틱, 1개, 슬로잉코랄
```

---

### 2-5. coupang scoring 파이프라인 제외 이유 (공식 확인)

`build_recommendation_scores.py`와 `build_product_skin_aggregates.py` 스크립트 내부 코멘트:

```python
# coupang 전체 base_skin_type 없음 → 피부 타입별 집계 불가
# coupang rows: 0이어야 정상
```

→ coupang 제외는 데이터 누락이 아닌 **파이프라인 설계상 의도적 결정**이다.  
→ coupang에는 피부 타입 정보가 없어 recommendation_score 계산이 원천 불가능하다.

---

## 3. Import 전략 비교

### A안 — oliveyoung + musinsa만 import (scored products 기준)

| 항목 | 값 |
|---|---|
| BL_PRODUCTS | **1,521개** |
| BL_REVIEWS | **~323,574건** |
| 플랫폼 | oliveyoung 830개 + musinsa 691개 상품 |
| 데이터 품질 | ✅ 높음 — brand/category/score/caution 모두 존재 |
| 추천 API 사용 | ✅ 전 상품이 skinType 기반 추천 대상 |
| coupang 포함 | ❌ 완전 제외 (리뷰 36,185건 스킵) |
| 스크립트 변경 | 불필요 — 기존 스크립트 그대로 |
| 예상 소요시간 | import 30분~1시간 |

---

### B안 — oliveyoung + musinsa + coupang 부분 import

| 항목 | 값 |
|---|---|
| BL_PRODUCTS | **1,894개** (1,521 + coupang 373) |
| BL_REVIEWS | **~359,759건** (323,574 + coupang 36,185) |
| coupang 상품 데이터 | brand=NULL / category='beauty' / score=NULL / caution='insufficient_evidence' / base_skin_type=NULL |
| 추천 API 사용 | coupang 상품은 추천 대상 **절대 없음** (base_skin_type 없음) |
| 검색 API 사용 | ✅ GET /api/products?keyword=... 로는 노출 가능 |
| 리뷰 노출 | ✅ GET /api/products/{id}/reviews 가능 |
| 스크립트 변경 | import_products_full.py에 coupang 추출 로직 추가 필요 |
| 데이터 품질 리스크 | brand NULL / 상품명 정제 필요 / category 단일값 |
| 예상 소요시간 | import 40분~1.5시간 |

---

### C안 — coupang 전처리 파이프라인 실행 후 scoring

| 항목 | 판단 |
|---|---|
| 가능 여부 | **불가** |
| 이유 | coupang 리뷰에 피부 타입(skin_type) 정보가 없음 → base_skin_type 집계 불가 → recommendation_score 산출 불가 |
| 추가 작업 | coupang 피부 타입 수집부터 재크롤링 필요 — 현실적으로 scope 초과 |

---

### 전략 비교 요약

| 항목 | A안 | B안 | C안 |
|---|---|---|---|
| BL_PRODUCTS | 1,521 | 1,894 | 불가 |
| BL_REVIEWS | ~323,574 | ~359,759 | 불가 |
| 데이터 품질 | ★★★ | ★★☆ (coupang 낮음) | — |
| coupang 포함 | ❌ | ✅ (품질 제한) | — |
| 추천 API coupang | — | 불가 (설계상) | — |
| 스크립트 변경 | 없음 | coupang 추출 로직 추가 | — |
| 권장 | ✅ **기본 선택** | coupang 리뷰 필요 시 | — |

---

## 4. 소스 파일 최종 후보

### A안 기준 (권장)

| 용도 | 파일 | 행 수 | insert 예상 |
|---|---|---|---|
| BL_PRODUCTS | `preprocessed_v3/product_recommendation_scores.parquet` | 6,008 | 1,521 (dedup) |
| BL_REVIEWS | `preprocessed_v3/service_reviews.parquet` | 402,438 | ~323,574 (product 매칭) |

### B안 기준 (coupang 포함 시)

| 용도 | 파일 | 비고 |
|---|---|---|
| BL_PRODUCTS (oliveyoung/musinsa) | `product_recommendation_scores.parquet` | 1,521개 |
| BL_PRODUCTS (coupang 추가) | `service_reviews.parquet` 추출 | 371개 (이름 공백 2개 제외) |
| BL_REVIEWS | `service_reviews.parquet` | ~359,759건 |

---

## 5. Dry-run 결과 (A안 기준)

### import_products_full.py --dry-run (2026-06-30 02:14)

```
Total rows read:          6,008
Unique products:          1,521
Skip (dup product_key):   4,487
Skip (no product_key):    0
Skip (no product_name):   0
Expected insert: 1,521
```

### import_reviews_full.py --dry-run (2026-06-30 02:14)

```
Total rows processed:     402,438
Matched (product found):  323,574  (80.4%)
Skip (no product match):  78,864
Skip (no review_text):    0
Expected insert: ~323,574
```

---

## 6. 현재 DB 상태 (import 전)

| 테이블 | 현재 행 수 |
|---|---|
| BL_PRODUCTS | 179 |
| BL_REVIEWS | 580 |
| BL_MEMBERS | 1 (보존 대상) |
| BL_IMPORT_LOGS | 3 |
| BL_RECOMMENDATIONS | 0 |
| BL_FAVORITES | 0 |

---

## 7. 실제 Import 실행 계획 (6차-2 예정, 사용자 확인 후)

### STEP 0: 백업 (선택)

```sql
CREATE TABLE BL_PRODUCTS_PREV_BAK AS SELECT * FROM BL_PRODUCTS;
CREATE TABLE BL_REVIEWS_PREV_BAK  AS SELECT * FROM BL_REVIEWS;
```

---

### STEP 1: 초기화 (FK 순서 중요)

**방법1 — DELETE (트랜잭션, rollback 가능, 권장)**

```sql
DELETE FROM BL_RECOMMENDATIONS;
DELETE FROM BL_FAVORITES;
DELETE FROM BL_REVIEWS;
DELETE FROM BL_PRODUCTS;
COMMIT;
```

DELETE는 COMMIT 전에 `ROLLBACK`으로 취소 가능. 대용량 시 느릴 수 있음.

**방법2 — TRUNCATE (DDL, rollback 불가, 빠름)**

```sql
TRUNCATE TABLE BL_RECOMMENDATIONS;
TRUNCATE TABLE BL_FAVORITES;
TRUNCATE TABLE BL_REVIEWS;
TRUNCATE TABLE BL_PRODUCTS;
```

TRUNCATE는 Oracle에서 DDL이므로 rollback 불가. 단, **FK 제약 순서 (child → parent)** 를 반드시 지켜야 한다:
- BL_REVIEWS → BL_PRODUCTS (FK_REVIEWS_PRODUCT) → REVIEWS 먼저 TRUNCATE
- BL_FAVORITES → BL_PRODUCTS/BL_MEMBERS → FAVORITES 먼저 TRUNCATE
- BL_RECOMMENDATIONS → BL_PRODUCTS/BL_MEMBERS → RECOMMENDATIONS 먼저 TRUNCATE
- BL_MEMBERS: **TRUNCATE 금지** (테스트 계정 보존)

현재 BL_RECOMMENDATIONS=0, BL_FAVORITES=0이므로 두 테이블 TRUNCATE는 사실상 no-op.

**권장**: STEP 0 백업 없이 진행 시 **DELETE 방식**으로 진행 (rollback 가능성 확보).

---

### STEP 2: 시퀀스 리셋 (선택)

```sql
DROP SEQUENCE SEQ_BL_PRODUCTS;
CREATE SEQUENCE SEQ_BL_PRODUCTS START WITH 1 INCREMENT BY 1;
DROP SEQUENCE SEQ_BL_REVIEWS;
CREATE SEQUENCE SEQ_BL_REVIEWS START WITH 1 INCREMENT BY 1;
```

리셋 안 해도 기능 무관. product_id 연속성이 필요한 경우에만 실행.

---

### STEP 3: 상품 import

```
python scripts/import_products_full.py
```

예상: 1,521건 INSERT, ~30초

---

### STEP 4: 리뷰 import

```
python scripts/import_reviews_full.py
```

예상: ~323,574건 INSERT, 20~40분 (행별 commit 방식)

**속도 개선 방법 (선택)**: 스크립트를 배치 commit으로 수정 시 5분 이내 가능.  
현재 스크립트는 INSERT마다 `conn.commit()` → 323,574번 commit → 느림.

---

### STEP 5: 검증

```sql
SELECT COUNT(*) FROM BL_PRODUCTS;   -- 기대: 1,521
SELECT COUNT(*) FROM BL_REVIEWS;    -- 기대: ~323,574

SELECT PLATFORM, COUNT(*) FROM BL_PRODUCTS GROUP BY PLATFORM;
-- 기대: oliveyoung=830, musinsa=691

SELECT PLATFORM, COUNT(*) FROM BL_REVIEWS GROUP BY PLATFORM;
-- 기대: oliveyoung=~172,048 / musinsa=~151,526

SELECT SENTIMENT_LABEL, COUNT(*) FROM BL_REVIEWS GROUP BY SENTIMENT_LABEL;
-- 기대: positive>negative>neutral

-- 고아 리뷰 없음 확인
SELECT COUNT(*) FROM BL_REVIEWS r
WHERE NOT EXISTS (SELECT 1 FROM BL_PRODUCTS p WHERE p.PRODUCT_ID = r.PRODUCT_ID);
-- 기대: 0
```

API 검증:
```
GET /api/products?sortBy=score     → 상위 20개 (score 있는 상품만)
GET /api/recommendations/me        → 피부 타입별 추천 (1,521개 풀에서)
GET /api/products/{id}/reviews/negative  → 부정 리뷰 조회
```

---

## 8. 위험 요소

| 위험 | 내용 | 대응 |
|---|---|---|
| BL_REVIEWS 리뷰 텍스트 길이 | 일부 리뷰가 CLOB 한도 초과 가능성 거의 없으나 watch | 에러 발생 시 해당 row skip + fail_samples 기록 |
| 리뷰 import 속도 | 323,574건 × conn.commit() → 40분+ | 배치 commit 수정 시 5분 |
| BL_IMPORT_LOGS TRUNCATE 여부 | 기존 3개 로그 기록 삭제 여부 | 사용자 결정 (TRUNCATE 안 해도 무방) |
| TRUNCATE 후 rollback 불가 | DDL이므로 undo 불가 | DELETE 방식 권장, 또는 STEP 0 백업 선행 |
| coupang 상품 포함 여부 | B안 선택 시 product_name 정제 + brand NULL 노출 | A안 권장, B안은 품질 문제 동의 후 진행 |

---

## 9. 사용자 확인 필요 항목

1. **전략 선택**: A안 (oliveyoung+musinsa, 1,521개) vs B안 (3개 플랫폼, 1,894개)?
2. **초기화 방식**: DELETE (느리고 안전) vs TRUNCATE (빠르고 rollback 불가)?
3. **백업 여부**: CTAS 백업 테이블 생성 후 진행 vs 바로 진행?
4. **리뷰 import 속도**: 현재 스크립트(느림) vs 배치 commit 수정(빠름)?
5. **시퀀스 리셋 여부**: product_id 1부터 시작 vs 현재 seq 값 이어서 사용?

---

## 10. 각 안 최종 예상 수치

| | A안 (권장) | B안 (coupang 포함) |
|---|---|---|
| BL_PRODUCTS 최종 | 1,521 | 1,894 |
| BL_REVIEWS 최종 | ~323,574 | ~359,759 |
| 추천 가능 상품 | 1,521 (전체) | 1,521 (oliveyoung/musinsa만) |
| coupang 리뷰 활용 | ❌ | ✅ |
| 데이터 품질 | ★★★ | ★★☆ |
| 스크립트 변경 필요 | 없음 | import_products_full.py 수정 필요 |
