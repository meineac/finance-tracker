# Finance Tracker API

A RESTful personal finance management API built with **hexagonal architecture** (ports & adapters). Supports user registration/authentication, multi-account management, transaction tracking with categories, and budget planning.

## Tech Stack

- **Java 25** · **Spring Boot 4**
- **Spring Security** + **JWT** (jjwt) — stateless authentication
- **Spring Data JPA** + **PostgreSQL** — persistence
- **Flyway** — database migrations
- **Docker Compose** — containerized PostgreSQL
- **SpringDoc OpenAPI** — interactive API documentation
- **Lombok** — boilerplate reduction

## Architecture

The project follows **hexagonal (ports & adapters) architecture** with feature-based packaging:

```
com.financetracker
├── user/                          # User & authentication module
│   ├── api/web/                   # Controllers, DTOs (driving adapter)
│   ├── core/model/                # Domain entities
│   ├── core/service/              # Business logic (ports)
│   └── infrastructure/            # JPA repositories, security (driven adapters)
│
├── account/                       # Account management module
│   ├── api/web/
│   ├── core/model/
│   ├── core/service/
│   └── infrastructure/
│
├── transaction/                   # Transaction & category module
│   ├── api/web/
│   ├── core/model/
│   ├── core/service/
│   └── infrastructure/
│
└── shared/                        # Cross-cutting concerns
    ├── api/                       # Global exception handling, annotations
    ├── core/                      # Shared exceptions
    └── infrastructure/            # Security config, OpenAPI config, CORS
```

Each module defines its own **ports** (interfaces in `core/service/`) and **adapters** (implementations in `infrastructure/` and `api/`), keeping the domain logic independent of frameworks.

## Features

- **Authentication** — register, login, JWT token issuance and validation
- **Accounts** — create and manage multiple financial accounts (checking, savings, etc.)
- **Transactions** — record income/expense transactions tied to accounts and categories
- **Categories** — user-defined transaction categories
- **Budgets** — monthly budget limits per category with constraint enforcement
- **Ownership Validation** — users can only access their own resources
- **Database Migrations** — versioned schema evolution via Flyway

## Getting Started

### Prerequisites

- Java 25+
- Docker & Docker Compose
- Maven 3.9+

### Run

1. Clone the repository:
   ```bash
   git clone https://github.com/meineac/finance-tracker.git
   cd finance-tracker
   ```

2. Create a `.env` file from the example:
   ```bash
   cp .env.example .env
   ```

3. Start PostgreSQL:
   ```bash
   docker compose up -d
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Open API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### API Overview

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and receive JWT | No |
| POST | `/api/accounts` | Create a financial account | Yes |
| GET | `/api/accounts` | List user's accounts | Yes |
| POST | `/api/transactions` | Record a transaction | Yes |
| GET | `/api/transactions/account/{id}` | Get account transactions | Yes |
| POST | `/api/categories` | Create a category | Yes |
| GET | `/api/categories` | List user's categories | Yes |

All authenticated endpoints require a `Bearer` token in the `Authorization` header.

## Testing

```bash
./mvnw test
```
