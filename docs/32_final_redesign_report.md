# BeautyLens Final Redesign Report

작성일: 2026-07-01

## 1. 전체 진행 요약

`docs/27_product_redesign_prd.md`와 `docs/29_redesign_phase_execution_checklist.md` 기준으로 남은 Phase D/E/F를 진행했다.

- Phase E: 관리자 UX 재구성
- Phase D: 얼굴 등록/로그인 UX 개선
- Phase F: 빌드, WTP 반영, HTTP/API 검증, 문서 정리

Phase A/B/C에서 만든 사용자 화면 정보구조, 상품 카드, 추천 페이지, 상품 상세 구조는 유지했다. 이번 작업에서는 큰 DB 변경, 기존 API 경로 변경, React/Vite 도입, 기존 기능 삭제는 하지 않았다.

## 2. 진행한 Phase와 순서

1. 현재 코드와 PRD/체크리스트 재확인
2. Phase E 관리자 UX 안정화
3. Phase D 얼굴 등록/로그인 UX 개선
4. Phase F 빌드와 실행 서버 검증
5. 문서 업데이트

관리자 화면은 운영 흐름과 원시값 노출 위험이 먼저 보여서 Phase E를 먼저 처리했다. 얼굴 UX는 API 구조를 유지한 채 `face-camera.js` 중심으로 개선했다.

## 3. 수정한 파일 목록

- `src/main/webapp/WEB-INF/jsp/admin/dashboard.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/products.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comments.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comment_reports.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/logs.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/mypage.jsp`
- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/assets/css/beautylens.css`
- `docs/29_redesign_phase_execution_checklist.md`
- `docs/30_redesign_phase_implementation_report.md`
- `docs/32_final_redesign_report.md`

## 4. 관리자 UX 개선 내용

- 대시보드에 운영 우선순위 카드 추가
- 신고 대기, 이미지 미확보, 추천 제외, 운영 로그를 바로 확인하도록 배치
- 플랫폼/피부 타입/주의 신호 집계값을 사람이 읽는 문구로 변환
- 상품 관리의 숨김, 복구, 추천 제외, 메인 노출, 메모 저장에 확인 절차 추가
- 댓글 삭제/복구는 confirm과 화면 내 notice로 처리
- 신고 관리에서 신고 사유와 댓글 상태를 한글 문구로 표시
- 운영 로그의 변경 전/후 값에서 `Y/N`, `ACTIVE/DELETED`, 품질 상태, JSON 값을 운영자용 문구로 변환

## 5. 얼굴 등록/로그인 UX 개선 내용

- 얼굴 등록 모달에 진행률과 현재 단계 표시 추가
- 정면, 왼쪽, 오른쪽, 위쪽, 아래쪽 촬영 흐름 유지
- 얼굴 등록 완료 전 `얼굴 정보를 등록하고 있습니다.` 상태 표시
- 얼굴 로그인은 3초 카운트다운 후 확인하는 흐름으로 정리
- 카메라 권한 거부, 카메라 없음, 카메라 사용 중 오류를 사용자 친화 문구로 변환
- 카메라 실패 시 Promise가 pending으로 남지 않도록 reject 처리 유지
- 로그인 화면에서 카메라 오류가 서버 장애처럼 보이지 않도록 분기 보강
- 마이페이지 얼굴 로그인 설정에 등록 단계 안내 추가

## 6. 전역 UX 품질 기준 반영 내용

- 사용자 화면 기술 용어 검색 수행
- 마이페이지 평가 내역의 자극/재구매 `Y/N` 노출 제거
- 관리자 화면 원시 enum 표시 가능 지점 보강
- 빈 상태 문구는 기존 Phase A/B/C의 `BL.empty()` 구조 유지
- 위험 운영 작업은 확인 절차 추가

## 7. 유지한 기존 기능

- 기존 비밀번호 로그인
- 기존 얼굴 로그인 API
- 기존 얼굴 등록 API
- 상품 목록
- 추천 페이지
- 상품 상세
- 찜
- 별점
- 자극 여부
- 재구매 의사
- 추천 피드백
- 댓글 작성/삭제/신고
- 마이페이지 활동 조회
- 관리자 상품 관리 API
- 관리자 댓글/신고 관리 API
- 관리자 운영 로그 API

## 8. 빌드 결과

`mvn clean package` 성공.

WAR 생성 경로:

- `target/beautylens-mvc-0.0.1-SNAPSHOT.war`

## 9. 실행 서버와 확인한 URL

현재 실행 서버는 Eclipse WTP 방식으로 확인했다.

- 프로세스: Eclipse JRE 기반 `javaw`
- 실행 로그 최신 경로: `D:\Lecture\eclipse-server\logs`
- 실행 배포 경로: `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc`

변경한 JSP/CSS/JS 파일은 WTP 배포 경로에 반영했다.

HTTP 200 확인:

- `/beautylens-mvc/`
- `/beautylens-mvc/login`
- `/beautylens-mvc/signup`
- `/beautylens-mvc/products`
- `/beautylens-mvc/recommend`
- `/beautylens-mvc/products/1446`
- `/beautylens-mvc/mypage`
- `/beautylens-mvc/admin`
- `/beautylens-mvc/admin/dashboard`
- `/beautylens-mvc/admin/products`
- `/beautylens-mvc/admin/comments`
- `/beautylens-mvc/admin/comment-reports`
- `/beautylens-mvc/admin/logs`
- `/beautylens-mvc/assets/css/beautylens.css`
- `/beautylens-mvc/assets/js/ui.js`
- `/beautylens-mvc/assets/js/face-camera.js`

## 10. test01 사용자 검증 결과

`test01 / 1234` 로그인 성공.

API 확인:

- `/api/members/me` 200
- `/api/members/me/favorites` 200
- `/api/members/me/ratings` 200
- `/api/members/me/recent-products` 200
- `/api/recommendations/me?size=5` 200
- `/api/products/1446/rating` 200
- `/api/products/1446/comments` 200

실제 브라우저 클릭은 수행하지 못했다.

## 11. admin 관리자 검증 결과

`admin / 1234` 로그인 성공.

API 확인:

- `/api/admin/summary` 200
- `/api/admin/products?size=5` 200
- `/api/admin/comments?size=5` 200
- `/api/admin/comment-reports?status=all&size=5` 200
- `/api/admin/logs?size=5&actionType=all&targetType=all` 200

권한 확인:

- 미로그인 `/api/admin/summary` 401
- `test01` `/api/admin/summary` 403

데이터 변경을 수반하는 관리자 작업은 실제 실행하지 않았다.

## 12. 실제 브라우저 클릭/Console 검증 여부

수행하지 못했다.

사유:

- in-app browser 연결 시 `Browser is not available: iab`
- `agent.browsers.list()` 결과 `[]`

대체 검증:

- HTTP 200 확인
- 정적 JS/CSS 반영 확인
- `node --check` 문법 검사
- 로그인/API 세션 검증

## 13. 사용자 화면 기술 용어 검색 결과

사용자 화면 JSP/JS 기준 다음 문자열 직접 노출 없음:

- `insightface-buffalo_l`
- `buffalo_l`
- `InsightFace`
- `embedding`
- `face vector`
- `FastAPI`
- `Python server`
- `modelName`
- `얼굴 특징`
- `얼굴 인증 서버`

## 14. 남은 문제

- 실제 브라우저 클릭 검증 미수행
- 개발자도구 Console 확인 미수행
- 모바일 실제 렌더링 확인 미수행
- 얼굴 카메라 권한 팝업과 실제 촬영 성공/실패 검증 미수행
- 얼굴 서버 미실행 상태에서의 화면 유지 검증 미수행
- 관리자 숨김/복구, 추천 제외/포함, 신고 처리 같은 데이터 변경 작업은 실제 실행 검증하지 않음
- Git 저장소 메타데이터가 비어 있어 `git status`, `git diff` 확인 불가

## 14.1 `/review` P2 보완 결과

작성일: 2026-07-01

추가 수정 파일:

- `src/main/java/kr/ac/kopo/recommendation/service/RecommendationServiceImpl.java`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/resources/config/sqlMap/oracle/productInteractionMapper.xml`
- `src/main/resources/config/sqlMap/oracle/commentMapper.xml`

수정 내용:

- 추천 페이지 client fallback에서 일반 상품 API 호출 제거
- 추천 부족분 보완을 서버의 recommendation-safe fallback으로 이동
- 숨김 상품을 interaction existence check에서 unavailable 처리
- 찜, 평가, 추천 피드백, 최근 본 상품 조회에서 숨김 상품 제외
- 댓글 작성 전 상품 존재 확인에도 숨김 제외 조건 적용
- 내 댓글 목록과 상품 댓글 목록에서도 숨김 상품 제외

검증:

- `mvn clean package` 성공
- `/products`, `/recommend`, `/mypage`, `/products/1446`, `/admin/products` 200
- 추천 제외 상품이 `/api/recommendations?skinType=dry&size=20`에서 제외됨 확인
- 숨김 상품 `1446`에 대한 direct event/comment/product API가 404로 막힘 확인
- 숨김 상품이 최근 본 상품에서 제외됨 확인
- 복구 후 정상 상품 `/api/products/1446`, event 기록 200 확인

검증 한계:

- 찜/평가/추천 피드백 목록은 SQL 조건과 빌드로 확인했지만, 기존 사용자 데이터 보호를 위해 실제 생성/삭제를 동반한 전체 클릭 검증은 하지 않았다.

## 14.2 댓글 수정과 평가 삭제 보완 결과

작성일: 2026-07-01

추가 수정 파일:

- `src/main/java/kr/ac/kopo/comment/controller/CommentController.java`
- `src/main/java/kr/ac/kopo/comment/service/CommentService.java`
- `src/main/java/kr/ac/kopo/comment/service/CommentServiceImpl.java`
- `src/main/java/kr/ac/kopo/comment/dao/CommentDAO.java`
- `src/main/java/kr/ac/kopo/comment/dao/CommentDAOImpl.java`
- `src/main/java/kr/ac/kopo/interaction/controller/ProductInteractionController.java`
- `src/main/java/kr/ac/kopo/interaction/service/ProductInteractionService.java`
- `src/main/java/kr/ac/kopo/interaction/service/ProductInteractionServiceImpl.java`
- `src/main/java/kr/ac/kopo/interaction/dao/ProductInteractionDAO.java`
- `src/main/java/kr/ac/kopo/interaction/dao/ProductInteractionDAOImpl.java`
- `src/main/resources/config/sqlMap/oracle/commentMapper.xml`
- `src/main/resources/config/sqlMap/oracle/productInteractionMapper.xml`
- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/mypage.jsp`
- `src/main/webapp/assets/css/beautylens.css`

추가된 기능:

- 댓글 수정 API: `PUT /api/comments/{commentId}`
- 평가 삭제 API: `DELETE /api/products/{productId}/rating`
- 상품 상세 댓글 인라인 수정
- 상품 상세 내 평가 삭제
- 마이페이지 내 의견 인라인 수정
- 마이페이지 내 평가 삭제

검증:

- `mvn clean package` 성공
- 댓글 작성 → 수정 → 수정 내용 조회 → 삭제 정리 성공
- 평가 저장 → 삭제 → 조회 결과 null 확인 성공
- 주요 화면 `/products`, `/recommend`, `/products/1446`, `/mypage`, `/admin/products` 200

추가로 확인한 남은 한계:

- 마이페이지 계정 설정은 아직 실제 비밀번호 변경/회원 탈퇴 기능이 없다.
- 사용자용 댓글 신고 사유 선택 모달은 아직 없다.
- 사이트 평가 요약의 자극/재구매 비율은 집계 API가 부족해 상세 수치로 표시하지 않는다.
- 추천 피드백 사유 수집은 아직 없다.

## 15. 시연할 때 강조할 포인트

- BeautyLens가 단순 기능 나열에서 추천 서비스 정보구조로 정리됨
- 상품 카드와 상세는 외부 리뷰 분석, 사이트 사용자 평가, 회원 의견을 분리해 보여줌
- 마이페이지는 계정 대시보드 구조로 정리됨
- 관리자 화면은 운영 콘솔처럼 신고, 이미지, 추천 제외, 로그를 우선 확인하게 됨
- 얼굴 등록/로그인은 기술 용어 없이 단계와 진행률 중심으로 정리됨
- 기존 DB/API 구조와 기능을 유지하면서 UX 품질을 개선함

## 16. 사람이 직접 확인해야 할 항목

- 브라우저에서 상품 카드 찜 클릭
- 추천 숨기기 클릭 후 새로고침 재노출 여부
- 상품 상세 별점, 자극 여부, 재구매 의사 저장
- 댓글 작성, 삭제, 신고 클릭
- 마이페이지 탭 전환과 빈 상태 문구
- 얼굴 등록 카메라 권한 허용/거부 흐름
- 얼굴 로그인 실제 촬영 흐름
- 관리자 상품 숨김/복구와 추천 제외/포함
- 관리자 신고 처리와 댓글 복구
- 개발자도구 Console 오류
- 모바일 레이아웃

## 17. 얼굴 로그인 상태 피드백 보강

작성일: 2026-07-01

추가 수정 파일:

- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/java/kr/ac/kopo/face/controller/FaceController.java`
- `src/main/java/kr/ac/kopo/face/service/FaceCredentialService.java`
- `src/main/java/kr/ac/kopo/face/service/FaceCredentialServiceImpl.java`

개선 내용:

- 얼굴 로그인은 정면 얼굴을 기본으로 자동 확인한다.
- 촬영 버튼 없이 카메라 모달에서 원형 스캔 링, 진행률, 상태 문구를 표시한다.
- `얼굴 확인 중`, `로그인 정보 확인 중`, `얼굴 확인이 완료되었습니다. 로그인 중입니다` 상태를 구분한다.
- 실패 시 비밀번호 로그인으로 돌아갈 수 있는 사용자 문구를 제공한다.
- 비슷한 얼굴 후보가 있거나 정면 판정 여유가 작으면 왼쪽/오른쪽 얼굴 추가 확인을 요청한다.
- 추가 촬영된 이미지는 실제 서버 판정 평균에 반영된다.

검증:

- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- `mvn clean package` 성공
- `/beautylens-mvc/login`, `/beautylens-mvc/assets/js/face-camera.js`, `/beautylens-mvc/assets/css/beautylens.css` HTTP 200

검증 한계:

- in-app browser가 제공되지 않아 실제 카메라 권한 팝업과 자동 촬영 클릭 흐름은 직접 검증하지 못했다.
- 실행 중인 Tomcat이 class reload를 하지 않으면 Eclipse Server 재시작 또는 Clean/Publish가 필요하다.

## 18. 얼굴 로그인 보수적 판정과 취소 처리 보강

작성일: 2026-07-01

추가 수정 파일:

- `src/main/java/kr/ac/kopo/face/service/FaceCredentialServiceImpl.java`
- `src/main/java/kr/ac/kopo/face/controller/FaceController.java`
- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`

개선 내용:

- 얼굴만으로 계정을 특정하는 흐름은 추가 각도 촬영 후에도 best/second 점수 차이가 작으면 로그인 실패로 처리한다.
- 첫 정면 1장에서는 추가 촬영을 요청하지만, 3장 이후에도 애매하면 임의 계정으로 세션을 만들지 않는다.
- 이 경우 사용자는 아이디를 입력하고 다시 얼굴 로그인하거나 비밀번호로 로그인하도록 안내받는다.
- 서버 확인 중 사용자가 얼굴 로그인 모달을 닫으면 진행 중인 로그인 요청을 abort한다.
- 취소 이후 성공 응답이 도착해도 redirect가 실행되지 않도록 `isCanceled()` 가드를 추가했다.

검증:

- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- `mvn clean package` 성공
- `/beautylens-mvc/login`, `/beautylens-mvc/assets/js/face-camera.js`, `/beautylens-mvc/api/health` HTTP 200
- `test01 / 1234` 비밀번호 로그인 API 성공

검증 한계:

- 실제 ambiguous 3장 샘플과 브라우저 카메라 클릭 검증은 수행하지 못했다.
- in-app browser가 제공되지 않아 개발자도구 Console 확인은 수행하지 못했다.

## 19. 얼굴 로그인 스트림 정리와 동시 실행 방지

작성일: 2026-07-01

추가 수정 파일:

- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`

개선 내용:

- 카메라 시작 중 모달을 닫은 뒤 stream이 늦게 열리는 경우에도 즉시 `stopStream()`으로 정리한다.
- 얼굴 등록/추가 촬영 흐름과 얼굴 로그인 자동 캡처 흐름 모두에 취소 후 stream 정리 guard를 적용했다.
- 얼굴 로그인 버튼 더블클릭 또는 진행 중 재클릭을 `activeFaceLogin`과 disabled 상태로 막았다.
- 성공 redirect 전에는 버튼을 계속 비활성화해 중복 로그인 흐름이 새로 시작되지 않게 했다.
- 실패, 취소, 오류에서는 `finally`에서 버튼 상태를 복구한다.

검증:

- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- `mvn clean package` 성공
- `/beautylens-mvc/login`, `/beautylens-mvc/assets/js/face-camera.js`, `/beautylens-mvc/api/health` HTTP 200
- 배포된 JSP/JS 응답에 새 guard 코드 포함 확인

검증 한계:

- 실제 브라우저 카메라 권한 승인 중 닫기와 더블클릭 방지는 in-app browser 미제공으로 클릭 검증하지 못했다.
