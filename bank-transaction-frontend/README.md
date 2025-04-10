# Bank Transaction Management System - Frontend

A modern Vue.js frontend application for managing bank transactions.

## Features

- View all transactions in a responsive table
- Filter and paginate transactions
- Create new transactions
- Edit existing transactions
- Delete transactions
- Real-time amount formatting
- Transaction type categorization (Deposit, Withdrawal, Transfer)
- Status tracking for transactions

## Technology Stack

- Vue.js 3
- Element Plus UI Framework
- Axios for API communication
- Vue Router for navigation
- Nginx for production deployment

## Prerequisites

- Node.js (version 16 or higher)
- npm or yarn package manager

## Installation

1. Clone the repository:
```bash
git clone https://github.com/beyondhp/bank_transaction_management
cd bank-transaction-frontend
```

2. Install dependencies:
```bash
npm install
# or
yarn install
```

## Development

To start the development server:

```bash
npm run serve
# or
yarn serve
```

The application will be available at `http://localhost:8080` (port may vary if 8080 is already in use)

## Building for Production

To create a production build:

```bash
npm run build
# or
yarn build
```

The built files will be generated in the `dist` directory.

## Docker Deployment

The project includes a Dockerfile for containerization:

```bash
# Build the Docker image
docker build -t bank-transaction-frontend .

# Run the container
docker run -p 8081:80 bank-transaction-frontend
```

For a complete deployment including both frontend and backend, use Docker Compose from the project root:

```bash
# Navigate to the project root
cd ..

# Start all services
docker-compose up -d
```

## Nginx Configuration

The application includes `nginx.conf` for production deployment, which provides:

- Static file serving for the Vue.js application
- API proxy to the backend service
- Health check endpoint for container orchestration
- SPA routing support (client-side routing)

Key Nginx configuration features:

```nginx
# Static file handling for SPA
location / {
    try_files $uri $uri/ /index.html;
}

# Proxy backend API requests
location /api/ {
    proxy_pass http://backend:8080/;
    # Additional proxy headers...
}

# Health check endpoint
location /health {
    return 200 'OK';
    # Additional config...
}
```

## Project Structure

```
src/
├── views/           # Page components (like TransactionList.vue)
├── router/          # Vue Router configuration
├── utils/           # Helper functions and utilities
├── App.vue          # Root component
└── main.js          # Application entry point
```

Other important files:

```
public/              # Static assets and index.html
Dockerfile           # Docker configuration
nginx.conf           # Nginx configuration for production
package.json         # npm dependencies and scripts
```

## API Integration

The frontend communicates with the backend REST API. Key endpoints used:

- GET /api/transactions/paged - Fetch paginated transactions with filtering
- POST /api/transactions - Create a new transaction
- PUT /api/transactions/:id - Update an existing transaction
- DELETE /api/transactions/:id - Delete a transaction

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details 