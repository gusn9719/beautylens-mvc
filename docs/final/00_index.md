# BeautyLens Final Documentation

이 폴더는 BeautyLens 최종 발표와 보고서 작성을 위해 정리한 문서 모음입니다. 실제 SQL, MyBatis Mapper, Java Controller/Service/DAO, JSP 화면, import 스크립트와 기존 개발 보고서를 확인한 뒤 작성했습니다.

## 문서 목록

| 순서 | 문서 | 설명 |
|---|---|---|
| 1 | [01_project_overview.md](01_project_overview.md) | BeautyLens가 어떤 사이트인지, 왜 만들었는지, 전체 사용자/관리자 흐름 |
| 2 | [02_database_and_erd.md](02_database_and_erd.md) | DB 테이블 역할, 논리 관계, ERD, 데이터 적재 흐름 |
| 3 | [03_feature_summary.md](03_feature_summary.md) | 사용자 기능과 관리자 기능을 화면, URL, 테이블, Controller/Mapper 기준으로 정리 |
| 4 | [04_implemented_and_not_implemented.md](04_implemented_and_not_implemented.md) | 구현한 것과 시간상 보완으로 남긴 것 |
| 5 | [05_demo_script.md](05_demo_script.md) | 발표 시연 순서와 발표 멘트 |
| 6 | [06_demo_checklist.md](06_demo_checklist.md) | 발표 전 실행/기능 점검표 |
| 7 | [07_table_data_flow.md](07_table_data_flow.md) | 크롤링 데이터가 서비스 DB와 화면에서 어떻게 사용되는지 설명 |

## 발표 전에 보는 순서

1. `01_project_overview.md`로 서비스 목적을 먼저 정리한다.
2. `02_database_and_erd.md`에서 상품, 리뷰, 회원 활동, 관리자 운영 테이블이 왜 분리됐는지 확인한다.
3. `03_feature_summary.md`에서 기능별 URL과 관련 테이블을 확인한다.
4. `05_demo_script.md`를 띄워놓고 실제 시연 순서를 연습한다.
5. 발표 직전에는 `06_demo_checklist.md`로 서버, DB, 계정, 주요 기능을 점검한다.

## 시연 전에 확인할 것

- Oracle XE가 실행 중인지 확인한다.
- Python 얼굴 인증 서버가 `http://127.0.0.1:8090/health`에 응답하는지 확인한다.
- Eclipse Tomcat이 `http://localhost:8088/beautylens-mvc/`에 배포됐는지 확인한다.
- 사용자 계정 `test01 / 1234` 로그인 확인.
- 관리자 계정 `admin / 1234` 로그인 확인.
- 상품 상세, 찜, 평가, 댓글, 신고, 관리자 상품 관리, 운영 로그가 시연용 데이터에서 동작하는지 확인.

