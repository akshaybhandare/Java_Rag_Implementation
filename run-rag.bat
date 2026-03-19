@echo off
REM Simple RAG Application Runner Script for Windows
REM Provides convenience commands to run the RAG application

set JAR_FILE=target\org.simple.rag-1.0.0-fat.jar

REM Check if JAR exists
if not exist "%JAR_FILE%" (
    echo X Application JAR not found: %JAR_FILE%
    echo.
    echo Please build the application first:
    echo   setup.bat
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Java is not installed
    exit /b 1
)

REM Run the application with provided arguments
java -jar "%JAR_FILE%" %*
