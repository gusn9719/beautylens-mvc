# 02. API 명세 (API Specification)

작성일: 2026-06-29  
최종 수정: 2026-06-30 (11차 수정: 댓글 삭제 권한 변경 — 작성자 본인 또는 ADMIN)  
버전: 11차 구현 (권한 수정 포함)

---

## 공통 응답 형식

```json
{
  "success": true | false,
  "message": "응답 메시지",
  "data": <데이터 또는 null>
}
```

클래스: `kr.ac.kopo.common.vo.ApiResponse<T>`

---

## Health API

### GET /api/health

서버 상태 및 DB 연결 확인.

**응답 예시 (200 OK)**:
```json
{
  "success": true,
  "message": "healthy",
  "data": {
    "app": "beautylens-mvc",
    "version": "1.0.0",
    "db": "ok",
    "timestamp": "Mon Jun 29 21:31:46 KST 2026"
  }
}
```

---

## Product API

### GET /api/products

상품 목록 조회 (페이징 + 검색 필터 + 정렬).

**Query Parameters**:

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| page | int | 1 | 페이지 번호 |
| size | int | 20 | 페이지당 건수 |
| keyword | String | - | 상품명 또는 브랜드 검색 |
| category | String | - | 카테고리 필터 (skincare/maskpack/cleansing/suncare) |
| brand | String | - | 브랜드 필터 |
| skinType | String | - | 피부 타입 필터 (BASE_SKIN_TYPE 기준) |
| sortBy | String | - | 정렬 기준 (score/rating, 없으면 PRODUCT_ID DESC) |

**페이징**: Oracle 12c `OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY`

**응답 예시 (200 OK)**:
```json
{
  "success": true,
  "message": "products found",
  "data": [
    {
      "productId": 1,
      "platform": "oliveyoung",
      "productKey": "oliveyoung::12345",
      "productName": "세타필 모이스처라이징 크림 로션 250ml 건성민감성피부용",
      "displayName": "세타필 모이스처라이징 로션",
      "brand": "세타필",
      "category": "skincare",
      "price": 19900.0,
      "avgRating": 4.5,
      "recommendationScore": 87.3,
      "cautionLevel": "normal",
      "productUrl": "https://www.oliveyoung.co.kr/store/goods/getGoodsDetail.do?goodsNo=A000000012345",
      "imageUrl": "https://image.oliveyoung.co.kr/cfimages/cf-goods/uploads/images/thumbnails/500/10/0000/0017/A00000001234500.jpg",
      "imageStatus": "found"
    }
  ]
}
```

---

### GET /api/products/{productId}

상품 단건 조회.

**Path Parameters**: `productId` (int)

**응답 (200 OK)**: 단건 ProductVO

**응답 (404 Not Found)**:
```json
{
  "success": false,
  "message": "product not found",
  "data": null
}
```

---

## Review API

### GET /api/products/{productId}/reviews

상품의 전체 리뷰 조회.

**Path Parameters**: `productId` (int)

**응답 (200 OK)**:
```json
{
  "success": true,
  "message": "reviews found",
  "data": [
    {
      "reviewId": 1,
      "productId": 1,
      "rating": 5.0,
      "reviewText": "피부에 잘 맞아요. 촉촉하게 유지됩니다.",
      "reviewDate": "2026-01-15",
      "reviewerSkinType": "건성",
      "sentimentLabel": "positive",
      "sentimentId": 2
    }
  ]
}
```

---

### GET /api/products/{productId}/reviews/negative

상품의 부정 리뷰 조회 (`SENTIMENT_LABEL = 'negative'`).

**Path Parameters**: `productId` (int)

**응답 (200 OK)**: negative 리뷰만 포함된 ReviewVO 배열

---

### GET /api/products/{productId}/reviews/positive

상품의 긍정 리뷰 조회 (`SENTIMENT_LABEL = 'positive'`).

**Path Parameters**: `productId` (int)

**응답 (200 OK)**: positive 리뷰만 포함된 ReviewVO 배열

---

## VO 명세

### ProductVO (`kr.ac.kopo.product.vo.ProductVO`)

| 필드 | 타입 | DB 컬럼 | 비고 |
|---|---|---|---|
| productId | Integer | PRODUCT_ID | PK |
| platform | String | PLATFORM | oliveyoung/musinsa |
| platformProductId | String | PLATFORM_PRODUCT_ID | |
| productKey | String | PRODUCT_KEY | UNIQUE: platform::product_id |
| productName | String | PRODUCT_NAME | 원본 상품명 (수정 불가) |
| displayName | String | — | Service 계층 정제 (DisplayNameCleaner.clean()) |
| productUrl | String | PRODUCT_URL | 상품 상세 페이지 URL |
| imageUrl | String | IMAGE_URL | 대표 이미지 URL (og:image 추출), imageStatus=found일 때만 non-null |
| imageStatus | String | IMAGE_STATUS | found/not_found/fetch_failed/blocked/pending |
| brand | String | BRAND | |
| category | String | CATEGORY | skincare/maskpack 등 |
| price | Double | PRICE | nullable |
| avgRating | Double | AVG_RATING | nullable |
| totalReviewCount | Integer | TOTAL_REVIEW_COUNT | |
| overallPosRate | Double | OVERALL_POS_RATE | nullable |
| overallNeuRate | Double | OVERALL_NEU_RATE | nullable |
| overallNegRate | Double | OVERALL_NEG_RATE | nullable |
| baseSkinType | String | BASE_SKIN_TYPE | |
| skinReviewCount | Integer | SKIN_REVIEW_COUNT | |
| skinPosRate | Double | SKIN_POS_RATE | nullable |
| skinNeuRate | Double | SKIN_NEU_RATE | nullable |
| skinNegRate | Double | SKIN_NEG_RATE | nullable |
| topNeedTags | String | TOP_NEED_TAGS | |
| topConcernTags | String | TOP_CONCERN_TAGS | |
| cautionLevel | String | CAUTION_LEVEL | normal/moderate_negative_signal/high_negative_signal |
| recommendationScore | Double | RECOMMENDATION_SCORE | nullable |
| recommendationTier | String | RECOMMENDATION_TIER | |
| evidenceLevel | String | EVIDENCE_LEVEL | |
| regDate | String | REG_DATE | to_char(YYYY-MM-DD) |

### ReviewVO (`kr.ac.kopo.review.vo.ReviewVO`)

| 필드 | 타입 | DB 컬럼 | 비고 |
|---|---|---|---|
| reviewId | Integer | REVIEW_ID | PK |
| productId | Integer | PRODUCT_ID | FK → BL_PRODUCTS |
| platformReviewId | String | PLATFORM_REVIEW_ID | |
| rating | Double | RATING | nullable |
| reviewText | String | REVIEW_TEXT | CLOB |
| reviewDate | String | REVIEW_DATE | to_char(YYYY-MM-DD) |
| reviewerSkinType | String | REVIEWER_SKIN_TYPE | 원본 저장 |
| reviewerSkinConcern | String | REVIEWER_SKIN_CONCERN | |
| sentimentLabel | String | SENTIMENT_LABEL | positive/neutral/negative |
| sentimentId | Integer | SENTIMENT_ID | 2/1/0, nullable |
| regDate | String | REG_DATE | to_char(YYYY-MM-DD) |

### PageParam (`kr.ac.kopo.common.vo.PageParam`)

| 필드 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| page | int | 1 | 페이지 번호 |
| size | int | 20 | 페이지당 건수 |
| keyword | String | null | 검색어 |
| category | String | null | 카테고리 |
| brand | String | null | 브랜드 |
| skinType | String | null | 피부 타입 |
| sortBy | String | null | 정렬 기준 |
| offset | int | getOffset() | (page-1)*size |

---

## Member API

### POST /api/members

회원가입.

**요청 Body (application/json)**:
```json
{
  "loginId": "test01",
  "password": "1234",
  "nickname": "테스트회원",
  "skinType": "dry",
  "skinConcern": "보습, 진정"
}
```

| 필드 | 필수 | 설명 |
|---|---|---|
| loginId | ✅ | 로그인 ID (UNIQUE) |
| password | ✅ | 비밀번호 (SHA-256 해시 저장) |
| nickname | - | 닉네임 |
| skinType | - | 피부 타입 (dry/oily/combination/sensitive/normal) |
| skinConcern | - | 피부 고민 |

**응답 (201 Created)**:
```json
{ "success": true, "message": "member registered", "data": null }
```

**응답 (409 Conflict)** — 중복 loginId:
```json
{ "success": false, "message": "loginId already exists", "data": null }
```

**응답 (400 Bad Request)** — 필수 필드 누락:
```json
{ "success": false, "message": "loginId is required", "data": null }
```

---

### GET /api/members/me

내 정보 조회. 로그인 세션 필요.

**응답 (200 OK)**:
```json
{
  "success": true,
  "message": "member found",
  "data": {
    "memberId": 1,
    "loginId": "test01",
    "nickname": "테스트회원",
    "skinType": "dry",
    "skinConcern": "보습, 진정",
    "regDate": "2026-06-30"
  }
}
```

**응답 (401 Unauthorized)** — 미로그인:
```json
{ "success": false, "message": "not logged in", "data": null }
```

*password 필드는 응답에 포함되지 않음 (`@JsonProperty(access = WRITE_ONLY)`)*

---

### PUT /api/members/me

내 정보 수정 (nickname, skinType, skinConcern). 로그인 세션 필요.

**요청 Body**:
```json
{
  "nickname": "수정회원",
  "skinType": "sensitive",
  "skinConcern": "진정, 자극"
}
```

수정 후 DB에서 재조회하여 세션 갱신.

**응답 (200 OK)**:
```json
{
  "success": true,
  "message": "profile updated",
  "data": {
    "memberId": 1,
    "loginId": "test01",
    "nickname": "수정회원",
    "skinType": "sensitive",
    "skinConcern": "진정, 자극",
    "regDate": "2026-06-30"
  }
}
```

**응답 (401 Unauthorized)** — 미로그인

---

## Auth API

### POST /api/auth/login

세션 기반 로그인.

**요청 Body**:
```json
{ "loginId": "test01", "password": "1234" }
```

로그인 성공 시 `session.setAttribute("loginMember", member)`.

**응답 (200 OK)**:
```json
{
  "success": true,
  "message": "login success",
  "data": {
    "memberId": 1,
    "loginId": "test01",
    "nickname": "테스트회원",
    "skinType": "dry",
    "skinConcern": "보습, 진정",
    "regDate": "2026-06-30"
  }
}
```

**응답 (401 Unauthorized)** — loginId/password 불일치:
```json
{ "success": false, "message": "invalid loginId or password", "data": null }
```

---

### POST /api/auth/logout

세션 무효화.

**응답 (200 OK)**:
```json
{ "success": true, "message": "logout success", "data": null }
```

---

## VO 명세 — MemberVO (`kr.ac.kopo.member.vo.MemberVO`)

| 필드 | 타입 | DB 컬럼 | 비고 |
|---|---|---|---|
| memberId | Integer | MEMBER_ID | PK |
| loginId | String | LOGIN_ID | UNIQUE NOT NULL |
| password | String | PASSWORD | SHA-256 해시, JSON 응답 제외 (WRITE_ONLY) |
| nickname | String | NICKNAME | |
| skinType | String | SKIN_TYPE | dry/oily/combination/sensitive/normal |
| skinConcern | String | SKIN_CONCERN | |
| regDate | String | REG_DATE | to_char(YYYY-MM-DD) |

---

## Recommendation API

### GET /api/recommendations/me

로그인 회원의 피부 타입 기반 상품 추천.

**Query Parameters**:

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| size | int | 20 | 반환 상품 수 |

**처리 로직**:
1. 세션에서 `loginMember` 조회 → 없으면 HTTP 401
2. `skinType` 없으면 HTTP 400
3. `skinType`(영어) → 한국어 매핑 (dry=건성, oily=지성, combination=복합성, sensitive=민감성, normal=중성)
4. `BASE_SKIN_TYPE = 매핑값` 조건으로 `RECOMMENDATION_SCORE DESC NULLS LAST` 정렬
5. 결과 없으면 fallback: 전체 상품에서 점수 순
6. 각 상품에 `reason` 생성 (skinType 일치 여부 + skinConcern 태그 매칭 + cautionLevel)

**응답 (200 OK)**:
```json
{
  "success": true,
  "message": "recommendations found",
  "data": [
    {
      "productId": 15,
      "platform": "musinsa",
      "productName": "리얼 히알루로닉 100 토너 500ml 대용량",
      "displayName": "리얼 히알루로닉 100 토너 500ml",
      "brand": "스킨1004",
      "category": "skincare",
      "avgRating": 4.8,
      "totalReviewCount": 1200,
      "baseSkinType": "복합성",
      "cautionLevel": "normal",
      "recommendationScore": 99.29,
      "recommendationTier": "top",
      "productUrl": "https://www.musinsa.com/products/1234567",
      "imageUrl": "https://image.msscdn.net/images/goods_img/20230101/1234567/1234567_1_500.jpg",
      "imageStatus": "found",
      "reason": "복합성 피부 타입 기준 추천 점수가 높습니다. 피부 고민 키워드와 관련된 태그가 포함되어 있습니다. 부정 리뷰 신호가 낮은 편입니다."
    }
  ]
}
```

**응답 (401 Unauthorized)** — 미로그인:
```json
{ "success": false, "message": "not logged in", "data": null }
```

**응답 (400 Bad Request)** — skinType 없음:
```json
{ "success": false, "message": "skin type is required", "data": null }
```

**reason 생성 규칙**:

| 조건 | reason 문구 |
|---|---|
| baseSkinType = 회원 skinType | "{skinType} 피부 타입 기준 추천 점수가 높습니다." |
| baseSkinType 불일치 (fallback) | "다양한 피부 타입에 적합한 상품으로 추천됩니다." |
| skinConcern 키워드가 TOP_NEED_TAGS/TOP_CONCERN_TAGS에 포함 | " 피부 고민 키워드와 관련된 태그가 포함되어 있습니다." |
| cautionLevel = normal | " 부정 리뷰 신호가 낮은 편입니다." |
| cautionLevel = moderate_negative_signal | " 일부 부정 리뷰 신호가 있어 상세 리뷰 확인을 권장합니다." |
| cautionLevel = high_negative_signal | " 부정 리뷰 신호가 높은 편이므로 구매 전 부정 리뷰 확인이 필요합니다." |
| cautionLevel = insufficient_evidence | " 리뷰 데이터가 충분하지 않아 신뢰도 판단에 참고가 필요합니다." |

---

## VO 명세 — RecommendationVO (`kr.ac.kopo.recommendation.vo.RecommendationVO`)

| 필드 | 타입 | DB 컬럼 | 비고 |
|---|---|---|---|
| productId | Integer | PRODUCT_ID | |
| platform | String | PLATFORM | oliveyoung/musinsa |
| productName | String | PRODUCT_NAME | 원본 상품명 |
| displayName | String | — | Service 계층 정제 (DisplayNameCleaner.clean()) |
| productUrl | String | PRODUCT_URL | 상품 상세 페이지 URL |
| imageUrl | String | IMAGE_URL | 대표 이미지 URL (og:image), imageStatus=found일 때만 non-null |
| imageStatus | String | IMAGE_STATUS | found/not_found/fetch_failed/blocked/pending |
| brand | String | BRAND | |
| category | String | CATEGORY | |
| price | Double | PRICE | nullable |
| avgRating | Double | AVG_RATING | nullable |
| totalReviewCount | Integer | TOTAL_REVIEW_COUNT | |
| overallPosRate | Double | OVERALL_POS_RATE | nullable |
| overallNegRate | Double | OVERALL_NEG_RATE | nullable |
| baseSkinType | String | BASE_SKIN_TYPE | |
| skinReviewCount | Integer | SKIN_REVIEW_COUNT | |
| skinPosRate | Double | SKIN_POS_RATE | nullable |
| skinNegRate | Double | SKIN_NEG_RATE | nullable |
| topNeedTags | String | TOP_NEED_TAGS | |
| topConcernTags | String | TOP_CONCERN_TAGS | |
| cautionLevel | String | CAUTION_LEVEL | normal/moderate_negative_signal/high_negative_signal/insufficient_evidence |
| recommendationScore | Double | RECOMMENDATION_SCORE | nullable |
| recommendationTier | String | RECOMMENDATION_TIER | |
| evidenceLevel | String | EVIDENCE_LEVEL | |
| reason | String | — | Service 계층에서 생성 (DB 컬럼 없음) |

---

## Comment API (11차)

### GET /api/products/{productId}/comments

상품 댓글 목록 조회 (STATUS=ACTIVE, 최신순). 인증 불필요.

**응답 (200 OK)**:
```json
{
  "success": true,
  "message": "comments found",
  "data": [
    { "commentId": 1, "productId": 730, "memberId": 1, "nickname": "테스트회원",
      "content": "정말 좋은 크림이에요!", "status": "ACTIVE", "createdAt": "2026-06-30 10:37" }
  ]
}
```

---

### POST /api/products/{productId}/comments

댓글 작성. 로그인 필요.

**요청 Body**: `{ "content": "내용" }`

| 조건 | 응답 |
|---|---|
| 미로그인 | 401 |
| content 없음/공백 | 400 |
| content 1000자 초과 | 400 |
| productId 없음 | 404 |
| 성공 | **201** |

---

### DELETE /api/comments/{commentId}

댓글 soft delete. **작성자 본인 또는 ADMIN** 가능.

| 조건 | 응답 |
|---|---|
| 미로그인 | 401 |
| 작성자 본인 | 200, DELETE_REASON='USER_DELETE' |
| ADMIN | 200, DELETE_REASON='ADMIN_DELETE' |
| 로그인했지만 작성자도 ADMIN도 아님 | 403 |
| commentId 없음 또는 이미 DELETED | 404 |

**삭제 사유(DELETE_REASON)**:

| 삭제 주체 | DELETE_REASON |
|---|---|
| 작성자 본인 | USER_DELETE |
| 관리자 (ADMIN) | ADMIN_DELETE |

---

## Admin API (11차)

모든 관리자 API: 미로그인 → 401, ADMIN 아님 → 403

### GET /api/admin/summary

대시보드 통계 반환.

**응답 (200 OK)**:
```json
{
  "success": true, "message": "summary ok",
  "data": {
    "productCount": 1521, "reviewCount": 323574, "memberCount": 2,
    "commentCount": 2, "activeCommentCount": 0, "deletedCommentCount": 2,
    "imageFoundProductCount": 216,
    "platformProductCounts": { "musinsa": 691, "oliveyoung": 830 },
    "sentimentCounts": { "negative": 32267, "neutral": 11458, "positive": 279849 },
    "cautionLevelCounts": { "high_negative_signal": 35, "normal": 1302, "..." : 0 }
  }
}
```

---

### GET /api/admin/comments?status=ACTIVE&size=50

댓글 목록 (전체 상태). `status` 생략 시 전체 반환.

| 파라미터 | 기본값 | 설명 |
|---|---|---|
| status | null (전체) | ACTIVE / DELETED |
| size | 50 | 최대 반환 수 |

---

### DELETE /api/admin/comments/{commentId}

댓글 soft delete (관리자 전용 경로). 내부적으로 DELETE /api/comments/{commentId}와 동일한 soft delete 메서드 재사용.
DELETE_REASON은 항상 ADMIN_DELETE.

---

## VO 명세 — CommentVO (`kr.ac.kopo.comment.vo.CommentVO`)

| 필드 | 타입 | 설명 |
|---|---|---|
| commentId | Integer | PK |
| productId | Integer | FK → BL_PRODUCTS |
| memberId | Integer | FK → BL_MEMBERS |
| nickname | String | 작성자 닉네임 (JOIN) |
| productName | String | 상품명 (관리자 목록에만 포함) |
| content | String | 댓글 내용 (최대 1000자) |
| status | String | ACTIVE / DELETED |
| createdAt | String | 작성일시 (YYYY-MM-DD HH24:MI) |
| deletedAt | String | 삭제일시 (null if ACTIVE) |
| deletedBy | Integer | 삭제 처리자 memberId (본인 또는 ADMIN) |
| deleteReason | String | USER_DELETE (본인 삭제) / ADMIN_DELETE (관리자 삭제) |

---

## VO 명세 — MemberVO 추가 필드 (11차)

| 필드 | 타입 | DB 컬럼 | 비고 |
|---|---|---|---|
| role | String | ROLE | USER / ADMIN |

---

## VO 명세 (기존)
