@echo off
echo =========================================================================
echo   Starting API Gateway & Microservices Platform
echo   (Eureka, Gateway, Auth, Product, Order, AI Analytics)
echo =========================================================================

echo [1/6] Launching Eureka Service Discovery Server on Port 8761...
start "Eureka Server [8761]" cmd /k "cd eureka-server && mvn spring-boot:run"

echo Waiting 8 seconds for Eureka Server to initialize...
timeout /t 8 /nobreak >nul

echo [2/6] Launching Auth & User Service on Port 8081...
start "Auth Service [8081]" cmd /k "cd auth-service && mvn spring-boot:run"

echo [3/6] Launching Product Catalog Service on Port 8082...
start "Product Service [8082]" cmd /k "cd product-service && mvn spring-boot:run"

echo [4/6] Launching Order Processing Service on Port 8083...
start "Order Service [8083]" cmd /k "cd order-service && mvn spring-boot:run"

echo [5/6] Launching AI Analytics & Threat Defense Service on Port 8084...
start "AI Analytics Service [8084]" cmd /k "cd ai-analytics-service && mvn spring-boot:run"

echo Waiting 5 seconds before launching API Gateway...
timeout /t 5 /nobreak >nul

echo [6/6] Launching Spring Cloud API Gateway & Thymeleaf UI on Port 8080...
start "API Gateway [8080]" cmd /k "cd api-gateway && mvn spring-boot:run"

echo.
echo =========================================================================
echo   ALL MICROSERVICES LAUNCHED!
echo.
echo   - Web Dashboard & API Sandbox: http://localhost:8080/
echo   - Eureka Service Discovery UI: http://localhost:8761/
echo   - Auth Microservice:          http://localhost:8081/api/auth/health
echo   - Product Microservice:       http://localhost:8082/api/products/health
echo   - Order Microservice:         http://localhost:8083/api/orders/health
echo   - AI Analytics Microservice:  http://localhost:8084/api/ai/health
echo =========================================================================
pause
