-- Flyway Migration: Add SymptomResult table and update SymptomDetails FK
-- This migration ensures the schema matches the JPA entity definitions

USE MedicalDiagnosisDB;
GO

-- Check if SymptomResult table exists, if not create it
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SymptomResult')
BEGIN
    CREATE TABLE SymptomResult
    (
        symptomResultID INT IDENTITY (1,1) PRIMARY KEY,
        sessionID       INT UNIQUE NOT NULL,
        status          NVARCHAR(50) DEFAULT 'PENDING',
        createdAt       DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (sessionID) REFERENCES DiagnosisSession (sessionID)
    );
    
    CREATE NONCLUSTERED INDEX IX_SymptomResult_SessionID ON SymptomResult(sessionID);
END
GO

-- Check if SymptomDetails already has FK to SymptomResult
-- If it still references sessionID directly, drop old constraint and add new one
IF EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE name = 'FK_SymptomDetails_SessionID'
)
BEGIN
    ALTER TABLE SymptomDetails DROP CONSTRAINT FK_SymptomDetails_SessionID;
END
GO

-- Add symptomResultID column to SymptomDetails if it doesn't exist
IF NOT EXISTS (
    SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'SymptomDetails' AND COLUMN_NAME = 'symptomResultID'
)
BEGIN
    ALTER TABLE SymptomDetails ADD symptomResultID INT;
    
    -- Migrate data: Map SymptomDetails to SymptomResult via session
    UPDATE SymptomDetails
    SET symptomResultID = (
        SELECT sr.symptomResultID 
        FROM SymptomResult sr
        INNER JOIN DiagnosisSession ds ON sr.sessionID = ds.sessionID
        WHERE ds.sessionID IN (
            SELECT sessionID FROM DiagnosisSession WHERE sessionID IN (
                SELECT sessionID FROM SymptomDetails 
            )
        )
        LIMIT 1
    )
    WHERE symptomResultID IS NULL;
    
    -- Add FK constraint
    ALTER TABLE SymptomDetails 
    ADD CONSTRAINT FK_SymptomDetails_SymptomResultID 
    FOREIGN KEY (symptomResultID) REFERENCES SymptomResult(symptomResultID);
END
GO

-- Drop old sessionID FK if still exists on SymptomDetails
IF COL_LENGTH('SymptomDetails', 'sessionID') IS NOT NULL
BEGIN
    -- Only drop if it's a direct session reference
    IF EXISTS (
        SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'SymptomDetails' AND COLUMN_NAME = 'sessionID'
    )
    BEGIN
        -- For migration purposes, keep sessionID but make it nullable for now
        -- It will be removed once data migration is verified
        ALTER TABLE SymptomDetails ALTER COLUMN sessionID INT NULL;
    END
END
GO

PRINT 'Migration completed: SymptomResult table and SymptomDetails FK updates';
