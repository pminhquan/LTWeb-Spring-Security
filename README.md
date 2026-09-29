# LTWeb - Spring Security

Spring Security exercises for Web Programming.

This repository contains three Spring Security examples implemented with Spring Boot, demonstrating authentication, authorization, database integration, Thymeleaf, MapStruct, OTP verification, and CRUD management.

---

# Requirements

- Java 21
- Maven
- SQL Server

---

# Project Structure

```
LTWeb-Spring-Security
│
├── example1
│   └── Basic Spring Security authentication
│
├── example2
│   └── Custom authentication with username/email and roles
│
└── example3
    └── Complete Spring Security application with OTP, CRUD, Cloudinary
```

---

# Example 1

## Spring Boot + Spring Security + Thymeleaf + MapStruct + SQL Server

## Features

- Login with email and password
- BCrypt password encryption
- User and Role stored in SQL Server
- Spring Security authentication
- Protected pages
- Header displays authenticated user
- Logout with CSRF protection
- Thymeleaf fragments

## Demo Account

```
Email:
user01@gmail.com

Password:
123456
```

## Run

```powershell
cd example1
mvn spring-boot:run
```

Access:

```
http://localhost:8080/login
```

---

# Example 2

## Spring Boot + Spring Security + Thymeleaf Layout Dialect + MapStruct + SQL Server

## Features

- Login using username or email
- BCrypt password encryption
- Custom UserDetailsService
- Role-based authorization
- User profile information in header
- Avatar display
- Thymeleaf Layout Dialect
- Logout with CSRF protection

## Demo Account

```
Username:
user01

Password:
123456
```

## Run

```powershell
cd example2
mvn spring-boot:run
```

Access:

```
http://localhost:8081/login
```

---

# Example 3

## Complete Spring Security Application

Technology stack:

- Spring Boot 4.1.1
- Spring Security
- Spring Data JPA
- Thymeleaf
- MapStruct
- SQL Server
- Cloudinary
- Gmail SMTP

---

# Example 3 Features

## Authentication

Implemented:

- Register account
- Email OTP verification
- BCrypt password hashing
- Login/logout
- Forgot password
- Reset password
- Session management
- CSRF protection


## OTP Verification

Flow:

```
Register
   |
   ↓
Send OTP Email
   |
   ↓
Verify OTP
   |
   ↓
Activate Account
   |
   ↓
Login
```

OTP security:

- OTP stored as hash
- Expiration time
- Limited verification attempts
- One-time usage


---

# Authorization

Role-based access:

| Feature | ADMIN | USER |
|---|---|---|
| Login | ✓ | ✓ |
| View products | ✓ | ✓ |
| Create product | ✓ | ✗ |
| Edit product | ✓ | ✗ |
| Delete product | ✓ | ✗ |
| Manage users | ✓ | ✗ |


---

# Product Management

Features:

- Product CRUD
- Search product
- Pagination
- Upload image
- Cloudinary integration
- Image validation
- Product ownership management


---

# User Management

Features:

- User CRUD
- Search user
- Pagination
- Role assignment
- Enable/disable account
- Safe delete handling


---

# Database

Database initialization file:

```
example3/database/webst3_init.sql
```

Database includes:

```
roles
users
products
otp_tokens
```

Seed data:

```
ROLE_USER
ROLE_ADMIN

admin
user01

6 demo products
```

Default accounts:

| Username | Password | Role |
|---|---|---|
| admin | 123456 | ROLE_ADMIN |
| user01 | 123456 | ROLE_USER |

---

# Configure Example 3

Before running, configure environment variables.

## SQL Server

```powershell
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=webst3;encrypt=false;trustServerCertificate=true

DB_USERNAME=sa

DB_PASSWORD=your_password
```

## Gmail SMTP

Required for OTP:

```powershell
MAIL_HOST=smtp.gmail.com

MAIL_PORT=587

MAIL_USERNAME=your_email@gmail.com

MAIL_PASSWORD=your_app_password
```

## Cloudinary

Required for image upload:

```powershell
CLOUDINARY_CLOUD_NAME=your_cloud_name

CLOUDINARY_API_KEY=your_api_key

CLOUDINARY_API_SECRET=your_api_secret
```

---

# Create Database

Run:

```
example3/database/webst3_init.sql
```

Example:

```sql
sqlcmd -S localhost -U sa -P your_password -i example3/database/webst3_init.sql
```

---

# Run Example 3

```powershell
cd example3

mvn spring-boot:run
```

Access:

```
http://localhost:8080/login
```

---

# Testing Checklist

After running Example 3:

## Authentication

- [ ] Register new account
- [ ] Receive OTP email
- [ ] Verify OTP
- [ ] Login successfully
- [ ] Logout successfully
- [ ] Forgot password works


## ADMIN

Login:

```
admin
123456
```

Test:

- [ ] Dashboard
- [ ] User management
- [ ] Add product
- [ ] Edit product
- [ ] Delete product


## USER

Login:

```
user01
123456
```

Test:

- [ ] View products
- [ ] Search products
- [ ] Pagination
- [ ] Cannot access admin features


---

# Build Test

Each example can be tested using:

```powershell
mvn clean test
```

Package:

```powershell
mvn clean package
```

---

# Notes

- Database passwords, SMTP credentials, and Cloudinary keys are not included.
- Generated files such as `target/` are ignored.
- Example 3 contains complete setup instructions in:

```
example3/database/README.md
```

- Demo passwords are only for local testing.