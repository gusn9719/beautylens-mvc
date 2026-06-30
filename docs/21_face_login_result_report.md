# 21. Face Login Result Report

작성일: 2026-06-30

## 1. 구현 범위

- 마이페이지 얼굴 등록/재등록/해제 UI
- 로그인 페이지 얼굴 로그인 UI
- Python FastAPI 얼굴 인증 서버
- InsightFace buffalo_l 기반 얼굴 검출/임베딩/검증
- Spring MVC 얼굴 인증 API
- Oracle 얼굴 credential 테이블
- 기존 비밀번호 로그인 유지

## 2. 사용 기술

- Python FastAPI, uvicorn
- InsightFace `buffalo_l`
- ONNXRuntime CPU provider
- OpenCV fallback
- Browser `getUserMedia`
- Spring MVC Java 21 `HttpClient`
- Oracle CLOB JSON embedding 저장
- MyBatis XML

## 3. Python FastAPI 구조

경로:

```text
face_auth_server/
  app.py
  face_service.py
  requirements.txt
  README.md
```

API:

| API | 설명 |
|---|---|
| `GET /health` | 모델 상태 확인 |
| `POST /face/check-quality` | 얼굴 검출 품질 확인 |
| `POST /face/enroll` | 여러 이미지 평균 임베딩 생성 |
| `POST /face/verify` | 저장 임베딩과 현재 얼굴 비교 |

실행 확인:

```text
GET http://127.0.0.1:8090/health
200
modelName=insightface-buffalo_l
engineError=null
```

## 4. Spring API 구조

| API | 권한 | 설명 |
|---|---|---|
| `GET /api/members/me/face` | 로그인 | 얼굴 등록 상태 |
| `POST /api/members/me/face` | 로그인 | 얼굴 등록 |
| `DELETE /api/members/me/face` | 로그인 | 얼굴 등록 해제 |
| `POST /api/auth/face-login` | 공개 | loginId + 얼굴 인증 로그인 |

얼굴 로그인 성공 시 기존 세션 키 `loginMember`를 그대로 사용한다.

## 5. DB 테이블

```text
BL_FACE_CREDENTIALS
SEQ_BL_FACE_CREDENTIALS
```

저장 항목:

- memberId
- faceEmbedding JSON 배열 문자열
- modelName
- faceEnabled
- createdAt/updatedAt

원본 얼굴 사진은 저장하지 않는다.

검증:

```text
BL_FACE_CREDENTIALS 존재 확인
테스트 후 faceRows=1, active=0
```

테스트로 생성한 test01 얼굴 credential은 최종적으로 비활성화했다.

## 6. 얼굴 등록 흐름

1. 비밀번호로 로그인
2. `/mypage`
3. 얼굴 등록하기
4. 정면/좌/우/상/하 5장 촬영
5. Spring `POST /api/members/me/face`
6. Python `/face/enroll`
7. 평균 임베딩 저장
8. `registered=true`

## 7. 얼굴 로그인 흐름

1. `/login`
2. loginId 입력
3. 얼굴로 로그인
4. 2초 카운트다운 후 촬영
5. Spring `POST /api/auth/face-login`
6. Python `/face/verify`
7. 성공 시 `/recommend` 이동

## 8. 성공/실패 검증 결과

기존 기능 사전 검증:

| 항목 | 결과 |
|---|---|
| `mvn clean package` | BUILD SUCCESS |
| `/api/health` | 200, db=ok |
| Product API | 200 |
| Recommendation API | 200 |
| Admin summary | 200(admin) |

얼굴 기능 검증:

| 항목 | 결과 |
|---|---|
| Python `/health` | 200, insightface-buffalo_l |
| 미로그인 얼굴 상태 조회 | 401 |
| test01 얼굴 상태 조회 | 200, registered=false |
| 얼굴 미등록 face-login | 404 |
| 빈 등록 요청 | 400 |
| Python 서버 off 등록 시도 | 503 |
| 샘플 얼굴 5장 등록 | 200, registered=true |
| 샘플 얼굴 face-login | 200, `/api/members/me` 200 |
| 얼굴 등록 해제 | 200 |
| 해제 후 상태 | registered=false |

샘플 얼굴 테스트는 `skimage.data.astronaut()` 이미지를 밝기만 조금 다르게 만든 5장으로 수행했다. 실제 웹캠 촬영은 브라우저 권한과 물리 카메라가 필요해 이 자동화 세션에서는 수행하지 못했다.

## 9. 보안 한계

- 임베딩은 원본 사진보다 안전하지만 생체정보이므로 민감 데이터다.
- 현재 DB 암호화는 적용하지 않았다.
- loginId 없이 얼굴만으로 로그인하지 않도록 제한했다.
- API 직접 호출로 이미지를 보낼 수 있으므로 서버 측 고급 라이브니스는 아직 부족하다.

## 10. 라이브니스 한계

적용한 최소 조치:

- UI는 파일 업로드가 아니라 웹캠 캡처만 제공
- 등록 시 5방향 촬영
- 로그인 시 2초 카운트다운
- Python 등록 API에서 동일 base64 이미지 반복은 제외

한계:

- 사진을 카메라에 보여주는 공격은 완전히 차단하지 못한다.
- 눈 깜빡임, 랜덤 동작, 깊이 정보 검사는 미구현이다.

## 11. 남은 문제

- 이 Codex 명령 실행 환경은 하위 프로세스를 명령 종료 시 정리하므로 FastAPI 서버 장시간 실행은 수동 명령으로 유지해야 한다.
- 실제 웹캠 브라우저 E2E는 사용자의 브라우저에서 최종 확인이 필요하다.
- production-grade 얼굴 인증에는 threshold 튜닝, 암호화, 감사 로그, 라이브니스 강화가 필요하다.

## 12. 다음 개선점

- MediaPipe Face Mesh 기반 랜덤 방향/깜빡임 검사
- FACE_EMBEDDING 암호화 저장
- 얼굴 등록 이력/로그 테이블
- threshold 관리자 설정
- Python 서버 Windows 서비스 등록

## 13. 실행 명령

FastAPI 서버:

```powershell
cd D:\Lecture\spring-workspace\beautylens-mvc\face_auth_server
python -m uvicorn app:app --host 127.0.0.1 --port 8090
```

Spring/Tomcat:

```powershell
cd D:\Lecture\spring-workspace\beautylens-mvc
mvn clean package
copy target\beautylens-mvc-0.0.1-SNAPSHOT.war D:\Lecture\bin\apache-tomcat-11.0.18\webapps\beautylens-mvc.war
```
