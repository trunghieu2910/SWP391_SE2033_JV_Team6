
Đây là repository chứa toàn bộ mã nguồn AI cho dự án **Nhận diện U xơ tử cung (Myoma Detection)** thuộc môn học SWP391. Dự án sử dụng mô hình học sâu **YOLO (You Only Look Once)** để phát hiện và khoanh vùng vị trí khối u trên hình ảnh siêu âm y tế.
## 📁 Cấu trúc thư mục
Dự án được chia thành các phân hệ rõ ràng để dễ quản lý:
```text
myoma-detection-yolo/
├── data_pipeline/      # Các script xử lý dữ liệu trước khi train
│   ├── step1_audit/    # Code thống kê, kiểm tra lỗi ảnh gốc
│   ├── step2_clean/    # Code làm sạch, chia tập train/val, chuyển đổi VOC sang YOLO
│   └── dataset_v2.yaml # File cấu hình dataset cho YOLO
├── training/           # Notebook dùng để huấn luyện mô hình
│   └── colab_training.ipynb  # Chạy trên Google Colab
├── deployment/         # Web App Demo dùng để test AI
│   └── test_app.py     # Source code Flask server
├── models/             # Chứa trọng số mô hình đã được train xong
│   └── train_1/        # Trọng số của lần train tốt nhất (best.pt)
├── results/            # Kết quả và chỉ số đánh giá của mô hình
│   └── train_1/        # Các biểu đồ Loss, mAP, Confusion Matrix
└── docs/               # Tài liệu chi tiết của dự án
```
## 🚀 Hướng dẫn sử dụng
### 1. Tiền xử lý dữ liệu (Data Pipeline)
Nếu bạn muốn tự chạy lại quy trình làm sạch dữ liệu từ đầu:
1. Chạy `data_pipeline/step1_audit/analyze_image_processing_complexity.py` để quét ảnh lỗi.
2. Chạy `data_pipeline/step2_clean/convert_voc_to_yolo_v2.py` để chuẩn hóa ảnh và tạo nhãn YOLO.
### 2. Huấn luyện mô hình (Training)
Mô hình được huấn luyện trên **Google Colab** để tận dụng GPU:
1. Upload thư mục `myoma-detection-yolo` lên Google Drive.
2. Mở file `training/colab_training.ipynb` bằng Google Colab.
3. Chạy lần lượt các cell trong notebook để tiến hành train.
4. Trọng số tốt nhất sau khi train sẽ được lưu lại (tương tự như file `best.pt` trong thư mục `models/`).
### 3. Chạy Web App Demo (Inference)
Để chạy thử giao diện dự đoán trên máy cá nhân:
1. Cài đặt các thư viện cần thiết:
   ```bash
   pip install ultralytics flask werkzeug
   ```
2. Di chuyển vào thư mục dự án và chạy server:
   ```bash
   cd deployment
   python test_app.py
   ```
3. Mở trình duyệt và truy cập `http://localhost:5000` để upload ảnh và xem AI dự đoán.
## 📊 Kết quả huấn luyện
Mô hình hiện tại (Train 1) đạt kết quả rất khả quan trên tập Validation:
- **mAP50**: ~99%
- **Precision**: ~98%
- **Recall**: ~97%
Chi tiết biểu đồ xem thêm trong thư mục `results/train_1/charts/`.
---
