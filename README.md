# 🔐 VaultEdge — Secure Banking Backend (Spring Boot)

VaultEdge is a **production-grade banking backend** built using **Spring Boot, MySQL, JWT authentication, Flyway migrations, and Docker**.

It supports secure customer onboarding, account management, transactions (deposit / withdraw / transfer), auditing, and role-based access control — designed to be **frontend-ready** for hackathons and real-world applications.

---

## 🚀 Tech Stack

- **Backend:** Spring Boot 3 (Java 17)
- **Security:** Spring Security + JWT
- **Database:** MySQL 8 + Flyway migrations
- **ORM:** Spring Data JPA (Hibernate)
- **Auth:** JWT (Stateless)
- **API Docs:** Swagger / OpenAPI
- **DevOps:** Docker, Docker Compose
- **CI/CD:** GitHub Actions
- **Audit & Logging:** Custom Audit Service

---

## ✨ Key Features

### 🔑 Authentication & Authorization
- JWT-based stateless authentication
- Role-based access (`USER`, `ADMIN`)
- Secure password hashing (BCrypt)

### 👤 Customer Management
- Customer registration
- Secure login
- Profile ownership enforcement

### 🏦 Account Management
- Create bank accounts
- View account balance
- Ownership checks (only owner can access)

### 💸 Transactions
- Deposit
- Withdraw
- Transfer (atomic, transactional)
- Idempotency support (safe retries)
- Pagination, sorting, filtering
- Transaction summary (inflow / outflow)

### 📜 Auditing
- Login success / failure
- Deposits, withdrawals, transfers
- Timestamped, structured metadata
- Future-ready for compliance & analytics

### 🐳 Dockerized
- Spring Boot app container
- MySQL container
- Adminer (DB UI)
- One-command startup

---

## 🧱 System Architecture

### High-Level Architecture

[ Frontend (React / Next / Flutter) ]
|
| REST API (JSON)
v
[ Spring Boot API — VaultEdge ]
|

| | |
[ Auth ] [ Business Logic ] [ Audit ]
| |
[ JWT ] [ Accounts / Txns ]
|
[ MySQL ]


---

## 🔄 Transfer Flow (Sequence)

User
|
| POST /transfer
v
AuthController
|
| validate JWT
v
TransactionService
|
| debit source account
| credit destination account
| save transactions
| audit log
v
Database (MySQL)


---

## 📦 Database Design (ER Overview)

**Customer**
- id (UUID)
- name, email, password

**AuthUser**
- id (UUID)
- username
- roles

**Account**
- id (UUID)
- customer_id
- balance

**Transaction**
- id (UUID)
- account_id
- type (DEPOSIT / WITHDRAW / TRANSFER)
- amount
- reference_id
- timestamp

**AuditLog**
- action
- user_id
- metadata
- ip
- timestamp

--

## 🧪 API Examples (cURL)

### 🔐 Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{
  "username": "user@example.com",
  "password": "password"
}'


### 💰 Deposit
curl -X POST http://localhost:8080/api/v1/transactions/accounts/{id}/deposit \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d '{ "amount": 1000 }'

### 📖 Swagger UI

Once running:
http://localhost:8080/swagger-ui.html

🐳 Run with Docker:
docker compose up --build

### Services:

App → http://localhost:8080
Adminer → http://localhost:8081
MySQL → localhost:3307

### ⚙️ CI/CD

VaultEdge uses GitHub Actions for CI:
Java 17 build
Maven verification
Automatic checks on push & PR

### 📌 Future Enhancements

Rate limiting

Notifications

KYC workflows

Admin dashboards

Event streaming (Kafka)

### 👨‍💻 Author

Rohit Surya
B.Tech — AI & Data Science

Backend | Java | Spring Boot | Systems Design
