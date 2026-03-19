@echo off
REM Simple RAG Application Setup Script for Windows
REM This script builds the application and prepares it for use

echo ===========================================
echo Simple RAG - Setup Script (Windows)
echo ===========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Maven is not installed
    echo Please install Maven 3.6+ from https://maven.apache.org/
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Java is not installed
    echo Please install Java 17+ from https://openjdk.java.net/
    exit /b 1
)

for /f "tokens=* USEBACKQ" %%F in (`java -version 2^>^&1`) do (
    timeout /t 0 >nul
)

echo OK Java is installed
echo OK Maven is installed
echo.

REM Clean and build
echo Building application...
mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo X Build failed
    exit /b 1
)

echo.
echo ===========================================
echo OK Build complete!
echo ===========================================
echo.
echo Application JAR: target\org.simple.rag-1.0.0-fat.jar
echo.
echo Quick start:
echo   1. run-rag.bat init
echo   2. Place PDFs in: data\documents\
echo   3. run-rag.bat ingest
echo   4. run-rag.bat ask "Your question"
echo.
echo View all commands: run-rag.bat --help
echo.
