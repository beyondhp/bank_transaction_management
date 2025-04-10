# Bank Transaction Management System - Backend

Spring Boot backend service for the Bank Transaction Management System.

## Overview

This backend service provides a RESTful API for managing bank transactions with features including caching, validation, and comprehensive error handling. The service uses an in-memory data store (ConcurrentHashMap) with caching support.

## Technology Stack

- Java 21
- Spring Boot 3.2.3
- Spring Cache
- Spring Validation
- Spring Boot Actuator
- Lombok
- SpringDoc/OpenAPI

## Prerequisites

- JDK 21 or higher
- Maven 3.8+
- IDE with Lombok support (IntelliJ IDEA recommended)

## External Libraries

This project uses the following external libraries outside the standard JDK:

| Library | Version | Purpose |
|---------|---------|---------|
| Spring Boot | 3.2.3 | Core framework providing dependency injection, web server, auto-configuration |
| Spring Boot Starter Web | 3.2.3 | Web application development with Spring MVC |
| Spring Boot Starter Validation | 3.2.3 | Bean validation with Hibernate validator |
| Spring Boot Starter Cache | 3.2.3 | Caching abstraction for improving application performance |
| Spring Boot Starter Actuator | 3.2.3 | Provides production-ready features like health checks and monitoring |
| Project Lombok | 1.18.30 | Reduces boilerplate code by generating getters, setters, constructors, etc. |
| SpringDoc OpenAPI | 2.3.0 | Generates OpenAPI documentation and provides Swagger UI |

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/beyondhp/bank_transaction_management
cd bank-transaction-backend
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

## API Endpoints

### Transaction Management

- `GET /api/transactions` - Retrieve all transactions
- `GET /api/transactions/{id}` - Get a specific transaction by ID
- `GET /api/transactions/paged` - Get paginated transactions with filtering support
- `POST /api/transactions` - Create a new transaction
- `PUT /api/transactions/{id}` - Update an existing transaction
- `DELETE /api/transactions/{id}` - Delete a transaction

## Request/Response Examples

### Create Transaction
```json
POST /api/transactions
{
    "amount": 1000.00,
    "type": "DEPOSIT",
    "description": "Monthly salary deposit",
    "sourceAccount": null,
    "destinationAccount": "ACCT1234567890",
    "status": "INITIATED"
}
```

### Transaction Response
```json
{
    "id": 1234567890123456789,
    "amount": 1000.00,
    "type": "DEPOSIT",
    "description": "Monthly salary deposit",
    "sourceAccount": null,
    "destinationAccount": "ACCT1234567890",
    "status": "INITIATED",
    "timestamp": "2024-03-15T10:30:00Z",
    "processingDate": "2024-03-15T10:30:00Z"
}
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/bank/transaction/
│   │       ├── config/         # Configuration classes
│   │       ├── controller/     # REST controllers
│   │       ├── model/          # Domain models and enums
│   │       ├── service/        # Business logic interfaces and implementations
│   │       ├── validator/      # Custom validation logic
│   │       ├── exception/      # Exception handling
│   │       ├── util/           # Utility classes (including SnowflakeIdGenerator)
│   │       └── BankTransactionApplication.java
│   └── resources/
│       └── application.yml     # Application configuration
└── test/
    └── java/
        └── com/bank/transaction/
            ├── controller/     # Controller tests
            ├── loadtest/       # Performance and stress tests
            └── service/        # Service tests
```

## Scripts

```
scripts/
└── generate_transactions.py    # Python script for generating test transaction data
```

## Features

- In-memory transaction storage using ConcurrentHashMap with caching
- Snowflake ID generation for transaction IDs
- Comprehensive input validation
- Exception handling with meaningful error messages
- Swagger API documentation
- Unit and integration tests for core functionality
- Spring Boot Actuator for health checks and monitoring
- Performance testing with detailed metrics
- Test data generation for bulk transaction creation

## Containerization and Deployment

### Docker

The application can be containerized using Docker. A Dockerfile is provided in the project root.

To build and run with Docker:

```bash
# Build the Docker image
docker build -t bank-transaction-backend .

# Run the container
docker run -p 8080:8080 bank-transaction-backend
```

### Docker Compose

For convenience, a docker-compose.yml file is provided at the project root to run the complete application (both backend and frontend):

```bash
# Navigate to the project root
cd ..

# Start all services
docker-compose up -d
```

The Docker Compose configuration includes:
- Backend Spring Boot service with health checks
- Volume mapping for logs
- Custom network for service communication
- Environment variables for optimal JVM configuration

## Environment Variables

The application supports configuration through environment variables:

- `SPRING_PROFILES_ACTIVE`: Set the active Spring profile (default, docker, etc.)
- `JAVA_OPTS`: JVM configuration options for memory management
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`: Configure Actuator endpoints to expose
- `MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS`: Control health check detail level

## Performance Testing

The application includes comprehensive performance tests to measure API responsiveness under load.

### Running Performance Tests

```bash
mvn test -Dtest=TransactionApiPerformanceTest
```

Performance tests include:
- Create transaction performance test
- Update transaction performance test
- Query transaction performance test
- Delete transaction performance test

Each test measures:
- Average response time
- Throughput (transactions per second)
- Success rate
- Error distribution

### Test Parameters
The performance tests can be configured with the following parameters:
- Number of concurrent users
- Test duration
- Transaction types distribution
- Ramp-up period

## Test Data Generation

For testing with large datasets, a Python script is provided to generate realistic transaction data directly through API calls.

### Running Data Generation Script

```bash
cd scripts
python generate_transactions.py
```

The script will generate 100 random transactions by default and send them to the API endpoint running at `http://localhost:8080/api/transactions`.

### Script Configuration

To modify the script behavior, you can edit the following variables at the top of the script:

- `API_BASE_URL`: The base URL of the API (default: "http://localhost:8080/api")
- `NUM_TRANSACTIONS`: Number of transactions to generate (default: 100)
- `STATUS_WEIGHTS`: Weight distribution for transaction statuses
- `TRANSACTION_TYPES`: Available transaction types (DEPOSIT, WITHDRAWAL, TRANSFER)

The script provides real-time feedback during execution and a summary of the generated transactions when complete.

## Development

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add appropriate comments for complex logic
- Include JavaDoc for public methods

### Testing

Run the tests using:
```bash
mvn test
```

### API Documentation

Access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Error Handling

The API uses standard HTTP status codes:
- 200: Success
- 400: Bad Request
- 404: Not Found
- 500: Internal Server Error

Error responses include:
```json
{
    "status": 400,
    "message": "Invalid input",
    "details": ["Amount must be positive"]
}
```

## Contributing

1. Follow the code style guidelines
2. Write unit tests for new features
3. Update documentation as needed
4. Create detailed pull requests

## License

This project is licensed under the MIT License - see the LICENSE file for details 