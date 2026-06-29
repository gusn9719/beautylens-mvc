# 08. 추천 API 품질 검증 보고서

작성일: 2026-06-30  
단계: 7차 — 6차-2 전체 import 완료 후 품질 재검증  
검증 대상: `GET /api/recommendations/me` 및 연관 API 전체

---

## 1. 검증 환경

| 항목 | 값 |
|---|---|
| BL_PRODUCTS | 1,521개 |
| BL_REVIEWS | 323,574개 |
| 테스트 계정 | test01 (MEMBER_ID=1) |
| Tomcat PID | 4916, 포트 8088 |
| Health API | HTTP 200, `"db":"ok"` ✅ |

### DB 분포 (검증 시점)

| BASE_SKIN_TYPE | 상품 수 |
|---|---|
| 복합성 | 537 |
| 지성 | 503 |
| 건성 | 306 |
| 민감성 | 165 |
| 중성 | 10 |

| CAUTION_LEVEL | 상품 수 |
|---|---|
| normal | 1,025 |
| insufficient_evidence | 308 |
| moderate_negative_signal | 142 |
| high_negative_signal | 46 |

---

## 2. 인증 테스트

| 시나리오 | 결과 | 비고 |
|---|---|---|
| 미로그인 GET /api/recommendations/me | ✅ HTTP 401 | success=false, "not logged in" |
| POST /api/auth/login (test01/1234) | ✅ HTTP 200 | JSESSIONID 발급, password 응답 제외 |
| 로그아웃 후 GET /api/recommendations/me | ✅ HTTP 401 | 세션 무효화 정상 |

---

## 3. skinType별 추천 API 테스트

### 3-1. 건성 (dry)

PUT /api/members/me → skinType=dry 설정 후 GET /api/recommendations/me

| 항목 | 결과 |
|---|---|
| PUT skinType 반영 | ✅ |
| GET /me 확인 | ✅ dry |
| 기본 요청 (default size) | ✅ HTTP 200, 20개 반환 |
| size=5 | ✅ 5개 |
| size=20 | ✅ 20개 |
| baseSkinType 일치율 | ✅ 20/20 (건성 100%) |
| 점수 내림차순 | ✅ |
| reason null | ✅ 0건 |
| 중복 상품 | ✅ 없음 |

**상위 10개 (건성)**:

| 순위 | productId | brand | score | caution | 상품명 |
|---|---|---|---|---|---|
| 1 | 545 | irecipe | 99.40 | normal | [콜라보] 블랙헤드 세라마이드 유자 힐링 클렌징 밤 135ml+여행용 10g+헤어핀 굿즈 |
| 2 | 733 | torriden | 99.36 | normal | [SET] 다이브인 저분자 히알루론산 스킨부스터 200ml x 2개 |
| 3 | 1005 | thelabbyblan | 99.18 | normal | 저분자 히알루론산 선 에센스 40ml [SPF50+/PA++++] |
| 4 | 581 | sennok | 99.12 | normal | 바디로션 300ml (3종 택1) +(증정) 고체향수키링 레몬비누향 |
| 5 | 691 | lush | 99.10 | normal | 슬리피 200ml - 바디 스프레이(트와일라잇) |
| 6 | 1379 | aromatica1 | 99.00 | normal | 서렌 바디오일 라벤더 & 마조람 100ML |
| 7 | 325 | elroel | 98.99 | normal | [100분 타임어택] 팡팡 선쿠션 미니 본품 8g |
| 8 | 377 | lush | 98.98 | normal | 마스크 오브 매그너민티 125g - 파워 마스크/페이스 앤 바디 마스크 팩 |
| 9 | 781 | abib | 98.94 | normal | 수분초 히알루론 선세럼 워터리 튜브 SPF50+ PA++++ 50ml(+선크림 3EA 랜덤 증정) |
| 10 | 899 | illiyoon | 98.90 | normal | [증정] 튼살 붉은선 개선 저자극튼살크림 330ml |

reason 패턴: `건성 피부 타입 기준 추천 점수가 높습니다. 피부 고민 키워드와 관련된 태그가 포함되어 있습니다. 부정 리뷰 신호가 낮은 편입니다.`

---

### 3-2. 지성 (oily)

| 항목 | 결과 |
|---|---|
| PUT skinType 반영 | ✅ |
| 기본 요청 | ✅ HTTP 200, 20개 |
| size=5/20 | ✅ |
| baseSkinType 일치율 | ✅ 20/20 (지성 100%) |
| 점수 내림차순 | ✅ |
| reason null | ✅ 0건 |
| 중복 상품 | ✅ 없음 |

**상위 10개 (지성)**:

| 순위 | productId | brand | score | caution | 상품명 |
|---|---|---|---|---|---|
| 1 | 743 | 네시픽 | 99.77 | normal | [짜서쓰는클밤/쫀쫀모공] 네시픽 라이스 펩타 콜라겐 샤베트 클렌징밤 80g |
| 2 | 476 | onthebody | 99.62 | normal | [옵션선택] 코튼풋 발을씻자 풋샴푸 510ml 2개 + 리필 500ml 1개 |
| 3 | 1052 | 쥬베룩 | 99.60 | normal | 쥬베룩 PDLLA 콜라겐 부스팅 앰플 20g (+프로텍팅 크림 2ml*5EA) |
| 4 | 807 | 바이오던스 | 99.57 | normal | [NEW/단독선런칭] 바이오던스 아이패치 60매 2종 택 1 |
| 5 | 1178 | 조성아뷰티 | 99.54 | normal | [성한빈 PICK] 원더바스 레몬청 팩클렌저 200ml 기획 (+50ml) |
| 6 | 665 | dermab | 99.24 | normal | 프레쉬 모이스처 바디 로션 400MLX 2개+[증정]쿨시트1매+세라엠디로션20ml |
| 7 | 1201 | 션리 | 99.19 | normal | [수분진정/광채캡슐크림] 션리 다시마 글레이즈드 크림 50g |
| 8 | 228 | 체이싱래빗 | 99.17 | normal | NEW[파데프리/톤업선/톤업선크림] 체이싱래빗 올어바웃 글로우 톤업 선 70ml |
| 9 | 1229 | anessa | 99.17 | normal | 퍼펙트 UV 선스크린 스킨케어 밀크 NA 60ml |
| 10 | 681 | 수이사이 | 99.13 | normal | 수이사이 뷰티 클리어 그린/핑크 파우더워시 4종 택1 |

---

### 3-3. 복합성 (combination)

| 항목 | 결과 |
|---|---|
| 기본 요청 | ✅ HTTP 200, 20개 |
| size=5/20 | ✅ |
| baseSkinType 일치율 | ✅ 20/20 (복합성 100%) |
| 점수 내림차순 | ✅ |
| reason null | ✅ 0건 |
| 중복 상품 | ✅ 없음 |

**상위 10개 (복합성)**:

| 순위 | productId | brand | score | caution | 상품명 |
|---|---|---|---|---|---|
| 1 | 1446 | 쥬베룩 | 99.82 | normal | 쥬베룩 PDLLA 콜라겐 부스팅 마스크 4매 |
| 2 | 730 | elizabetharden | 99.77 | normal | 그린티 허니드롭스 바디크림 500ml |
| 3 | 348 | obge | 99.76 | normal | 액티브 스웨트프루프 선스틱 + 쿨 데오 샤워티슈 1매 |
| 4 | 580 | hamel | 99.73 | normal | 비건 릴리프 워터 에센스 선크림 |
| 5 | 978 | beplain | 99.63 | normal | [SET] 아쿠아 퓨어 히알루로닉 크림 2개 + 세럼 2개 |
| 6 | 1139 | typeno | 99.60 | normal | TYPE No.91 원더바이브 핸드워시 350ml |
| 7 | 707 | innisfree | 99.44 | normal | [사은품 증정] (대용량) 그린티 씨드 히알루론산 크림 80mL + 15mL 2개 |
| 8 | 1416 | imfrom | 99.40 | normal | 라이스 토너 150ml |
| 9 | 1272 | AHC | 99.39 | normal | [포켓몬 에디션] AHC 프로샷 아이크림 포페이스 30ml 기획 |
| 10 | 1528 | lush | 99.37 | normal | 스토미 웨더 200ml - 보디 스프레이 |

---

### 3-4. 민감성 (sensitive)

| 항목 | 결과 |
|---|---|
| 기본 요청 | ✅ HTTP 200, 20개 |
| size=5/20 | ✅ |
| baseSkinType 일치율 | ✅ 20/20 (민감성 100%) |
| 점수 내림차순 | ✅ |
| reason null | ✅ 0건 |
| 중복 상품 | ✅ 없음 |

**상위 10개 (민감성)**:

| 순위 | productId | brand | score | caution | totalReview | 상품명 |
|---|---|---|---|---|---|---|
| 1 | 698 | snature | 99.33 | normal | 263 | [2PACK] 아쿠아 스쿠알란 세럼 70ml |
| 2 | 267 | illiyoon | 99.30 | normal | 799 | [증정] 무향 100시간 보습 세라마이드 아토 로션 350ml 2입 |
| 3 | 383 | demaf | 99.03 | normal | 366 | 히어로 마이 퍼스트 세럼 a.k.a. 만능기초 155ml |
| 4 | 474 | aromatica1 | 98.69 | normal | 691 | 수딩 알로에 베라 젤 500ML |
| 5 | 246 | roundlab | 98.44 | normal | 1,687 | [무신사 단독] [2pack] 소나무 진정 시카 앰플 대용량 50ml |
| 6 | 497 | cosrx | 98.27 | normal | 176 | 울트라 라이트 인비저블 선세럼 50ml SPF50+ PA++++ |
| 7 | 297 | aestura | 97.91 | normal | 3,048 | 아토베리어365 로션 150ml |
| 8 | 282 | drbronners | 97.72 | normal | 898 | [페이스&바디클렌저]티트리 퓨어 캐스틸 솝 950ml |
| 9 | 560 | demaf | 97.50 | normal | 223 | 크림스컴트루 스쿠알란 배리어 리페어크림 50ml |
| 10 | 366 | cosrx | 95.39 | normal | 2,444 | 어드밴스드 더 비타민씨 23 세럼 |

---

### 3-5. 중성 (normal)

| 항목 | 결과 | 비고 |
|---|---|---|
| 기본 요청 | ✅ HTTP 200, 10개 | DB 중성 상품 10개뿐 |
| size=5 | ✅ 5개 | |
| size=20 | ⚠️ 10개 반환 | **데이터 한계** — DB 중성 상품이 10개 |
| baseSkinType 일치율 | ✅ 10/10 | |
| 점수 내림차순 | ✅ | |
| reason null | ✅ 0건 | |
| insufficient_evidence reason | ✅ "리뷰 데이터가 충분하지 않아 신뢰도 판단에 참고가 필요합니다." | |
| 중복 상품 | ✅ 없음 | |

**상위 10개 (중성)**:

| 순위 | productId | score | caution | skinReview | 상품명 |
|---|---|---|---|---|---|
| 1 | 933 | 77.68 | normal | 5 | [NEW/키링증정] 토코보 미니 선스틱 11g 기획 3종 SPF50+ PA++++ |
| 2 | 419 | 52.88 | insufficient_evidence | 2 | [NEW/단독선런칭] 바이오던스 리포좀 버블 부스터 95ml |
| 3 | 913 | 47.00 | insufficient_evidence | 2 | [단독기획/추가증정] 셀리맥스 시카 지우개 패드 60+20매 기획세트 |
| 4 | 958 | 45.48 | insufficient_evidence | 2 | [블랙헤드 쏙] 바닐라코 클린잇제로 포어클래리파잉 클렌징밤 100ml |
| 5 | 640 | 44.81 | insufficient_evidence | 2 | [수분민감/미세먼지OUT] 라곰 셀럽 마이크로 폼 클렌저 2종 택 1 |
| 6 | 537 | 43.12 | insufficient_evidence | 1 | [단독기획] 아누아 어성초 센텔라 레드 스팟 크림 30g 2입 |
| 7 | 683 | 43.11 | insufficient_evidence | 4 | [유수분밸런스]브링그린 티트리 시카 수딩 크림 플러스 100ml |
| 8 | 368 | 42.87 | insufficient_evidence | 3 | [5월 올영픽][대용량] 제로이드 수딩 크림 80ml 기획 (+50ml) |
| 9 | 931 | 40.42 | insufficient_evidence | 3 | 아누아 어성초 피지쏙 모공 폼 150ml 2입 기획 |
| 10 | 361 | 25.73 | insufficient_evidence | 2 | [장도연 PICK/초밀착 세럼팩] 메디힐 랩핑 세럼 마스크 10매 6종 |

**중성 스코어 특이사항**: top 점수가 77.68로 다른 피부 타입 대비 현저히 낮음. 중성 피부 타입 리뷰 데이터(skinReview) 자체가 1~5건 수준으로 매우 적어 점수 산출 한계.

---

## 4. 상품-리뷰 연결 검증

skinType별 상위 3개 상품 × 5개 타입 = 15개 상품 대상

| 상품 ID | GET /products/{id} | GET /reviews | /reviews/negative | /reviews/positive | 판정 |
|---|---|---|---|---|---|
| 545 (건성 1위) | 200 | 95건 | 0건 ✅ | 95건 ✅ | ✅ |
| 733 (건성 2위) | 200 | 291건 | 1건 ✅ | 290건 ✅ | ✅ |
| 1005 (건성 3위) | 200 | 161건 | 0건 ✅ | 160건 ✅ | ✅ |
| 743 (지성 1위) | 200 | 109건 | 0건 ✅ | 109건 ✅ | ✅ |
| 476 (지성 2위) | 200 | 164건 | 0건 ✅ | 164건 ✅ | ✅ |
| 1052 (지성 3위) | 200 | 119건 | 0건 ✅ | 119건 ✅ | ✅ |
| 1446 (복합성 1위) | 200 | 124건 | 0건 ✅ | 124건 ✅ | ✅ |
| 730 (복합성 2위) | 200 | 53건 | 0건 ✅ | 53건 ✅ | ✅ |
| 348 (복합성 3위) | 200 | 82건 | 0건 ✅ | 82건 ✅ | ✅ |
| 698 (민감성 1위) | 200 | 263건 | 0건 ✅ | 262건 ✅ | ✅ |
| 267 (민감성 2위) | 200 | 799건 | 0건 ✅ | 796건 ✅ | ✅ |
| 383 (민감성 3위) | 200 | 366건 | 1건 ✅ | 365건 ✅ | ✅ |
| 933 (중성 1위) | 200 | 216건 | 5건 ✅ | 204건 ✅ | ✅ |
| 419 (중성 2위) | 200 | 188건 | 5건 ✅ | 182건 ✅ | ✅ |
| 913 (중성 3위) | 200 | 249건 | 32건 ✅ | 184건 ✅ | ✅ |

**sentiment label 정확성**: /reviews/negative는 negative만, /reviews/positive는 positive만 — **15/15 통과** ✅

---

## 5. 성능 측정

### 추천 API (GET /api/recommendations/me)

| skinType | default | size=5 | size=20 |
|---|---|---|---|
| dry | 6ms | 4ms | 5ms |
| oily | 5ms | 4ms | 5ms |
| combination | 7ms | 3ms | 4ms |
| sensitive | 4ms | 3ms | 5ms |
| normal | 3ms | 3ms | 4ms |

- 전체 min=3ms / max=7ms / **avg=4ms** ✅
- 3초 초과: **없음** ✅

### 기타 API

| API | 응답 시간 | 상태 |
|---|---|---|
| GET /api/products?sortBy=score | 27ms | ✅ |
| GET /api/products?keyword=크림 | 5ms | ✅ |
| GET /api/products?size=20&sortBy=score | 4ms | ✅ |

---

## 6. 발견된 문제

### BUG-1: `platform` 필드 누락 → ✅ FIXED

**원인**:
- `src/main/java/kr/ac/kopo/recommendation/vo/RecommendationVO.java` — `platform` 필드 없음
- `src/main/resources/config/sqlMap/oracle/recommendationMapper.xml` — `PLATFORM AS platform` SELECT 없음

**증상 (수정 전)**: 추천 API 전 skinType 응답에서 `platform=null`

**수정 내용**:
- `RecommendationVO.java`: `private String platform;` + `getPlatform()`/`setPlatform()` 추가
- `recommendationMapper.xml` `selectBySkinType`, `selectFallback` 양쪽에 `PLATFORM AS platform,` 추가

**빌드 결과**: `mvn clean package → BUILD SUCCESS`

**재배포**: Tomcat autoDeploy (WAR 교체, PID 4916 유지)

**수정 후 재검증 결과**:
- platform null: 0/20 ✅
- platform 유효값(oliveyoung/musinsa): 전 skinType 확인 ✅
- 기타 필드(reason, score, baseSkinType) 이상 없음 ✅
- HTTP 500 없음 ✅

---

### DATA-1: 중성(normal) 상품 10개 — 데이터 한계 [WARNING — 수정 불필요]

**원인**: BL_PRODUCTS에서 `BASE_SKIN_TYPE='중성'` 상품이 10개뿐 (원본 scoring 파이프라인 결과)

**증상**: `size=20` 요청 시 10개만 반환 → 테스트 기준 실패

**판단**: 로직 버그 아님. fallback(전체 상품 중 점수순) 미작동은 서비스 설계 기준 (피부 타입 미매칭 fallback은 결과 없을 때만 발동).

**향후 검토**: normal→fallback을 "부족할 때 보충"으로 수정하거나, coupang B안으로 중성 상품 보강 가능.

---

## 7. 품질 판정 요약

| 항목 | 판정 | 비고 |
|---|---|---|
| 미로그인 401 | ✅ SUCCESS | |
| 로그인 / JSESSIONID | ✅ SUCCESS | |
| 건성 추천 | ✅ SUCCESS | 20/20 건성 타입, 점수순 정상 |
| 지성 추천 | ✅ SUCCESS | 20/20 지성 타입 |
| 복합성 추천 | ✅ SUCCESS | 20/20 복합성 타입 |
| 민감성 추천 | ✅ SUCCESS | 20/20 민감성 타입 |
| 중성 추천 | ⚠️ WARNING | size=20 → 10개 반환 (데이터 한계) |
| reason 생성 | ✅ SUCCESS | 전 타입 null 없음, 피부 타입별 문구 정상 |
| cautionLevel → reason 반영 | ✅ SUCCESS | insufficient_evidence reason 확인 |
| 중복 상품 없음 | ✅ SUCCESS | |
| 상품-리뷰 연결 | ✅ SUCCESS | 15/15 상품 정상 |
| sentiment label 정확성 | ✅ SUCCESS | negative/positive 분리 정확 |
| 응답 성능 | ✅ SUCCESS | avg 4ms, 3초 초과 없음 |
| **platform 필드** | ✅ FIXED | RecommendationVO + mapper 추가 완료 |
| 로그아웃 후 401 | ✅ SUCCESS | |

**전체 판정: SUCCESS with 1 BUG (platform 필드 누락)**

---

## 8. 다음 조치

| 우선순위 | 항목 | 내용 |
|---|---|---|
| ~~HIGH~~ | ~~BUG-1 수정~~ | ✅ 완료 (2026-06-30) |
| MEDIUM | 중성 상품 보강 | coupang B안 재검토 or fallback 로직 개선 |
| LOW | 상품명 정제 | 프로모션 키워드 UI layer 처리 방식 결정 |
| LOW | 중성 fallback | size < 요청 size 시 다른 타입으로 보충 여부 결정 |
