# 14. BeautyLens 오늘 시연용 Master PRD

작성일: 2026-06-30

## 1. 프로젝트 목표

BeautyLens는 피부 타입, 상품 리뷰, 추천 점수, 회원 의견을 함께 보여 주는 Spring MVC 기반 화장품 추천 서비스다. 오늘 목표는 이미 구현된 API를 JSP 웹 화면으로 연결해 바로 시연 가능한 웹사이트를 완성하는 것이다.

## 2. 오늘 시연 목표

- 일반 사용자가 로그인하고 추천 상품을 확인한다.
- 추천 상품 상세에서 리뷰 신호와 회원 댓글을 확인한다.
- 회원이 댓글을 작성하고 본인 댓글을 삭제한다.
- 관리자가 통계 대시보드와 댓글 관리 화면을 확인한다.
- 관리자 권한, 미로그인 차단, 일반 사용자 차단을 화면에서 확인한다.

## 3. 현재 완료된 백엔드 기능

- Product API, Review API
- Member/Auth API
- Recommendation API
- displayName 응답 반영
- imageUrl, productUrl, imageStatus 일부 상품 반영
- 회원 댓글 작성, 목록, soft delete
- 작성자 본인 댓글 삭제
- ADMIN 전체 댓글 삭제
- 관리자 summary, comments API
- admin/1234, test01/1234 시연 계정

## 4. 오늘 구현할 화면 목록

| URL | 목적 |
|---|---|
| `/` | 메인, 서비스 소개, 이미지 상품 카드 |
| `/login` | 로그인 |
| `/signup` | 회원가입 |
| `/mypage` | 내 정보 확인 및 수정 |
| `/recommend` | 로그인 회원 추천 상품 목록 |
| `/products/{productId}` | 상품 상세, 리뷰, 회원 댓글 |
| `/admin` | 관리자 대시보드 |
| `/admin/comments` | 관리자 댓글 관리 |

## 5. 오늘 구현하지 않을 것

- React, Vite, npm 기반 프론트 프로젝트
- DB 데이터 삭제 또는 import 재실행
- 전체 이미지 수집
- 추천 로직 변경
- displayName 로직 변경
- PRODUCT_NAME 수정
- 상품 자동 병합
- 대규모 구조 리팩토링

## 6. 사용자 시연 동선

1. `/` 접속 후 이미지 상품 카드 확인
2. `/login`에서 `test01 / 1234` 로그인
3. `/recommend`에서 추천 상품 20개 확인
4. 상품 카드를 눌러 `/products/{productId}` 이동
5. 긍정/부정 리뷰와 추천 이유 확인
6. 댓글 작성
7. 본인 댓글 삭제
8. `/mypage`에서 피부 타입과 관심사를 수정
9. 다시 추천 화면 확인

## 7. 관리자 시연 동선

1. `/login`에서 `admin / 1234` 로그인
2. `/admin`에서 통계 카드 확인
3. `/admin/comments`에서 댓글 목록 확인
4. ACTIVE 댓글 삭제
5. DELETED 상태와 삭제 사유 확인

## 8. API 사용 계획

- 공통 로그인 상태: `GET /api/members/me`
- 로그인: `POST /api/auth/login`
- 로그아웃: `POST /api/auth/logout`
- 회원가입: `POST /api/members`
- 내 정보 수정: `PUT /api/members/me`
- 메인 상품: `GET /api/products?sortBy=score&size=20`
- 추천: `GET /api/recommendations/me?size=20`
- 상품 상세: `GET /api/products/{productId}`
- 리뷰: `GET /api/products/{productId}/reviews/positive`, `GET /api/products/{productId}/reviews/negative`
- 댓글: `GET/POST /api/products/{productId}/comments`, `DELETE /api/comments/{commentId}`
- 관리자: `GET /api/admin/summary`, `GET /api/admin/comments`, `DELETE /api/admin/comments/{commentId}`

## 9. 이미지 표시 정책

- `imageUrl`이 있으면 상품 이미지로 표시한다.
- `imageUrl`이 없거나 로딩 실패 시 CSS placeholder를 표시한다.
- 메인 페이지는 프론트에서 `imageUrl`이 있는 상품을 우선 필터링한다.
- 이미지 비율은 카드와 상세 모두 `object-fit: cover`로 유지한다.

## 10. 댓글 표시/삭제 정책

- 일반 상품 상세 댓글 목록은 ACTIVE 댓글만 표시한다.
- 로그인 회원만 댓글을 작성할 수 있다.
- 작성자 본인은 자기 댓글 삭제 버튼을 볼 수 있다.
- ADMIN은 모든 댓글 삭제 버튼을 볼 수 있다.
- 삭제는 soft delete이며 일반 목록에서는 사라지고 관리자 목록에서는 DELETED로 보인다.

## 11. 관리자 권한 정책

- ADMIN만 `/admin`, `/admin/comments` 데이터를 볼 수 있다.
- 미로그인은 로그인 안내를 표시한다.
- 일반 USER는 접근 차단 안내를 표시한다.
- 서버 API 권한 정책은 401/403을 그대로 유지한다.

## 12. 디자인 가이드

- 흰색과 연한 회색 배경
- 넓은 여백, 8px 중심의 둥근 카드
- 부드러운 그림자
- 이미지 중심 상품 카드
- 버튼, 배지, 탭 스타일 일관성
- 모바일 1열 카드 배치
- 이모지 사용 금지
- JSON 원문 노출 금지
- 부정 리뷰 신호는 차분한 문구로 표현

## 13. 단계별 구현 계획

1. 공통 CSS, 공통 JS, header/footer JSP 생성
2. DemoViewController로 화면 URL 연결
3. 메인 페이지 구현
4. 로그인, 회원가입, 마이페이지 구현
5. 추천 상품 화면 구현
6. 상품 상세와 댓글 기능 구현
7. 관리자 대시보드와 댓글 관리 구현
8. 반응형 및 오류 표시 정리
9. HTTP 및 브라우저 검증
10. 완료 보고서 작성

## 14. 단계별 검증 기준

- Maven 빌드 성공
- Tomcat WAR 재배포 후 JSP URL 200
- API 기존 응답 유지
- 로그인/로그아웃 정상
- 추천 20개 표시
- 상품 상세 리뷰와 댓글 표시
- 댓글 작성 201, 본인 삭제 200
- 관리자 summary/comments 200
- USER 관리자 접근 403 또는 차단 안내
- 미로그인 관리자 접근 401 또는 로그인 안내

## 15. 최종 시연 체크리스트

- `/` 이미지 상품 카드 표시
- `/login` 시연 계정 로그인 가능
- `/recommend` 추천 카드 표시
- `/products/{productId}` 상세, 리뷰, 댓글 표시
- 댓글 작성과 본인 삭제 가능
- `/mypage` 회원 정보 수정 가능
- `/admin` 통계 표시
- `/admin/comments` 댓글 삭제와 DELETED 상태 표시
- 모바일 폭에서도 카드와 버튼이 깨지지 않음
