@echo off
echo ================================================================
echo   Building API Gateway & Microservices Platform (Maven)
echo ================================================================

call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed. Check the error log above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ================================================================
echo   BUILD SUCCESSFUL! All 6 modules packaged cleanly.
echo ================================================================
pause
