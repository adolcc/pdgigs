### PDGIGS - Music Score Management System

Reactive RESTful API for managing music scores in PDF format, built with Spring WebFlux and MongoDB.

### 🚀 Technologies

- **Java 21**
- **Spring Boot 3.5.7** (WebFlux - Reactive)
- **MongoDB** (Reactive)
- **Docker & Docker Compose**
- **SpringDoc OpenAPI 2.8.13** (Swagger UI)
- **Maven**
- **Testing: JUnit 5, Mockito, TestContainers**
- **Lombok**

### 📋 Functionalities (Full CRUD)

- ✅ **CREATE**: Upload PDF scores with metadata (title, author, musical style)
- ✅ **READ**: Get and download scores
- ✅ **UPDATE**: Update metadata of existing scores
- ✅ **DELETE**: Delete scores and their associated files

### 🛠️ Prerequisites

- Docker and Docker Compose Installed
- Java 21 (optional, only if you want to run without Docker)
- Maven 3.9+ (optional, only if you want to compile manually)

### 🐳 Running with Docker

### 1. Compile the project

```bash
mvn clean package -DskipTests
```

### 2. Start the services with Docker Compose

```bash
docker-compose up -d
```

This will start:

- **MongoDB** on port `27017`
- **PDGIGS API** on port `8080`

### 3. Verify that the containers are running

```bash
docker-compose ps
```

### 4. View logs

```bash
# Application logs
docker-compose logs -f pdgigs-app

# MongoDB Logs
docker-compose logs -f mongodb
```

### 5. Stop the services

```bash
docker-compose down
```

### 6. Stop and delete volumes (full cleanup)

```bash
docker-compose down -v
```

### 📖 API Documentation (Swagger)

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### 👨‍💻 Autor

**adolcc** - [GitHub](https://github.com/adolcc)
