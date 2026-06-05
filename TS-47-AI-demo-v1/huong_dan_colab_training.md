# 🚀 Hướng Dẫn Từng Bước Chạy YOLO26n Trên Google Colab Free (Autosave & Resume)

Tài liệu này hướng dẫn bạn cách thiết lập môi trường nội bộ bằng Miniconda, chuẩn bị dữ liệu (3 tập Train/Val/Test), và huấn luyện mô hình phát hiện U xơ tử cung (**YOLO26n**) trên **Google Colab Free (GPU T4)** một cách tối ưu.

---

## 💻 1. Setup Môi Trường Ban Đầu (Local PC) Bằng Miniconda

Để xử lý script chia dữ liệu một cách mượt mà và cô lập môi trường trên máy tính của bạn, chúng ta sẽ sử dụng **Miniconda** với **Python 3.10** (phiên bản ổn định nhất cho YOLO và AI hiện tại).

### Bước 1: Cài đặt Miniconda
- Tải Miniconda từ trang chủ: [Miniconda Download](https://docs.conda.io/en/latest/miniconda.html) và cài đặt theo các bước mặc định.
- Mở **Anaconda Prompt** (hoặc Miniconda Prompt) trên Windows.

### Bước 2: Tạo môi trường ảo (Virtual Environment)
Gõ lần lượt các lệnh sau vào terminal:
```bash
# Tạo môi trường tên là yolo26n với Python 3.10
conda create -n yolo26n python=3.10 -y

# Kích hoạt môi trường vừa tạo
conda activate yolo26n

# Cài đặt thư viện Ultralytics (hỗ trợ tính toán và YOLO)
pip install ultralytics
```

---

## 📁 2. Chuẩn Bị & Chia Dữ Liệu (Train / Validate / Final Test)

Tôi đã nâng cấp file mã nguồn `convert_voc_to_yolo.py` để tự động đọc hình ảnh từ **JPEGImages** và phân chia dữ liệu thành **3 tập hoàn chỉnh**:
- **Train (70%)**: Dùng để huấn luyện mô hình.
- **Validate (20%)**: Dùng để đánh giá chéo trong quá trình huấn luyện nhằm tránh overfitting.
- **Final Test (10%)**: Dùng để kiểm tra mô hình ở bước cuối cùng với các bức ảnh hoàn toàn mới (mô hình chưa từng nhìn thấy trong lúc train).

### Các bước thực hiện:
1. Đảm bảo bạn đã kích hoạt môi trường conda (`conda activate yolo26n`).
2. Điều hướng terminal đến thư mục chứa dự án (ví dụ `d:\SWP391\YOLO26n`).
3. Chạy lệnh:
   ```bash
   python convert_voc_to_yolo.py
   ```
4. Kịch bản sẽ tự động tạo ra thư mục `datasets/` với cấu trúc chuẩn:
   ```text
   datasets/
   ├── images/
   │   ├── train/    (70% ảnh)
   │   ├── val/      (20% ảnh)
   │   └── test/     (10% ảnh - Final Test)
   └── labels/
       ├── train/    
       ├── val/      
       └── test/     
   ```
5. Nén toàn bộ thư mục `datasets/` vừa được tạo thành file **`datasets.zip`**.
6. Tải tệp **`datasets.zip`** và tệp **`dataset.yaml`** lên thẳng thư mục gốc **Google Drive** của bạn (`MyDrive`).

---

## 📌 3. Nguyên Lý Tự Động Lưu (Autosave) Trên Colab Free

* **Hạn chế của Colab Free:** Trình duyệt bị tắt hoặc không có tương tác chuột/phím (Idle) trong một khoảng thời gian sẽ làm Colab ngắt kết nối. Ngoài ra, GPU T4 miễn phí cũng có giới hạn thời gian chạy tối đa (khoảng 4 - 8 tiếng), hết thời gian này Google sẽ chủ động ngắt kết nối (Absolute Timeout).
* **Mẹo chống ngắt kết nối do "Treo máy" (Idle Timeout):** 
  - Rất khuyến khích bạn cài đặt tiện ích mở rộng Chrome: **[Colab Keep Alive](https://chromewebstore.google.com/detail/colab-keepalive/kimcnbdidgbhaljnclmhlojklmnpgcpa)** hoặc **[Google Colab Keep Alive](https://chromewebstore.google.com/detail/google-colab-keep-alive/bokldcdphgknojlbfhpbbgkggjfhhaek)**. 
  - Khi đang train trên Colab, hãy bật extension này lên. Nó sẽ liên tục mô phỏng thao tác nhấp chuột/nhấn phím ngầm, giúp bạn có thể đi vắng hoặc đi ngủ mà tab không bị đóng do "treo máy".
  - *Lưu ý:* Extension này không thể chống lại giới hạn thời gian tối đa (4-8 tiếng) hoặc Captcha chống bot của Google. Vì vậy, cơ chế Autosave bên dưới vẫn bắt buộc phải có.
* **Cơ chế hoạt động tối ưu (Đã cấu hình trong `colab_training.ipynb`):**
  - **Dataset:** Giải nén thẳng vào bộ nhớ tạm của Colab để tăng tốc đọc ảnh tối đa.
  - **Trọng số & Logs:** Ghi lưu trực tiếp lên Google Drive (`project=DRIVE_RUNS_DIR`). Cứ sau mỗi 10 epochs hoặc mỗi vòng lặp, trọng số (`best.pt`, `last.pt`) được đẩy thẳng lên cloud. Nhờ vậy, ngay cả khi Google tự động "rút phích cắm" giữa chừng, tiến trình học **vẫn được bảo toàn trọn vẹn** trên Drive!

---

## 🛠️ 4. Tải Lên & Huấn Luyện Trên Google Colab

1. Vào [Google Colab](https://colab.research.google.com/), chọn **Upload** và tải lên file **`colab_training.ipynb`**.
2. Kích hoạt **GPU T4 Miễn Phí**: Chọn menu **Runtime** $\rightarrow$ **Change runtime type** $\rightarrow$ **T4 GPU** $\rightarrow$ **Save**.
3. Bấm **Play** chạy lần lượt các ô mã (Cells) từ trên xuống dưới:
   - **Cell 1:** Cài đặt Ultralytics.
   - **Cell 2:** Liên kết với Google Drive (Mount Drive).
   - **Cell 3:** Giải nén `datasets.zip` vào hệ thống tốc độ cao.
   - **Cell 4:** Nhận diện GPU T4.
   - **Cell 5 (Huấn luyện):** Tiến hành huấn luyện. 
     *👉 Làm sao AI biết lấy ảnh ở đâu? Khi chạy lệnh `model.train(data='dataset.yaml')`, hệ thống sẽ đọc file `dataset.yaml` của bạn để tìm đến đường dẫn `train: images/train` và lấy toàn bộ ảnh ở đó ra học. Nó cũng tự động lấy ảnh từ `val: images/val` ra để "thi thử" đánh giá độ chính xác.*
     Mọi nhật ký và checkpoint sẽ tự động "bơm" về Google Drive của bạn theo đường dẫn `yolo26n_myoma/runs/myoma_yolo26n`.

---

## ⏸️ 5. Cách Chủ Động Dừng Để Test Thử & Tiếp Tục (Resume)

### A. Cách dừng để kiểm tra (Test)
* Bấm nút **Stop** (ô vuông màu đỏ) bên cạnh Cell 5 đang chạy.
* Cuộn xuống **Mục 7️⃣ (Thử nghiệm dự đoán trên ảnh mới)** trong Colab. Ở đây, bạn có thể chỉnh sửa đường dẫn thành một bức ảnh trong tập `datasets/images/test/` (Final Test) để chạy thử nghiệm độ chính xác thực tế xem mô hình học tới đâu.

### B. Cách tiếp tục huấn luyện (Resume Training)
Nếu bạn tắt Colab và mở lại vào hôm sau để train tiếp từ Epoch đã dừng:
1. Chạy lại **Cell 1, 2, 3** để cài môi trường và Mount Drive.
2. Thêm một ô mã (Cell) mới phía dưới và gõ:

```python
from ultralytics import YOLO
import os

# 1. Đường dẫn thư mục runs được Autosave trên Drive
DRIVE_RUNS_DIR = "/content/drive/MyDrive/yolo26n_myoma/runs"

# 2. Load checkpoint gần nhất (last.pt)
model = YOLO(os.path.join(DRIVE_RUNS_DIR, "myoma_yolo26n/weights/last.pt"))

# 3. Resume!
model.train(resume=True)
```

🚀 Hệ thống sẽ tự động bắt lấy mạch cấu hình cũ và chạy nốt các Epochs còn thiếu hoàn toàn trơn tru.

---

## 📊 6. Đọc Kết Quả & Tinh Chỉnh Tham Số (Fine-tuning)

### A. Xem kết quả và trọng số tốt nhất ở đâu?
Khi quá trình huấn luyện diễn ra, YOLO sẽ liên tục đánh giá mô hình. Bạn có thể xem các thông số này ngay trên Google Drive:
- **`best.pt`**: File trọng số có điểm số đánh giá (mAP) cao nhất nằm trong thư mục `yolo26n_myoma/runs/myoma_yolo26n/weights/`. Đây là file bạn sẽ dùng để đem đi nhận diện ảnh thực tế sau này.
- **Biểu đồ trực quan**: Mở thư mục `yolo26n_myoma/runs/myoma_yolo26n/`, bạn sẽ thấy hàng loạt file ảnh như `results.png` (Biểu đồ Loss và mAP), `confusion_matrix.png` (Ma trận nhầm lẫn), và `val_batch0_pred.jpg` (Ảnh minh họa dự đoán thử). Nhìn vào `results.png`, nếu đường đồ thị Loss đang cắm đầu đi xuống và đường mAP đi lên đều đặn thì mô hình đang học rất tốt.

### B. Cách tinh chỉnh tham số (Fine-tuning)
Nếu bạn cảm thấy mô hình học chưa đủ tốt, bạn có thể quay lại Colab và sửa đổi các "siêu tham số" (Hyperparameters) trong hàm `model.train()` ở Cell 5:
- **`epochs=150`**: Số vòng lặp huấn luyện toàn bộ dữ liệu. Tăng lên 200 hoặc 300 nếu mô hình chưa "chín" (chưa hội tụ).
- **`batch=16`**: Số ảnh nhồi vào GPU mỗi lần. Tăng lên `32` nếu GPU T4 còn trống RAM (giúp train nhanh hơn), hoặc giảm xuống `8` nếu bị lỗi quá tải bộ nhớ (Out of Memory).
- **`lr0=0.01`**: (Learning rate) Tốc độ học ban đầu. Nếu bạn load file `best.pt` cũ lên để học tiếp (fine-tune thêm một chút), hãy thử giảm xuống `0.001` để mô hình học chậm và tỉ mỉ hơn, tránh phá hỏng các kiến thức cũ.
- **`imgsz=640`**: Kích thước ảnh nạp vào. Nếu khối u xơ của bạn rất nhỏ, có thể tăng lên `800` hoặc `1024` để nhìn rõ hơn (tuy nhiên sẽ train chậm hơn và tốn VRAM hơn).

*Ví dụ lệnh train với tham số tinh chỉnh để Fine-tune:*
```python
model.train(data='dataset.yaml', epochs=200, batch=16, imgsz=800, lr0=0.001)
```

---

## 🗑️ 7. Hướng Dẫn Xóa và Làm Lại Notebook Từ Đầu
Nếu bạn lỡ tay xóa nhầm ô code, thao tác sai, hoặc đơn giản muốn làm lại một phiên bản sạch sẽ từ đầu trên Colab:
1. Trong giao diện Colab, chọn menu **File** $\rightarrow$ **Locate in Drive** (Định vị trong Drive). Tab Google Drive sẽ mở ra.
2. Xóa file `colab_training.ipynb` cũ đi.
3. Vào lại trang web Colab, chọn tải lại file `colab_training.ipynb` sạch từ máy tính của bạn lên và chạy lại.

**Lưu ý: Nếu tạo Notebook mới nhưng dùng chung một thư mục trên Drive thì sao?**
- **Về việc Giải nén:** Nếu bạn chạy lại các ô lệnh trên một phiên làm việc (Runtime) chưa bị Google ngắt kết nối, lệnh ở Cell 3 sẽ phát hiện thư mục đã giải nén rồi và sẽ bỏ qua thao tác đó, không làm lại giúp tiết kiệm thời gian.
- **Về việc Lưu trọng số:** Bạn cứ yên tâm tạo bao nhiêu file Colab mới cũng được. Ultralytics YOLO rất thông minh, nếu phát hiện thư mục `myoma_yolo26n` đã tồn tại, nó sẽ không ghi đè, mà tự động tạo một thư mục mới tinh với số thứ tự tăng dần (ví dụ: `myoma_yolo26n2`, `myoma_yolo26n3`...) để lưu kết quả của lần train mới này. Toàn bộ tiến trình train trước đó của bạn vẫn an toàn tuyệt đối trên Drive!
