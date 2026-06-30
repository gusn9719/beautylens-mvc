# 12. 시드 이미지 URL 수집 보고서 (10차 Lite)

작성일: 2026-06-30  
작성 방식: 피부 타입별 추천 상위 상품 우선 수집 (Lite 모드)

---

## 배경 및 전환 이유

전체 1,521건 이미지 수집 루프(PID 35332)를 실행하였으나, 시연/UI 준비 목적에 맞게  
피부 타입별 추천 상위 상품(약 70건)만 우선 확보하는 방향으로 전환.  
전체 수집은 추후 별도 단계로 진행 예정.

- 중단 시점: 200건 수집 완료 상태에서 중단 (기존 found 보존)
- 시드 타겟 70건 중 54건이 기 수집된 상태여서 추가 수집은 16건만 진행

---

## 수집 대상 선정

**기준**: 피부 타입별 RECOMMENDATION_SCORE 상위 15개, 중복 productId 제거

| 피부 타입 | 대상 수 | DB 한국어 |
|---|---|---|
| 건성 (dry) | 15 | 건성 |
| 지성 (oily) | 15 | 지성 |
| 복합성 (combination) | 15 | 복합성 |
| 민감성 (sensitive) | 15 | 민감성 |
| 중성 (normal) | **10** | 중성 (DB 상품 10개뿐, 로직 정상) |
| **합계** | **70** | — |

대상 파일: `docs/seed_image_targets.csv`

---

## 수집 방법

| 항목 | 내용 |
|---|---|
| 스크립트 | `scripts/collect_seed_images.py` |
| Cloudflare 우회 | cloudscraper 라이브러리 |
| 이미지 추출 | og:image meta 태그 (1순위) → twitter:image → CDN 직접 |
| 재시도 | 403/429/503 시 5초 대기 후 최대 2회 |
| Resume | IMAGE_STATUS='found' 상품 자동 스킵 |
| 커밋 주기 | 즉시 커밋 (건별) |
| sleep | 2.5초 |

---

## 수집 결과

### 전체 요약

| 항목 | 값 |
|---|---|
| 대상 상품 수 | **70** |
| 기존 found (스킵) | 54 |
| 신규 수집 | 16 |
| 성공 (found) | **70 / 70 = 100%** |
| not_found | 0 |
| fetch_failed | 0 |
| blocked | 0 |
| 실패 | **0** |

### 피부 타입별 수집 결과

| 피부 타입 | 목표 | found | 성공률 |
|---|---|---|---|
| 건성 | 15 | 15 | 100% ✅ |
| 지성 | 15 | 15 | 100% ✅ |
| 복합성 | 15 | 15 | 100% ✅ |
| 민감성 | 15 | 15 | 100% ✅ |
| 중성 | 10 | 10 | 100% ✅ |
| **합계** | **70** | **70** | **100%** |

### 플랫폼별 분포 (시드 70건)

| 플랫폼 | 건수 | 비율 |
|---|---|---|
| musinsa | 46 | 65.7% |
| oliveyoung | 24 | 34.3% |

### 이미지 CDN 도메인

| 플랫폼 | CDN 도메인 |
|---|---|
| oliveyoung | `image.oliveyoung.co.kr` |
| musinsa | `image.msscdn.net` |

---

## DB 최종 상태 (전체 1,521건 기준)

| IMAGE_STATUS | 건수 |
|---|---|
| found | 216 |
| NULL (미수집) | 1,305 |
| **TOTAL** | **1,521** |

> 216건 = 시드 70건 + 이전 부분 수집 146건 (보존)  
> 시드 70건은 전원 found 상태로 추천 API 정상 동작

---

## 피부 타입별 상위 상품 샘플

### 건성 (dry) 상위 3개

| rank | productId | productName | platform | imageUrl 도메인 |
|---|---|---|---|---|
| 1 | 545 | [콜라보] 블랙헤드 세라마이드 유자 힐링 클렌징 밤 | musinsa | image.msscdn.net |
| 2 | 733 | [SET] 다이브인 저분자 히알루론산 스킨부스터 200ml x 2개 | musinsa | image.msscdn.net |
| 3 | 1005 | 저분자 히알루론산 선 에센스 40ml | musinsa | image.msscdn.net |

### 지성 (oily) 상위 3개

| rank | productId | productName | platform | imageUrl 도메인 |
|---|---|---|---|---|
| 1 | 743 | [짜서쓰는클밤/쫀쫀모공] 네시픽 라이스 펩타 콜라겐 샤베트 클렌징밤 | oliveyoung | image.oliveyoung.co.kr |
| 2 | 476 | [옵션선택] 코튼풋 발을씻자 풋샴푸 510ml 2개 | musinsa | image.msscdn.net |
| 3 | 1052 | 쥬베룩 PDLLA 콜라겐 부스팅 앰플 20g | oliveyoung | image.oliveyoung.co.kr |

### 복합성 (combination) 상위 3개

| rank | productId | productName | platform | imageUrl 도메인 |
|---|---|---|---|---|
| 1 | 1446 | 쥬베룩 PDLLA 콜라겐 부스팅 마스크 4매 | oliveyoung | image.oliveyoung.co.kr |
| 2 | 730 | 그린티 허니드롭스 바디크림 500ml | musinsa | image.msscdn.net |
| 3 | 348 | 액티브 스웨트프루프 선스틱 + 쿨 데오 샤워티슈 | musinsa | image.msscdn.net |

### 민감성 (sensitive) 상위 3개

| rank | productId | productName | platform | imageUrl 도메인 |
|---|---|---|---|---|
| 1 | 698 | [2PACK] 아쿠아 스쿠알란 세럼 70ml | musinsa | image.msscdn.net |
| 2 | 267 | [증정] 무향 100시간 보습 세라마이드 아토 로션 350ml 2입 | musinsa | image.msscdn.net |
| 3 | 383 | 히어로 마이 퍼스트 세럼 155ml | musinsa | image.msscdn.net |

### 중성 (normal) 상위 3개

| rank | productId | productName | platform | imageUrl 도메인 |
|---|---|---|---|---|
| 1 | 933 | [NEW/키링증정] 토코보 미니 선스틱 11g 기획 3종 | oliveyoung | image.oliveyoung.co.kr |
| 2 | 419 | 바이오던스 리포좀 버블 부스터 95ml | oliveyoung | image.oliveyoung.co.kr |
| 3 | 913 | [단독기획/추가증정] 셀리맥스 시카 지우개 패드 60+20매 | oliveyoung | image.oliveyoung.co.kr |

---

## API 검증 결과

### GET /api/products?sortBy=score&size=20

| 항목 | 결과 |
|---|---|
| HTTP 상태 | 200 ✅ |
| imageStatus=found | 20/20 ✅ |
| imageUrl not null | 20/20 ✅ |
| productUrl not null | 20/20 ✅ |
| displayName not null | 20/20 ✅ |

### GET /api/products/{productId} (단건)

| 항목 | 결과 |
|---|---|
| id=730 (musinsa, found) | HTTP 200, imageUrl=`https://image.msscdn.net/...` ✅ |
| id=462 (NULL 상태) | HTTP 200, imageUrl=null, imageStatus=null ✅ |

### GET /api/recommendations/me?size=20 (로그인 후, skinType=normal)

| 항목 | 결과 |
|---|---|
| HTTP 상태 | 200 ✅ |
| count | 10 (중성 상품 10개) ✅ |
| imageStatus=found | 10/10 ✅ |
| imageUrl not null | 10/10 ✅ |
| productUrl not null | 10/10 ✅ |
| displayName not null | 10/10 ✅ |
| reason not null | 10/10 ✅ |
| platform null | 0/10 ✅ |
| HTTP 500 발생 | 없음 ✅ |

---

## 결과 파일

| 파일 | 내용 |
|---|---|
| `docs/seed_image_targets.csv` | 피부 타입별 수집 대상 70건 목록 |
| `docs/seed_image_collection_results.csv` | 신규 수집 16건 결과 (imageUrl, imageStatus, method) |
| `scripts/collect_seed_images.py` | 타겟 수집 스크립트 (Resume 가능) |

---

## placeholder 필요 여부

**불필요** — 시드 70건 전원 found 상태.  
미수집 상품(IMAGE_STATUS IS NULL)에 대해서는 API가 imageUrl=null 반환하며, 클라이언트에서 fallback 처리 권장:
- imageStatus == 'found' 이면 imageUrl 사용
- null / 미수집 상태이면 로컬 placeholder 이미지 표시

---

## 전체 수집 가능 여부

**가능** — 이후 단계에서 `scripts/collect_image_urls.py --actual --sleep 2.5` 실행.

| 항목 | 상태 |
|---|---|
| 전체 컬럼 준비 | PRODUCT_URL, IMAGE_URL, IMAGE_STATUS 이미 추가됨 ✅ |
| Resume 지원 | IMAGE_STATUS='found' 자동 스킵 (`--force`로 강제 재수집) ✅ |
| 미수집 잔량 | 1,305건 |
| 예상 소요 시간 | ~98분 (sleep 2.5s 기준) |
| 전체 수집 명령 | `python scripts/collect_image_urls.py --actual --sleep 2.5` |
