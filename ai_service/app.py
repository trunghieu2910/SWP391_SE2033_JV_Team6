import io
import os
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse, Response
from ultralytics import YOLO
import cv2
import numpy as np
import pydicom

app = FastAPI()

# Đường dẫn trỏ tới thư mục weights nằm bên ngoài ai_service
model_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'weights', 'best.pt'))

print(f"Loading model from: {model_path}")
try:
    model = YOLO(model_path)
except Exception as e:
    print(f"Lỗi khi load model: {e}")
    model = None

def validate_image(image_np: np.ndarray):
    """
    Thực hiện kiểm tra chất lượng ảnh đầu vào:
    1. Kích thước tối thiểu: 300x300 px
    2. Độ sắc nét (phương sai Laplacian) phải nằm trong khoảng hợp lý [50.0, 2500.0]
       - Nhỏ hơn 50.0: Ảnh bị mờ.
       - Lớn hơn 2500.0: Ảnh chứa đồ họa máy tính quá sắc nét (như ảnh chụp màn hình UI).
    3. Phù hợp ảnh y tế (grayscale >= 75% và nền tối >= 5%)
    4. Không chứa quá nhiều đường kẻ thẳng nhân tạo (UI borders, tables)
    """
    height, width = image_np.shape[:2]
    min_w, min_h = 300, 300
    if width < min_w or height < min_h:
        return False, f"Kích thước ảnh quá nhỏ ({width}x{height}). Kích thước tối thiểu phải là {min_w}x{min_h} pixels."
        
    gray = cv2.cvtColor(image_np, cv2.COLOR_BGR2GRAY)
    
    # Kiểm tra ảnh bị mờ hoặc chứa đồ họa máy tính siêu sắc nét
    laplacian_var = cv2.Laplacian(gray, cv2.CV_64F).var()
    if laplacian_var < 50.0:
        return False, f"Ảnh quá mờ (độ sắc nét {laplacian_var:.1f} < 50.0). Vui lòng cung cấp ảnh rõ nét hơn."
    if laplacian_var > 2500.0:
        return False, "Ảnh không phải ảnh y tế phù hợp (Ảnh chứa các chi tiết đồ họa máy tính quá sắc nét, nghi ngờ là ảnh chụp màn hình)."
        
    # Tính độ sắc nét vùng trung tâm (tránh bị ảnh hưởng bởi khung viền sắc nét hoặc chữ chú thích ở rìa)
    h, w = gray.shape
    cy, cx = h // 2, w // 2
    dy, dx = int(h * 0.3), int(w * 0.3)
    crop = gray[cy-dy:cy+dy, cx-dx:cx+dx]
    crop_var = cv2.Laplacian(crop, cv2.CV_64F).var()
    if crop_var < 50.0:
        return False, f"Ảnh quá mờ ở vùng trung tâm (độ sắc nét trung tâm {crop_var:.1f} < 50.0). Vui lòng cung cấp ảnh rõ nét hơn."

        
    # Kiểm tra ảnh y tế phù hợp (grayscale và nền tối)
    hsv = cv2.cvtColor(image_np, cv2.COLOR_BGR2HSV)
    s = hsv[:, :, 1]
    grayscale_pixels = np.sum(s < 30)
    total_pixels = s.size
    grayscale_ratio = (grayscale_pixels / total_pixels) * 100
    
    dark_pixels = np.sum(gray < 30)
    dark_ratio = (dark_pixels / total_pixels) * 100
    
    if grayscale_ratio < 75.0:
        return False, f"Ảnh không phải ảnh y tế phù hợp (Ảnh chứa quá nhiều màu sắc: {100-grayscale_ratio:.1f}% màu sắc, không giống ảnh siêu âm/y tế)."
        
    if dark_ratio < 5.0:
        return False, f"Ảnh không phải ảnh y tế phù hợp (Thiếu vùng tối nền đặc trưng của ảnh siêu âm/y tế: {dark_ratio:.1f}% < 5.0%)."
        
    # Kiểm tra đường kẻ thẳng nhân tạo (UI grid) bằng HoughLinesP
    edges = cv2.Canny(gray, 50, 150, apertureSize=3)
    lines = cv2.HoughLinesP(edges, 1, np.pi/180, threshold=80, minLineLength=60, maxLineGap=5)
    
    h_lines = 0
    v_lines = 0
    if lines is not None:
        for line in lines:
            coords = line.ravel()
            if len(coords) == 4:
                x1, y1, x2, y2 = coords
                if abs(y1 - y2) <= 2:
                    h_lines += 1
                elif abs(x1 - x2) <= 2:
                    v_lines += 1
                    
    total_straight_lines = h_lines + v_lines
    if total_straight_lines > 50:
        return False, f"Ảnh không phải ảnh y tế phù hợp (Phát hiện quá nhiều đường kẻ thẳng nhân tạo: {total_straight_lines} đường, nghi ngờ là ảnh chụp màn hình UI)."
        
    return True, "OK"


@app.get("/")
def read_root():
    return {"status": "AI Service is running", "model_loaded": model is not None}

@app.post("/convert-dicom")
async def convert_dicom(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        
        # Kiểm tra file DICOM
        is_dicom = (
            file.filename.lower().endswith(('.dcm', '.dicom')) or 
            (len(contents) > 132 and contents[128:132] == b'DICM')
        )
        if not is_dicom:
            return JSONResponse(status_code=400, content={"error": "File không phải định dạng DICOM hợp lệ."})
            
        dcm = pydicom.dcmread(io.BytesIO(contents))
        pixel_array = dcm.pixel_array
        
        if pixel_array.max() == pixel_array.min():
            image_cv = np.zeros((pixel_array.shape[0], pixel_array.shape[1], 3), dtype=np.uint8)
        else:
            img_normalized = ((pixel_array - pixel_array.min()) / (pixel_array.max() - pixel_array.min()) * 255.0).astype(np.uint8)
            
        if len(img_normalized.shape) == 2:
            image_cv = cv2.cvtColor(img_normalized, cv2.COLOR_GRAY2BGR)
        else:
            image_cv = img_normalized
            
        # Kiểm tra chất lượng ảnh sau khi chuyển đổi
        is_valid, msg = validate_image(image_cv)
        if not is_valid:
            return JSONResponse(status_code=400, content={"error": msg})
            
        is_success, buffer = cv2.imencode(".jpg", image_cv)
        if not is_success:
            return JSONResponse(status_code=500, content={"error": "Không thể xuất ảnh JPEG từ DICOM."})
            
        return Response(content=buffer.tobytes(), media_type="image/jpeg")
    except Exception as e:
        return JSONResponse(status_code=400, content={"error": f"Lỗi xử lý file DICOM: {str(e)}"})

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if model is None:
        return JSONResponse(status_code=500, content={"error": "Model not loaded. Check path to best.pt"})
    
    try:
        # Đọc ảnh từ request
        contents = await file.read()
        
        # Check if DICOM
        is_dicom = (
            file.filename.lower().endswith(('.dcm', '.dicom')) or 
            (len(contents) > 132 and contents[128:132] == b'DICM')
        )
        
        if is_dicom:
            try:
                dcm = pydicom.dcmread(io.BytesIO(contents))
                pixel_array = dcm.pixel_array
                if pixel_array.max() == pixel_array.min():
                    image_cv = np.zeros((pixel_array.shape[0], pixel_array.shape[1], 3), dtype=np.uint8)
                else:
                    img_normalized = ((pixel_array - pixel_array.min()) / (pixel_array.max() - pixel_array.min()) * 255.0).astype(np.uint8)
                if len(img_normalized.shape) == 2:
                    image_cv = cv2.cvtColor(img_normalized, cv2.COLOR_GRAY2BGR)
                else:
                    image_cv = img_normalized
            except Exception as e:
                return JSONResponse(status_code=400, content={"error": f"Không thể đọc file DICOM: {str(e)}"})
        else:
            image_cv = cv2.imdecode(np.frombuffer(contents, np.uint8), cv2.IMREAD_COLOR)
            if image_cv is None:
                return JSONResponse(status_code=400, content={"error": "Không thể giải mã file ảnh. Chỉ chấp nhận các định dạng JPG, PNG hoặc DICOM."})
                
        # Thực hiện kiểm tra chất lượng ảnh
        is_valid, msg = validate_image(image_cv)
        if not is_valid:
            return JSONResponse(status_code=400, content={"error": msg})
            
        # Chạy mô hình YOLOv8
        results = model(image_cv)
        
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

