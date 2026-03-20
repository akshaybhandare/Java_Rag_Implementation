@echo off
REM Simple RAG Application Runner
set JAR=./org.simple.rag-0.0.1.jar
if not exist "%JAR%" (echo Error: JAR not found & exit /b 1)
java -jar "%JAR%" %*
