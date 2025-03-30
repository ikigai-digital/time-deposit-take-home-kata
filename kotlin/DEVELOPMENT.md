# Time Deposit Application Development Guide

## Architecture & Design Principles

### SOLID Principles Implementation

1. **Single Responsibility Principle (SRP)**
   - Each class has one primary responsibility
   - Services handle business logic
   - Repositories handle data persistence
   - DTOs handle data transfer

2. **Open/Closed Principle (OCP)**
   - Use interfaces for extensibility
   - New features can be added without modifying existing code
   - Abstract base classes for common functionality

3. **Liskov Substitution Principle (LSP)**
   - All implementations can be substituted for their base types
   - Consistent behavior across implementations

4. **Interface Segregation Principle (ISP)**
   - Focused interfaces for specific functionality
   - Clients only depend on methods they use

5. **Dependency Inversion Principle (DIP)**
   - High-level modules depend on abstractions
   - Use dependency injection for loose coupling

## Package Structure

```
kotlin/
├── domain/           # Domain models and business logic
│   ├── model/       # Core domain entities
│   └── service/     # Business services
├── infrastructure/   # External dependencies and implementations
│   ├── repository/  # Data persistence
│   └── config/      # Application configuration
└── api/             # API endpoints and controllers
    ├── dto/         # Data transfer objects
    └── controller/  # REST controllers
```

## Building and Running

### Prerequisites
- JDK 17 or higher
- Gradle 7.x or higher

### Build Instructions
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Running the Application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### API Documentation
Once running, access Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## Development Workflow

1. Create feature branch from `main`
2. Implement changes following SOLID principles
3. Add unit tests
4. Create pull request
5. Code review
6. Merge to main

## Testing Strategy

- Unit tests for business logic
- Integration tests for repositories
- API tests for controllers
- Coverage target: >80%
