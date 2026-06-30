# Runtime Bugfix and E2E Report

작성일: 2026-06-30

## 1. 실행 중인 Tomcat 배포 방식

- `server.xml`: `D:\Lecture\eclipse-server\conf\server.xml`
- HTTP port: `8088`
- Shutdown port: `8005`
- `CATALINA_HOME`: `D:\Lecture\bin\apache-tomcat-11.0.18`
- `CATALINA_BASE`: `D:\Lecture\eclipse-server`
- 실제 `/beautylens-mvc` Context:

```text
<Context docBase="D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc"
         path="/beautylens-mvc"
         reloadable="true"
         source="org.eclipse.jst.jee.server:beautylens-mvc"/>
```

따라서 현재 실행 환경은 standalone `webapps`가 아니라 Eclipse WTP 배포 경로를 기준으로 동작한다.

## 2. 빌드 및 배포 반영

- `mvn clean package`: 성공
- WAR 생성: `target/beautylens-mvc-0.0.1-SNAPSHOT.war`
- WTP 배포 반영:

```text
D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc
```

수정 후 `productInteractionMapper.xml` 타임스탬프가 소스와 WTP 배포본에서 동일함을 확인했다.

## 3. 발견한 오류

### ORA-17004

증상:

```text
POST /beautylens-mvc/api/products/743/events HTTP/1.1" 500
ORA-17004: 열 유형이 부적합합니다.
```

원인:

- `BL_USER_PRODUCT_EVENTS.EVENT_VALUE`는 `VARCHAR2(200)`이다.
- `eventValue`가 null인 상태로 MyBatis가 `jdbcType` 없이 바인딩하면서 Oracle JDBC가 부적합한 null 타입으로 처리했다.

수정:

- `productInteractionMapper.xml`
  - `eventValue,jdbcType=VARCHAR`
  - `skinTypeAtTime,jdbcType=VARCHAR`
  - 평점/추천 피드백 nullable 파라미터에도 `jdbcType` 명시
- 같은 유형의 런타임 오류를 막기 위해 다음 mapper의 nullable 파라미터도 보강:
  - `adminProductMapper.xml`
  - `commentReportMapper.xml`

## 4. 화면 검증 결과

모두 실제 HTTP 기준 200:

- `/`
- `/products`
- `/recommend`
- `/products/1446`
- `/mypage`
- `/admin`
- `/admin/products`
- `/admin/comments`
- `/admin/comment-reports`
- `/admin/logs`
- `/api/health`

## 5. API 검증 결과

공개 API:

- `/api/products?sortBy=score&size=20`: 200
- `/api/products?imageOnly=true&sortBy=score&size=20`: 200
- `/api/recommendations?skinType=dry&size=20`: 200

미로그인 보호 API:

- `/api/members/me/favorites`: 401
- `/api/members/me/ratings`: 401
- `/api/members/me/recent-products`: 401
- `/api/admin/summary`: 401
- `/api/admin/products`: 401
- `/api/admin/comment-reports`: 401
- `/api/admin/logs`: 401

위 401은 정상 보호 동작이다.

## 6. test01 E2E 결과

계정: `test01 / 1234`

- 로그인: 200
- `/api/members/me`: 200
- `/api/recommendations/me?size=20`: 200
- USER가 `/api/admin/summary` 접근: 403
- 상품 상세 조회 이벤트 기록: 200
- 상품 찜하기: 200
- 찜 해제: 200
- 별점/자극 여부/재구매 의사 저장: 200
- 추천 피드백 저장: 200
- 마이페이지 찜 조회: 200
- 마이페이지 평가 조회: 200
- 최근 본 상품 조회: 200
- 추천 피드백 내역 조회: 200
- 댓글 작성: 201
- 댓글 신고: 201

## 7. admin E2E 결과

계정: `admin / 1234`

- 로그인: 200
- 관리자 요약: 200
- 관리자 상품 목록: 200
- 상품 운영 플래그 조회: 200
- 상품 숨김: 200
- 숨김 후 일반 상품 상세 제외: 404
- 상품 복구: 200
- 복구 후 일반 상품 상세: 200
- 추천 제외: 200
- 추천 포함: 200
- 품질 상태/운영 메모 저장: 200
- 신고 댓글 목록: 200
- 관리자 댓글 삭제: 200
- 댓글 복구: 200
- 관리자 활동 로그: 200

숨김 상품 일반 상세 404는 정상 동작이다.

## 8. 기존 기능 회귀 검증

- 비밀번호 로그인: 정상
- 얼굴 서버 `/health`: 200
- 추천 페이지: 200
- 상품 상세: 200
- 댓글 작성/삭제: 정상
- 관리자 댓글 삭제: 정상
- 관리자 권한 USER 403: 정상
- 미로그인 관리자 API 401: 정상

## 9. 브라우저/JS 검증

브라우저 개발자도구 직접 확인은 수행하지 못했다. 대신 다음을 확인했다.

- 정적 파일 HTTP 로딩:
  - `/assets/js/api.js`: 200
  - `/assets/js/ui.js`: 200
  - `/assets/js/face-camera.js`: 200
  - `/assets/css/beautylens.css`: 200
- Node 문법 검사:
  - `node --check src/main/webapp/assets/js/api.js`: 통과
  - `node --check src/main/webapp/assets/js/ui.js`: 통과
  - `node --check src/main/webapp/assets/js/face-camera.js`: 통과

## 10. 추가된 검증 스크립트

- `scripts/start_beautylens_tomcat.ps1`
- `scripts/runtime_e2e_check.ps1`

검증 스크립트는 새 기능이 아니라 재배포/런타임 검증 보조 도구다.

## 11. 남은 문제

- Eclipse GUI의 Servers 탭에서 직접 `Clean/Publish/Start`를 누르는 검증은 사용자가 Eclipse를 조작해야 한다.
- 현재 코드와 WTP 배포 폴더에는 최신 수정이 반영되어 있다.
- 브라우저 개발자도구 Console 기반 JS 런타임 확인은 수행하지 못했다.

## 12. 최종 판정

실제 Tomcat/WTP 배포 경로 기준으로 HTTP/API/E2E 검증을 완료했다. `ORA-17004`는 해결됐고, Phase 1~8 신규 화면과 API는 런타임 기준으로 동작한다.

Eclipse에서 서버를 실행할 경우 `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc` 기준으로 publish된 상태라 시연 가능하다. Eclipse가 다시 덮어쓰는 경우에는 `Clean/Publish` 후 실행하면 된다.
