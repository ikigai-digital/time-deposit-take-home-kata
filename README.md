# Time Deposit Kata

A RESTful API for managing time deposits with automatic interest calculation. Built with .NET 9.0, following Clean Architecture principles and SOLID design patterns.

## Overview

This application provides endpoints to manage time deposits with different plan types (Basic, Student, Premium), each with specific interest calculation rules. The implementation follows Test-Driven Development (TDD) and uses Testcontainers for integration testing.

### Features

- **RESTful API** for time deposit management
- **Interest Calculation** with extensible Strategy pattern
- **PostgreSQL Database** with EF Core migrations
- **Automatic Seeding** of sample data
- **Docker Containerization** with Docker Compose
- **Comprehensive Testing** with unit and integration tests

### API Endpoints

- `GET /api/time-deposits` - Retrieve all time deposits with withdrawals
- `POST /api/time-deposits/update-balances` - Update balances by applying monthly interest

### Interest Calculation Rules

- **Basic Plan**: 1% annual interest (applied after 30 days)
- **Student Plan**: 3% annual interest (applied between 30-365 days, no interest after 1 year)
- **Premium Plan**: 5% annual interest (applied after 45 days)
- **All Plans**: No interest for the first 30 days

## Project Structure

```
c#/
├── time-deposit-kata-net/          # Main application project
│   ├── Api/                        # API layer (Controllers)
│   │   └── TimeDepositsController.cs
│   ├── Application/                # Application layer (Use cases, DTOs)
│   │   ├── ITimeDepositRepository.cs
│   │   ├── ITimeDepositService.cs
│   │   ├── TimeDepositResponse.cs
│   │   └── TimeDepositService.cs
│   ├── Domain/                     # Domain layer (Business logic)
│   │   ├── Entities/              # Domain entities
│   │   │   ├── TimeDeposit.cs
│   │   │   └── Withdrawal.cs
│   │   ├── Services/              # Domain services
│   │   │   └── TimeDepositCalculator.cs
│   │   ├── Interfaces/            # Domain interfaces
│   │   │   └── IInterestCalculationStrategy.cs
│   │   ├── Strategies/           # Strategy pattern implementations
│   │   │   ├── BasicPlanInterestStrategy.cs
│   │   │   ├── StudentPlanInterestStrategy.cs
│   │   │   └── PremiumPlanInterestStrategy.cs
│   │   └── Factories/           # Factory pattern
│   │       └── InterestStrategyFactory.cs
│   ├── Infrastructure/           # Infrastructure layer (Data access)
│   │   ├── TimeDepositDbContext.cs
│   │   ├── TimeDepositRepository.cs
│   │   └── DatabaseSeeder.cs
│   ├── Migrations/              # EF Core migrations
│   ├── Program.cs               # Application entry point
│   ├── Dockerfile              # Docker image definition
│   └── appsettings.json        # Configuration
│
├── time-deposit-kata-test/      # Test project
│   ├── Unit/                    # Unit tests
│   │   └── TimeDepositCalculatorTest.cs
│   └── Integration/             # Integration tests
│       ├── GetTimeDepositsIntegrationTest.cs
│       └── UpdateBalancesIntegrationTest.cs
│
└── docker-compose.yml           # Docker Compose configuration
```

### Architecture

The project follows **Clean Architecture** (Hexagonal Architecture) principles:

- **Domain Layer**: Core business entities and logic (no dependencies)
- **Application Layer**: Use cases and application services (depends on Domain)
- **Infrastructure Layer**: Data access and external services (depends on Application/Domain)
- **API Layer**: Controllers and HTTP concerns (depends on Application)

### Design Patterns

- **Strategy Pattern**: Extensible interest calculation for different plan types
- **Repository Pattern**: Abstraction of data access
- **Factory Pattern**: Strategy creation and registration

## How to Run

### Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- .NET 9.0 SDK (for local development without Docker)

### Option 1: Docker Compose (Recommended)

1. Navigate to the `c#` directory:
   ```bash
   cd c#
   ```

2. Build and start all services:
   ```bash
   docker-compose up --build
   ```

3. The application will be available at:
   - **API**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger
   - **PostgreSQL**: localhost:5432

4. Database is automatically:
   - Migrated on startup
   - Seeded with sample data

### Option 2: Local Development

1. **Set up PostgreSQL**:
   - Install PostgreSQL 16
   - Create database: `timedeposits`
   - Update connection string in `appsettings.json` or `appsettings.Development.json`

2. **Restore dependencies**:
   ```bash
   cd c#
   dotnet restore
   ```

3. **Run migrations**:
   ```bash
   cd time-deposit-kata-net
   dotnet ef database update
   ```

4. **Run the application**:
   ```bash
   dotnet run
   ```

5. Access the API at http://localhost:5000 (or https://localhost:5001)

### Running Tests

Run all tests:
```bash
cd c#
dotnet test
```

Run only unit tests:
```bash
dotnet test --filter "Category!=Integration"
```

Run only integration tests:
```bash
dotnet test --filter "Category=Integration"
```

**Note**: Integration tests require Docker to be running (uses Testcontainers).

## Services

- **api**: The .NET Web API application
- **postgres**: PostgreSQL 16 database

## Environment Variables

Connection string can be configured via:
- Environment variable: `ConnectionStrings__DefaultConnection`
- `appsettings.json` / `appsettings.Development.json`
- Docker Compose environment variables

## Database

- **Automatic migrations** on application startup
- **Sample data seeding** on first run (if database is empty)
- **Data persistence** in Docker volume `postgres_data`

## Stopping Services

Stop containers:
```bash
docker-compose down
```

Stop and remove volumes (deletes database data):
```bash
docker-compose down -v
```

## Technology Stack

- **.NET 9.0** - Application framework
- **Entity Framework Core 9.0** - ORM
- **PostgreSQL** - Database
- **NUnit** - Testing framework
- **Testcontainers** - Integration testing
- **Docker & Docker Compose** - Containerization
- **Swagger/OpenAPI** - API documentation
