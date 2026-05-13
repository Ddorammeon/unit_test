@echo off
setlocal

set "JAVA_HOME=C:\Users\ADMIN\.jdks\ms-17.0.16"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JDK 17 not found at %JAVA_HOME%
  exit /b 1
)

java -version
call gradlew.bat test --tests com.do_an.paymentservice.service.PaymentServiceTest
