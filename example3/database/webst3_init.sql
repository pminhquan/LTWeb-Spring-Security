-- ============================================================================
-- Script: webst3_init.sql
-- Target Database: Microsoft SQL Server (webst3)
-- Purpose: Self-contained DDL schema and idempotent demo data seeding
--          for Example 3 (Spring Boot 4.1.1, Spring Security 7, JPA, Thymeleaf)
--
-- Execution Order:
--   1. Create database 'webst3' if absent and switch context to 'webst3'.
--   2. Create mapped tables ('roles', 'users', 'products', 'otp_tokens') if absent.
--   3. Create indexes and foreign key constraints matching JPA annotations.
--   4. Idempotently seed roles: ROLE_USER, ROLE_ADMIN.
--   5. Idempotently seed demo users:
--        - admin  (ROLE_ADMIN, password: 123456 via BCrypt hash)
--        - user01 (ROLE_USER,  password: 123456 via BCrypt hash)
--   6. Idempotently seed sample products linked to admin and user01.
--   7. otp_tokens table is left empty (tokens are ephemeral runtime data).
-- ============================================================================

-- Step 1: Create Database if not exists and switch context
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'webst3')
BEGIN
    CREATE DATABASE [webst3];
END
GO

USE [webst3];
GO

-- ============================================================================
-- Step 2: Create Tables, Constraints, and Indexes if absent
-- ============================================================================

-- 2.1 Table: roles (vn.iotstar.entity.Role)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'roles' AND type = 'U')
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) NOT NULL,
        name VARCHAR(30) NOT NULL,
        CONSTRAINT PK_roles PRIMARY KEY (id)
    );
END
GO

-- 2.2 Table: users (vn.iotstar.entity.User)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users' AND type = 'U')
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) NOT NULL,
        username VARCHAR(50) NOT NULL,
        email VARCHAR(150) NOT NULL,
        password VARCHAR(255) NOT NULL,
        full_name NVARCHAR(500) NULL,
        enabled BIT NOT NULL DEFAULT 0,
        role_id BIGINT NOT NULL,
        CONSTRAINT PK_users PRIMARY KEY (id),
        CONSTRAINT UQ_users_username UNIQUE (username),
        CONSTRAINT UQ_users_email UNIQUE (email),
        CONSTRAINT FK_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_username' AND object_id = OBJECT_ID('users'))
BEGIN
    CREATE INDEX idx_users_username ON users(username);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_email' AND object_id = OBJECT_ID('users'))
BEGIN
    CREATE INDEX idx_users_email ON users(email);
END
GO

-- 2.3 Table: products (vn.iotstar.entity.Product)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products' AND type = 'U')
BEGIN
    CREATE TABLE products (
        id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(500) NOT NULL,
        description NVARCHAR(500) NULL,
        price NUMERIC(18,2) NOT NULL,
        image_url VARCHAR(1000) NULL,
        user_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_products PRIMARY KEY (id),
        CONSTRAINT FK_products_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_products_name' AND object_id = OBJECT_ID('products'))
BEGIN
    CREATE INDEX idx_products_name ON products(name);
END
GO

-- 2.4 Table: otp_tokens (vn.iotstar.entity.OtpToken)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'otp_tokens' AND type = 'U')
BEGIN
    CREATE TABLE otp_tokens (
        id BIGINT IDENTITY(1,1) NOT NULL,
        email VARCHAR(150) NOT NULL,
        otp_hash VARCHAR(100) NOT NULL,
        type VARCHAR(30) NOT NULL,
        expires_at DATETIME2 NOT NULL,
        attempts INT NOT NULL DEFAULT 0,
        used BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_otp_tokens PRIMARY KEY (id)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_otp_email_type' AND object_id = OBJECT_ID('otp_tokens'))
BEGIN
    CREATE INDEX idx_otp_email_type ON otp_tokens(email, type);
END
GO

-- ============================================================================
-- Step 3: Idempotent Seed Data - Roles
-- ============================================================================

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_USER');
END
GO

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
END
GO

-- ============================================================================
-- Step 4: Idempotent Seed Data - Demo Users
-- Password for both accounts is '123456', hashed using Spring Security BCryptPasswordEncoder (strength 10)
-- Hash: $2a$10$YmWXZosFB23BikvoBPOFxOAGDElU9NmaTpEzBtU8IIoCHMsLjZDJq
-- ============================================================================

DECLARE @adminRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
DECLARE @userRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_USER');

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, email, password, full_name, enabled, role_id)
    VALUES (
        'admin',
        'admin@iotstar.vn',
        '$2a$10$YmWXZosFB23BikvoBPOFxOAGDElU9NmaTpEzBtU8IIoCHMsLjZDJq',
        N'Administrator',
        1,
        @adminRoleId
    );
END

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'user01')
BEGIN
    INSERT INTO users (username, email, password, full_name, enabled, role_id)
    VALUES (
        'user01',
        'user01@gmail.com',
        '$2a$10$YmWXZosFB23BikvoBPOFxOAGDElU9NmaTpEzBtU8IIoCHMsLjZDJq',
        N'Nguyễn Văn A',
        1,
        @userRoleId
    );
END
GO

-- ============================================================================
-- Step 5: Idempotent Seed Data - Sample Products
-- Products are linked to either 'admin' or 'user01' with valid Cloudinary image URLs
-- ============================================================================

DECLARE @adminUserId BIGINT = (SELECT id FROM users WHERE username = 'admin');
DECLARE @user01UserId BIGINT = (SELECT id FROM users WHERE username = 'user01');

-- Sample products owned by admin
IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Bàn phím cơ không dây Keychron K2' AND user_id = @adminUserId)
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Bàn phím cơ không dây Keychron K2',
        N'Bàn phím cơ Bluetooth và Type-C layout 75%, đèn RGB, Gateron switch.',
        1890000.00,
        'https://res.cloudinary.com/demo/image/upload/sample.jpg|sample',
        @adminUserId,
        SYSDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Chuột công thái học Logitech MX Master 3S' AND user_id = @adminUserId)
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Chuột công thái học Logitech MX Master 3S',
        N'Chuột không dây yên tĩnh Quiet Clicks, cảm biến 8000 DPI Darkfield.',
        2450000.00,
        'https://res.cloudinary.com/demo/image/upload/sample.jpg|sample',
        @adminUserId,
        SYSDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Màn hình Dell UltraSharp U2724D 27 inch 2K' AND user_id = @adminUserId)
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Màn hình Dell UltraSharp U2724D 27 inch 2K',
        N'Màn hình đồ họa IPS Black, tần số quét 120Hz, 98% DCI-P3.',
        8990000.00,
        'https://res.cloudinary.com/demo/image/upload/sample.jpg|sample',
        @adminUserId,
        SYSDATETIME()
    );
END

-- Sample products owned by user01
IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Tai nghe chụp tai Sony WH-1000XM5' AND user_id = @user01UserId)
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Tai nghe chụp tai Sony WH-1000XM5',
        N'Tai nghe chống ồn chủ động không dây cao cấp, âm thanh Hi-Res, pin 30 giờ.',
        6990000.00,
        'https://res.cloudinary.com/demo/image/upload/sample.jpg|sample',
        @user01UserId,
        SYSDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Đế tản nhiệt laptop nhôm gập gọn' AND user_id = @user01UserId)
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Đế tản nhiệt laptop nhôm gập gọn',
        N'Hợp kim nhôm nguyên khối, 6 nấc chỉnh độ cao tản nhiệt tối ưu.',
        320000.00,
        'https://res.cloudinary.com/demo/image/upload/sample.jpg|sample',
        @user01UserId,
        SYSDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Cáp sạc nhanh Type-C to Type-C 100W Baseus' AND user_id = @user01UserId)
BEGIN
    INSERT INTO products (name, description, price, image_url, user_id, created_at)
    VALUES (
        N'Cáp sạc nhanh Type-C to Type-C 100W Baseus',
        N'Dây bọc dù siêu bền, hỗ trợ sạc nhanh PD 100W/5A, chip E-marker.',
        150000.00,
        'https://res.cloudinary.com/demo/image/upload/sample.jpg|sample',
        @user01UserId,
        SYSDATETIME()
    );
END
GO

-- ============================================================================
-- Step 6: OTP Tokens Note
-- ============================================================================
-- No rows are seeded into 'otp_tokens'. OTP tokens are short-lived verification codes
-- generated dynamically by OtpService during user registration and password reset workflows.
-- ============================================================================
