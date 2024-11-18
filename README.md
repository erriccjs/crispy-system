# CrispySystem Project

This project is a monolithic application for managing user accounts and transactions, built with Java Spring Boot for the backend. The project is designed with domain separation to facilitate an eventual transition to a microservices architecture. It also includes a frontend directory for future UI development.

## Features

- User authentication and account management
- Transaction tracking
- Database setup with PostgreSQL
- Redis setup for caching and distributed locking
- Prepared for easy transition to microservices
- Frontend CLI

## Project Structure

.
├── backend
│   └── crispy-system                 # Backend directory for the main Spring Boot application
│       ├── src
│       │   └── main
│       │       └── java
│       │           └── com
│       │               └── myproject
│       │                   └── crispysystem
│       │                       ├── user          # User domain: user model, service, repository, controller
│       │                       ├── account       # Account domain: account model, service, repository, controller
│       │                       └── config        # Configuration files (Redis, Security, etc.)
│       └── pom.xml                    # Backend project configuration file (Maven)
│
├── frontend                           # Frontend directory for future UI components (currently empty)
├── docker-compose.yml                 # Docker Compose file for running PostgreSQL and Redis services
└── README.md                          # Project README with setup instructions and documentation

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Docker** for running PostgreSQL and Redis
- **Maven** (if not using IntelliJ’s built-in Maven)

### Setting Up the Environment

1. **Clone the repository**:

   ```bash
   git clone https://github.com/erriccjs/crispysystem.git
   cd crispysystem
   ```

2. **Run the Docker services**: Ensure Docker is running and set up PostgreSQL, Redis and backend with Docker Compose:

   ```bash
   docker compose up --build -d
   ```

3. **Configure the Backend Database**: The database schema will be created automatically by Spring Boot on application startup. Database settings are in application.properties.

4. **Build and run the backend**:

   ```bash
   cd cli/SystemAtmCli
   mvn clean package
   ```

## Project Setup Notes

- **Backend (Java Spring Boot)**: The backend project (crispy-system) handles user and account management, with PostgreSQL for data persistence and Redis for caching and distributed locking.
- **cli**: The cli directory is the SimpleATMCLI Application.
- **Microservices Transition**: The project structure is designed to allow for easy separation of the backend services into microservices.

## API Endpoints

- **POST /api/login** - Login User or Create a new user.
- **POST /api/deposit** - Deposit this amount to logged in customer.
- **POST /api/withdraw** - Withdraw this amount from the logged in customer.
- **POST /api/transfer** - Transfers this amount from the logged in customer to the target customer.
- **POST /api/logout** - Logout of the current customer. 