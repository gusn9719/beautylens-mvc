# 기능 요약

이 문서는 실제 Controller, Mapper, JSP를 기준으로 사용자 기능과 관리자 기능을 정리한 것입니다.

## 사용자 기능

| 기능명 | 화면/URL | 설명 | 관련 테이블 | 관련 Controller/Mapper | 구현 상태 |
|---|---|---|---|---|---|
| 회원가입 | `/signup` | 아이디, 비밀번호, 닉네임, 피부 타입, 피부 고민 등록 | `BL_MEMBERS` | `MemberController`, `memberMapper.xml` | 구현 |
| 로그인/로그아웃 | `/login`, `/api/auth/login`, `/api/auth/logout` | 세션 기반 로그인/로그아웃 | `BL_MEMBERS` | `AuthController`, `memberMapper.xml` | 구현 |
| 피부 타입 기반 추천 | `/recommend`, `/api/recommendations`, `/api/recommendations/me` | 피부 타입 기준 추천 상품 조회 | `BL_PRODUCTS`, `BL_PRODUCT_RATINGS`, `BL_RECOMMENDATION_FEEDBACK`, `BL_USER_PRODUCT_EVENTS` | `RecommendationController`, `recommendationMapper.xml` | 구현 |
| 상품 목록 조회 | `/products`, `/api/products` | 검색, 플랫폼, 이미지 여부, 정렬 조건으로 상품 조회 | `BL_PRODUCTS`, `BL_PRODUCT_ADMIN_FLAGS` | `ProductController`, `productMapper.xml` | 구현 |
| 상품 상세 조회 | `/products/{productId}`, `/api/products/{productId}` | 상품 요약, 추천 근거, 외부 리뷰 분석, 평가, 댓글 표시 | `BL_PRODUCTS`, `BL_REVIEWS`, `BL_PRODUCT_RATINGS`, `BL_PRODUCT_COMMENTS` | `ProductController`, `ReviewController`, `CommentController` | 구현 |
| 외부 리뷰 분석 확인 | `/products/{productId}` | 긍정/주의 리뷰를 감성 라벨 기준으로 조회 | `BL_REVIEWS` | `ReviewController`, `reviewMapper.xml` | 구현 |
| 사이트 사용자 평가 확인 | `/products/{productId}` | 내부 평균 별점, 평가 수, 내 평가 상태 확인 | `BL_PRODUCT_RATINGS` | `ProductInteractionController`, `productInteractionMapper.xml` | 구현 |
| 찜하기/해제 | 상품 카드, 상품 상세, 마이페이지 | 관심 상품 저장/해제 | `BL_PRODUCT_FAVORITES`, `BL_USER_PRODUCT_EVENTS` | `ProductInteractionController`, `productInteractionMapper.xml` | 구현 |
| 최근 본 상품 | `/mypage` | 상세 조회 이벤트 기반 최근 본 상품 표시 | `BL_USER_PRODUCT_EVENTS` | `ProductInteractionController`, `productInteractionMapper.xml` | 구현 |
| 별점/자극/재구매 평가 등록/수정/삭제 | `/products/{productId}`, `/mypage` | 사이트 내부 정량 평가 저장, 수정, 삭제 | `BL_PRODUCT_RATINGS` | `ProductInteractionController`, `productInteractionMapper.xml` | 구현 |
| 댓글 작성/수정/삭제 | `/products/{productId}`, `/mypage` | 다른 회원에게 보이는 자유 의견 작성/관리 | `BL_PRODUCT_COMMENTS` | `CommentController`, `commentMapper.xml` | 구현 |
| 댓글 신고 | `/products/{productId}` | 부적절한 댓글 신고 | `BL_COMMENT_REPORTS` | `CommentController`, `commentReportMapper.xml` | 구현 |
| 마이페이지 내 활동 확인 | `/mypage` | 프로필, 피부 타입, 얼굴 로그인, 찜, 평가, 최근 본 상품, 의견, 추천 피드백 확인 | `BL_MEMBERS`, 활동 테이블 전체 | `MemberController`, `ProductInteractionController`, `CommentController`, `FaceController` | 구현 |
| 얼굴 등록 | `/mypage` | 정면/좌/우/위/아래 촬영 후 얼굴 로그인 정보 등록 | `BL_FACE_CREDENTIALS` | `FaceController`, `faceCredentialMapper.xml` | 구현 |
| 얼굴 로그인 | `/login` | 카메라 자동 확인 후 얼굴 로그인 | `BL_FACE_CREDENTIALS`, `BL_MEMBERS` | `FaceController`, `FacePythonClient`, `faceCredentialMapper.xml` | 구현 |

## 관리자 기능

| 기능명 | 화면/URL | 설명 | 관련 테이블 | 관련 Controller/Mapper | 구현 상태 |
|---|---|---|---|---|---|
| 관리자 대시보드 | `/admin`, `/admin/dashboard`, `/api/admin/summary` | 상품, 리뷰, 회원, 댓글, 신고, 내부 평가, 얼굴 등록 회원 수 요약 | `BL_PRODUCTS`, `BL_REVIEWS`, `BL_MEMBERS`, `BL_PRODUCT_COMMENTS`, `BL_COMMENT_REPORTS`, `BL_PRODUCT_RATINGS`, `BL_FACE_CREDENTIALS` | `AdminViewController`, `AdminApiController`, `adminMapper.xml` | 구현 |
| 상품 관리 | `/admin/products`, `/api/admin/products` | 상품 검색, 플랫폼/숨김/추천 제외/품질 상태 필터 | `BL_PRODUCTS`, `BL_PRODUCT_ADMIN_FLAGS` | `AdminProductApiController`, `adminProductMapper.xml` | 구현 |
| 상품 숨김/복구 | `/admin/products` | 사용자 상품 목록/상세에서 숨김 상품 제외 | `BL_PRODUCT_ADMIN_FLAGS`, `BL_ADMIN_AUDIT_LOGS` | `AdminProductApiController`, `AdminProductService` | 구현 |
| 추천 제외/포함 | `/admin/products` | 추천 결과와 추천 fallback에서 제외/포함 | `BL_PRODUCT_ADMIN_FLAGS`, `BL_ADMIN_AUDIT_LOGS` | `AdminProductApiController`, `recommendationMapper.xml` | 구현 |
| 품질 상태 관리 | `/admin/products` | 이미지 없음, 리뷰 부족, 주의 신호 등 운영 상태 저장 | `BL_PRODUCT_ADMIN_FLAGS` | `AdminProductApiController`, `adminProductMapper.xml` | 구현 |
| 운영 메모 | `/admin/products` | 상품별 관리자 메모 저장 | `BL_PRODUCT_ADMIN_FLAGS` | `AdminProductApiController`, `adminProductMapper.xml` | 구현 |
| 댓글 관리 | `/admin/comments`, `/api/admin/comments` | 댓글 목록, 삭제, 복구 관리 | `BL_PRODUCT_COMMENTS`, `BL_ADMIN_AUDIT_LOGS` | `AdminApiController`, `AdminCommentReportApiController`, `commentMapper.xml` | 구현 |
| 신고 댓글 처리 | `/admin/comment-reports`, `/api/admin/comment-reports` | 신고 목록 확인, 신고 처리, 댓글 복구 | `BL_COMMENT_REPORTS`, `BL_PRODUCT_COMMENTS`, `BL_ADMIN_AUDIT_LOGS` | `AdminCommentReportApiController`, `commentReportMapper.xml` | 구현 |
| 관리자 활동 로그 확인 | `/admin/logs`, `/api/admin/logs` | 관리자 작업 이력 조회 | `BL_ADMIN_AUDIT_LOGS` | `AdminAuditLogApiController`, `adminAuditLogMapper.xml` | 구현 |

