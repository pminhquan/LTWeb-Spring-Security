# LTWeb - Spring Security

Spring Security exercises for Web Programming.

## Requirements

- Java 21
- Maven
- SQL Server

---

## Example 1

Spring Boot 4 + Spring Security + Thymeleaf + MapStruct + SQL Server.

### Features

- Login with email and password
- BCrypt password encryption
- User and Role stored in SQL Server
- Authenticated user information displayed in the header
- Protected pages with Spring Security
- Logout with CSRF and session invalidation
- Thymeleaf fragments without Layout Dialect

### Demo Account

- Email: `user01@gmail.com`
- Password: `123456`
- Port: `8080`

### Database

Create a SQL Server database, for example:

```sql
CREATE DATABASE LTWebSecurityExample1;
```

Set environment variables in PowerShell:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=LTWebSecurityExample1;encrypt=false;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YOUR_SQL_SERVER_PASSWORD"
```

### Run

```powershell
cd example1
mvn spring-boot:run
```

Open:

`http://localhost:8080/login`

---

## Example 2

Spring Boot 4 + Spring Security + Thymeleaf Layout Dialect + MapStruct + SQL Server.

### Features

- Custom login using username or email
- BCrypt password encryption
- User and Role stored in SQL Server
- Full name, username, email, role and avatar displayed in the header
- Thymeleaf Layout Dialect
- Protected pages with Spring Security
- Logout with CSRF and session invalidation

### Demo Account

- Username: `user01`
- Email: `user01@gmail.com`
- Password: `123456`
- Port: `8081`

Login supports either the username or email with the same password.

### Database

Create a SQL Server database, for example:

```sql
CREATE DATABASE LTWebSecurityExample2;
```

Set environment variables in PowerShell:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=LTWebSecurityExample2;encrypt=false;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YOUR_SQL_SERVER_PASSWORD"
```

### Run

```powershell
cd example2
mvn spring-boot:run
```

Open:

`http://localhost:8081/login`

---

## Notes

- Database tables are created automatically by JPA/Hibernate.
- Demo users are seeded automatically when the application starts.
- SQL Server credentials are not stored in the repository.