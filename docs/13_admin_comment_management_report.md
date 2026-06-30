# 13. 회원 댓글 + 관리자 기능 보고서 (11차)

작성일: 2026-06-30

---

## 관리자 기능 범위

| 기능 | 구현 여부 | 비고 |
|---|---|---|
| 관리자 로그인 (ROLE=ADMIN) | ✅ | admin/1234 |
| 대시보드 통계 조회 | ✅ | GET /api/admin/summary |
| 댓글 전체 목록 조회 | ✅ | GET /api/admin/comments |
| 댓글 soft delete (관리자 경로) | ✅ | DELETE /api/admin/comments/{id} |
| 댓글 soft delete (공통 경로) | ✅ | DELETE /api/comments/{id} — 작성자 본인 또는 ADMIN |
| 상품 삭제 | ❌ 구현 안 함 (요구사항 제외) | |
| 리뷰 삭제 | ❌ 구현 안 함 (요구사항 제외) | |
| import 재실행 버튼 | ❌ 구현 안 함 (요구사항 제외) | |

---

## ROLE 컬럼 추가

| 항목 | 결과 |
|---|---|
| BL_MEMBERS ROLE 컬럼 추가 | ✅ VARCHAR2(20) DEFAULT 'USER' |
| 기존 회원 ROLE 설정 | ✅ test01 → USER |
| admin 계정 생성 | ✅ loginId=admin, ROLE=ADMIN |
| 비밀번호 방식 | SHA-256('1234') = 03ac674... |
| MemberVO.role 필드 추가 | ✅ getter/setter 포함 |
| memberMapper.xml ROLE 반영 | ✅ 전체 SELECT 쿼리에 추가 |
| 로그인 시 세션에 role 포함 | ✅ loginMember.getRole() 가능 |

---

## 댓글 테이블 설계

**테이블**: BL_PRODUCT_COMMENTS  
**시퀀스**: SEQ_BL_PRODUCT_COMMENTS

| 컬럼 | 타입 | 설명 |
|---|---|---|
| COMMENT_ID | NUMBER PK | 시퀀스 자동 생성 |
| PRODUCT_ID | NUMBER NOT NULL | FK → BL_PRODUCTS |
| MEMBER_ID | NUMBER NOT NULL | FK → BL_MEMBERS |
| CONTENT | VARCHAR2(1000) NOT NULL | 댓글 내용 |
| STATUS | VARCHAR2(20) DEFAULT 'ACTIVE' | ACTIVE / DELETED |
| CREATED_AT | DATE DEFAULT SYSDATE | 작성일시 |
| UPDATED_AT | DATE | 수정일시 (현재 미사용) |
| DELETED_AT | DATE | 삭제일시 |
| DELETED_BY | NUMBER | 삭제 관리자 memberId |
| DELETE_REASON | VARCHAR2(300) | 삭제 사유 |

**FK 제약**: FK_COMMENT_PRODUCT (BL_PRODUCTS), FK_COMMENT_MEMBER (BL_MEMBERS)

---

## Soft Delete 선택 이유

- 삭제된 댓글도 관리자 감사 기록 목적으로 보존
- 삭제 시각(DELETED_AT)과 삭제자(DELETED_BY)를 추적 가능
- 실수로 삭제 시 STATUS만 되돌리면 복구 가능 (향후)
- 일반 사용자 조회(STATUS='ACTIVE')와 관리자 전체 조회 분리

---

## 구현 파일 목록

| 분류 | 파일 |
|---|---|
| VO | `kr.ac.kopo.comment.vo.CommentVO` |
| VO | `kr.ac.kopo.admin.vo.AdminSummaryVO` |
| DAO | `kr.ac.kopo.comment.dao.CommentDAO` / `CommentDAOImpl` |
| DAO | `kr.ac.kopo.admin.dao.AdminDAO` / `AdminDAOImpl` |
| Service | `kr.ac.kopo.comment.service.CommentService` / `CommentServiceImpl` |
| Service | `kr.ac.kopo.admin.service.AdminService` / `AdminServiceImpl` |
| Controller (REST) | `kr.ac.kopo.comment.controller.CommentController` |
| Controller (REST) | `kr.ac.kopo.admin.controller.AdminApiController` |
| Controller (JSP) | `kr.ac.kopo.admin.controller.AdminViewController` |
| Mapper | `config/sqlMap/oracle/commentMapper.xml` |
| Mapper | `config/sqlMap/oracle/adminMapper.xml` |
| JSP | `WEB-INF/jsp/admin/dashboard.jsp` |
| JSP | `WEB-INF/jsp/comment/test.jsp` |
| JSP | `WEB-INF/jsp/error/forbidden.jsp` |
| 수정 | `MemberVO.java` (role 필드) |
| 수정 | `memberMapper.xml` (ROLE AS role 추가) |
| 수정 | `sqlMapConfig.xml` (commentVO, adminSummaryVO alias) |
| 수정 | `docs/schema.sql` |
| 수정 | `docs/02_api_spec.md` |

---

## 댓글 삭제 권한 정책

| 주체 | 가능 여부 | DELETE_REASON |
|---|---|---|
| 작성자 본인 | 자기 댓글 soft delete 가능 | USER_DELETE |
| ADMIN | 모든 댓글 soft delete 가능 | ADMIN_DELETE |
| 일반 USER (타인 댓글) | 삭제 불가 → 403 | — |
| 미로그인 | 삭제 불가 → 401 | — |

---

## API 목록

| 메서드 | 경로 | 설명 | 인증 |
|---|---|---|---|
| GET | /api/products/{id}/comments | 댓글 목록 (ACTIVE) | 없음 |
| POST | /api/products/{id}/comments | 댓글 작성 | 로그인 |
| DELETE | /api/comments/{id} | 댓글 soft delete. 작성자 본인 또는 ADMIN 가능 | 작성자 본인 또는 ADMIN |
| GET | /api/admin/summary | 대시보드 통계 | ADMIN |
| GET | /api/admin/comments | 전체 댓글 목록 | ADMIN |
| DELETE | /api/admin/comments/{id} | 댓글 soft delete (관리자 경로) | ADMIN |
| GET | /admin | 관리자 대시보드 JSP | ADMIN |
| GET | /comment-test | 댓글 테스트 페이지 JSP | 없음 |

---

## 권한 검증 결과

### 기존 검증 (11차 초기)

| 시나리오 | 기대값 | 결과 |
|---|---|---|
| 미로그인 GET /api/admin/summary | 401 | ✅ |
| test01(USER) GET /api/admin/summary | 403 | ✅ |
| admin GET /api/admin/summary | 200 | ✅ |
| 미로그인 POST /api/products/{id}/comments | 401 | ✅ |
| test01 POST /api/products/{id}/comments | 201 | ✅ |
| test01 DELETE /api/admin/comments/{id} | 403 | ✅ |
| admin DELETE /api/admin/comments/{id} | 200 | ✅ |
| 빈 content POST | 400 | ✅ |
| 없는 productId POST | 404 | ✅ |

### 추가 검증 (댓글 삭제 권한 변경 후)

| 시나리오 | 기대값 | 비고 |
|---|---|---|
| test01 댓글 작성 → 201 | 201 | 작성 성공 |
| test01 본인 댓글 DELETE /api/comments/{id} | 200, DELETE_REASON=USER_DELETE | 본인 삭제 |
| 삭제된 댓글이 GET /api/products/{id}/comments 에서 제외 | STATUS=ACTIVE만 반환 | 일반 목록 |
| GET /api/admin/comments 에서 DELETED 상태 확인 | deleteReason=USER_DELETE 포함 | 관리자 목록 |
| test01이 다른 회원 댓글 DELETE 시도 | 403 | 권한 없음 |
| admin이 test01 댓글 DELETE /api/comments/{id} | 200, DELETE_REASON=ADMIN_DELETE | 관리자 삭제 |
| 미로그인이 DELETE /api/comments/{id} | 401 | 미인증 |

---

## JSP 화면 확인 결과

| 화면 | URL | 확인 내용 | 결과 |
|---|---|---|---|
| 관리자 대시보드 | /admin | admin 로그인 시 200, 통계 표시 | ✅ |
| 관리자 대시보드 | /admin | test01 로그인 시 접근 차단 | ✅ |
| 댓글 테스트 | /comment-test | 로드 성공, 댓글 작성/조회 JS 포함 | ✅ |

관리자 대시보드 기능:
- 상품수/리뷰수/회원수/댓글수/이미지확보수 카드 표시
- 플랫폼별 상품 분포 + 리뷰 감성 분포
- 최근 30건 댓글 목록 (ACTIVE/DELETED 표시)
- 삭제 버튼 → AJAX DELETE → 화면 즉시 반영

---

## 기존 기능 유지 확인

| 항목 | 결과 |
|---|---|
| Product API (displayName, imageUrl) | ✅ |
| Recommendation API | ✅ |
| BL_REVIEWS 수정 없음 | ✅ |
| BL_PRODUCTS 수정 없음 | ✅ |
| 추천 로직 변경 없음 | ✅ |
| displayName 로직 변경 없음 | ✅ |

---

## 11차 보완 검증 결과

검증 일시: 2026-06-30  
검증 방식: Tomcat 재배포 후 실제 HTTP 요청 + JDBC SELECT

| 시나리오 | HTTP | DB 확인 |
|---|---|---|
| test01 본인 댓글 삭제 | 200 | STATUS=DELETED, DELETED_BY=1, DELETE_REASON=USER_DELETE |
| test02가 test01 댓글 삭제 시도 | 403 | STATUS=ACTIVE 유지 |
| admin이 test01 댓글 삭제 | 200 | STATUS=DELETED, DELETED_BY=2, DELETE_REASON=ADMIN_DELETE |
| 미로그인 댓글 삭제 시도 | 401 | 변경 없음 |
| 일반 댓글 목록 | 200 | DELETED 댓글 제외 |
| 관리자 댓글 목록 | 200 | DELETED 댓글 표시 |

기존 기능 유지 확인:
- GET /api/health: 200, db=ok
- GET /api/products?sortBy=score&size=20: 200, displayName/imageUrl/productUrl/imageStatus 유지
- GET /api/recommendations/me?size=20: 200, reason/platform/score 유지
- GET /api/admin/summary: ADMIN 200, USER 403, 미로그인 401
- GET /api/admin/comments: ADMIN 200

---

## 남은 한계

| 항목 | 설명 |
|---|---|
| 댓글 수정 기능 없음 | POST와 DELETE만 구현, PUT 없음 |
| 페이징 없음 | 관리자 댓글 목록은 size 파라미터로만 제한 |
| admin 계정 skinType 없음 | 추천 API 호출 시 400 |
| 이미지 전체 미수집 | 1,305건 IMAGE_STATUS=NULL (별도 단계 예정) |
| 회원가입 시 ROLE 선택 불가 | 기본 USER, admin은 DB 직접 설정 |
