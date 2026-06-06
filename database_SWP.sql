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
    status       VARCHAR(20), -- ACTIVE , PENDING , BLOCKED
    lastChangePassTime DATETIME,
    createdAt    DATETIME     DEFAULT GETDATE(),
    nationalID   CHAR(12) UNIQUE,
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
    FOREIGN KEY (userID) REFERENCES [Users] (userID),
    FOREIGN KEY (patientID) REFERENCES Patient (patientID)
);

-- Bảng mới: SymptomResult (Mối quan hệ 1-1 với DiagnosisSession)
CREATE TABLE SymptomResult
(
    symptomResultID INT IDENTITY (1,1) PRIMARY KEY,
    sessionID       INT UNIQUE NOT NULL, -- Đảm bảo quan hệ 1-1 nhờ UNIQUE
    status          NVARCHAR(50) DEFAULT 'PENDING',
    createdAt       DATETIME DEFAULT GETDATE(),
    menopauseStatus NVARCHAR(50),
    symptomDuration NVARCHAR(50),
    symptomProgressing BIT,
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
);

-- Bảng trung gian: Chuyển sang quan hệ 1-n với SymptomResult
CREATE TABLE SymptomDetails
(
    symptomDetailsID INT IDENTITY (1,1) PRIMARY KEY,
    symptomResultID  INT NOT NULL, -- Thay đổi từ sessionID sang symptomResultID
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
    status      NVARCHAR(50) default 'PENDING', -- PENDING , COMPLETED , CANCLED
    createdAt   DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
);

CREATE TABLE LabResultParameter
(
    LabResultParameterID INT IDENTITY (1,1) PRIMARY KEY,
    labResultID INT NOT NULL,
    parameterID INT NOT NULL,
    value       NVARCHAR(100),
    FOREIGN KEY (labResultID) REFERENCES LabResult (labResultID),
    FOREIGN KEY (parameterID) REFERENCES Parameter (parameterID)
);

CREATE TABLE MedicalImage
(
    medicalImageID INT IDENTITY (1,1) PRIMARY KEY,
    sessionID      INT NOT NULL,
    imageType      NVARCHAR(50),
    status         NVARCHAR(50) DEFAULT 'PENDING', -- PENDING , COMPLETED , CANCLED
    createdAt      DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
);

CREATE TABLE MedicalImageDetails
(
    imageID        INT IDENTITY (1,1) PRIMARY KEY,
    medicalImageID INT           NOT NULL,
    imageUrl       NVARCHAR(255) NOT NULL,
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
    userID         INT        NOT NULL, -- Bác sĩ review
    verdict        NVARCHAR(50), -- phan loại: CONFIRM , REJECT , MODIFY
    finalDiagnosis NVARCHAR(MAX),
    icd10Code      VARCHAR(20), -- ma cua benh
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
    targetType  VARCHAR(50), -- ten cua bang ma minnh tac dong nen
    targetID    INT, -- id cua record ma minh tac dong nen
    action      VARCHAR(50),
    description NVARCHAR(MAX),
    performedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (userID) REFERENCES [Users] (userID)
);
GO

--======================================================================================================================
-- PHẦN CHÈN DỮ LIỆU
--======================================================================================================================

-- ROLE
INSERT INTO [Role] (roleName)
VALUES (N'ADMIN'), (N'DOCTOR'), (N'AITRAINER'), (N'PATIENT');

-- USERS
INSERT INTO [Users]
(roleID, username, fullName, email, passwordHash, phoneNumber, status, lastChangePassTime, nationalID)
VALUES
(4, 'patient_nam', N'Phạm Thùy Linh', 'namvipnhatgt@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445466', 'ACTIVE', GETDATE(), '043678901234'),
(1, 'admin_system', N'Quản trị viên', 'admin@hospital.com', 'hashed_password_123', '0999999999', 'ACTIVE', GETDATE(), '001234567890'),
(2, 'dr_nguyen', N'Bác sĩ Nguyễn Văn Tùng', 'likey2404@gmail.com', '$2a$10$isOoAiOotmFDzvDafB0hjOYSsqgyAf18oqSEIjF00s4fWUVa9Hu5u', '0912345678', 'ACTIVE', GETDATE(), '012345678901'),
(2, 'dr_tran', N'Bác sĩ Trần Thị Mai', 'mai.tran@hospital.com', 'hashed_password_123', '0987654321', 'ACTIVE', GETDATE(), '023456789012'),
(4, 'patient_hoang', N'Lê Minh Hoàng', 'hoang.le@email.com', 'hashed_password_123', '0901112233', 'ACTIVE', GETDATE(), '034567890123'),
(4, 'patient_linh', N'Phạm Thùy Linh', 'linh.pham@email.com', 'hashed_password_123', '0904445566', 'ACTIVE', GETDATE(), '045678901234'),
(4, 'patient_huong', N'Nguyễn Thu Hương', 'huong.nguyen@email.com', 'hashed_password_123', '0905556677', 'ACTIVE', GETDATE(), '056789012345'),
(4, 'patient_lan', N'Trần Thị Lan', 'lan.tran@email.com', 'hashed_password_123', '0906667788', 'ACTIVE', GETDATE(), '067890123456'),
(4, 'patient_my', N'Đỗ Thanh Mỹ', 'my.do@email.com', 'hashed_password_123', '0907778899', 'ACTIVE', GETDATE(), '078901234567');

-- PATIENT
INSERT INTO Patient (gender, dob, address, userID)
VALUES
(N'Male', '1995-08-15', N'Cầu Giấy, Hà Nội', 1),
(N'Female', '2001-02-20', N'Thanh Xuân, Hà Nội', 6),
(N'Female', '1988-06-12', N'Ba Đình, Hà Nội', 7),
(N'Female', '1975-11-25', N'Đống Đa, Hà Nội', 8),
(N'Female', '1992-09-08', N'Hoàng Mai, Hà Nội', 9);

-- Symptom
INSERT INTO Symptom (symptomName) VALUES
(N'Chưa mãn kinh'), (N'Đã mãn kinh'),
(N'Ra máu giữa kỳ kinh'), (N'Kinh nguyệt kéo dài bất thường'), (N'Rong kinh'), (N'Ra máu sau mãn kinh'), (N'Ra máu sau quan hệ'),
(N'Khí hư nhiều'), (N'Khí hư có mùi hôi'), (N'Khí hư lẫn máu'), (N'Dịch tiết màu nâu'),
(N'Đau vùng chậu'), (N'Đau bụng dưới'), (N'Đau lưng dưới'), (N'Đau khi quan hệ'),
(N'Sụt cân không rõ nguyên nhân'), (N'Mệt mỏi kéo dài'), (N'Chán ăn'),
(N'Tiểu nhiều lần'), (N'Tiểu buốt'), (N'Tiểu khó'), (N'Tiểu ra máu'),
(N'Táo bón kéo dài'), (N'Đầy bụng'), (N'Chướng bụng'), (N'Buồn nôn'),
(N'Tiền sử gia đình mắc ung thư phụ khoa'), (N'Béo phì'), (N'Đái tháo đường'), (N'Tăng huyết áp'), (N'Hội chứng buồng trứng đa nanoc (PCOS)'), (N'Điều trị estrogen kéo dài'),
(N'Triệu chứng dưới 1 tháng'), (N'Triệu chứng 1-3 tháng'), (N'Triệu chứng 3-6 tháng'), (N'Triệu chứng trên 6 tháng'),
(N'Triệu chứng nặng dần');

-- Parameter
INSERT INTO Parameter (parameterName, unit)
VALUES (N'Hồng cầu (RBC)', 'T/L'), (N'Bạch cầu (WBC)', 'G/L'), (N'Đường huyết (Glucose)', 'mmol/L');

-- DiagnosisSession
INSERT INTO DiagnosisSession (userID, patientID, weight, height, status, isShared)
VALUES
(3,1,52,155,N'Completed',0),
(4,2,58,160,N'Completed',1),
(3,3,61,158,N'Pending',0),
(4,4,49,152,N'Completed',0),
(3,5,67,162,N'Pending',1);

-- Thêm dữ liệu vào bảng mới: SymptomResult (Mối quan hệ 1-1 ứng với 5 session ở trên)
INSERT INTO SymptomResult (sessionID, status) VALUES
(1, N'Completed'),
(2, N'Completed'),
(3, N'Pending'),
(4, N'Completed'),
(5, N'Pending');

-- Thêm chi tiết triệu chứng vào SymptomDetails (Được map thông qua symptomResultID thay vì sessionID)
INSERT INTO SymptomDetails (symptomResultID, symptomID) VALUES
(1,2),(1,6),(1,12),(1,17),         -- Thuộc về kết quả triệu chứng của phiên 1
(2,1),(2,3),(2,8),(2,13),(2,18),     -- Thuộc về kết quả triệu chứng của phiên 2
(3,2),(3,10),(3,14),(3,16),(3,29),   -- Thuộc về kết quả triệu chứng của phiên 3
(4,1),(4,4),(4,9),(4,13),(4,24),     -- Thuộc về kết quả triệu chứng của phiên 4
(5,2),(5,6),(5,12),(5,17),(5,30);    -- Thuộc về kết quả triệu chứng của phiên 5

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
INSERT INTO Review (sessionID, userID, verdict, finalDiagnosis, icd10Code, treatmentPlan, doctorAdvice, note)
VALUES
    (1, 3, 'CONFIRM', N'Viêm phế quản cấp tính', 'J20.9', N'Kê đơn kháng sinh 5 ngày, siro ho', N'Tránh nước đá, giữ ấm cổ họng', N'Bệnh nhân có tiền sử dị ứng thời tiết');

-- SystemLog
INSERT INTO SystemLog (userID, targetType, targetID, action, description)
VALUES
    (1, 'User', 2, 'CREATE', N'Admin tạo tài khoản cho Bác sĩ Tùng'),
    (4, 'DiagnosisSession', 1, 'CREATE', N'Bệnh nhân Hoàng đăng ký lịch khám'),
    (2, 'Review', 1, 'CREATE', N'Bác sĩ Tùng chốt kết quả chẩn đoán cho phiên khám 1');
GO