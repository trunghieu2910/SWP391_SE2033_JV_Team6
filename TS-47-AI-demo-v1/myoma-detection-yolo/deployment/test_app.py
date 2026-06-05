import os
import io
import cv2
import numpy as np
from PIL import Image
from flask import Flask, request, render_template_string, send_file
from ultralytics import YOLO

app = Flask(__name__)

# Tên file model bạn tải về từ Colab (để cùng thư mục với script này)
MODEL_PATH = "best.pt" 
try:
    model = YOLO(MODEL_PATH)
    print("✅ Đã load model thành công!")
except Exception as e:
    model = None
    print(f"❌ Lỗi load model: {e}")
    print(f"⚠️ Vui lòng copy file '{MODEL_PATH}' vào thư mục: {os.getcwd()}")

# ==========================================
# 1. HTML THUẦN SIÊU ĐƠN GIẢN ĐỂ TEST
# ==========================================
HTML_PAGE = """
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Test YOLO Myoma - Web Interface</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; margin: 0; padding: 40px; text-align: center; color: #333; }
        .container { max-width: 800px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        h2 { color: #2c3e50; }
        p.note { color: #e74c3c; font-size: 14px; font-weight: bold; background: #fadbd8; padding: 10px; border-radius: 6px; }
        input[type="file"] { margin: 20px 0; padding: 10px; border: 1px dashed #3498db; background: #ebf5fb; border-radius: 6px; width: 80%; cursor: pointer; }
        button { padding: 12px 25px; font-size: 16px; font-weight: bold; cursor: pointer; background: #27ae60; color: white; border: none; border-radius: 6px; transition: 0.3s; }
        button:hover { background: #2ecc71; }
        .result-img { margin-top: 30px; max-width: 100%; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.2); }
    </style>
</head>
<body>
    <div class="container">
        <h2>🩺 Công cụ Test Siêu Âm U Xơ (YOLO26n)</h2>
        <p class="note">⚠️ LƯU Ý: Ảnh upload lên sẽ tự động được Crop viền và chuyển sang Grayscale (Trắng Đen) trước khi AI dự đoán (Đúng chuẩn Pipeline đã thiết kế).</p>
        
        <form action="/predict" method="post" enctype="multipart/form-data">
            <input type="file" name="image" accept="image/*" required>
            <br>
            <button type="submit">Phân tích bằng AI</button>
        </form>
    </div>
</body>
</html>
"""

# ==========================================
# 2. HÀM TIỀN XỬ LÝ (QUAN TRỌNG NHẤT)
# ==========================================
def preprocess_for_inference(img_pil):
    """V11 Morphological: Crop UI + Top-Hat/Black-Hat + TELEA + Speckle Noise"""
    w, h = img_pil.size
    
    # Bước 1: Auto-Crop (Cắt 15% top, 5% bottom, 5% left, 10% right)
    img_cropped = img_pil.crop((int(w*0.05), int(h*0.15), int(w*0.90), int(h*0.95)))
    
    # Bước 2: Morphological Filtering
    cv_img = cv2.cvtColor(np.array(img_cropped), cv2.COLOR_RGB2BGR)
    gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    tophat = cv2.morphologyEx(gray, cv2.MORPH_TOPHAT, kernel)
    blackhat = cv2.morphologyEx(gray, cv2.MORPH_BLACKHAT, kernel)
    combined_morph = cv2.add(tophat, blackhat)

    _, mask = cv2.threshold(combined_morph, 35, 255, cv2.THRESH_BINARY)
    dilate_kernel = np.ones((3, 3), np.uint8)
    mask_dilated = cv2.dilate(mask, dilate_kernel, iterations=1)

    # Bước 3: Inpaint + Speckle Noise
    inpainted_img = cv2.inpaint(cv_img, mask_dilated, 3, cv2.INPAINT_TELEA)

    noise = np.zeros(inpainted_img.shape, np.int16)
    cv2.randn(noise, mean=0, stddev=8)
    noisy_inpainted = cv2.add(inpainted_img, noise, dtype=cv2.CV_8UC3)

    mask_3channel = cv2.cvtColor(mask_dilated, cv2.COLOR_GRAY2BGR) / 255.0
    final_healed = (noisy_inpainted * mask_3channel + inpainted_img * (1 - mask_3channel)).astype(np.uint8)

    # Bước 4: Grayscale
    gray_final = cv2.cvtColor(final_healed, cv2.COLOR_BGR2GRAY)
    final_rgb = cv2.cvtColor(gray_final, cv2.COLOR_GRAY2RGB)
    
    return Image.fromarray(final_rgb)

# ==========================================
# 3. ROUTING & INFERENCE
# ==========================================
@app.route('/', methods=['GET'])
def index():
    # Render giao diện HTML
    return render_template_string(HTML_PAGE)

@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files:
        return "Không có file ảnh", 400
        
    file = request.files['image']
    if file.filename == '':
        return "Chưa chọn file", 400
        
    if not model:
        return "Model best.pt chưa được load. Hãy kiểm tra lại file model.", 500

    try:
        # Đọc ảnh từ memory
        img_bytes = file.read()
        img_pil = Image.open(io.BytesIO(img_bytes)).convert("RGB")
        
        # BẮT BUỘC: Tiền xử lý (Crop + Grayscale)
        processed_img = preprocess_for_inference(img_pil)
        
        # YOLO DỰ ĐOÁN
        # conf=0.25: Chỉ lấy những box có độ tự tin > 25%
        results = model.predict(source=processed_img, conf=0.25)
        
        # Vẽ Bounding Box lên ảnh
        res_plotted = results[0].plot()
        
        # Chuyển OpenCV BGR về PIL RGB để xuất
        result_pil = Image.fromarray(res_plotted[..., ::-1])
        
        # Gửi ảnh thẳng về trình duyệt
        img_io = io.BytesIO()
        result_pil.save(img_io, 'JPEG', quality=95)
        img_io.seek(0)
        
        return send_file(img_io, mimetype='image/jpeg')

    except Exception as e:
        return f"Lỗi trong quá trình phân tích: {str(e)}", 500

if __name__ == '__main__':
    print("\n" + "="*50)
    print("🚀 ĐANG KHỞI ĐỘNG SERVER TEST YOLO")
    print("👉 Hãy mở trình duyệt và truy cập: http://127.0.0.1:5000")
    print("="*50 + "\n")
    # Chạy server
    app.run(host='127.0.0.1', port=5000, debug=False)
