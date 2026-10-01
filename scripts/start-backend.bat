@echo off
title OrderCraft Backend Service
echo ========================================================
echo Starting OrderCraft Spring Boot Backend (Port 8080)...
echo ========================================================
cd /d "%~dp0\..\backend"
call mvnw.cmd spring-boot:run
pause
