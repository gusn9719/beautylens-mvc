# 20. Face Login Implementation Plan

작성일: 2026-06-30

## 1. 기능 목표

BeautyLens 기존 Spring MVC/JSP 사이트에 얼굴 등록과 얼굴 로그인 기능을 추가한다. 기존 비밀번호 로그인, 추천, 상품, 댓글, 관리자 기능은 유지한다.

## 2. 전체 아키텍처

- Browser JSP: 웹캠 촬영, 얼굴 등록/로그인 UI, 사용자 안내
- Spring MVC: 세션/회원 권한 처리, Oracle 저장, Python API 호출
- Python FastAPI: 얼굴 검출, 품질 검사, 임베딩 생성, 유사도 비교
- Oracle DB: 얼굴 원본 사진이 아닌 임베딩 JSON만 저장

## 3. Browser JSP 역할

- `/mypage`: 로그인 회원의 얼굴 등록/재등록/해제
- `/login`: loginId 입력 후 얼굴로 로그인
- `getUserMedia`로 웹캠 스트림을 열고 canvas 캡처로 base64 dataURL 생성
- 등록 시 정면/좌/우/상/하 5장 촬영
- 로그인 시 2초 카운트다운 후 1장 촬영
- 웹캠 권한 거부, 미지원, API 실패를 사용자 문구로 안내

## 4. Spring MVC 역할

- `GET /api/members/me/face`: 얼굴 등록 상태 조회
- `POST /api/members/me/face`: 로그인 회원 얼굴 등록
- `DELETE /api/members/me/face`: 얼굴 등록 해제
- `POST /api/auth/face-login`: loginId + 얼굴 이미지로 세션 로그인
- Python 서버 장애 시 503 응답
- 검증 성공 시 기존 `loginMember` 세션을 그대로 사용

## 5. Python FastAPI 역할

- `GET /health`: 모델/엔진 상태 확인
- `POST /face/check-quality`: 얼굴 검출 및 품질 확인
- `POST /face/enroll`: 여러 이미지에서 임베딩을 만들고 평균 임베딩 반환
- `POST /face/verify`: 현재 이미지 임베딩과 저장 임베딩의 cosine similarity 비교

## 6. Oracle DB 역할

`BL_FACE_CREDENTIALS`에 회원별 얼굴 credential을 저장한다.

- `FACE_EMBEDDING`: JSON 배열 문자열
- `MODEL_NAME`: 사용 엔진 이름
- `FACE_ENABLED`: 활성 여부
- 원본 사진 저장 금지

## 7. 얼굴 등록 흐름

1. test01 비밀번호 로그인
2. `/mypage`에서 얼굴 등록 시작
3. 브라우저가 5장 촬영
4. Spring `POST /api/members/me/face`
5. Spring이 Python `/face/enroll` 호출
6. Python이 얼굴 검출, 품질 검사, 임베딩 평균 생성
7. Spring이 임베딩 JSON을 Oracle에 저장
8. 등록 상태 true 반환

## 8. 얼굴 로그인 흐름

1. `/login`에서 loginId 입력
2. 얼굴로 로그인 버튼 클릭
3. 브라우저가 웹캠으로 1장 촬영
4. Spring `POST /api/auth/face-login`
5. Spring이 loginId로 회원과 얼굴 credential 조회
6. Spring이 Python `/face/verify` 호출
7. 유사도 threshold 이상이면 기존 세션 로그인 처리
8. 실패하면 401 반환, 비밀번호 로그인은 계속 사용 가능

## 9. 얼굴 검출/임베딩/유사도 비교 방식

- 우선순위: InsightFace/ArcFace 엔진
- 얼굴 검출 결과는 1명이어야 한다.
- 등록 이미지는 각각 임베딩을 만들고 L2 정규화 후 평균한다.
- 검증은 cosine similarity로 비교한다.
- threshold는 엔진별 기본값을 두고 운영 중 조정 가능하게 한다.

## 10. YOLO의 역할과 한계

YOLO만으로 얼굴 로그인은 불가능하다. YOLO는 얼굴 위치 검출에는 사용할 수 있지만, 같은 사람인지 판단하는 얼굴 임베딩을 만들지 않는다.

## 11. InsightFace/ArcFace/FaceNet 계열이 필요한 이유

얼굴 로그인의 핵심은 얼굴 이미지를 같은 사람끼리는 가깝고 다른 사람끼리는 멀게 배치하는 임베딩 벡터로 변환하는 것이다. 이 역할은 ArcFace, FaceNet 같은 얼굴 인식 모델이 담당한다.

## 12. 라이브니스 검사 계획

오늘 구현의 최소 라이브니스:

- 파일 업로드 대신 웹캠 캡처만 UI에서 제공
- 등록 시 5방향 촬영
- 로그인 시 2초 카운트다운 후 촬영
- 캡처 시각을 포함해 같은 요청 재사용 가능성을 낮춤

한계:

- 사진/영상 재생 공격을 완전히 막지 못한다.
- 눈 깜빡임, 깊이, 랜덤 동작 검사는 추후 개선 항목이다.

## 13. 보안 주의사항

- 얼굴 원본 사진은 DB에 저장하지 않는다.
- 임베딩도 생체정보이므로 DB 접근 권한을 제한해야 한다.
- loginId 없이 얼굴만으로 로그인하지 않는다.
- Python 서버는 localhost 전용으로 운영한다.
- 얼굴 인증 실패 시 비밀번호 로그인 흐름을 깨뜨리지 않는다.

## 14. 실패 시 fallback 전략

- Python 서버 장애: Spring은 503과 명확한 메시지를 반환
- 모델 import 실패: 원인 로그와 결과 보고서에 기록
- InsightFace 모델 다운로드 실패: OpenCV 기반 데모 엔진으로 fallback 가능하나 보안 수준은 낮다고 명시
- 웹캠 권한 거부: 비밀번호 로그인 사용 안내

## 15. 단계별 검증 계획

1. 기존 빌드/API 정상 확인
2. DB 테이블 생성/조회 확인
3. Python `/health` 확인
4. Python quality/enroll/verify 단위 확인
5. Spring 빌드 확인
6. 얼굴 등록 API 확인
7. 얼굴 로그인 API 확인
8. JSP 화면 HTTP 확인
9. 기존 Product/Recommendation/Comment/Admin API 회귀 확인
