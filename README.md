# Bank Transaction Management System

A full-stack application for managing bank transactions, built with Spring Boot and Vue.js.

## Project Overview

This project consists of two main components:

### Backend (bank-transaction-backend)
- Built with Java 21 and Spring Boot 3.2.3
- RESTful API design
- In-memory transaction storage using ConcurrentHashMap with caching
- Exception handling and validation
- Swagger API documentation via SpringDoc
- Spring Boot Actuator for health checks and monitoring

### Frontend (bank-transaction-frontend)
- Built with Vue.js 3
- Modern and responsive UI using Element Plus
- Real-time transaction management
- API integration with Axios
- Nginx server for production deployment

## System Requirements

### Backend
- Java Development Kit (JDK) 21 or higher
- Maven 3.8+

### Frontend
- Node.js 16 or higher
- npm or yarn package manager

## Quick Start

### Local Development

1. Clone the repository:
```bash
git clone https://github.com/beyondhp/bank_transaction_management
cd bank-transaction
```

2. Start the backend:
```bash
cd bank-transaction-backend
mvn spring-boot:run
```
The backend server will start at `http://localhost:8080`

3. Start the frontend:
```bash
cd bank-transaction-frontend
npm install
npm run serve
```
The frontend development server will be available at `http://localhost:8080` (may vary depending on port availability)

### Docker Deployment

The project includes a complete Docker setup for easy deployment:

```bash
# Build and start the containers
docker-compose up -d

# Stop the containers
docker-compose down
```

With Docker deployment:
- Backend API will be available at `http://localhost:8080`
- Frontend application will be available at `http://localhost:8081`

## Features

- Transaction Management
  - Create, read, update, and delete transactions
  - Support for multiple transaction types (Deposit, Withdrawal, Transfer)
  - Transaction filtering and pagination
  - Real-time amount formatting
- Responsive Design
  - Mobile-friendly interface
  - Modern UI/UX with Element Plus components
- Data Validation
  - Server-side validation
  - Client-side form validation
- Error Handling
  - Comprehensive error messages
  - User-friendly error displays
- Performance Testing
  - API performance stress tests for CRUD operations
  - Detailed performance metrics (response time, throughput, success rate)
  - Concurrent user simulation
- Test Data Generation
  - Python script for generating realistic test transactions
  - Direct API integration for creating test data
  - Configurable transaction types and status distribution
  - Real-time feedback during data generation

## Project Structure

```
bank_transaction_management/
├── bank-transaction-backend/    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bank/transaction/
│   │   │   │   ├── controller/     # REST API controllers
│   │   │   │   ├── service/        # Business logic implementation
│   │   │   │   ├── model/          # Domain models
│   │   │   │   ├── validator/      # Custom validation logic
│   │   │   │   ├── exception/      # Exception handling
│   │   │   │   ├── config/         # Application configuration
│   │   │   │   ├── util/           # Utility classes (including SnowflakeIdGenerator)
│   │   │   │   └── BankTransactionApplication.java  # Application entry point
│   │   │   └── resources/
│   │   │       └── application.yml  # Application configuration
│   │   └── test/                   # Unit and integration tests
│   │       └── java/com/bank/transaction/
│   │           ├── controller/     # Controller tests
│   │           └── loadtest/       # Performance and stress tests
│   ├── scripts/                    # Utility scripts
│   │   └── generate_transactions.py # Script for generating test data
│   ├── pom.xml                     # Maven dependencies and build configuration
│   ├── Dockerfile                  # Backend Docker configuration
│   └── README.md                   # Backend documentation
│
├── bank-transaction-frontend/    # Vue.js frontend
│   ├── src/
│   │   ├── views/                 # Vue components for each page
│   │   ├── router/                # Vue Router configuration
│   │   ├── utils/                 # Helper functions and utilities
│   │   ├── App.vue                # Root component
│   │   └── main.js                # Frontend entry point
│   ├── public/                    # Static assets and index.html
│   ├── package.json               # npm dependencies and scripts
│   ├── nginx.conf                 # Nginx configuration for production
│   ├── Dockerfile                 # Frontend Docker configuration
│   └── README.md                  # Frontend documentation
│
├── docker-compose.yml            # Docker Compose configuration for multi-container deployment
└── README.md                     # Project overview documentation
```

## Docker Configuration

The included `docker-compose.yml` provides a complete deployment setup:

- **Backend Container**:
  - Spring Boot application with in-memory data storage
  - Configured with health checks using Spring Boot Actuator
  - JVM optimization parameters
  - Persistent volume for logs

- **Frontend Container**:
  - Nginx server serving the Vue.js production build
  - Configured to proxy API requests to the backend
  - Health check endpoint at `/health`

- **Networking**:
  - Custom bridge network for container communication
  - Frontend can reference the backend service by name

## API Documentation

The backend API documentation is available at `http://localhost:8080/swagger-ui.html` when the backend server is running.

## Development

For detailed development instructions, please refer to:
- [Backend README](bank-transaction-backend/README.md)
- [Frontend README](bank-transaction-frontend/README.md)

