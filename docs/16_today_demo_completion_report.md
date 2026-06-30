# 16. 오늘 시연용 JSP 웹사이트 완료 보고서

작성일: 2026-06-30

## 1. 최종 구현 범위

기존 Spring MVC 프로젝트 안에서 JSP, CSS, vanilla JavaScript(fetch) 기반 시연 웹사이트를 구현했다. React, Vite, npm 프론트 프로젝트는 추가하지 않았다. 기존 Product, Review, Member/Auth, Recommendation, Comment, Admin API를 그대로 사용했다.

## 2. 화면 목록

| URL | 상태 | 설명 |
|---|---|---|
| `/` | 완료 | 메인, 서비스 소개, 이미지 상품 카드 |
| `/login` | 완료 | 로그인 |
| `/signup` | 완료 | 회원가입 |
| `/mypage` | 완료 | 내 정보 조회/수정 |
| `/recommend` | 완료 | 추천 상품 목록, 이미지/플랫폼 필터 |
| `/products/{productId}` | 완료 | 상품 상세, 긍정/주의 리뷰, 회원 댓글 |
| `/admin` | 완료 | 관리자 대시보드 |
| `/admin/comments` | 완료 | 관리자 댓글 관리 |

## 3. 사용자 시연 시나리오

1. `/`에서 BeautyLens 소개와 이미지 상품 카드 확인
2. `/login`에서 `test01 / 1234` 로그인
3. `/recommend`에서 추천 상품 카드 확인
4. 상품 카드 클릭 후 `/products/{productId}` 이동
5. 상품 이미지, 추천 점수, 리뷰, 댓글 확인
6. 댓글 작성
7. 본인 댓글 삭제
8. `/mypage`에서 피부 타입과 관심사 수정
9. `/recommend`로 돌아가 추천 화면 확인

## 4. 관리자 시연 시나리오

1. `/login`에서 `admin / 1234` 로그인
2. `/admin`에서 상품, 리뷰, 회원, 댓글, 이미지 확보 현황 확인
3. `/admin/comments`에서 댓글 목록 확인
4. ACTIVE 댓글 삭제
5. DELETED 상태와 DELETE_REASON 확인

## 5. API 검증 결과

| 항목 | 결과 |
|---|---|
| `/api/health` | 200, db=ok |
| `/api/products?sortBy=score&size=20` | 200, 20건, displayName/imageUrl 확인 |
| `/api/recommendations/me?size=20` | 200, 현재 test01 기준 10건, reason/platform/score 확인 |
| `/api/products/{productId}` | 200, imageUrl/productUrl 확인 |
| Review API | positive/negative 200 |
| Admin summary/comments API | admin 200 |

## 6. 권한 검증 결과

| 시나리오 | 결과 |
|---|---|
| admin `/api/admin/summary` | 200 |
| test01 `/api/admin/summary` | 403 |
| 미로그인 `/api/admin/summary` | 401 |
| test02가 test01 댓글 삭제 시도 | 403 |
| admin 댓글 삭제 | 200 |
| 미로그인 댓글 삭제 | 401 |

## 7. 이미지 표시 정책

- `imageUrl`이 있으면 실제 상품 이미지를 표시한다.
- `imageUrl`이 없거나 로딩 실패 시 "이미지 준비 중" placeholder를 표시한다.
- 메인 화면은 `imageUrl`이 있는 상품을 우선 필터링한다.
- 추천 화면은 추천 API 결과가 20건 미만이면 Product API 점수 상위 상품을 보완 표시한다. 추천 API 자체는 변경하지 않았다.

## 8. 댓글 정책

- 상품 상세의 일반 댓글 목록은 ACTIVE 댓글만 표시한다.
- 로그인 회원만 댓글을 작성한다.
- 작성자 본인은 자기 댓글 삭제 버튼을 볼 수 있다.
- ADMIN은 모든 댓글 삭제 버튼을 볼 수 있다.
- 삭제 후 일반 목록에서 사라지고 관리자 목록에서 DELETED로 확인된다.

## 9. 관리자 정책

- `/admin`, `/admin/comments` 화면은 로딩 후 로그인 상태와 role을 확인한다.
- 미로그인은 로그인 안내를 표시한다.
- USER는 관리자 접근 차단 안내를 표시한다.
- 실제 데이터 접근은 관리자 API의 401/403/200 정책을 따른다.

## 10. 검증 상세

빌드:
```text
mvn clean package
BUILD SUCCESS
```

배포:
```text
D:\Lecture\bin\apache-tomcat-11.0.18\webapps\beautylens-mvc.war
```

JSP 라우트:
```text
/ 200
/login 200
/signup 200
/mypage 200
/recommend 200
/products/1446 200
/admin 200
/admin/comments 200
```

댓글 E2E:
```text
댓글 작성 201
본인 삭제 200
삭제 후 일반 목록 제외 true
타인 삭제 시도 403
관리자 삭제 200
미로그인 삭제 401
```

## 11. 남은 한계

- 브라우저 자동화 도구가 현재 세션에서 Windows 경로 메타데이터 문제로 실행되지 않아 HTTP/JSP 라우트/API/JS 문법 검사로 대체 검증했다.
- 현재 test01 추천 API는 20개 요청에 10개를 반환한다. 화면에서는 Product API 점수 상위 상품으로 보완 표시한다.
- 전체 1,521개 이미지 수집은 수행하지 않았다.
- 댓글 수정 기능은 없다.
- 관리자 화면은 대시보드와 댓글 관리만 제공한다.

## 11-1. 최종 UI/UX 보완 사항

- 일반 회원과 미로그인 사용자에게 관리자 메뉴가 보이지 않도록 공통 네비게이션을 정리했다.
- 로그인 화면에서 시연 계정 노출 문구를 제거했다. 시연 계정은 문서에만 유지한다.
- 상품 카드 제목과 추천 이유를 줄 수 제한으로 정리하고, 상세 버튼을 카드 하단에 정렬했다.
- 추천 필터와 관리자 댓글 상태 필터에 명확한 active 스타일과 `aria-pressed` 상태를 적용했다.
- 현재 URL 기준으로 홈/추천/마이페이지/관리자 nav active 상태를 보정한다.
- footer에서 기술 스택 문구를 제거하고 BeautyLens 사용자 문구로 정리했다.

검증:
- 로그인 화면 시연 계정 문구 미노출 확인
- 관리자 메뉴 초기 숨김 확인
- test01 role=USER, admin role=ADMIN 확인
- admin API 200, USER 403, 미로그인 401 유지
- 추천/상품/댓글 API 정상 유지

## 11-2. 실제 서비스형 UX 추가 보완

- 메인 hero에서 `관리자 페이지` CTA와 큰 로그인 CTA를 제거하고, 사용자 중심의 `추천 받기`, `피부 타입 선택하기` 동선으로 정리했다.
- 상단 네비게이션 정책을 확정했다.
  - 미로그인: 홈, 추천, 로그인
  - USER: 홈, 추천, 마이페이지, 로그아웃
  - ADMIN: 홈, 추천, 마이페이지, 관리자, 로그아웃
- 추천 페이지에 피부 타입 즉시 선택 버튼을 추가했다.
  - 건성, 지성, 복합성, 민감성, 중성
  - 선택은 임시 추천 기준이며 마이페이지 회원 정보는 자동 변경하지 않는다.
- 기존 추천 로직은 유지하고, `GET /api/recommendations?skinType={type}&size=20`만 추가했다.
- 마이페이지에 `내가 남긴 의견` 목록을 추가했다.
  - 상품명, 브랜드, 이미지, 댓글 내용, 작성일, 상태 표시
  - `상품 보러가기`로 상세 이동
  - ACTIVE 댓글은 본인 삭제 가능
- 화면에 남아 있던 시연/관리자/내부 구현 중심 문구를 사용자 서비스 문구로 정리했다.

검증:
- `mvn clean package`: BUILD SUCCESS
- WAR 재배포 완료
- `/`, `/login`, `/recommend`, `/mypage`, `/products/1446`, `/admin`, `/admin/comments`: HTTP 200
- 피부 타입별 추천 API 5종 모두 HTTP 200
- test01 댓글 작성 201, 마이페이지 댓글 조회 200, 본인 삭제 200, 삭제 상태 `DELETED`
- 관리자 API 권한: 미로그인 401, USER 403, ADMIN 200

## 12. 다음에 하면 좋은 작업

- 추천 API 결과 부족 시 서버 측 fallback 정책 정리
- 전체 이미지 수집 별도 실행
- 댓글 수정 기능
- 관리자 댓글 목록 페이지네이션
- CSS/JS 파일 캐시 버전 관리

## 12-1. 얼굴 로그인 추가 구현 결과

- Python FastAPI 얼굴 인증 서버를 `face_auth_server`에 추가했다.
- InsightFace `buffalo_l` 모델과 ONNXRuntime CPU 실행을 확인했다.
- Oracle `BL_FACE_CREDENTIALS`에 얼굴 원본이 아닌 임베딩 JSON만 저장한다.
- 마이페이지에서 얼굴 등록/재등록/해제가 가능하다.
- 로그인 페이지에서 loginId + 얼굴 촬영으로 로그인할 수 있다.
- 기존 비밀번호 로그인은 유지한다.

검증:
- Python `/health`: 200, `modelName=insightface-buffalo_l`
- 얼굴 미등록 사용자 face-login: 404
- Python 서버 off 등록 시도: 503
- 샘플 얼굴 등록: 200
- 샘플 얼굴 로그인: 200
- 얼굴 등록 해제: 200
- 기존 Product/Recommendation/Comment/Admin API 정상 유지

한계:
- 이 자동화 세션에서는 실제 웹캠 촬영 E2E를 수행하지 못했다.
- FastAPI 서버는 별도 터미널에서 실행해야 한다.

## 13. 시연 계정

| 역할 | 아이디 | 비밀번호 |
|---|---|---|
| 일반 회원 | test01 | 1234 |
| 관리자 | admin | 1234 |

## 14. 권장 시연 순서

1. `/`
2. `/login`에서 test01 로그인
3. `/recommend`
4. 상품 상세
5. 댓글 작성/삭제
6. `/mypage`
7. 로그아웃
8. admin 로그인
9. `/admin`
10. `/admin/comments`
