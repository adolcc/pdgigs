# PDGIGS - Music Score Management System

A high-performance, Reactive RESTful API designed for musicians to manage PDF music scores. This project demonstrates a professional-grade architecture using **Spring WebFlux**, **MongoDB Atlas**, and **AWS S3**, fully containerized and deployed via **CI/CD pipelines**.

### 🚀 Tech Stack

* **Backend:** Java 21, Spring Boot 3.5.7 (WebFlux - Reactive)
* **Database:** MongoDB Atlas (NoSQL Cloud)
* **Cloud Storage:** AWS S3 (Madrid Region `eu-south-2`)
* **DevOps:** Docker, GitHub Actions (CI/CD)
* **Security:** JWT (JSON Web Tokens) & Spring Security
* **Documentation:** SpringDoc OpenAPI 2.8.1 (Swagger UI)
* **Testing:** JUnit 5, Mockito, TestContainers

### 📋 Key Features

* **Reactive Stack:** End-to-end non-blocking operations for high scalability.
* **Secure Storage:** PDF files are securely stored in AWS S3 with reactive streaming.
* **JWT Security:** Stateless authentication for protected endpoints.
* **Automated Deployment:** CI/CD pipeline that builds, tests, and deploys to AWS EC2 on every push.
* **Full CRUD:** Management of score metadata and physical files.

---

### 🛠️ Local Development Setup

To run this project locally, you will need Docker and your own AWS/Mongo credentials.

#### 1. Environment Variables
Create a `.env` file in the root directory (refer to the table below):

| Variable | Description |
| :--- | :--- |
| `SPRING_DATA_MONGODB_URI` | Your MongoDB connection string. |
| `AWS_ACCESS_KEY_ID` | AWS IAM Access Key. |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM Secret Key. |
| `AWS_REGION` | e.g., `eu-south-2`. |
| `AWS_BUCKET_NAME` | Your S3 Bucket name. |

#### 2. Running with Docker Compose
The project includes a `docker-compose.yml` that sets up the application and a local MongoDB instance for testing:

```bash
# Build and start services
docker-compose up -d
```
📖 API Documentation (Swagger)
Once the application is running, you can explore the API endpoints:

Swagger UI: http://localhost:8080/webjars/swagger-ui/index.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

Note: Public access is enabled for Swagger UI, but API endpoints require a valid JWT token in the Authorization: Bearer <token> header.
___
🏗️ Architecture & CI/CD
This repository follows professional DevOps practices:

CI/CD: GitHub Actions automates the build process and deploys the Dockerized application to an AWS EC2 instance.

Externalized Config: All sensitive data is managed via Environment Variables and GitHub Secrets, keeping the codebase secure.
___
👨‍💻 Author
adolcc - GitHub Profile: https://github.com/adolcc
