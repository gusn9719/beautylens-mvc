# BeautyLens

피부 타입에 따라 화장품 리뷰의 평가가 크게 갈린다는 점에서 출발한 화장품 추천 서비스입니다. 단순 별점 평균이 아니라, 같은 피부 타입 사용자들의 리뷰 감성(긍/부정)을 집계해 "이 제품이 내 피부 타입에서는 실제로 어떤 평가를 받는지"를 보여주는 것이 목표입니다.

Spring MVC 6 + Oracle + MyBatis로 만든 웹 애플리케이션이며, 얼굴 인식 로그인은 별도의 Python FastAPI 서버로 분리되어 있습니다.

## 데이터는 어디서 왔나

이 프로젝트의 상품/리뷰 데이터는 이전에 별도로 만든 [올리브영 크롤링 프로젝트]([../oliveyoung_crawler](https://github.com/gusn9719/WebCrawling))에서 가져왔습니다. 그 프로젝트에서 올리브영/무신사/쿠팡의 리뷰 22만여 건을 크롤링하고, KLUE-BERT를 파인튜닝한 감성분석 모델로 리뷰마다 긍정/중립/부정 라벨을 붙인 뒤, 상품·피부타입별로 집계한 추천 점수 CSV를 만들었습니다. BeautyLens는 그 결과물(`product_recommendation_scores`, `product_skin_aggregates`, 리뷰 원본)을 Oracle DB로 임포트해서 서비스 데이터로 사용합니다.

쿠팡 리뷰는 피부 타입(`base_skin_type`) 정보가 전혀 없어서, 피부타입별 집계에서는 제외하고 올리브영/무신사 데이터만 사용했습니다.

## 주요 기능

- 회원가입 / 로그인 (일반 로그인 + 얼굴 인식 로그인)
- 상품 목록 / 상세, 피부타입 기반 추천
- 리뷰 조회, 즐겨찾기, 평점, 최근 조회, 추천 피드백
- 댓글 및 댓글 신고
- 관리자 콘솔 (상품 노출 관리, 신고 처리, 감사 로그)

## 기술 스택

| 영역 | 기술 |
|---|---|
| 백엔드 | Spring MVC 6.2, MyBatis 3.5, Oracle JDBC (ojdbc11), JDK 21 |
| 얼굴 인증 | FastAPI + InsightFace(ArcFace, buffalo_l) 별도 서버 |
| 뷰 | JSP |
| 데이터 파이프라인 | Python (crawling → 전처리 → KLUE-BERT 감성분석 → 추천 점수 산출) |

## 개발하면서 겪은 문제들

- Spring 6에서 `@PathVariable`/`@RequestParam`이 reflection으로 파라미터 이름을 읽는데, `-parameters` 컴파일 플래그 없이 빌드했더니 `Name for argument of type [int] not specified` 에러가 남. Spring Boot는 자동으로 잡아주지만 Spring MVC + Maven war 조합에서는 직접 추가해야 했음.
- `schema.sql`에 한글 주석을 달아뒀더니 sqlplus로 실행할 때 인코딩 문제로 컬럼 몇 개가 조용히 누락됨. `user_tab_columns`로 컬럼 수를 확인하고 나서야 발견해서 `ALTER TABLE`로 수동 보완.
- 상품명 정제 규칙에서 대괄호(`[...]`)를 프로모션 문구로 보고 지우게 했는데, `[SET]`처럼 정상적인 상품 구성 표기까지 같이 지워지는 버그가 있었음. 규칙에 예외를 추가해서 수정(v1.1).
- 상품 이미지 수집 중 일반 `requests`로는 Cloudflare에서 403이 떠서 `cloudscraper`로 바꿔야 했음.
- MyBatis에서 nullable 파라미터에 `jdbcType`을 안 적어줬더니 `ORA-17004` 오류 발생.
- 기능을 하나씩 만들어 붙이다 보니 화면들이 서로 이어지지 않고 각자 나열되어 있다는 걸 뒤늦게 깨닫고, 개발자용 값(`null`, `[]`, `ACTIVE` 같은)이 그대로 노출되지 않도록 화면을 다시 다듬는 재설계 작업을 했음.

## 실행 방법

1. Oracle XE 실행 (로컬 실습용 기본 계정 `hr/hr`, `localhost:1521/xepdb1` 기준으로 설정되어 있음 — 별도 계정을 쓰려면 `src/main/resources/config/spring/spring-mvc.xml`과 `scripts/` 하위 임포트 스크립트의 접속 정보를 바꿔야 함)
2. `docs/schema.sql` 및 `scripts/23_feature_expansion_phase1.sql` 등으로 스키마 생성
3. `scripts/import_products_full.py`, `scripts/import_reviews_full.py`로 데이터 임포트
4. Maven 빌드 후 Tomcat 11에 배포 (`scripts/start_beautylens_tomcat.ps1` 참고)
5. (선택) 얼굴 로그인을 쓰려면 `face_auth_server`를 별도로 실행. 단, InsightFace 모델 가중치(`buffalo_l`)는 용량 문제로 이 저장소에 포함하지 않았으므로 별도로 받아야 함

## 참고 문서

`docs/` 폴더에 데이터 임포트, 상품 품질 점검, 얼굴 로그인 설계, 재설계 등 단계별 진행 기록이 남아 있습니다.
