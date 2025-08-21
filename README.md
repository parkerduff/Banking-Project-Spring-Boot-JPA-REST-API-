
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

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/create`             | Create Account |
| POST   | `/deposit/{id}/{amt}` | Deposit Money 💰 |
| POST   | `/withdraw/{id}/{amt}`| Withdraw Money 🏧 |
| POST   | `/transfer/{from}/{to}/{amt}` | Transfer Funds 🔄 |
| GET    | `/balance/{id}`       | Check Balance 💳 |
| GET    | `/statement/{id}`     | Transaction History 📜 |

👉 **Base URL:** `http://localhost:8080/api/bank`  

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
