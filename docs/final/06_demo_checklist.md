# 발표 전 점검표

## 서버 실행 확인

- [ ] Oracle XE 실행 확인
- [ ] Python 얼굴 인증 서버 실행 확인
- [ ] `http://127.0.0.1:8090/health` 응답 확인
- [ ] Eclipse Tomcat 실행 확인
- [ ] `http://localhost:8088/beautylens-mvc/` 접속 확인
- [ ] `http://localhost:8088/beautylens-mvc/api/health` 응답 확인

## 계정 확인

- [ ] `test01 / 1234` 로그인 확인
- [ ] `admin / 1234` 로그인 확인
- [ ] 로그아웃 확인

## 사용자 화면 확인

- [ ] 홈 화면 확인
- [ ] 상품 목록 `/products` 확인
- [ ] 추천 페이지 `/recommend` 확인
- [ ] 상품 상세 `/products/{productId}` 확인
- [ ] 상품 상세 외부 리뷰 분석 확인
- [ ] 사이트 사용자 평가 영역 확인
- [ ] 회원 의견 영역 확인
- [ ] 찜하기 확인
- [ ] 평가 등록 확인
- [ ] 평가 수정 확인
- [ ] 평가 삭제 확인
- [ ] 댓글 작성 확인
- [ ] 댓글 수정 확인
- [ ] 댓글 삭제 확인
- [ ] 댓글 신고 확인
- [ ] 마이페이지 탭 전환 확인
- [ ] 찜한 상품 확인
- [ ] 내가 평가한 상품 확인
- [ ] 최근 본 상품 확인
- [ ] 내가 남긴 의견 확인
- [ ] 추천 피드백 확인
- [ ] 얼굴 등록 확인
- [ ] 얼굴 로그인 확인

## 관리자 화면 확인

- [ ] 관리자 대시보드 `/admin` 확인
- [ ] 관리자 상품 관리 `/admin/products` 확인
- [ ] 상품 검색/필터 확인
- [ ] 상품 숨김/복구는 시연용 상품으로만 확인
- [ ] 추천 제외/포함은 시연용 상품으로만 확인
- [ ] 품질 상태 저장 확인
- [ ] 운영 메모 저장 확인
- [ ] 관리자 댓글 관리 `/admin/comments` 확인
- [ ] 댓글 삭제/복구 확인
- [ ] 신고 관리 `/admin/comment-reports` 확인
- [ ] 신고 처리 확인
- [ ] 운영 로그 `/admin/logs` 확인

## 브라우저 확인

- [ ] 개발자도구 Console 오류 확인
- [ ] CSS/JS 404 없음 확인
- [ ] 모바일 크기에서 주요 화면이 크게 깨지지 않는지 확인

## 발표용 URL 정리

| 구분 | URL |
|---|---|
| 홈 | `http://localhost:8088/beautylens-mvc/` |
| 로그인 | `http://localhost:8088/beautylens-mvc/login` |
| 상품 목록 | `http://localhost:8088/beautylens-mvc/products` |
| 추천 | `http://localhost:8088/beautylens-mvc/recommend` |
| 마이페이지 | `http://localhost:8088/beautylens-mvc/mypage` |
| 관리자 | `http://localhost:8088/beautylens-mvc/admin` |
| 관리자 상품 관리 | `http://localhost:8088/beautylens-mvc/admin/products` |
| 관리자 신고 관리 | `http://localhost:8088/beautylens-mvc/admin/comment-reports` |
| 관리자 로그 | `http://localhost:8088/beautylens-mvc/admin/logs` |

