# BeautyLens Face Auth Server

Local FastAPI service for BeautyLens face enrollment and face login.

## Run

```powershell
cd D:\Lecture\spring-workspace\beautylens-mvc\face_auth_server
python -m pip install -r requirements.txt
python -m uvicorn app:app --host 127.0.0.1 --port 8090
```

## APIs

```text
GET  /health
POST /face/check-quality
POST /face/enroll
POST /face/verify
```

The server tries InsightFace first. If InsightFace model initialization fails, it falls back to an OpenCV Haar-cascade demo engine. The fallback is useful for local wiring tests but is not production-grade biometric authentication.
