# Recommendation Feedback Design

작성일: 2026-06-30

## 목적

기존 BeautyLens 추천은 크롤링 리뷰 기반 `RECOMMENDATION_SCORE`에 의존한다. 이번 확장은 사이트 내부 사용자 행동을 별도 테이블에 저장하고, 원본 점수를 보존한 상태에서 `serviceScore`를 추가 계산한다.

## 데이터 분리 원칙

- 크롤링 리뷰: `BL_REVIEWS`, `BL_PRODUCTS.RECOMMENDATION_SCORE`
- 사이트 내부 별점: `BL_PRODUCT_RATINGS`
- 찜: `BL_PRODUCT_FAVORITES`
- 추천 피드백: `BL_RECOMMENDATION_FEEDBACK`
- 조회/행동 로그: `BL_USER_PRODUCT_EVENTS`
- 관리자 운영 상태: `BL_PRODUCT_ADMIN_FLAGS`

## 피드백 타입

```text
LIKE
DISLIKE
NOT_INTERESTED
```

- `LIKE`: 추천 품질이 좋다는 신호
- `DISLIKE`: 추천 품질이 낮다는 신호
- `NOT_INTERESTED`: 해당 회원에게 다시 추천하지 않는 제외 신호

## 이벤트 타입

```text
VIEW
DETAIL_VIEW
FAVORITE
UNFAVORITE
RATE
COMMENT
RECOMMEND_LIKE
RECOMMEND_DISLIKE
NOT_INTERESTED
RECOMMEND_FEEDBACK
```

이벤트 테이블은 나중에 ML 학습용 user-product interaction 로그로 재사용할 수 있다.

## 보정 점수

```text
serviceScore =
recommendation_score * 0.70
+ site_rating_score * 0.15
+ same_skin_rating_score * 0.10
+ engagement_score * 0.05
- admin_or_quality_penalty
```

현재 구현:

- `recommendation_score`: 기존 크롤링 기반 점수
- `site_rating_score`: 내부 별점 평균 * 20
- `same_skin_rating_score`: 평가 당시 피부 타입이 같은 사용자 별점 평균 * 20
- `engagement_score`: 찜, 조회, 댓글, 추천 좋아요를 0~100 범위로 압축
- `image_missing_penalty`: 이미지가 없으면 감점
- `dislike_penalty`: 추천 싫어요 수만큼 감점

## 추천 제외 규칙

- `BL_PRODUCT_ADMIN_FLAGS.IS_VISIBLE='N'`: 일반 목록과 추천 모두 제외
- `BL_PRODUCT_ADMIN_FLAGS.EXCLUDE_RECOMMENDATION='Y'`: 추천에서 제외
- 로그인 사용자의 `NOT_INTERESTED`: 해당 사용자 추천에서 제외

## 화면 표시

- 상품 상세: 기존 크롤링 평점/리뷰 수와 사이트 내부 별점/찜/조회/댓글 수를 분리 표시
- 추천 카드: 기존 추천 점수와 `serviceScore`, 사이트 반응을 함께 표시
- 관리자 상품 관리: 상품별 내부 별점, 찜, 조회, 댓글 수 표시
