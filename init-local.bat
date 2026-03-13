@echo off
setlocal
chcp 65001 >nul
title Campus Trading Local Init

set "SCRIPT_DIR=%~dp0"
set "PS1_PATH=%SCRIPT_DIR%init-local.ps1"

if not exist "%PS1_PATH%" (
  echo [ERROR] init-local.ps1 not found:
  echo         %PS1_PATH%
  echo.
  pause
  exit /b 1
)

echo ============================================================
echo   Campus Trading - Local Environment Initialization
echo ============================================================
echo.

set "DB_HOST=127.0.0.1"
set "DB_PORT=3306"
set "DB_NAME=campus_trading"
set "DB_USER=root"
set "DB_PASSWORD=123456"
set "MYSQL_CMD=mysql"

set /p INPUT_DB_HOST=MySQL Host [127.0.0.1]: 
if not "%INPUT_DB_HOST%"=="" set "DB_HOST=%INPUT_DB_HOST%"

set /p INPUT_DB_PORT=MySQL Port [3306]: 
if not "%INPUT_DB_PORT%"=="" set "DB_PORT=%INPUT_DB_PORT%"

set /p INPUT_DB_NAME=Database Name [campus_trading]: 
if not "%INPUT_DB_NAME%"=="" set "DB_NAME=%INPUT_DB_NAME%"

set /p INPUT_DB_USER=MySQL User [root]: 
if not "%INPUT_DB_USER%"=="" set "DB_USER=%INPUT_DB_USER%"

set /p INPUT_DB_PASSWORD=MySQL Password [123456]: 
if not "%INPUT_DB_PASSWORD%"=="" set "DB_PASSWORD=%INPUT_DB_PASSWORD%"
set /p INPUT_MYSQL_CMD=MySQL Command or full mysql.exe path [mysql]: 
if not "%INPUT_MYSQL_CMD%"=="" set "MYSQL_CMD=%INPUT_MYSQL_CMD%"

echo.
echo [INFO] Running initialization...

powershell -NoProfile -ExecutionPolicy Bypass -File "%PS1_PATH%" ^
  -DbHost "%DB_HOST%" ^
  -DbPort %DB_PORT% ^
  -DbUser "%DB_USER%" ^
  -DbPassword "%DB_PASSWORD%" ^
  -DbName "%DB_NAME%" ^
  -MySqlCmd "%MYSQL_CMD%"

if errorlevel 1 (
  echo.
  echo [ERROR] Initialization failed.
  echo Check messages above, fix the configuration, then run again.
  echo.
  pause
  exit /b 1
)

echo.
echo [OK] Initialization completed.
echo Default admin account: admin / 123456
echo Next step:
echo   1^) Open backend folder
echo   2^) Run: mvn spring-boot:run
echo.
pause
exit /b 0

