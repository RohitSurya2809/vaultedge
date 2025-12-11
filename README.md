# Banking Microservice (Spring Boot + MySQL)

## Elevator pitch
A production-minded microservice exposing REST APIs for customer & account management, secure authentication (JWT), and ACID-safe money transfers with transaction ledger.

## Tech stack
- Java 17, Spring Boot (Web, Data JPA, Security)
- MySQL, Flyway
- JWT authentication, BCrypt
- Docker & Docker Compose
- OpenAPI / Swagger

## Getting started (local)
1. `docker-compose up --build`
2. App: `http://localhost:8080`
3. Swagger UI: `http://localhost:8080/swagger-ui.html` (or `/swagger-ui/index.html`)

## Key endpoints
- `POST /api/v1/auth/register` - create user
- `POST /api/v1/auth/login` - login (returns JWT)
- `GET /api/v1/customers/{id}`
- `POST /api/v1/accounts`
- `POST /api/v1/accounts/{id}/deposit`
- `POST /api/v1/transfer` - transfer between accounts

## Architecture
(Add architecture.png diagram here)

## Design notes
- Use optimistic locking (`@Version`) to prevent lost updates
- Insert immutable transaction rows for every money movement
- Transfers are executed in a DB transaction (debit + credit + ledger inserts)
- Idempotency keys for safe retries (planned)

## Next steps
- Add idempotency-key table/logic
- Add Testcontainers integration tests
- Improve audit & monitoring (Actuator, Prometheus)

## API Documentation (Swagger)

After running the app (locally or via Docker), open:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Use the **Authorize** button in Swagger UI to paste your JWT `Bearer <token>` to test protected endpoints.

