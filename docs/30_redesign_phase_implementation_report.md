# BeautyLens Redesign Phase Implementation Report

작성일: 2026-06-30

기준 문서:

- `docs/27_product_redesign_prd.md`
- `docs/29_redesign_phase_execution_checklist.md`

## 1. Phase B 구현 결과

### 수정 파일

- `src/main/webapp/WEB-INF/jsp/demo/products.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/webapp/assets/js/ui.js`
- `src/main/webapp/assets/css/beautylens.css`

### 변경 내용

- 공통 상품 카드 `BL.productCard()`를 상세 보기와 찜하기 중심 구조로 정리했다.
- 추천 카드의 즉시 피드백 버튼을 카드 메인 행동에서 제거하고 `이 추천이 맞지 않나요?` 보조 영역으로 이동했다.
- 기존 추천 피드백 API는 유지했다.
- `NOT_INTERESTED` 피드백은 `추천 숨기기` 문구로 바꾸고, 저장 후 해당 카드를 화면에서 숨기도록 했다.
- `/products`에는 비교 목적 안내와 상품 목록 헤더를 추가했다.
- `/recommend`에는 추천 기준 설명과 평가를 상세에서 남기도록 유도하는 안내를 추가했다.

### UX/UI 판단 근거

- 상품 카드는 빠른 비교 단위이므로 행동을 `자세히 보기`, `찜하기`로 제한했다.
- 좋아요/별로예요/관심 없음은 상품을 충분히 보기 전 수집하면 데이터 품질이 낮아질 수 있어 보조 UI로 이동했다.
- 내부 사용자 반응은 카드에서 과하게 설명하지 않고 보조 텍스트로만 표시했다.

### 검증 URL

- `http://localhost:8088/beautylens-mvc/products` 200
- `http://localhost:8088/beautylens-mvc/recommend` 200
- `http://localhost:8088/beautylens-mvc/mypage` 200
- `http://localhost:8088/beautylens-mvc/assets/css/beautylens.css` 200
- `http://localhost:8088/beautylens-mvc/assets/js/ui.js` 200

### 검증한 기능

- 상품 목록 표시 API 확인
- 상품 카드 찜 API 확인
- 추천 피드백 API 확인
- 마이페이지 최근 본 상품 API 확인
- `ui.js` 문법 검사 확인

### 발견 문제

- 검증 스크립트에서 PowerShell 예약 변수 `$PID`를 사용해 잘못된 상품 ID로 API를 호출했다.

### 수정한 문제

- 검증 스크립트 변수명을 `$productIdValue`로 바꿔 재검증했다.
- 재검증 결과 상품 ID `1446` 기준 찜과 추천 피드백 API가 정상 동작했다.

### Phase B 완료 여부

- 완료.

## 2. Phase C 구현 결과

### 수정 파일

- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
- `src/main/webapp/assets/css/beautylens.css`

### 변경 내용

- 상품 상세를 다음 섹션으로 재구성했다.
  - 상품 요약
  - 추천 근거
  - 외부 리뷰 분석
  - 사이트 사용자 평가
  - 내 평가 남기기
  - 회원 의견
- 상품 요약에는 이미지, 브랜드, 플랫폼, 상품명, 추천 점수, 서비스 반응, 외부 상품 페이지, 찜하기를 배치했다.
- 추천 근거에는 피부 타입 기준, 긍정 신호, 주의 신호, 내부 반응 부족 여부를 분리했다.
- 외부 리뷰 분석은 긍정 포인트와 주의 포인트로 나누었다.
- 사이트 사용자 평가는 입력 영역이 아니라 내부 평균 별점과 평가 수 중심의 요약 영역으로 분리했다.
- 별점, 자극 여부, 재구매 의사는 `내 평가 남기기` 영역에 유지했다.
- 회원 댓글은 `회원 의견`으로 분리했다.
- 댓글 신고 시 사용자에게 내부 신고 코드 입력을 요구하지 않도록 바꾸었다.

### UX/UI 판단 근거

- 상품 상세는 구매 판단에 필요한 정보를 순서대로 보여주는 화면이어야 하므로 요약, 근거, 외부 리뷰, 내부 평가, 내 평가, 댓글 순서로 정리했다.
- 사이트 사용자 평가는 정량 요약이고 회원 의견은 자유 댓글이므로 시각적으로 분리했다.
- 평가 입력은 리뷰 분석과 내부 평가 요약을 본 뒤에 나오도록 배치했다.

### 검증 URL

- `http://localhost:8088/beautylens-mvc/products/1446` 200

### 검증한 기능

- 상품 상세 API 확인
- 최근 본 상품 이벤트 기록 API 확인
- 상품 상세 찜 API 확인
- 별점 저장 API 확인
- 자극 여부 저장 API 확인
- 재구매 의사 저장 API 확인
- 댓글 작성 API 확인
- 댓글 신고 API 확인
- 댓글 삭제 API 확인

### 발견 문제

- 기존 댓글 신고 UX가 사용자에게 `SPAM`, `FALSE_INFO`, `ETC` 같은 내부 사유 코드를 입력하게 했다.

### 수정한 문제

- 댓글 신고는 확인 메시지만 보여주고, API에는 기존 경로와 필드를 유지한 채 기본 사유로 저장하도록 수정했다.

### Phase C 완료 여부

- 완료.

## 3. 전체 결과

### 전체 수정 파일 목록

- `docs/29_redesign_phase_execution_checklist.md`
- `docs/30_redesign_phase_implementation_report.md`
- `src/main/webapp/WEB-INF/jsp/demo/products.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
- `src/main/webapp/assets/js/ui.js`
- `src/main/webapp/assets/css/beautylens.css`

### 빌드 결과

- `mvn clean package` 성공
- WTP 배포 경로 `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc`에 빌드 결과를 반영했다.

### 사용자 화면 기술 용어 검색 결과

다음 문구 검색 결과 사용자 화면 JSP/JS 기준 직접 노출 없음:

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

### 기존 기능 회귀 검증 결과

test01 계정 기준 API 레벨로 확인:

- 상품 목록 표시 정상
- 추천 페이지 표시 정상
- 마이페이지 표시 정상
- 상품 상세 표시 정상
- 찜 정상
- 추천 피드백 정상
- 최근 본 상품 기록 정상
- 별점 저장 정상
- 자극 여부 저장 정상
- 재구매 의사 저장 정상
- 댓글 작성 정상
- 댓글 신고 정상
- 댓글 삭제 정상

### 남은 문제

- 실제 브라우저 개발자도구에서 클릭/콘솔 검증은 수행하지 못했다.
- HTTP 확인, 정적 JS 문법 검사, API 호출 검증으로 대체했다.
- 사이트 사용자 평가의 자극 여부/재구매 의사 비율은 현재 API 응답에 집계값이 없어 상세한 비율 대신 데이터 부족/수집 중 문구로 표시했다.

### Phase D 또는 Phase E로 넘어가기 전 주의사항

- Phase D에서는 얼굴 등록 wizard와 카메라 UX를 다룬다. 이번 Phase에서 얼굴 관련 JS는 수정하지 않았다.
- Phase E에서는 관리자 UX를 다룬다. 이번 Phase에서 관리자 JSP/API는 수정하지 않았다.
- Phase B에서 공통 카드 `BL.productCard()`를 수정했으므로 이후 화면 검증 시 `/products`, `/recommend`, `/mypage` 최근 본 상품을 함께 확인해야 한다.

## 4. 전역 UX 품질 기준 보강 결과

### 반영 문서

- `docs/27_product_redesign_prd.md`
- `docs/29_redesign_phase_execution_checklist.md`

### 추가한 전역 기준

- 사용자 화면에 개발자식 값이 노출되지 않아야 한다.
- 데이터가 없을 때 자연스러운 빈 상태 문구를 보여야 한다.
- 보조 API 실패가 전체 화면 실패로 이어지면 안 된다.
- 로그인 필요 행동은 로그인 후 원래 화면으로 복귀해야 한다.
- redirect는 내부 경로만 허용해야 한다.
- 정량 평가와 회원 의견은 목적과 UI가 구분되어야 한다.
- 관리자 화면도 원시 상태값이 아니라 운영자가 이해할 수 있는 문구를 사용해야 한다.

### 수정한 코드

- `src/main/webapp/assets/js/ui.js`
  - `BL.currentInternalPath()`, `BL.safeRedirectPath()`, `BL.loginPath()` 추가
  - `BL.requireLoginView()`가 현재 페이지 redirect를 포함한 로그인 링크를 만들도록 수정
  - 찜/추천 피드백이 401을 받으면 현재 페이지로 돌아오는 로그인 URL로 이동하도록 수정
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
  - `redirect` 파라미터를 읽고 안전한 내부 경로일 때 로그인 후 해당 경로로 이동하도록 수정
  - 잘못된 redirect 값은 기존 기본 경로로 이동
- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
  - 긍정 리뷰/주의 리뷰 API를 섹션별로 독립 처리
  - 빈 배열 또는 비배열 응답은 자연스러운 빈 상태 문구로 표시
  - 리뷰 API 하나가 실패해도 전체 상세 화면이 죽지 않도록 수정
  - 평가 메모는 compact한 선택 입력으로 축소
  - 회원 의견은 댓글 목록 중심으로 배치하고 작성 버튼을 `의견 작성`으로 변경
- `src/main/webapp/assets/css/beautylens.css`
  - compact textarea 스타일 보강
  - 댓글 작성 폼 여백을 목록 이후 배치에 맞게 조정

### 전체 화면 재점검 결과

- `/` 200
- `/login` 200
- `/signup` 200
- `/products` 200
- `/recommend` 200
- `/products/1446` 200
- `/mypage` 200
- `/admin` 200
- `/admin/dashboard` 200
- `/admin/products` 200
- `/admin/comments` 200
- `/admin/comment-reports` 200
- `/admin/logs` 200

### 로그인 redirect 확인 결과

- `/login?redirect=%2Fproducts%2F1446` HTTP 200 확인
- 로그인 페이지에 `safeRedirectPath` 기반 redirect 처리 코드 포함 확인
- 실제 브라우저에서 로그인 폼 제출 후 복귀하는 클릭 검증은 수행하지 못했다.

### 기능 회귀 검증 결과

test01 계정 기준 API 레벨로 확인:

- 상품 상세 조회 정상
- 최근 본 상품 이벤트 기록 정상
- 찜 정상
- 별점 저장 정상
- 자극 여부 저장 정상
- 재구매 의사 저장 정상
- 댓글 작성 정상
- 댓글 신고 정상
- 댓글 삭제 정상

### 빈 상태/API 실패 보강 결과

- 긍정 리뷰가 없으면 `아직 긍정 리뷰가 충분하지 않습니다.`
- 주의 리뷰가 없으면 `아직 주의 리뷰가 충분하지 않습니다.`
- 리뷰 API 실패 시 해당 리뷰 섹션만 `리뷰를 불러오지 못했습니다. 잠시 후 다시 확인해 주세요.`
- 회원 의견이 없으면 `아직 회원 댓글이 없습니다.`
- 사이트 사용자 평가가 부족하면 부족 상태 문구 표시

### 실제 브라우저 검증 결과

- 현재 Codex 세션에서 in-app browser를 사용할 수 없어 실제 브라우저 클릭 검증은 수행하지 못했다.
- 브라우저 목록 조회 결과 사용 가능한 브라우저가 없었다.
- 따라서 실제 클릭 검증과 개발자도구 Console 확인은 남은 문제로 기록한다.

### 남은 문제

- 실제 브라우저 클릭 검증 미수행
- 개발자도구 Console 확인 미수행
- 모바일 실제 렌더링 검수 미수행
- 로그인 redirect는 코드/HTTP/정적 확인까지 완료했지만 실제 로그인 폼 제출 후 복귀 클릭 검증은 추가 필요
- 사이트 사용자 평가의 자극/재구매 집계값은 API가 없어 여전히 `평가 수집 중`으로 표시

## 5. Phase D/E/F 최종 구현 결과

작성일: 2026-07-01

### Phase E 관리자 UX 재구성

수정 파일:

- `src/main/webapp/WEB-INF/jsp/admin/dashboard.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/products.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comments.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comment_reports.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/logs.jsp`

변경 내용:

- 관리자 대시보드에 신고 대기, 이미지 미확보, 추천 제외, 운영 로그 확인 카드 추가
- 플랫폼/피부 타입/주의 신호 집계 키를 사용자 친화 라벨로 변환
- 상품 관리 위험 작업에 confirm 추가
- 댓글 삭제/복구 작업에 confirm과 페이지 내 notice 추가
- 신고 관리에서 신고 사유, 댓글 상태를 원시 코드가 아닌 한글 문구로 표시
- 운영 로그의 `Y/N`, `ACTIVE/DELETED`, 품질 상태, JSON 변경값을 운영자가 읽는 문구로 변환

검증:

- `/admin`, `/admin/dashboard`, `/admin/products`, `/admin/comments`, `/admin/comment-reports`, `/admin/logs` HTTP 200
- `admin / 1234` 로그인 성공
- `/api/admin/summary`, `/api/admin/products`, `/api/admin/comments`, `/api/admin/comment-reports`, `/api/admin/logs` 200
- 미로그인 관리자 API 401, 일반 사용자 관리자 API 403 확인

한계:

- 데이터 변경이 큰 숨김/복구, 추천 제외/포함, 신고 처리 실제 클릭은 수행하지 않았다.
- in-app browser가 없어 개발자도구 Console 확인은 수행하지 못했다.

### Phase D 얼굴 등록/로그인 UX 개선

수정 파일:

- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/mypage.jsp`
- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/assets/css/beautylens.css`

변경 내용:

- 얼굴 등록 모달에 진행률, 현재 단계, 안내 문구, 처리 중 상태 추가
- 얼굴 등록 단계는 정면, 왼쪽, 오른쪽, 위쪽, 아래쪽 촬영 흐름 유지
- 얼굴 로그인은 3초 카운트다운 후 얼굴 확인 흐름으로 정리
- 카메라 권한 거부, 카메라 없음, 카메라 사용 중 오류를 사용자 친화 문구로 변환
- 카메라 시작 실패 시 Promise가 pending으로 남지 않고 reject 되도록 유지
- 로그인 화면에서 카메라 오류는 서버 장애 문구가 아니라 실제 카메라 안내 문구로 표시
- 마이페이지 얼굴 로그인 설정에 등록 단계 안내 pill 추가

검증:

- `/login`, `/mypage`, `/assets/js/face-camera.js`, `/assets/css/beautylens.css` HTTP 200
- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- 얼굴 서버 `/health` 200
- 사용자 화면 기술 용어 검색 결과 노출 없음

한계:

- 실제 카메라 권한 팝업, 촬영 성공/실패, 브라우저 Console은 in-app browser 미제공으로 검증하지 못했다.
- 얼굴 서버 미실행 상태는 이번 실행 환경에서 서버가 켜져 있어 별도 재현하지 않았다.

### Phase F 최종 검증

빌드:

- `mvn clean package` 성공

배포:

- 현재 실행 Tomcat은 Eclipse WTP 방식으로 확인
- 실행 배포 경로: `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc`
- 변경 JSP/CSS/JS를 WTP 배포 경로에 반영
- 실행 서버에서 변경된 `face-camera.js`, `beautylens.css`, 관리자 JSP 내용 반영 확인

HTTP 확인:

- `/beautylens-mvc/` 200
- `/beautylens-mvc/login` 200
- `/beautylens-mvc/signup` 200
- `/beautylens-mvc/products` 200
- `/beautylens-mvc/recommend` 200
- `/beautylens-mvc/products/1446` 200
- `/beautylens-mvc/mypage` 200
- `/beautylens-mvc/admin` 200
- `/beautylens-mvc/admin/dashboard` 200
- `/beautylens-mvc/admin/products` 200
- `/beautylens-mvc/admin/comments` 200
- `/beautylens-mvc/admin/comment-reports` 200
- `/beautylens-mvc/admin/logs` 200
- `/beautylens-mvc/assets/css/beautylens.css` 200
- `/beautylens-mvc/assets/js/ui.js` 200
- `/beautylens-mvc/assets/js/face-camera.js` 200

계정/API 확인:

- `test01 / 1234` 로그인 200
- `/api/members/me` 200
- `/api/members/me/favorites` 200
- `/api/members/me/ratings` 200
- `/api/members/me/recent-products` 200
- `/api/recommendations/me?size=5` 200
- `/api/products/1446/rating` 200
- `/api/products/1446/comments` 200
- `admin / 1234` 로그인 200
- `/api/admin/summary` 200
- `/api/admin/products?size=5` 200
- `/api/admin/comments?size=5` 200
- `/api/admin/comment-reports?status=all&size=5` 200
- `/api/admin/logs?size=5&actionType=all&targetType=all` 200

기술 용어 검색:

- `insightface-buffalo_l`, `buffalo_l`, `InsightFace`, `embedding`, `face vector`, `FastAPI`, `Python server`, `modelName`, `얼굴 특징`, `얼굴 인증 서버` 사용자 화면 JSP/JS 직접 노출 없음

남은 문제:

- 실제 브라우저 클릭 테스트 미수행
- 개발자도구 Console 확인 미수행
- 모바일 실제 렌더링 확인 미수행
- 데이터 변경을 수반하는 관리자 작업은 읽기 API와 버튼 유지 확인까지만 수행

## 6. `/review` P2 운영 정책 일관성 보완

작성일: 2026-07-01

### 수정 배경

관리자 상품 운영 정책이 일부 사용자 경로에서 일관되게 적용되지 않는 문제가 있었다.

- 추천 페이지 client fallback이 일반 상품 API를 사용해 추천 제외 상품을 다시 섞을 수 있음
- 숨김 상품이 과거 찜/평가/최근 본 상품/추천 피드백 목록에 남을 수 있음
- 숨김 상품 productId를 직접 호출하면 찜, 평가, 피드백, 이벤트, 댓글 작성이 가능할 수 있음

### 수정 파일

- `src/main/java/kr/ac/kopo/recommendation/service/RecommendationServiceImpl.java`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/resources/config/sqlMap/oracle/productInteractionMapper.xml`
- `src/main/resources/config/sqlMap/oracle/commentMapper.xml`

### 추천 제외 정책 적용

- `recommend.jsp`에서 `/api/products?sortBy=score&size=20` client fallback 제거
- 추천 부족분 보완은 서버 `RecommendationServiceImpl`에서 `RecommendationDAO.selectFallback()`으로 처리
- `recommendationMapper.xml`의 기존 fallback 조건은 `IS_VISIBLE='N' OR EXCLUDE_RECOMMENDATION='Y'` 제외 조건을 이미 포함하고 있어 추천 제외 정책을 서버 쪽으로 일원화
- 중복 상품은 Java 서비스에서 `productId` 기준으로 제거

### 숨김 상품 visibility 정책 적용

`productInteractionMapper.xml`에 public 상품 상세와 같은 숨김 제외 조건을 추가했다.

- `existsProduct`
- `selectFavorites`
- `selectRating`
- `selectRatingsByMember`
- `selectFeedbackByMember`
- `selectRecentProducts`

적용 조건:

```sql
AND NOT EXISTS (
    SELECT 1
      FROM BL_PRODUCT_ADMIN_FLAGS af
     WHERE af.PRODUCT_ID = p.PRODUCT_ID
       AND af.IS_VISIBLE = 'N'
)
```

`commentMapper.xml`에도 같은 정책을 적용했다.

- 댓글 목록 조회
- 내 댓글 조회
- 댓글 작성 전 상품 존재 확인

### direct interaction guard 보강

`ProductInteractionController`의 찜, 별점, 추천 피드백, 이벤트 기록은 기존에 `productInteractionService.productExists(productId)`를 거치고 있었다. 해당 mapper의 `existsProduct`에 visibility 조건을 추가해 direct API 요청도 숨김 상품에 대해 404로 처리되게 했다.

댓글 작성은 `CommentService.productExists(productId)`가 사용하는 `commentMapper.existsProduct`에 visibility 조건을 추가했다.

### 빌드 결과

- `mvn clean package` 성공

### 실행 서버 반영

현재 실행 서버는 Eclipse WTP 배포 방식이다.

- JSP 반영: `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc\WEB-INF\jsp\demo\recommend.jsp`
- mapper 반영:
  - `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc\WEB-INF\classes\config\sqlMap\oracle\productInteractionMapper.xml`
  - `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc\WEB-INF\classes\config\sqlMap\oracle\commentMapper.xml`
- class 반영:
  - `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc\WEB-INF\classes\kr\ac\kopo\recommendation\service\RecommendationServiceImpl.class`

### 검증 결과

HTTP 확인:

- `/beautylens-mvc/products` 200
- `/beautylens-mvc/recommend` 200
- `/beautylens-mvc/mypage` 200
- `/beautylens-mvc/products/1446` 200
- `/beautylens-mvc/admin/products` 200

추천 제외 검증:

- 추천 결과 첫 상품 `545`를 관리자 API로 추천 제외 처리
- `/api/recommendations?skinType=dry&size=20` 재조회 결과 해당 상품 포함 수 `0`
- 원래 flag로 복구

숨김 상품 direct interaction 검증:

- 상품 `1446`을 관리자 API로 숨김 처리
- `/api/products/1446/events` POST 결과 404
- `/api/products/1446/comments` POST 결과 404
- `/api/products/1446` GET 결과 404
- 원래 flag로 복구

마이페이지 최근 본 상품 검증:

- 상품 `1446` 최근 본 상품 이벤트 기록
- 숨김 처리 전 최근 본 상품 포함 수 `1`
- 숨김 처리 후 최근 본 상품 포함 수 `0`
- 원래 flag로 복구

정상 상품 회귀:

- 복구 후 `/api/products/1446` 200
- 복구 후 `/api/products/1446/events` POST 200

### 남은 검증 한계

- 찜/평가/추천 피드백 목록은 같은 mapper visibility 조건을 적용했지만, 데이터 변경을 피하기 위해 실제 hide 상태에서 개별 생성/조회/삭제까지 모두 수행하지는 않았다.
- 추천 fallback의 서버 top-up은 API 결과와 코드 경로로 확인했다. 추천 결과가 항상 20개 미만이 되는 특수 데이터 조건을 강제로 만들지는 않았다.

## 7. 댓글 수정과 평가 삭제 UX 보완

작성일: 2026-07-01

### 수정 배경

전체 기능을 다시 추적한 결과, 회원 의견과 사이트 평가에 다음 누락이 있었다.

- 댓글은 작성/삭제/신고만 가능하고 수정 기능이 없었다.
- 사이트 평가는 저장/수정은 가능했지만 삭제 API와 삭제 UX가 없었다.
- 마이페이지 활동 카드에서 내 의견과 내 평가를 바로 관리하는 흐름이 약했다.

### 수정 파일

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

### 추가 API

- `PUT /api/comments/{commentId}`
  - 작성자만 수정 가능
  - 삭제된 댓글은 404
  - 빈 내용 또는 1000자 초과는 400
- `DELETE /api/products/{productId}/rating`
  - 로그인 회원 본인의 사이트 평가 삭제
  - 숨김 상품은 기존 visibility guard에 따라 404

### UX 개선

- 상품 상세 댓글 목록에서 내 댓글은 `수정`, `삭제`가 함께 표시된다.
- 댓글 수정은 별도 페이지 이동 없이 인라인 textarea로 처리한다.
- 상품 상세 평가 폼에 기존 평가가 있으면 `내 평가 삭제` 버튼을 표시한다.
- 마이페이지 `내가 남긴 의견`에서 의견 수정/삭제가 바로 가능하다.
- 마이페이지 `내가 평가한 상품`에서 `평가 수정`, `평가 삭제`가 가능하다.

### 검증 결과

- `mvn clean package` 성공
- `/products`, `/recommend`, `/products/1446`, `/mypage`, `/admin/products` 200
- `test01 / 1234` 기준 댓글 작성 → 수정 → 수정 반영 확인 → 삭제 정리 성공
- `test01 / 1234` 기준 평가 저장 → 삭제 → 조회 결과 null 확인 성공

### 추가 역공학 점검 결과

현재 남아 있는 기능/UX 한계:

- 마이페이지 `계정 설정`은 아직 안내성 빈 상태이며 비밀번호 변경/회원 탈퇴 같은 실제 계정 관리 기능은 없다.
- 댓글 신고 사유는 상품 상세에서 기본 사유로 처리되며, 사용자용 신고 사유 선택 모달은 없다.
- 사이트 사용자 평가 요약의 자극 여부/재구매 의사 비율은 별도 집계 API가 없어 상세 수치로 표시하지 못한다.
- 추천 피드백은 저장/취소가 가능하지만 피드백 사유 수집은 없다.
- 실제 브라우저 클릭/Console 검증은 in-app browser 미제공으로 수행하지 못했다.

## 8. 얼굴 로그인 완성도 보강

작성일: 2026-07-01

### 수정 배경

얼굴 로그인에서 카메라가 켜진 뒤 사용자가 현재 상태를 알기 어려웠다. 특히 얼굴 확인 중인지, 로그인 정보를 확인 중인지, 실패한 것인지 구분되지 않아 처리 시간이 길면 화면이 멈춘 것처럼 보였다. 또한 정면 1장으로 충분히 확인되는 경우는 바로 로그인하되, 비슷한 얼굴 후보가 있거나 판정 여유가 작은 경우에는 추가 각도를 요청해야 했다.

### 수정 파일

- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/java/kr/ac/kopo/face/controller/FaceController.java`
- `src/main/java/kr/ac/kopo/face/service/FaceCredentialService.java`
- `src/main/java/kr/ac/kopo/face/service/FaceCredentialServiceImpl.java`

### UX 변경

- 얼굴 로그인은 촬영 버튼 없이 정면 얼굴을 자동 확인한다.
- 카메라 화면 위에 원형 스캔 링과 상태 배지를 표시한다.
- 상태 문구를 `얼굴을 화면 중앙에 맞춰주세요`, `얼굴 확인 중입니다`, `로그인 정보 확인 중입니다`, `얼굴 확인이 완료되었습니다. 로그인 중입니다` 순서로 보여준다.
- 실패 시 `얼굴을 확인하지 못했습니다. 다시 시도하거나 비밀번호로 로그인해 주세요` 계열의 사용자 문구를 표시한다.
- 비슷한 얼굴 후보가 있거나 정면 판정 여유가 작으면 왼쪽/오른쪽 얼굴을 추가로 촬영하도록 안내한다.

### 판정 보강

- 아이디를 입력한 얼굴 로그인도 여러 장의 이미지를 서버 판정에 반영하도록 변경했다.
- 정면 1장만으로 충분하면 기존처럼 바로 로그인한다.
- 정면 결과가 임계값에 너무 가까우면 409 응답으로 추가 각도를 요청한다.
- 추가 촬영 후에는 정면, 왼쪽, 오른쪽 이미지를 평균 판정해 오인식 가능성을 줄인다.

### 검증 결과

- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- `mvn clean package` 성공
- WTP 배포 경로와 standalone webapps 경로에 변경 JSP/JS/CSS/class 반영
- `/beautylens-mvc/login` HTTP 200
- `/beautylens-mvc/assets/js/face-camera.js` HTTP 200 및 새 자동 확인 문구 포함 확인
- `/beautylens-mvc/assets/css/beautylens.css` HTTP 200 및 스캔 링 스타일 포함 확인
- `http://127.0.0.1:8090/health` 응답 확인

### 검증 한계

- in-app browser 목록이 비어 있어 실제 카메라 권한 팝업, 자동 촬영, 성공/실패 모달, Console 오류는 직접 클릭 검증하지 못했다.
- Java class 변경은 배포 폴더에 반영했지만, 실행 중인 Tomcat이 class reload를 하지 않는 설정이면 Eclipse Server 재시작 또는 Clean/Publish 후 완전히 반영된다.

## 9. 얼굴 로그인 리뷰 P1/P2 후속 수정

작성일: 2026-07-01

### 수정 배경

리뷰에서 다음 문제가 확인되었다.

- 얼굴만으로 로그인하는 흐름에서 정면 이미지가 ambiguous라 추가 촬영한 뒤에도 best/second 점수 차이가 작으면 임의 계정으로 로그인될 수 있었다.
- 자동 촬영 후 서버 확인 중 사용자가 모달을 닫아도 이미 진행 중인 로그인 요청이 성공하면 redirect가 실행될 수 있었다.

### 수정 파일

- `src/main/java/kr/ac/kopo/face/service/FaceCredentialServiceImpl.java`
- `src/main/java/kr/ac/kopo/face/controller/FaceController.java`
- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`

### 수정 내용

- 얼굴만으로 계정을 식별하는 `identify(List<String>)`에서 best/second 차이가 `AMBIGUOUS_MARGIN`보다 작으면 3장 이후에도 성공 처리하지 않는다.
- 첫 정면 1장에서는 기존처럼 409로 추가 촬영을 요청한다.
- 추가 촬영 후에도 애매하면 `verified=false`, `memberId=null`로 처리해 로그인 실패로 돌린다.
- 사용자 문구는 `얼굴을 확실히 확인하지 못했습니다. 아이디를 입력하고 다시 시도하거나 비밀번호로 로그인해 주세요.`로 정리했다.
- `faceCaptureLogin()` 세션에 `isCanceled()`, `onCancel()`을 추가했다.
- 로그인 JSP는 얼굴 로그인 요청에 `AbortController`를 사용하고, 모달 닫기 시 진행 중인 `/api/auth/face-login` 요청을 abort한다.
- abort 또는 취소 상태에서는 성공 응답이 뒤늦게 와도 redirect를 실행하지 않는다.

### 검증 결과

- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- `mvn clean package` 성공
- WTP 배포 경로와 standalone webapps 경로에 변경 JSP/JS/class 반영
- Tomcat 재기동 후 `/beautylens-mvc/api/health` 200
- `/beautylens-mvc/login` 200
- `/beautylens-mvc/assets/js/face-camera.js` 200
- `/beautylens-mvc/login` 응답에 `AbortController`, `isCanceled`, 취소 안내 문구 포함 확인
- `test01 / 1234` 비밀번호 로그인 API 성공

### 검증 한계

- in-app browser가 제공되지 않아 실제 카메라 모달 닫기와 redirect 미발생은 브라우저 클릭으로 확인하지 못했다.
- 실제 ambiguous 3장 재시도 데이터는 준비하지 못해, 판정 로직 코드 경로와 빌드 기준으로 확인했다.

## 10. 얼굴 로그인 리뷰 P2 스트림/동시 실행 후속 수정

작성일: 2026-07-01

### 수정 배경

리뷰에서 다음 문제가 추가로 확인되었다.

- 카메라 권한 승인 또는 `openStream()` 완료 전에 모달을 닫으면, 이후 stream이 늦게 열려 카메라가 켜진 채 남을 수 있었다.
- 얼굴 로그인 버튼을 더블클릭하면 독립적인 얼굴 로그인 흐름이 여러 개 시작되어 요청과 redirect가 중복될 수 있었다.

### 수정 파일

- `src/main/webapp/assets/js/face-camera.js`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`

### 수정 내용

- `faceCaptureSequence()`에 `canceled`, `settled`, `cleaned` 플래그를 추가했다.
- `openStream()` 이후 취소/종료 상태를 다시 확인하고, 이미 닫힌 상태면 즉시 `stopStream(stream)` 후 반환한다.
- 자동 얼굴 로그인 캡처도 `openStream()` 이후 `canceled || done`을 확인해 늦게 열린 stream을 즉시 정리한다.
- `cleanup()`을 idempotent하게 만들어 같은 모달에서 중복 정리가 발생해도 안전하게 처리한다.
- 얼굴 로그인 버튼에 `activeFaceLogin` guard와 disabled 상태를 추가했다.
- 얼굴 로그인 전체 흐름이 끝날 때까지 버튼 재클릭을 막고, 실패/취소/오류에서는 `finally`에서 버튼 상태를 복구한다.
- 성공 redirect가 예정된 경우에는 이동 전 중복 클릭을 막기 위해 disabled 상태를 유지한다.

### 검증 결과

- `node --check src/main/webapp/assets/js/face-camera.js` 성공
- `mvn clean package` 성공
- WTP 배포 경로와 standalone webapps 경로에 변경 JSP/JS 반영
- `/beautylens-mvc/login` 200
- `/beautylens-mvc/assets/js/face-camera.js` 200
- `/beautylens-mvc/api/health` 200
- `/login` 응답에 `activeFaceLogin`, `faceLoginButton.disabled`, `redirectPending` 포함 확인
- `face-camera.js` 응답에 `settled`, `cleaned`, 취소 후 stream 정리 guard 포함 확인
- 사용자 화면 JSP/JS 기술 용어 직접 노출 없음

### 검증 한계

- in-app browser가 제공되지 않아 실제 카메라 권한 팝업 중 닫기, stream 정리, 더블클릭 방지는 브라우저 클릭으로 확인하지 못했다.
