# 15. 오늘 시연용 실행 계획

작성일: 2026-06-30

## Phase 0. 기준선 확인

- 백업 폴더 생성
- `mvn clean package` 확인
- `/api/health`, Product, Recommendation, Admin API 확인

## Phase 1. 문서 작성

- `docs/14_master_prd_today_demo.md` 작성
- `docs/15_today_demo_execution_plan.md` 작성

## Phase 2. 공통 UI 기반

생성:
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/assets/js/api.js`
- `src/main/webapp/assets/js/ui.js`
- `src/main/webapp/WEB-INF/jsp/common/header.jsp`
- `src/main/webapp/WEB-INF/jsp/common/footer.jsp`

수정 또는 생성:
- JSP 화면 URL 매핑 컨트롤러
- `index.jsp`

검증:
- 정적 CSS/JS 로딩
- 상단 네비게이션 표시
- 로그인 상태 확인 API 호출

## Phase 3. 메인 페이지

- 서비스 소개
- 기능 카드 3개
- 이미지 상품 8~12개
- 추천 받기, 로그인, 관리자 CTA

검증:
- `/` HTTP 200
- 상품 카드 표시
- 이미지 없는 상품은 placeholder 처리

## Phase 4. 인증/마이페이지

- `/login`
- `/signup`
- `/mypage`

검증:
- `test01 / 1234` 로그인
- `admin / 1234` 로그인
- 로그아웃
- 미로그인 마이페이지 로그인 안내
- 회원 정보 수정

## Phase 5. 추천 화면

- `/recommend`
- `/api/recommendations/me?size=20`
- 이미지 필터, 플랫폼 필터

검증:
- 로그인 사용자 추천 20개 표시
- reason, platform, score, displayName 표시
- 카드 클릭 상세 이동

## Phase 6. 상품 상세와 댓글

- `/products/{productId}`
- 상품 정보, 리뷰, 댓글 표시
- 댓글 작성과 삭제

검증:
- 상품 상세 HTTP 200
- 긍정/부정 리뷰 목록 표시
- 댓글 작성 201
- 본인 댓글 삭제 200
- 삭제 후 일반 목록에서 제외

## Phase 7. 관리자 화면

- `/admin`
- `/admin/comments`
- summary 카드
- 댓글 필터와 삭제 버튼

검증:
- ADMIN 접근 가능
- USER 접근 차단
- 미로그인 로그인 안내
- 댓글 삭제 200
- DELETED 상태 확인

## Phase 8. 디자인 정리

- 카드 간격과 버튼 문구 정리
- 모바일 1열 대응
- 로딩, 에러, empty 상태 정리
- 이모지 제거

## Phase 9. E2E 검증

사용자:
1. `/`
2. 로그인
3. 추천
4. 상세
5. 댓글 작성
6. 본인 삭제
7. 마이페이지 수정

관리자:
1. admin 로그인
2. `/admin`
3. `/admin/comments`
4. 댓글 삭제
5. DELETED 확인

권한:
- 미로그인 관리자 접근 차단
- USER 관리자 접근 차단
- USER 타인 댓글 삭제 불가
- ADMIN 댓글 삭제 가능

## Phase 10. 완료 보고서

생성:
- `docs/16_today_demo_completion_report.md`

수정:
- `docs/03_development_log.md`

최종 확인:
- 빌드 성공
- WAR 재배포
- 브라우저 시연 가능
