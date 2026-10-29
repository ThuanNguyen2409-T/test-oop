@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
set "DIR=%~dp0"
cd /d "%DIR%"

if not exist "bin" mkdir "bin"

set "ARGFILE=%DIR%sources_run.txt"
if exist "!ARGFILE!" del /f /q "!ARGFILE!" >nul 2>&1

(for /r "src" %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!"
)) > "!ARGFILE!"

echo [1/2] Dang bien dich ung dung voi JDK 17 UTF-8...
javac --release 17 -encoding UTF-8 -d "bin" @"!ARGFILE!"
set "COMPILE_STATUS=%ERRORLEVEL%"
if exist "!ARGFILE!" del /f /q "!ARGFILE!" >nul 2>&1

if %COMPILE_STATUS% neq 0 (
    echo [LOI] Bien dich that bai voi ma: %COMPILE_STATUS%
    exit /b %COMPILE_STATUS%
)

echo [2/2] Khoi dong chuong trinh...
echo ===============================================================
java -Dfile.encoding=UTF-8 -cp "bin" Main %*
set "RUN_STATUS=%ERRORLEVEL%"
exit /b %RUN_STATUS%
