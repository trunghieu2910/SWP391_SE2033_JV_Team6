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
VALUES (N'ADMIN'), (N'DOCTOR'), (N'AITRAINER'), (N'PATIENT'),(N'PHARMACIST');

-- USERS
INSERT INTO [Users]
(roleID, username, fullName, email, passwordHash, phoneNumber, status, lastChangePassTime, nationalID)
VALUES
(4, 'patient_nam', N'Phạm Thùy Linh', 'ntpl2404@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445466', 'ACTIVE', GETDATE(), '043678901234'),
(1, 'admin_system', N'Quản trị viên', 'luugiang205@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0999999999', 'ACTIVE', GETDATE(), '001234567890'),
(2, 'dr_nguyen', N'Bác sĩ Nguyễn Văn Tùng', 'likey2404@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0912345678', 'ACTIVE', GETDATE(), '012345678901'),
(2, 'dr_tran', N'Bác sĩ Trần Thị Mai', 'mai.tran@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0987654321', 'ACTIVE', GETDATE(), '023456789012'),
(4, 'patient_hoang', N'Lê Minh Hoàng', 'hoang.le@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0901112233', 'ACTIVE', GETDATE(), '034567890123'),
(4, 'patient_linh', N'Phạm Thùy Linh', 'linh.pham@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445566', 'ACTIVE', GETDATE(), '045678901234'),
(4, 'patient_huong', N'Nguyễn Thu Hương', 'huong.nguyen@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0905556677', 'ACTIVE', GETDATE(), '056789012345'),
(4, 'patient_lan', N'Trần Thị Lan', 'lan.tran@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0906667788', 'PENDING', GETDATE(), '067890123456'),
(4, 'patient_my', N'Đỗ Thanh Mỹ', 'my.do@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0907778899', 'BANNED', GETDATE(), '078901234567'),
(5, 'pharmacist', N'Đỗ Trà My', 'bangyeong5678@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0904445566', 'ACTIVE', GETDATE(), '043679901234');

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
    drugID INT IDENTITY(1,1) PRIMARY KEY,
    drugCode VARCHAR(20) NOT NULL UNIQUE,
    drugName NVARCHAR(200) NOT NULL,
    strength DECIMAL(10,2) NOT NULL, -- Hàm lượng trong 1 đơn vị sử dụng
    strengthUnit VARCHAR(10) NOT NULL, -- mg, g, mcg, IU, %
    dosageForm NVARCHAR(50) NOT NULL, --Dạng bào chế - Viên nén bao phim, Dung dịch tiêm
    routeOfAdministration NVARCHAR(50) NOT NULL, --Đường dùng - Uống, Tiêm tĩnh mạch
    subCategoryID INT NOT NULL,
    packaging NVARCHAR(100), --Quy cách đóng gói - Hộp 30 viên, Lọ 450mg/45ml
    manufacturer NVARCHAR(100), --Nhà sản xuất
    countryOfOrigin NVARCHAR(50), --Nước sản xuất
    storageCondition NVARCHAR(200), --Điều kiện bảo quản - 15-25°C, tránh ánh sáng
    shelfLifeMonths INT DEFAULT 24, --	Hạn sử dụng - tính theo tháng, mặc định 24 tháng
    notes NVARCHAR(500),
    status TINYINT DEFAULT 1, -- 1: Đang dùng, 0: Ngừng
    createdAt DATETIME DEFAULT GETDATE(),
    createdBy INT, -- userID của người tạo (Dược sĩ/Admin)
    FOREIGN KEY (subCategoryID) REFERENCES DrugSubCategory(subCategoryID),
    FOREIGN KEY (createdBy) REFERENCES [Users](userID)
);

-- 2.5. Quy đổi đơn vị (Nhập theo LỌ/VỈ, Xuất theo VIÊN/ml)
CREATE TABLE UnitConversion (
    conversionID INT IDENTITY(1,1) PRIMARY KEY,
    drugID INT NOT NULL,
    largeUnitID INT NOT NULL, -- Đơn vị nhập kho (LỌ, VỈ, HỘP)
    smallUnitID INT NOT NULL, -- Đơn vị sử dụng (VIÊN, ỐNG, ml)
    conversionQuantity INT NOT NULL, -- 1 đơn vị lớn = bao nhiêu đơn vị nhỏ
    createdAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (drugID) REFERENCES Drug(drugID),
    FOREIGN KEY (largeUnitID) REFERENCES Unit(unitID),
    FOREIGN KEY (smallUnitID) REFERENCES Unit(unitID)
);

-- 2.6. Lô hàng nhập kho
CREATE TABLE DrugBatch (
    batchID INT IDENTITY(1,1) PRIMARY KEY,
    drugID INT NOT NULL,
    batchNumber VARCHAR(50) NOT NULL,
    manufactureDate DATE NOT NULL,
    expiryDate DATE NOT NULL, -- Hạn sử dụng (EXP)
    unitID INT NOT NULL, -- Đơn vị nhập (LỌ/VỈ/HỘP)
    quantity INT NOT NULL, -- Số lượng theo đơn vị nhập
    importPrice DECIMAL(18,2) DEFAULT 0,
    supplier NVARCHAR(200),
    importDate DATETIME DEFAULT GETDATE(),
    importedBy INT NOT NULL, -- Dược sĩ nhập kho
    status TINYINT DEFAULT 1, -- 1: Đang dùng, 0: Ngừng xuất
    notes NVARCHAR(500),
    FOREIGN KEY (drugID) REFERENCES Drug(drugID),
    FOREIGN KEY (unitID) REFERENCES Unit(unitID),
    FOREIGN KEY (importedBy) REFERENCES [Users](userID),
    UNIQUE (batchNumber, drugID)
);

-- 2.7. Tồn kho chi tiết (Tính theo đơn vị nhỏ nhất)
CREATE TABLE Inventory (
    inventoryID INT IDENTITY(1,1) PRIMARY KEY,
    batchID INT NOT NULL,
    quantityInStock INT NOT NULL DEFAULT 0, -- Tồn theo đơn vị nhỏ nhất (VIÊN/ỐNG/ml)
    lastUpdated DATETIME DEFAULT GETDATE(),
    status TINYINT DEFAULT 1, -- 1: Bình thường, 2: Sắp hết (<30 ngày), 0: Hết
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
    status TINYINT DEFAULT 0, -- 0: Mới kê, 1: Đã cấp phát, 2: Đã hủy
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

-- =============================================
-- 3. CHÈN DỮ LIỆU MẪU
-- =============================================

-- 3.1. Đơn vị tính
INSERT INTO Unit (unitName, description) VALUES 
('VIÊN', N'Đơn vị phân phối nhỏ nhất cho thuốc viên'),
('ỐNG', N'Ống tiêm lẻ'),
('LỌ', N'Đóng gói thương mại - lọ'),
('VỈ', N'Đóng gói thương mại - vỉ nhôm'),
('HỘP', N'Đóng gói thương mại - hộp giấy'),
('ml', N'Mililit - dung dịch tiêm hoặc si-rô');

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
(2, N'Corticoid', 2, 1, N'Chống viêm, chống dị ứng, giảm phù nề');

-- 3.4. Danh mục thuốc
INSERT INTO Drug (drugCode, drugName, strength, strengthUnit, dosageForm, routeOfAdministration, subCategoryID, packaging, manufacturer, countryOfOrigin, storageCondition, shelfLifeMonths, notes, createdBy) VALUES
-- Thuốc điều trị ung thư cổ tử cung
('DRUG-001', N'Tamoxifen (Nolvadex)', 20, 'mg', N'Viên nén bao phim', N'Uống', 1, N'Hộp 30 viên', 'AstraZeneca', N'Anh', N'15-25°C, tránh ẩm', 36, N'Uống sau bữa ăn', 2),
('DRUG-002', N'Letrozole (Femara)', 2.5, 'mg', N'Viên nén bao phim', N'Uống', 1, N'Hộp 30 viên', 'Novartis', N'Thụy Sĩ', N'15-30°C', 36, N'Uống xa bữa ăn', 2),
('DRUG-003', N'Carboplatin (Paraplatin)', 450, 'mg/45ml', N'Dung dịch tiêm', N'Tiêm tĩnh mạch', 3, N'Lọ 450mg/45ml', 'Teva', N'Israel', N'15-25°C, tránh ánh sáng', 24, N'Truyền 30 phút - 1 giờ, tính liều theo AUC', 2),
('DRUG-004', N'Paclitaxel (Taxol)', 300, 'mg/50ml', N'Dung dịch tiêm', N'Tiêm tĩnh mạch', 3, N'Lọ 300mg/50ml', 'Bristol-Myers', N'Mỹ', N'20-25°C, tránh ánh sáng', 24, N'Cần tiền thuốc chống dị ứng, truyền 3 giờ', 2),
('DRUG-005', N'Bevacizumab (Avastin)', 100, 'mg/4ml', N'Dung dịch tiêm', N'Tiêm tĩnh mạch', 2, N'Lọ 100mg/4ml', 'Roche', N'Thụy Sĩ', N'2-8°C, tránh ánh sáng', 36, N'Truyền sau hóa trị, không dùng cùng lúc với hóa chất', 2),
('DRUG-006', N'Pembrolizumab (Keytruda)', 100, 'mg/4ml', N'Dung dịch tiêm', N'Tiêm tĩnh mạch', 4, N'Lọ 100mg/4ml', 'MSD', N'Mỹ', N'2-8°C, không đông đá', 36, N'Truyền 30 phút, dùng cho PD-L1 dương tính', 2),
-- Thuốc hỗ trợ
('DRUG-007', N'Morphine SR (MST Continus)', 30, 'mg', N'Viên giải phóng kéo dài', N'Uống', 5, N'Hộp 20 viên', 'Mundipharma', N'Anh', N'15-25°C', 36, N'Uống cách 12 giờ, không nghiền nát', 2),
('DRUG-008', N'Ondansetron (Zofran)', 8, 'mg', N'Viên nén bao phim', N'Uống', 6, N'Hộp 10 viên', 'GSK', N'Anh', N'15-30°C', 24, N'Uống 1 giờ trước hóa trị', 2),
('DRUG-009', N'Pantoprazole (Pantozol)', 40, 'mg', N'Viên nén bao phim', N'Uống', 7, N'Hộp 14 viên', 'Takeda', N'Nhật', N'15-25°C', 36, N'Uống trước bữa ăn 30 phút', 2),
('DRUG-010', N'Dexamethasone (Decadron)', 4, 'mg', N'Viên nén', N'Uống', 14, N'Hộp 30 viên', 'Merck', N'Mỹ', N'15-25°C', 36, N'Uống theo phác đồ, giảm liều từ từ', 2);

-- 3.5. Quy đổi đơn vị
INSERT INTO UnitConversion (drugID, largeUnitID, smallUnitID, conversionQuantity) VALUES
-- Thuốc viên (HỘP -> VIÊN)
(1, 5, 1, 30),  -- Tamoxifen: 1 HỘP = 30 VIÊN
(2, 5, 1, 30),  -- Letrozole: 1 HỘP = 30 VIÊN
(7, 5, 1, 20),  -- Morphine: 1 HỘP = 20 VIÊN
(8, 5, 1, 10),  -- Ondansetron: 1 HỘP = 10 VIÊN
(9, 5, 1, 14),  -- Pantoprazole: 1 HỘP = 14 VIÊN
(10, 5, 1, 30), -- Dexamethasone: 1 HỘP = 30 VIÊN
-- Thuốc tiêm (LỌ -> ml)
(3, 3, 6, 45),  -- Carboplatin: 1 LỌ = 45 ml
(4, 3, 6, 50),  -- Paclitaxel: 1 LỌ = 50 ml
(5, 3, 6, 4),   -- Bevacizumab: 1 LỌ = 4 ml
(6, 3, 6, 4);   -- Pembrolizumab: 1 LỌ = 4 ml

-- 3.6. Thêm tài khoản Dược sĩ
INSERT INTO [Users] (roleID, userName, fullName, email, passwordHash, phoneNumber, status, lastChangePassTime, nationalID)
VALUES 
(5, 'pharmacist_anh', N'Dược sĩ Nguyễn Hoàng Anh', 'anh.nguyen@pharmacy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0908889999', 'ACTIVE', GETDATE(), '089012345678'),
(5, 'pharmacist_linh', N'Dược sĩ Trần Thị Linh', 'linh.tran@pharmacy.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0907776666', 'ACTIVE', GETDATE(), '090123456789');

-- 3.7. Nhập kho mẫu (Dược sĩ nhập)
INSERT INTO DrugBatch (drugID, batchNumber, manufactureDate, expiryDate, unitID, quantity, importPrice, supplier, importedBy, notes) VALUES
(1, 'LOT-TAM-0125', '2025-01-01', '2027-12-31', 5, 50, 850000, N'Công ty Dược XYZ', 10, N'Nhập lô Tamoxifen tháng 1/2025'),
(2, 'LOT-LET-0325', '2025-03-15', '2028-02-28', 5, 30, 1200000, N'Công ty Dược ABC', 10, N'Nhập lô Letrozole tháng 3/2025'),
(3, 'LOT-CAR-0725', '2025-07-10', '2027-06-30', 3, 20, 2200000, N'Công ty Dược Teva', 10, N'Nhập lô Carboplatin tháng 7/2025'),
(6, 'LOT-PEM-0825', '2025-08-01', '2028-07-31', 3, 10, 45000000, N'Công ty Dược MSD', 10, N'Nhập lô Pembrolizumab tháng 8/2025'),
(8, 'LOT-OND-0525', '2025-05-20', '2027-04-30', 5, 100, 180000, N'Công ty Dược GSK', 10, N'Nhập lô Ondansetron tháng 5/2025'),
(9, 'LOT-PAN-0925', '2025-09-01', '2028-08-31', 5, 50, 250000, N'Công ty Dược Takeda', 10, N'Nhập lô Pantoprazole tháng 9/2025'),
(10, 'LOT-DEX-1025', '2025-10-15', '2028-09-30', 5, 60, 150000, N'Công ty Dược Merck', 10, N'Nhập lô Dexamethasone tháng 10/2025'),
(4, 'LOT-PAC-0925', '2025-09-01', '2027-08-31', 3, 15, 3200000, N'Công ty Dược Bristol', 11, N'Nhập lô Paclitaxel tháng 9/2025'),
(5, 'LOT-BEV-1125', '2025-11-01', '2028-10-31', 3, 8, 18000000, N'Công ty Dược Roche', 11, N'Nhập lô Bevacizumab tháng 11/2025');


-- 3.8. Khởi tạo tồn kho mẫu (Khớp với các lô hàng nhập kho)
INSERT INTO Inventory (batchID, quantityInStock, lastUpdated, status) VALUES
(1, 1500, GETDATE(), 1), -- LOT-TAM-0125: 50 hộp * 30 viên = 1500 viên
(2, 900, GETDATE(), 1),  -- LOT-LET-0325: 30 hộp * 30 viên = 900 viên
(3, 900, GETDATE(), 1),  -- LOT-CAR-0725: 20 lọ * 45 ml = 900 ml
(4, 40, GETDATE(), 1),   -- LOT-PEM-0825: 10 lọ * 4 ml = 40 ml
(5, 1000, GETDATE(), 1), -- LOT-OND-0525: 100 hộp * 10 viên = 1000 viên
(6, 700, GETDATE(), 1),  -- LOT-PAN-0925: 50 hộp * 14 viên = 700 viên
(7, 1800, GETDATE(), 1), -- LOT-DEX-1025: 60 hộp * 30 viên = 1800 viên
(8, 750, GETDATE(), 1),  -- LOT-PAC-0925: 15 lọ * 50 ml = 750 ml
(9, 32, GETDATE(), 1);   -- LOT-BEV-1125: 8 lọ * 4 ml = 32 ml

-- 3.9. Khởi tạo Log xuất nhập kho mẫu cho việc nhập kho đầu kỳ
INSERT INTO InventoryLog (batchID, userID, actionType, quantityChange, quantityBefore, quantityAfter, referenceID, referenceType, performedAt, notes) VALUES
(1, 10, 'IMPORT', 1500, 0, 1500, 1, 'BATCH', GETDATE(), N'Nhập lô Tamoxifen tháng 1/2025'),
(2, 10, 'IMPORT', 900, 0, 900, 2, 'BATCH', GETDATE(), N'Nhập lô Letrozole tháng 3/2025'),
(3, 10, 'IMPORT', 900, 0, 900, 3, 'BATCH', GETDATE(), N'Nhập lô Carboplatin tháng 7/2025'),
(4, 10, 'IMPORT', 40, 0, 40, 4, 'BATCH', GETDATE(), N'Nhập lô Pembrolizumab tháng 8/2025'),
(5, 10, 'IMPORT', 1000, 0, 1000, 5, 'BATCH', GETDATE(), N'Nhập lô Ondansetron tháng 5/2025'),
(6, 10, 'IMPORT', 700, 0, 700, 6, 'BATCH', GETDATE(), N'Nhập lô Pantoprazole tháng 9/2025'),
(7, 10, 'IMPORT', 1800, 0, 1800, 7, 'BATCH', GETDATE(), N'Nhập lô Dexamethasone tháng 10/2025'),
(8, 11, 'IMPORT', 750, 0, 750, 8, 'BATCH', GETDATE(), N'Nhập lô Paclitaxel tháng 9/2025'),
(9, 11, 'IMPORT', 32, 0, 32, 9, 'BATCH', GETDATE(), N'Nhập lô Bevacizumab tháng 11/2025');


-- 3.10. Tạo đơn thuốc mẫu (Bác sĩ kê)
INSERT INTO Prescription (prescriptionCode, sessionID, patientID, doctorID, diagnosis, treatmentCycle, notes)
VALUES 
('RX-202607-001', 1, 1, 3, N'Ung thư cổ tử cung giai đoạn IVB, PD-L1 dương tính', N'Chu kỳ 1/6', N'Bệnh nhân cần theo dõi tác dụng phụ'),
('RX-202607-002', 4, 4, 4, N'Ung thư cổ tử cung giai đoạn IIIB, đang hóa xạ trị', N'Chu kỳ 2/6', N'Kết hợp với xạ trị vùng chậu');

-- 3.11. Chi tiết đơn thuốc (Bác sĩ kê - Chưa xuất kho)
INSERT INTO PrescriptionDetail (prescriptionID, drugID, dosePerTime, timesPerDay, daysOfTreatment, quantityPrescribed, dispenseUnit, instruction, notes)
VALUES
-- Đơn 1: Phác đồ Pembrolizumab + Carboplatin + Paclitaxel
(1, 6, 200, 1, 1, 2, N'LỌ', N'Truyền tĩnh mạch 30 phút, ngày 1 của chu kỳ', N'Pembrolizumab 200mg'),
(1, 3, 525, 1, 1, 2, N'LỌ', N'Truyền tĩnh mạch 1 giờ, ngày 1', N'Carboplatin AUC5'),
(1, 4, 280, 1, 1, 1, N'LỌ', N'Truyền tĩnh mạch 3 giờ, ngày 1, cần tiền thuốc chống dị ứng', N'Paclitaxel 175mg/m²'),
-- Thuốc hỗ trợ
(1, 8, 8, 2, 3, 6, N'VIÊN', N'Uống 1 giờ trước hóa trị và 8 giờ sau', N'Ondansetron chống nôn'),
(1, 10, 8, 1, 3, 3, N'VIÊN', N'Uống trước hóa trị 30 phút', N'Dexamethasone chống dị ứng'),
-- Đơn 2: Phác đồ Paclitaxel + Carboplatin (không có Pembrolizumab)
(2, 4, 280, 1, 1, 1, N'LỌ', N'Truyền tĩnh mạch 3 giờ, ngày 1', N'Paclitaxel 175mg/m²'),
(2, 3, 525, 1, 1, 2, N'LỌ', N'Truyền tĩnh mạch 1 giờ, ngày 1', N'Carboplatin AUC5'),
(2, 8, 8, 2, 3, 6, N'VIÊN', N'Uống 1 giờ trước hóa trị', N'Ondansetron chống nôn');
GO






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