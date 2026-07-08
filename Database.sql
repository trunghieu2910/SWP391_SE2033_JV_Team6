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
    status       VARCHAR(20), -- ACTIVE , PENDING , BLOCKED
    lastChangePassTime DATETIME,
	lastLogoutTime DATETIME,
    createdAt    DATETIME     DEFAULT GETDATE(),
    nationalID   CHAR(12),
    FOREIGN KEY (roleID) REFERENCES Role (roleID)
);

CREATE TABLE Patient
(
    patientID INT IDENTITY (1,1) PRIMARY KEY,
    gender    NVARCHAR(10),
    dob       DATE,
    address   NVARCHAR(255),
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

CREATE TABLE MedicalImageDetails
(
    imageID        INT IDENTITY (1,1) PRIMARY KEY,
    medicalImageID INT           NOT NULL,
    imageUrl       NVARCHAR(255) NOT NULL,
    aiImageUrl     NVARCHAR(255) NULL,
    confidenceScore FLOAT NULL,
    uploadedAt     DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (medicalImageID) REFERENCES MedicalImage (medicalImageID)
);

-- ==========================================
-- 4. REVIEW BÁC SĨ
-- ==========================================
CREATE TABLE Review
(
    reviewID       INT IDENTITY (1,1) PRIMARY KEY,
    sessionID      INT UNIQUE NOT NULL,
    userID         INT        NOT NULL, 
    finalDiagnosis NVARCHAR(MAX),
    treatmentPlan  NVARCHAR(MAX),
    doctorAdvice   NVARCHAR(MAX),
    note           NVARCHAR(MAX),
    reviewedAt     DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID),
    FOREIGN KEY (userID) REFERENCES [Users] (userID)
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
VALUES (N'ADMIN'), (N'DOCTOR'), (N'AITRAINER'), (N'PATIENT');

-- USERS
INSERT INTO [Users]
(roleID, username, fullName, email, passwordHash, phoneNumber, status, lastChangePassTime, nationalID)
VALUES
(4, 'patient_nam', N'Phạm Thùy Linh', 'namvipnhatgt@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445466', 'ACTIVE', GETDATE(), '043678901234'),
(1, 'admin_system', N'Quản trị viên', 'luugiang205@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0999999999', 'ACTIVE', GETDATE(), '001234567890'),
(2, 'dr_nguyen', N'Bác sĩ Nguyễn Văn Tùng', 'likey2404@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0912345678', 'ACTIVE', GETDATE(), '012345678901'),
(2, 'dr_tran', N'Bác sĩ Trần Thị Mai', 'mai.tran@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0987654321', 'ACTIVE', GETDATE(), '023456789012'),
(4, 'patient_hoang', N'Lê Minh Hoàng', 'hoang.le@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0901112233', 'ACTIVE', GETDATE(), '034567890123'),
(4, 'patient_linh', N'Phạm Thùy Linh', 'linh.pham@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445566', 'ACTIVE', GETDATE(), '045678901234'),
(4, 'patient_huong', N'Nguyễn Thu Hương', 'huong.nguyen@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0905556677', 'ACTIVE', GETDATE(), '056789012345'),
(4, 'patient_lan', N'Trần Thị Lan', 'lan.tran@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0906667788', 'PENDING', GETDATE(), '067890123456'),
(4, 'patient_my', N'Đỗ Thanh Mỹ', 'my.do@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0907778899', 'BANNED', GETDATE(), '078901234567');

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
INSERT INTO LabResult (sessionID, testType, status)
VALUES
    (1, N'Xét nghiệm máu tổng quát', 'COMPLETED'),
    (2, N'Kiểm tra đường huyết', 'PENDING');

-- LabResultParameter
INSERT INTO LabResultParameter (labResultID, parameterID, value)
VALUES
    (1, 1, '4.8'), 
    (1, 2, '8.5'); 

-- MedicalImage
INSERT INTO MedicalImage (sessionID, imageType, status)
VALUES
    (1, 'X-Ray Phổi', 'COMPLETED'),
    (2, 'Siêu âm bụng', 'PENDING');

-- MedicalImageDetails
INSERT INTO MedicalImageDetails (medicalImageID, imageUrl)
VALUES
    (1, 'https://storage.hospital.com/xray/2026/session1_lung.jpg');

-- Review
INSERT INTO Review (sessionID, userID, finalDiagnosis, treatmentPlan, doctorAdvice, note)
VALUES
    (1, 3, N'Viêm phế quản cấp tính', N'Kê đơn kháng sinh 5 ngày, siro ho', N'Tránh nước đá, giữ ấm cổ họng', N'Bệnh nhân có tiền sử dị ứng thời tiết');

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



select * from DiagnosisSession where sessionID = 1

select * from Symptom
select * from SymptomResult where sessionID = 1
select * from SymptomDetails where symptomResultID = 1

select * from Parameter
select * from LabResult
select * from LabResultParameter

select * from SystemLog

DELETE FROM SystemLog WHERE logID = 9

select * from Users order by createdAt desc