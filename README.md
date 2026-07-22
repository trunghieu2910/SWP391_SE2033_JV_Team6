# SWP391_SE2033_JV_Team6
Uterine Cancer Diagnosis Support System

Hệ thống được tích hợp trí tuệ nhân tạo (AI - YOLOv8) để hỗ trợ chẩn đoán u xơ tử cung thông qua hình ảnh siêu âm. Dự án bao gồm 2 thành phần chính hoạt động song song: **Java Spring Boot (Web Backend)** và **Python FastAPI (AI Service)**.

---

## 🛠 Hướng dẫn Cài đặt & Khởi động Hệ thống

Để toàn bộ quy trình tải ảnh và phân tích AI hoạt động, bạn cần khởi động cả 2 server cùng lúc (mỗi server chạy trên một cửa sổ Terminal/IDE riêng).

### 1. Khởi động AI Service (Python - Cổng 5000)

Thành phần này dùng để chạy mô hình nhận diện ảnh YOLOv8. Đảm bảo bạn đã đưa thư mục `ai_service` và thư mục `weights` (chứa `best.pt`) vào trong cùng thư mục gốc của project (nằm ngang hàng với thư mục `backend` của Java).

**Các bước chạy:**
1. Mở Terminal (Command Prompt / PowerShell) tại thư mục `ai_service`.
2. Lần đầu tiên chạy, bạn cần cài đặt các thư viện cần thiết:
   ```bash
   pip install -r requirements.txt
   ```
3. Sau khi cài xong, khởi động server AI bằng lệnh:
   ```bash
   python app.py
   ```
4. Khi thấy dòng chữ `Application startup complete` hiện ra, server AI đã sẵn sàng nhận ảnh. Đừng tắt cửa sổ Terminal này đi.

### 2. Khởi động Web Server (Java Spring Boot - Cổng 8080)

1. Mở project trong IDE của bạn (IntelliJ IDEA / Eclipse).
2. Chắc chắn rằng Database SQL Server đã bật và cấu hình chuỗi kết nối trong `application.properties` là chính xác.
3. Chạy (Run) class chứa hàm `main` của Spring Boot (thường là `BackendApplication.java`).
4. Nếu báo thành công, web server đã chạy trên cổng `8080`.

---

## 🧪 Hướng dẫn Test Luồng (Quy trình thực tế)

Hệ thống được thiết kế theo đúng quy trình thực tế của bệnh viện: Bác sĩ siêu âm đẩy ảnh lên hệ thống -> AI tự động phân tích -> Bác sĩ xem kết quả ở trang bệnh án.

### Bước 1: Bác sĩ siêu âm (Upload ảnh)
Màn hình này giả lập màn hình của một chiếc máy siêu âm. 
- **Link truy cập:** 👉 [http://localhost:8080/ultrasound-simulator.html](http://localhost:8080/ultrasound-simulator.html)
- **Cách test:**
  1. Nhập **Session ID** (Mã phiên khám, ví dụ: `1`). Đảm bảo ID này có thật trong Database.
  2. Bấm vào khung upload để chọn một bức ảnh siêu âm `.jpg` hoặc `.png`.
  3. Bấm **"Truyền Dữ Liệu Lên Hệ Thống"**.
  4. Hệ thống sẽ tự động gửi ảnh này qua cho Python (AI). Nếu bạn mở Terminal của Python lên sẽ thấy log hệ thống đang vẽ khung đỏ báo hiệu u xơ. Cuối cùng, Java nhận kết quả và lưu toàn bộ vào Database.

### Bước 2: Bác sĩ (Xem kết quả Bệnh án)
Sau khi bác sĩ siêu âm upload xong, bác sĩ sẽ xem ảnh và độ tin cậy của AI đưa ra.
- **Link truy cập:** 👉 `http://localhost:8080/doctor/medical-records/{id}` (Thay `{id}` bằng Session ID, ví dụ: [http://localhost:8080/doctor/medical-records/1](http://localhost:8080/doctor/medical-records/1))
- *(Lưu ý: Bác sĩ cần phải Login trước nếu hệ thống yêu cầu)*.
- **Cách test:**
  1. Cuộn trang xuống phần **"Hình Ảnh Y Khoa"**.
  2. Bác sĩ sẽ thấy bức ảnh mà bác sĩ siêu âm vừa đẩy lên.
  3. Bấm vào nút màu xanh **"Phân tích AI"** nằm dưới bức ảnh.
  4. Bác sĩ sẽ được chuyển sang giao diện HTML mới chia đôi màn hình: Một bên là ảnh siêu âm gốc, một bên là ảnh AI đã khoanh đỏ vùng u xơ kèm theo tỷ lệ chính xác (Confidence Score).
  5. Nếu muốn truy cập thẳng trang chia đôi ảnh AI, có thể dùng link: 👉 `http://localhost:8080/ai-result.html?imageId={imageId}` (Thay `{imageId}` bằng ID của tấm ảnh đó trong DB).

---
*Lưu ý: Nếu bị lỗi "Quá nhiều yêu cầu", hãy đợi 1 phút (tính năng chặn DDoS của hệ thống sẽ tự động gỡ khóa).*
