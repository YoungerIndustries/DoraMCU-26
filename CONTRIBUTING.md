# Contributing to DoraMCU

Thank you for your interest in contributing to DoraMCU! This guide will help you set up the development environment and understand the project structure.

## Prerequisites

- **Java 27** (Temurin distribution recommended)
- **Gradle 9.8+** (wrapper included)
- **MySQL 8.0+** (for local database)
- **Node.js 18+** (for dora-electron client)
- **Docker** (optional, for containerized development)

## Setting Up the Development Environment

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/DoraMCU.git
cd DoraMCU
```

### 2. Configure the Database

Create a MySQL database and run the Flyway migrations:

```sql
CREATE DATABASE doramcu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/doramcu
    username: your_username
    password: your_password
```

### 3. Build the Project

```bash
# Build the main application
./gradlew :bootJar --no-daemon

# Build all modules
./gradlew build --no-daemon -x test

# Run tests
./gradlew test --no-daemon
```

### 4. Run the Application

```bash
./gradlew bootRun --no-daemon
```

The application will be available at `http://localhost:8080`.

## Project Structure

```
DoraMCU/
├── src/main/java/com/yisy/modules/doramcu/
│   ├── config/          # Application configuration
│   ├── controller/      # REST API controllers
│   ├── service/         # Business logic services
│   ├── repository/      # JPA data repositories
│   ├── model/           # JPA entity models
│   ├── dto/             # Data transfer objects
│   ├── hex/             # Core 4X game engine
│   ├── traveller/       # Traveller RPG module
│   ├── captainslog/     # Star Trek solo RPG
│   ├── star_trek_4x/    # Star Trek 4X strategy
│   ├── sfb/             # Star Frontiers Battle
│   ├── earth2026/       # Earth 2026 sci-fi RPG
│   ├── dnd5e/           # D&D 5th Edition
│   ├── babylon5/        # Babylon 5 universe
│   ├── zero_zero_seven/ # James Bond 4X
│   ├── fourx/           # Generic 4X framework
│   ├── neuron/          # AI neuron federation
│   ├── unified/         # Cross-game adapters
│   └── shared/          # Universal identity
├── dora-ai/             # AI chat and code analysis
├── dora-dl-core/        # Deep learning core (DL4J)
├── dora-neuron/         # Edge AI node
├── dora-electron/       # Electron desktop client
├── dora-pwa/            # Progressive Web App
├── unity-client/        # Unity 3D game client
├── k8s/                 # Kubernetes deployment configs
└── docs/                # Documentation
```

## Development Guidelines

### Code Style

- Follow the existing code style in each module
- Use 4-space indentation
- Write meaningful commit messages
- Add Javadoc to public APIs

### Adding a New Game Module

1. Create a new package under `src/main/java/com/yisy/modules/doramcu/`
2. Add controller, service, repository, and model classes
3. Add Flyway migration for new tables
4. Register the module in `GameModuleController`
5. Add tests

### Working with the 4X Framework

The `fourx/` module provides a generic 4X game framework. To implement a new 4X game:

1. Implement the service interfaces: `FourxExplorationService`, `FourxCombatService`, `FourxDiplomacyService`, `FourxEconomyService`, `FourxCharacterService`
2. Register your implementations with Spring `@ConditionalOnProperty` for your game module
3. The `FourxController` will automatically route to your implementations

### Running Tests

```bash
# Run all tests
./gradlew test --no-daemon

# Run tests for a specific module
./gradlew :test --tests "*Traveller*" --no-daemon
```

## Docker Development

```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f app
```

## Kubernetes Deployment

```bash
# Deploy to development
kubectl apply -f k8s/base/ -n doramcu-dev
kubectl apply -f k8s/overlays/dev/ -n dorcu-dev

# Deploy to production
kubectl apply -f k8s/base/ -n doramcu-prod
kubectl apply -f k8s/overlays/prod/ -n doramcu-prod
```

## Reporting Issues

- Use GitHub Issues for bug reports and feature requests
- Include steps to reproduce for bugs
- Include your environment details (Java version, OS, etc.)

## Pull Request Process

1. Create a feature branch from `develop`
2. Make your changes
3. Run tests: `./gradlew test --no-daemon`
4. Commit with descriptive messages
5. Push and create a Pull Request
6. Wait for CI to pass and maintainers to review

## License

This project is licensed under the MIT License.
