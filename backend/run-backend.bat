@echo off
REM Build and run backend (Windows)
cd /d %~dp0
echo Building backend...
mvn -DskipTests package
if ERRORLEVEL 1 (
  echo Build failed.
  exit /b 1
)
echo Running backend jar...
java -jar target/backend-0.0.1-SNAPSHOT.jar