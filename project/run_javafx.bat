@echo off
REM Performance Review System - JavaFX Launcher
REM JavaFX SDK is included in the project folder

SET JAVAFX_PATH=%~dp0javafx-sdk-21.0.2\lib
SET SQLITE_JAR=%~dp0lib\sqlite-jdbc-3.49.1.0.jar

echo =======================================
echo  Performance Review System (JavaFX)
echo =======================================
echo.

REM Copy resources to bin
if not exist bin\styles mkdir bin\styles
copy /Y "src\resources\styles\main.css" "bin\styles\main.css" >nul 2>&1

REM Compile the application
echo Compiling...
javac -encoding UTF-8 ^
  --module-path "%JAVAFX_PATH%" ^
  --add-modules javafx.controls,javafx.fxml ^
  -cp "%SQLITE_JAR%" ^
  -d bin ^
  -sourcepath src ^
  src\com\performance\Main.java 2>&1

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo *** COMPILATION FAILED ***
    echo Check the errors above.
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Run the application
echo Starting application...
echo.
java --module-path "%JAVAFX_PATH%" ^
  --add-modules javafx.controls,javafx.fxml ^
  --enable-native-access=javafx.graphics ^
  -cp "bin;%SQLITE_JAR%" ^
  com.performance.Main

pause
