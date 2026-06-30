# BeautyLens Feature Expansion Master Plan

작성일: 2026-06-30

## 1. 현재 부족한 점

- 사용자 기능 부족: 현재 사용자는 상품 탐색, 추천 확인, 댓글 작성, 마이페이지 프로필/댓글 확인 정도만 가능하다. 상품 찜, 별점, 만족도, 최근 본 상품, 추천 피드백 같은 반복 방문 기능이 부족하다.
- 관리자 기능 부족: 관리자 대시보드는 기본 통계와 댓글 삭제 중심이다. 상품 숨김, 추천 제외, 메인 노출, 품질 상태, 운영 메모처럼 실제 운영자가 매일 쓰는 기능이 부족하다.
- 내부 사용자 피드백 데이터 부족: 기존 데이터는 크롤링 리뷰 기반이며, 사이트 안에서 발생하는 사용자 행동 데이터가 거의 쌓이지 않는다.
- 추천 고도화 근거 부족: `RECOMMENDATION_SCORE`는 크롤링 리뷰 기반 점수로 의미가 있지만, 사이트 내부 별점/찜/조회/추천 피드백을 반영하지 못한다.
- 상품 운영 기능 부족: 이미지 없는 상품, 리뷰 수 부족 상품, 높은 주의 신호 상품을 운영 상태로 분류하고 관리할 수 없다.
- 신고/품질 관리 기능 부족: 댓글 신고, 처리 상태, 복구, 관리자 삭제 사유, 관리자 작업 로그가 부족하다.

## 2. 추가할 사용자 기능 후보

- 상품 찜하기: 상품 카드와 상세에서 저장.
- 찜 해제: 동일 위치와 마이페이지에서 해제.
- 찜한 상품 마이페이지에서 보기: 계정 기반 목록 제공.
- 상품 별점 주기: 사이트 내부 별점으로 크롤링 평점과 분리.
- 같은 피부 타입 기준 만족도 평가: 평가 당시 피부 타입을 저장해 추천 품질 분석에 사용.
- 자극 여부 평가: `IRRITATION_YN`으로 저장.
- 재구매 의사 평가: `REPURCHASE_YN`으로 저장.
- 추천 피드백 좋아요: 추천 결과가 적합할 때 저장.
- 추천 피드백 별로예요: 추천 결과가 부적합할 때 저장.
- 추천 피드백 관심 없음: 추천에서 숨김 신호로 저장.
- 최근 본 상품: 상세 조회 이벤트 기반으로 마이페이지에 표시.
- 상품 조회 기록: `VIEW`, `DETAIL_VIEW` 이벤트 저장.
- 상품 비교: Phase 후속 확장 후보. 첫 구현에서는 DB/API 기반만 마련하고 UI는 제외 가능.
- 댓글 신고: 사유와 함께 신고.
- 댓글 추천 또는 도움돼요: Phase 후속 확장 후보. 댓글 품질 지표로 확장 가능.
- 내가 평가한 상품 보기: 마이페이지에서 별점/자극/재구매 상태 확인.
- 내가 남긴 활동 보기: 댓글, 찜, 평가, 최근 본 상품, 추천 피드백 통합.
- 추천에서 숨기기: `NOT_INTERESTED` 피드백으로 처리.
- 관심 없는 상품 제외: 추천 API에서 로그인 사용자 기준 제외.

## 3. 추가할 관리자 기능 후보

- 상품 숨김/복구: 일반 상품 목록과 상세 노출 제어.
- 상품 추천 제외: 추천 결과에서만 제외.
- 메인 노출 상품 지정: 홈/관리자에서 강조 상품 후보로 활용.
- 이미지 없는 상품 관리: 이미지 URL/상태가 없는 상품 필터.
- 이미지 재수집 대상 관리: `QUALITY_STATUS=IMAGE_MISSING` 또는 운영 메모로 표시.
- 상품 품질 상태 관리: `NORMAL`, `IMAGE_MISSING`, `LOW_REVIEW`, `HIGH_CAUTION`, `NAME_REVIEW_NEEDED`, `LINK_BROKEN`.
- 상품별 댓글 수 보기: 내부 활동 지표.
- 상품별 사이트 내부 별점 보기: 크롤링 평점과 분리.
- 상품별 찜 수 보기: 관심도 지표.
- 상품별 조회 수 보기: 노출/관심 지표.
- 신고 댓글 관리: 신고 목록, 상태, 처리.
- 댓글 복구: 실수 삭제 또는 신고 처리 후 복구.
- 관리자 삭제 사유 관리: 댓글 삭제 사유와 신고 처리 결과 저장.
- 회원 활동 내역 보기: Phase 후속 확장 후보.
- 피부 타입별 회원 수: 관리자 대시보드에 추가.
- 얼굴 등록 회원 수: 기존 얼굴 인증 테이블 기반 표시.
- 관리자 활동 로그: 상품/댓글/신고 처리 작업 추적.
- 추천 품질 관리 대시보드: 내부 별점, 피드백, 숨김/추천 제외 상태 표시.
- 위험 상품 목록: 부정 리뷰 비율 높음, 이미지 없음, 리뷰 수 부족, 점수는 높은데 caution 높음.
- 운영 메모: 상품 운영 판단 근거 저장.

## 4. 추천 고도화 후보

- 기존 `RECOMMENDATION_SCORE`: 원본 크롤링 리뷰 기반 점수로 유지.
- 사이트 내부 별점 평균: `BL_PRODUCT_RATINGS` 평균.
- 같은 피부 타입 사용자 별점: `SKIN_TYPE_AT_TIME` 기준 평균.
- 찜 수: `BL_PRODUCT_FAVORITES` 집계.
- 조회 수: `BL_USER_PRODUCT_EVENTS` 중 `DETAIL_VIEW` 집계.
- 댓글 수: `BL_PRODUCT_COMMENTS` 활성 댓글 집계.
- 추천 좋아요/싫어요 피드백: `BL_RECOMMENDATION_FEEDBACK` 집계.
- 관심 없음 처리: 로그인 사용자에게 해당 상품 추천 제외.
- 관리자 숨김/추천 제외 반영: 숨김 상품은 일반/추천 모두 제외, 추천 제외 상품은 추천에서 제외.
- 이미지 없는 상품 페널티: 보정 점수 계산 시 감점.
- 신고/주의 신호 반영: 부정 리뷰 비율, caution level, 신고 수를 관리자 품질 지표에 표시.
- ML 학습용 interaction 테이블: `BL_USER_PRODUCT_EVENTS`에 사용자-상품-행동-시간을 축적.

## 5. Phase 계획

### Phase 1: 사용자 반응 데이터 기반 기능

- 구현 기능: 찜/해제, 별점/자극/재구매 저장, 추천 피드백, 상품 조회 이벤트, 최근 본 상품, 마이페이지 찜/평가/최근 본 상품.
- 추가/수정 DB 테이블: `BL_PRODUCT_FAVORITES`, `BL_PRODUCT_RATINGS`, `BL_RECOMMENDATION_FEEDBACK`, `BL_USER_PRODUCT_EVENTS`.
- 추가/수정 API: `POST/DELETE /api/products/{productId}/favorite`, `GET /api/members/me/favorites`, `POST/GET /api/products/{productId}/rating`, `GET /api/members/me/ratings`, `POST /api/products/{productId}/recommendation-feedback`, `POST /api/products/{productId}/events`, `GET /api/members/me/recent-products`.
- 추가/수정 JSP: `product_detail.jsp`, `recommend.jsp`, `mypage.jsp`.
- 추가/수정 JS/CSS: 공통 카드 액션, 평가 폼, 마이페이지 활동 카드 스타일.
- 검증 방법: `test01/1234` 로그인 후 찜, 별점, 자극/재구매, 추천 피드백, 최근 본 상품 저장 및 조회. 로그아웃 상태 401/로그인 안내 확인.
- 위험 요소: 기존 `BL_FAVORITES`와 이름 충돌 가능. 요청 테이블명 `BL_PRODUCT_FAVORITES`로 새로 분리해 원본 보존.

### Phase 2: 상품 운영/관리자 기능

- 구현 기능: 상품 숨김/복구, 추천 제외/포함, 메인 노출 지정, 이미지 없는 상품 필터, 운영 메모, 품질 상태 관리.
- 추가/수정 DB 테이블: `BL_PRODUCT_ADMIN_FLAGS`.
- 추가/수정 API: `GET /api/admin/products`, `GET/PUT /api/admin/products/{productId}/flags`, hide/restore/exclude/include/feature/unfeature 단축 API.
- 추가/수정 JSP: 신규 `admin/products.jsp`, 관리자 대시보드 링크.
- 추가/수정 JS/CSS: 관리자 테이블, 필터, 메모 입력 스타일.
- 검증 방법: admin 접근 가능, USER 403, 미로그인 401, 숨김 후 `/products` 제외, 복구 후 재노출, 추천 제외 후 `/recommend` 제외.
- 위험 요소: 기존 상품 상세 직접 URL 처리. 숨김 상품 상세는 우선 API에서 404 처리하고 admin API에서는 조회 가능하게 분리.

### Phase 3: 신고/댓글 품질 관리

- 구현 기능: 댓글 신고, 사유 선택, 관리자 신고 목록, 신고 처리, 댓글 복구, 삭제 사유 표시.
- 추가/수정 DB 테이블: `BL_COMMENT_REPORTS`.
- 추가/수정 API: `POST /api/comments/{commentId}/report`, `GET /api/admin/comment-reports`, `POST /api/admin/comment-reports/{reportId}/resolve`, `POST /api/admin/comments/{commentId}/restore`.
- 추가/수정 JSP: 상품 상세 댓글 신고 버튼, 신규 `admin/comment_reports.jsp`, 기존 `admin/comments.jsp` 복구 버튼.
- 추가/수정 JS/CSS: 신고 폼/처리 버튼/상태 배지.
- 검증 방법: test01 신고, admin 신고 목록 확인/처리/댓글 복구, USER 관리자 API 403.
- 위험 요소: 동일 사용자의 중복 신고. 유니크 제약으로 중복 방지.

### Phase 4: 추천 점수 보정 및 추천 품질 표시

- 구현 기능: `serviceScore` 계산, 추천 카드 사이트 반응 표시, 상품 상세 사이트 사용자 평가 표시, 추천 제외/숨김/관심 없음 반영.
- 추가/수정 DB 테이블: 신규 없음. Phase 1/2 테이블 집계.
- 추가/수정 API: `/api/products`, `/api/products/{id}`, `/api/recommendations` 응답에 내부 통계/보정 점수 추가.
- 추가/수정 JSP: `product_detail.jsp`, `recommend.jsp`, 관리자 상품 관리.
- 추가/수정 JS/CSS: 통계 배지, 부족 데이터 안내.
- 검증 방법: 별점/찜/피드백 등록 후 집계 반영, 내부 평가 없어도 화면 정상.
- 위험 요소: 기존 추천 점수를 덮어쓰지 않음. `recommendationScore`는 유지하고 `serviceScore`를 별도 필드로 추가.

### Phase 5: 마이페이지 활동 통합

- 구현 기능: 프로필, 피부 타입, 얼굴 로그인 설정, 내가 남긴 댓글, 찜한 상품, 내가 평가한 상품, 최근 본 상품, 추천 피드백 내역, 관심 없음 상품 관리.
- 추가/수정 DB 테이블: 신규 없음. Phase 1 테이블 활용.
- 추가/수정 API: `GET /api/members/me/recommendation-feedback`, `DELETE /api/members/me/recommendation-feedback/{productId}` 또는 관심 없음 취소 API.
- 추가/수정 JSP: `mypage.jsp`.
- 추가/수정 JS/CSS: 활동 섹션, 카드형 리스트, 빠른 액션.
- 검증 방법: test01 기준 각 섹션 표시, 찜 해제/별점 수정/댓글 삭제/상품 이동 반영.
- 위험 요소: 마이페이지가 길어질 수 있으므로 섹션을 명확히 분리.

### Phase 6: 관리자 대시보드 고도화

- 구현 기능: 상품 운영 통계, 내부 별점/찜/조회/신고/품질 상태 지표, 최근 사용자 활동, 이미지 부족/추천 제외/품질 이슈 목록.
- 추가/수정 DB 테이블: 신규 없음. Phase 1/2/3/7 테이블 활용.
- 추가/수정 API: `GET /api/admin/summary` 확장, 필요 시 `GET /api/admin/activity`.
- 추가/수정 JSP: `admin/dashboard.jsp`.
- 추가/수정 JS/CSS: 통계 카드, 운영 리스트.
- 검증 방법: 각 지표 SQL 집계와 화면 수치 비교.
- 위험 요소: 빈 데이터에서 Null 처리 필요.

### Phase 7: 관리자 활동 로그

- 구현 기능: 관리자 상품/댓글/신고 작업 로그 저장, `/admin/logs` 조회.
- 추가/수정 DB 테이블: `BL_ADMIN_AUDIT_LOGS`.
- 추가/수정 API: `GET /api/admin/logs`.
- 추가/수정 JSP: 신규 `admin/logs.jsp`.
- 추가/수정 JS/CSS: 로그 필터/테이블.
- 검증 방법: 관리자 작업 후 로그 생성 및 목록 확인.
- 위험 요소: BEFORE/AFTER 값은 CLOB 또는 VARCHAR2 JSON 문자열로 단순 저장해 의존성을 낮춤.

### Phase 8: 최종 검증과 문서 정리

- 구현 기능: 전체 빌드, 필수 화면/API 검증, 계정별 시나리오 검증, 결과 문서 작성.
- 추가/수정 DB 테이블: 전체 DDL을 `docs/schema.sql`에 반영.
- 추가/수정 API: `docs/02_api_spec.md` 또는 결과 문서에 추가 API 정리.
- 추가/수정 JSP: 최종 링크 점검.
- 추가/수정 JS/CSS: 최종 정리.
- 검증 방법: `mvn clean package`, 필수 화면 200 확인, 필수 API 응답 확인, test01/admin 시나리오 실행.
- 위험 요소: 현재 실행 중인 Tomcat이 target 변경을 즉시 반영하지 않을 수 있음. 빌드 검증과 실행 검증을 분리해 기록.
