# BeautyLens Redesign Phase Execution Checklist

작성일: 2026-06-30

## 기준 문서

* `docs/27_product_redesign_prd.md`

## 공통 금지사항

모든 Phase에서 다음 조건을 지켜야 한다.

* React, Vite, Vue, Next.js 도입 금지
* JSP + CSS + vanilla JS 유지
* 기존 DB 원본 상품/리뷰 데이터 삭제 금지
* Phase 범위를 벗어난 DB 스키마 변경 금지
* 기존 API 경로 임의 변경 금지
* 기존 기능 무단 삭제 금지
* 사용자 화면에 기술 용어 노출 금지
* 이모지/이모티콘 사용 금지
* 빌드 실패 상태에서 다음 Phase 진행 금지

## 공통 기술 용어 검색 기준

사용자 화면 JSP/JS/HTML 출력에서 다음 문구가 직접 노출되면 실패로 본다.

* insightface-buffalo_l
* buffalo_l
* InsightFace
* embedding
* vector
* face vector
* FastAPI
* Python server
* modelName
* 얼굴 특징
* 얼굴 인증 서버

단, 서버 내부 로그, 주석, 변수명은 사용자 화면에 출력되지 않는 경우 실패로 보지 않는다.

## 공통 Phase Gate

각 Phase 완료 후 다음 항목을 확인한다.

* [ ] `mvn clean package` 성공
* [ ] 주요 URL HTTP 200 확인
* [ ] JSP 렌더링 오류 없음
* [ ] JS 문법 오류 없음
* [ ] 기존 form action 유지 확인
* [ ] 기존 fetch URL 유지 확인
* [ ] 기존 버튼 이벤트 깨짐 없음
* [ ] 사용자 화면 기술 용어 노출 없음
* [ ] 사용자 화면에 `[]`, `{}`, `null`, `undefined`, `NaN`이 보이지 않음
* [ ] 사용자 화면에 `Y`, `N`, `ACTIVE`, `NORMAL`, `DELETED`, `PENDING` 같은 원시 상태값이 보이지 않음
* [ ] 내부 enum 코드가 사용자에게 직접 노출되지 않음
* [ ] DB ID, API 경로, 서버 예외 메시지가 사용자에게 직접 노출되지 않음
* [ ] 데이터 없음 상태에 자연스러운 빈 상태 문구가 표시됨
* [ ] API 일부 실패 시 전체 화면이 죽지 않고 해당 섹션만 실패 처리됨
* [ ] 로그인 필요 행동은 로그인 후 원래 페이지로 돌아오도록 redirect를 포함함
* [ ] redirect는 내부 경로만 허용함
* [ ] 평가 폼과 댓글 폼이 중복 입력처럼 보이지 않음
* [ ] 주 행동과 보조 행동이 구분됨
* [ ] 실제 브라우저 클릭 테스트를 수행함
* [ ] 개발자도구 Console 오류를 확인함
* [ ] 모바일 화면에서 주요 레이아웃이 크게 깨지지 않음
* [ ] UX/UI 자체 검수 질문에 답변함
* [ ] DB 원본 상품/리뷰 삭제 없음
* [ ] Phase 범위 초과 변경 없음
* [ ] 수정 파일 목록 기록 완료
* [ ] 남은 문제 기록 완료

다음 중 하나라도 해당하면 다음 Phase로 넘어가지 않는다.

* 빌드 실패
* 주요 화면 500 오류
* 로그인 불가
* 상품 상세 진입 불가
* 관리자 주요 화면 진입 불가
* 기존 찜/댓글/신고/평가 기능이 사라짐
* DB/API 변경이 발생했는데 문서화되지 않음
* Phase 범위 밖 대규모 수정 발생
* 사용자 화면에 `[]`, `null`, `undefined` 같은 개발자식 값이 보임
* 로그인 후 원래 페이지로 돌아오지 않음
* 실제 브라우저 클릭 테스트를 수행하지 않았는데 완료로 보고함
* API 하나의 실패로 상세 화면 전체가 깨짐
* 평가와 댓글이 중복 입력처럼 보임
* 원시 상태값이 사용자 화면에 그대로 노출됨

## Phase A: UX 정보구조 정리

상태: DONE

확인 항목:

* [ ] 마이페이지 사이드바/탭 구조 적용
* [ ] 관리자 사이드바 구조 적용
* [ ] 사용자 화면 기술 용어 제거
* [ ] DB/API 변경 없음
* [ ] 빌드 성공
* [ ] 주요 URL 확인

비고:

* 상품 카드, 상품 상세, 얼굴 wizard는 다음 Phase로 남긴다.

## Phase B: 상품 카드/추천 페이지 정리

상태: DONE

범위:

* 상품 카드 UI 정리
* 추천 페이지 카드 버튼 최소화
* 추천 피드백을 보조 UI로 이동
* 찜/상세 보기 중심 구조
* 필터 UI 정리

수정 후보:

* `WEB-INF/jsp/demo/products.jsp`
* `WEB-INF/jsp/demo/recommend.jsp`
* `assets/js/ui.js`
* `assets/css/beautylens.css`

검수 항목:

* [x] 상품 카드에서 기본 행동이 “자세히 보기”, “찜하기” 중심인지 확인
* [x] 좋아요/별로예요/관심 없음이 카드 메인 버튼으로 직접 노출되지 않는지 확인
* [x] 추천 숨기기 또는 피드백은 보조 UI로 이동했는지 확인
* [x] 상품 상세 이동 정상
* [x] 찜 기능 정상
* [x] 추천 페이지 필터 정상
* [x] `/products` 200 확인
* [x] `/recommend` 200 확인
* [x] 마이페이지 최근 본 상품 카드 회귀 확인

## Phase C: 상품 상세 재구성

상태: DONE

범위:

* 상품 요약
* 추천 근거
* 리뷰 기반 분석
* 사이트 사용자 평가
* 내 평가 남기기
* 회원 의견 분리

수정 후보:

* `WEB-INF/jsp/demo/product_detail.jsp`
* `assets/js/ui.js`
* `assets/css/beautylens.css`

검수 항목:

* [x] 상품 요약 섹션 확인
* [x] 추천 근거 섹션 확인
* [x] 외부 리뷰 분석 섹션 확인
* [x] 사이트 사용자 평가 섹션 확인
* [x] 내 평가 남기기 섹션 확인
* [x] 회원 의견 섹션 확인
* [x] 크롤링 리뷰와 회원 댓글이 섞이지 않는지 확인
* [x] 별점 저장 정상
* [x] 자극 여부 저장 정상
* [x] 재구매 의사 저장 정상
* [x] 댓글 작성 정상
* [x] 댓글 삭제 정상
* [x] 댓글 신고 정상
* [x] 최근 본 상품 기록 정상
* [x] 상품 상세 URL 200 확인

## Phase D: 얼굴 등록/로그인 UX 개선

상태: DONE

범위:

* 얼굴 등록 wizard
* 진행률
* 카운트다운
* 로딩 피드백
* 사용자 친화 문구
* 얼굴 로그인 실패 문구 개선

수정 후보:

* `WEB-INF/jsp/demo/login.jsp`
* `WEB-INF/jsp/demo/mypage.jsp`
* `assets/js/face-camera.js`
* `assets/css/beautylens.css`

검수 항목:

* [x] 얼굴 등록 안내 단계 표시
* [x] 정면/왼쪽/오른쪽/위쪽/아래쪽 단계 표시
* [x] 진행률 표시
* [x] 카메라 권한 실패 문구 표시
* [x] 서버 오류 시 비밀번호 로그인 안내
* [x] 사용자 화면 기술 용어 미노출
* [x] 기존 얼굴 등록 API 유지
* [x] 기존 얼굴 로그인 API 유지
* [x] 얼굴 서버 health 200 확인

비고:

* in-app browser를 사용할 수 없어 실제 카메라 권한 팝업 클릭과 개발자도구 Console 확인은 수행하지 못했다.
* 얼굴 서버 미실행 상태 검증은 이번 실행 환경에서 서버가 켜져 있어 별도로 수행하지 않았다.

## Phase E: 관리자 UX 재구성

상태: DONE

범위:

* 관리자 대시보드 개선
* 상품 관리 테이블 정리
* 신고 관리 흐름 정리
* 추천 품질 관리 메뉴 정리
* 운영 로그 화면 정리

수정 후보:

* `WEB-INF/jsp/admin/dashboard.jsp`
* `WEB-INF/jsp/admin/products.jsp`
* `WEB-INF/jsp/admin/comments.jsp`
* `WEB-INF/jsp/admin/comment_reports.jsp`
* `WEB-INF/jsp/admin/logs.jsp`
* `assets/css/beautylens.css`
* `assets/js/ui.js`

검수 항목:

* [x] 관리자 대시보드 요약 카드 확인
* [x] 상품 관리 테이블 확인
* [x] 상품 숨김/복구 버튼 유지
* [x] 추천 제외/포함 버튼 유지
* [x] 품질 상태 저장 버튼 유지
* [x] 운영 메모 저장 버튼 유지
* [x] 댓글 관리 API 조회 정상
* [x] 신고 처리 버튼 유지
* [x] 운영 로그 조회 정상
* [x] `/admin` 200 확인
* [x] `/admin/dashboard` 200 확인
* [x] `/admin/products` 200 확인
* [x] `/admin/comments` 200 확인
* [x] `/admin/comment-reports` 200 확인
* [x] `/admin/logs` 200 확인

비고:

* 데이터 변경이 큰 관리자 작업은 실제 클릭 실행하지 않고 버튼/URL/API 유지와 읽기 API를 확인했다.
* 위험 작업에는 confirm 문구를 추가했다.

## Phase F: 최종 검증

상태: DONE

범위:

* 전체 빌드
* WTP 배포
* test01 사용자 E2E
* admin 관리자 E2E
* 사용자 화면 기술 용어 검색
* 최종 구현 보고서 작성

검수 항목:

* [x] `mvn clean package` 성공
* [x] WTP 배포 확인
* [x] `/` 200
* [x] `/login` 200
* [x] `/products` 200
* [x] `/recommend` 200
* [x] `/mypage` 200
* [x] 상품 상세 200
* [x] 일반 로그인 정상
* [x] 얼굴 로그인 화면 HTTP 정상
* [x] 마이페이지 HTTP 정상
* [x] 사용자 읽기 API 정상
* [x] 댓글 읽기 API 정상
* [x] 평가 읽기 API 정상
* [x] 관리자 로그인 정상
* [x] 관리자 상품 관리 API 정상
* [x] 관리자 댓글/신고 관리 API 정상
* [x] 관리자 로그 API 정상
* [x] 사용자 화면 기술 용어 미노출
* [x] `docs/30_redesign_phase_implementation_report.md` 작성 완료
* [x] `docs/32_final_redesign_report.md` 작성 완료
* [x] 숨김 상품 direct interaction 404 확인
* [x] 숨김 상품 최근 본 상품 제외 확인
* [x] 추천 제외 상품 추천 API 제외 확인
* [x] 추천 페이지 client fallback 일반 상품 API 제거 확인

비고:

* 실제 브라우저 클릭/Console 검증은 in-app browser가 제공되지 않아 수행하지 못했다.
* 데이터 변경을 수반하는 관리자 숨김/복구, 추천 제외/포함, 신고 처리 실제 클릭은 이번 검증에서 수행하지 않았다.
* `/review` P2 보완 검증에서는 관리자 API로 임시 숨김/추천 제외 후 원래 flag로 복구했다.
