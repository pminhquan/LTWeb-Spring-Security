# Database Initialization & Configuration - Example 3

This directory contains the database setup script for **Example 3** (`shop-springboot-4-1-1`), featuring Spring Boot 4.1.1, Spring Security 7.1.x, Spring Data JPA, Thymeleaf, and Microsoft SQL Server.

---

## 1. Overview of `webst3_init.sql`

[`webst3_init.sql`](./webst3_init.sql) is a self-contained, idempotent SQL Server initialization script that:

1. **Creates & Selects Database**: Conditionally creates database `webst3` if absent and switches context (`USE [webst3];`).
2. **Generates Schema**: Creates the four mapped tables (`roles`, `users`, `products`, `otp_tokens`), primary identity keys, unique constraints, foreign keys, and indexes matching the JPA entities exactly.
3. **Idempotently Seeds Data**:
   - Roles: `ROLE_USER`, `ROLE_ADMIN`.
   - Demo Users: `admin` (`ROLE_ADMIN`) and `user01` (`ROLE_USER`), both with password `123456` encoded with Spring Security-compatible BCrypt.
   - Sample Products: 6 realistic products (3 assigned to `admin`, 3 assigned to `user01`) with Cloudinary image identifiers.
   - `otp_tokens`: Intentionally left empty as OTP tokens are short-lived verification codes generated dynamically at runtime during registration and password reset.

Rerunning the script is safe; stable natural-key checks prevent duplicate records.

---

## 2. Prerequisites & Execution Order

### Prerequisites
- Microsoft SQL Server 2019+ (or Azure SQL / SQL Server 2025) running and accessible (default port: `1433`).
- SQL Server client tool: **SQL Server Management Studio (SSMS)**, **Azure Data Studio**, or **sqlcmd CLI**.

### Execution Timing
- **Preferred Path**: Run `webst3_init.sql` **before** launching the application so demo users and products are immediately available upon startup.
- **Alternative Path**: Since `spring.jpa.hibernate.ddl-auto=update` is configured in `application.properties`, you can also start the Spring Boot application once to let Hibernate generate the schema, then execute `webst3_init.sql` to populate the seed data. Because table creation and data seeding in `webst3_init.sql` use conditional guards (`IF NOT EXISTS`), both execution orders produce the identical, consistent schema.

---

## 3. How to Execute `webst3_init.sql`

### Option A: Using `sqlcmd` (Command Line)

With Windows Authentication:
```powershell
sqlcmd -S localhost -E -C -i "example3\database\webst3_init.sql"
```

With SQL Server Authentication:
```powershell
sqlcmd -S localhost -U sa -P "YOUR_PASSWORD" -C -i "example3\database\webst3_init.sql"
```
*(Note: `-C` trusts the server certificate to avoid SSL validation warnings on local development instances).*

### Option B: Using SSMS (SQL Server Management Studio)
1. Open SSMS and connect to your SQL Server instance.
2. Go to **File -> Open -> File...** and select `example3/database/webst3_init.sql`.
3. Click **Execute** (or press `F5`).
4. Ensure the query completes successfully.

### Option C: Using Azure Data Studio
1. Connect to your SQL Server connection.
2. Open `webst3_init.sql` and click **Run**.

---

## 4. Demo Accounts

| Username | Password | Role | Status | Description |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `123456` | `ROLE_ADMIN` | Active (`enabled=1`) | Can manage users and create/update/delete products |
| `user01` | `123456` | `ROLE_USER` | Active (`enabled=1`) | Can log in and view products, but cannot create/update/delete products or manage users |

> [!NOTE]
> Passwords in the database are stored as BCrypt hashes (`$2a$10$YmWXZosFB23BikvoBPOFxOAGDElU9NmaTpEzBtU8IIoCHMsLjZDJq`) verified against Spring Security 7's `BCryptPasswordEncoder`. Plaintext passwords are never stored.

---

## 5. Required Environment Variables

Before running `example3`, configure the following environment variables in PowerShell or provide an uncommitted `.env` file in `example3/`:

```powershell
# ===============================
# DATABASE (SQL Server)
# ===============================
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=webst3;encrypt=false;trustServerCertificate=true;sslProtocol=TLSv1.2;characterEncoding=UTF-8"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YOUR_SQL_SERVER_PASSWORD"

# ===============================
# SPRING MAIL (Gmail / SMTP for OTP)
# ===============================
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password"

# ===============================
# CLOUDINARY (Product Image Storage)
# ===============================
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
$env:CLOUDINARY_API_KEY="your_api_key"
$env:CLOUDINARY_API_SECRET="your_api_secret"

# ===============================
# SERVER (Optional, default 8080)
# ===============================
$env:SERVER_PORT="8080"
```

---

## 6. Verification & Running the Application

After running `webst3_init.sql` and configuring the environment variables:

```powershell
cd example3
mvn spring-boot:run
```

Access the application in your browser:
- Login page: `http://localhost:8080/login`
- Dashboard: `http://localhost:8080/`
