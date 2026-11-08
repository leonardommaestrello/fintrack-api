# FinTrack API

A REST API for personal finance management, built with **Java 17**, **Spring Boot 4**, and **PostgreSQL**. FinTrack provides JWT-based authentication, user-specific income and expense tracking, category management, financial summaries, filtering, and CSV exports.

> **Project status:** Backend implementation completed for the currently planned features. Automated tests: **59 passing** in the latest verified full run. A React frontend is planned separately.

## Technology stack

- Java 17 and Spring Boot 4.0.9
- Spring Web MVC, Spring Data JPA, Hibernate, Spring Security, Jakarta Validation
- PostgreSQL and Flyway migrations
- JSON Web Tokens (JJWT 0.12.6) and BCrypt password hashing
- Springdoc OpenAPI 3.0.3 / Swagger UI
- Maven Wrapper, JUnit 5, and Mockito

## Features

- Register and sign in with JWT authentication
- Automatically create ten default income and expense categories when registering
- Manage categories belonging to the authenticated user
- Create, read, update, and delete income and expense transactions
- Filter transactions by type, category, and date range
- Export transactions to CSV, with optional filters
- View overall balance, income, expenses, monthly summaries, and expenses by category
- Keep each user's financial data isolated
- Explore the API using Swagger UI

## Requirements

- **JDK 17**
- **PostgreSQL**, running locally on port `5432`
- **Windows PowerShell** for the commands below (other shells require different syntax)
- The included Maven Wrapper (`mvnw.cmd`)

Create a PostgreSQL database named `fintrack` and ensure the configured PostgreSQL user has permission to connect and apply Flyway migrations.

The default connection settings in `src/main/resources/application.yaml` are:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fintrack
    username: postgres
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: 3600000
```

**Never commit database passwords, JWT secrets, or other credentials to Git.** Supply them through environment variables, not hard-coded configuration.

## Important: configure environment variables

FinTrack requires **both** environment variables:

| Variable | Purpose |
| --- | --- |
| `DB_PASSWORD` | Password for the PostgreSQL user configured in `application.yaml` (`postgres` by default) |
| `JWT_SECRET` | Secret used to sign and verify JWTs; use a securely generated, sufficiently long secret |

### PowerShell — recommended setup for the current terminal

Open PowerShell in the project directory. Set both values **before running Maven or starting Spring Boot**:

```powershell
$env:DB_PASSWORD = Read-Host "PostgreSQL password"
$env:JWT_SECRET = Read-Host "JWT secret"
```

Each `Read-Host` command waits for you to **type the value on the next prompt** and press Enter. Do not append the actual password to the command or put it inside the prompt message. Note that plain `Read-Host` displays what you type; use a private terminal and avoid sharing screenshots containing credentials.

For a new development-only JWT secret, you can generate a random 64-byte Base64 value using PowerShell and assign it without displaying the secret:

```powershell
$randomBytes = [byte[]]::new(64)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($randomBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($randomBytes)
```

This generated secret is suitable for local development. Tokens signed with an earlier secret will stop working when the secret changes; log in again to get a new token. In a deployed environment, use a securely managed, persistent secret.

Check whether the variables exist **without printing their values**:

```powershell
Test-Path Env:DB_PASSWORD
Test-Path Env:JWT_SECRET
```

Both commands should return `True`.

**Important:** PowerShell environment variables set with `$env:` apply only to the current PowerShell process and child processes. If you open a **new terminal**, set them again. An IntelliJ Run Configuration has its **own environment configuration**; values configured there are not automatically available to Maven commands run in a separate terminal, and vice versa. Configure both variables in the IntelliJ Run Configuration if you start the app using the IDE.

### Run tests and start the application

This project uses a project-specific Maven settings file named `settings-fintrack.xml` for its development environment. If that file exists locally, run:

```powershell
.\mvnw.cmd -s .\settings-fintrack.xml clean test
.\mvnw.cmd -s .\settings-fintrack.xml spring-boot:run
```

If your environment does not require the custom settings file, use:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Run the commands from the directory containing `pom.xml`. The application runs on **http://localhost:8080** by default. Flyway applies database migrations during startup; Hibernate validates the database schema rather than creating it automatically.

### Troubleshooting environment-variable errors

**`FATAL: password authentication failed for user "postgres"`**

The PostgreSQL password is missing or incorrect in the process running the application/tests, or the database credentials do not match. Set `DB_PASSWORD` in the same PowerShell session and verify that the database user/password are correct. A passing Maven compilation does not prove database connectivity; the Spring Boot context test initializes the database and Flyway.

**`Could not resolve placeholder 'DB_PASSWORD'` or `'JWT_SECRET'`**

Set the missing environment variable before starting the app or tests. If using IntelliJ, check the environment variables in the application's Run Configuration.

**JWT authentication fails after changing `JWT_SECRET`**

Previously issued tokens may no longer be valid. Sign in again to obtain a new token.

**Maven works in PowerShell but IntelliJ shows unresolved Swagger imports**

Check that IntelliJ imported the same `pom.xml` and Maven settings. In this project's development environment, switching IntelliJ's Maven home from **Use Maven wrapper** to **Bundled (Maven 3)** resolved a Maven import/synchronization error. The command-line Maven Wrapper can still be used independently.

## API endpoints

All endpoints below are relative to `http://localhost:8080`. Except for registration and login, API endpoints require `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/auth/register` | Register a user and initialize default categories |
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| GET | `/api/categories` | List the authenticated user's categories |
| GET | `/api/categories/{id}` | Get a category |
| POST | `/api/categories` | Create a category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete a category |
| GET | `/api/transactions` | List transactions; optional filters |
| GET | `/api/transactions/{id}` | Get a transaction |
| POST | `/api/transactions` | Create a transaction |
| PUT | `/api/transactions/{id}` | Update a transaction |
| DELETE | `/api/transactions/{id}` | Delete a transaction |
| GET | `/api/transactions/export` | Download transactions as CSV |
| GET | `/api/dashboard/summary` | Overall financial summary |
| GET | `/api/dashboard/monthly` | Monthly financial summaries |
| GET | `/api/dashboard/expenses-by-category` | Expense totals grouped by category |

### Transaction filters

The `GET /api/transactions` and `GET /api/transactions/export` endpoints support these optional query parameters:

| Parameter | Example | Meaning |
| --- | --- | --- |
| `type` | `EXPENSE` | Transaction type: `INCOME` or `EXPENSE` |
| `categoryId` | `7` | Category ID |
| `startDate` | `2026-11-01` | Start of date range, ISO format |
| `endDate` | `2026-11-30` | End of date range, ISO format |

Example:

```http
GET /api/transactions?type=EXPENSE&startDate=2026-11-01&endDate=2026-11-30
Authorization: Bearer <token>
```

A date range where `startDate` is later than `endDate` is rejected.

## Swagger / OpenAPI

After starting the application, visit:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

To try a protected endpoint in Swagger UI:

1. Call `POST /api/auth/login` to obtain a JWT.
2. Click **Authorize** in Swagger UI.
3. Paste the token **without** the `Bearer ` prefix; the HTTP Bearer security scheme adds it automatically.
4. Execute a protected request such as `GET /api/dashboard/summary`.

Swagger documentation is intended for development. Consider disabling or restricting it in production using appropriate security and Springdoc configuration.

## Data model and business rules

The database uses Portuguese table names: `usuario`, `categoria`, and `transacao`.

- Users can access only their own categories and transactions.
- Each transaction has a positive monetary amount and a type (`INCOME` or `EXPENSE`).
- The category assigned to a transaction must belong to the same user and have the same transaction type.
- Categories cannot be deleted while referenced by transactions.
- Database constraints and service validation enforce important ownership and consistency rules.
- Monetary values use `BigDecimal` in Java and a decimal column in PostgreSQL.

## Tests

Run the full suite:

```powershell
.\mvnw.cmd -s .\settings-fintrack.xml clean test
```

The latest verified run reported:

```text
Tests run: 59, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The suite includes unit tests for authentication, category management, transactions, dashboards, CSV exporting, and a Spring Boot application-context test. The context test requires a working PostgreSQL connection and both environment variables.

## Project structure

```text
com.fintrack.api
├── auth
├── category
├── common
├── config
├── dashboard
├── security
├── transaction
├── user
└── FintrackApiApplication
```

Business logic is organized behind service interfaces, with implementations under `service.impl`. Controllers expose REST endpoints, repositories handle persistence, and DTOs define API payloads.

## Roadmap

- [x] Backend REST API and PostgreSQL persistence
- [x] JWT authentication and user-level data isolation
- [x] Categories, transactions, filters, CSV export, and dashboards
- [x] Automated backend tests
- [x] Swagger UI and OpenAPI generation
- [ ] Final end-to-end API validation and Postman collection
- [ ] React frontend based on the finalized API specification
- [ ] Full-stack integration and portfolio presentation

## License

No license has been selected yet. Add a `LICENSE` file before specifying reuse permissions.
