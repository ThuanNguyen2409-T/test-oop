@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
set "DIR=%~dp0"
cd /d "%DIR%"

if not exist "bin" mkdir "bin"

set "ARGFILE=%DIR%sources_test.txt"
if exist "!ARGFILE!" del /f /q "!ARGFILE!" >nul 2>&1

(for /r "src" %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!"
)) > "!ARGFILE!"

(for /r "test" %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!"
)) >> "!ARGFILE!"

echo [1/2] Dang bien dich bo kiem thu va ma nguon (JDK 17 UTF-8)...
javac --release 17 -encoding UTF-8 -d "bin" @"!ARGFILE!"
set "COMPILE_STATUS=%ERRORLEVEL%"
if exist "!ARGFILE!" del /f /q "!ARGFILE!" >nul 2>&1

if %COMPILE_STATUS% neq 0 (
    echo [LOI] Bien dich that bai voi ma: %COMPILE_STATUS%
    exit /b %COMPILE_STATUS%
)

echo [2/2] Dang chay bo kiem thu tu dong (java -ea)...
echo ===============================================================
java -ea -Dfile.encoding=UTF-8 -cp "bin" PetCareTestSuite
set "TEST_STATUS=%ERRORLEVEL%"
exit /b %TEST_STATUS%
