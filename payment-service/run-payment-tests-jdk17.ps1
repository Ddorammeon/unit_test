$ErrorActionPreference = "Stop"

$jdk17Home = "C:\Users\ADMIN\.jdks\ms-17.0.16"

if (-not (Test-Path "$jdk17Home\bin\java.exe")) {
    throw "JDK 17 not found at $jdk17Home"
}

$env:JAVA_HOME = $jdk17Home
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
.\gradlew.bat test --tests com.do_an.paymentservice.service.PaymentServiceTest
