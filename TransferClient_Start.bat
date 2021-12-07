@echo off
:: Version 2.5
::
:: batch file that starts the transfer client application
::
:: ***************************************************************************************************
::
:: If you need to use another JRE than the contained in the zip, 
:: change the path to the java runtime vm to your needs (Variable JAVA_RT_PATH).
:: The JRE version must be at least 1.17.1
::
:: ===================================================================================================

title TransferClient Start Up
setlocal
pushd %cd%

:: change here
set JAVA_RT_PATH=%cd%\bin\jre_17.0.1

set JAVAW="%JAVA_RT_PATH:"=%\bin\javaw.exe"

start /b "" %JAVAW% -jar bin\transferClient.jar

if errorlevel 1 (
  echo.
  echo Error occured at startup. Please call your local support!
  echo.
  pause
  exit /b 1
)
popd
endlocal