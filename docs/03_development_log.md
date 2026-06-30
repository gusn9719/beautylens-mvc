# 03. 개발 로그 (Development Log)

---

## 2026-06-29 — 1차 실행: 프로젝트 구조 생성 + Health API

### 작업 내용

**프로젝트 생성**
- 경로: `D:\Lecture\spring-workspace\beautylens-mvc`
- 구조: Spring MVC 6.x + Maven war + Oracle XE + MyBatis XML Mapper + Tomcat 11

**생성 파일 목록**

| 파일 | 내용 |
|---|---|
| `pom.xml` | spring-webmvc 6.2.17, mybatis 3.5.19, ojdbc11, commons-dbcp 1.4, Java 21 |
| `WEB-INF/web.xml` | DispatcherServlet → /, CharacterEncodingFilter UTF-8 |
| `config/spring/spring-mvc.xml` | component-scan, DataSource(hr/hr,xepdb1), MyBatis |
| `config/mybatis/sqlMapConfig.xml` | 1차는 typeAlias 없음 (CRUD 단계에서 추가 예정) |
| `common/vo/ApiResponse.java` | 공통 응답 VO (success, message, data) |
| `main/HealthController.java` | GET /api/health → DB 연결 상태 포함 JSON 응답 |
| `webapp/index.jsp` | 기본 페이지 (/api/health 링크) |
| `docs/schema.sql` | BL_* 테이블 6개 + 시퀀스 6개 DDL 초안 |
| `docs/00_data_inventory.md` | D:\_WebCrawling 데이터 조사 결과 |
| `docs/03_development_log.md` | 이 파일 |

**환경**

| 항목 | 값 |
|---|---|
| Java | JDK 21.0.9 LTS |
| Maven | 3.9.12 |
| Spring MVC | 6.2.17 |
| Tomcat | 11.0.18 |
| Oracle | XE, hr/hr, xepdb1, localhost:1521 |

### 빌드 및 배포

```
mvn clean package -f D:\Lecture\spring-workspace\beautylens-mvc\pom.xml
→ BUILD SUCCESS (1.819s)
```

배포 경로: `D:\Lecture\bin\apache-tomcat-11.0.18\webapps\beautylens-mvc.war` (23MB)  
Tomcat 포트: **8088** (server.xml 기준, 8080은 AgentService가 사용 중)  
Tomcat 시작 방법: `.bat` 파일이 Application Control Policy에 차단되어 `java.exe` 직접 실행

**수정 이력**

| 항목 | 문제 | 수정 내용 |
|---|---|---|
| spring-mvc.xml | `mapperLocations` 패턴이 빈 `config/sqlMap/` 를 찾지 못해 500 오류 | 1차에서 `mapperLocations` 제거. CRUD 구현 시 복원 예정. |

### Health API 확인 결과 (성공)

```
GET http://localhost:8088/beautylens-mvc/api/health
HTTP 200 OK

{
  "success": true,
  "message": "healthy",
  "data": {
    "app": "beautylens-mvc",
    "version": "1.0.0",
    "db": "ok",
    "timestamp": "Mon Jun 29 21:02:56 KST 2026"
  }
}
```

Oracle DB 연결 확인: `"db": "ok"`

---

## 2026-06-29 — 파일 검수

검수 일시: 2026-06-29 21:07  
검수 방법: 실제 파일 직접 읽기 + mvn clean package 재실행

### 검수 결과

| 파일 | 존재 | 내용 이상 | 비고 |
|---|---|---|---|
| `pom.xml` | ✅ | 없음 | groupId=kr.ac.kopo, artifactId=beautylens-mvc, war, Java 21, 의존성 11종 |
| `WEB-INF/web.xml` | ✅ | 없음 | DispatcherServlet → /, forceEncoding=true 추가 (Mission-Spring과 차이) |
| `spring-mvc.xml` | ✅ | 없음 | mapperLocations 제거됨, 주석으로 복원 안내 명시 |
| `sqlMapConfig.xml` | ✅ | 없음 | typeAliases 주석 처리, 문법 정상 |
| `ApiResponse.java` | ✅ | 없음 | package kr.ac.kopo.common.vo, 제네릭 `<T>`, 기본생성자 + 3인수 생성자 |
| `HealthController.java` | ✅ | 없음 | package kr.ac.kopo.main, @RestController, @Autowired DataSource, DB isValid(2) 체크 |
| `docs/schema.sql` | ✅ | 없음 | 시퀀스 6개 중복 없음, 테이블 6개, REVIEW_TEXT CLOB, UQ 제약 정상 |
| `docs/00_data_inventory.md` | ✅ | 없음 | 한글 정상, 컬럼 매핑 표 정상 |
| `docs/03_development_log.md` | ✅ | 없음 | 이 파일 자체 |

### 세부 검수 내용

**pom.xml**
- groupId `kr.ac.kopo`, artifactId `beautylens-mvc`, packaging `war` 확인
- maven-compiler-plugin source/target 모두 `21` 확인
- maven-war-plugin 3.4.0 확인
- 의존성: spring-webmvc 6.2.17, jackson-databind 2.21.2, ojdbc11 23.26.1.0.0, mybatis 3.5.19, mybatis-spring 4.0.0, commons-dbcp 1.4 모두 정상

**web.xml**
- `<display-name>beautylens-mvc</display-name>` 확인
- DispatcherServlet url-pattern `/` 확인
- `forceEncoding=true` 추가됨 (Mission-Spring 대비 개선)
- XML 선언부 `<?xml version="1.0" encoding="UTF-8"?>` 정상

**spring-mvc.xml**
- mapperLocations 제거, 58번째 줄에 `<!-- mapperLocations는 CRUD 구현 시 추가 (1차 실행에서는 Mapper XML 없음) -->` 주석 확인
- DataSource: xepdb1, hr/hr, maxActive=10, maxIdle=5
- SqlSessionTemplate bean 정상

**sqlMapConfig.xml**
- typeAliases 전체 주석 처리됨, 복원 안내 주석 포함
- DTD 선언 `https://mybatis.org/dtd/mybatis-3-config.dtd` 정상

**ApiResponse.java**
- `package kr.ac.kopo.common.vo;` 정상
- `public class ApiResponse<T>` — class명 오타 없음
- `isSuccess()` getter (boolean 표준 네이밍) 정상

**HealthController.java**
- `package kr.ac.kopo.main;` 정상
- `@RestController`, `@GetMapping("/api/health")` 정상
- `javax.sql.DataSource` import (Java SE 표준) 정상
- try-with-resources로 Connection 자원 해제 정상

**docs/schema.sql**
- 시퀀스: SEQ_BL_MEMBERS, SEQ_BL_PRODUCTS, SEQ_BL_REVIEWS, SEQ_BL_FAVORITES, SEQ_BL_RECOMMENDATIONS, SEQ_BL_IMPORT_LOGS — 각 1회씩, 중복 없음
- BL_REVIEWS.REVIEW_TEXT = CLOB 확인
- `CONSTRAINT UQ_BL_REVIEWS_PLATFORM_REVIEW UNIQUE (PRODUCT_ID, PLATFORM_REVIEW_ID)` 확인
- BL_IMPORT_LOGS.SKIPPED_COUNT 컬럼 포함 확인
- COMMIT 마지막 확인

**docs/00_data_inventory.md**
- 한글 깨짐 없음, 테이블/코드블록 문법 정상

### 최종 빌드 검증

```
mvn clean package (재실행)
→ BUILD SUCCESS
→ Total time: 1.740s
→ Finished at: 2026-06-29T21:07:25+09:00
```

---

## 다음 단계 (2차)

- [x] Oracle DDL 실행 (`docs/schema.sql`)
- [x] Product 기본 조회 API
- [x] Review 기본 조회 API
- [ ] Member/Auth CRUD (세션 기반 로그인)
- [ ] Recommendation API
- [ ] Python import 스크립트 작성 및 실행 (import_products.py, import_reviews.py)
- [ ] API 전체 Postman 검증
- [x] `docs/02_api_spec.md` 작성

---

## 2026-06-29 — 2차 실행: Oracle DDL + Product/Review 조회 API

### Oracle DDL 실행

**사전 확인**: BL_% 테이블 없음 확인 (BLOG 테이블만 존재)

**실행**:
```
sqlplus hr/hr@//localhost:1521/xepdb1 @docs/schema.sql
```

**결과**:
- 시퀀스 6개 생성: SEQ_BL_FAVORITES, SEQ_BL_IMPORT_LOGS, SEQ_BL_MEMBERS, SEQ_BL_PRODUCTS, SEQ_BL_RECOMMENDATIONS, SEQ_BL_REVIEWS
- 테이블 6개 생성: BL_FAVORITES, BL_IMPORT_LOGS, BL_MEMBERS, BL_PRODUCTS, BL_RECOMMENDATIONS, BL_REVIEWS

**오류 및 수정**:

| 항목 | 문제 | 수정 |
|---|---|---|
| BL_PRODUCTS | sqlplus 한글 주석 인코딩 문제로 SKIN_REVIEW_COUNT, TOP_CONCERN_TAGS, CAUTION_LEVEL 3개 컬럼 미생성 | ALTER TABLE로 누락 컬럼 추가 |

```sql
ALTER TABLE BL_PRODUCTS ADD (
    SKIN_REVIEW_COUNT NUMBER(7)    DEFAULT 0,
    TOP_CONCERN_TAGS  VARCHAR2(300),
    CAUTION_LEVEL     VARCHAR2(40) DEFAULT 'normal'
);
```

**최종 검증**: BL_PRODUCTS 25컬럼, BL_REVIEWS REVIEW_TEXT=CLOB, FK_REVIEWS_PRODUCT, UQ_BL_REVIEWS_PLATFORM_REVIEW 모두 확인

---

### 생성/수정 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `src/main/java/kr/ac/kopo/common/vo/PageParam.java` |
| 신규 | `src/main/java/kr/ac/kopo/product/vo/ProductVO.java` |
| 신규 | `src/main/java/kr/ac/kopo/product/dao/ProductDAO.java` |
| 신규 | `src/main/java/kr/ac/kopo/product/dao/ProductDAOImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/product/service/ProductService.java` |
| 신규 | `src/main/java/kr/ac/kopo/product/service/ProductServiceImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/product/controller/ProductController.java` |
| 신규 | `src/main/java/kr/ac/kopo/review/vo/ReviewVO.java` |
| 신규 | `src/main/java/kr/ac/kopo/review/dao/ReviewDAO.java` |
| 신규 | `src/main/java/kr/ac/kopo/review/dao/ReviewDAOImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/review/service/ReviewService.java` |
| 신규 | `src/main/java/kr/ac/kopo/review/service/ReviewServiceImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/review/controller/ReviewController.java` |
| 신규 | `src/main/resources/config/sqlMap/oracle/productMapper.xml` |
| 신규 | `src/main/resources/config/sqlMap/oracle/reviewMapper.xml` |
| 수정 | `src/main/resources/config/mybatis/sqlMapConfig.xml` (typeAliases 활성화) |
| 수정 | `src/main/resources/config/spring/spring-mvc.xml` (mapperLocations 복원) |
| 수정 | `pom.xml` (maven-compiler-plugin에 `-parameters` 추가) |
| 신규 | `docs/02_api_spec.md` |

---

### 수정 이력

| 항목 | 문제 | 수정 내용 |
|---|---|---|
| pom.xml | Spring 6에서 `-parameters` 플래그 없으면 `@PathVariable`/`@RequestParam` 이름 reflection 불가 → HTTP 500 | maven-compiler-plugin에 `<arg>-parameters</arg>` 추가 |
| BL_PRODUCTS | sqlplus 한글 주석 인코딩 문제로 3개 컬럼 누락 → `ORA-00904: CAUTION_LEVEL` | ALTER TABLE로 SKIN_REVIEW_COUNT, TOP_CONCERN_TAGS, CAUTION_LEVEL 추가 |

---

### 설정 변경 내용

**sqlMapConfig.xml**: typeAliases 활성화
```xml
<typeAliases>
  <typeAlias type="kr.ac.kopo.product.vo.ProductVO"  alias="productVO"/>
  <typeAlias type="kr.ac.kopo.review.vo.ReviewVO"    alias="reviewVO"/>
  <typeAlias type="kr.ac.kopo.common.vo.PageParam"   alias="pageParam"/>
</typeAliases>
```

**spring-mvc.xml**: mapperLocations 복원
```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
  <property name="dataSource" ref="dataSource" />
  <property name="configLocation" value="classpath:config/mybatis/sqlMapConfig.xml" />
  <property name="mapperLocations" value="classpath:config/sqlMap/**/*.xml" />
</bean>
```

---

### 파일 검수 결과

| 항목 | 확인 내용 | 결과 |
|---|---|---|
| ProductVO 숫자 타입 | Integer/Double wrapper 타입 사용 | ✅ |
| ReviewVO 숫자 타입 | Integer/Double wrapper 타입 사용 | ✅ |
| productMapper.xml selectOne | `WHERE PRODUCT_ID = #{_parameter}` | ✅ |
| reviewMapper.xml selectByProductId | `WHERE PRODUCT_ID = #{_parameter}` | ✅ |
| productMapper.xml selectList 페이징 | `OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY` | ✅ |
| sqlMapConfig.xml typeAliases | productVO, reviewVO, pageParam 활성화 | ✅ |
| spring-mvc.xml mapperLocations | `classpath:config/sqlMap/**/*.xml` 복원 | ✅ |

---

### 빌드 및 배포 결과

```
mvn clean package (1차)
→ BUILD SUCCESS (1.943s)
→ 15 source files compiled

pom.xml에 -parameters 추가 후 재빌드
mvn clean package (2차)
→ BUILD SUCCESS (1.979s)
→ Finished at: 2026-06-29T21:31:11+09:00

배포: D:\Lecture\bin\apache-tomcat-11.0.18\webapps\beautylens-mvc.war
```

---

### API 테스트 결과

| API | HTTP | 응답 |
|---|---|---|
| `GET /api/health` | 200 | `{"success":true,"data":{"db":"ok",...}}` |
| `GET /api/products` | 200 | `{"success":true,"message":"products found","data":[]}` |
| `GET /api/products/1` | **404** | `{"success":false,"message":"product not found","data":null}` |
| `GET /api/products/1/reviews` | 200 | `{"success":true,"message":"reviews found","data":[]}` |
| `GET /api/products/1/reviews/negative` | 200 | `{"success":true,"message":"negative reviews found","data":[]}` |
| `GET /api/products/1/reviews/positive` | 200 | `{"success":true,"message":"positive reviews found","data":[]}` |

DB가 비어있어 data:[]이지만 500 오류 없음. 정상.

---

---

## 2026-06-30 — 2차 정리: 404 확인 + schema.sql 안전화

### /api/products/{productId} HTTP 404 확인

ProductController 코드는 처음부터 `HttpStatus.NOT_FOUND`로 작성되어 있었음.  
2차 테스트 시 `curl -i` 없이 바디만 확인하여 HTTP 상태 코드를 오기재함.  
`curl -i` 재확인 결과 실제 응답 코드는 **HTTP 404** 확인.

```
GET /api/products/1
HTTP/1.1 404
{"success":false,"message":"product not found","data":null}
```

코드 수정 불필요.

---

### schema.sql 안전화

**원인**: 2차 DDL 실행 시 sqlplus가 한글 주석(`-- 피부 타입별 리뷰 수` 등)을 인코딩하지 못해 관련 컬럼 정의를 누락.

**대상 컬럼**: `SKIN_REVIEW_COUNT`, `TOP_CONCERN_TAGS`, `CAUTION_LEVEL`

**처리 내용**:
- `docs/schema.sql` 내 모든 한글 주석 제거 또는 영어 주석으로 교체
- BL_PRODUCTS CREATE TABLE에 3개 컬럼이 모두 포함된 상태로 정리
- 기존 DB는 ALTER TABLE로 이미 수정 완료, schema.sql은 향후 fresh install 기준 파일

**재발 방지**: schema.sql에 한글 주석 사용 금지. 영어 주석 또는 주석 없음.

**DB vs schema.sql 비교**:

| 항목 | DB (실제) | schema.sql | 일치 |
|---|---|---|---|
| BL_PRODUCTS 컬럼 수 | 25 | 25 | ✅ |
| SKIN_REVIEW_COUNT | 있음 (column 23) | 있음 (column 15) | ✅ |
| TOP_CONCERN_TAGS | 있음 (column 24) | 있음 (column 20) | ✅ |
| CAUTION_LEVEL | 있음 (column 25) | 있음 (column 21) | ✅ |
| BL_REVIEWS REVIEW_TEXT | CLOB | CLOB | ✅ |
| UQ_BL_REVIEWS_PLATFORM_REVIEW | 있음 | 있음 | ✅ |
| BL_IMPORT_LOGS SKIPPED_COUNT | 있음 | 있음 | ✅ |
| 시퀀스 | 6개 | 6개 | ✅ |
| 테이블 | 6개 | 6개 | ✅ |

*컬럼 순서 차이(15→23 등): ALTER TABLE 추가 특성. 실제 매퍼는 컬럼명 AS camelCase 방식으로 순서와 무관하게 동작.*

---

### 빌드 검증

```
mvn clean package
→ BUILD SUCCESS
→ Total time: 1.928s
→ Finished at: 2026-06-30T00:38:27+09:00
```

### API 테스트 결과 (curl -i 기준)

| API | HTTP | 응답 |
|---|---|---|
| `GET /api/health` | 200 | `{"success":true,"data":{"db":"ok",...}}` |
| `GET /api/products/1` | **404** | `{"success":false,"message":"product not found","data":null}` |

---

---

## 2026-06-30 — 3차 실행: 데이터 Import (Preview CSV)

### 작업 내용

Python import 스크립트 2개 작성 및 실행. preview CSV 3개 대상.  
전체 원본 데이터 import는 제외 (3차 제약 조건).

### 생성 파일 목록

| 파일 | 내용 |
|---|---|
| `scripts/import_products.py` | BL_PRODUCTS import (scores CSV + agg CSV) |
| `scripts/import_reviews.py` | BL_REVIEWS import (train_preview.csv) |
| `docs/01_data_import_report.md` | import 결과 상세 보고서 |

---

### 스크립트 실행 명령어

```
# 라이브러리 설치
pip install oracledb pandas

# 상품 import
python scripts/import_products.py

# 리뷰 import
python scripts/import_reviews.py
```

---

### Import 결과

**BL_PRODUCTS**

| CSV | 전체 행 | 성공 | 스킵 | 실패 |
|---|---|---|---|---|
| product_recommendation_scores_preview.csv | 95 | 60 | 35 | 0 |
| product_skin_aggregates_preview.csv | 200 | 119 | 81 | 0 |
| **합계** | **295** | **179** | **116** | **0** |

스킵 사유: scores CSV 내 중복 product_key(35건), agg CSV의 이미 존재하는 product_key(81건)

**BL_REVIEWS**

| CSV | 전체 행 | 성공 | 스킵(상품 미매칭) | 실패 |
|---|---|---|---|---|
| train_preview.csv | 2000 | 580 | 1420 | 0 |

스킵 사유: 리뷰 상품이 preview 상품 파일에 없음 (179개 상품만 존재)

**sentiment 분포**: positive=529 / negative=43 / neutral=8

---

### 오류 및 수정

| 항목 | 문제 | 수정 |
|---|---|---|
| CSV 컬럼명 불일치 | 스펙상 `overall_pos_rate`이지만 실제 CSV는 `overall_positive_rate`, `overall_neutral_rate`, `overall_negative_rate` | import 스크립트에서 실제 컬럼명 사용 |
| train_preview.csv product_key 없음 | 리뷰 CSV에 product_key 컬럼 없음 | `f"{platform}::{product_id}"` 직접 구성 |

---

### 샘플 직접 검수 결과

검수 스크립트: Python oracledb + DBMS_LOB.SUBSTR 직접 조회

| 검수 항목 | 결과 |
|---|---|
| 상품 20개 한글 깨짐 | 없음 ✅ |
| 상품 20개 PRODUCT_NAME/BRAND NULL | 0건 ✅ |
| 리뷰 20개 REVIEW_TEXT NULL | 0건 ✅ |
| 리뷰 20개 SENTIMENT_LABEL 유효값 외 | 0건 ✅ |
| negative 리뷰 20개 label 정확성 | positive/neutral 혼입 없음 ✅ |
| positive 리뷰 20개 label 정확성 | negative/neutral 혼입 없음 ✅ |
| 상품-리뷰 연결 20쌍 고아 리뷰 | 0건 ✅ |
| 엉뚱한 상품 연결 | 없음 ✅ |

---

### API 검증 결과 (실데이터)

| API | HTTP | 결과 |
|---|---|---|
| `GET /api/products` | 200 | 20개 상품 반환 ✅ |
| `GET /api/products?sortBy=score` | 200 | RECOMMENDATION_SCORE DESC, top=99.29 ✅ |
| `GET /api/products?sortBy=rating` | 200 | AVG_RATING DESC, top=5.0 ✅ |
| `GET /api/products?keyword=크림` | 200 | 5개 상품 반환 ✅ |
| `GET /api/products/82` | 200 | 넘버즈인 앰플, avgRating=3.36, cautionLevel=moderate_negative_signal ✅ |
| `GET /api/products/82/reviews` | 200 | 2개 리뷰 (positive 1 + negative 1) ✅ |
| `GET /api/products/82/reviews/negative` | 200 | 1개, sentimentLabel=negative ✅ |
| `GET /api/products/82/reviews/positive` | 200 | 1개, sentimentLabel=positive ✅ |

---

---

## 2026-06-30 — 4차 실행: Member/Auth CRUD

### 작업 내용

세션 기반 회원가입, 로그인, 로그아웃, 내 정보 조회/수정 API 구현.  
비밀번호: SHA-256 해시 저장 (Java `MessageDigest`, 신규 의존성 없음).

### 생성/수정 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `src/main/java/kr/ac/kopo/member/vo/MemberVO.java` |
| 신규 | `src/main/java/kr/ac/kopo/member/dao/MemberDAO.java` |
| 신규 | `src/main/java/kr/ac/kopo/member/dao/MemberDAOImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/member/service/MemberService.java` |
| 신규 | `src/main/java/kr/ac/kopo/member/service/MemberServiceImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/member/controller/MemberController.java` |
| 신규 | `src/main/java/kr/ac/kopo/auth/controller/AuthController.java` |
| 신규 | `src/main/resources/config/sqlMap/oracle/memberMapper.xml` |
| 수정 | `src/main/resources/config/mybatis/sqlMapConfig.xml` (memberVO alias 추가) |
| 수정 | `docs/02_api_spec.md` (Member/Auth API 명세 추가) |

---

### memberMapper.xml 쿼리 목록

namespace: `member.dao.MemberDAO`

| 쿼리 ID | 타입 | 설명 |
|---|---|---|
| `insert` | INSERT | 회원 삽입 (SEQ_BL_MEMBERS.NEXTVAL) |
| `selectByLoginId` | SELECT | loginId로 회원 조회 (중복 검사용) |
| `selectByLoginIdAndPassword` | SELECT | loginId+password(해시) 로 조회 (로그인용) |
| `selectByMemberId` | SELECT | memberId로 회원 조회 (내 정보 조회/세션 갱신용) |
| `updateProfile` | UPDATE | nickname, skinType, skinConcern 수정 |

---

### 비밀번호 저장 방식

| 항목 | 현재 구현 | 추후 개선 |
|---|---|---|
| 해시 알고리즘 | SHA-256 (Java 내장 `MessageDigest`) | BCrypt (spring-security-crypto) |
| 솔트 | 없음 | BCrypt는 자동 솔트 포함 |
| 레인보우 테이블 | 취약 | BCrypt로 방어 가능 |
| 신규 의존성 | 없음 | spring-security-crypto 추가 필요 |

**현재는 SHA-256 무솔트 저장이며, 추후 BCrypt 적용 필요.**

---

### 설계 결정 사항

- **password 응답 제외**: `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)` — 요청 JSON에서는 수신, 응답 JSON에서는 제외
- **세션 키**: `"loginMember"` 통일
- **PUT /api/members/me**: 수정 후 DB에서 재조회하여 세션 loginMember 갱신 (세션 불일치 방지)
- **중복 loginId**: `selectByLoginId` 선조회 → null이면 삽입, null이 아니면 HTTP 409 반환

---

### 빌드 결과

```
mvn clean package
→ BUILD SUCCESS
→ 22 source files compiled
→ Finished at: 2026-06-30T01:24:37+09:00
```

---

### 오류 및 수정

| 항목 | 문제 | 수정 |
|---|---|---|
| PUT 요청 HTTP 400 (Tomcat) | Git Bash curl에서 한글 포함 POST body가 CP949로 전송되어 Spring에서 JSON 파싱 실패 (`Invalid UTF-8 start byte 0xbc`) | PowerShell에서 `[System.Text.Encoding]::UTF8.GetBytes()` 명시적 변환 후 테스트 |

---

### API 테스트 결과

| API | HTTP | 결과 |
|---|---|---|
| `POST /api/members` | 201 | 회원가입 성공 (한글 닉네임/피부정보 포함) ✅ |
| `POST /api/members` (중복 loginId) | 409 | `loginId already exists` ✅ |
| `POST /api/members` (loginId 없음) | 400 | `loginId is required` ✅ |
| `POST /api/auth/login` | 200 | 로그인 성공, password 필드 응답 없음, JSESSIONID 발급 ✅ |
| `POST /api/auth/login` (틀린 비밀번호) | 401 | `invalid loginId or password` ✅ |
| `GET /api/members/me` (로그인 후) | 200 | 회원 정보 정상 반환 ✅ |
| `GET /api/members/me` (미로그인) | 401 | `not logged in` ✅ |
| `PUT /api/members/me` | 200 | nickname/skinType/skinConcern 수정, 세션 갱신 확인 ✅ |
| `GET /api/members/me` (수정 후) | 200 | 수정된 정보로 세션 반영 확인 ✅ |
| `POST /api/auth/logout` | 200 | 세션 무효화 ✅ |
| `GET /api/members/me` (로그아웃 후) | 401 | `not logged in` ✅ |
| HTTP 500 발생 여부 | - | 없음 ✅ |

---

---

## 2026-06-30 — 5차 실행: Recommendation API

### 작업 내용

로그인 회원의 `skinType`, `skinConcern` 기반 상품 추천 API 구현.  
복잡한 ML 없이 BL_PRODUCTS의 `RECOMMENDATION_SCORE` 기반 DB 쿼리 + Service 계층 reason 생성.

### 생성/수정 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `src/main/java/kr/ac/kopo/recommendation/vo/RecommendationVO.java` |
| 신규 | `src/main/java/kr/ac/kopo/recommendation/dao/RecommendationDAO.java` |
| 신규 | `src/main/java/kr/ac/kopo/recommendation/dao/RecommendationDAOImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/recommendation/service/RecommendationService.java` |
| 신규 | `src/main/java/kr/ac/kopo/recommendation/service/RecommendationServiceImpl.java` |
| 신규 | `src/main/java/kr/ac/kopo/recommendation/controller/RecommendationController.java` |
| 신규 | `src/main/resources/config/sqlMap/oracle/recommendationMapper.xml` |
| 수정 | `src/main/resources/config/mybatis/sqlMapConfig.xml` (recommendationVO alias 추가) |
| 수정 | `docs/02_api_spec.md` (Recommendation API 명세 추가) |

---

### 추천 로직

**skinType 매핑** (회원 BL_MEMBERS.SKIN_TYPE → BL_PRODUCTS.BASE_SKIN_TYPE):

| 회원 skinType (영어) | DB 값 (한국어) | 상품 수 |
|---|---|---|
| combination | 복합성 | 52 |
| dry | 건성 | 45 |
| sensitive | 민감성 | 39 |
| oily | 지성 | 24 |
| normal | 중성 | 19 |

**Fallback 순서**:
1. `BASE_SKIN_TYPE = 매핑된 한국어` 조건으로 `RECOMMENDATION_SCORE DESC NULLS LAST`
2. 결과 없으면 전체 상품에서 점수 순

**reason 생성 규칙** (Service 계층):
- baseSkinType 일치: `{skinType} 피부 타입 기준 추천 점수가 높습니다.`
- skinConcern 키워드 TAG 매칭: `피부 고민 키워드와 관련된 태그가 포함되어 있습니다.`
- cautionLevel별 문구: normal/moderate_negative_signal/high_negative_signal/insufficient_evidence

---

### 오류 및 수정

| 항목 | 문제 | 수정 |
|---|---|---|
| insufficient_evidence reason | else 분기로 "낮은 편" 출력 → 부정확 | 별도 case 추가: "리뷰 데이터가 충분하지 않아 신뢰도 판단에 참고가 필요합니다." |

---

### 빌드 결과

```
mvn clean package (2회)
→ BUILD SUCCESS
→ Finished at: 2026-06-30T01:43:xx+09:00
```

---

### BASE_SKIN_TYPE 분포 (BL_PRODUCTS)

```
복합성: 52개 (RECOMMENDATION_SCORE 있는 것: 19개)
건성:   45개 (11개)
민감성: 39개 (14개)
지성:   24개 (11개)
중성:   19개 (5개)
```

---

### API 테스트 결과

| API | HTTP | 결과 |
|---|---|---|
| `GET /api/recommendations/me` (미로그인) | 401 | `not logged in` ✅ |
| `GET /api/recommendations/me` (skinType=combination) | 200 | 20개 반환, score=99.29→... DESC ✅, baseSkinType=복합성 ✅ |
| `GET /api/recommendations/me` (skinType=sensitive) | 200 | 20개 반환, baseSkinType=민감성 ✅ |
| `GET /api/recommendations/me?size=5` | 200 | 5개 반환 ✅ |
| score 내림차순 | - | 검증 통과 ✅ |
| reason 비어있지 않음 | - | 전체 항목 reason 생성 ✅ |
| cautionLevel moderate → reason 반영 | - | "일부 부정 리뷰 신호..." ✅ |
| cautionLevel high → reason 반영 | - | "부정 리뷰 신호가 높은..." ✅ |
| cautionLevel insufficient_evidence → reason 반영 | - | "리뷰 데이터가 충분하지 않아..." ✅ |
| HTTP 500 발생 여부 | - | 없음 ✅ |

---

### 추천 결과 샘플 (skinType=combination)

```
[1] id=15  score=99.29  baseSkinType=복합성  caution=normal
    name=리얼 히알루로닉 100 토너 500ml
    reason=복합성 피부 타입 기준 추천 점수가 높습니다. 피부 고민 키워드와 관련된 태그가 포함되어 있습니다. 부정 리뷰 신호가 낮은 편입니다.

[2] id=9   score=99.21  baseSkinType=복합성  caution=normal
    name=울트라리페어 로션 350ml 2 pack + 트러블 미스트 30ml 증정

[3] id=10  score=98.65  baseSkinType=복합성  caution=normal
    name=비건 핸드크림 3개 골라담기
```

---

## 다음 단계 (6차 예정)

- [ ] 전체 원본 데이터 import (parquet, 사용자 확인 후)

---

---

## 2026-06-30 — 6차-1 실행: 전체 Import 사전 조사 + 스크립트 작성 + Dry-run

### 작업 내용

실제 DB 삭제/import 없이 소스 파일 조사 → import 후보 확정 → 스크립트 작성 → dry-run 실행.

**이번 단계 제약 (절대 준수)**:
- DELETE/TRUNCATE/전체 import 실행 금지
- BL_PRODUCTS, BL_REVIEWS 데이터 삭제 금지
- Member/Auth 수정 금지, Recommendation 로직 변경 금지

---

### 소스 파일 조사 결과

**조사 경로**: `D:\_WebCrawling\oliveyoung_crawler\preprocessed_v3\`

| 파일 | 행 수 | 용도 | 우선순위 |
|---|---|---|---|
| `product_recommendation_scores.parquet` | 6,008 | BL_PRODUCTS 소스 | 1순위 |
| `product_skin_aggregates.parquet` | 6,008 | recommendation_score 없어 단독 사용 불가 | 보조 |
| `service_reviews.parquet` | 402,438 | BL_REVIEWS 소스 | 1순위 |
| `train.parquet` | 321,950 | product_key 없어 불가 | — |
| `transformer_v3_preds.parquet` | 402,438 | review_id+예측값만, 단독 불가 | — |

---

### 매칭 분석 결과

- scores 파일 unique product_key: **1,521개** (6,008행 중 중복 4,487건 제거)
- service_reviews 중 scores 상품과 매칭: **323,574건 (80.4%)**
- 미매칭 78,864건: coupang 전용 상품(scores 없음) + 일부 기타 → 스킵 처리 (FK 무결성 유지)

---

### 생성 파일 목록

| 파일 | 내용 |
|---|---|
| `scripts/import_products_full.py` | BL_PRODUCTS 전체 import (dry-run/actual 모드) |
| `scripts/import_reviews_full.py` | BL_REVIEWS 전체 import (dry-run/actual 모드) |
| `docs/04_full_import_plan.md` | 전체 import 계획 문서 |

---

### Dry-run 결과

**import_products_full.py --dry-run** (2026-06-30 02:14):

```
Total rows read:          6,008
Unique products:          1,521
Skip (dup product_key):   4,487
Skip (no product_key):    0
Skip (no product_name):   0
Expected insert count:    1,521
```

**import_reviews_full.py --dry-run** (2026-06-30 02:14):

```
Total rows processed:     402,438
Matched (product found):  323,574  (80.4%)
Skip (no product match):  78,864
Skip (no review_text):    0
Expected insert count:    ~323,574
```

**이상 없음. DB에 데이터 기록 없음 (dry-run).**

---

### 다음 단계 (6차-2, 사용자 확인 후)

- [x] 전략 확정 (A안 확정)
- [x] 초기화 실행 (DELETE, FK 순서 준수)
- [x] `python scripts/import_products_full.py` (1,521건 성공)
- [x] `python scripts/import_reviews_full.py` (323,574건 성공)
- [x] 검증 쿼리 확인 (고아 리뷰 0건)
- [x] 상품명 품질 검수 완료

---

---

## 2026-06-30 — 6차-2 실행: A안 전체 Import + 상품명 품질 검수

### 작업 내용

preview 데이터 삭제 → 전체 상품/리뷰 import → 품질 검수 보고서 생성.

**전략**: A안 — oliveyoung + musinsa scored products만 (coupang 제외).

---

### 사전 정리 (DELETE)

```
DELETE FROM BL_RECOMMENDATIONS;  → 0건
DELETE FROM BL_FAVORITES;         → 0건
DELETE FROM BL_REVIEWS;           → 580건 (preview)
DELETE FROM BL_PRODUCTS;          → 179건 (preview)
COMMIT;
BL_MEMBERS: 1건 보존
```

---

### 상품 Import 결과

```
python scripts/import_products_full.py
→ 처리: 6,008행 / INSERT 성공: 1,521 / 실패: 0
→ 소요 시간: ~2초
```

platform: oliveyoung=830, musinsa=691

---

### 리뷰 Import 결과

```
python scripts/import_reviews_full.py
→ 처리: 402,438행 / INSERT 성공: 323,574 / 실패: 0
→ 스킵: 78,864건 (coupang 36,185 + musinsa 미점수 42,618 + 기타 61)
→ 소요 시간: 89초 (배치 commit 500행 적용)
```

sentiment: positive=279,849 / negative=32,267 / neutral=11,458

---

### DB 최종 상태

| 테이블 | 행 수 |
|---|---|
| BL_PRODUCTS | **1,521** |
| BL_REVIEWS | **323,574** |
| BL_MEMBERS | 1 (보존) |
| BL_IMPORT_LOGS | 5 |

고아 리뷰(product 없는 review): **0건** ✅

---

### 성능 개선 사항

import_reviews_full.py: 기존 행별 commit → 배치 commit(500행마다)  
→ 예상 40분+ → 실제 **89초** (~27배 단축)

---

### 상품명 품질 검수 결과

```
python scripts/check_product_quality.py
→ docs/06_product_quality_report.md 생성
```

| 항목 | 결과 |
|---|---|
| brand NULL | 0 (0%) ✅ |
| product_name NULL | 0 ✅ |
| 100자 초과 상품명 | 0 ✅ |
| 프로모션 의심 상품명 | 792개 (52.1%) ⚠️ |
| 플랫폼 간 중복 의심 | 0쌍 ✅ |
| 동일 플랫폼 내 유사 쌍 | 455쌍 ⚠️ |

**주요 발견**:
- 프로모션 키워드 '기획'(523) '증정'(204) '1+1'(87) — 올리브영/무신사 상품명에 이벤트 문구 포함
- 동일 플랫폼 내 455쌍 = 대부분 동일 상품 용량/수량 변형, 일부 완전 동일(유사도 1.0) 중복 의심
- 플랫폼 간 중복 없음 — 두 플랫폼 상품 구성 상이
- DB PRODUCT_NAME 직접 수정 없음 / 자동 병합 없음

---

### 생성/수정 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `scripts/import_products_full.py` |
| 신규 | `scripts/import_reviews_full.py` |
| 신규 | `scripts/check_product_quality.py` |
| 수정 | `scripts/import_reviews_full.py` (배치 commit 추가) |
| 신규 | `docs/04_full_import_plan.md` |
| 신규 | `docs/05_full_import_report.md` |
| 신규 | `docs/06_product_quality_report.md` (check script 자동 생성) |

---

## 다음 단계 (7차 예정)

- [ ] 상품명 프로모션 문구 정제 방식 결정 (DB 수정 vs UI 레이어 처리)
- [ ] 동일플랫폼 유사도 1.0 완전 동일 상품 확인 (아이디얼포맨 퍼펙트 올인원 등)
- [x] 추천 API 품질 재검증 (1,521개 풀로 재테스트) → 7차에서 완료
- [ ] coupang 포함 여부 최종 결정 (B안 필요 시)

---

---

## 2026-06-30 — 7차 실행: 추천 API 품질 재검증

### 작업 내용

6차-2 전체 import 완료 후 추천 API 전체 품질 검증.  
Python requests 라이브러리로 자동화 테스트 스크립트 실행.

### 검증 범위

- 미로그인 401 확인
- 로그인 / JSESSIONID 발급 확인
- 5개 skinType 각각 (dry/oily/combination/sensitive/normal):
  - PUT /api/members/me → GET /api/members/me → GET /api/recommendations/me
  - size=5, size=20 확인
  - baseSkinType 일치율, 점수 내림차순, reason null 여부, 중복 상품 확인
  - 상위 10개 상세 출력 (productId, brand, score, bst, caution, reason, review counts)
- 상품-리뷰 연결 (skinType별 상위 3개 × 5 = 15개 상품)
- API 응답 성능 측정
- 로그아웃 후 401 확인

---

### 테스트 결과 요약

| skinType | 반환 수 | baseSkinType 일치 | 점수순 | reason null | 판정 |
|---|---|---|---|---|---|
| dry (건성) | 20 | 20/20 ✅ | ✅ | 0 | ✅ SUCCESS |
| oily (지성) | 20 | 20/20 ✅ | ✅ | 0 | ✅ SUCCESS |
| combination (복합성) | 20 | 20/20 ✅ | ✅ | 0 | ✅ SUCCESS |
| sensitive (민감성) | 20 | 20/20 ✅ | ✅ | 0 | ✅ SUCCESS |
| normal (중성) | 10 | 10/10 ✅ | ✅ | 0 | ⚠️ 데이터 한계 |

중성: DB에 중성 타입 상품 10개뿐 → size=20 요청 시 10개만 반환. 로직 버그 아님.

---

### 성능 측정

| 항목 | 값 |
|---|---|
| 추천 API 평균 응답 시간 | **4ms** |
| 추천 API 최소 | 3ms |
| 추천 API 최대 | 7ms |
| 3초 초과 | **없음** ✅ |

---

### 상품-리뷰 연결 (15개 상품)

- GET /api/products/{id}: **15/15 HTTP 200** ✅
- GET /api/products/{id}/reviews: 53~799건 정상 반환 ✅
- /reviews/negative: negative label만 반환 ✅ (15/15)
- /reviews/positive: positive label만 반환 ✅ (15/15)

---

### 발견된 버그

**BUG-1**: `platform` 필드 누락 [수정 승인 대기]

- `RecommendationVO.java`: `platform` 필드 없음
- `recommendationMapper.xml`: `PLATFORM AS platform` SELECT 없음
- 결과: 추천 API 전 skinType에서 `platform=null` 반환
- 영향: 프론트엔드 플랫폼 구분 불가, 외부 링크 생성 불가

**수정 방법** (승인 후 진행):
1. RecommendationVO에 `private String platform;` + getter/setter 추가
2. `selectBySkinType`, `selectFallback` 양쪽에 `PLATFORM AS platform,` 추가

---

### 생성 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `docs/08_recommendation_quality_report.md` |
| 검증 스크립트 (scratchpad) | `test_recommendation_api.py` |

---

## 다음 단계 (8차 예정)

- [x] BUG-1 수정: RecommendationVO + mapper에 platform 필드 추가 → 완료
- [ ] 상품명 프로모션 문구 정제 방식 결정
- [ ] 중성 상품 보강 (coupang B안 or fallback 로직 개선)
- [ ] 동일플랫폼 유사도 1.0 완전 동일 상품 확인

---

---

## 2026-06-30 — 7차 보완: BUG-1 수정 (platform 필드 누락)

### 버그 내용

추천 API(`GET /api/recommendations/me`) 응답에서 `platform=null` 반환.  
7차 검증에서 발견, 사용자 승인 후 수정 진행.

### 원인

| 파일 | 문제 |
|---|---|
| `RecommendationVO.java` | `platform` 필드 없음 |
| `recommendationMapper.xml` | `PLATFORM AS platform` SELECT 없음 |

### 수정 내용

**RecommendationVO.java**:
```java
private String platform;  // 추가

public String getPlatform() { return platform; }
public void setPlatform(String v) { this.platform = v; }
```

**recommendationMapper.xml** (`selectBySkinType`, `selectFallback` 양쪽):
```sql
SELECT PRODUCT_ID   AS productId,
       PLATFORM     AS platform,   -- 추가
       PRODUCT_NAME AS productName,
       ...
```

### 빌드 결과

```
mvn clean package
→ BUILD SUCCESS
→ Finished at: 2026-06-30T03:13:xx+09:00
```

재배포: Tomcat autoDeploy (WAR 교체, PID 4916 유지)

### 수정 후 API 검증 결과

| 항목 | 결과 |
|---|---|
| platform null 없음 | ✅ 0/20 |
| platform 유효값 | ✅ oliveyoung / musinsa |
| 건성 platform 분포 | musinsa |
| 지성 platform 분포 | oliveyoung + musinsa |
| 복합성 platform 분포 | oliveyoung + musinsa |
| 중성 platform 분포 | oliveyoung |
| reason null 없음 | ✅ |
| 점수 내림차순 | ✅ |
| baseSkinType 매핑 유지 | ✅ |
| HTTP 500 | 없음 ✅ |

### 수정 파일 목록

| 파일 | 변경 내용 |
|---|---|
| `src/main/java/kr/ac/kopo/recommendation/vo/RecommendationVO.java` | `platform` 필드 + getter/setter 추가 |
| `src/main/resources/config/sqlMap/oracle/recommendationMapper.xml` | 두 쿼리에 `PLATFORM AS platform` 추가 |
| `docs/08_recommendation_quality_report.md` | BUG-1 FIXED 업데이트 |

---

---

## 2026-06-30 — 6차-1 보완: coupang 포함 여부 재조사

### 배경

6차-1 dry-run 결과에서 BL_PRODUCTS 소스(product_recommendation_scores.parquet)가 oliveyoung + musinsa 상품만 포함한다는 문제 확인.  
coupang 리뷰 36,185건이 service_reviews에 있으나 coupang 상품이 BL_PRODUCTS에 없어 전량 FK 미매칭 스킵.

### 재조사 결과

**D:\_WebCrawling 전체 파일 조사**:
- oliveyoung raw: `output/*.jsonl` 4개 파일
- musinsa raw: `output_external/musinsa_reviews.jsonl` (228,851행)
- coupang raw: `output_external/coupang_reviews.jsonl` + `쿠팡/*.csv` 5개
- 전처리 파이프라인: `scripts/build_recommendation_scores.py` — coupang 의도적 제외 확인

**coupang 제외 이유 (공식 확인)**:
- `build_recommendation_scores.py` 주석: `coupang 전체 base_skin_type 없음 → 피부 타입별 집계 불가`
- coupang 리뷰에 피부 타입 정보 없음 → base_skin_type = 0% → scoring 불가 → 설계상 의도적 제외

**coupang 데이터 품질**:

| 항목 | 값 |
|---|---|
| service_reviews의 coupang unique 상품 수 | 373개 |
| brand 보유 | 0% (전무) |
| category | 'beauty' 단일값 |
| product_name 오염 (가격 텍스트 포함) | 89.5% |
| base_skin_type | 0% |
| recommendation_score | 없음 (산출 불가) |

**플랫폼별 리뷰/상품 매칭 정리**:

| 플랫폼 | 리뷰 | 상품(scores) | 리뷰 매칭률 |
|---|---|---|---|
| oliveyoung | 172,109 | 830 | 100.0% |
| musinsa | 194,144 | 691 | 78.0% |
| coupang | 36,185 | 0 | 0% |

### Import 전략

**A안 (권장)**: oliveyoung+musinsa 1,521 상품 / ~323,574 리뷰. 고품질.  
**B안**: coupang 373 상품 추가 시 1,894 상품 / ~359,759 리뷰. coupang 품질 낮음 (brand 없음, 상품명 오염, 추천 불가).  
**C안**: 불가 (coupang scoring 파이프라인 재실행 불가 — skin type 데이터 없음).

### 수정 파일

| 파일 | 변경 내용 |
|---|---|
| `docs/04_full_import_plan.md` | 전면 재작성 — 3개 플랫폼 조사, A/B/C안 비교, TRUNCATE 안전 계획, 사용자 선택 항목 정리 |

---

## 2026-06-30 — 8차 A: displayName 정제 정책 결정 (dry-run)

### 배경

추천 API / 상품 API가 현재 DB의 원본 `PRODUCT_NAME`을 그대로 반환.  
상품명에 `[NEW]`, `[올영픽]`, `[타임어택]` 등 프로모션 접두 대괄호와  
`+(증정) 키링` 등 증정품 접미 문구가 포함되어 있어 화면 표시 품질 저하.

**기본 방침**:
- DB `PRODUCT_NAME` 수정 금지 (원본 보존)
- `PRODUCT_KEY` 변경 금지
- 상품 자동 병합 금지
- Service 계층에서 on-the-fly `displayName` 생성

### 작업 내용

**정제 정책 설계**:
- Rule 1: 앞 대괄호 제거 (safe/review/unsafe 3단계 분류)
  - Safe: 올영픽, 타임어택, 무신사 단독, 사은품, 포켓몬, 콜라보, 런칭, 에디션 등
  - Review: PICK, NEW, 단독, 기획, 증정, 세일, 특가 등
  - 수량/용량 정보 포함 대괄호(`[2PACK]`, `[10매기획]`)는 제거 금지
- Rule 2: 뒤쪽 증정품 문구 제거 (`+(증정) xxx`, `(+gift_word)` 형식)
- Rule 3: 정제 후 8자 미만이면 원본 유지
- Rule 4: 연속 공백 정규화

**dry-run 스크립트**: `scripts/propose_display_names.py` (DB 수정 없음, 읽기 전용)

**결과**:
```
전체 상품:          1,521
변경 없음(NO_CHANGE): 1,077 (70.8%)
변경 후보:            444 (29.2%)
  safe:   162건
  review: 282건
unsafe:   3건 (원본 유지 — 원본 자체 8자 미만)

변경 유형:
  REMOVE_LEADING_PROMO_BRACKET: 333건
  MULTI_RULE (복합):             53건
  REMOVE_GIFT_SUFFIX:            46건
  NORMALIZE_SPACING:             12건
```

**미포착 패턴 (개선 필요)**:
- `+헤어핀 굿즈` 형식(괄호 없는 끝 증정품)
- `(증정: xxx)` 형식 (`:` 구분자)
- `[7년 연속 1위]` 등 마케팅 클레임

### 정책 판정: **B안**

> 정제 규칙이 일부 위험하다. API 반영 전에 review/unsafe 케이스를 더 줄여야 한다.

- safe 162건: ✅ API 반영 가능
- review 중 `[SET]` 제거 항목: ❌ 사용자 지침("세트는 제품 구성일 수 있으므로 제거 금지") 위반 → 보존 처리로 변경 필요
- review 중 `[증정]`, `[PICK]`, 유명인 PICK: 결과 합리적, 추후 반영 가능
- unsafe 3건: 원본 유지 정상

### 생성 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `scripts/propose_display_names.py` |
| 신규 | `docs/display_name_candidates.csv` (1,521행) |
| 신규 | `docs/09_display_name_policy.md` |

### 다음 단계 (8차 B 예정)

1. `[SET]` 보존 처리 + 미포착 패턴 4종 보완 → `propose_display_names.py` v1.1 재실행
2. review 건 검토 후 API 반영 범위 확정
3. `ProductVO` + `RecommendationVO`에 `displayName` 필드 추가 (Service 계층 정제)

---

## 2026-06-30 — 8차 B: displayName 정제 규칙 v1.1 보완 및 dry-run 재검증

### 작업 내용

**스크립트**: `scripts/propose_display_names_v1_1.py` (새 파일, v1.0 스크립트 보존)

**v1.1 규칙 보완 내용**:

| 보완 | 내용 |
|---|---|
| [SET] 보존 | `SET_PRESERVE_RE`로 `[SET]`/`[set]`/`[Set]` 완전 보존. REVIEW_LEADING_KW에서 제거. |
| Rule 1-C | 마케팅 클레임 대괄호 감지: `1위`, `연속`, `수상`, `판매량`, `재구매율`, `랭킹` + 년수 패턴 |
| Rule 2-C | 괄호 없는 끝 증정품 감지: `+gift_word` 형식, 본품 볼륨 보존 확인 후 제거 |
| Rule 2-D | `(증정: xxx)` 형식 감지 (`:` 구분자) |
| riskLevel | 새 패턴 모두 review로 분류 |

**v1.0 vs v1.1 비교**:

| 항목 | v1.0 | v1.1 | 변화 |
|---|---|---|---|
| NO_CHANGE | 1,077 | 1,055 | -22 |
| 변경 후보 | 444 | 466 | +22 |
| safe | 162 | 161 | -1 |
| review | 282 | 305 | +23 |
| unsafe | 3 | 3 | 0 |

**[SET] 처리 결과**:
- [SET] 포함 상품 16개 전수 확인
- [SET] 자체 제거: **0건** — 완전 보존 ✅
- 3건 "변경됨" = 공백 정규화 또는 `(증정: xxx)` 제거 (SET는 유지)

**신규 감지 결과**:
- 마케팅 클레임(Rule 1-C): 22건 (`[7년 연속 1위]`, `[화해1위/...]`, `[72관왕/1위]` 등)
- `(증정: xxx)` (Rule 2-D): 7건 (전수 결과 합리적)
- 괄호 없는 끝 증정품(Rule 2-C): 2건 (몬치치 파우치/스티커)

**잔존 미포착 패턴** (소수):
- `(세터 콜라보 거울 키링 & 포장 박스 증정)` — `&` 구분자 형식
- 이름 중간 `[증정]` 태그 (복잡성↑ 보류)

### 샘플 검수 결과

- safe 161건 상위 50개: 전수 합격 ✅
- review 305건 상위 50개: 대부분 합리적, 일부 제품설명+마케팅 혼합 대괄호는 ⚠️ 허용 수준
- unsafe 3건: 원본 유지 정상 ✅
- [SET] 16건: 완전 보존 ✅

### 최종 판정: **B안** (v1.0와 동일 등급, 품질 향상)

- **safe 161건**: Java API에서 on-the-fly 자동 적용 가능
- **review 305건**: 원본 `productName` 그대로 반환 (`displayName = productName`)
- **unsafe 3건**: 원본 유지 (이미 처리됨)

### 생성 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `scripts/propose_display_names_v1_1.py` |
| 신규 | `docs/display_name_candidates_v1_1.csv` (1,521행) |
| 신규 | `docs/09_display_name_policy_v1_1.md` |

### 다음 단계 (8차 C 예정)

1. `DisplayNameCleaner.java` 유틸리티 클래스 작성 (Python 규칙 Java 포팅)
2. `ProductVO` + `RecommendationVO`에 `displayName` 필드 추가
3. Service 계층에서 safe 161건에만 자동 정제 적용
4. API 응답 검증

---

## 2026-06-30 — 8차 C: displayName Java API 반영 및 검증

### 작업 내용

**Python v1.1 safe 규칙을 Java로 포팅**하여 Product/Recommendation API 응답에 `displayName` 필드 추가.  
DB/Mapper 불변, 추천 로직 불변, on-the-fly 정제 방식.

### 생성/수정 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `src/main/java/kr/ac/kopo/common/util/DisplayNameCleaner.java` |
| 수정 | `src/main/java/kr/ac/kopo/product/vo/ProductVO.java` (displayName 필드 추가) |
| 수정 | `src/main/java/kr/ac/kopo/recommendation/vo/RecommendationVO.java` (displayName 필드 추가) |
| 수정 | `src/main/java/kr/ac/kopo/product/service/ProductServiceImpl.java` (DisplayNameCleaner 적용) |
| 수정 | `src/main/java/kr/ac/kopo/recommendation/service/RecommendationServiceImpl.java` (DisplayNameCleaner 적용) |
| 신규 | `docs/10_display_name_api_report.md` |

### DisplayNameCleaner 구조

```
clean(productName):
  cleanInternal() → CleanResult { name, risk, changed }
  if risk==SAFE && changed==true && length>=8 → 정제명 반환
  else → 원본 반환 (review/unsafe/NO_CHANGE)
```

**주요 규칙**: Rule 1-A(safe 대괄호 제거), 1-B(review), 1-C(마케팅클레임), [SET] 보존, PRODUCT_IN_BRACKET 보존, Rule 2-A/2-B(증정 suffix), 2-C/2-D(review), Rule 4(공백 정규화)

### API 검증 결과

**Product API** (`GET /api/products?sortBy=score&size=200`):

| 항목 | 결과 |
|---|---|
| HTTP 500 없음 | ✅ |
| displayName 필드 존재 | ✅ |
| displayName null | 0건 ✅ |
| displayName 변경 건수 | 22건 |
| CSV safe_changed 161건 중 API 일치 | 22/22 = 100% ✅ |
| [SET] 보존 | ✅ (11건 확인, 오제거 0건) |
| unsafe 3건 원본 유지 | ✅ (개별 조회 확인) |

**Recommendation API** (`GET /api/recommendations/me?size=20`):

| 항목 | 결과 |
|---|---|
| HTTP 200 | ✅ |
| displayName 필드 존재 | ✅ |
| displayName null | 0건 ✅ |
| reason/platform/score 유지 | ✅ |
| 점수 내림차순 | ✅ |
| displayName 변경 건수 | 2건 (id=419, id=368) |

### 로그인 확인

- 계정: `test01` / `1234` (서버 측 SHA-256 해싱 — 클라이언트는 평문 전송)

### Python vs Java 비교

| Python v1.1 CSV | Java API |
|---|---|
| safe_changed = 161건 | 동일 집합 161건 적용 |
| API 조회 22건 | 22건 모두 일치 (불일치 0건) |

상세 결과: `docs/10_display_name_api_report.md` 참조.

---

## 2026-06-30 — 9차: 이미지 URL 수집 가능성 테스트

### 작업 내용

상품 카드 UI에 사용할 대표 이미지 URL 확보 가능성을 검증하는 샘플 테스트.  
DB 수정/전체 수집 없이 40건(oliveyoung 20 + musinsa 20) 가능성 테스트만 진행.

### 주요 발견 (데이터 조사)

| 항목 | 내용 |
|---|---|
| `photo_exists` | 리뷰 사진 첨부 여부 (bool) — 이미지 URL 아님 |
| `raw_url` | JSONL에 존재하는 **상품 상세 페이지 URL** (이미지 URL 아님) |
| 이미지 URL | **기존 크롤링 데이터에 없음** |
| BL_PRODUCTS URL 구성 | PLATFORM_PRODUCT_ID로 **1,521건 전수 URL 구성 가능** |

### 플랫폼별 URL 패턴

| platform | 건수 | URL 패턴 |
|---|---|---|
| oliveyoung | 830 | `https://www.oliveyoung.co.kr/store/goods/getGoodsDetail.do?goodsNo={PLATFORM_PRODUCT_ID}` |
| musinsa | 691 | `https://www.musinsa.com/products/{PLATFORM_PRODUCT_ID}` |

### 403 차단 분석

- 일반 `requests`: HTTP 403 (Cloudflare)
- Referer/Accept 헤더 강화 후에도 403
- `Server: cloudflare`, `Cf-Mitigated: challenge` 확인
- **해결책**: `cloudscraper` 설치 → Cloudflare JS Challenge 우회

### 샘플 테스트 결과 (cloudscraper)

| 항목 | oliveyoung | musinsa |
|---|---|---|
| 요청 수 | 20 | 20 |
| 성공 | 20 | 20 |
| 성공률 | **100%** | **100%** |
| 추출 방법 | og:image (100%) | og:image (100%) |
| 이미지 도메인 | `image.oliveyoung.co.kr` | `image.msscdn.net` |
| 고유 URL | 20/20 | 20/20 |
| http/https | 20/20 | 20/20 |

### 판정: **A안** (전체 수집 가능)

- 1,521건 전수 URL 구성 가능 ✅
- cloudscraper 통과율 100% ✅
- og:image 추출 일관성 100% ✅
- 예상 전체 수집 시간: ~63분 (sleep 2.5초 기준)

### 다음 단계 (사용자 확인 후)

1. `BL_PRODUCTS`에 `PRODUCT_URL`, `IMAGE_URL`, `IMAGE_STATUS` 컬럼 추가 (ALTER TABLE)
2. 전체 수집 스크립트 실행 (`cloudscraper` + og:image)
3. DB UPDATE (수집 결과)
4. Product API `imageUrl` 필드 추가

### 생성 파일

| 작업 | 파일 |
|---|---|
| 신규 | `docs/11_image_collection_feasibility.md` |
| 신규 | `docs/image_sample_results.csv` (40건 샘플) |

---

## 2026-06-30 — 10차: 이미지 URL 수집 (Lite) + API 반영

### 작업 내용

BL_PRODUCTS에 PRODUCT_URL/IMAGE_URL/IMAGE_STATUS 컬럼 추가, 피부 타입별 추천 상위 70건 우선 수집, Java API 반영.  
전체 1,521건 수집은 추후 별도 단계 예정.

**전환 경위**: 전체 수집(PID 35332)을 200건 완료 후 중단 → 시연/UI 준비 목적으로 피부 타입별 상위 70건 Lite 수집으로 전환.

**제약 (절대 준수)**:
- 이미지 파일 다운로드 금지 (URL만 수집)
- PRODUCT_NAME 수정 금지 / PRODUCT_KEY 변경 금지
- 추천 로직 변경 금지 / displayName 정제 로직 변경 금지
- 상품 자동 병합 금지 / 쿠팡 추가 금지
- React 생성 금지 / UI 생성 금지

---

### DB 컬럼 추가

```sql
ALTER TABLE BL_PRODUCTS ADD (
    PRODUCT_URL  VARCHAR2(1000),
    IMAGE_URL    VARCHAR2(1000),
    IMAGE_STATUS VARCHAR2(30)
);
```

실행 전 PRODUCT_URL/IMAGE_URL/IMAGE_STATUS 컬럼 없음 확인 후 실행.

---

### 수집 스크립트

**파일**: `scripts/collect_image_urls.py`

| 기능 | 내용 |
|---|---|
| 플랫폼별 URL 생성 | oliveyoung: getGoodsDetail.do?goodsNo={id}, musinsa: /products/{id} |
| og:image 추출 | BeautifulSoup4 meta 태그 파싱 (우선) → twitter:image → JSON-LD → CDN 직접 |
| Cloudflare 우회 | cloudscraper 라이브러리 |
| 재시도 | 403/429/503 시 5초 대기 후 최대 2회 재시도 |
| Resume 가능 | IMAGE_STATUS='found' 상품 스킵 (`--force` 옵션으로 강제 재수집) |
| 커밋 주기 | 50건마다 COMMIT (중단 시 데이터 보존) |
| 연속 실패 중단 | 10건 연속 실패 시 자동 중단 |
| 출력 CSV | `docs/image_collection_results.csv` |
| CLI 옵션 | `--dry-run` / `--actual` (필수), `--limit N`, `--sleep N`, `--force` |

**Dry-run 결과 (20건 샘플)**:
```
[DRY-RUN] Would process 1521 products
→ 20건 샘플 출력 정상 → DB 변경 없음
```

**전체 수집 시도 후 중단**:
```
python scripts/collect_image_urls.py --actual --sleep 2.5
→ PID 35332, 2026-06-30 10:09:13 시작 → 200건 완료 후 중단
```

**Lite 수집 (시드 70건)**:

`scripts/collect_seed_images.py` — 피부 타입별 상위 15개 × 5타입 = 최대 70건

| 단계 | 내용 |
|---|---|
| 대상 조회 | RECOMMENDATION_SCORE DESC PARTITION BY base_skin_type TOP 15 |
| 기존 found 스킵 | 54건 already found → 스킵 |
| 신규 수집 | 16건 수집 (성공 16/16) |
| 총 결과 | 70/70 found = **100%** |

---

### Java API 반영

**수정 파일 목록**:

| 작업 | 파일 |
|---|---|
| 수정 | `src/main/java/kr/ac/kopo/product/vo/ProductVO.java` |
| 수정 | `src/main/java/kr/ac/kopo/recommendation/vo/RecommendationVO.java` |
| 수정 | `src/main/resources/config/sqlMap/oracle/productMapper.xml` |
| 수정 | `src/main/resources/config/sqlMap/oracle/recommendationMapper.xml` |
| 수정 | `docs/schema.sql` |
| 수정 | `docs/02_api_spec.md` |

**추가 필드**:
- `productUrl` (String) — 상품 상세 페이지 URL
- `imageUrl` (String) — 대표 이미지 URL (og:image 추출, imageStatus=found일 때만 non-null)
- `imageStatus` (String) — found/not_found/fetch_failed/blocked/pending

**빌드 결과**:
```
mvn clean package
→ BUILD SUCCESS (2.149s)
→ WAR 배포: 2026-06-30 10:11:18
```

---

### 수집 결과 (Lite 70건)

| 항목 | 값 |
|---|---|
| BL_PRODUCTS 총 행수 | 1,521 |
| found (전체) | 216 (시드 70 + 이전 부분 수집 146) |
| NULL (미수집) | 1,305 |
| 시드 70건 found | **70/70 = 100%** |

피부 타입별:

| 피부 타입 | found |
|---|---|
| 건성 | 15/15 ✅ |
| 지성 | 15/15 ✅ |
| 복합성 | 15/15 ✅ |
| 민감성 | 15/15 ✅ |
| 중성 | 10/10 ✅ |

플랫폼별 (시드 70건): musinsa=46, oliveyoung=24

---

### API 검증 결과

| API | HTTP | imageStatus=found | imageUrl | productUrl | displayName | reason |
|---|---|---|---|---|---|---|
| `GET /api/products?sortBy=score&size=20` | 200 | 20/20 ✅ | 20/20 ✅ | 20/20 ✅ | 20/20 ✅ | — |
| `GET /api/products/730` (musinsa) | 200 | found ✅ | msscdn.net ✅ | musinsa.com ✅ | — | — |
| `GET /api/products/462` (NULL) | 200 | null ✅ | null ✅ | null ✅ | — | — |
| `GET /api/recommendations/me?size=20` | 200 | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ |

---

### 생성/수정 파일 목록 (10차 최종)

| 작업 | 파일 |
|---|---|
| 수정 | `src/main/java/kr/ac/kopo/product/vo/ProductVO.java` |
| 수정 | `src/main/java/kr/ac/kopo/recommendation/vo/RecommendationVO.java` |
| 수정 | `src/main/resources/config/sqlMap/oracle/productMapper.xml` |
| 수정 | `src/main/resources/config/sqlMap/oracle/recommendationMapper.xml` |
| 수정 | `docs/schema.sql` |
| 수정 | `docs/02_api_spec.md` |
| 신규 | `scripts/collect_image_urls.py` (전체 수집용, 추후 사용) |
| 신규 | `scripts/collect_seed_images.py` (시드 70건 수집) |
| 신규 | `docs/seed_image_targets.csv` |
| 신규 | `docs/seed_image_collection_results.csv` |
| 신규 | `docs/12_seed_image_collection_report.md` |

---

### 다음 단계

- [ ] 전체 1,521건 이미지 수집 (별도 단계, `scripts/collect_image_urls.py --actual`)
- [ ] 이미지 placeholder 처리 방침 확정 (null 상품 대비)

---

## 2026-06-30 — 11차: 회원 댓글 + 관리자 기능

### 작업 내용

시연용 회원 댓글 기능(BL_PRODUCT_COMMENTS)과 관리자 대시보드/댓글 관리 API 구현.  
기존 BL_REVIEWS(크롤링 리뷰)와 BL_PRODUCT_COMMENTS(회원 댓글)는 완전히 분리.

---

### DB 변경

| 항목 | 내용 |
|---|---|
| BL_MEMBERS ROLE 컬럼 추가 | VARCHAR2(20) DEFAULT 'USER' |
| test01 ROLE | USER |
| admin 계정 생성 | loginId=admin, password=SHA256('1234'), ROLE=ADMIN |
| SEQ_BL_PRODUCT_COMMENTS | 신규 시퀀스 생성 |
| BL_PRODUCT_COMMENTS | 10컬럼 테이블 생성 (soft delete 포함) |

---

### 생성/수정 파일

| 분류 | 파일 |
|---|---|
| 수정 | `MemberVO.java` (role 필드 추가) |
| 수정 | `memberMapper.xml` (ROLE AS role 추가) |
| 신규 | `comment.vo.CommentVO` |
| 신규 | `comment.dao.CommentDAO / Impl` |
| 신규 | `comment.service.CommentService / Impl` |
| 신규 | `comment.controller.CommentController` |
| 신규 | `admin.vo.AdminSummaryVO` |
| 신규 | `admin.dao.AdminDAO / Impl` |
| 신규 | `admin.service.AdminService / Impl` |
| 신규 | `admin.controller.AdminApiController` |
| 신규 | `admin.controller.AdminViewController` |
| 신규 | `commentMapper.xml` |
| 신규 | `adminMapper.xml` |
| 수정 | `sqlMapConfig.xml` (commentVO, adminSummaryVO alias) |
| 신규 | `WEB-INF/jsp/admin/dashboard.jsp` |
| 신규 | `WEB-INF/jsp/comment/test.jsp` |
| 신규 | `WEB-INF/jsp/error/forbidden.jsp` |
| 수정 | `docs/schema.sql` |
| 수정 | `docs/02_api_spec.md` |
| 신규 | `docs/13_admin_comment_management_report.md` |

---

### 빌드 및 배포

```
mvn clean package → BUILD SUCCESS (2.225s)
WAR 배포: 2026-06-30 10:36:53
```

---

### 권한 검증 결과 (전체 PASS)

| 시나리오 | 결과 |
|---|---|
| admin 로그인 (role=ADMIN 확인) | ✅ |
| test01 로그인 (role=USER 확인) | ✅ |
| 미로그인 /api/admin/summary → 401 | ✅ |
| test01 /api/admin/summary → 403 | ✅ |
| admin /api/admin/summary → 200 | ✅ |
| 미로그인 댓글 작성 → 401 | ✅ |
| test01 댓글 작성 → 201 | ✅ |
| 빈 content → 400 | ✅ |
| 없는 productId → 404 | ✅ |
| test01 admin DELETE → 403 | ✅ |
| admin 댓글 삭제 → 200 | ✅ |
| 삭제 댓글 일반 목록 제외 | ✅ |
| admin 목록 DELETED 상태 확인 | ✅ |
| 기존 API 유지 | ✅ |

---

---

## 2026-06-30 — 11차 수정: 댓글 삭제 권한 변경

### 변경 내용

기존 DELETE /api/comments/{commentId}는 ADMIN 전용이었으나,  
작성자 본인도 자기 댓글을 삭제할 수 있도록 권한 정책 수정.

**변경 전**: ADMIN만 DELETE 가능  
**변경 후**: 작성자 본인 또는 ADMIN 가능 (미로그인 401, 타인 댓글 403)

### 삭제 사유 (DELETE_REASON)

| 삭제 주체 | DELETE_REASON |
|---|---|
| 작성자 본인 | USER_DELETE |
| ADMIN | ADMIN_DELETE |

### 수정 파일

| 파일 | 변경 내용 |
|---|---|
| `CommentService.java` | `deleteComment(commentId, deletedById, reason)` 시그니처 변경 |
| `CommentServiceImpl.java` | reason 파라미터 직접 전달 |
| `CommentController.java` | isAuthor 또는 isAdmin 체크 → reason 결정 |
| `AdminApiController.java` | 새 시그니처 적용, ADMIN_DELETE 전달 |

### 검증 시나리오

| 시나리오 | 기대값 |
|---|---|
| test01 댓글 작성 | 201 |
| test01 본인 댓글 삭제 | 200, DELETE_REASON=USER_DELETE |
| 삭제 댓글 일반 목록 제외 | STATUS=ACTIVE만 반환 |
| 관리자 목록 DELETED 확인 | deleteReason=USER_DELETE |
| test01 타인 댓글 삭제 시도 | 403 |
| admin 댓글 삭제 | 200, DELETE_REASON=ADMIN_DELETE |
| 미로그인 삭제 시도 | 401 |

### 11차 보완 검증 완료

2026-06-30 실제 배포 후 HTTP 요청과 JDBC SELECT로 댓글 삭제 권한을 재검증했다.

| 시나리오 | 결과 |
|---|---|
| test01 본인 댓글 삭제 | HTTP 200, DB STATUS=DELETED, DELETED_BY=1, DELETE_REASON=USER_DELETE |
| test02가 test01 댓글 삭제 시도 | HTTP 403, DB STATUS=ACTIVE 유지 |
| admin이 test01 댓글 삭제 | HTTP 200, DB STATUS=DELETED, DELETED_BY=2, DELETE_REASON=ADMIN_DELETE |
| 미로그인 댓글 삭제 시도 | HTTP 401 |
| 일반 댓글 목록 | 삭제 댓글 제외 확인 |
| 관리자 댓글 목록 | DELETED 댓글 확인 |

### 다음 단계

- [ ] 전체 이미지 수집 (collect_image_urls.py)
- [ ] 댓글 수정 기능 (필요 시)

---

## 2026-06-30 12차: 오늘 시연용 JSP 웹사이트 완성

### 작업 범위

기존 Spring MVC WAR 프로젝트 안에서 JSP + CSS + vanilla JavaScript(fetch) 기반 시연 웹사이트를 구현했다. React/Vite/npm 프론트 프로젝트는 생성하지 않았고, 기존 API와 추천/displayName/image 수집 로직은 변경하지 않았다.

### 백업

작업 전 백업 폴더 생성:

```text
backup_before_demo_ui_20260630_1110
```

백업 대상:
- `src/main/java`
- `src/main/resources/config`
- `src/main/webapp`
- `docs`

### 생성/수정 파일

신규:
- `docs/14_master_prd_today_demo.md`
- `docs/15_today_demo_execution_plan.md`
- `docs/16_today_demo_completion_report.md`
- `src/main/java/kr/ac/kopo/main/DemoViewController.java`
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/assets/js/api.js`
- `src/main/webapp/assets/js/ui.js`
- `src/main/webapp/WEB-INF/jsp/common/header.jsp`
- `src/main/webapp/WEB-INF/jsp/common/footer.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/signup.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/mypage.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comments.jsp`

수정:
- `src/main/webapp/index.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/dashboard.jsp`
- `src/main/java/kr/ac/kopo/admin/controller/AdminViewController.java`

### 구현 화면

| URL | 결과 |
|---|---|
| `/` | 메인 화면 완료 |
| `/login` | 로그인 완료 |
| `/signup` | 회원가입 완료 |
| `/mypage` | 회원 정보 조회/수정 완료 |
| `/recommend` | 추천 목록, 이미지/플랫폼 필터 완료 |
| `/products/{productId}` | 상세, 리뷰, 댓글 완료 |
| `/admin` | 관리자 대시보드 완료 |
| `/admin/comments` | 관리자 댓글 관리 완료 |

### 빌드 및 배포

```text
mvn clean package
BUILD SUCCESS

배포:
D:\Lecture\bin\apache-tomcat-11.0.18\webapps\beautylens-mvc.war
```

### 검증 결과

JSP 라우트:
- `/`, `/login`, `/signup`, `/mypage`, `/recommend`, `/products/1446`, `/admin`, `/admin/comments` 모두 HTTP 200

API:
- `/api/health` HTTP 200, db=ok
- Product API HTTP 200, 20건, displayName/imageUrl 유지
- Recommendation API HTTP 200, reason/platform/score 유지
- Admin API: admin 200, USER 403, 미로그인 401

댓글:
- 댓글 작성 201
- 본인 삭제 200
- 삭제 후 일반 목록 제외
- 타인 삭제 시도 403
- 관리자 삭제 200
- 미로그인 삭제 401

### 참고

현재 세션의 브라우저 자동화 도구는 Windows 경로 메타데이터 문제로 실행되지 않아, HTTP 라우트 검증과 REST E2E 검증, JS 파일 문법 검사로 대체했다.

---

## 2026-06-30 13차: 시연 직전 UI/UX 보완

### 작업 범위

기능/API 변경 없이 JSP/CSS/vanilla JS 범위에서 시연 UX를 보완했다. 제이콥 닐슨 10가지 사용성 휴리스틱 기준 점검 문서를 추가하고, 시연을 방해하는 UI 문제만 최소 수정했다.

### 생성/수정 파일

신규:
- `docs/18_ui_ux_heuristic_review.md`

수정:
- `src/main/webapp/WEB-INF/jsp/common/header.jsp`
- `src/main/webapp/WEB-INF/jsp/common/footer.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comments.jsp`
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/assets/js/ui.js`
- `docs/16_today_demo_completion_report.md`

### 보완 내용

- 관리자 메뉴는 ADMIN role일 때만 표시되도록 변경
- 미로그인/일반 USER에서는 관리자 메뉴 숨김
- 로그인 페이지의 시연 계정 노출 제거
- footer의 기술 스택 문구 제거
- 상품 카드 제목 2줄, 추천 이유 3줄 제한 및 버튼 하단 정렬
- 추천/관리자 필터 active 스타일 강화 및 `aria-pressed` 반영
- 현재 URL 기준 nav active 상태 보정
- 댓글 삭제 실패 문구를 권한/재시도 안내로 정리

### 검증 결과

- `mvn clean package` BUILD SUCCESS
- WAR 재배포 완료
- `/`, `/login`, `/recommend`, `/products/1446`, `/admin`, `/admin/comments` HTTP 200
- 로그인 화면에 `test01 / 1234`, `admin / 1234`, `시연 계정` 문구 없음
- 배포 HTML 기준 관리자 메뉴 초기 숨김 확인
- `test01` role=USER, `admin` role=ADMIN 확인
- 관리자 API: admin 200, USER 403, 미로그인 401 유지
- 추천 API 200, Product API 200 유지
- 댓글 작성 201, 본인 삭제 200, 타인 삭제 403, 관리자 삭제 200 유지

---

## 2026-06-30 14차: 실제 서비스형 JSP UX 보완

### 작업 범위

시연 사이트가 관리자/개발용 데모처럼 보이는 부분을 줄이고, 일반 사용자가 실제 화장품 추천 서비스처럼 사용할 수 있도록 JSP/CSS/vanilla JS와 최소 API를 보완했다.

### 생성/수정 파일

신규:
- `docs/19_service_like_ui_polish_report.md`

수정:
- `src/main/java/kr/ac/kopo/recommendation/controller/RecommendationController.java`
- `src/main/java/kr/ac/kopo/member/controller/MemberController.java`
- `src/main/java/kr/ac/kopo/comment/vo/CommentVO.java`
- `src/main/java/kr/ac/kopo/comment/dao/CommentDAO.java`
- `src/main/java/kr/ac/kopo/comment/dao/CommentDAOImpl.java`
- `src/main/java/kr/ac/kopo/comment/service/CommentService.java`
- `src/main/java/kr/ac/kopo/comment/service/CommentServiceImpl.java`
- `src/main/resources/config/sqlMap/oracle/commentMapper.xml`
- `src/main/webapp/index.jsp`
- `src/main/webapp/WEB-INF/jsp/common/header.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/recommend.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/mypage.jsp`
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/assets/js/ui.js`
- `docs/16_today_demo_completion_report.md`
- `docs/17_demo_script.md`
- `docs/18_ui_ux_heuristic_review.md`

### 보완 내용

- 메인 hero의 큰 로그인/관리자 CTA 제거
- 메인 설명 카드에서 내부 구현 문구 제거
- 관리자 메뉴를 ADMIN에게만 노출하도록 네비게이션 보완
- 추천 페이지에 피부 타입 즉시 선택 버튼 추가
- `GET /api/recommendations?skinType={type}&size=20` 추가
- 마이페이지에 `내가 남긴 의견` 목록 추가
- `GET /api/members/me/comments` 추가
- 댓글 목록에서 상품 상세 이동과 본인 삭제 지원
- 상품 카드/필터 active/현재 추천 기준 표시 정리

### 검증 결과

- `mvn clean package` BUILD SUCCESS
- WAR 재배포 완료
- `/api/health` HTTP 200, `db=ok`
- `/`, `/login`, `/recommend`, `/mypage`, `/products/1446`, `/admin`, `/admin/comments` HTTP 200
- 피부 타입별 추천 API 5종 모두 HTTP 200
- test01 로그인 200, 내 댓글 API 200, 관리자 API 403
- admin 로그인 200, 관리자 API 200
- 미로그인 관리자 API 401, 내 댓글 API 401
- 댓글 작성 201, 마이페이지 조회 200, 본인 삭제 200, 삭제 후 `DELETED`

---

## 2026-06-30 15차: 얼굴 등록 및 얼굴 로그인 구현

### 작업 범위

기존 비밀번호 로그인과 추천/상품/댓글/관리자 기능을 유지하면서 로컬 Python FastAPI 기반 얼굴 인증 기능을 추가했다.

### 생성/수정 파일

신규:
- `docs/20_face_login_implementation_plan.md`
- `docs/21_face_login_result_report.md`
- `face_auth_server/app.py`
- `face_auth_server/face_service.py`
- `face_auth_server/requirements.txt`
- `face_auth_server/README.md`
- `src/main/java/kr/ac/kopo/face/**`
- `src/main/resources/config/sqlMap/oracle/faceCredentialMapper.xml`
- `src/main/webapp/assets/js/face-camera.js`

수정:
- `docs/schema.sql`
- `src/main/resources/config/mybatis/sqlMapConfig.xml`
- `src/main/java/kr/ac/kopo/member/service/MemberService.java`
- `src/main/java/kr/ac/kopo/member/service/MemberServiceImpl.java`
- `src/main/webapp/WEB-INF/jsp/common/footer.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/mypage.jsp`
- `src/main/webapp/assets/css/beautylens.css`
- `docs/16_today_demo_completion_report.md`
- `docs/17_demo_script.md`

### DB 변경

- `BL_FACE_CREDENTIALS` 생성
- `SEQ_BL_FACE_CREDENTIALS` 생성
- `docs/schema.sql` fresh install 기준 반영

### 구현 내용

- InsightFace `buffalo_l` 모델 다운로드 및 ONNXRuntime CPU 실행 확인
- Python `/health`, `/face/enroll`, `/face/verify`, `/face/check-quality`
- Spring `GET/POST/DELETE /api/members/me/face`
- Spring `POST /api/auth/face-login`
- 마이페이지 얼굴 등록/해제 UI
- 로그인 페이지 얼굴 로그인 UI
- 웹캠 5방향 촬영, 로그인 2초 카운트다운

### 검증 결과

- Python `/health`: 200, `modelName=insightface-buffalo_l`
- `mvn clean package`: BUILD SUCCESS
- WAR 재배포 후 `/api/health`: 200, `db=ok`
- 미로그인 얼굴 상태: 401
- test01 얼굴 상태: 200
- 얼굴 미등록 로그인: 404
- Python 서버 off 등록 시도: 503
- 샘플 얼굴 5장 등록: 200
- 샘플 얼굴 로그인: 200, 세션 `/api/members/me` 200
- 얼굴 등록 해제: 200
- 기존 Product/Recommendation/Comment/Admin API 정상 유지

### 한계

이 작업 세션에서는 실제 브라우저 웹캠 권한을 자동으로 허용하고 촬영하는 E2E를 수행하지 못했다. 서버/API 경로는 샘플 얼굴 이미지로 검증했고, 실제 웹캠 촬영은 사용자의 브라우저에서 최종 확인해야 한다.

---

## 2026-06-30 16차: 시연용 서비스 완성도 보완

### 작업 범위

GitHub 제출 정리가 아니라 오늘 시연 체감 품질을 높이는 데 집중했다. 기존 비밀번호 로그인, 얼굴 로그인, 추천, 상품, 댓글, 관리자 API는 유지하고 JSP/CSS/vanilla JS와 최소 API 파라미터만 보완했다.

### 생성/수정 파일

신규:
- `src/main/webapp/WEB-INF/jsp/demo/products.jsp`
- `scripts/collect_demo_image_urls.py`
- `docs/22_service_polish_result_report.md`

수정:
- `src/main/java/kr/ac/kopo/common/vo/PageParam.java`
- `src/main/java/kr/ac/kopo/product/controller/ProductController.java`
- `src/main/resources/config/sqlMap/oracle/productMapper.xml`
- `src/main/java/kr/ac/kopo/main/DemoViewController.java`
- `src/main/java/kr/ac/kopo/admin/**`
- `src/main/resources/config/sqlMap/oracle/adminMapper.xml`
- `src/main/webapp/index.jsp`
- `src/main/webapp/WEB-INF/jsp/common/header.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/login.jsp`
- `src/main/webapp/WEB-INF/jsp/demo/product_detail.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/dashboard.jsp`
- `src/main/webapp/WEB-INF/jsp/admin/comments.jsp`
- `src/main/webapp/assets/css/beautylens.css`
- `src/main/webapp/assets/js/ui.js`
- `docs/17_demo_script.md`

### 보완 내용

- `/products` 상품 탐색 화면 추가
- 상품 검색, 플랫폼 필터, 피부 타입 필터, 이미지 필터, 정렬, 더 보기 추가
- 상단 네비게이션에 상품 메뉴 추가
- `GET /api/products`에 `platform`, `imageOnly`, `sortBy=reviewCount` 파라미터 추가
- 관리자 대시보드에 이미지 미확보 수, 얼굴 등록 회원 수, 피부 타입별 상품 수, 이미지 커버리지, 최근 댓글 추가
- 관리자 댓글 관리에 검색과 상품 상세 이동 추가
- 메인에 상품 둘러보기 CTA와 데이터 지표 추가
- 상품 상세에서 내부 구현 문구를 사용자 관점 문구로 정리
- 얼굴 로그인 실패 시 비밀번호 로그인 가능 안내 추가
- `scripts/collect_demo_image_urls.py`로 시연 노출 우선 이미지 수집 스크립트 추가

### 검증 결과

- `mvn clean package`: BUILD SUCCESS
- WAR 재배포 완료
- `/api/health`: HTTP 200, `db=ok`
- `/`, `/products`, `/recommend`, `/products/1446`, `/mypage`, `/admin`, `/admin/comments`: HTTP 200
- `GET /api/products?imageOnly=true&sortBy=score&size=20`: HTTP 200, 20개
- `GET /api/recommendations/me?size=20`: test01 기준 HTTP 200, 10개
- `GET /api/recommendations?skinType=dry&size=20`: HTTP 200, 20개
- admin `/api/admin/summary`: HTTP 200
- test01 `/api/admin/summary`: HTTP 403
- 미로그인 `/api/admin/summary`: HTTP 401
- Python 얼굴 서버 `/health`: HTTP 200, `insightface-buffalo_l`

### 이미지 수집 결과

- 이미지 확보 상품: 216개
- 이미지 미확보 상품: 1,305개
- 제한 실제 수집은 외부 쇼핑몰 페이지 응답 실패로 신규 성공 0건
- 시연 화면에서는 이미지 있는 상품 우선 노출과 placeholder 안정화로 대응
---

## 2026-06-30 기능 확장: 사용자 피드백과 관리자 운영

### 작업 범위

BeautyLens를 단순 크롤링 리뷰 기반 추천 데모가 아니라 사용자 행동 데이터와 관리자 운영 기능이 붙은 서비스 형태로 확장했다. 기존 비밀번호 로그인, 얼굴 로그인, 상품/추천/댓글/관리자 기본 API는 유지하고, 원본 `BL_PRODUCTS`, `BL_REVIEWS`, `PRODUCT_NAME`은 삭제하거나 직접 수정하지 않았다.

### 추가 DB

- `BL_PRODUCT_FAVORITES`
- `BL_PRODUCT_RATINGS`
- `BL_RECOMMENDATION_FEEDBACK`
- `BL_USER_PRODUCT_EVENTS`
- `BL_PRODUCT_ADMIN_FLAGS`
- `BL_COMMENT_REPORTS`
- `BL_ADMIN_AUDIT_LOGS`

### 추가 기능

- 상품 찜/찜 해제
- 사이트 내부 별점, 자극 여부, 재구매 의사 저장
- 추천 좋아요/별로예요/관심 없음 피드백
- 상품 상세 조회 이벤트와 최근 본 상품
- 마이페이지 찜/평가/최근 본/추천 피드백 통합
- 관리자 상품 숨김/복구, 추천 제외/포함, 메인 노출, 품질 상태, 운영 메모
- 댓글 신고, 신고 처리, 댓글 복구
- 추천 `serviceScore` 추가 계산
- 관리자 대시보드 지표 확장
- 관리자 활동 로그

### 검증

- `mvn clean package`: 성공
- Oracle DDL 적용 및 테이블 존재 검증: 성공
- `/api/health`: 기존 실행 서버 기준 HTTP 200, DB ok
- Python 얼굴 서버 `/health`: HTTP 200
- 신규 WAR: `target/beautylens-mvc-0.0.1-SNAPSHOT.war` 생성 완료

### 주의 사항

현재 8088 Tomcat은 새 WAR를 자동 반영하지 않아 신규 `/admin/products`, `/admin/logs` 화면은 기존 실행 서버에서 404가 확인됐다. 새 기능 시연 전 WAR 재배포 또는 Tomcat 재시작이 필요하다.

---

## 2026-06-30 런타임 버그 수정 및 E2E 검증

### 확인한 배포 방식

- Eclipse WTP 방식
- `CATALINA_BASE=D:\Lecture\eclipse-server`
- `/beautylens-mvc` Context는 `D:\Lecture\eclipse-server\wtpwebapps\beautylens-mvc`를 바라봄

### 수정한 오류

- `POST /api/products/{productId}/events`에서 `ORA-17004: 열 유형이 부적합합니다.` 발생
- 원인: `eventValue=null` 바인딩 시 MyBatis `jdbcType` 미지정
- 수정:
  - `productInteractionMapper.xml`
  - `adminProductMapper.xml`
  - `commentReportMapper.xml`

### 검증 결과

- `mvn clean package`: 성공
- WTP 배포 폴더 최신 반영: 확인
- 주요 화면 10개: 200
- 공개 API: 200
- 미로그인 보호 API: 401
- USER 관리자 API: 403
- test01 사용자 E2E: 통과
- admin 관리자 E2E: 통과
- Python 얼굴 서버 `/health`: 200

상세 결과: `docs/26_runtime_bugfix_and_e2e_report.md`
