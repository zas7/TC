@echo on
:: 
:: Extracts a minimal JRE from JDK based on the necessary modules
:: Parameters:
:: 1: JAVA_HOME
:: 2: JENKINS WORKSPACE

if [%1]==[] goto :usage
if [%2]==[] goto :usage

"%~1\bin\jlink.exe" ^
--module-path "%~1\bin\jmods" ^
--output "%~2\target\jre" ^
--no-header-files ^
--strip-debug ^
--no-man-pages ^
--add-modules java.base,java.desktop,java.xml && ^
exit /b %%ERRORLEVEL%%

:usage
echo Call with parameters
echo 1: JAVA_HOME
echo 2: JENKINS WORKSPACE
exit /b 1
