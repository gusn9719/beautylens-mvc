import base64
import hashlib
import io
import os
from dataclasses import dataclass
from typing import Any, Dict, List, Optional

import numpy as np
from PIL import Image


def decode_image(image_data: str) -> np.ndarray:
    if not image_data:
        raise ValueError("image is required")
    if "," in image_data:
        image_data = image_data.split(",", 1)[1]
    raw = base64.b64decode(image_data, validate=False)
    image = Image.open(io.BytesIO(raw)).convert("RGB")
    return np.array(image)


def l2_normalize(vector: np.ndarray) -> np.ndarray:
    norm = np.linalg.norm(vector)
    if norm == 0:
        return vector
    return vector / norm


def cosine_similarity(a: List[float], b: List[float]) -> float:
    va = np.asarray(a, dtype=np.float32)
    vb = np.asarray(b, dtype=np.float32)
    if va.shape != vb.shape:
        raise ValueError("embedding dimension mismatch")
    denom = float(np.linalg.norm(va) * np.linalg.norm(vb))
    if denom == 0:
        return 0.0
    return float(np.dot(va, vb) / denom)


@dataclass
class FaceResult:
    embedding: Optional[np.ndarray]
    face_count: int
    message: str


class InsightFaceEngine:
    model_name = "insightface-buffalo_l"
    threshold = 0.45

    def __init__(self) -> None:
        from insightface.app import FaceAnalysis

        model_root = os.path.join(os.path.dirname(__file__), "models")
        self.app = FaceAnalysis(name="buffalo_l", root=model_root, providers=["CPUExecutionProvider"])
        self.app.prepare(ctx_id=-1, det_size=(640, 640))

    def extract(self, image: np.ndarray) -> FaceResult:
        # InsightFace expects BGR input.
        bgr = image[:, :, ::-1]
        faces = self.app.get(bgr)
        if len(faces) != 1:
            return FaceResult(None, len(faces), "one face is required")
        embedding = l2_normalize(np.asarray(faces[0].embedding, dtype=np.float32))
        return FaceResult(embedding, 1, "ok")


class OpenCvFallbackEngine:
    model_name = "opencv-histogram-fallback"
    threshold = 0.88

    def __init__(self) -> None:
        import cv2

        self.cv2 = cv2
        cascade_path = cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
        self.detector = cv2.CascadeClassifier(cascade_path)
        if self.detector.empty():
            raise RuntimeError("OpenCV Haar cascade not found")

    def extract(self, image: np.ndarray) -> FaceResult:
        cv2 = self.cv2
        gray = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)
        faces = self.detector.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(80, 80))
        if len(faces) != 1:
            return FaceResult(None, int(len(faces)), "one face is required")
        x, y, w, h = faces[0]
        crop = gray[y:y + h, x:x + w]
        resized = cv2.resize(crop, (32, 32), interpolation=cv2.INTER_AREA)
        vector = resized.astype(np.float32).flatten() / 255.0
        vector = vector - float(np.mean(vector))
        return FaceResult(l2_normalize(vector), 1, "ok")


class FaceService:
    def __init__(self) -> None:
        self.engine_error: Optional[str] = None
        try:
            self.engine = InsightFaceEngine()
        except Exception as exc:
            self.engine_error = f"InsightFace unavailable: {exc}"
            self.engine = OpenCvFallbackEngine()

    @property
    def model_name(self) -> str:
        return self.engine.model_name

    @property
    def threshold(self) -> float:
        return self.engine.threshold

    def health(self) -> Dict[str, Any]:
        return {
            "success": True,
            "modelName": self.model_name,
            "engineError": self.engine_error,
        }

    def check_quality(self, image_data: str) -> Dict[str, Any]:
        image = decode_image(image_data)
        result = self.engine.extract(image)
        return {
            "success": result.face_count == 1,
            "faceDetected": result.face_count > 0,
            "faceCount": result.face_count,
            "modelName": self.model_name,
            "message": result.message,
        }

    def enroll(self, images: List[str]) -> Dict[str, Any]:
        if not images:
            raise ValueError("images are required")
        embeddings: List[np.ndarray] = []
        failures: List[str] = []
        seen_hashes = set()
        for index, image_data in enumerate(images):
            try:
                raw_part = image_data.split(",", 1)[1] if "," in image_data else image_data
                digest = hashlib.sha256(raw_part.encode("utf-8")).hexdigest()
                if digest in seen_hashes:
                    failures.append(f"image {index + 1}: duplicate image")
                    continue
                seen_hashes.add(digest)
                result = self.engine.extract(decode_image(image_data))
                if result.embedding is None:
                    failures.append(f"image {index + 1}: {result.message} (faces={result.face_count})")
                    continue
                embeddings.append(result.embedding)
            except Exception as exc:
                failures.append(f"image {index + 1}: {exc}")

        if len(embeddings) < 2:
            raise ValueError("at least two valid face images are required; " + "; ".join(failures))

        mean_embedding = l2_normalize(np.mean(np.stack(embeddings), axis=0))
        return {
            "success": True,
            "embedding": [round(float(v), 8) for v in mean_embedding.tolist()],
            "modelName": self.model_name,
            "validImageCount": len(embeddings),
            "message": "enrollment embedding created",
            "warnings": failures,
        }

    def verify(self, image_data: str, stored_embedding: List[float]) -> Dict[str, Any]:
        if not stored_embedding:
            raise ValueError("storedEmbedding is required")
        result = self.engine.extract(decode_image(image_data))
        if result.embedding is None:
            return {
                "success": False,
                "verified": False,
                "similarity": 0.0,
                "threshold": self.threshold,
                "modelName": self.model_name,
                "message": result.message,
            }
        similarity = cosine_similarity(result.embedding.tolist(), stored_embedding)
        verified = bool(similarity >= self.threshold)
        return {
            "success": True,
            "verified": verified,
            "similarity": round(float(similarity), 6),
            "threshold": self.threshold,
            "modelName": self.model_name,
            "message": "verified" if verified else "not verified",
        }
