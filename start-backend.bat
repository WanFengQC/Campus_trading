@echo off
setlocal
chcp 65001 >nul
title Campus Trading Backend

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%backend"

if not exist "%BACKEND_DIR%\pom.xml" (
  echo [ERROR] backend\pom.xml not found.
  echo.
  pause
  exit /b 1
)

cd /d "%BACKEND_DIR%"

where mvn >nul 2>nul
if %errorlevel%==0 (
  echo [INFO] Starting backend with mvn...
  mvn spring-boot:run
  goto :end
)

if exist "C:\Users\EDY\scoop\apps\maven\3.9.13\bin\mvn.cmd" (
  echo [INFO] mvn not in PATH, using scoop maven...
  call "C:\Users\EDY\scoop\apps\maven\3.9.13\bin\mvn.cmd" spring-boot:run
  goto :end
)

echo [ERROR] Maven command not found.
echo Please install Maven or add mvn to PATH, then retry.

:end
echo.
pause
exit /b 0
