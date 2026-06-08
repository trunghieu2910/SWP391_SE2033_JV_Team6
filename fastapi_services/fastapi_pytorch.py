"""
FastAPI Service - PyTorch (.pt)
================================
Deploy YOLO26s-seg model bằng PyTorch native.
Dùng cho máy có CUDA GPU (NVIDIA) hoặc CPU.

Chạy server:
    uvicorn fastapi_pytorch:app --host 0.0.0.0 --port 8000

Test nhanh:
    curl -X POST http://localhost:8000/predict \
         -F "file=@ultrasound_image.jpg"

Spring Boot gọi endpoint:
    POST http://localhost:8000/predict  (multipart/form-data, field "file")
    POST http://localhost:8000/predict/path (JSON body: {"image_path": "..."})
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
MODEL_PATH = r"D:\Hieu\Project_SWP391\YOLO26s-seg\best.pt"  # TODO: Thay đổi đường dẫn thực tế
DEFAULT_CONF = 0.25

# ========================== BIẾN TOÀN CỤC ==========================
model = None


# ========================== LIFESPAN (Load model 1 lần duy nhất) ==========================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load model vào RAM/VRAM khi server khởi động, giải phóng khi tắt."""
    global model
    from ultralytics import YOLO

    print(f"🔄 Đang nạp model PyTorch từ: {MODEL_PATH}")
    if not os.path.exists(MODEL_PATH):
        raise FileNotFoundError(f"Không tìm thấy model: {MODEL_PATH}")

    model = YOLO(MODEL_PATH)

    # Warmup: chạy 1 lần với ảnh giả để khởi tạo CUDA context
    dummy = np.zeros((640, 640, 3), dtype=np.uint8)
    _ = model(dummy, conf=0.99, verbose=False)
    print(f"✅ Model PyTorch đã sẵn sàng! (Device: {model.device})")

    yield  # Server đang chạy

    # Cleanup khi shutdown
    model = None
    print("🛑 Server đã tắt, giải phóng model.")


# ========================== KHỞI TẠO APP ==========================
app = FastAPI(
    title="YOLO26s-seg | PyTorch API",
    description="API phát hiện u xơ tử cung (Myoma) từ ảnh siêu âm - Backend: PyTorch",
    version="1.0.0",
    lifespan=lifespan,
)


# ========================== SCHEMAS ==========================
class ImagePathRequest(BaseModel):
    """Schema cho request gửi đường dẫn ảnh."""
    image_path: str
    confidence: float = DEFAULT_CONF


class DetectionResult(BaseModel):
    """Schema cho 1 detection (1 vùng phát hiện)."""
    class_id: int
    class_name: str
    confidence: float
    bbox: list[float]           # [x1, y1, x2, y2]
    mask_polygon: list | None   # Segmentation polygon points (nếu có)


class PredictionResponse(BaseModel):
    """Schema cho response trả về Spring Boot."""
    success: bool
    backend: str
    inference_time_ms: float
    image_width: int
    image_height: int
    num_detections: int
    detections: list[DetectionResult]
    annotated_image_base64: str | None = None  # Ảnh đã vẽ bbox, encode base64


# ========================== HELPER ==========================
def process_results(results, inference_ms: float, img_shape: tuple) -> dict:
    """
    Chuyển đổi kết quả YOLO thành dict chuẩn để trả về Spring Boot.

    Args:
        results: Kết quả từ model(...)
        inference_ms: Thời gian inference (ms)
        img_shape: (height, width) của ảnh đã tiền xử lý

    Returns:
        dict: Response data
    """
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

            # Trích xuất mask polygon nếu model là segmentation
            if result.masks is not None and i < len(result.masks):
                mask_xy = result.masks[i].xy
                if len(mask_xy) > 0:
                    det["mask_polygon"] = [[round(float(x), 2), round(float(y), 2)] for x, y in mask_xy[0]]

            detections.append(det)

    # Render ảnh annotated và encode base64
    annotated_img = result.plot()
    _, buffer = cv2.imencode(".jpg", annotated_img)
    img_base64 = base64.b64encode(buffer).decode("utf-8")

    return {
        "success": True,
        "backend": "PyTorch (.pt)",
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
    """Health check endpoint - Spring Boot dùng để kiểm tra service còn sống."""
    return {
        "status": "healthy",
        "backend": "PyTorch (.pt)",
        "model_loaded": model is not None,
    }


@app.post("/predict", response_model=PredictionResponse)
async def predict_upload(
    file: UploadFile = File(..., description="File ảnh siêu âm (.jpg, .png)"),
    confidence: float = DEFAULT_CONF,
):
    """
    Nhận ảnh upload từ Spring Boot, chạy AI inference, trả kết quả JSON.

    - **file**: File ảnh siêu âm (multipart/form-data)
    - **confidence**: Ngưỡng confidence (mặc định 0.25)
    """
    # Validate file type
    if file.content_type not in ["image/jpeg", "image/png", "image/bmp", "image/tiff"]:
        raise HTTPException(status_code=400, detail=f"Định dạng ảnh không hỗ trợ: {file.content_type}")

    try:
        # Đọc file bytes
        image_bytes = await file.read()

        # Tiền xử lý ảnh siêu âm
        preprocessed = preprocess_ultrasound_from_bytes(image_bytes)

        # Chạy inference
        t0 = time.perf_counter()
        results = model(preprocessed, conf=confidence, verbose=False)
        inference_ms = (time.perf_counter() - t0) * 1000

        # Trả kết quả
        return process_results(results, inference_ms, preprocessed.shape[:2])

    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý AI: {str(e)}")


@app.post("/predict/path", response_model=PredictionResponse)
def predict_path(request: ImagePathRequest):
    """
    Nhận đường dẫn ảnh từ Spring Boot (ảnh đã lưu trên server), chạy AI inference.

    - **image_path**: Đường dẫn tuyệt đối tới file ảnh trên server
    - **confidence**: Ngưỡng confidence (mặc định 0.25)
    """
    if not os.path.exists(request.image_path):
        raise HTTPException(status_code=404, detail=f"Không tìm thấy ảnh: {request.image_path}")

    try:
        # Tiền xử lý ảnh siêu âm
        preprocessed = preprocess_ultrasound_from_path(request.image_path)

        # Chạy inference
        t0 = time.perf_counter()
        results = model(preprocessed, conf=request.confidence, verbose=False)
        inference_ms = (time.perf_counter() - t0) * 1000

        # Trả kết quả
        return process_results(results, inference_ms, preprocessed.shape[:2])

    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý AI: {str(e)}")


# ========================== CHẠY TRỰC TIẾP ==========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("fastapi_pytorch:app", host="0.0.0.0", port=8000, reload=False)
