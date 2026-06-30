# BeautyLens Feature Expansion Result Report

작성일: 2026-06-30

## 1. 구현한 Phase 목록

- Phase 1: 사용자 반응 데이터 기능
- Phase 2: 상품 운영/관리자 기능
- Phase 3: 신고/댓글 품질 관리
- Phase 4: 추천 점수 보정 및 추천 품질 표시
- Phase 5: 마이페이지 활동 통합
- Phase 6: 관리자 대시보드 고도화
- Phase 7: 관리자 활동 로그
- Phase 8: 빌드, DB, 문서 검증

## 2. 추가된 사용자 기능

- 상품 찜/찜 해제
- 마이페이지 찜한 상품 조회
- 상품 상세 사이트 내부 별점 등록/수정
- 자극 여부, 재구매 의사 저장
- 추천 카드 좋아요/별로예요/관심 없음 피드백
- 상품 상세 조회 이벤트 저장
- 마이페이지 최근 본 상품 조회
- 마이페이지 내가 평가한 상품 조회
- 마이페이지 추천 피드백 내역 조회 및 취소
- 상품 상세 댓글 신고

## 3. 추가된 관리자 기능

- `/admin/products` 상품 운영 관리
- 상품 숨김/복구
- 추천 제외/포함
- 메인 노출 지정/해제
- 이미지 없는 상품 필터
- 품질 상태 관리
- 운영 메모 저장
- 상품별 내부 별점/찜/조회/댓글 지표 표시
- `/admin/comment-reports` 신고 댓글 관리
- 신고 처리/반려
- 삭제 댓글 복구
- `/admin/logs` 관리자 활동 로그 조회
- 대시보드 운영 지표 확장

## 4. 추가된 DB 테이블

- `BL_PRODUCT_FAVORITES`
- `BL_PRODUCT_RATINGS`
- `BL_RECOMMENDATION_FEEDBACK`
- `BL_USER_PRODUCT_EVENTS`
- `BL_PRODUCT_ADMIN_FLAGS`
- `BL_COMMENT_REPORTS`
- `BL_ADMIN_AUDIT_LOGS`

원본 `BL_PRODUCTS`, `BL_REVIEWS`, `PRODUCT_NAME`, 기존 크롤링 리뷰 데이터는 삭제하거나 직접 수정하지 않았다.

## 5. 추가/수정 API

- `POST /api/products/{productId}/favorite`
- `DELETE /api/products/{productId}/favorite`
- `GET /api/members/me/favorites`
- `POST /api/products/{productId}/rating`
- `GET /api/products/{productId}/rating`
- `GET /api/members/me/ratings`
- `POST /api/products/{productId}/recommendation-feedback`
- `GET /api/members/me/recommendation-feedback`
- `DELETE /api/members/me/recommendation-feedback/{productId}`
- `POST /api/products/{productId}/events`
- `GET /api/members/me/recent-products`
- `GET /api/admin/products`
- `GET /api/admin/products/{productId}/flags`
- `PUT /api/admin/products/{productId}/flags`
- `POST /api/admin/products/{productId}/hide`
- `POST /api/admin/products/{productId}/restore`
- `POST /api/admin/products/{productId}/exclude-recommendation`
- `POST /api/admin/products/{productId}/include-recommendation`
- `POST /api/admin/products/{productId}/feature`
- `POST /api/admin/products/{productId}/unfeature`
- `POST /api/comments/{commentId}/report`
- `GET /api/admin/comment-reports`
- `POST /api/admin/comment-reports/{reportId}/resolve`
- `POST /api/admin/comments/{commentId}/restore`
- `GET /api/admin/logs`

## 6. 추가/수정 JSP

- 추가: `admin/products.jsp`
- 추가: `admin/comment_reports.jsp`
- 추가: `admin/logs.jsp`
- 수정: `demo/products.jsp`
- 수정: `demo/recommend.jsp`
- 수정: `demo/product_detail.jsp`
- 수정: `demo/mypage.jsp`
- 수정: `admin/dashboard.jsp`
- 수정: `admin/comments.jsp`

## 7. 추천 고도화 구조

기존 `recommendationScore`는 유지했다. 신규 `serviceScore`는 별도 필드로 계산한다.

```text
serviceScore =
recommendation_score * 0.70
+ site_rating_score * 0.15
+ same_skin_rating_score * 0.10
+ engagement_score * 0.05
- image_missing_penalty
- dislike_penalty
```

반영 신호:

- 사이트 내부 별점 평균
- 같은 피부 타입 별점 평균
- 찜 수
- 조회 수
- 댓글 수
- 추천 좋아요/싫어요
- 관심 없음 추천 제외
- 관리자 숨김/추천 제외
- 이미지 없는 상품 페널티

## 8. 검증 결과

- `mvn clean package`: 성공
- Oracle DDL 적용: 성공
- DB 테이블 존재 검증: 성공
- 기존 `/api/health`: HTTP 200, DB ok
- Python 얼굴 서버 `/health`: HTTP 200
- 기존 화면 `/`, `/products`, `/recommend`, `/mypage`, `/admin`, `/admin/comments`: 기존 실행 서버 기준 접근 가능
- 신규 화면/API 런타임 검증: 현재 8088 Tomcat이 새 WAR를 아직 반영하지 않아 `/admin/products`, `/admin/logs`는 404. `target/beautylens-mvc-0.0.1-SNAPSHOT.war`는 최신 빌드 완료.

## 9. 실패하거나 제외한 기능

- 상품 비교 UI는 이번 구현에서 제외했다.
- 댓글 도움돼요 기능은 이번 구현에서 제외했다.
- 미로그인 최근 본 상품 localStorage 저장은 제외하고 로그인 회원 기준 이벤트로 구현했다.
- 실행 중 Tomcat 자동 재배포는 확인되지 않았다. 새 WAR 배포 또는 서버 재시작 후 신규 화면/API 확인이 필요하다.

## 10. 남은 한계

- 평문 비밀번호 구조는 기존 요구대로 유지했지만 실제 서비스 수준 보안에는 부족하다.
- 추천 보정 점수는 규칙 기반이며 ML 학습은 아직 하지 않는다.
- 관리자 활동 로그의 before/after 값은 간단한 문자열 중심이다.
- JSP 내부 스크립트가 길어져 장기 유지보수 시 JS 파일 분리가 필요하다.

## 11. 시연 때 강조할 포인트

- 크롤링 리뷰 점수와 사이트 내부 평가가 분리되어 보인다.
- 사용자가 찜/별점/추천 피드백/최근 조회를 남기면 추천 개선 근거가 쌓인다.
- 관리자는 상품을 삭제하지 않고 숨김/추천 제외/품질 상태/메모로 운영한다.
- 신고 댓글과 삭제 댓글 복구가 있어 운영 흐름이 실제 서비스에 가까워졌다.
- 관리자 활동 로그로 누가 어떤 상품/댓글을 조작했는지 추적할 수 있다.

## 12. 지금 바로 시연 가능한지 최종 판정

2026-06-30 런타임 재검증에서 Eclipse WTP 배포 경로 `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc`에 최신 빌드 결과를 반영했고, 실제 Tomcat HTTP 기준으로 신규 화면/API/E2E 검증을 통과했다. `ORA-17004`는 `jdbcType` 명시로 해결했다.

최종 상세 결과는 `docs/26_runtime_bugfix_and_e2e_report.md`에 기록했다.
