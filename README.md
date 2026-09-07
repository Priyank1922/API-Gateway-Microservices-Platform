# 🌐 API Gateway & Microservices Platform

An enterprise-grade, distributed **API Gateway & Microservices Platform** built with **Java 17+ / Java 25**, **Spring Boot 3.3**, **Spring Cloud Gateway**, **Netflix Eureka Service Discovery**, **Resilience4j**, **MySQL**, and a standalone **Thymeleaf UI Dashboard** with sleek micro-animations and hover effects.

---

## 🏗️ Architecture Overview

```
                                +-------------------------------------------+
                                |  Web Browser / Standalone Thymeleaf UI    |
                                +---------------------+---------------------+
                                                      |
                                          HTTP Requests (Port 8080)
                                                      v
+-------------------------------------------------------------------------------------------------------------+
|                                              SPRING CLOUD API GATEWAY                                       |
|  - Standalone Thymeleaf Management Console (White Theme + Micro-Animations & Hover Effects)                 |
|  - Correlation ID / Distributed Tracing Filter (X-Correlation-Id, X-Response-Time-Millis)                   |
|  - Global HMAC-SHA256 JWT & API Key Authentication Filter                                                   |
|  - Token Bucket Rate Limiting Filter (10 req/s, Burst: 20, HTTP 429 Retry-After)                            |
|  - AI Threat & Anomaly Inspection Filter (SQLi, XSS, Path Traversal, Security Scanners)                     |
|  - Resilience4j Circuit Breakers (Auth, Product, Order, AI with Fallbacks)                                  |
|  - Eureka Service Discovery Load Balancer (lb://...)                                                        |
+----------------------+----------------------+----------------------+----------------------+-----------------+
                       |                      |                      |                      |
               lb://AUTH-SERVICE      lb://PRODUCT-SERVICE   lb://ORDER-SERVICE     lb://AI-ANALYTICS-SERVICE
                       |                      |                      |                      |
+----------------------+   +------------------+   +------------------+   +------------------+   +-------------+
|     Auth Service     |   | Product Service  |   |  Order Service   |   |   AI Analytics   |   |Eureka Server|
|     (Port 8081)      |   |   (Port 8082)    |   |   (Port 8083)    |   |   (Port 8084)    |   |(Port 8761)  |
| - JWT & Roles        |   | - Product CRUD   |   | - Multi-Item     |   | - Anomaly Heuristics|  Service     |
| - User Management    |   | - Inventory Stock|   | - Payment Sim    |   | - Traffic Forecast|   | Registry    |
| - MySQL: auth_db     |   | - MySQL:product_db|  | - MySQL: order_db|   | - MySQL: ai_db   |   |             |
+----------------------+   +------------------+   +------------------+   +------------------+   +-------------+
```

---

## 🚀 Key Features

1. **Request Routing & Load Balancing**:
   - Eureka dynamically routes `lb://AUTH-SERVICE`, `lb://PRODUCT-SERVICE`, `lb://ORDER-SERVICE`, and `lb://AI-ANALYTICS-SERVICE`.
2. **Authentication & Authorization**:
   - HMAC-SHA256 JWT tokens with role-based claims (`ROLE_ADMIN`, `ROLE_DEVELOPER`, `ROLE_USER`).
   - Unique API Key verification (`ak_...`).
3. **Token Bucket Rate Limiting**:
   - Enforces sliding window rate limit per IP/API Key.
   - Responds with `HTTP 429 Too Many Requests`, `Retry-After: 3`, and remaining token count headers.
4. **Resilience4j Circuit Breaker & Retries**:
   - Automatic fallback handling when services are slow or down.
   - Exponential backoff retry policies for transient network errors.
5. **AI Threat & Anomaly Defense**:
   - Deep inspection of query parameters, paths, and headers.
   - Real-time heuristic scoring (0–100) for SQL injection, XSS, Path Traversal, and malicious scanner user-agents.
   - Moving-average linear autoregressive traffic prediction.
6. **Standalone Thymeleaf UI Dashboard**:
   - Minimalist, crisp white theme (`#ffffff` base with soft slate accents).
   - Card elevation hover animations (`transform: translateY(-4px)` with glow shadows).
   - Live API Sandbox with pre-filled test presets.
   - Rate limiting burst test simulator & Circuit breaker fallback monitor.

---

## 🗄️ MySQL Database Setup

Each microservice automatically creates and maintains its schema in MySQL:
- `auth_db`: Users, roles, credentials, API keys
- `product_db`: Products, categories, prices, inventory stock
- `order_db`: Orders, items, payment statuses
- `ai_db`: Security threat logs, traffic metrics

Default database configuration in `application.yml`:
- **Host**: `localhost:3306`
- **Username**: `root`
- **Password**: `root` (or configured via environment variable `DB_PASSWORD`)
- **Connection Param**: `createDatabaseIfNotExist=true` (Auto-creates missing databases on startup)

---

## 💻 How to Build & Run (No Docker/Kubernetes)

### 1. Build All Modules
Run the build script or use standard Maven:
```powershell
.\build.bat
# Or via Maven:
mvn clean package -DskipTests
```

### 2. Start All Microservices
Launch Eureka, Gateway, and all 4 microservices in separate windows:
```powershell
.\start-all.bat
```

### 3. Stop All Microservices
To terminate all running microservice instances:
```powershell
.\stop-all.bat
```

---

## 🌐 Dashboard & Service URLs

| Component | URL | Description |
|---|---|---|
| **API Gateway & Web Dashboard** | `http://localhost:8080/` | Main control center, live telemetry, routes |
| **Interactive API Sandbox** | `http://localhost:8080/sandbox` | Test requests, headers, and responses |
| **Resilience & Rate Limiter Lab** | `http://localhost:8080/resilience` | Trigger 25-request bursts, test fallbacks |
| **AI Threat & Anomaly Radar** | `http://localhost:8080/security` | Inspect payloads, live threat stream |
| **API Documentation** | `http://localhost:8080/docs` | OpenAPI endpoint contracts |
| **Eureka Service Discovery** | `http://localhost:8761/` | Eureka node registry |
| **Auth Service** | `http://localhost:8081/api/auth/health` | Auth microservice health |
| **Product Service** | `http://localhost:8082/api/products/health` | Product microservice health |
| **Order Service** | `http://localhost:8083/api/orders/health` | Order microservice health |
| **AI Analytics Service** | `http://localhost:8084/api/ai/health` | AI microservice health |

---

## 🔑 Pre-Seeded Default Accounts

| Username | Password | Role | Default API Key |
|---|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` | `ak_admin_prod_key_77889900112233` |
| `developer` | `developer123` | `ROLE_DEVELOPER` | `ak_dev_sandbox_key_44556677889900` |
| `demouser` | `demo123` | `ROLE_USER` | `ak_demo_client_key_11223344556677` |
