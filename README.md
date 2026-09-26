# Gym CRM

A backend CRM system for managing gym trainees, trainers, training programs, and trainee–trainer relationships. The project demonstrates a progressive Java and Spring backend architecture, from layered application design and relational persistence to security, asynchronous messaging, observability, and containerized development.

## Related Microservice

The project includes a separate workload microservice responsible for workload-related processing and asynchronous communication:

- [Workload Service](https://github.com/ktmn21/workload_service)

See the microservice repository for its own setup instructions, architecture, and implementation details.

## Features

- Manage trainees, trainers, training types, and training records.
- Create and update trainee–trainer relationships.
- Expose backend functionality through REST APIs.
- Apply layered architecture with controllers, services, repositories/DAOs, DTOs, and domain models.
- Validate incoming requests and handle errors through centralized exception handling.
- Support authentication and authorization with Spring Security and JWT.
- Persist transactional data in PostgreSQL using Hibernate/JPA.
- Store trainer summary and follow-up data in MongoDB.
- Communicate with the workload service through asynchronous, message-driven processing with ActiveMQ.
- Provide health and metrics components for application monitoring.
- Add request tracing with MDC-based transaction identifiers.
- Run the application and infrastructure services with Docker Compose.
- Include unit and integration tests for core services, repositories, listeners, and messaging components.

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot, Spring MVC, Spring Data
- **Persistence:** Hibernate, JPA, PostgreSQL, MongoDB
- **Security:** Spring Security, JWT, password hashing
- **Messaging:** ActiveMQ, asynchronous processing, event-driven communication
- **Testing:** JUnit, Mockito, integration testing
- **Build:** Maven
- **DevOps:** Docker, Docker Compose
- **Configuration:** Spring profiles
- **Observability:** Logging, MDC request/transaction identifiers, health checks, and metrics

## Architecture

The main application follows a layered backend structure:

```text
Controller
    ↓
Service
    ↓
Repository / DAO
    ↓
Database
```

The workload functionality is separated into its own microservice. The main application communicates with it asynchronously through messaging, keeping workload processing independent from the core CRM request flow.

### Main packages

- `config` — application and framework configuration.
- `controller` — REST API endpoints.
- `dao` — data-access components.
- `dto` — request and response objects.
- `exceptions` — custom exceptions and global error handling.
- `health` — application health checks.
- `logging` — request and transaction logging support.
- `metrics` — application metrics.
- `model` — domain entities and models.
- `security` — authentication, authorization, and JWT-related functionality.
- `service` — business logic.
- `util` — shared utility classes.

## Project Structure

```text
.
├── Dockerfile
├── docker-compose.yml
├── docker-compose.standalone.yml
├── pom.xml
├── init-scripts/
└── src/
    ├── main/
    │   ├── java/com/example/gymcrm/
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dao/
    │   │   ├── dto/
    │   │   ├── exceptions/
    │   │   ├── health/
    │   │   ├── logging/
    │   │   ├── metrics/
    │   │   ├── model/
    │   │   ├── security/
    │   │   ├── service/
    │   │   └── util/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       ├── application-dev.yml
    │       ├── application-docker.yml
    │       ├── application-standalone.yml
    │       ├── application-stg.yml
    │       ├── application-prod.yml
    │       ├── data.sql
    │       └── logback.xml
    └── test/
```

## Prerequisites

### Local JVM setup

- Java Development Kit compatible with the version configured in `pom.xml`.
- Maven, or the included Maven Wrapper.
- PostgreSQL running locally.
- MongoDB running locally if MongoDB-backed features are enabled.
- ActiveMQ running locally if asynchronous messaging is enabled.

### Docker setup

- Docker Engine.
- Docker Compose v2 or a compatible Docker Compose installation.

## Configuration

The application contains separate Spring configuration files for different environments:

- `application-local.yml` — local development.
- `application-dev.yml` — development environment.
- `application-docker.yml` — Docker-based execution.
- `application-standalone.yml` — standalone configuration.
- `application-stg.yml` — staging environment.
- `application-prod.yml` — production environment.

Review the selected profile before starting the application. Configure database, messaging, JWT, and other environment-specific values through environment variables or the appropriate YAML file. Never commit real passwords, JWT secrets, or other credentials.

## Running with Docker Compose

```bash
git clone https://github.com/ktmn21/gym-project.git
cd gym-project
docker compose up --build
```

Run in the background:

```bash
docker compose up --build -d
```

Stop the services:

```bash
docker compose down
```

Run the standalone configuration:

```bash
docker compose -f docker-compose.standalone.yml up --build
```

## Running with Maven

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\\mvnw.cmd spring-boot:run
```

Or with installed Maven:

```bash
mvn spring-boot:run
```

Select a profile explicitly:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Testing

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\\mvnw.cmd test
```

The test suite covers business services, repositories/DAOs, messaging listeners, and other application components using JUnit and Mockito. Integration tests may require the configured databases or containerized dependencies.

## Database and Seed Data

The repository includes `src/main/resources/data.sql` for initial data. When using a fresh database, check the selected Spring profile to confirm whether schema initialization and seed data are enabled.

## API Documentation

REST endpoints are implemented in:

```text
src/main/java/com/example/gymcrm/controller
src/main/java/com/example/gymcrm/dto
```

If API documentation is enabled in the selected configuration, use the generated OpenAPI/Swagger endpoint exposed by the running application.

## Security

The project includes Spring Security and JWT-based authentication and authorization. In a deployed environment:

- Store JWT secrets outside the source code.
- Use strong, environment-specific credentials.
- Do not commit local `.env` files or passwords.
- Use HTTPS when exposing the API outside a trusted local network.
- Restrict database and ActiveMQ access to trusted services.

## Development Practices

- Keep business logic in the service layer.
- Use DTOs at API boundaries instead of exposing persistence entities directly.
- Validate incoming data before processing requests.
- Use centralized exception handling for consistent API errors.
- Add tests when introducing or changing business behavior.
- Use feature branches and descriptive commit messages.
- Review configuration files before running the application in a new environment.

## Future Improvements

- Add complete OpenAPI documentation and example requests.
- Add a production deployment guide.
- Add CI/CD checks for build, tests, and code quality.
- Add Testcontainers-based integration tests for PostgreSQL, MongoDB, and ActiveMQ.
- Add role-based endpoint documentation and sample JWT authentication flow.
- Add API response examples and database diagrams.

## Author

**Kutman Mukarapov**

- GitHub: [@ktmn21](https://github.com/ktmn21)
- Repository: [gym-project](https://github.com/ktmn21/gym-project)
- Related service: [workload_service](https://github.com/ktmn21/workload_service)
