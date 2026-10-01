@echo off
title OrderCraft Full-Stack Launcher
echo ========================================================
echo Launching OrderCraft Full-Stack Application
echo ========================================================
echo 1. Launching Backend on http://localhost:8080...
start "OrderCraft Backend" cmd /k "%~dp0\start-backend.bat"

echo 2. Waiting 5 seconds before starting frontend...
timeout /t 5 /nobreak >nul

echo 3. Launching Frontend on http://localhost:4200...
start "OrderCraft Frontend" cmd /k "%~dp0\start-frontend.bat"

echo ========================================================
echo Both services are spinning up!
echo Frontend will be accessible at: http://localhost:4200
echo Backend API will be live at:    http://localhost:8080/api
echo ========================================================
pause
