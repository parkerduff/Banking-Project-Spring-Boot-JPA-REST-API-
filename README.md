
# 🏦 Banking Project – Spring Boot + JPA + REST API  

![Java](https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white)  
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)  
![MySQL](https://img.shields.io/badge/Database-MySQL-orange?logo=mysql)  
![Postman](https://img.shields.io/badge/API%20Testing-Postman-red?logo=postman)  

---

## ✨ About the Project  
A real-world style **Banking Management System** built with **Spring Boot**.  
This project covers:  
✅ Account creation  
✅ Deposit & Withdraw money  
✅ Fund transfer between accounts  
✅ Transaction history (Bank Statements)  

🔗 **Tested using Postman Collection**  
⚡ **Auto DB initialization with `data.sql`**  

---

## 🛠 Tech Stack  
- ☕ Java 21 / 24  
- 🍃 Spring Boot (REST + JPA + Hibernate)  
- 🗄️ Database: MySQL / H2  
- 📦 Build Tool: Maven  
- 🌐 API Testing: Postman  
- 💻 IDE: IntelliJ / Eclipse  

---

## 📂 Project Structure  

Standard Maven layout under `src/main/java/com/sr_banking/banking_project`:

```
src/main/java/com/sr_banking/banking_project/
 ├── controller/   # Versioned REST endpoints (BankController)
 ├── service/      # Business logic (BankService)
 ├── repository/   # Spring Data JPA repositories
 ├── model/        # JPA entities (BankAccount, Transaction)
 ├── dto/          # Request/response records + ApiError
 ├── exception/    # Custom exceptions + GlobalExceptionHandler
 ├── config/       # Security, Jackson, OpenAPI configuration
 └── web/          # FieldProjection helper
src/main/resources/
 ├── application.properties
 └── data.sql
src/test/java/...  # Service + controller (MockMvc) tests
```

> 📐 See [`docs/ABS-MAS-COMPLIANCE.md`](docs/ABS-MAS-COMPLIANCE.md) for how this API maps to the
> ABS-MAS Finance-as-a-Service API Playbook guidelines.

---

## ⚙️ Setup & Run  

1️⃣ Clone the repo  
```bash
git clone https://github.com/YourUsername/Banking_PROJECT.git
cd Banking_PROJECT
```

2️⃣ Configure Database (For H2 DB → no setup required)  

3️⃣ Run the app  
```bash
mvn spring-boot:run
```

---

## 🗄 MySQL Configuration  

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bank_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

---

## 🔗 API Endpoints  

Resource-oriented (noun-based) and explicitly versioned. Request/response bodies are JSON.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/accounts`                              | Create account |
| GET    | `/accounts?page=&size=&fields=`          | List accounts (paginated, field projection) |
| GET    | `/accounts/{accountNumber}?fields=`      | Get one account |
| POST   | `/accounts/{accountNumber}/deposits`     | Deposit money 💰 |
| POST   | `/accounts/{accountNumber}/withdrawals`  | Withdraw money 🏧 |
| GET    | `/accounts/{accountNumber}/transactions` | Statement (paginated) 📜 |
| POST   | `/transfers`                             | Transfer funds 🔄 |

👉 **Base URL:** `http://localhost:8080/api/v1/bank`  

### 🔐 Authentication & Authorization

All endpoints require HTTP Basic authentication. Reads need the `USER` role; state-changing
operations (create / deposit / withdraw / transfer) need the `ADMIN` role. Demo credentials are
configurable via environment variables (`API_USER`/`API_USER_PASSWORD`, `API_ADMIN`/`API_ADMIN_PASSWORD`);
defaults are `user`/`changeit` and `admin`/`changeit`.

```bash
# Read (USER)
curl -u user:changeit http://localhost:8080/api/v1/bank/accounts

# Create (ADMIN)
curl -u admin:changeit -X POST -H 'Content-Type: application/json' \
  -d '{"accountHolderName":"Jane Doe","accountType":"SAVINGS","initialDeposit":500.00}' \
  http://localhost:8080/api/v1/bank/accounts
```

> Production deployments should terminate TLS 1.2+ and replace HTTP Basic with OAuth 2.0 / OpenID
> Connect issuing signed JWT access tokens, per the playbook's Information Security guidelines.

### 📖 Interactive Docs (OpenAPI / Swagger)

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

---

## 🧪 Postman Collection  

📥 Import & test APIs: [Postman Link](https://x-space-1228.postman.co/workspace/10cb7320-18c2-42a7-ad35-ac362fa90ab8/collection/38637976-0490cd9f-957b-4253-93b5-e9501ed127b6?action=share&source=copy-link&creator=38637976)_

---

## 📊 Sample Data  

```sql
INSERT INTO bank_account (id, account_holder, balance) VALUES (1, 'Somnath Rana', 10000);
INSERT INTO bank_account (id, account_holder, balance) VALUES (2, 'Rahul Verma', 5000);
```

---

## 🚀 Future Enhancements  

- 🔐 OAuth 2.0 / OpenID Connect with signed JWT access tokens  
- 🏦 Multiple Account Types (Savings / Current)  
- 📤 Export Transactions → PDF/Excel  
- 🐳 Docker Deployment  

---

## 👨‍💻 Author  

**Somnath Rana**  
💼 Java Backend Developer | Spring Boot | APIs  
🔗 [GitHub Profile](https://github.com/SOMNATH43910)
