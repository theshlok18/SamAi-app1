@rem Gradle startup script for Windows
@rem
@if "%DEBUG%"=="" @echo off
@rem Set local scope for the variables
setlocal
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

for /f "delims=" %%i in ('where java') do set JAVA_EXE=%%i
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
