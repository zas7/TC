:: Version 2.5
::
:: batch file which starts the transfer client application
::
:: ***************************************************************************************************
::
:: Change the path to the java runtime vm to your needs (Variable JAVA_RT_PATH).
:: The version must be at least 1.17.1
::
:: ===================================================================================================

@echo on
title TransferClient Start Up
setlocal
pushd %cd%

:: change here
set JAVA_RT_PATH=%cd%\bin\jre1.8.0_311

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