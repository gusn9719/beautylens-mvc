# Phase B/C Review Bundle

작성일: 2026-06-30

기준 문서:

- `docs/27_product_redesign_prd.md`
- `docs/29_redesign_phase_execution_checklist.md`
- `docs/30_redesign_phase_implementation_report.md`

목적: Phase B/C 결과를 코드 전체 없이 검수할 수 있도록 핵심 변경, 연결 경로, 위험 지점, 검증 결과를 한 문서에 모은다.

## 1. Phase B/C 요약

### Phase B에서 바꾼 내용

- 공통 상품 카드 `BL.productCard()`를 `자세히 보기`, `찜하기` 중심으로 정리했다.
- `/products` 상품 탐색 화면에 비교 목적 안내와 상품 목록 헤더를 추가했다.
- `/recommend` 추천 화면에 추천 기준 설명과 “평가는 상세에서” 안내를 추가했다.
- 추천 카드의 `좋아요`, `별로예요`, `관심 없음` 직접 노출을 제거하고 `이 추천이 맞지 않나요?` 보조 영역으로 이동했다.
- `NOT_INTERESTED` 피드백은 사용자 문구상 `추천 숨기기`로 바꾸고, 저장 후 해당 카드를 화면에서 숨기도록 했다.

### Phase C에서 바꾼 내용

- `/products/{productId}` 상품 상세를 다음 섹션으로 재구성했다.
  - 상품 요약
  - 추천 근거
  - 외부 리뷰 분석
  - 사이트 사용자 평가
  - 내 평가 남기기
  - 회원 의견
- 사이트 사용자 평가는 입력 영역이 아니라 정량 요약 영역으로 분리했다.
- 별점, 자극 여부, 재구매 의사는 `내 평가 남기기` 영역으로 이동했다.
- 회원 댓글은 `회원 의견` 영역으로 분리했다.
- 댓글 신고 시 사용자에게 `SPAM`, `ETC` 같은 내부 사유 코드를 직접 입력시키지 않도록 수정했다.

### 변경 목적

- 상품 카드에서는 빠른 비교와 상세 진입에 집중시킨다.
- 추천 페이지가 설문 UI처럼 보이지 않게 한다.
- 상품 상세에서는 구매 판단에 필요한 정보를 순서대로 보여준 뒤 평가를 받는다.
- 외부 리뷰 분석, 사이트 내부 평가, 회원 의견의 역할을 분리한다.

### 기존 기능 유지 여부

- API 경로 변경 없음.
- DB 스키마 변경 없음.
- 상품/리뷰 원본 데이터 삭제 없음.
- 찜, 별점 저장, 자극 여부 저장, 재구매 의사 저장, 댓글 작성/삭제/신고, 최근 본 상품 기록 유지 확인.

## 2. 수정 파일 목록

### `src/main/webapp/WEB-INF/jsp/demo/products.jsp`

- 수정 이유: 상품 탐색 화면을 단순 목록이 아니라 비교/탐색 화면처럼 보이게 정리.
- 주요 변경 내용:
  - `page-shell` 적용.
  - `browse-intro` 추가.
  - 상품 목록 섹션 헤더 추가.
  - 기존 검색, 플랫폼 필터, 피부 타입 필터, 이미지 필터, 정렬, 더 보기, 찜 이벤트 유지.
- 회귀 위험:
  - `BL.productCard()` 변경 영향으로 카드 레이아웃이 달라진다.
  - 필터 이벤트 자체는 유지했지만 실제 브라우저 클릭 검증은 하지 못했다.

### `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`

- 수정 이유: 추천 화면을 설문/피드백 수집 화면이 아니라 추천 후보 비교 화면처럼 정리.
- 주요 변경 내용:
  - 추천 기준 안내 카드 추가.
  - “평가는 상세에서” 안내 추가.
  - 추천 결과 섹션 헤더 추가.
  - 추천 카드는 `BL.productCard(p, { reason: true, feedback: true })`를 계속 사용.
  - 기존 피부 타입, 플랫폼, 이미지 필터 이벤트 유지.
- 회귀 위험:
  - 추천 피드백 UI가 `details` 안으로 들어가 사용자가 덜 발견할 수 있다.
  - `NOT_INTERESTED`는 저장 후 카드가 즉시 숨겨지므로 목록 개수가 줄어든다.

### `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`

- 수정 이유: 상품 상세를 구매 판단 흐름에 맞게 재구성.
- 주요 변경 내용:
  - 상품 요약, 추천 근거, 외부 리뷰 분석, 사이트 사용자 평가, 내 평가 남기기, 회원 의견 섹션 분리.
  - 추천 피드백 보조 UI를 추천 근거 영역에 배치.
  - 댓글 신고 UX에서 내부 코드 입력 prompt 제거.
  - 기존 API 호출 유지:
    - `/api/products/{productId}`
    - `/api/products/{productId}/reviews/positive`
    - `/api/products/{productId}/reviews/negative`
    - `/api/products/{productId}/events`
    - `/api/products/{productId}/favorite`
    - `/api/products/{productId}/rating`
    - `/api/products/{productId}/comments`
    - `/api/comments/{commentId}`
    - `/api/comments/{commentId}/report`
- 회귀 위험:
  - 상세 화면은 API 연결이 가장 많아 렌더링 중 한 API 실패가 UX에 영향을 줄 수 있다.
  - 사이트 사용자 평가의 자극/재구매 집계값은 현재 상세 API에 없어 “평가 수집 중” 문구로 대체했다.

### `src/main/webapp/assets/js/ui.js`

- 수정 이유: 공통 상품 카드와 추천 피드백 UX를 정리.
- 주요 변경 내용:
  - `BL.productCard()` 구조 변경.
  - 카드 기본 버튼을 `자세히 보기`, `찜하기` 중심으로 정리.
  - 추천 피드백을 `details.recommend-feedback` 내부로 이동.
  - `NOT_INTERESTED` 저장 후 `.product-card.is-hidden-recommendation` 적용.
  - `BL.ynLabel()` 추가. 현재 주요 사용처는 향후 원시 Y/N 표시 정리에 대비한 유틸이다.
- 회귀 위험:
  - `BL.productCard()`는 `/products`, `/recommend`, `/mypage` 최근 본 상품에서 재사용되므로 영향 범위가 넓다.
  - 추천 피드백 버튼은 숨겨진 보조 UI 안에 있으므로 이벤트 위임이 유지되는지 주의해야 한다.

### `src/main/webapp/assets/css/beautylens.css`

- 수정 이유: 상품 카드, 추천 안내, 상세 섹션의 서비스형 레이아웃 보강.
- 주요 변경 내용:
  - `product-card-badges`, `recommend-feedback`, `browse-intro`, `recommend-guide` 추가.
  - `detail-summary`, `detail-summary-card`, `detail-feedback`, `comment-form-panel` 추가.
  - 모바일에서 `recommend-guide`, `detail-summary`, 카드 버튼이 1열로 내려오도록 보강.
- 회귀 위험:
  - 공통 `.product-card-actions` 그리드 변경이 다른 카드형 UI에 영향을 줄 수 있다.
  - 실제 모바일 브라우저 렌더링은 HTTP 확인만 했고 시각 검수는 하지 못했다.

### `docs/29_redesign_phase_execution_checklist.md`

- 수정 이유: Phase B/C 완료 상태 기록.
- 주요 변경 내용:
  - Phase B 상태를 DONE으로 변경.
  - Phase C 상태를 DONE으로 변경.
  - Phase B/C 검수 항목 체크 완료 처리.
- 회귀 위험:
  - 문서 변경만 있으므로 런타임 영향 없음.

### `docs/30_redesign_phase_implementation_report.md`

- 수정 이유: Phase B/C 구현 결과, 검증 결과, 남은 문제 기록.
- 주요 변경 내용:
  - Phase B/C 변경 요약.
  - 빌드, URL, API 검증 결과.
  - 브라우저 클릭/콘솔 검증 미수행 사실 기록.
- 회귀 위험:
  - 문서 변경만 있으므로 런타임 영향 없음.

## 3. 상품 카드 변경 상세

### 기존 카드 구조

- 상품 이미지
- 브랜드/플랫폼
- 상품명
- 추천 점수, 서비스 점수, 주의 배지
- 추천 페이지에서는 사이트 반응 표시
- 추천 이유
- 자세히 보기
- 찜
- 추천 페이지에서는 좋아요/별로예요/관심 없음 버튼 3개 직접 노출

### 변경 후 카드 구조

- 상품 이미지
- 브랜드/플랫폼
- 상품명
- 추천 점수, 서비스 반응, 주의 배지
- 짧은 추천 이유 또는 외부 리뷰 데이터 안내
- 사이트 평가/찜 요약
- 기본 행동:
  - 자세히 보기
  - 찜하기
- 보조 행동:
  - `이 추천이 맞지 않나요?`
  - 추천 숨기기
  - 도움 됐어요
  - 맞지 않아요

### `BL.productCard()` 변경 내용

핵심 변경 블록 요약:

```javascript
const feedbackActions = opts.feedback ? `
  <details class="recommend-feedback">
    <summary>이 추천이 맞지 않나요?</summary>
    <div class="feedback-actions" aria-label="추천 피드백">
      <button ... data-rec-feedback="NOT_INTERESTED">추천 숨기기</button>
      <button ... data-rec-feedback="LIKE">도움 됐어요</button>
      <button ... data-rec-feedback="DISLIKE">맞지 않아요</button>
    </div>
  </details>` : "";
```

```javascript
<div class="product-card-actions">
  <a class="btn btn-primary" href="${BL.url("/products/" + item.productId)}">자세히 보기</a>
  ${favoriteAction}
</div>
```

### 이 함수가 호출되는 화면 목록

- `/products`: `BL.productCard(p)`
- `/recommend`: `BL.productCard(p, { reason: true, feedback: true })`
- `/mypage` 최근 본 상품: `BL.productCard(p)`

### `/products`, `/recommend`, `/mypage` 최근 본 상품에 미치는 영향

- `/products`: 카드의 기본 버튼이 `자세히 보기`, `찜하기`로 정리된다.
- `/recommend`: 피드백 버튼은 보조 `details` 영역으로 숨겨진다.
- `/mypage` 최근 본 상품: 같은 카드 디자인이 적용된다. 피드백 UI는 나오지 않는다.

### 찜 버튼 이벤트 유지 방식

- 버튼 속성 유지: `data-favorite-product="{productId}"`
- JS 함수 유지: `BL.handleFavoriteClick(button)`
- fetch URL 유지: `POST /api/products/{productId}/favorite`

### 자세히 보기 이동 유지 방식

- 링크 유지: `href="${BL.url("/products/" + item.productId)}"`
- 별도 fetch 없이 일반 상세 페이지 이동.

### 추천 피드백 버튼 이동 위치

- 기존: 카드 하단에 3개 버튼 직접 노출.
- 변경: 추천 카드 내부 `details.recommend-feedback` 안의 보조 UI.
- fetch URL 유지: `POST /api/products/{productId}/recommendation-feedback`

## 4. 추천 페이지 변경 상세

### 추천 페이지의 기존 문제

- 추천 카드가 좋아요/별로예요/관심 없음 버튼을 바로 노출해 설문 UI처럼 보였다.
- 사용자가 상품 정보를 충분히 보기 전에 추천 피드백을 누르기 쉬웠다.
- 추천 기준 설명이 약해 추천 결과의 신뢰 근거가 부족해 보였다.

### 변경 후 추천 흐름

1. 추천 기준 설명을 먼저 읽는다.
2. 피부 타입을 선택한다.
3. 플랫폼/이미지 필터를 조정한다.
4. 추천 결과 카드에서 `자세히 보기` 또는 `찜하기`를 우선 수행한다.
5. 추천이 맞지 않으면 보조 UI에서 `추천 숨기기` 등을 선택한다.

### 좋아요/별로예요/관심 없음 직접 노출 제거 방식

- `좋아요`: `도움 됐어요`로 문구 변경 후 보조 UI로 이동.
- `별로예요`: `맞지 않아요`로 문구 변경 후 보조 UI로 이동.
- `관심 없음`: `추천 숨기기`로 문구 변경 후 보조 UI로 이동.

### 추천 숨기기 동작 방식

- `data-rec-feedback="NOT_INTERESTED"` 버튼 클릭.
- 기존 `BL.handleRecommendationFeedbackClick()` 사용.
- API 저장 성공 후 현재 카드에 `.is-hidden-recommendation` 클래스 추가.
- CSS에서 `.product-card.is-hidden-recommendation { display: none; }` 처리.

### 기존 추천 피드백 API 유지 여부

- 유지.
- URL: `POST /api/products/{productId}/recommendation-feedback`
- Body: `{ feedbackType: "LIKE" | "DISLIKE" | "NOT_INTERESTED" }`

### 추천 피드백 데이터 품질 관점에서 좋아진 점

- 추천 피드백이 카드 메인 버튼이 아니므로 무의미한 즉시 클릭 가능성이 줄었다.
- 깊은 평가는 상품 상세에서 하도록 안내해 별점/자극/재구매 데이터 품질을 보호한다.
- `추천 숨기기`는 가벼운 선호 제외 신호로 분리된다.

### 아직 부족한 점

- 보조 UI가 `details`라 사용자가 발견하지 못할 수 있다.
- 추천 숨김 후 되돌리기 UI는 아직 없다.
- 추천 숨김은 화면에서만 즉시 숨기며, 현재 목록 보충 로딩은 하지 않는다.

## 5. 상품 상세 변경 상세

### 상품 요약 섹션

- 표시 데이터:
  - 이미지
  - 브랜드
  - 플랫폼
  - 상품명
  - 카테고리
  - 추천 점수
  - 서비스 반응
  - 주의 신호
  - 찜하기
  - 외부 상품 페이지
- 기존 JSP 변수/API 연결:
  - `BL.get("/api/products/" + productId)`
  - `product.imageUrl`, `product.brand`, `product.platform`, `product.displayName`, `product.recommendationScore`, `product.serviceScore`, `product.cautionLevel`, `product.productUrl`
- 빈 데이터 문구:
  - 브랜드 없음: `브랜드 정보 없음`
  - 카테고리 없음: `카테고리 정보 준비 중`
- 기능 회귀 위험:
  - `product.productUrl`이 없는 상품은 외부 상품 페이지 버튼이 표시되지 않는다.

### 추천 근거 섹션

- 표시 데이터:
  - 피부 타입 기준
  - 긍정 신호
  - 주의 신호
  - 내부 반응
  - 추천 피드백 보조 UI
- 기존 JSP 변수/API 연결:
  - `product.baseSkinType`
  - `product.topNeedTags`
  - `product.topConcernTags`
  - `product.cautionLevel`
  - `product.siteRatingAvg`
  - `product.siteRatingCount`
  - `BL.handleRecommendationFeedbackClick()`
- 빈 데이터 문구:
  - `리뷰 신호 분석 중`
  - `아직 부족`
- 기능 회귀 위험:
  - 추천 피드백이 상세에도 추가되어 추천 페이지와 같은 API를 호출한다. 중복 저장 정책은 서버 구현에 의존한다.

### 외부 리뷰 분석 섹션

- 표시 데이터:
  - 긍정 포인트
  - 주의 포인트
  - 외부 리뷰 수 기반 설명
- 기존 JSP 변수/API 연결:
  - `GET /api/products/{productId}/reviews/positive`
  - `GET /api/products/{productId}/reviews/negative`
  - `reviewItem(r, "positive" | "negative")`
- 빈 데이터 문구:
  - `긍정 리뷰가 없습니다.`
  - `주의 리뷰가 없습니다.`
  - 외부 리뷰 수 부족 시 `외부 리뷰 수가 부족해 분석 근거가 제한적입니다.`
- 기능 회귀 위험:
  - 긍정/부정 리뷰 API 중 하나가 실패하면 현재 전체 상세 렌더 흐름이 catch로 빠질 수 있다.

### 사이트 사용자 평가 섹션

- 표시 데이터:
  - 내부 평균 별점
  - 평가 수
  - 자극 여부 요약
  - 재구매 의사 요약
- 기존 JSP 변수/API 연결:
  - `product.siteRatingAvg`
  - `product.siteRatingCount`
- 빈 데이터 문구:
  - `아직 사이트 사용자 평가가 부족합니다. 평가가 쌓이면 별점, 자극 여부, 재구매 의사 요약에 반영됩니다.`
- 기능 회귀 위험:
  - 자극 여부/재구매 의사 집계값이 현재 상세 API에 없어 실제 비율 대신 `평가 수집 중`으로 표시한다.

### 내 평가 남기기 섹션

- 표시 데이터:
  - 별점 select
  - 자극 여부 select
  - 재구매 의사 select
  - 평가 메모 textarea
- 기존 JSP 변수/API 연결:
  - `GET /api/products/{productId}/rating`
  - `POST /api/products/{productId}/rating`
  - `#site-rating`
  - `#irritation-yn`
  - `#repurchase-yn`
  - `#rating-text`
- 빈 데이터 문구:
  - 미로그인: `사이트 내부 평가는 로그인이 필요합니다.`
- 기능 회귀 위험:
  - 기존 저장 API는 유지했지만 실제 브라우저 select 클릭 검증은 하지 못했다. API 레벨 저장은 확인했다.

### 회원 의견 섹션

- 표시 데이터:
  - 자유 댓글 작성 form
  - 댓글 목록
  - 신고 버튼
  - 삭제 버튼
- 기존 JSP 변수/API 연결:
  - `GET /api/products/{productId}/comments`
  - `POST /api/products/{productId}/comments`
  - `DELETE /api/comments/{commentId}`
  - `POST /api/comments/{commentId}/report`
- 빈 데이터 문구:
  - `아직 회원 댓글이 없습니다.`
  - 미로그인: `댓글 작성은 로그인이 필요합니다.`
- 기능 회귀 위험:
  - 신고 사유는 현재 UI에서 상세 선택을 받지 않고 기본 `ETC`로 저장한다.

## 6. 유지한 기능과 연결 경로

### 찜

- 관련 버튼: `#detail-favorite`, `[data-favorite-product]`
- 관련 JS 함수: `BL.handleFavoriteClick(button)`
- fetch URL:
  - `POST /api/products/{productId}/favorite`
- 검증 결과:
  - test01 세션에서 상품 ID `1446` 기준 API 성공 확인.

### 별점 저장

- 관련 입력:
  - `#site-rating`
  - `#rating-form`
- 관련 JS:
  - `ratingFormEl.addEventListener("submit", ...)`
- fetch URL:
  - `POST /api/products/{productId}/rating`
- 검증 결과:
  - test01 세션에서 `rating=4` 저장 후 조회 확인.

### 자극 여부 저장

- 관련 입력:
  - `#irritation-yn`
- fetch URL:
  - `POST /api/products/{productId}/rating`
- 검증 결과:
  - test01 세션에서 `irritationYn=N` 저장 후 조회 확인.

### 재구매 의사 저장

- 관련 입력:
  - `#repurchase-yn`
- fetch URL:
  - `POST /api/products/{productId}/rating`
- 검증 결과:
  - test01 세션에서 `repurchaseYn=Y` 저장 후 조회 확인.

### 댓글 작성

- 관련 입력:
  - `#comment-form`
  - `#comment-content`
- fetch URL:
  - `POST /api/products/{productId}/comments`
- 검증 결과:
  - test01 세션에서 댓글 생성 성공 확인.

### 댓글 삭제

- 관련 버튼:
  - `[data-delete-comment]`
- fetch URL:
  - `DELETE /api/comments/{commentId}`
- 검증 결과:
  - 생성한 검증 댓글 삭제 성공 확인.

### 댓글 신고

- 관련 버튼:
  - `[data-report-comment]`
- fetch URL:
  - `POST /api/comments/{commentId}/report`
- 검증 결과:
  - 생성한 검증 댓글 신고 성공 확인.

### 최근 본 상품 기록

- 관련 JS:
  - 상세 렌더 후 `BL.post("/api/products/" + productId + "/events", { eventType: "DETAIL_VIEW" })`
- fetch URL:
  - `POST /api/products/{productId}/events`
- 검증 결과:
  - test01 세션에서 이벤트 기록 API 성공, 최근 본 상품 API 200 확인.

### 상품 상세 이동

- 관련 링크:
  - `href="${BL.url("/products/" + item.productId)}"`
- 검증 결과:
  - `/beautylens-mvc/products/1446` HTTP 200 확인.

### 외부 상품 페이지 이동

- 관련 링크:
  - `product.productUrl`이 있을 때 `target="_blank"` 링크 표시.
- 검증 결과:
  - 링크 렌더 조건은 코드상 유지. 실제 외부 사이트 클릭은 검증하지 않았다.

## 7. UX/UI 자체 평가

### 사용자의 1순위 행동이 명확한가?

- 상품 카드에서는 `자세히 보기`가 primary 버튼, `찜하기`가 보조 버튼으로 정리되어 명확하다.
- 추천 페이지에서도 상품 상세로 이동하는 흐름이 더 분명해졌다.

### 상품 카드의 버튼 수가 적절한가?

- 기본 노출 버튼은 2개로 줄었다.
- 추천 피드백은 보조 UI 안에 들어가 카드가 덜 복잡해졌다.

### 추천 페이지가 설문 UI처럼 보이지 않는가?

- 기존처럼 좋아요/별로예요/관심 없음이 바로 보이지 않는다.
- 다만 `details`를 열면 피드백 버튼 3개가 있으므로 보조 UI의 문구와 위치 검수는 필요하다.

### 상품 상세가 구매 판단 순서대로 구성되었는가?

- 요약, 추천 근거, 외부 리뷰 분석, 사이트 사용자 평가, 내 평가, 회원 의견 순서로 재배치했다.

### 외부 리뷰 분석, 사이트 사용자 평가, 회원 의견이 명확히 분리되었는가?

- 섹션 제목과 설명 문구로 분리했다.
- 외부 리뷰는 긍정/주의 포인트, 사이트 평가는 정량 요약, 회원 의견은 자유 댓글로 분리했다.

### 상품을 충분히 보기 전에 깊은 평가를 강요하지 않는가?

- 카드에서는 별점/자극/재구매 입력을 노출하지 않는다.
- 상세에서 정보 섹션을 본 뒤 `내 평가 남기기`가 나온다.

### 빈 상태 문구가 자연스러운가?

- 상품 카드: `아직 내부 평가가 부족합니다.`
- 상세 평가 요약: `아직 사이트 사용자 평가가 부족합니다...`
- 리뷰: `긍정 리뷰가 없습니다.`, `주의 리뷰가 없습니다.`
- 댓글: `아직 회원 댓글이 없습니다.`

### 원시 상태값이나 개발자 용어가 사용자에게 보이지 않는가?

- 사용자 화면 JSP/JS 기준 기술 용어 검색 결과 매칭 없음.
- 상세 신고 UI에서 내부 신고 코드 prompt 제거.

### 모바일에서 크게 깨질 위험은 없는가?

- CSS 미디어쿼리에서 `detail-summary`, `recommend-guide`, `product-card-actions`를 1열로 내려오게 했다.
- 실제 모바일 브라우저 시각 검수는 하지 못했다.

### 실제 추천/커머스 서비스처럼 신뢰감이 있는가?

- 추천 기준과 외부 리뷰 분석을 먼저 보여주도록 바뀌어 이전보다 신뢰 근거가 명확하다.
- 아직 실제 이미지 품질, 카드 높이 균일성, 모바일 시각 검수는 추가 확인이 필요하다.

## 8. 검증 결과

### `mvn clean package` 결과

- 성공.

### 확인한 URL

- `http://localhost:8088/beautylens-mvc/products` 200
- `http://localhost:8088/beautylens-mvc/recommend` 200
- `http://localhost:8088/beautylens-mvc/mypage` 200
- `http://localhost:8088/beautylens-mvc/products/1446` 200
- `http://localhost:8088/beautylens-mvc/assets/css/beautylens.css` 200
- `http://localhost:8088/beautylens-mvc/assets/js/ui.js` 200

### 확인한 API

- `GET /api/products?sortBy=score&size=1`
- `GET /api/products/1446`
- `POST /api/products/1446/favorite`
- `POST /api/products/1446/recommendation-feedback`
- `POST /api/products/1446/events`
- `POST /api/products/1446/rating`
- `GET /api/products/1446/rating`
- `POST /api/products/1446/comments`
- `GET /api/products/1446/comments`
- `POST /api/comments/{commentId}/report`
- `DELETE /api/comments/{commentId}`
- `GET /api/members/me/recent-products`

### 로그인 계정으로 확인한 항목

- 계정: `test01 / 1234`
- 확인:
  - 찜 API 정상
  - 추천 피드백 API 정상
  - 상세 조회 이벤트 API 정상
  - 별점/자극/재구매 저장 정상
  - 댓글 작성/신고/삭제 정상
  - 최근 본 상품 API 정상

### 실제 브라우저 클릭 테스트 여부

- 하지 못했다.
- 대신 HTTP 200, 정적 JS 문법 검사, 로그인 세션 기반 API 호출로 대체했다.

### 브라우저 개발자도구 콘솔 확인 여부

- 하지 못했다.

### HTTP 확인만 한 항목

- `/products`
- `/recommend`
- `/mypage`
- `/products/1446`
- CSS/JS 정적 파일

### 실제 클릭 확인한 항목

- 없음.
- 기능은 API 호출로 검증했다.

## 9. 기술 용어 검색 결과

검색 대상:

- `src/main/webapp/WEB-INF/jsp`
- `src/main/webapp/assets/js`

검색 문자열:

- `insightface-buffalo_l`
- `buffalo_l`
- `InsightFace`
- `embedding`
- `vector`
- `face vector`
- `FastAPI`
- `Python server`
- `modelName`
- `얼굴 특징`
- `얼굴 인증 서버`

결과:

- 사용자 화면 JSP/JS 기준 직접 노출 매칭 없음.
- 서버 내부 Java 코드에는 얼굴 인증 관련 내부 변수/서비스명이 있을 수 있으나, 이번 검색 기준에서는 사용자 출력 대상이 아니다.

## 10. git diff 요약

### 명령 실행 결과

현재 `D:\Lecture\spring-workspace\beautylens-mvc`는 Git 저장소가 아니어서 아래 명령은 사용할 수 없었다.

```bash
git diff --stat
git diff -- src/main/webapp/WEB-INF/jsp/demo/products.jsp
git diff -- src/main/webapp/WEB-INF/jsp/demo/recommend.jsp
git diff -- src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp
git diff -- src/main/webapp/assets/js/ui.js
git diff -- src/main/webapp/assets/css/beautylens.css
```

실제 오류:

```text
warning: Not a git repository. Use --no-index to compare two paths outside a working tree
fatal: not a git repository (or any of the parent directories): .git
```

### 핵심 변경 블록 요약

#### `ui.js`

- `BL.productCard()`에서 추천 피드백 직접 버튼을 제거하고 `details.recommend-feedback`로 이동.
- 찜 URL 유지:

```javascript
await BL.post("/api/products/" + productId + "/favorite", {});
```

- 추천 피드백 URL 유지:

```javascript
await BL.post("/api/products/" + productId + "/recommendation-feedback", { feedbackType: feedbackType });
```

#### `recommend.jsp`

- 카드 생성 호출 유지:

```javascript
BL.productCard(p, { reason: true, feedback: true })
```

- 이벤트 위임 유지:

```javascript
const favoriteButton = event.target.closest("[data-favorite-product]");
const feedbackButton = event.target.closest("[data-rec-feedback]");
```

#### `products.jsp`

- 카드 생성 호출 유지:

```javascript
BL.productCard(p)
```

- 찜 이벤트 위임 유지:

```javascript
const favoriteButton = event.target.closest("[data-favorite-product]");
```

#### `product_detail.jsp`

- 최근 본 상품 기록 유지:

```javascript
await BL.post("/api/products/" + productId + "/events", { eventType: "DETAIL_VIEW" });
```

- 평가 저장 URL 유지:

```javascript
await BL.post("/api/products/" + productId + "/rating", body);
```

- 댓글 작성 URL 유지:

```javascript
await BL.post("/api/products/" + productId + "/comments", { content: content });
```

- 댓글 삭제/신고 URL 유지:

```javascript
await BL.del("/api/comments/" + button.dataset.deleteComment);
await BL.post("/api/comments/" + reportButton.dataset.reportComment + "/report", {
  reasonType: "ETC",
  reasonText: "상품 상세에서 신고"
});
```

## 11. 남은 문제

### 실제 클릭 테스트를 못 한 부분

- 상품 카드 `자세히 보기` 클릭
- 상품 카드 `찜하기` 클릭
- 추천 카드 `details` 열기
- 추천 숨기기 클릭
- 상세 화면 찜 버튼 클릭
- 평가 form submit
- 댓글 작성/삭제/신고 버튼 클릭

API로는 검증했지만 실제 브라우저 클릭 이벤트는 확인하지 못했다.

### 브라우저 콘솔 확인을 못 한 부분

- 개발자도구 Console 오류 확인 미수행.
- 실제 DOM 이벤트 충돌 여부는 브라우저에서 추가 확인 필요.

### 데이터 집계가 부족해서 임시 문구로 처리한 부분

- 상세 화면의 자극 여부 요약.
- 상세 화면의 재구매 의사 요약.
- 현재 상세 API에 집계 비율이 없어 `평가 수집 중`으로 표시한다.

### Phase D/E 전에 주의할 부분

- Phase D는 얼굴 등록/로그인 wizard만 다룬다. 이번 Phase에서 얼굴 JS는 수정하지 않았다.
- Phase E는 관리자 UX만 다룬다. 이번 Phase에서 관리자 JSP/API는 수정하지 않았다.
- 공통 카드 함수가 바뀌었으므로 Phase D/E 후에도 `/products`, `/recommend`, `/mypage` 최근 본 상품 회귀 확인이 필요하다.

### 추가 검수가 필요한 부분

- 모바일 실제 렌더링.
- 추천 카드 보조 피드백이 너무 숨겨져 있지 않은지.
- 상세 화면 섹션 순서가 실제 사용자에게 자연스러운지.
- 외부 리뷰 API 실패 시 상세 전체가 실패하지 않도록 추가 방어가 필요한지.

## 13. 전역 UX 품질 기준 보강 후 추가 변경

### 문서 기준 보강

- `docs/27_product_redesign_prd.md`에 `19. 전역 UX 품질 기준` 추가.
- `docs/29_redesign_phase_execution_checklist.md`의 공통 Phase Gate에 개발자식 값 노출, redirect, 부분 API 실패, 실제 브라우저 검증, Console 확인 기준 추가.

### 코드 보강

- `ui.js`
  - `BL.currentInternalPath()`
  - `BL.safeRedirectPath()`
  - `BL.loginPath()`
  - `BL.requireLoginView()` redirect 반영
  - 찜/추천 피드백 401 시 redirect 포함 로그인 페이지 이동
- `login.jsp`
  - `redirect` 파라미터를 내부 경로일 때만 로그인 후 이동 대상으로 사용
  - 외부 URL, `//`, 제어 문자, `javascript:` 계열 값은 차단
- `product_detail.jsp`
  - 긍정 리뷰/주의 리뷰 API를 섹션별 독립 로딩으로 변경
  - `Array.isArray()`로 배열 여부 확인
  - 빈 리뷰 문구를 `아직 긍정 리뷰가 충분하지 않습니다.`, `아직 주의 리뷰가 충분하지 않습니다.`로 변경
  - 리뷰 API 실패 시 해당 섹션만 실패 문구 표시
  - 평가 메모 textarea를 compact 선택 입력으로 축소
  - 회원 의견은 댓글 목록 중심으로 배치하고 작성 버튼을 `의견 작성`으로 변경

### 추가 검증 결과

- `mvn clean package` 성공
- 전체 주요 URL 200:
  - `/`
  - `/login`
  - `/signup`
  - `/products`
  - `/recommend`
  - `/products/1446`
  - `/mypage`
  - `/admin`
  - `/admin/dashboard`
  - `/admin/products`
  - `/admin/comments`
  - `/admin/comment-reports`
  - `/admin/logs`
- test01 API 레벨 검증:
  - 찜 정상
  - 별점 저장 정상
  - 자극 여부 저장 정상
  - 재구매 의사 저장 정상
  - 댓글 작성 정상
  - 댓글 신고 정상
  - 댓글 삭제 정상
  - 최근 본 상품 기록 정상

### 실제 브라우저 검증 상태

- 현재 Codex 세션에서 in-app browser가 제공되지 않아 실제 브라우저 클릭 검증은 수행하지 못했다.
- 개발자도구 Console 확인도 수행하지 못했다.
- 따라서 이 문서는 브라우저 검수 전 사전 점검 번들이며, 실제 클릭 검증은 별도 수행이 필요하다.

## 12. ChatGPT 검수 요청용 요약

Phase B/C 구현 완료 상태입니다.

검수받고 싶은 핵심:

- 상품 카드가 실제 추천/커머스 서비스처럼 비교와 상세 진입에 집중되어 보이는지.
- 추천 페이지가 설문 UI처럼 보이지 않고, 피드백 수집 위치가 적절한지.
- 상품 상세에서 추천 근거, 외부 리뷰 분석, 사이트 사용자 평가, 내 평가, 회원 의견이 명확히 분리되어 있는지.

가장 위험한 변경 3개:

1. 공통 `BL.productCard()` 변경  
   `/products`, `/recommend`, `/mypage` 최근 본 상품에 모두 영향이 있다.
2. 추천 피드백을 `details` 보조 UI로 이동  
   이벤트는 유지했지만 실제 브라우저 클릭 검증은 못 했다.
3. 상품 상세 대규모 섹션 재배치  
   여러 API가 연결된 화면이라 일부 API 실패 시 UX 영향 가능성이 있다.

실제 확인한 것:

- `mvn clean package` 성공.
- `/products`, `/recommend`, `/mypage`, `/products/1446`, CSS/JS 정적 파일 HTTP 200.
- test01 세션으로 찜, 추천 피드백, 최근 본 상품 이벤트, 별점/자극/재구매 저장, 댓글 작성/신고/삭제 API 성공.
- 사용자 화면 기술 용어 검색 결과 직접 노출 없음.

확인하지 못한 것:

- 실제 브라우저 클릭 테스트.
- 브라우저 개발자도구 Console 오류.
- 모바일 실제 화면 검수.
- 외부 상품 페이지 링크 실제 이동.

내가 판단해줬으면 하는 것:

- Phase B/C UX 흐름이 PRD 의도에 맞는지.
- 카드에서 정보량과 버튼 수가 적절한지.
- 상세 화면 섹션 순서와 문구가 실제 서비스처럼 신뢰감을 주는지.
- `details` 기반 추천 피드백 UI가 너무 숨겨져 있거나 어색하지 않은지.
- Phase D/E 전에 반드시 보완해야 할 회귀 위험이 있는지.
