# Time Deposit Application

## Technologies Used
- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Flyway (database migrations)
- Maven
- Docker & Docker Compose

## Project Structure
- `src/main/java/org/ikigaidigital/` - Main application code
- `src/main/resources/` - Configuration and migration scripts
- `src/test/java/org/ikigaidigital/` - Unit and integration tests
- `docker-compose.yml` - Multi-container orchestration
- `Dockerfile` - Container build for the application
- `.env` / `.env.test` - Environment variables for secrets/configuration (not committed)

## How to Run Locally

### Prerequisites
- Docker & Docker Compose installed
- Java 17 and Maven (for local builds)

### 1. Configure Environment Variables
Create a `.env` file with your database credentials (do not commit this file):
```
DB_USER=your_user
DB_PASSWORD=your_password
DB_NAME=deposit_db
DB_HOST=postgres
DB_PORT=5432
```

### 2. Build and Start with Docker Compose
```
docker-compose up --build
```
This will start both the PostgreSQL database and the Spring Boot application.

### 3. Access the Application
- The API will be available at: `http://localhost:8080`
- Database will be available at: `localhost:5432`

## Running Tests

### 1. Configure Test Environment
Create `.env.test` for test database credentials (do not commit this file):
```
DB_USER=test_user
DB_PASSWORD=test_password
DB_NAME=test_db
DB_HOST=localhost
DB_PORT=5432
```

### 2. Export Test Environment Variables
```
export $(cat .env.test | xargs)
```

### 3. Run Tests
```
mvn test
```

## Database Migrations
- Flyway automatically runs migration scripts in `src/main/resources/db/migration` on startup.
- To add new tables or seed data, create a new migration file (e.g., `V2__insert_values.sql`).

## API Endpoints
- `GET /v1/time-deposit` - List all time deposits
- `PUT /v1/time-deposit/balances` - Update balances for all time deposits

## Security Best Practices
- All sensitive credentials are managed via environment variables and never hardcoded.
- `.env` and `.env.test` must NOT be committed to version control. Add them to your `.gitignore`.

## Design Patterns Applied

- **Builder Pattern**: Used in entity/model classes (e.g., TimeDeposit, Withdrawal) to simplify object creation, especially for objects with many fields.
- **Factory Pattern**: The InterestCalculatorFactory class selects and returns the correct InterestCalculator implementation (Basic, Student, Premium) based on the plan type, encapsulating the creation logic.
- **Strategy Pattern**: Each interest calculation logic (BasicInterestCalculator, StudentInterestCalculator, PremiumInterestCalculator) implements a common interface (InterestCalculator), allowing the calculation strategy to be selected at runtime.
- **Repository Pattern**: Spring Data JPA repositories (e.g., TimeDepositRepository) abstract data access, providing a clean separation between business logic and persistence.
- **Service Layer Pattern**: Business logic is encapsulated in service classes (e.g., TimeDepositService), separating it from controllers and repositories.
- **DTO Pattern**: Data Transfer Objects (e.g., TimeDepositResponse, WithdrawalResponse) are used to shape API responses, decoupling internal models from external representations.
- **Mapper Pattern**: Mapper classes (e.g., TimeDepositMapper, WithdrawalMapper) convert between entities and DTOs, centralizing transformation logic.
- **Dependency Injection**: Spring’s @Autowired and constructor injection are used throughout, promoting loose coupling and testability.

These patterns together ensure the codebase is modular, maintainable, testable, and easy to extend.
