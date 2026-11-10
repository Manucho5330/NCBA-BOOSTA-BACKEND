@echo off
setlocal
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd"
set "MAVEN_ARCHIVE=%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_BIN%" (
  if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
  echo Downloading Apache Maven %MAVEN_VERSION%...
  curl.exe --fail --location --silent --show-error "https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip" --output "%MAVEN_ARCHIVE%"
  if errorlevel 1 (
    echo Failed to download Maven. Check network access to archive.apache.org.
    exit /b 1
  )
  "%SystemRoot%\System32\tar.exe" -xf "%MAVEN_ARCHIVE%" -C "%MAVEN_HOME%"
  if errorlevel 1 (
    echo Failed to extract Maven. Ensure Windows tar.exe is available.
    exit /b 1
  )
)

call "%MAVEN_BIN%" -f "%~dp0pom.xml" %*
exit /b %ERRORLEVEL%
