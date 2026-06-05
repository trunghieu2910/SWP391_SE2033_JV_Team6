# Pipeline Overview

## Project Structure & Workflow

```
D:/Subject/4_SWP391/TS-47-AI-demo-v1/myoma-detection-yolo/
├─ data_pipeline/
│   ├─ step1_audit/          # analyze_image_processing_complexity.py – audit raw images
│   ├─ step2_clean/          # convert_voc_to_yolo_v2.py – clean & convert, split dataset
│   └─ dataset_v2.yaml        # dataset configuration for YOLO
├─ training/
│   └─ colab_training.ipynb   # train model (YOLOv5/YOLOv8) on Google Colab
├─ deployment/
│   └─ test_app.py            # Flask inference app (upload image → prediction)
├─ models/
│   └─ train_1/
│       └─ best.pt            # best trained weights
├─ results/
│   └─ train_1/
│       ├─ charts/            # loss / mAP / confusion matrix charts
│       └─ results.csv        # numeric metrics per epoch
└─ docs/                      # ← **this folder** – documentation files
```

### 1️⃣ Step 1 – **Audit**
*Script:* `analyze_image_processing_complexity.py`
- Đọc toàn bộ ảnh gốc (`0_Dataset_raw`).
- Thống kê kích thước, kênh màu, độ phân giải và những ảnh bị lỗi.
- Kết quả: CSV/MD báo cáo giúp quyết định cách tiền xử lý.

### 2️⃣ Step 2 – **Clean & Split**
*Script:* `convert_voc_to_yolo_v2.py`
- Loại bỏ ảnh lỗi, chuẩn hoá kích thước, chuyển format VOC → YOLO.
- Split dataset thành **train / val / test** và tạo file `train.txt`, `val.txt`.
- Tạo `results.csv` chứa các thông số (số ảnh, số lớp, …).

### 3️⃣ Step 3 – **Training**
*Notebook:* `colab_training.ipynb`
- Sử dụng **YOLOv5/YOLOv8** trên Google Colab.
- Huấn luyện *n* epoch, lưu `best.pt` và `best.onnx`.
- Tự động sinh các biểu đồ **Loss, mAP, Precision, Recall, Confusion Matrix** trong `results/train_1/charts/`.
- Export `results.csv` (loss, mAP per epoch).

### 4️⃣ Step 4 – **Inference / Deployment**
*Script:* `test_app.py`
- Flask web server (`localhost:5000`).
- Người dùng upload ảnh → model dự đoán → trả về bounding‑box + nhãn.
- Sử dụng trọng số `best.pt` đã training.

### 5️⃣ Step 5 – **Reporting**
- Các file markdown trong **docs/** (README, pipeline_overview, github_structure, báo cáo) mô tả chi tiết quy trình và kết quả.
- Các file ảnh trong `results/train_1/charts/` và `results.csv` được đưa vào báo cáo.

---

**How to use the pipeline**
1. Clone the repository.
2. Run `step1_audit/analyze_image_processing_complexity.py` on the raw dataset.
3. Run `step2_clean/convert_voc_to_yolo_v2.py` to obtain a clean YOLO‑formatted dataset.
4. Open `colab_training.ipynb` in Google Colab and execute all cells.
5. Start the Flask server with `python test_app.py` to serve the inference API.
6. Consult the charts in `results/train_1/charts/` and the CSV for model performance.

---

*This file was generated automatically to serve as a quick reference for anyone reviewing the repository.*
