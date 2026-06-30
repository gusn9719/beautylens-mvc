from typing import List

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from face_service import FaceService


app = FastAPI(title="BeautyLens Face Auth Server")
service = FaceService()


class EnrollRequest(BaseModel):
    memberId: int
    images: List[str]


class VerifyRequest(BaseModel):
    image: str
    storedEmbedding: List[float]


class QualityRequest(BaseModel):
    image: str


@app.get("/health")
def health():
    return service.health()


@app.post("/face/check-quality")
def check_quality(req: QualityRequest):
    try:
        return service.check_quality(req.image)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@app.post("/face/enroll")
def enroll(req: EnrollRequest):
    try:
        return service.enroll(req.images)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))


@app.post("/face/verify")
def verify(req: VerifyRequest):
    try:
        return service.verify(req.image, req.storedEmbedding)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))
