# 01. 데이터 Import 보고서 (Data Import Report)

---

## 개요

| 항목 | 값 |
|---|---|
| 실행일 | 2026-06-30 |
| 스크립트 | `scripts/import_products.py`, `scripts/import_reviews.py` |
| 대상 DB | Oracle XE, hr/hr, xepdb1, localhost:1521 |
| 사용 라이브러리 | oracledb (thin mode), pandas |
| 대상 데이터 | preview CSV 3개 (전체 원본 아님) |

---

## 소스 CSV 정보

| 파일 | 경로 | 원본 행 수 | 인코딩 |
|---|---|---|---|
| `product_recommendation_scores_preview.csv` | `D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\` | 95 | UTF-8-SIG |
| `product_skin_aggregates_preview.csv` | `D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\` | 200 | UTF-8-SIG |
| `train_preview.csv` | `D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\` | 2000 | UTF-8-SIG |

---

## 컬럼 검증 결과

### product_recommendation_scores_preview.csv

| 필수 컬럼 | 존재 | 비고 |
|---|---|---|
| `product_key` | ✅ | 형식: `platform::product_id` |
| `product_name` | ✅ | |
| `platform` | ✅ | oliveyoung / musinsa |
| `product_id` | ✅ | 플랫폼 고유 ID |
| `overall_positive_rate` | ✅ | 스펙 컬럼명(`overall_pos_rate`)과 다름 — 실제 컬럼명 사용 |
| `overall_neutral_rate` | ✅ | |
| `overall_negative_rate` | ✅ | |
| `recommendation_score` | ✅ | |
| `recommendation_tier` | ✅ | |
| `evidence_level` | ✅ | |
| `caution_level` | ✅ | |

### product_skin_aggregates_preview.csv

| 필수 컬럼 | 존재 | 비고 |
|---|---|---|
| `product_key` | ✅ | |
| `product_name` | ✅ | |
| `platform` | ✅ | |
| `product_id` | ✅ | |
| `base_skin_type` | ✅ | |
| `skin_review_count` | ✅ | |
| `skin_positive_rate` | ✅ | 스펙 컬럼명(`skin_pos_rate`)과 다름 — 실제 컬럼명 사용 |
| `skin_neutral_rate` | ✅ | |
| `skin_negative_rate` | ✅ | |
| `top_skin_need_tags` | ✅ | |
| `top_skin_concern_tags` | ✅ | |
| `recommendation_score` 외 | ✅ | has_rec_cols=False로 처리 — CAUTION_LEVEL='normal', SCORE/TIER/EVIDENCE=NULL |

### train_preview.csv

| 필수 컬럼 | 존재 | 비고 |
|---|---|---|
| `review_id` | ✅ | PLATFORM_REVIEW_ID로 저장 |
| `product_id` | ✅ | product_key 구성에 사용 (`platform::product_id`) |
| `platform` | ✅ | product_key 구성에 사용 |
| `review_text` | ✅ | CLOB 저장 |
| `sentiment_label` | ✅ | positive / neutral / negative |
| `product_key` | ❌ | 컬럼 없음 → `f"{platform}::{product_id}"` 로 직접 구성 |

---

## Import 실행 결과

### BL_PRODUCTS

| CSV | 전체 행 | 성공 | 스킵 | 실패 |
|---|---|---|---|---|
| product_recommendation_scores_preview.csv | 95 | 60 | 35 | 0 |
| product_skin_aggregates_preview.csv | 200 | 119 | 81 | 0 |
| **합계** | **295** | **179** | **116** | **0** |

**스킵 사유**:
- scores CSV 35건: 동일 CSV 내 중복 product_key (같은 상품의 피부타입별 행이 복수 존재 → 최초 1건만 삽입)
- agg CSV 81건: scores CSV에서 이미 삽입된 product_key (60건) + 기타 중복 (21건)

### BL_REVIEWS

| CSV | 전체 행 | 성공 | 스킵(상품 미매칭) | 스킵(중복) | 스킵(기타) | 실패 |
|---|---|---|---|---|---|---|
| train_preview.csv | 2000 | 580 | 1420 | 0 | 0 | 0 |

**스킵 사유 상세**:
- 1420건: train_preview.csv에 포함된 리뷰 상품이 preview product CSV에 없음 (상품 2개 파일은 preview 95+200행 기준, 리뷰는 훨씬 많은 상품 포함)

### BL_IMPORT_LOGS

| LOG_ID | FILE_NAME | TOTAL | SUCCESS | FAIL | SKIPPED | STATUS |
|---|---|---|---|---|---|---|
| 1 | product_recommendation_scores_preview.csv | 95 | 60 | 0 | 35 | success |
| 2 | product_skin_aggregates_preview.csv | 200 | 119 | 0 | 81 | success |
| 3 | train_preview.csv | 2000 | 580 | 0 | 1420 | success |

---

## 최종 DB 상태

| 테이블 | 건수 |
|---|---|
| BL_PRODUCTS | 179 |
| BL_REVIEWS | 580 |
| BL_IMPORT_LOGS | 3 |

**sentiment 분포**:

| sentiment_label | 건수 | 비율 |
|---|---|---|
| positive | 529 | 91.2% |
| negative | 43 | 7.4% |
| neutral | 8 | 1.4% |

**상품-리뷰 연결**:
- 리뷰가 있는 상품: 151개 / 179개 (28개는 리뷰 없음)
- 고아 리뷰(상품 없는 리뷰): 0건

---

## 샘플 검수 결과

검수 스크립트: Python oracledb + DBMS_LOB.SUBSTR 직접 조회  
검수 일시: 2026-06-30

### 상품 샘플 20개

| 검수 항목 | 결과 |
|---|---|
| 한글 깨짐 | 없음 ✅ |
| PRODUCT_NAME NULL | 0건 ✅ |
| BRAND NULL | 0건 ✅ |
| PLATFORM 값 | oliveyoung / musinsa 정상 ✅ |
| PRODUCT_KEY 형식 | `platform::product_id` 형식 정상 ✅ |
| RECOMMENDATION_SCORE NULL (agg-only 상품) | 예상 동작 (scores CSV 미포함 상품은 NULL) ✅ |

**상품 샘플 (발췌)**:
```
[106] musinsa | tonymoly | 원더 히알루론산 촉촉 앰플 100ml(+히알루론산 마스크 증정)
[112] oliveyoung | 넘버즈인 | [NEW/화잘먹] 넘버즈인 3번 매끈결 PHA 프렙 버블팩 90ml
[114] oliveyoung | 스킨푸드 | 스킨푸드 블랙슈가 마스크 워시오프 120g 더블기획 (본품+본품)
[122] oliveyoung | 뉴트로지나 | [1+1/트러블예방/여드름기능성] 뉴트로지나 아크네 포밍 클렌저 150g
```

### 리뷰 샘플 20개

| 검수 항목 | 결과 |
|---|---|
| 한글 깨짐 | 없음 ✅ |
| REVIEW_TEXT NULL | 0건 ✅ |
| SENTIMENT_LABEL 값 (positive/neutral/negative 외) | 0건 ✅ |
| PRODUCT_ID → BL_PRODUCTS 연결 | 정상 ✅ |

### negative 리뷰 샘플 (전체 43건 중 20건)

| 검수 항목 | 결과 |
|---|---|
| SENTIMENT_LABEL=negative 외 혼입 | 없음 ✅ |
| 부정 내용 일치 | 따가움, 피부 뒤집어짐, 건조함 등 실제 부정 텍스트 ✅ |
| rating 분포 | 1.0~3.0 (낮은 평점과 일치) ✅ |

**negative 샘플 (발췌)**:
```
[1]   pid=1   | rating=2.0 | "생각보다 안 시원함..."
[75]  pid=80  | rating=1.0 | "좁쌀 사라지는지도 모르겠고 걍 따갑고 건조하기만해요"
[105] pid=82  | rating=1.0 | "갑자기 간지럽고 모낭염이 올라오더라고요"
[117] pid=195 | rating=1.0 | "한번 쓰자마자 피부 다 뒤집어졌습니다"
```

### positive 리뷰 샘플 (전체 529건 중 20건)

| 검수 항목 | 결과 |
|---|---|
| SENTIMENT_LABEL=positive 외 혼입 | 없음 ✅ |
| 긍정 내용 일치 | 촉촉함, 재구매, 보습감 좋음 등 실제 긍정 텍스트 ✅ |
| rating 분포 | 4.0~5.0 (높은 평점과 일치) ✅ |

**positive 샘플 (발췌)**:
```
[92]  pid=81  | rating=5.0 | "로션은 이거만써요 겨울에쓰면 조아요 보습감 굿이에요"
[95]  pid=101 | rating=5.0 | "예전부터 여름이면 꼭 사는 크림이에요."
[109] pid=67  | rating=5.0 | "장벽 무너졌을때 이거만 바르면 3일안에 복구가능"
[110] pid=8   | rating=5.0 | "건성인 제가 써도 괜찮아서 만족하는 제품이예요"
```

### 상품-리뷰 연결 20쌍

| 검수 항목 | 결과 |
|---|---|
| PRODUCT_ID FK 정상 | ✅ |
| PRODUCT_KEY와 PLATFORM_REVIEW_ID 형식 | `platform::product_id` / `platform_productid_hash` 정상 ✅ |
| 고아 리뷰 (상품 미존재) | 0건 ✅ |
| 엉뚱한 상품에 연결된 리뷰 | 없음 (review[91]→product[106] musinsa::3162357 → 원더 히알루론산 촉촉 앰플 정상) ✅ |

---

## API 검증 결과 (실데이터 기준)

| API | HTTP | 결과 |
|---|---|---|
| `GET /api/products` | 200 | 20개 상품 반환 (page 1 기본 페이징) |
| `GET /api/products?sortBy=score` | 200 | RECOMMENDATION_SCORE DESC 정렬, top=99.29 |
| `GET /api/products?sortBy=rating` | 200 | AVG_RATING DESC 정렬, top=5.0 |
| `GET /api/products?keyword=크림` | 200 | 키워드 매칭 5개 상품 반환 |
| `GET /api/products/82` | 200 | `[흔적미백] 넘버즈인 5번 글루타치온C 흔적 앰플`, brand=넘버즈인, avgRating=3.36, cautionLevel=moderate_negative_signal |
| `GET /api/products/82/reviews` | 200 | 2개 리뷰 반환 (positive 1 + negative 1) |
| `GET /api/products/82/reviews/negative` | 200 | 1개 리뷰, sentimentLabel=negative |
| `GET /api/products/82/reviews/positive` | 200 | 1개 리뷰, sentimentLabel=positive |

---

## 특이사항 및 한계

1. **scores CSV 중복**: `product_recommendation_scores_preview.csv`는 동일 product_key가 피부타입별로 복수 행 존재. 최초 삽입 후 나머지는 IntegrityError(UNIQUE 제약)로 스킵. **정상 동작**.

2. **리뷰 상품 미매칭 1420건**: preview 상품 파일(179개 상품 기준)에 없는 상품의 리뷰는 삽입 불가. 전체 원본 import 시 해소 예정.

3. **sentiment 편향**: positive 91.2%, neutral 1.4% — train_preview.csv의 샘플링 특성. 전체 원본에서는 분포가 다를 수 있음.

4. **agg-only 상품의 RECOMMENDATION_SCORE=NULL**: scores CSV에 없는 상품은 RECOMMENDATION_SCORE, RECOMMENDATION_TIER, EVIDENCE_LEVEL이 모두 NULL. API `?sortBy=score` 시 `NULLS LAST`로 처리.

5. **sqlplus 한글 출력 문제**: Oracle NLS 설정 차이로 sqlplus 직접 출력 시 한글 깨짐. 실제 DB 저장 데이터는 Python oracledb 조회 및 Spring API 반환 모두 정상 UTF-8.
