# 데이터 적재와 사용 흐름

## 1. 크롤링 데이터

BeautyLens의 출발점은 외부 쇼핑 플랫폼에서 수집한 화장품 상품과 리뷰 데이터입니다. 전처리 결과는 `D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\` 경로의 parquet/CSV 파일을 사용하도록 import 스크립트에 정의되어 있습니다.

주요 소스 파일:

| 파일 | 사용 스크립트 | 역할 |
|---|---|---|
| `product_recommendation_scores.parquet` | `scripts/import_products_full.py` | 상품 기준 정보, 추천 점수, 리뷰 집계 |
| `service_reviews.parquet` | `scripts/import_reviews_full.py` | 리뷰 원문, 평점, 감성 라벨 |
| `product_recommendation_scores_preview.csv` | `scripts/import_products.py` | preview 상품 import |
| `train_preview.csv` | `scripts/import_reviews.py` | preview 리뷰 import |

## 2. 전처리 데이터

상품 전처리 데이터에는 다음 값이 포함됩니다.

- `product_key`: `platform::product_id` 형태의 상품 연결 키
- `platform`, `product_id`, `product_name`
- `brand`, `category`, `price`
- `avg_rating`, `total_review_count`
- 전체 긍정/중립/부정 비율
- 피부 타입별 리뷰 수와 긍정/중립/부정 비율
- `top_skin_need_tags`, `top_skin_concern_tags`
- `recommendation_score`, `recommendation_tier`, `evidence_level`
- `caution_level`

리뷰 전처리 데이터에는 다음 값이 포함됩니다.

- `product_key`
- `review_id`
- `review_text`
- `rating`
- `review_date`
- `skin_type`, `skin_concern`
- `sentiment_label`, `sentiment_id`

## 3. 상품 테이블 적재

`scripts/import_products_full.py`는 `product_recommendation_scores.parquet`를 batch 단위로 읽고 `BL_PRODUCTS`에 적재합니다.

처리 기준:

- `product_key`, `platform`, `product_id`, `product_name` 필수
- 중복 `product_key`는 스킵
- Oracle `SEQ_BL_PRODUCTS.NEXTVAL`로 내부 `PRODUCT_ID` 생성
- import 결과는 `BL_IMPORT_LOGS`에 기록

`BL_PRODUCTS`는 단순 상품명만 저장하지 않고 추천과 화면 표시에 필요한 집계 컬럼도 같이 가집니다. 그래서 추천 화면은 매번 리뷰 전체를 계산하지 않고 상품 단위 집계값을 바로 사용할 수 있습니다.

## 4. 리뷰 테이블 적재

`scripts/import_reviews_full.py`는 `service_reviews.parquet`를 읽고 `BL_REVIEWS`에 적재합니다.

처리 기준:

- 먼저 DB의 `BL_PRODUCTS`에서 `PRODUCT_KEY -> PRODUCT_ID` map을 만듭니다.
- 리뷰의 `product_key`가 상품 map에 있을 때만 적재합니다.
- 내부 `PRODUCT_ID`로 연결해 `BL_REVIEWS.PRODUCT_ID`에 저장합니다.
- 상품이 없는 리뷰는 스킵합니다.
- import 결과는 `BL_IMPORT_LOGS`에 기록합니다.

이 방식 덕분에 고아 리뷰가 생기지 않고, 상품 상세에서 `PRODUCT_ID` 기준으로 리뷰를 안정적으로 조회할 수 있습니다.

## 5. 사용자 활동 데이터 생성

사용자가 서비스를 이용하면서 생기는 데이터는 원본 상품/리뷰와 분리했습니다.

| 사용자 행동 | 저장 테이블 | 사용 화면 |
|---|---|---|
| 찜하기 | `BL_PRODUCT_FAVORITES` | 상품 카드, 상품 상세, 마이페이지 |
| 상품 평가 | `BL_PRODUCT_RATINGS` | 상품 상세, 마이페이지, 추천 보조 점수 |
| 최근 본 상품 | `BL_USER_PRODUCT_EVENTS` | 상품 상세 진입, 마이페이지 |
| 추천 피드백 | `BL_RECOMMENDATION_FEEDBACK` | 추천 페이지, 마이페이지, 추천 제외 |
| 댓글 작성 | `BL_PRODUCT_COMMENTS` | 상품 상세, 마이페이지, 관리자 댓글 관리 |
| 댓글 신고 | `BL_COMMENT_REPORTS` | 상품 상세, 관리자 신고 관리 |

크롤링한 데이터를 그대로 보여주는 것에서 끝내지 않고, 사용자의 반응을 별도 테이블로 쌓아야 추천 서비스처럼 보일 수 있다고 판단했습니다.

## 6. 관리자 운영 데이터 생성

관리자가 상품을 운영할 때는 원본 상품을 삭제하지 않습니다.

| 관리자 행동 | 저장 테이블 | 이유 |
|---|---|---|
| 상품 숨김/복구 | `BL_PRODUCT_ADMIN_FLAGS` | 원본 상품 보존, 복구 가능 |
| 추천 제외/포함 | `BL_PRODUCT_ADMIN_FLAGS` | 추천 영역 정책만 변경 |
| 품질 상태 관리 | `BL_PRODUCT_ADMIN_FLAGS` | 이미지 없음, 리뷰 부족, 주의 신호 관리 |
| 운영 메모 | `BL_PRODUCT_ADMIN_FLAGS` | 상품별 운영 판단 기록 |
| 댓글 삭제/복구 | `BL_PRODUCT_COMMENTS` | soft delete 방식 관리 |
| 신고 처리 | `BL_COMMENT_REPORTS` | 신고 상태 추적 |
| 관리자 작업 기록 | `BL_ADMIN_AUDIT_LOGS` | 누가 어떤 작업을 했는지 추적 |

원본 상품 데이터를 삭제하면 import 검증, 추천 점수 확인, 복구가 어려워집니다. 그래서 상품 자체는 유지하고 운영 상태만 별도로 분리했습니다.

## 7. 추천 화면 데이터 조회 흐름

추천 화면은 `RecommendationController`와 `recommendationMapper.xml`을 통해 조회합니다.

흐름:

1. 사용자가 피부 타입을 선택하거나 로그인 회원의 피부 타입을 사용합니다.
2. Mapper가 `BL_PRODUCTS.BASE_SKIN_TYPE` 기준으로 상품을 조회합니다.
3. `BL_PRODUCT_ADMIN_FLAGS.IS_VISIBLE='N'` 상품은 제외합니다.
4. `BL_PRODUCT_ADMIN_FLAGS.EXCLUDE_RECOMMENDATION='Y'` 상품은 추천에서 제외합니다.
5. 로그인 사용자의 `NOT_INTERESTED` 피드백 상품은 제외합니다.
6. 내부 반응 데이터인 찜 수, 사이트 평가 수, 평균 별점, 조회 수, 댓글 수, 추천 피드백 수를 함께 조회합니다.
7. Service에서 기존 추천 점수와 내부 반응을 이용해 `serviceScore`를 계산합니다.

## 8. 상품 상세 데이터 조합 흐름

상품 상세는 여러 데이터 출처를 한 화면에 조합합니다.

| 섹션 | 데이터 출처 |
|---|---|
| 상품 요약 | `BL_PRODUCTS` |
| 추천 근거 | `BL_PRODUCTS` 집계값, `RecommendationService` reason |
| 외부 리뷰 분석 | `BL_REVIEWS` positive/negative |
| 사이트 사용자 평가 | `BL_PRODUCT_RATINGS` |
| 내 평가 남기기 | `BL_PRODUCT_RATINGS` 저장/조회 |
| 회원 의견 | `BL_PRODUCT_COMMENTS` |
| 댓글 신고 | `BL_COMMENT_REPORTS` |
| 최근 본 상품 기록 | `BL_USER_PRODUCT_EVENTS` |

외부 리뷰 분석, 사이트 사용자 평가, 회원 의견을 분리한 이유는 각 데이터의 출처와 목적이 다르기 때문입니다.

## 9. 마이페이지 데이터 조회 흐름

마이페이지는 사용자의 활동 데이터를 모아 보여줍니다.

| 마이페이지 섹션 | API | 테이블 |
|---|---|---|
| 내 프로필 | `/api/members/me` | `BL_MEMBERS` |
| 찜한 상품 | `/api/members/me/favorites` | `BL_PRODUCT_FAVORITES`, `BL_PRODUCTS` |
| 내가 평가한 상품 | `/api/members/me/ratings` | `BL_PRODUCT_RATINGS`, `BL_PRODUCTS` |
| 최근 본 상품 | `/api/members/me/recent-products` | `BL_USER_PRODUCT_EVENTS`, `BL_PRODUCTS` |
| 내가 남긴 의견 | `/api/members/me/comments` | `BL_PRODUCT_COMMENTS`, `BL_PRODUCTS` |
| 추천 피드백 | `/api/members/me/recommendation-feedback` | `BL_RECOMMENDATION_FEEDBACK`, `BL_PRODUCTS` |
| 얼굴 로그인 | `/api/members/me/face` | `BL_FACE_CREDENTIALS` |

숨김 처리된 상품은 마이페이지 활동 목록에서도 제외됩니다. 사용자가 예전에 찜하거나 본 상품이라도 관리자가 숨김 처리했다면 사용자 화면에 계속 노출되지 않도록 Mapper에 visibility 조건을 넣었습니다.

## 10. 정리

BeautyLens의 데이터 구조는 크롤링 데이터를 그대로 화면에 보여주는 구조가 아니라, 서비스에서 쓰기 좋게 나눈 구조입니다.

- 상품 기준 정보: `BL_PRODUCTS`
- 외부 리뷰 원문: `BL_REVIEWS`
- 사용자 활동: 찜, 평가, 최근 본 상품, 추천 피드백
- 사이트 내부 의견: 댓글, 신고
- 관리자 운영: 상품 운영 상태, 운영 로그
- 얼굴 로그인: 회원과 분리된 인증 정보

이렇게 나눈 이유는 기능이 늘어날수록 데이터의 출처, 목적, 운영 정책이 달라지기 때문입니다.

