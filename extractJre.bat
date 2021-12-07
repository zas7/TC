@echo on
:: 
:: Extracts a minimal JRE from JDK based on the necessary modules
::


"%JAVA_HOME:"=%\bin\jlink.exe" ^
--module-path "%JAVA_HOME:"=%\bin\jmods" ^
--output "%WORKSPACE:"=%\target\jre" ^
--no-header-files ^
--strip-debug ^
--no-man-pages ^
--add-modules java.base,java.desktop,java.xml ^
&& exit %ERRORLEVEL%