# CrispySystem Project

This project is a monolithic application for managing user accounts and transactions, built with Java Spring Boot for the backend. The project is designed with domain separation to facilitate an eventual transition to a microservices architecture. It also includes a frontend directory for future UI development.

## Features

- User authentication and account management
- Transaction tracking with audit logs
- Database setup with PostgreSQL
- Redis setup for caching and distributed locking
- Prepared for easy transition to microservices
- Frontend setup for future development

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

2. **Run the Docker services**: Ensure Docker is running and set up PostgreSQL and Redis with Docker Compose:

   ```bash
   docker-compose up -d
   ```

3. **Configure the Backend Database**: The database schema will be created automatically by Spring Boot on application startup. Database settings are in application.properties.

4. **Build and run the backend**:

   ```bash
   cd backend/crispy-system
   mvn clean package
   java -jar target/crispy-system.jar
   ```

## Project Setup Notes

- **Backend (Java Spring Boot)**: The backend project (crispy-system) handles user and account management, with PostgreSQL for data persistence and Redis for caching and distributed locking.
- **Frontend**: The frontend directory is included for future UI development. Initializing this directory and adding frontend assets can happen as needed.
- **Microservices Transition**: The project structure is designed to allow for easy separation of the backend services into microservices.
