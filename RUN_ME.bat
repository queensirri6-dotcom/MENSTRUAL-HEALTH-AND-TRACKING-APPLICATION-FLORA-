@echo off
echo ================================================
echo       STUDENT GRADING CALCULATOR - LAUNCHER
echo ================================================
echo.

REM --- Use Java bundled with Android Studio ---
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java from: %JAVA_HOME%
echo.

REM --- Verify Java exists ---
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Java not found at %JAVA_HOME%
    echo Please check your Android Studio installation.
    pause
    exit /b 1
)

echo Java found. Building project...
echo.

REM --- Build the fat JAR ---
call gradlew.bat jar
if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED. Check the error messages above.
    pause
    exit /b 1
)

echo.
echo Build successful! Starting application...
echo ================================================
echo.

REM --- Run the application ---
"%JAVA_HOME%\bin\java.exe" -jar build\libs\GradingCalculator-1.0.0.jar

pause
