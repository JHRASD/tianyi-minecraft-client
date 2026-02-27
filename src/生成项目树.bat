@echo off
chcp 65001 >nul
title Project Structure Generator

set PROJECT_PATH=C:\Users\jhr\Desktop\tianyiclient-template-1.21.8新\src
set OUTPUT_FILE=project_structure_%date:~0,4%-%date:~5,2%-%date:~8,2%.txt

cd /d "%PROJECT_PATH%"

tree /f /a > "%OUTPUT_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Structure saved to:
    echo   %CD%\%OUTPUT_FILE%
    echo.
    echo [NOTE] Using ASCII format for compatibility
    timeout /t 1 /nobreak >nul
    start "" "%OUTPUT_FILE%"
) else (
    echo.
    echo [ERROR] Failed to generate structure
)

pause