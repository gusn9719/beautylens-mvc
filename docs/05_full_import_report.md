# 05. 전체 데이터 Import 결과 보고서

작성일: 2026-06-30  
단계: 6차-2 A안 실행 완료

---

## 1. 실행 개요

| 항목 | 내용 |
|---|---|
| 실행 전략 | **A안** — oliveyoung + musinsa scored products (coupang 제외) |
| 실행 방식 | DELETE(preview) → import_products_full.py → import_reviews_full.py |
| 상품 소스 | `preprocessed_v3/product_recommendation_scores.parquet` |
| 리뷰 소스 | `preprocessed_v3/service_reviews.parquet` |
| 실행 일시 | 2026-06-30 02:43 ~ 02:46 |

---

## 2. 사전 정리 (DELETE)

| 테이블 | 삭제 전 | 삭제 후 |
|---|---|---|
| BL_PRODUCTS | 179 (preview) | 0 |
| BL_REVIEWS | 580 (preview) | 0 |
| BL_RECOMMENDATIONS | 0 | 0 |
| BL_FAVORITES | 0 | 0 |
| BL_MEMBERS | 1 (보존) | 1 |

실행 방법: `DELETE FROM ... COMMIT` (FK 순서: BL_RECOMMENDATIONS → BL_FAVORITES → BL_REVIEWS → BL_PRODUCTS)

---

## 3. 상품 Import 결과 (import_products_full.py)

실행 시각: 2026-06-30 02:44:22 ~ 02:44:24 (약 2초)

| 항목 | 값 |
|---|---|
| 처리 행 수 (parquet 총 행) | 6,008 |
| 중복 product_key 스킵 | 4,487 |
| no product_key 스킵 | 0 |
| no product_name 스킵 | 0 |
| **INSERT 성공** | **1,521** |
| INSERT 실패 | 0 |

**Platform 분포**:

| Platform | 상품 수 |
|---|---|
| oliveyoung | 830 |
| musinsa | 691 |
| **합계** | **1,521** |

**Caution level 분포**:

| caution_level | 상품 수 |
|---|---|
| normal | 1,025 |
| insufficient_evidence | 308 |
| moderate_negative_signal | 142 |
| high_negative_signal | 46 |

**Recommendation score 통계**:  
min=8.82 / max=99.82 / avg=75.21

---

## 4. 리뷰 Import 결과 (import_reviews_full.py)

실행 시각: 2026-06-30 02:45:06 ~ 02:46:35 (약 89초)

리뷰 스크립트 성능 개선: 기존 행별 commit → **배치 commit (500행마다)**  
→ 예상 40분+ → 실제 **89초** (약 27배 단축)

| 항목 | 값 |
|---|---|
| 처리 행 수 (parquet 총 행) | 402,438 |
| 상품 매칭 (product_key 일치) | 323,574 (80.4%) |
| no product match 스킵 | 78,864 (19.6%) |
| no review_text 스킵 | 0 |
| 중복(UNIQUE 위반) 스킵 | 0 |
| **INSERT 성공** | **323,574** |
| INSERT 실패 | 0 |

**스킵 사유 분석 (78,864건)**:

| 원인 | 건수 |
|---|---|
| coupang 리뷰 (상품 없음) | 36,185 |
| musinsa 미점수 상품 리뷰 | ~42,618 |
| oliveyoung 미점수 상품 리뷰 | ~61 |

**Platform 분포 (BL_REVIEWS)**:

| Platform | 리뷰 수 |
|---|---|
| oliveyoung | 172,048 |
| musinsa | 151,526 |
| 합계 | 323,574 |

**Sentiment 분포 (BL_REVIEWS)**:

| sentiment_label | 리뷰 수 | 비율 |
|---|---|---|
| positive | 279,849 | 86.5% |
| negative | 32,267 | 10.0% |
| neutral | 11,458 | 3.5% |

---

## 5. DB 최종 상태

| 테이블 | 최종 행 수 |
|---|---|
| BL_PRODUCTS | **1,521** |
| BL_REVIEWS | **323,574** |
| BL_MEMBERS | 1 (보존) |
| BL_IMPORT_LOGS | 5 |
| BL_RECOMMENDATIONS | 0 |
| BL_FAVORITES | 0 |

**무결성 검증**: 고아 리뷰(product 없는 review) = **0건** ✅

---

## 6. API 동작 변화 요약

| 비교 항목 | import 전 (preview) | import 후 (full) |
|---|---|---|
| BL_PRODUCTS 상품 수 | 179 | 1,521 |
| BL_REVIEWS 리뷰 수 | 580 | 323,574 |
| GET /api/products?sortBy=score | 상위 20개 (179 중) | 상위 20개 (1,521 중) |
| GET /api/recommendations/me | 건성/지성/복합성 등 | 동일 로직, 데이터 풍부 |
| GET /api/products/{id}/reviews | 소수 리뷰 | 다수 리뷰 가능 |

---

## 7. 상품명 품질 검수 결과 요약

상세 보고서: [`docs/06_product_quality_report.md`](06_product_quality_report.md)

| 항목 | 결과 |
|---|---|
| brand NULL | **0 (0%)** ✅ |
| product_name NULL | **0 (0%)** ✅ |
| 100자 초과 상품명 | **0** ✅ |
| 최대 상품명 길이 | 86자 |
| 프로모션 의심 상품명 | **792개 (52.1%)** ⚠️ |
| 플랫폼 간 중복 의심 | **0쌍** ✅ |
| 동일 플랫폼 내 유사 쌍 | **455쌍** ⚠️ (대부분 용량/수량 변형) |

**주요 발견사항**:
1. **프로모션 키워드 52.1%**: '기획'(523건), '증정'(204건), '1+1'(87건) 등 — 올리브영/무신사 상품명에 이벤트 문구가 직접 포함된 것. UI 표시 시 정제 필요.
2. **동일 플랫폼 내 유사 455쌍**: 대부분 동일 상품의 용량(1매 vs 10매), 수량(단품 vs 기획) 변형. 일부(유사도 1.0 = 완전 동일)는 실제 중복 의심.
3. **플랫폼 간 중복 없음**: 올리브영↔무신사 상품명 65% 유사도 이상 0쌍 — 두 플랫폼의 상품 구성이 상이함.
4. **자동 정제 권장**: PRODUCT_NAME 직접 수정이 아닌, UI layer에서 displayName 정제 처리 권장.

---

## 8. 다음 단계 (7차 예정)

- [ ] 상품명 프로모션 문구 정제 방식 결정 (DB 수정 vs UI 레이어 처리)
- [ ] 유사도 1.0 완전 동일 상품 확인 (아이디얼포맨 퍼펙트 올인원 중복 여부)
- [ ] 동일플랫폼 유사 455쌍 중 실제 중복 vs 용량 변형 구분
- [ ] coupang 포함 여부 재검토 (B안 선택 시 별도 스크립트 개발)
- [ ] 추천 API 품질 재검증 (1,521개 풀로 재테스트)
