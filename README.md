
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

```
Banking_PROJECT/
 ├── controller/         # REST Endpoints (APIs)
 │    └── BankController.java
 ├── model/              # Entities (Tables)
 │    ├── BankAccount.java
 │    └── Transaction.java
 ├── repository/         # DAO Layer
 │    ├── BankAccountRepository.java
 │    └── TransactionRepository.java
 ├── service/            # Business Logic
 │    └── BankService.java
 ├── resources/
 │    ├── application.properties   # DB Config
 │    └── data.sql                 # Initial Data
 └── BankingProjectApplication.java # Main App
```

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

All write operations take a **JSON request body** (not query/path params), use **noun-based, versioned** resource paths, and return JSON. Monetary amounts carry an ISO 4217 `currency`.

| Method | Endpoint | Description | Request body |
|--------|----------|-------------|--------------|
| POST   | `/accounts`                                | Create account            | `{ "accountHolderName", "accountType" (SAVINGS\|CURRENT), "initialDeposit", "currency"? }` |
| GET    | `/accounts`                                | List accounts             | — |
| GET    | `/accounts/{accountNumber}`                | Get account               | — |
| POST   | `/accounts/{accountNumber}/deposits`       | Deposit 💰                | `{ "amount", "description"? }` |
| POST   | `/accounts/{accountNumber}/withdrawals`    | Withdraw 🏧               | `{ "amount", "description"? }` |
| POST   | `/transfers`                               | Transfer funds 🔄         | `{ "fromAccount", "toAccount", "amount", "description"? }` |
| GET    | `/accounts/{accountNumber}/transactions`   | Statement 📜              | — |

👉 **Base URL:** `http://localhost:8080/api/v1`

Errors return a consistent JSON contract: `{ timestamp, status, error, message, path, fieldErrors? }` with correct HTTP status codes (`400` validation, `404` not found, `422` insufficient balance).

Example:
```bash
curl -X POST http://localhost:8080/api/v1/accounts/ACC1001/deposits \
  -H 'Content-Type: application/json' \
  -d '{"amount": 250.00, "description": "Cash deposit"}'
```

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

- 🔐 JWT Authentication  
- 🏦 Multiple Account Types (Savings / Current)  
- 📑 Pagination for Bank Statements  
- 📤 Export Transactions → PDF/Excel  
- 🐳 Docker Deployment  

---

## 👨‍💻 Author  

**Somnath Rana**  
💼 Java Backend Developer | Spring Boot | APIs  
🔗 [GitHub Profile](https://github.com/SOMNATH43910)
