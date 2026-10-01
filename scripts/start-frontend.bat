@echo off
title OrderCraft Frontend Portal
echo ========================================================
echo Starting OrderCraft Angular Frontend (Port 4200)...
echo ========================================================
cd /d "%~dp0\..\frontend"
call npm start
pause
