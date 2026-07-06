import io
import os
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse, Response
from ultralytics import YOLO
from PIL import Image
import cv2

app = FastAPI()

# Đường dẫn trỏ tới thư mục weights nằm bên ngoài ai_service
model_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'weights', 'best.pt'))

print(f"Loading model from: {model_path}")
try:
    model = YOLO(model_path)
except Exception as e:
    print(f"Lỗi khi load model: {e}")
    model = None

@app.get("/")
def read_root():
    return {"status": "AI Service is running", "model_loaded": model is not None}

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if model is None:
        return JSONResponse(status_code=500, content={"error": "Model not loaded. Check path to best.pt"})
    
    try:
        # Đọc ảnh từ request
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        
        # Chạy mô hình YOLOv8
        results = model(image)
        
        highest_conf = 0.0
        
        # Tìm độ tin cậy cao nhất trong các khung dự đoán (nếu có u xơ)
        for r in results:
            for box in r.boxes:
                conf = float(box.conf[0])
                if conf > highest_conf:
                    highest_conf = conf
                
        # Vẽ bounding box lên ảnh
        res_plotted = results[0].plot() # numpy array (BGR)
        
        # Chuyển đổi ảnh vẽ xong về dạng bytes JPG
        is_success, buffer = cv2.imencode(".jpg", res_plotted)
        if not is_success:
            return JSONResponse(status_code=500, content={"error": "Could not encode image"})
            
        image_bytes = buffer.tobytes()
        
        # Trả về file ảnh dạng binary và gửi kèm độ tin cậy qua HTTP Header
        headers = {
            "X-AI-Confidence": str(highest_conf)
        }
        
        return Response(content=image_bytes, media_type="image/jpeg", headers=headers)
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": str(e)})

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
