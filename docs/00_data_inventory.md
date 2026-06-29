# 00. 데이터 조사 결과 (Data Inventory)

조사 일자: 2026-06-29  
조사 대상: `D:\_WebCrawling`  
조사 방법: 에이전트 탐색 + 실제 파일 읽기

---

## 1. 전체 폴더 구조

```
D:\_WebCrawling\
├── musinsa_beauty_TOTAL.csv          (93.49 MB) — 뮤신사 상품+리뷰 통합, sentiment 컬럼 포함
├── CLAUDE.md, AGENTS.md
├── 쿠팡\
│   └── coupang_reviews_20260514_001626.csv   (78.90 MB) — 쿠팡 리뷰 원본
└── oliveyoung_crawler\
    ├── output_external\              — musinsa/coupang 리뷰 JSONL
    │   ├── musinsa_reviews.jsonl     (138.95 MB)
    │   └── coupang_reviews.jsonl     (113.85 MB)
    ├── output\                       — 올리브영 카테고리별 리뷰 JSONL
    │   ├── skincare_reviews.jsonl    (42.61 MB)
    │   ├── maskpack_reviews.jsonl    (29.96 MB)
    │   ├── cleansing_reviews.jsonl   (1.96 MB)
    │   └── suncare_reviews.jsonl     (1.26 MB)
    ├── preprocessed_v3\              ★ 1차 import 대상 (감성분석 완료)
    │   ├── product_recommendation_scores_preview.csv  (0.06 MB, 95행)
    │   ├── product_skin_aggregates_preview.csv        (0.09 MB, 200행)
    │   ├── train_preview.csv                          (1.84 MB, ~2000행)
    │   ├── ambiguous_preview.csv                      (2.91 MB)
    │   └── service_reviews_preview.csv                (0.17 MB)
    ├── models\                       — Transformer 감성분석 모델 (총 약 8.2GB)
    │   ├── transformer_final_v3\     ★ BERT multilingual, 권장
    │   ├── transformer_final_v2\
    │   └── transformer_klue-bert-base_balanced\
    └── reports\                      — 모델 평가 보고서
```

---

## 2. 1차 Import 대상 파일

### 2-1. product_recommendation_scores_preview.csv (95행)

**역할**: BL_PRODUCTS 주 소스. 추천 점수가 계산된 상품만 포함.

| 컬럼명 | 설명 | DB 컬럼 |
|---|---|---|
| product_key | platform::product_id (UNIQUE) | PRODUCT_KEY |
| product_id | 플랫폼 내 상품 ID | PLATFORM_PRODUCT_ID |
| platform | oliveyoung / musinsa | PLATFORM |
| product_name | 상품명 | PRODUCT_NAME |
| brand | 브랜드명 | BRAND |
| category | skincare / maskpack 등 | CATEGORY |
| price | 가격 | PRICE |
| avg_rating | 평균 평점 | AVG_RATING |
| total_review_count | 총 리뷰 수 | TOTAL_REVIEW_COUNT |
| overall_positive_rate | 전체 긍정 비율 | OVERALL_POS_RATE |
| overall_neutral_rate | 전체 중립 비율 | OVERALL_NEU_RATE |
| overall_negative_rate | 전체 부정 비율 | OVERALL_NEG_RATE |
| base_skin_type | 대표 피부 타입 | BASE_SKIN_TYPE |
| skin_review_count | 피부 타입별 리뷰 수 | SKIN_REVIEW_COUNT |
| skin_positive_rate | 피부 타입별 긍정 비율 | SKIN_POS_RATE |
| skin_neutral_rate | 피부 타입별 중립 비율 | SKIN_NEU_RATE |
| skin_negative_rate | 피부 타입별 부정 비율 | SKIN_NEG_RATE |
| top_skin_need_tags | 주요 필요 태그 | TOP_NEED_TAGS |
| top_skin_concern_tags | 주요 고민 태그 | TOP_CONCERN_TAGS |
| caution_level | normal / moderate_negative_signal / high_negative_signal | CAUTION_LEVEL |
| recommendation_score | 추천 점수 (20.02~99.29 범위) | RECOMMENDATION_SCORE |
| recommendation_tier | strong_candidate 등 | RECOMMENDATION_TIER |
| evidence_level | strong_evidence 등 | EVIDENCE_LEVEL |

### 2-2. product_skin_aggregates_preview.csv (200행)

**역할**: BL_PRODUCTS 보완 소스. 추천 점수 없는 상품 105개 추가 확보 가능.  
**컬럼**: product_recommendation_scores_preview.csv와 동일하나 recommendation_score, recommendation_tier, evidence_level, caution_level 없음.  
**Import 조건**: product_key + product_name 필수값 있는 행만 insert.

### 2-3. train_preview.csv (~2000행)

**역할**: BL_REVIEWS 소스. 감성 분석 완료.

| 컬럼명 | 설명 | DB 컬럼 |
|---|---|---|
| platform | 플랫폼 | — (product_key 생성에 사용) |
| product_id | 플랫폼 내 상품 ID | — (product_key → 내부 FK 매핑) |
| review_id | 리뷰 고유 ID | PLATFORM_REVIEW_ID |
| product_name | 상품명 | — |
| brand | 브랜드 | — |
| rating | 평점 | RATING |
| review_text | 리뷰 본문 | REVIEW_TEXT (CLOB) |
| review_date | 작성일 (YYYY-MM-DD) | REVIEW_DATE |
| skin_type | 피부 타입 원본 ('복합성 · 진정/보습') | REVIEWER_SKIN_TYPE |
| skin_concern | 피부 고민 | REVIEWER_SKIN_CONCERN |
| sentiment_label | **positive / neutral / negative** ★ | SENTIMENT_LABEL |
| sentiment_id | **2 / 1 / 0** ★ | SENTIMENT_ID |
| label_confidence | high / medium / low | — |
| is_ambiguous | 불명확 여부 | — |

---

## 3. 전체 원본 데이터 (2차 import 예정)

| 파일 | 크기 | 플랫폼 | 행 수 추정 |
|---|---|---|---|
| musinsa_beauty_TOTAL.csv | 93.49 MB | 뮤신사 | 300,000+ |
| coupang_reviews_20260514_001626.csv | 78.90 MB | 쿠팡 | 200,000+ |
| musinsa_reviews.jsonl | 138.95 MB | 뮤신사 | 300,000+ |
| coupang_reviews.jsonl | 113.85 MB | 쿠팡 | 200,000+ |
| skincare_reviews.jsonl | 42.61 MB | 올리브영 | 100,000+ |
| maskpack_reviews.jsonl | 29.96 MB | 올리브영 | 70,000+ |

처리 방법: `pandas read_json(lines=True, chunksize=5000)` 청크 처리

---

## 4. AI 모델

| 모델명 | 크기 | 기반 | 레이블 |
|---|---|---|---|
| transformer_final_v3 ★ | 2956 MB | BERT multilingual | negative(0)/neutral(1)/positive(2) |
| transformer_final_v2 | 2956 MB | BERT | 동일 |
| transformer_klue-bert-base_balanced | 1267 MB | KLUE-BERT (한국어 특화) | 동일 |

**1차 구현에서는 모델 서빙 불필요.** train_preview.csv에 sentiment_label이 이미 저장되어 있음.

---

## 5. 데이터 연결 구조

```
product_recommendation_scores_preview.csv
  ↓ product_key (UNIQUE)
BL_PRODUCTS (내부 PRODUCT_ID 생성)
  ↓ FK
BL_REVIEWS
  ← train_preview.csv (platform::product_id → product_key → BL_PRODUCTS.PRODUCT_ID 매핑)
```

---

## 6. 주의사항

- `_preview.csv` 파일은 전처리 완료된 샘플 데이터. 충분한 품질이 확인되면 전체 JSONL 처리.
- skin_type 컬럼 원본값 형태: `'복합성 · 진정/보습 · 모공'` — BL_REVIEWS에 통째로 저장.
- review_date 포맷: `'YYYY-MM-DD'` 문자열 → Oracle DATE 변환 필요.
- 감성 분석 레이블 확인: positive ≫ neutral > negative 분포 예상.
