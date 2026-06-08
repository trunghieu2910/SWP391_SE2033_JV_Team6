"""
FastAPI Service - NCNN + Vulkan (folder model)
================================================
Deploy YOLO26s-seg model bằng NCNN + Vulkan GPU.
Chạy trên ĐA NỀN TẢNG - Windows, Linux, macOS, Android, iOS.
GPU: Vulkan API (Intel, AMD, NVIDIA, Mali, Adreno).

Chạy server:
    uvicorn fastapi_ncnn:app --host 0.0.0.0 --port 8006

Spring Boot gọi endpoint:
    POST http://localhost:8006/predict  (multipart/form-data, field "file")
    POST http://localhost:8006/predict/path (JSON body: {"image_path": "..."})
"""

import os
import io
import time
import base64
from contextlib import asynccontextmanager

import cv2
import numpy as np
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from preprocess import preprocess_ultrasound_from_bytes, preprocess_ultrasound_from_path

# ========================== CẤU HÌNH ==========================
MODEL_PATH = r"D:\Hieu\Project_SWP391\YOLO26s-seg\best_ncnn_model"  # TODO: Thay đổi đường dẫn thực tế (folder)
DEFAULT_CONF = 0.25

# ========================== BIẾN TOÀN CỤC ==========================
model = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load model NCNN khi server khởi động."""
    global model
    from ultralytics import YOLO

    print(f"🔄 Đang nạp model NCNN từ: {MODEL_PATH}")
    if not os.path.exists(MODEL_PATH) or not os.path.isdir(MODEL_PATH):
        raise FileNotFoundError(
            f"Không tìm thấy thư mục model NCNN: {MODEL_PATH}\n"
            f"Hãy chạy:\n"
            f"  from ultralytics import YOLO\n"
            f"  model = YOLO('best.pt')\n"
            f"  model.export(format='ncnn')"
        )

    model = YOLO(MODEL_PATH)

    # Warmup: Khởi tạo Vulkan context
    dummy = np.zeros((640, 640, 3), dtype=np.uint8)
    _ = model(dummy, conf=0.99, verbose=False)
    print(f"✅ Model NCNN + Vulkan đã sẵn sàng!")

    yield
    model = None
    print("🛑 Server đã tắt, giải phóng model.")


app = FastAPI(
    title="YOLO26s-seg | NCNN Vulkan API",
    description="API phát hiện u xơ tử cung (Myoma) từ ảnh siêu âm - Backend: NCNN + Vulkan (cross-platform)",
    version="1.0.0",
    lifespan=lifespan,
)


# ========================== SCHEMAS ==========================
class ImagePathRequest(BaseModel):
    image_path: str
    confidence: float = DEFAULT_CONF


class DetectionResult(BaseModel):
    class_id: int
    class_name: str
    confidence: float
    bbox: list[float]
    mask_polygon: list | None


class PredictionResponse(BaseModel):
    success: bool
    backend: str
    inference_time_ms: float
    image_width: int
    image_height: int
    num_detections: int
    detections: list[DetectionResult]
    annotated_image_base64: str | None = None


# ========================== HELPER ==========================
def process_results(results, inference_ms: float, img_shape: tuple) -> dict:
    """Chuyển đổi kết quả YOLO thành dict chuẩn."""
    detections = []
    result = results[0]

    if result.boxes is not None and len(result.boxes) > 0:
        for i, box in enumerate(result.boxes):
            det = {
                "class_id": int(box.cls[0]),
                "class_name": result.names[int(box.cls[0])],
                "confidence": round(float(box.conf[0]), 4),
                "bbox": [round(float(x), 2) for x in box.xyxy[0].tolist()],
                "mask_polygon": None,
            }
            if result.masks is not None and i < len(result.masks):
                mask_xy = result.masks[i].xy
                if len(mask_xy) > 0:
                    det["mask_polygon"] = [[round(float(x), 2), round(float(y), 2)] for x, y in mask_xy[0]]
            detections.append(det)

    annotated_img = result.plot()
    _, buffer = cv2.imencode(".jpg", annotated_img)
    img_base64 = base64.b64encode(buffer).decode("utf-8")

    return {
        "success": True,
        "backend": "NCNN + Vulkan (folder)",
        "inference_time_ms": round(inference_ms, 2),
        "image_width": img_shape[1],
        "image_height": img_shape[0],
        "num_detections": len(detections),
        "detections": detections,
        "annotated_image_base64": img_base64,
    }


# ========================== ENDPOINTS ==========================
@app.get("/health")
def health_check():
    return {"status": "healthy", "backend": "NCNN + Vulkan (folder)", "model_loaded": model is not None}


@app.post("/predict", response_model=PredictionResponse)
async def predict_upload(
    file: UploadFile = File(..., description="File ảnh siêu âm (.jpg, .png)"),
    confidence: float = DEFAULT_CONF,
):
    """Nhận ảnh upload từ Spring Boot, chạy NCNN Vulkan inference."""
    if file.content_type not in ["image/jpeg", "image/png", "image/bmp", "image/tiff"]:
        raise HTTPException(status_code=400, detail=f"Định dạng ảnh không hỗ trợ: {file.content_type}")

    try:
        image_bytes = await file.read()
        preprocessed = preprocess_ultrasound_from_bytes(image_bytes)

        t0 = time.perf_counter()
        results = model(preprocessed, conf=confidence, verbose=False)
        inference_ms = (time.perf_counter() - t0) * 1000

        return process_results(results, inference_ms, preprocessed.shape[:2])

    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý AI: {str(e)}")


@app.post("/predict/path", response_model=PredictionResponse)
def predict_path(request: ImagePathRequest):
    """Nhận đường dẫn ảnh từ Spring Boot, chạy NCNN Vulkan inference."""
    if not os.path.exists(request.image_path):
        raise HTTPException(status_code=404, detail=f"Không tìm thấy ảnh: {request.image_path}")

    try:
        preprocessed = preprocess_ultrasound_from_path(request.image_path)

        t0 = time.perf_counter()
        results = model(preprocessed, conf=request.confidence, verbose=False)
        inference_ms = (time.perf_counter() - t0) * 1000

        return process_results(results, inference_ms, preprocessed.shape[:2])

    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý AI: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("fastapi_ncnn:app", host="0.0.0.0", port=8006, reload=False)
