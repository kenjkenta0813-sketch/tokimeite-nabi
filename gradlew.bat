@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Gradle is not installed or is not on PATH. Install Android Studio or Gradle 8.x and rerun this command.
    exit /b 1
)

gradle %*
