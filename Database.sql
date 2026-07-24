USE master;
GO

-- =============================================
-- 1. LÀM SẠCH DATABASE CŨ (NẾU CÓ)
-- =============================================
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'MedicalDiagnosisDB')
BEGIN
    ALTER DATABASE MedicalDiagnosisDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE MedicalDiagnosisDB;
END
GO

-- Tạo Database
CREATE DATABASE MedicalDiagnosisDB;
GO
USE MedicalDiagnosisDB;
GO

-- ==========================================
-- 1. DANH MỤC CƠ BẢN & NGƯỜI DÙNG
-- ==========================================
CREATE TABLE [Role]
(
    roleID   INT IDENTITY (1,1) PRIMARY KEY,
    roleName NVARCHAR(50) NOT NULL
);

CREATE TABLE [Users]
(
    userID       INT IDENTITY (1,1) PRIMARY KEY,
    roleID       INT                NOT NULL,
    userName     VARCHAR(50) UNIQUE NOT NULL,
    fullName     NVARCHAR(100),
    email        VARCHAR(100) UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    phoneNumber  VARCHAR(20),
    certificateUrl VARCHAR(255),
    status       VARCHAR(20), -- ACTIVE , INACTIVE , BANNED
    lastChangePassTime DATETIME,
	lastLogoutTime DATETIME,
    createdAt    DATETIME     DEFAULT GETDATE(),
    nationalID   CHAR(12),
	failedLoginAttempts INT NOT NULL DEFAULT 0,
	lockedUntil DATETIME NULL,
    FOREIGN KEY (roleID) REFERENCES Role (roleID)
);

CREATE TABLE Patient
(
    patientID INT IDENTITY (1,1) PRIMARY KEY,
    gender    NVARCHAR(10) NOT NULL,
    dob       DATE NOT NULL,
    address   NVARCHAR(255) NOT NULL,
    userID    INT UNIQUE NOT NULL, -- Ràng buộc UNIQUE tạo ra quan hệ 1-1
    FOREIGN KEY (userID) REFERENCES [Users] (userID)
);

-- CHUẨN HÓA: Chỉ chứa danh mục các ô CHECKBOX (chọn nhiều) trên Form
CREATE TABLE Symptom
(
    symptomID   INT IDENTITY (1,1) PRIMARY KEY,
    symptomName NVARCHAR(100) NOT NULL
);

CREATE TABLE Parameter
(
    parameterID   INT IDENTITY (1,1) PRIMARY KEY,
    parameterName NVARCHAR(100) NOT NULL,
    unit          NVARCHAR(50)
);

-- ==========================================
-- 2. QUY TRÌNH CHẨN ĐOÁN & TRIỆU CHỨNG
-- ==========================================
CREATE TABLE DiagnosisSession
(
    sessionID INT IDENTITY (1,1) PRIMARY KEY,
    userID    INT NOT NULL, -- Bác sĩ/Người phụ trách
    patientID INT NOT NULL,
    weight    FLOAT,
    height    FLOAT,
    status    NVARCHAR(50),
    createdAt DATETIME DEFAULT GETDATE(),
    isShared  BIT DEFAULT 0,
    clinicalInputMode NVARCHAR(20) NULL,
    FOREIGN KEY (userID) REFERENCES [Users] (userID),
    FOREIGN KEY (patientID) REFERENCES Patient (patientID)
);

CREATE TABLE MedicationReminder
(
    reminderID INT IDENTITY (1,1) PRIMARY KEY,
    patientID  INT NOT NULL,
    note       NVARCHAR(500) NOT NULL,
    scheduledAt DATETIME NOT NULL,
    createdAt  DATETIME NOT NULL DEFAULT GETDATE(),
    sentAt     DATETIME NULL,
    status     NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (patientID) REFERENCES Patient (patientID)
);

-- CẬP NHẬT: Thêm 3 trường lưu trữ cố định (Radio button) trực tiếp từ Form
CREATE TABLE SymptomResult
(
    symptomResultID    INT IDENTITY (1,1) PRIMARY KEY,
    sessionID          INT UNIQUE NOT NULL, -- Đảm bảo quan hệ 1-1 nhờ UNIQUE
    status             NVARCHAR(50) DEFAULT 'PENDING',
    createdAt          DATETIME DEFAULT GETDATE(),
    menopauseStatus    NVARCHAR(50),        -- Ô chọn: Chưa mãn kinh / Đã mãn kinh
    symptomDuration    NVARCHAR(50),        -- Ô chọn: Dưới 1 tháng / 1-3 tháng / 3-6 tháng / Trên 6 tháng
    symptomProgressing BIT,                 -- Ô chọn: 1 (Có nặng dần) / 0 (Không)
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
);

CREATE TABLE SymptomDetails
(
    symptomDetailsID INT IDENTITY (1,1) PRIMARY KEY,
    symptomResultID  INT NOT NULL, 
    symptomID        INT NOT NULL,
    createdAt        DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (symptomResultID) REFERENCES SymptomResult (symptomResultID),
    FOREIGN KEY (symptomID) REFERENCES Symptom (symptomID)
);

-- ==========================================
-- 3. KẾT QUẢ XÉT NGHIỆM & HÌNH ẢNH
-- ==========================================
CREATE TABLE LabResult
(
    labResultID INT IDENTITY (1,1) PRIMARY KEY,
    sessionID   INT NOT NULL,
    testType    NVARCHAR(100),
    status      NVARCHAR(50) default 'PENDING', 
    createdAt   DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
);

-- 1. Thêm giá cho Phiếu Xét nghiệm (LabResult)
ALTER TABLE LabResult
ADD price DECIMAL(18,2) NOT NULL DEFAULT 0;


CREATE TABLE LabResultParameter
(
    LabResultParameterID INT IDENTITY (1,1) PRIMARY KEY,
    labResultID          INT NOT NULL,
    parameterID          INT NOT NULL,
    value                NVARCHAR(MAX),
    FOREIGN KEY (labResultID) REFERENCES LabResult (labResultID),
    FOREIGN KEY (parameterID) REFERENCES Parameter (parameterID)
);

CREATE TABLE MedicalImage
(
    medicalImageID INT IDENTITY (1,1) PRIMARY KEY,
    sessionID      INT NOT NULL,
    imageType      NVARCHAR(50),
    status         NVARCHAR(50) DEFAULT 'PENDING', 
    createdAt      DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
);

-- 2. Thêm giá cho Phiếu Siêu âm / Chụp chiếu (MedicalImage)
ALTER TABLE MedicalImage
ADD price DECIMAL(18,2) NOT NULL DEFAULT 0;

CREATE TABLE MedicalImageDetails (
    imageID INT PRIMARY KEY IDENTITY(1,1),
    medicalImageID INT NOT NULL,
    imageUrl NVARCHAR(255) NOT NULL,
    aiImageUrl NVARCHAR(255),
    confidenceScore FLOAT,
    technicalConclusion NVARCHAR(MAX),
    imgResultConclusion NVARCHAR(255),
    uploadedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (medicalImageID) REFERENCES MedicalImage (medicalImageID)
);

-- ==========================================
-- 4. REVIEW BÁC SĨ
-- ==========================================
CREATE TABLE DiseaseType (
    diseaseTypeID INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,

    CONSTRAINT UQ_DiseaseType_Name UNIQUE (name)
);

CREATE TABLE Review
(
    reviewID       INT IDENTITY (1,1) PRIMARY KEY,
    sessionID      INT UNIQUE NOT NULL,
    userID         INT        NOT NULL, 
    diseaseTypeID  INT		  NULL,
    treatmentPlan  NVARCHAR(MAX),
    doctorAdvice   NVARCHAR(MAX),
    note           NVARCHAR(MAX),
    reviewedAt     DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID),
    FOREIGN KEY (userID) REFERENCES [Users] (userID),
	FOREIGN KEY (diseaseTypeID) REFERENCES DiseaseType(diseaseTypeID)
);

-- ==========================================
-- 5. SYSTEM LOG
-- ==========================================
CREATE TABLE SystemLog
(
    logID       INT IDENTITY (1,1) PRIMARY KEY,
    userID      INT,
    targetType  VARCHAR(50), 
    targetID    INT, 
    action      VARCHAR(50),
    description NVARCHAR(MAX),
    performedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (userID) REFERENCES [Users] (userID)
);
GO

-- 1. Bảng lưu IP bị chặn
CREATE TABLE BlockedIP (
    ipAddress VARCHAR(45) NOT NULL PRIMARY KEY,
    reason NVARCHAR(255) NULL,
    createdAt DATETIME NOT NULL DEFAULT GETDATE(),
    createdBy NVARCHAR(100) NULL
);
GO

-- 2. Bảng lưu nhật ký truy cập
CREATE TABLE RequestLog (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ipAddress VARCHAR(45) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    userAgent VARCHAR(500) NULL,
    [timestamp] DATETIME NOT NULL DEFAULT GETDATE()
);
GO

--======================================================================================================================
-- PHẦN CHÈN DỮ LIỆU CẬP NHẬT
--======================================================================================================================

-- ROLE
INSERT INTO [Role] (roleName)
VALUES (N'ADMIN'), (N'DOCTOR'), (N'TECHNICAL'), (N'PATIENT'), (N'RECEPTIONIST'),(N'PHARMACIST');

-- USERS
INSERT INTO [Users]
(roleID, username, fullName, email, passwordHash, phoneNumber, status, lastChangePassTime, nationalID)
VALUES
(4, 'patient_nam', N'Phạm Thùy Linh', 'namvipnhatgt@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445466', 'INACTIVE', GETDATE(), '043678901234'),
(1, 'admin_system', N'Quản trị viên', 'luugiang205@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0999999999', 'ACTIVE', GETDATE(), '001234567890'),
(2, 'dr_nguyen', N'Bác sĩ Nguyễn Văn Tùng', 'likey2404@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0912345678', 'ACTIVE', GETDATE(), '012345678901'),
(2, 'dr_tran', N'Bác sĩ Trần Thị Mai', 'mai.tran@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0987654321', 'ACTIVE', GETDATE(), '023456789012'),
(4, 'patient_hoang', N'Lê Minh Hoàng', 'hoang.le@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0901112233', 'ACTIVE', GETDATE(), '034567890123'),
(4, 'patient_linh', N'Phạm Thùy Linh', 'linh.pham@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445566', 'ACTIVE', GETDATE(), '045678901234'),
(4, 'patient_huong', N'Nguyễn Thu Hương', 'huong.nguyen@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0905556677', 'ACTIVE', GETDATE(), '056789012345'),
(4, 'patient_lan', N'Trần Thị Lan', 'lan.tran@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0906667788', 'INACTIVE', GETDATE(), '067890123456'),
(4, 'patient_my', N'Đỗ Thanh Mỹ', 'my.do@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0907778899', 'BANNED', GETDATE(), '078901234567'),
(5, 'rp_linh', N'Nguyễn Thị Huyền Linh', 'LinhNguyen205@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0998887776', 'ACTIVE', GETDATE(), '098765432109'),
(5, 'rp_ly', N'Nguyễn Thị Ly', 'LyNguyen205@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0998887676', 'ACTIVE', GETDATE(), '098765432009'),
(5, 'rp_hong', N'Hong Hae In', 'HongHaeIn@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0998887775', 'ACTIVE', GETDATE(), '098765032109'),
(6, 'pharmacist', N'Nguyễn Hoàng Anh', 'anh.pharmacist@pharmacy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0908889999', 'ACTIVE', GETDATE(), '089012345678'),
(6, 'pharmacist2', N'Trần Thị Linh', 'linh.pharmacist@pharmacy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0907776666', 'ACTIVE', GETDATE(), '090123456789'),
(6, 'pharmacist3', N'Nguyễn Hoàng Long', 'lonh.pharmacist@pharmacy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0918889999', 'ACTIVE', GETDATE(), '079012345678'),
(6, 'pharmacist4', N'Nguyễn Thị Linh', 'linh1.pharmacist@pharmacy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0907276666', 'ACTIVE', GETDATE(), '090123406789'),
(3, 'technical', N'Kỹ thuật viên AI', 'technical@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0911111111', 'ACTIVE', GETDATE(), '111111111111');


-- PATIENT (Sửa lại trường liên kết userID cho đúng logic phân quyền)
INSERT INTO Patient (gender, dob, address, userID)
VALUES
(N'Male', '1995-08-15', N'Cầu Giấy, Hà Nội', 1),
(N'Female', '2001-02-20', N'Thanh Xuân, Hà Nội', 6),
(N'Female', '1988-06-12', N'Ba Đình, Hà Nội', 7),
(N'Female', '1975-11-25', N'Đống Đa, Hà Nội', 8),
(N'Female', '1992-09-08', N'Hoàng Mai, Hà Nội', 9);

-- CHUẨN HÓA SYMPTOM: Đã lọc bỏ toàn bộ các ô trắc nghiệm đơn để chuyển sang bảng SymptomResult
INSERT INTO Symptom (symptomName) VALUES
-- 1. Triệu chứng chính: Ra máu âm đạo bất thường
(N'Ra máu giữa kỳ kinh'), (N'Kinh nguyệt kéo dài bất thường'), (N'Rong kinh'), (N'Ra máu sau mãn kinh'), (N'Ra máu sau quan hệ'),
-- Khí hư bất thường
(N'Khí hư nhiều'), (N'Khí hư có mùi hôi'), (N'Khí hư lẫn máu'), (N'Dịch tiết màu nâu'),
-- Đau
(N'Đau vùng chậu'), (N'Đau bụng dưới'), (N'Đau lưng dưới'), (N'Đau khi quan hệ'),
-- 2. Triệu chứng toàn thân
(N'Sụt cân không rõ nguyên nhân'), (N'Mệt mỏi kéo dài'), (N'Chán ăn'),
-- 3. Triệu chứng tiết niệu
(N'Tiểu nhiều lần'), (N'Tiểu buốt'), (N'Tiểu khó'), (N'Tiểu ra máu'),
-- 4. Triệu chứng tiêu hóa
(N'Táo bón kéo dài'), (N'Đầy bụng'), (N'Chướng bụng'), (N'Buồn nôn'),
-- 5. Yếu tố nguy cơ
(N'Tiền sử gia đình mắc ung thư phụ khoa'), (N'Béo phì'), (N'Đái tháo đường'), (N'Tăng huyết áp'), (N'Hội chứng buồng trứng đa nanoc (PCOS)'), (N'Điều trị estrogen kéo dài');

-- Parameter
INSERT INTO Parameter (parameterName, unit)
VALUES (N'Hồng cầu (RBC)', 'T/L'), (N'Bạch cầu (WBC)', 'G/L'), (N'Đường huyết (Glucose)', 'mmol/L');

-- DiagnosisSession
INSERT INTO DiagnosisSession (userID, patientID, weight, height, status, isShared)
VALUES
(3,1,52,155,N'COMPLETED',0),
(4,2,58,160,N'COMPLETED',1),
(3,3,61,158,N'PENDING',0),
(4,4,49,152,N'COMPLETED',0),
(3,5,67,162,N'PENDING',0);

-- CẬP NHẬT: Thêm dữ liệu mẫu trực tiếp vào 3 cột mới ứng với cấu trúc form
INSERT INTO SymptomResult (sessionID, status, menopauseStatus, symptomDuration, symptomProgressing) VALUES
(1, N'COMPLETED', N'Chưa mãn kinh', N'Dưới 1 tháng', 0),
(2, N'COMPLETED', N'Chưa mãn kinh', N'1-3 tháng', 1),
(3, N'PENDING',   N'Đã mãn kinh',   N'3-6 tháng', 1),
(4, N'COMPLETED', N'Chưa mãn kinh', N'Dưới 1 tháng', 0),
(5, N'PENDING',   N'Đã mãn kinh',   N'Trên 6 tháng', 1);

-- CẬP NHẬT: Định vị lại chính xác ID triệu chứng sau khi bảng Symptom thu gọn dữ liệu
INSERT INTO SymptomDetails (symptomResultID, symptomID) VALUES
(1,2),(1,4),(1,10),(1,15),          -- Phiên 1: Kinh nguyệt kéo dài, Ra máu sau mãn kinh, Đau vùng chậu...
(2,1),(2,3),(2,7),(2,11),(2,16),     -- Phiên 2: Ra máu giữa kỳ, Rong kinh, Khí hư có mùi hôi...
(3,2),(3,8),(3,12),(3,14),(3,25),    -- Phiên 3: Kinh nguyệt kéo dài, Dịch tiết màu nâu, Đau bụng dưới...
(4,1),(4,4),(4,8),(4,11),(4,22),     -- Phiên 4: Ra máu giữa kỳ, Ra máu sau mãn kinh...
(5,2),(5,4),(5,10),(5,15),(5,26);    -- Phiên 5: Kinh nguyệt kéo dài, Ra máu sau mãn kinh...

-- LabResult
INSERT INTO LabResult (sessionID, testType, price, status)
VALUES
    (1, N'Xét nghiệm máu tổng quát', 150000.00, 'COMPLETED'),
    (2, N'Kiểm tra đường huyết',       80000.00,  'PENDING');

-- LabResultParameter
INSERT INTO LabResultParameter (labResultID, parameterID, value)
VALUES
    (1, 1, '4.8'), 
    (1, 2, '8.5'); 

-- MedicalImage
INSERT INTO MedicalImage (sessionID, imageType, price, status)
VALUES
    (1, N'X-Ray Phổi',  200000.00, 'COMPLETED'),
    (2, N'Siêu âm bụng', 180000.00, 'PENDING');

-- MedicalImageDetails
INSERT INTO MedicalImageDetails (medicalImageID, imageUrl)
VALUES
    (1, 'https://storage.hospital.com/xray/2026/session1_lung.jpg');

INSERT INTO DiseaseType (name) VALUES
(N'Bình thường / Không phát hiện bất thường'),
(N'Viêm cổ tử cung'),
(N'Tổn thương biểu mô vảy mức độ thấp (LSIL/CIN 1)'),
(N'Tổn thương biểu mô vảy mức độ cao (HSIL/CIN 2-3)'),
(N'Ung thư biểu mô tại chỗ (CIS)'),
(N'Ung thư cổ tử cung xâm lấn giai đoạn sớm (I-II)'),
(N'Ung thư cổ tử cung xâm lấn giai đoạn muộn (III-IV)'),
(N'Cần theo dõi thêm / Chưa đủ dữ liệu kết luận');

-- Review (Chỉ các session 1, 2, 4 có kết luận chẩn đoán)
INSERT INTO Review (sessionID, userID, diseaseTypeID, treatmentPlan, doctorAdvice, note)
VALUES
    (1, 3, 1, N'Kê đơn kháng sinh 5 ngày, siro ho', N'Tránh nước đá, giữ ấm cổ họng', N'Bệnh nhân có tiền sử dị ứng thời tiết'),
    (2, 4, 2, N'Phác đồ điều trị nội tiết và theo dõi sát chức năng gan', N'Theo dõi sát chức năng tiêu hóa và tình trạng nôn nghén', N'Tái khám theo hẹn'),
    (4, 3, 3, N'Điều trị giảm nhẹ và kiểm soát đau thượng vị', N'Ăn thức ăn mềm, dễ tiêu', N'Bệnh nhân nuốt nghẹn');

-- SystemLog
INSERT INTO SystemLog (userID, targetType, targetID, action, description)
VALUES
    (2, 'User', 3, 'CREATE_DOCTOR', N'Admin tạo tài khoản cho Bác sĩ Tùng'),
    (3, 'Review', 1, 'CREATE_FINAL_DIAGNOSIS', N'Bác sĩ Tùng chốt kết quả chẩn đoán cho phiên khám 1');
GO

select * from Users

SELECT 
    tc.constraint_name,
    tc.table_name,
    kc.column_name
FROM 
    information_schema.table_constraints tc
JOIN 
    information_schema.key_column_usage kc 
    ON tc.constraint_name = kc.constraint_name
WHERE 
    tc.constraint_type = 'UNIQUE'
    AND tc.table_name = 'Users';










--=========================================================================================================================================================
USE MedicalDiagnosisDB;
GO

-- =============================================
-- =============================================
-- 2. TẠO CÁC BẢNG QUẢN LÝ THUỐC
-- =============================================

-- 2.1. Nhóm thuốc cấp 1
CREATE TABLE DrugCategory (
    categoryID INT IDENTITY(1,1) PRIMARY KEY,
    categoryName NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    createdAt DATETIME DEFAULT GETDATE()
);

-- 2.2. Nhóm thuốc cấp 2 (Chi tiết)
CREATE TABLE DrugSubCategory (
    subCategoryID INT IDENTITY(1,1) PRIMARY KEY,
    categoryID INT NOT NULL,
    subCategoryName NVARCHAR(100) NOT NULL UNIQUE,
    priorityLevel TINYINT DEFAULT 2, -- 1: Cao, 2: Trung bình, 3: Thấp
    requireSpecialPrescription BIT DEFAULT 0, -- 1: Chỉ BS chuyên khoa, 0: BS đa khoa
    description NVARCHAR(500),
    createdAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (categoryID) REFERENCES DrugCategory(categoryID)
);

-- 2.3. Đơn vị tính
CREATE TABLE Unit (
    unitID INT IDENTITY(1,1) PRIMARY KEY,
    unitName VARCHAR(20) NOT NULL UNIQUE,
    description NVARCHAR(100)
);

-- 2.4. Danh mục thuốc
CREATE TABLE Drug (
    -- Nhóm Định danh
    drugID INT IDENTITY(1,1) PRIMARY KEY,          -- Mã ID tự tăng, khóa chính định danh duy nhất
    drugCode VARCHAR(20) NOT NULL UNIQUE,          -- Mã quản lý thuốc (VD: TH-001), không trùng lặp
    drugName NVARCHAR(200) NOT NULL,               -- Tên biệt dược / tên hoạt chất (VD: Paracetamol)

    -- Nhóm Bào chế & Hàm lượng
    strength VARCHAR(50) NULL,                     -- Hàm lượng/nồng độ (VD: 500, 250/5)
    strengthUnit VARCHAR(10) NULL,                 -- Đơn vị hàm lượng (VD: mg, g, ml, IU)
    dosageForm NVARCHAR(50) NOT NULL,              -- Dạng bào chế (VD: Viên nén, Dung dịch tiêm)
    routeOfAdministration NVARCHAR(50) NOT NULL,   -- Đường dùng (VD: Uống, Tiêm tĩnh mạch, Bôi)

    -- Nhóm Phân loại & Đơn vị tính
    subCategoryID INT NOT NULL,                    -- FK: Khóa ngoại trỏ đến danh mục con (DrugSubCategory)
    baseUnitID INT NOT NULL,                       -- FK: Đơn vị cơ sở nhỏ nhất khi kê đơn/bán lẻ (Viên, Ống, ml)

    -- Nhóm Xuất xứ & Bảo quản
    manufacturer NVARCHAR(100) NULL,               -- Nhà sản xuất (VD: AstraZeneca, Pfizer)
    countryOfOrigin NVARCHAR(50) NULL,             -- Nước sản xuất (VD: Việt Nam, Pháp)
    storageCondition NVARCHAR(200) NULL,           -- Điều kiện bảo quản (VD: Tránh ánh nắng, 15-30°C)

    -- Trạng thái & Ghi chú
    notes NVARCHAR(500) NULL,                     
    status VARCHAR(20) DEFAULT 'ACTIVE',           -- Trạng thái: ACTIVE , INACTIVE 

    -- Nhóm Kiểm toán (Audit Log)
    createdAt DATETIME DEFAULT GETDATE(),          -- Thời gian tạo bản ghi (mặc định hiện tại)
    createdBy INT NULL,                            -- FK: ID người tạo bản ghi ([Users])
    updatedAt DATETIME NULL,                       -- Thời gian cập nhật gần nhất (NULL khi mới tạo)
    updatedBy INT NULL,                            -- FK: ID người cập nhật gần nhất ([Users])

    -- Ràng buộc Khóa ngoại (Foreign Keys)
    FOREIGN KEY (subCategoryID) REFERENCES DrugSubCategory(subCategoryID),
    FOREIGN KEY (baseUnitID) REFERENCES Unit(unitID),
    FOREIGN KEY (createdBy) REFERENCES [Users](userID),
    FOREIGN KEY (updatedBy) REFERENCES [Users](userID)
);

-- 2.5. Quy đổi đơn vị (Nhập theo LỌ/VỈ, Xuất theo VIÊN/ml)
CREATE TABLE UnitConversion (
    conversionID INT IDENTITY(1,1) PRIMARY KEY,
    drugID INT NOT NULL,
    largeUnitID INT NOT NULL,      -- Đơn vị lớn dùng để nhập (HỘP, LỌ)
    smallUnitID INT NOT NULL,      -- Đơn vị nhỏ dùng để xuất (VIÊN, ỐNG). Cột này bắt buộc phải trùng với baseUnitID của bảng Drug.
    conversionQuantity INT NOT NULL, -- Hệ số quy đổi: 1 đơn vị lớn = bao nhiêu đơn vị nhỏ
    createdAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (drugID) REFERENCES Drug(drugID),
    FOREIGN KEY (largeUnitID) REFERENCES Unit(unitID),
    FOREIGN KEY (smallUnitID) REFERENCES Unit(unitID),
    CONSTRAINT UQ_Drug_LargeUnit UNIQUE (drugID, largeUnitID) -- Đảm bảo một thuốc với một đơn vị lớn chỉ có 1 công thức quy đổi
);

-- 2.6. Lô hàng nhập kho
CREATE TABLE DrugBatch (
    -- Nhóm Định danh & Liên kết Thuốc
    batchID INT IDENTITY(1,1) PRIMARY KEY,      -- Mã ID tự tăng, khóa chính định danh duy nhất cho từng lô hàng
    drugID INT NOT NULL,                         -- FK: Mã thuốc nhập kho (liên kết bảng Drug)
    batchNumber VARCHAR(50) NOT NULL,            -- Số lô sản xuất ghi trên bao bì (VD: LOT-2026-001)

    -- Nhóm Hạn sử dụng (Phục vụ xuất kho FEFO - Hết hạn trước xuất trước)
    manufactureDate DATE NOT NULL,              -- Ngày sản xuất của lô thuốc
    expiryDate DATE NOT NULL,                   -- Ngày hết hạn sử dụng của lô thuốc

    -- Nhóm Số lượng & Đơn vị tính
    unitID INT NOT NULL,                        -- FK: Đơn vị lớn khi nhập (Hộp/Lọ/Thùng) (liên kết bảng Unit)
    quantity INT NOT NULL,                      -- Số lượng nhập theo đơn vị lớn (VD: 50 hộp)

    -- Nhóm Chi phí, Nhà cung cấp & Thời gian
    supplier NVARCHAR(200) NULL,                -- Tên công ty / Nhà cung cấp phân phối lô thuốc
    importDate DATETIME DEFAULT GETDATE(),       -- Ngày giờ tiến hành nhập kho (mặc định hiện tại)

    -- Trạng thái & Ghi chú
    importedBy INT NOT NULL,                     -- FK: ID nhân viên/thủ kho thực hiện nhập (liên kết bảng Users)
    status VARCHAR(20) DEFAULT 'ACTIVE',         -- ACTIVE, INACTIVE, EXPIRED
    notes NVARCHAR(500) NULL,                    -- Ghi chú bổ sung khi nhập kho (VD: Hàng tài trợ, Vỏ hộp bị móp)

	-- Nhóm Lịch sử & Kiểm toán chỉnh sửa (Audit Log)
    updatedAt DATETIME NULL,                     -- Ngày giờ diễn ra lần chỉnh sửa lô hàng gần nhất (NULL khi mới tạo)
    updatedBy INT NULL,                          -- FK: ID người thực hiện lần chỉnh sửa gần nhất (liên kết bảng Users)
    updateReason NVARCHAR(500) NULL,             -- Lý do điều chỉnh thông tin lô (VD: Sửa lại số lượng do kiểm kê sai)

    -- Ràng buộc Khóa ngoại (Foreign Keys)
    FOREIGN KEY (drugID) REFERENCES Drug(drugID),
    FOREIGN KEY (unitID) REFERENCES Unit(unitID),
    FOREIGN KEY (importedBy) REFERENCES [Users](userID),
    FOREIGN KEY (updatedBy) REFERENCES [Users](userID),

    -- Ràng buộc Duy nhất: Một loại thuốc không thể trùng mã số lô
    CONSTRAINT UQ_Batch_Drug UNIQUE (batchNumber, drugID)
);

-- 2.7. Tồn kho chi tiết (Tính theo đơn vị nhỏ nhất)
CREATE TABLE Inventory (
    inventoryID INT IDENTITY(1,1) PRIMARY KEY,
    batchID INT NOT NULL,
    quantityInStock INT NOT NULL DEFAULT 0, -- Tồn theo đơn vị nhỏ nhất (VIÊN/ỐNG/ml)
    lastUpdated DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (batchID) REFERENCES DrugBatch(batchID)
);

-- 2.8. Đơn thuốc (Liên kết với DiagnosisSession)
CREATE TABLE Prescription (
    prescriptionID INT IDENTITY(1,1) PRIMARY KEY,
    prescriptionCode VARCHAR(20) NOT NULL UNIQUE,
    sessionID INT NOT NULL, -- Liên kết với phiên chẩn đoán
    patientID INT NOT NULL,
    doctorID INT NOT NULL, -- Bác sĩ kê đơn (UserID)
    diagnosis NVARCHAR(500),
    treatmentCycle NVARCHAR(50),
    prescriptionDate DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING,DISPENSED
    notes NVARCHAR(500),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession(sessionID),
    FOREIGN KEY (patientID) REFERENCES Patient(patientID),
    FOREIGN KEY (doctorID) REFERENCES [Users](userID)
);

-- 2.9. Chi tiết đơn thuốc (Kê thuốc + Xuất kho)
CREATE TABLE PrescriptionDetail (
    detailID INT IDENTITY(1,1) PRIMARY KEY,
    prescriptionID INT NOT NULL,
    drugID INT NOT NULL,
    dosePerTime DECIMAL(10,2) NOT NULL, -- Liều mỗi lần (mg)
    timesPerDay INT NOT NULL DEFAULT 1,
    daysOfTreatment INT NOT NULL DEFAULT 1,
    quantityPrescribed INT NOT NULL, -- Tổng số lượng kê (theo đơn vị nhỏ nhất)
    batchID INT NULL, -- Lô hàng đã xuất
    quantityDispensed INT DEFAULT 0, -- Số lượng đã xuất thực tế
    actualExpiryDate DATE NULL, -- Hạn dùng ghi trên túi thuốc
    dispenseUnit VARCHAR(10) DEFAULT 'VIÊN',
    instruction NVARCHAR(500),
    dispensedAt DATETIME NULL,
    dispensedBy INT NULL, -- Dược sĩ xuất thuốc
    notes NVARCHAR(200),
	status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING,DISPENSED,CANCELLED
    FOREIGN KEY (prescriptionID) REFERENCES Prescription(prescriptionID),
    FOREIGN KEY (drugID) REFERENCES Drug(drugID),
    FOREIGN KEY (batchID) REFERENCES DrugBatch(batchID),
    FOREIGN KEY (dispensedBy) REFERENCES [Users](userID)
);

-- 2.10. Log xuất nhập kho
CREATE TABLE InventoryLog (
    logID INT IDENTITY(1,1) PRIMARY KEY,
    batchID INT NOT NULL,
    userID INT NOT NULL,
    actionType VARCHAR(20) NOT NULL, -- IMPORT, DISPENSE, ADJUST
    quantityChange INT NOT NULL, -- Số lượng thay đổi (theo đơn vị nhỏ)
    quantityBefore INT NOT NULL,
    quantityAfter INT NOT NULL,
    referenceID INT NULL, -- ID của PrescriptionDetail hoặc DrugBatch
    referenceType VARCHAR(50) NULL,
    performedAt DATETIME DEFAULT GETDATE(),
    notes NVARCHAR(500),
    FOREIGN KEY (batchID) REFERENCES DrugBatch(batchID),
    FOREIGN KEY (userID) REFERENCES [Users](userID)
);
GO

-- 3.1. Đơn vị tính
INSERT INTO Unit (unitName, description) VALUES
('VIÊN', N'Đơn vị nhỏ nhất - viên thuốc'),
('VỈ', N'Đóng gói thương mại - vỉ nhôm/nhựa'),
('HỘP', N'Đóng gói thương mại - hộp giấy'),
('LỌ', N'Đóng gói thương mại - lọ nhựa/thủy tinh'),
('ỐNG', N'Đóng gói thương mại - ống nhựa/giấy'),
('TUÝP', N'Đóng gói thương mại - tuýp nhôm/nhựa cho thuốc bôi');

-- 3.2. Nhóm thuốc cấp 1
INSERT INTO DrugCategory (categoryName, description) VALUES
(N'Thuốc điều trị', N'Thuốc tác động trực tiếp lên tế bào ung thư'),
(N'Thuốc hỗ trợ điều trị', N'Thuốc giảm tác dụng phụ, nâng đỡ cơ thể');

-- 3.3. Nhóm thuốc cấp 2
INSERT INTO DrugSubCategory (categoryID, subCategoryName, priorityLevel, requireSpecialPrescription, description) VALUES
(1, N'Thuốc nội tiết', 1, 1, N'Điều trị ung thư phụ thuộc hormone'),
(1, N'Thuốc điều trị đích', 1, 1, N'Tác động chọn lọc lên tế bào ung thư qua cơ chế phân tử'),
(1, N'Thuốc hóa trị toàn thân', 1, 1, N'Hóa chất tác động lên tế bào phân chia nhanh'),
(1, N'Thuốc miễn dịch', 1, 1, N'Kích thích hệ miễn dịch tấn công tế bào ung thư'),
(2, N'Thuốc giảm đau', 2, 0, N'Giảm đau do ung thư xâm lấn hoặc sau phẫu thuật'),
(2, N'Thuốc chống nôn', 2, 0, N'Dự phòng và điều trị buồn nôn do hóa trị/xạ trị'),
(2, N'Thuốc bảo vệ dạ dày', 2, 0, N'Bảo vệ niêm mạc dạ dày khi dùng Corticoid/NSAIDs'),
(2, N'Thuốc kháng sinh', 2, 0, N'Điều trị nhiễm khuẩn do suy giảm miễn dịch'),
(2, N'Thuốc kháng nấm', 2, 0, N'Điều trị nhiễm nấm cơ hội'),
(2, N'Vitamin và khoáng chất', 3, 0, N'Bổ sung vi chất, nâng cao thể trạng'),
(2, N'Thuốc nhuận tràng', 3, 0, N'Điều trị táo bón do thuốc giảm đau opioid'),
(2, N'Thuốc chống tiêu chảy', 3, 0, N'Điều trị tiêu chảy do hóa trị hoặc nhiễm trùng'),
(2, N'Thuốc chống dị ứng', 2, 0, N'Dự phòng và điều trị phản ứng dị ứng khi truyền thuốc'),
(2, N'Thuốc chống viêm', 2, 1, N'Chống viêm, chống dị ứng, giảm phù nề');

-- 3.4. Danh mục thuốc 
INSERT INTO Drug (
    drugCode, drugName, strength, strengthUnit, dosageForm,
    routeOfAdministration, subCategoryID, baseUnitID, manufacturer,
    countryOfOrigin, storageCondition, notes, status, 
    createdAt, createdBy, updatedAt, updatedBy
) VALUES
-- ========== NHÓM 1: THUỐC ĐIỀU TRỊ UNG THƯ (baseUnitID = 1: Viên) ==========
('DRUG-001', N'Tamoxifen (Nolvadex)', '20', 'mg', N'Viên nén bao phim', N'Uống', 1, 1, 'AstraZeneca', N'Anh', N'15-25°C, tránh ẩm', N'Uống sau bữa ăn', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-002', N'Letrozole (Femara)', '2.5', 'mg', N'Viên nén bao phim', N'Uống', 1, 1, 'Novartis', N'Thụy Sĩ', N'15-30°C', N'Uống xa bữa ăn', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-003', N'Anastrozole (Arimidex)', '1', 'mg', N'Viên nén bao phim', N'Uống', 1, 1, 'AstraZeneca', N'Anh', N'15-30°C', N'Uống xa bữa ăn', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-004', N'Exemestane (Aromasin)', '25', 'mg', N'Viên nén bao phim', N'Uống', 1, 1, 'Pfizer', N'Mỹ', N'15-30°C', N'Uống sau bữa ăn', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-005', N'Imatinib (Glivec)', '400', 'mg', N'Viên nén bao phim', N'Uống', 2, 1, 'Novartis', N'Thụy Sĩ', N'15-30°C', N'Uống trong bữa ăn, nhiều nước', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-006', N'Erlotinib (Tarceva)', '150', 'mg', N'Viên nén bao phim', N'Uống', 2, 1, 'Roche', N'Thụy Sĩ', N'15-25°C', N'Uống 1 giờ trước hoặc 2 giờ sau ăn', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-007', N'Capecitabine (Xeloda)', '500', 'mg', N'Viên nén bao phim', N'Uống', 3, 1, 'Roche', N'Thụy Sĩ', N'15-30°C', N'Uống trong vòng 30 phút sau ăn', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-008', N'Cyclophosphamide (Endoxan)', '50', 'mg', N'Viên nén bao phim', N'Uống', 3, 1, 'Baxter', N'Mỹ', N'15-25°C', N'Uống vào buổi sáng, nhiều nước', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-009', N'Methotrexate', '2.5', 'mg', N'Viên nén', N'Uống', 3, 1, 'Pfizer', N'Mỹ', N'15-30°C', N'Uống 1 lần/tuần theo chỉ định', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-010', N'Thalidomide', '50', 'mg', N'Viên nén', N'Uống', 4, 1, N'Celgene', N'Mỹ', N'15-30°C', N'Uống trước khi ngủ, cần tránh thai', 'ACTIVE', GETDATE(), 14, NULL, NULL),

-- ========== NHÓM 2: THUỐC HỖ TRỢ (baseUnitID = 1: Viên) ==========
('DRUG-011', N'Morphine SR (MST Continus)', '30', 'mg', N'Viên giải phóng kéo dài', N'Uống', 5, 1, 'Mundipharma', N'Anh', N'15-25°C', N'Uống cách 12 giờ, không nghiền nát', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-012', N'Tramadol (Tramal)', '50', 'mg', N'Viên nén', N'Uống', 5, 1, 'Grunenthal', N'Đức', N'15-30°C', N'Uống khi đau, tối đa 400mg/ngày', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-013', N'Paracetamol (Panadol)', '500', 'mg', N'Viên nén', N'Uống', 5, 1, N'GSK', N'Anh', N'15-30°C', N'Uống khi đau hoặc sốt, tối đa 4g/ngày', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-014', N'Ondansetron (Zofran)', '8', 'mg', N'Viên nén bao phim', N'Uống', 6, 1, 'GSK', N'Anh', N'15-30°C', N'Uống 1 giờ trước hóa trị', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-015', N'Metoclopramide (Primperan)', '10', 'mg', N'Viên nén', N'Uống', 6, 1, 'Sanofi', N'Pháp', N'15-30°C', N'Uống 30 phút trước ăn', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-016', N'Domperidone (Motilium)', '10', 'mg', N'Viên nén bao phim', N'Uống', 6, 1, 'Janssen', N'Bỉ', N'15-30°C', N'Uống trước bữa ăn 15-30 phút', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-017', N'Pantoprazole (Pantozol)', '40', 'mg', N'Viên nén bao phim', N'Uống', 7, 1, 'Takeda', N'Nhật', N'15-25°C', N'Uống trước bữa ăn 30 phút', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-018', N'Omeprazole (Losec)', '20', 'mg', N'Viên nén bao phim', N'Uống', 7, 1, 'AstraZeneca', N'Anh', N'15-25°C', N'Uống trước bữa ăn 30 phút', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-019', N'Cetirizine (Zyrtec)', '10', 'mg', N'Viên nén bao phim', N'Uống', 8, 1, 'UCB', N'Bỉ', N'15-30°C', N'Uống 1 lần/ngày, có thể gây buồn ngủ', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-020', N'Loratadine (Clarityn)', '10', 'mg', N'Viên nén', N'Uống', 8, 1, 'Bayer', N'Đức', N'15-30°C', N'Uống 1 lần/ngày, không gây buồn ngủ', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-021', N'Dexamethasone (Decadron)', '4', 'mg', N'Viên nén', N'Uống', 9, 1, 'Merck', N'Mỹ', N'15-25°C', N'Uống theo phác đồ, giảm liều từ từ', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-022', N'Methylprednisolone (Medrol)', '16', 'mg', N'Viên nén', N'Uống', 9, 1, 'Pfizer', N'Mỹ', N'15-30°C', N'Uống theo chỉ định bác sĩ', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-023', N'Prednisolone', '5', 'mg', N'Viên nén', N'Uống', 9, 1, N'Sanofi', N'Pháp', N'15-30°C', N'Uống vào buổi sáng, giảm liều từ từ', 'ACTIVE', GETDATE(), 15, NULL, NULl),

-- ========== NHÓM 3: VITAMIN VÀ KHOÁNG CHẤT (baseUnitID = 1: Viên) ==========
('DRUG-024', N'Vitamin C', '500', 'mg', N'Viên sủi', N'Uống', 10, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'15-30°C', N'Hòa tan với nước, uống 1 viên/ngày', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-025', N'Vitamin tổng hợp', 'Multivitamin', 'viên', N'Viên nén', N'Uống', 10, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'15-30°C', N'Uống 1 viên/ngày sau bữa ăn', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-026', N'Centrum (Vitamin tổng hợp)', 'Multivitamin', 'viên', N'Viên nén bao phim', N'Uống', 10, 1, 'Pfizer', N'Mỹ', N'15-30°C', N'Uống 1 viên/ngày sau bữa ăn', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-027', N'Calcium + Vitamin D3', '500', 'mg', N'Viên sủi', N'Uống', 10, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'15-30°C', N'Hòa tan trong nước, uống 1 viên/ngày', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-028', N'Omega-3 (Fish Oil)', '1000', 'mg', N'Viên nang mềm', N'Uống', 10, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'15-25°C', N'Uống sau bữa ăn, 1-2 viên/ngày', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-029', N'Probiotics (Bioflora)', '10 tỷ CFU', 'CFU', N'Viên nang', N'Uống', 10, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'2-8°C', N'Uống trước ăn 30 phút, bảo quản lạnh', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-030', N'Magnesium', '300', 'mg', N'Viên nén', N'Uống', 10, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'15-30°C', N'Uống 1 viên/ngày', 'ACTIVE', GETDATE(), 14, NULL, NULL),

-- ========== NHÓM 4: TIÊU HÓA & KHÁNG SINH UỐNG (baseUnitID = 1: Viên) ==========
('DRUG-031', N'Bisacodyl (Dulcolax)', '5', 'mg', N'Viên nén bao phim', N'Uống', 11, 1, 'Boehringer', N'Đức', N'15-25°C', N'Uống trước khi ngủ, tác dụng sau 6-12 giờ', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-032', N'Loperamide (Imodium)', '2', 'mg', N'Viên nang', N'Uống', 12, 1, N'Janssen', N'Bỉ', N'15-30°C', N'Uống sau mỗi lần đi tiêu lỏng, tối đa 8 viên/ngày', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-033', N'Amoxicillin', '500', 'mg', N'Viên nang', N'Uống', 13, 1, N'Công ty Dược Việt Nam', N'Việt Nam', N'15-30°C', N'Uống sau ăn, cách 8 giờ/lần', 'ACTIVE', GETDATE(), 13, NULL, NULL),
('DRUG-034', N'Ciprofloxacin (Ciprobay)', '500', 'mg', N'Viên nén bao phim', N'Uống', 13, 1, 'Bayer', N'Đức', N'15-30°C', N'Uống xa bữa ăn, nhiều nước', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-035', N'Azithromycin (Zithromax)', '500', 'mg', N'Viên nén bao phim', N'Uống', 13, 1, 'Pfizer', N'Mỹ', N'15-30°C', N'Uống 1 viên/ngày x 3 ngày', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-036', N'Fluconazole (Diflucan)', '150', 'mg', N'Viên nang', N'Uống', 14, 1, 'Pfizer', N'Mỹ', N'15-30°C', N'Uống 1 viên duy nhất, có thể lặp lại sau 72h', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-037', N'Itraconazole (Sporanox)', '100', 'mg', N'Viên nang', N'Uống', 14, 1, 'Janssen', N'Bỉ', N'15-25°C', N'Uống trong bữa ăn, nhiều nước', 'ACTIVE', GETDATE(), 13, NULL, NULL),

-- ========== NHÓM 5: THUỐC BÔI NGOÀI DA (baseUnitID = 6: Tuýp) ==========
('DRUG-038', N'Clotrimazole (Canesten)', '10', 'mg/g', N'Kem bôi ngoài da', N'Bôi ngoài da', 14, 6, 'Bayer', N'Đức', N'15-30°C', N'Bôi 2-3 lần/ngày lên vùng da bị nhiễm', 'ACTIVE', GETDATE(), 14, NULL, NULL),
('DRUG-039', N'Ketoconazole (Nizoral)', '2%', '%', N'Kem bôi ngoài da', N'Bôi ngoài da', 14, 6, 'Janssen', N'Bỉ', N'15-30°C', N'Thoa 1-2 lần/ngày, kéo dài 2-4 tuần', 'ACTIVE', GETDATE(), 15, NULL, NULL),
('DRUG-040', N'Terbinafine (Lamisil)', '1%', '%', N'Kem bôi ngoài da', N'Bôi ngoài da', 14, 6, 'Novartis', N'Thụy Sĩ', N'15-30°C', N'Thoa 1 lần/ngày lên vùng da sạch, khô', 'ACTIVE', GETDATE(), 16, NULL, NULL),
('DRUG-041', N'Miconazole (Daktarin)', '2%', '%', N'Gel bôi ngoài da', N'Bôi ngoài da', 14, 6, 'Janssen', N'Bỉ', N'15-25°C', N'Thoa 2 lần/ngày, tiếp tục 10 ngày sau khi hết triệu chứng', 'ACTIVE', GETDATE(), 13, NULL, NULL);
-- 3.5. Quy đổi đơn vị (Đã sửa chính xác drugID theo thứ tự bảng Drug từ 1 đến 41)
INSERT INTO UnitConversion (drugID, largeUnitID, smallUnitID, conversionQuantity) VALUES
-- ========== NHÓM 1: THUỐC ĐIỀU TRỊ UNG THƯ (ID: 1 - 10) ==========
-- HỘP (3) -> VỈ (2) -> VIÊN (1)
(1, 3, 2, 3), (1, 2, 1, 10),   -- DRUG-001: Tamoxifen (1 HỘP = 3 VỈ = 30 VIÊN)
(2, 3, 2, 3), (2, 2, 1, 10),   -- DRUG-002: Letrozole (1 HỘP = 3 VỈ = 30 VIÊN)
(3, 3, 2, 3), (3, 2, 1, 10),   -- DRUG-003: Anastrozole (1 HỘP = 3 VỈ = 30 VIÊN)
(4, 3, 2, 3), (4, 2, 1, 10),   -- DRUG-004: Exemestane (1 HỘP = 3 VỈ = 30 VIÊN)
(5, 3, 2, 3), (5, 2, 1, 10),   -- DRUG-005: Imatinib (1 HỘP = 3 VỈ = 30 VIÊN)
(6, 3, 2, 3), (6, 2, 1, 10),   -- DRUG-006: Erlotinib (1 HỘP = 3 VỈ = 30 VIÊN)
(7, 3, 2, 3), (7, 2, 1, 10),   -- DRUG-007: Capecitabine (1 HỘP = 3 VỈ = 30 VIÊN)
(8, 3, 2, 3), (8, 2, 1, 10),   -- DRUG-008: Cyclophosphamide (1 HỘP = 3 VỈ = 30 VIÊN)
(9, 3, 2, 3), (9, 2, 1, 10),   -- DRUG-009: Methotrexate (1 HỘP = 3 VỈ = 30 VIÊN)
(10, 3, 2, 3), (10, 2, 1, 10), -- DRUG-010: Thalidomide (1 HỘP = 3 VỈ = 30 VIÊN)

-- ========== NHÓM 2: THUỐC HỖ TRỢ (ID: 11 - 32) ==========
-- 2.1 - 2.5: Dạng Hộp -> Vỉ -> Viên
(11, 3, 2, 2), (11, 2, 1, 10), -- DRUG-011: Morphine (1 HỘP = 2 VỈ = 20 VIÊN)
(12, 3, 2, 2), (12, 2, 1, 10), -- DRUG-012: Tramadol (1 HỘP = 2 VỈ = 20 VIÊN)
(13, 3, 2, 2), (13, 2, 1, 10), -- DRUG-013: Paracetamol (1 HỘP = 2 VỈ = 20 VIÊN)
(14, 3, 2, 1), (14, 2, 1, 10), -- DRUG-014: Ondansetron (1 HỘP = 1 VỈ = 10 VIÊN)
(15, 3, 2, 3), (15, 2, 1, 10), -- DRUG-015: Metoclopramide (1 HỘP = 3 VỈ = 30 VIÊN)
(16, 3, 2, 2), (16, 2, 1, 10), -- DRUG-016: Domperidone (1 HỘP = 2 VỈ = 20 VIÊN)
(17, 3, 2, 2), (17, 2, 1, 7),  -- DRUG-017: Pantoprazole (1 HỘP = 2 VỈ = 14 VIÊN)
(18, 3, 2, 2), (18, 2, 1, 10), -- DRUG-018: Omeprazole (1 HỘP = 2 VỈ = 20 VIÊN)
(19, 3, 2, 1), (19, 2, 1, 10), -- DRUG-019: Cetirizine (1 HỘP = 1 VỈ = 10 VIÊN)
(20, 3, 2, 1), (20, 2, 1, 10), -- DRUG-020: Loratadine (1 HỘP = 1 VỈ = 10 VIÊN)
(21, 3, 2, 3), (21, 2, 1, 10), -- DRUG-021: Dexamethasone (1 HỘP = 3 VỈ = 30 VIÊN)
(22, 3, 2, 2), (22, 2, 1, 10), -- DRUG-022: Methylprednisolone (1 HỘP = 2 VỈ = 20 VIÊN)
(23, 3, 2, 3), (23, 2, 1, 10), -- DRUG-023: Prednisolone (1 HỘP = 3 VỈ = 30 VIÊN)

-- 2.6: Vitamin & Khoáng chất (ID: 24 - 30)
(24, 5, 1, 10),                -- DRUG-024: Vitamin C (1 ỐNG = 10 VIÊN)
(25, 4, 1, 60),                -- DRUG-025: Vitamin tổng hợp (1 LỌ = 60 VIÊN)
(26, 4, 1, 60),                -- DRUG-026: Centrum (1 LỌ = 60 VIÊN)
(27, 5, 1, 20),                -- DRUG-027: Calcium + D3 (1 ỐNG = 20 VIÊN)
(28, 4, 1, 30),                -- DRUG-028: Omega-3 (1 LỌ = 30 VIÊN)
(29, 4, 1, 30),                -- DRUG-029: Probiotics (1 LỌ = 30 VIÊN)
(30, 4, 1, 60),                -- DRUG-030: Magnesium (1 LỌ = 60 VIÊN)

-- 2.7 - 2.8: Thuốc tiêu hóa
(31, 3, 2, 2), (31, 2, 1, 10), -- DRUG-031: Bisacodyl (1 HỘP = 2 VỈ = 20 VIÊN)
(32, 3, 2, 1), (32, 2, 1, 10), -- DRUG-032: Loperamide (1 HỘP = 1 VỈ = 10 VIÊN)

-- ========== NHÓM KHÁNG SINH & KHÁNG NẤM (ID: 33 - 41) ==========
-- Kháng sinh & Thuốc uống (HỘP -> VỈ -> VIÊN)
(33, 3, 2, 2), (33, 2, 1, 10), -- DRUG-033: Amoxicillin (1 HỘP = 2 VỈ = 20 VIÊN)
(34, 3, 2, 1), (34, 2, 1, 10), -- DRUG-034: Ciprofloxacin (1 HỘP = 1 VỈ = 10 VIÊN)
(35, 3, 2, 1), (35, 2, 1, 3),  -- DRUG-035: Azithromycin (1 HỘP = 1 VỈ = 3 VIÊN)
(36, 3, 2, 1), (36, 2, 1, 1),  -- DRUG-036: Fluconazole (1 HỘP = 1 VỈ = 1 VIÊN)
(37, 3, 2, 2), (37, 2, 1, 10), -- DRUG-037: Itraconazole (1 HỘP = 2 VỈ = 20 VIÊN)

-- Thuốc kem/gel bôi ngoài da (HỘP -> TUÝP)
(38, 3, 6, 1),                 -- DRUG-038: Clotrimazole (1 HỘP = 1 TUÝP)
(39, 3, 6, 1),                 -- DRUG-039: Ketoconazole (1 HỘP = 1 TUÝP)
(40, 3, 6, 1),                 -- DRUG-040: Terbinafine (1 HỘP = 1 TUÝP)
(41, 3, 6, 1);                 -- DRUG-041: Miconazole (1 HỘP = 1 TUÝP)


-- ============================================================================
-- 3.7. Nhập kho mẫu (Sử dụng mã lô dạng LOT-DRUG-XXX-MMYY)
-- ============================================================================
INSERT INTO DrugBatch (
    drugID, batchNumber, manufactureDate, expiryDate, 
    unitID, quantity, supplier, importDate, 
    importedBy, status, notes, updatedAt, 
    updatedBy, updateReason
) VALUES
-- NHÓM 1: THUỐC ĐIỀU TRỊ UNG THƯ (ID 1-10)
(1,  'LOT-001-DRUG-001', '2025-01-01', '2027-12-31', 3, 50,  N'Công ty Dược XYZ',         GETDATE(), 13, 'ACTIVE', N'Nhập 50 hộp Tamoxifen',      NULL, NULL, NULL),
(2,  'LOT-002-DRUG-002', '2025-03-15', '2028-02-28', 3, 30,  N'Công ty Dược ABC',         GETDATE(), 14, 'ACTIVE', N'Nhập 30 hộp Letrozole',     NULL, NULL, NULL),
(3,  'LOT-003-DRUG-003', '2025-01-10', '2027-12-31', 3, 40,  N'AstraZeneca Việt Nam',     GETDATE(), 15, 'ACTIVE', N'Nhập 40 hộp Anastrozole',   NULL, NULL, NULL),
(4,  'LOT-004-DRUG-004', '2025-02-01', '2028-01-31', 3, 25,  N'Pfizer Việt Nam',          GETDATE(), 16, 'ACTIVE', N'Nhập 25 hộp Exemestane',    NULL, NULL, NULL),
(5,  'LOT-005-DRUG-005', '2025-02-15', '2028-02-15', 3, 20,  N'Novartis Việt Nam',        GETDATE(), 13, 'ACTIVE', N'Nhập 20 hộp Imatinib',      NULL, NULL, NULL),
(6,  'LOT-006-DRUG-006', '2025-03-01', '2028-03-01', 3, 15,  N'Roche Việt Nam',           GETDATE(), 14, 'ACTIVE', N'Nhập 15 hộp Erlotinib',      NULL, NULL, NULL),
(7,  'LOT-007-DRUG-007', '2025-04-10', '2028-04-10', 3, 60,  N'Roche Việt Nam',           GETDATE(), 15, 'ACTIVE', N'Nhập 60 hộp Capecitabine',  NULL, NULL, NULL),
(8,  'LOT-008-DRUG-008', '2025-01-20', '2027-12-31', 3, 80,  N'Baxter Việt Nam',          GETDATE(), 16, 'ACTIVE', N'Nhập 80 hộp Cyclophosphamide', NULL, NULL, NULL),
(9,  'LOT-009-DRUG-009', '2025-02-18', '2028-02-18', 3, 100, N'Pfizer Việt Nam',          GETDATE(), 13, 'ACTIVE', N'Nhập 100 hộp Methotrexate', NULL, NULL, NULL),
(10, 'LOT-010-DRUG-010', '2025-03-05', '2027-03-05', 3, 30,  N'Celgene Việt Nam',         GETDATE(), 14, 'ACTIVE', N'Nhập 30 hộp Thalidomide',   NULL, NULL, NULL),

-- NHÓM 2.1 - 2.5: THUỐC HỖ TRỢ UỐNG (ID 11-23)
(11, 'LOT-011-DRUG-011', '2025-04-01', '2028-03-31', 3, 25,  N'Công ty Dược Mundipharma', GETDATE(), 15, 'ACTIVE', N'Nhập 25 hộp Morphine',      NULL, NULL, NULL),
(12, 'LOT-012-DRUG-012', '2025-05-01', '2028-04-30', 3, 50,  N'Grunenthal Việt Nam',      GETDATE(), 16, 'ACTIVE', N'Nhập 50 hộp Tramadol',      NULL, NULL, NULL),
(13, 'LOT-013-DRUG-013', '2025-06-10', '2028-06-10', 3, 200, N'GSK Việt Nam',             GETDATE(), 13, 'ACTIVE', N'Nhập 200 hộp Paracetamol',  NULL, NULL, NULL),
(14, 'LOT-014-DRUG-014', '2025-05-20', '2027-04-30', 3, 100, N'Công ty Dược GSK',         GETDATE(), 14, 'ACTIVE', N'Nhập 100 hộp Ondansetron',  NULL, NULL, NULL),
(15, 'LOT-015-DRUG-015', '2025-06-01', '2027-06-01', 3, 80,  N'Sanofi Việt Nam',          GETDATE(), 15, 'ACTIVE', N'Nhập 80 hộp Metoclopramide', NULL, NULL, NULL),
(16, 'LOT-016-DRUG-016', '2025-07-01', '2027-07-01', 3, 70,  N'Janssen Việt Nam',         GETDATE(), 16, 'ACTIVE', N'Nhập 70 hộp Domperidone',   NULL, NULL, NULL),
(17, 'LOT-017-DRUG-017', '2025-09-01', '2028-08-31', 3, 50,  N'Công ty Dược Takeda',      GETDATE(), 13, 'ACTIVE', N'Nhập 50 hộp Pantoprazole',  NULL, NULL, NULL),
(18, 'LOT-018-DRUG-018', '2025-08-15', '2028-08-15', 3, 90,  N'AstraZeneca Việt Nam',     GETDATE(), 14, 'ACTIVE', N'Nhập 90 hộp Omeprazole',    NULL, NULL, NULL),
(19, 'LOT-019-DRUG-019', '2025-09-10', '2028-09-10', 3, 100, N'UCB Việt Nam',              GETDATE(), 15, 'ACTIVE', N'Nhập 100 hộp Cetirizine',   NULL, NULL, NULL),
(20, 'LOT-020-DRUG-020', '2025-10-01', '2028-10-01', 3, 100, N'Bayer Việt Nam',            GETDATE(), 16, 'ACTIVE', N'Nhập 100 hộp Loratadine',   NULL, NULL, NULL),
(21, 'LOT-021-DRUG-021', '2025-10-15', '2028-09-30', 3, 60,  N'Công ty Dược Merck',       GETDATE(), 13, 'ACTIVE', N'Nhập 60 hộp Dexamethasone', NULL, NULL, NULL),
(22, 'LOT-022-DRUG-022', '2025-11-01', '2028-11-01', 3, 50,  N'Pfizer Việt Nam',          GETDATE(), 14, 'ACTIVE', N'Nhập 50 hộp Methylprednisolone', NULL, NULL, NULL),
(23, 'LOT-023-DRUG-023', '2025-11-15', '2028-11-15', 3, 80,  N'Sanofi Việt Nam',          GETDATE(), 15, 'ACTIVE', N'Nhập 80 hộp Prednisolone',  NULL, NULL, NULL),

-- NHÓM 2.6: VITAMIN & KHOÁNG CHẤT (ID 24-30)
(24, 'LOT-024-DRUG-024', '2025-11-01', '2027-10-31', 5, 100, N'Công ty Dược Việt Nam',    GETDATE(), 16, 'ACTIVE', N'Nhập 100 ống Vitamin C',   NULL, NULL, NULL),
(25, 'LOT-025-DRUG-025', '2026-01-01', '2028-12-31', 4, 30,  N'Công ty Dược Việt Nam',    GETDATE(), 13, 'ACTIVE', N'Nhập 30 lọ Vitamin tổng hợp', NULL, NULL, NULL),
(26, 'LOT-026-DRUG-026', '2026-02-01', '2028-01-31', 4, 40,  N'Pfizer Việt Nam',          GETDATE(), 14, 'ACTIVE', N'Nhập 40 lọ Centrum',        NULL, NULL, NULL),
(27, 'LOT-027-DRUG-027', '2025-12-01', '2027-11-30', 5, 50,  N'Công ty Dược Việt Nam',    GETDATE(), 15, 'ACTIVE', N'Nhập 50 ống Calcium + D3',  NULL, NULL, NULL),
(28, 'LOT-028-DRUG-028', '2026-01-10', '2028-01-10', 4, 50,  N'Công ty Dược Việt Nam',    GETDATE(), 16, 'ACTIVE', N'Nhập 50 lọ Omega-3',        NULL, NULL, NULL),
(29, 'LOT-029-DRUG-029', '2026-02-15', '2027-08-15', 4, 40,  N'Công ty Dược Việt Nam',    GETDATE(), 13, 'ACTIVE', N'Nhập 40 lọ Probiotics',     NULL, NULL, NULL),
(30, 'LOT-030-DRUG-030', '2026-03-01', '2028-03-01', 4, 60,  N'Công ty Dược Việt Nam',    GETDATE(), 14, 'ACTIVE', N'Nhập 60 lọ Magnesium',      NULL, NULL, NULL),

-- NHÓM 2.7 - 2.8: THUỐC TIÊU HÓA (ID 31-32)
(31, 'LOT-031-DRUG-031', '2026-03-10', '2029-03-10', 3, 50,  N'Boehringer Việt Nam',      GETDATE(), 15, 'ACTIVE', N'Nhập 50 hộp Bisacodyl',     NULL, NULL, NULL),
(32, 'LOT-032-DRUG-032', '2026-04-01', '2029-04-01', 3, 60,  N'Janssen Việt Nam',         GETDATE(), 16, 'ACTIVE', N'Nhập 60 hộp Loperamide',    NULL, NULL, NULL),

-- NHÓM 2.9 - 2.10: KHÁNG SINH & KHÁNG NẤM UỐNG (ID 33-37)
(33, 'LOT-033-DRUG-033', '2026-04-15', '2028-04-15', 3, 100, N'Công ty Dược Việt Nam',    GETDATE(), 13, 'ACTIVE', N'Nhập 100 hộp Amoxicillin', NULL, NULL, NULL),
(34, 'LOT-034-DRUG-034', '2026-05-01', '2029-05-01', 3, 80,  N'Bayer Việt Nam',            GETDATE(), 14, 'ACTIVE', N'Nhập 80 hộp Ciprofloxacin', NULL, NULL, NULL),
(35, 'LOT-035-DRUG-035', '2026-05-10', '2029-05-10', 3, 50,  N'Pfizer Việt Nam',          GETDATE(), 15, 'ACTIVE', N'Nhập 50 hộp Azithromycin',  NULL, NULL, NULL),
(36, 'LOT-036-DRUG-036', '2026-06-01', '2029-06-01', 3, 40,  N'Pfizer Việt Nam',          GETDATE(), 16, 'ACTIVE', N'Nhập 40 hộp Fluconazole',   NULL, NULL, NULL),
(37, 'LOT-037-DRUG-037', '2026-06-15', '2029-06-15', 3, 30,  N'Janssen Việt Nam',         GETDATE(), 13, 'ACTIVE', N'Nhập 30 hộp Itraconazole',  NULL, NULL, NULL),

-- NHÓM THUỐC BÔI (ID 38-41)
(38, 'LOT-038-DRUG-038', '2026-07-01', '2029-07-01', 3, 50,  N'Bayer Việt Nam',            GETDATE(), 14, 'ACTIVE', N'Nhập 50 hộp Clotrimazole',  NULL, NULL, NULL),
(39, 'LOT-039-DRUG-039', '2026-07-10', '2029-07-10', 3, 40,  N'Janssen Việt Nam',         GETDATE(), 15, 'ACTIVE', N'Nhập 40 hộp Ketoconazole',  NULL, NULL, NULL),
(40, 'LOT-040-DRUG-040', '2026-08-01', '2029-08-01', 3, 30,  N'Novartis Việt Nam',        GETDATE(), 16, 'ACTIVE', N'Nhập 30 hộp Terbinafine',   NULL, NULL, NULL),
(41, 'LOT-041-DRUG-041', '2026-08-15', '2029-08-15', 3, 40,  N'Janssen Việt Nam',         GETDATE(), 13, 'ACTIVE', N'Nhập 40 hộp Miconazole',    NULL, NULL, NULL);

-- ============================================================================
-- 3.8. Khởi tạo tồn kho mẫu (Giữ nguyên theo batchID 1 -> 41)
-- ============================================================================
INSERT INTO Inventory (batchID, quantityInStock, lastUpdated) VALUES
(1,  1500, GETDATE()), (2,  900,  GETDATE()), (3,  1200, GETDATE()),
(4,  750,  GETDATE()), (5,  600,  GETDATE()), (6,  450,  GETDATE()),
(7,  1800, GETDATE()), (8,  2400, GETDATE()), (9,  3000, GETDATE()),
(10, 900,  GETDATE()), (11, 500,  GETDATE()), (12, 1000, GETDATE()),
(13, 4000, GETDATE()), (14, 1000, GETDATE()), (15, 2400, GETDATE()),
(16, 1400, GETDATE()), (17, 700,  GETDATE()), (18, 1800, GETDATE()),
(19, 1000, GETDATE()), (20, 1000, GETDATE()), (21, 1800, GETDATE()),
(22, 1000, GETDATE()), (23, 2400, GETDATE()), (24, 1000, GETDATE()),
(25, 1800, GETDATE()), (26, 2400, GETDATE()), (27, 1000, GETDATE()),
(28, 1500, GETDATE()), (29, 1200, GETDATE()), (30, 3600, GETDATE()),
(31, 1000, GETDATE()), (32, 600,  GETDATE()), (33, 2000, GETDATE()),
(34, 800,  GETDATE()), (35, 150,  GETDATE()), (36, 40,   GETDATE()),
(37, 600,  GETDATE()), (38, 50,   GETDATE()), (39, 40,   GETDATE()),
(40, 30,   GETDATE()), (41, 40,   GETDATE());

-- ============================================================================
-- 3.9. Log xuất nhập kho (Cập nhật ghi chú cho đồng bộ với mã lô mới)
-- ============================================================================
INSERT INTO InventoryLog (batchID, userID, actionType, quantityChange, quantityBefore, quantityAfter, referenceID, referenceType, performedAt, notes) VALUES
(1,  11, 'IMPORT', 1500, 0, 1500, 1,  'BATCH', GETDATE(), N'Nhập 50 hộp Tamoxifen (DRUG-001)'),
(2,  11, 'IMPORT', 900,  0, 900,  2,  'BATCH', GETDATE(), N'Nhập 30 hộp Letrozole (DRUG-002)'),
(3,  11, 'IMPORT', 1200, 0, 1200, 3,  'BATCH', GETDATE(), N'Nhập 40 hộp Anastrozole (DRUG-003)'),
(4,  11, 'IMPORT', 750,  0, 750,  4,  'BATCH', GETDATE(), N'Nhập 25 hộp Exemestane (DRUG-004)'),
(5,  11, 'IMPORT', 600,  0, 600,  5,  'BATCH', GETDATE(), N'Nhập 20 hộp Imatinib (DRUG-005)'),
(6,  11, 'IMPORT', 450,  0, 450,  6,  'BATCH', GETDATE(), N'Nhập 15 hộp Erlotinib (DRUG-006)'),
(7,  11, 'IMPORT', 1800, 0, 1800, 7,  'BATCH', GETDATE(), N'Nhập 60 hộp Capecitabine (DRUG-007)'),
(8,  11, 'IMPORT', 2400, 0, 2400, 8,  'BATCH', GETDATE(), N'Nhập 80 hộp Cyclophosphamide (DRUG-008)'),
(9,  11, 'IMPORT', 3000, 0, 3000, 9,  'BATCH', GETDATE(), N'Nhập 100 hộp Methotrexate (DRUG-009)'),
(10, 11, 'IMPORT', 900,  0, 900,  10, 'BATCH', GETDATE(), N'Nhập 30 hộp Thalidomide (DRUG-010)'),
(11, 11, 'IMPORT', 500,  0, 500,  11, 'BATCH', GETDATE(), N'Nhập 25 hộp Morphine (DRUG-011)'),
(12, 11, 'IMPORT', 1000, 0, 1000, 12, 'BATCH', GETDATE(), N'Nhập 50 hộp Tramadol (DRUG-012)'),
(13, 11, 'IMPORT', 4000, 0, 4000, 13, 'BATCH', GETDATE(), N'Nhập 200 hộp Paracetamol (DRUG-013)'),
(14, 11, 'IMPORT', 1000, 0, 1000, 14, 'BATCH', GETDATE(), N'Nhập 100 hộp Ondansetron (DRUG-014)'),
(15, 11, 'IMPORT', 2400, 0, 2400, 15, 'BATCH', GETDATE(), N'Nhập 80 hộp Metoclopramide (DRUG-015)'),
(16, 11, 'IMPORT', 1400, 0, 1400, 16, 'BATCH', GETDATE(), N'Nhập 70 hộp Domperidone (DRUG-016)'),
(17, 11, 'IMPORT', 700,  0, 700,  17, 'BATCH', GETDATE(), N'Nhập 50 hộp Pantoprazole (DRUG-017)'),
(18, 11, 'IMPORT', 1800, 0, 1800, 18, 'BATCH', GETDATE(), N'Nhập 90 hộp Omeprazole (DRUG-018)'),
(19, 11, 'IMPORT', 1000, 0, 1000, 19, 'BATCH', GETDATE(), N'Nhập 100 hộp Cetirizine (DRUG-019)'),
(20, 11, 'IMPORT', 1000, 0, 1000, 20, 'BATCH', GETDATE(), N'Nhập 100 hộp Loratadine (DRUG-020)'),
(21, 11, 'IMPORT', 1800, 0, 1800, 21, 'BATCH', GETDATE(), N'Nhập 60 hộp Dexamethasone (DRUG-021)'),
(22, 11, 'IMPORT', 1000, 0, 1000, 22, 'BATCH', GETDATE(), N'Nhập 50 hộp Methylprednisolone (DRUG-022)'),
(23, 11, 'IMPORT', 2400, 0, 2400, 23, 'BATCH', GETDATE(), N'Nhập 80 hộp Prednisolone (DRUG-023)'),
(24, 12, 'IMPORT', 1000, 0, 1000, 24, 'BATCH', GETDATE(), N'Nhập 100 ống Vitamin C (DRUG-024)'),
(25, 12, 'IMPORT', 1800, 0, 1800, 25, 'BATCH', GETDATE(), N'Nhập 30 lọ Vitamin tổng hợp (DRUG-025)'),
(26, 12, 'IMPORT', 2400, 0, 2400, 26, 'BATCH', GETDATE(), N'Nhập 40 lọ Centrum (DRUG-026)'),
(27, 12, 'IMPORT', 1000, 0, 1000, 27, 'BATCH', GETDATE(), N'Nhập 50 ống Calcium + D3 (DRUG-027)'),
(28, 12, 'IMPORT', 1500, 0, 1500, 28, 'BATCH', GETDATE(), N'Nhập 50 lọ Omega-3 (DRUG-028)'),
(29, 12, 'IMPORT', 1200, 0, 1200, 29, 'BATCH', GETDATE(), N'Nhập 40 lọ Probiotics (DRUG-029)'),
(30, 12, 'IMPORT', 3600, 0, 3600, 30, 'BATCH', GETDATE(), N'Nhập 60 lọ Magnesium (DRUG-030)'),
(31, 12, 'IMPORT', 1000, 0, 1000, 31, 'BATCH', GETDATE(), N'Nhập 50 hộp Bisacodyl (DRUG-031)'),
(32, 12, 'IMPORT', 600,  0, 600,  32, 'BATCH', GETDATE(), N'Nhập 60 hộp Loperamide (DRUG-032)'),
(33, 12, 'IMPORT', 2000, 0, 2000, 33, 'BATCH', GETDATE(), N'Nhập 100 hộp Amoxicillin (DRUG-033)'),
(34, 12, 'IMPORT', 800,  0, 800,  34, 'BATCH', GETDATE(), N'Nhập 80 hộp Ciprofloxacin (DRUG-034)'),
(35, 12, 'IMPORT', 150,  0, 150,  35, 'BATCH', GETDATE(), N'Nhập 50 hộp Azithromycin (DRUG-035)'),
(36, 12, 'IMPORT', 40,   0, 40,   36, 'BATCH', GETDATE(), N'Nhập 40 hộp Fluconazole (DRUG-036)'),
(37, 12, 'IMPORT', 600,  0, 600,  37, 'BATCH', GETDATE(), N'Nhập 30 hộp Itraconazole (DRUG-037)'),
(38, 12, 'IMPORT', 50,   0, 50,   38, 'BATCH', GETDATE(), N'Nhập 50 hộp Clotrimazole (DRUG-038)'),
(39, 12, 'IMPORT', 40,   0, 40,   39, 'BATCH', GETDATE(), N'Nhập 40 hộp Ketoconazole (DRUG-039)'),
(40, 12, 'IMPORT', 30,   0, 30,   40, 'BATCH', GETDATE(), N'Nhập 30 hộp Terbinafine (DRUG-040)'),
(41, 12, 'IMPORT', 40,   0, 40,   41, 'BATCH', GETDATE(), N'Nhập 40 hộp Miconazole (DRUG-041)');


-- ============================================================================
-- BỔ SUNG: 3.10. Tạo đơn thuốc mẫu (Chỉ kê đơn cho các session 1, 2, 4 ĐÃ CÓ REVIEW)
-- ============================================================================
INSERT INTO Prescription (prescriptionCode, sessionID, patientID, doctorID, diagnosis, treatmentCycle, status, notes)
VALUES
('RX-202607-001', 1, 1, 3, N'Ung thư vú giai đoạn II, sau phẫu thuật', N'Chu kỳ 3/6', 'PENDING', N'Bệnh nhân có biểu hiện thiếu máu nhẹ, bổ sung vi chất'),
('RX-202607-002', 2, 2, 4, N'Ung thư đại trực tràng giai đoạn IV, di căn gan', N'Chu kỳ 1/4', 'PENDING', N'Theo dõi sát chức năng tiêu hóa và tình trạng nôn nghén'),
('RX-202607-003', 4, 4, 3, N'Ung thư dạ dày giai đoạn III, đau xơ hóa vùng thượng vị', N'Điều trị giảm nhẹ', 'PENDING', N'Bệnh nhân nuốt nghẹn, đau nhiều, cần bọc dạ dày kỹ');

-- ============================================================================
-- BỔ SUNG: 3.11. Chi tiết đơn thuốc (Bác sĩ kê theo VIÊN)
-- ============================================================================
INSERT INTO PrescriptionDetail (
    prescriptionID, drugID, dosePerTime, timesPerDay, daysOfTreatment, 
    quantityPrescribed, batchID, quantityDispensed, actualExpiryDate, 
    dispenseUnit, instruction, dispensedAt, dispensedBy, notes, status
)
VALUES
-- ------------------------------------------------------------------------------------------------------------------------------------------------
-- Đơn 1 (PrescriptionID = 1, SessionID = 1): Đã xuất thuốc hoàn tất (DISPENSED) bởi Dược sĩ
-- ------------------------------------------------------------------------------------------------------------------------------------------------
(1, 3,  1,   1, 30, 30, 1, 30, '2027-12-31', N'VIÊN', N'Uống vào một khung giờ cố định mỗi ngày', '2026-07-20 09:15:00', 13, N'Anastrozole 1mg/ngày', 'DISPENSED'),
(1, 13, 1,   2, 5,  10, 2, 10, '2027-06-30', N'VIÊN', N'Uống khi có sốt hoặc đau xương > 38.5 độ', '2026-07-20 09:15:00', 13, N'Paracetamol giảm đau sốt', 'DISPENSED'),
(1, 26, 1,   1, 30, 30, 3, 30, '2028-01-15', N'VIÊN', N'Uống sau ăn sáng', '2026-07-20 09:15:00', 13, N'Centrum bổ sung đa vi chất', 'DISPENSED'),
(1, 30, 1,   2, 15, 30, 4, 30, '2027-10-20', N'VIÊN', N'Uống sáng và tối sau ăn', '2026-07-20 09:15:00', 13, N'Magnesium giảm co thắt cơ', 'DISPENSED'),

-- ------------------------------------------------------------------------------------------------------------------------------------------------
-- Đơn 2 (PrescriptionID = 2, SessionID = 2): Đã xuất thuốc hoàn tất (DISPENSED) bởi Dược sĩ
-- ------------------------------------------------------------------------------------------------------------------------------------------------
(2, 7,  1500,2, 14, 28, 5, 28, '2027-08-31', N'VIÊN', N'Uống trong vòng 30 phút sau bữa ăn sáng và tối', '2026-07-21 10:30:00', 14, N'Capecitabine 500mg x 3 viên/lần', 'DISPENSED'),
(2, 15, 1,   3, 5,  15, 6, 15, '2027-05-15', N'VIÊN', N'Uống trước 3 bữa ăn 30 phút', '2026-07-21 10:30:00', 14, N'Metoclopramide chống nôn', 'DISPENSED'),
(2, 18, 1,   1, 20, 20, 7, 20, '2028-03-10', N'VIÊN', N'Uống trước ăn sáng 30 phút', '2026-07-21 10:30:00', 14, N'Omeprazole bảo vệ dạ dày', 'DISPENSED'),

-- ------------------------------------------------------------------------------------------------------------------------------------------------
-- Đơn 3 (PrescriptionID = 3, SessionID = 4): Đang chờ nhà thuốc xuất (PENDING - Chưa gán lô, chưa xuất)
-- ------------------------------------------------------------------------------------------------------------------------------------------------
(3, 1,  30,  2, 15, 30, NULL, 0, NULL, N'VIÊN', N'Uống cách mỗi 12 giờ cố định', NULL, NULL, N'Morphine SR 30mg giảm đau nền', 'PENDING'),
(3, 12, 1,   2, 10, 20, NULL, 0, NULL, N'VIÊN', N'Uống xen kẽ khi có các cơn đau quặn cấp', NULL, NULL, N'Tramadol giảm đau bộc phát', 'PENDING'),
(3, 16, 1,   3, 7,  21, NULL, 0, NULL, N'VIÊN', N'Uống trước ăn 15-30 phút', NULL, NULL, N'Domperidone giảm chướng bụng', 'PENDING');

GO


------------------------------------------------------------
-- BƯỚC 0: Đảm bảo đủ 12 loại chẩn đoán (bỏ qua nếu tên đã tồn tại)
------------------------------------------------------------
INSERT INTO DiseaseType (name)
SELECT v.name FROM (VALUES
    (N'Bình thường'),
    (N'Bình thường / Không phát hiện bất thường'),
    (N'Cần theo dõi thêm / Chưa đủ dữ liệu kết luận'),
    (N'fbgfdbfd'),
    (N'Loạn sản cổ tử cung nặng (CIN 2/3)'),
    (N'Loạn sản cổ tử cung nhẹ (CIN 1)'),
    (N'Tổn thương biểu mô vảy mức độ cao (HSIL/CIN 2-3)'),
    (N'Tổn thương biểu mô vảy mức độ thấp (LSIL/CIN 1)'),
    (N'Ung thư biểu mô tại chỗ (CIS)'),
    (N'Ung thư cổ tử cung xâm lấn giai đoạn muộn (III-IV)'),
    (N'Ung thư cổ tử cung xâm lấn giai đoạn sớm (I-II)'),
    (N'Viêm cổ tử cung')
) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM DiseaseType d WHERE d.name = v.name);


------------------------------------------------------------
-- 1. Bình thường — 37 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),(32),(33),(34),(35),(36),(37)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (37)
    N'Không phát hiện bất thường, tái khám định kỳ theo lịch tầm soát',
    N'Duy trì tầm soát định kỳ 1-3 năm/lần',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Bình thường') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 2. Bình thường / Không phát hiện bất thường — 14 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (14)
    N'Không cần điều trị, tái khám định kỳ theo lịch tầm soát',
    N'Duy trì tầm soát định kỳ 1-3 năm/lần',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Bình thường / Không phát hiện bất thường') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 3. Cần theo dõi thêm / Chưa đủ dữ liệu kết luận — 9 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (9)
    N'Cần bổ sung xét nghiệm, hẹn tái khám sớm để có kết luận rõ ràng',
    N'Thực hiện thêm xét nghiệm theo chỉ định của bác sĩ',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Cần theo dõi thêm / Chưa đủ dữ liệu kết luận') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 4. fbgfdbfd — 1 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (1)
    N'Dữ liệu test', N'Dữ liệu test',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'fbgfdbfd') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 5. Loạn sản cổ tử cung nặng (CIN 2/3) — 7 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (7)
    N'Chỉ định soi cổ tử cung, cân nhắc khoét chóp (LEEP)',
    N'Cần tái khám đúng hẹn, không tự ý bỏ điều trị',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Loạn sản cổ tử cung nặng (CIN 2/3)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 6. Loạn sản cổ tử cung nhẹ (CIN 1) — 21 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (21)
    N'Theo dõi, làm lại xét nghiệm tế bào sau 6-12 tháng',
    N'Tăng cường theo dõi, tránh yếu tố nguy cơ (thuốc lá, HPV)',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Loạn sản cổ tử cung nhẹ (CIN 1)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 7. Tổn thương biểu mô vảy mức độ cao (HSIL/CIN 2-3) — 10 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (10)
    N'Soi cổ tử cung, sinh thiết xác định, cân nhắc LEEP/khoét chóp',
    N'Cần điều trị sớm, tái khám đúng hẹn',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Tổn thương biểu mô vảy mức độ cao (HSIL/CIN 2-3)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 8. Tổn thương biểu mô vảy mức độ thấp (LSIL/CIN 1) — 14 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (14)
    N'Theo dõi định kỳ, xét nghiệm lại sau 6-12 tháng',
    N'Duy trì theo dõi, tái khám đúng hẹn',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Tổn thương biểu mô vảy mức độ thấp (LSIL/CIN 1)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 9. Ung thư biểu mô tại chỗ (CIS) — 3 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (3)
    N'Chuyển tuyến chuyên khoa ung bướu, hội chẩn đa chuyên khoa',
    N'Cần điều trị sớm, tuân thủ lịch hẹn chuyên khoa',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Ung thư biểu mô tại chỗ (CIS)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 10. Ung thư cổ tử cung xâm lấn giai đoạn muộn (III-IV) — 12 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (12)
    N'Hội chẩn đa chuyên khoa, phác đồ hóa-xạ trị kết hợp',
    N'Cần nhập viện điều trị chuyên sâu, theo dõi sát',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Ung thư cổ tử cung xâm lấn giai đoạn muộn (III-IV)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 11. Ung thư cổ tử cung xâm lấn giai đoạn sớm (I-II) — 8 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (8)
    N'Phẫu thuật triệt căn hoặc xạ trị theo giai đoạn',
    N'Tuân thủ phác đồ điều trị, tái khám định kỳ',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Ung thư cổ tử cung xâm lấn giai đoạn sớm (I-II)') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;


------------------------------------------------------------
-- 12. Viêm cổ tử cung — 17 ca
------------------------------------------------------------
INSERT INTO DiagnosisSession (weight, height, status, createdAt, patientID, userID, clinicalInputMode, isShared)
SELECT 45 + (ABS(CHECKSUM(NEWID())) % 35), 148 + (ABS(CHECKSUM(NEWID())) % 27), 'COMPLETED',
       DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 365), GETDATE()), pt.patientID, dc.userID,
       CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'DOCTOR' ELSE 'PATIENT' END, 0
FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17)) AS t(n)
CROSS APPLY (SELECT TOP 1 patientID FROM Patient WHERE t.n = t.n ORDER BY NEWID()) pt
CROSS APPLY (SELECT TOP 1 u.userID FROM Users u JOIN Role r ON u.roleID = r.roleID WHERE r.roleName = 'DOCTOR' AND t.n = t.n ORDER BY NEWID()) dc;

INSERT INTO Review (treatmentPlan, doctorAdvice, note, reviewedAt, sessionID, userID, diseaseTypeID)
SELECT TOP (17)
    N'Kháng sinh đường âm đạo 7-10 ngày, tái khám sau 2 tuần',
    N'Giữ vệ sinh, tránh quan hệ trong thời gian điều trị',
    N'Dữ liệu khôi phục', DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 5, ds.createdAt),
    ds.sessionID, ds.userID, dt.diseaseTypeID
FROM DiagnosisSession ds
CROSS JOIN (SELECT diseaseTypeID FROM DiseaseType WHERE name = N'Viêm cổ tử cung') dt
WHERE NOT EXISTS (SELECT 1 FROM Review r WHERE r.sessionID = ds.sessionID)
ORDER BY ds.sessionID DESC;



select * from DiagnosisSession where sessionID = 1

select * from Symptom
select * from SymptomResult where sessionID = 1
select * from SymptomDetails where symptomResultID = 1

select * from Parameter
select * from LabResult
select * from LabResultParameter

select * from SystemLog

select * from Users
select * from Patient

DELETE FROM SystemLog WHERE logID = 9

select * from Users order by createdAt desc


SELECT drugID, drugCode FROM Drug ORDER BY drugID;


select * from Drug