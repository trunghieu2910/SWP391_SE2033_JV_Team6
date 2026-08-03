# Hệ thống Quản lý Phòng Khám Đa Khoa Chẩn Đoán Thông Minh (Cervical Dx / Smart Clinic)
## Tài liệu Yêu cầu Hệ thống (System Requirements)

---

## 1. Tổng quan hệ thống (Overview)
Hệ thống là một ứng dụng web quản lý phòng khám đa khoa, tích hợp trí tuệ nhân tạo (AI) hỗ trợ chẩn đoán (như phân tích siêu âm). Hệ thống quản lý toàn diện luồng bệnh nhân từ khâu tiếp đón đến khám bệnh, xét nghiệm cận lâm sàng, kê đơn và quản lý xuất/nhập kho thuốc. Hệ thống phục vụ nhiều đối tượng người dùng với phân quyền rõ ràng để đảm bảo quy trình vận hành bệnh viện trơn tru.

---

## 2. Các vai trò người dùng (User Roles)
Hệ thống phân chia thành 6 vai trò chính:
1. **Admin (Quản trị viên):** Quản lý toàn bộ hệ thống, nhân sự, tài khoản, và cấu hình.
2. **Receptionist (Lễ tân):** Tiếp đón bệnh nhân, quản lý tài khoản bệnh nhân, phân bổ ca khám dựa trên khối lượng công việc của bác sĩ.
3. **Doctor (Bác sĩ đa khoa/chuyên khoa):** Thực hiện khám bệnh, chẩn đoán, xem kết quả xét nghiệm/siêu âm, kê đơn thuốc và theo dõi bệnh án.
4. **Ultrasound Doctor (Bác sĩ siêu âm):** Quản lý chụp siêu âm, sử dụng hệ thống AI để hỗ trợ đưa ra kết quả chẩn đoán hình ảnh.
5. **Pharmacist (Dược sĩ):** Quản lý kho thuốc, danh mục thuốc, duyệt đơn thuốc và cấp phát thuốc cho bệnh nhân.
6. **Patient (Bệnh nhân):** Xem hồ sơ bệnh án cá nhân, lịch sử khám, đơn thuốc, và theo dõi nhắc nhở uống thuốc.

---

## 3. Yêu cầu chức năng chi tiết (Functional Requirements)

### 3.1. Phân hệ Xác thực & Bảo mật (Authentication & Security)
- **Đăng nhập/Đăng xuất:** Hỗ trợ đăng nhập bằng tài khoản nội bộ (Username/Password) và đăng nhập nhanh qua Google (OAuth2).
- **Quản lý mật khẩu:** Cấp lại mật khẩu (gửi mã OTP qua email), thay đổi mật khẩu.
- **Bảo mật & Phân quyền (Role-based access control):** Sử dụng Spring Security để bảo vệ các trang. Tự động điều hướng (redirect) người dùng đến đúng bảng điều khiển (dashboard) của role tương ứng sau khi đăng nhập thành công.
- **Giám sát hệ thống:** Ghi nhận lịch sử hoạt động hệ thống (System Log), lịch sử truy cập (Request Log), và có cơ chế chặn IP độc hại (Blocked IP).

### 3.2. Phân hệ Lễ tân (Receptionist Module)
- **Quản lý bệnh nhân:** Đăng ký tài khoản và tạo hồ sơ bệnh nhân mới (lưu trữ thông tin cá nhân, CCCD/CMND).
- **Quản lý luồng khám (Tạo ca khám):**
  - **Theo dõi Workload:** Xem số lượng ca đang chờ của từng bác sĩ để phân bổ đồng đều.
  - **Wizard Tạo ca khám:** Quy trình 4 bước (Step 1: Chọn bác sĩ -> Step 2: Tìm kiếm bệnh nhân -> Step 3: Nhập thông tin/chọn người nhập triệu chứng -> Step 4: Hoàn tất).
- **Quản lý thông tin cá nhân:** Xem và cập nhật hồ sơ (Profile) của bản thân.

### 3.3. Phân hệ Bác sĩ (Doctor Module)
- **Quản lý Danh sách ca khám:** Xem danh sách bệnh nhân đang chờ (Pending) và các ca đã hoàn thành (Completed).
- **Chẩn đoán & Khám bệnh:** 
  - Ghi nhận các triệu chứng lâm sàng và tiền sử bệnh.
  - Kết luận chẩn đoán bệnh, bao gồm ghi chú nội bộ (Private Note) và mã bệnh quốc tế (ICD-10).
- **Kê đơn thuốc (Prescription):**
  - Tra cứu thuốc từ danh mục có sẵn trong kho.
  - Kê định lượng, chỉ định liều dùng (sáng, trưa, chiều, tối), và số ngày sử dụng.
- **Cận lâm sàng (LIS & Hình ảnh):**
  - Tra cứu kết quả xét nghiệm máu/nước tiểu được đồng bộ từ hệ thống LIS.
  - Xem kết quả hình ảnh siêu âm và kết luận từ Bác sĩ siêu âm.
- **Hồ sơ bệnh án (Medical Records):**
  - Truy xuất lịch sử khám bệnh của bệnh nhân qua các lần đến khám.
  - **Xuất PDF:** Cho phép tải xuống Hồ sơ bệnh án bản đầy đủ (bao gồm mọi ghi chú và dữ liệu nhạy cảm).
- **Thống kê:** Xem thống kê hiệu suất khám bệnh (số lượng ca khám) theo thời gian.

### 3.4. Phân hệ Bác sĩ Siêu âm & Trí tuệ Nhân tạo (Ultrasound & AI Module)
- **Tiếp nhận chỉ định:** Xem danh sách các ca bệnh được bác sĩ lâm sàng chỉ định siêu âm.
- **Tích hợp AI chẩn đoán:**
  - Tải hình ảnh siêu âm/X-quang lên hệ thống.
  - AI phân tích hình ảnh và trả về kết quả dự đoán bệnh lý/bất thường.
- **Kết luận hình ảnh:** Bác sĩ siêu âm xem xét gợi ý của AI, chỉnh sửa và đưa ra kết luận cuối cùng để đồng bộ vào hồ sơ bệnh án của bệnh nhân.

### 3.5. Phân hệ Dược sĩ & Kho thuốc (Pharmacist Module)
- **Danh mục thuốc:** Phân loại và quản lý thông tin các loại thuốc (Drug, Drug Category, Drug Sub-Category).
- **Quản lý Lô/Date (Drug Batches & Inventory):** Quản lý nhập kho, hạn sử dụng (Date), số lượng tồn của từng lô.
- **Cấp phát thuốc:** Tiếp nhận đơn thuốc điện tử từ bác sĩ, tiến hành xuất kho và giao thuốc cho bệnh nhân.
- **Kiểm kê & Lịch sử:** Ghi nhận mọi biến động xuất/nhập kho (Inventory Logs).
- **Báo cáo Kho:** Hỗ trợ kết xuất báo cáo biến động kho, xuất nhập tồn dưới dạng PDF.

### 3.6. Phân hệ Bệnh nhân (Patient Module)
- **Hồ sơ sức khỏe:** Xem thông tin cá nhân và quản lý tài khoản.
- **Lịch sử y tế:** Theo dõi lịch sử bệnh án, xem chi tiết kết quả chẩn đoán, kết quả cận lâm sàng và đơn thuốc của các lần khám. 
- **Nhắc nhở uống thuốc (Medication Reminder):** Hệ thống tự động tạo lịch trình nhắc nhở uống thuốc dựa trên đơn thuốc bác sĩ kê.

### 3.7. Giao tiếp & Tích hợp (Integrations)
- **LIS Integration:** Hệ thống có API để nhận kết quả xét nghiệm tự động từ các máy móc/hệ thống xét nghiệm bên ngoài (Laboratory Information System).
- **Email Service:** Sử dụng SMTP để gửi mã OTP, thông báo tài khoản.
- **Export PDF:** Ứng dụng công nghệ OpenPDF (hỗ trợ phông chữ Tiếng Việt) để xuất báo cáo và hồ sơ linh hoạt trực tiếp trên bộ nhớ máy chủ.

---

## 4. Yêu cầu Phi chức năng (Non-Functional Requirements)

- **Giao diện & Trải nghiệm (UI/UX):** 
  - Giao diện thân thiện, sử dụng HTML/CSS (Thymeleaf template engine).
  - Áp dụng các chuẩn Semantic HTML, đảm bảo tối ưu SEO và Accessibility.
  - Responsive design: Hoạt động tốt trên cả máy tính
- **Mô hình Kiến trúc:** 
  - Backend sử dụng Java Spring Boot áp dụng kiến trúc MVC.
  - Giao tiếp giữa view và controller thông qua DTO (Data Transfer Objects) để đảm bảo bảo mật dữ liệu entity.
- **Cơ sở dữ liệu:**
  - Hệ quản trị CSDL quan hệ (như MySQL/SQL Server).
  - ORM thông qua Spring Data JPA & Hibernate để ánh xạ các thực thể phức tạp (Entity mapping).
- **Hiệu năng & Bảo mật:**
  - Xử lý các request form bằng CSRF Token.
  - Đảm bảo mã hóa mật khẩu bằng BCrypt trước khi lưu.
  - Hỗ trợ phân trang, tìm kiếm động (AJAX/Fetch) để xử lý dữ liệu lớn ở kho thuốc và danh sách bệnh nhân.
