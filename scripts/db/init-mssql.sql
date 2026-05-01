IF DB_ID(N'SosyalMedyaAnaliz') IS NULL
BEGIN
    CREATE DATABASE SosyalMedyaAnaliz;
END
GO

USE SosyalMedyaAnaliz;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) UNIQUE NOT NULL,
        password_hash NVARCHAR(255) NOT NULL,
        email NVARCHAR(100) UNIQUE NOT NULL,
        created_at DATETIME DEFAULT GETDATE()
    );
END
GO

IF OBJECT_ID(N'dbo.api_credentials', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.api_credentials (
        id INT IDENTITY(1,1) PRIMARY KEY,
        platform NVARCHAR(50) NOT NULL,
        api_key NVARCHAR(255) NOT NULL,
        api_secret NVARCHAR(255) NOT NULL,
        status BIT DEFAULT 1
    );
END
GO
