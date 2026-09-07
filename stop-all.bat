@echo off
echo ================================================================
echo   Stopping API Gateway & Microservices Platform Instances
echo ================================================================

echo Terminating processes on ports 8761, 8080, 8081, 8082, 8083, 8084...

for %%P in (8761 8080 8081 8082 8083 8084) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%%P" ^| findstr "LISTENING"') do (
        echo Stopping PID %%a listening on port %%P...
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo.
echo All microservice platform processes terminated cleanly.
pause
