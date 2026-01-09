@echo off
REM Script to download LangChain4j provider dependencies

echo Downloading LangChain4j provider JARs...

set LIB_DIR=%~dp0lib
set VERSION=1.0.0-beta1

echo Creating temp directory for downloads...
mkdir "%TEMP%\langchain4j-deps" 2>nul

REM Download OpenAI provider
echo Downloading langchain4j-open-ai...
mvn dependency:copy -Dartifact=dev.langchain4j:langchain4j-open-ai:%VERSION% -DoutputDirectory="%LIB_DIR%"

REM Download Anthropic provider
echo Downloading langchain4j-anthropic...
mvn dependency:copy -Dartifact=dev.langchain4j:langchain4j-anthropic:%VERSION% -DoutputDirectory="%LIB_DIR%"

REM Download Ollama provider
echo Downloading langchain4j-ollama...
mvn dependency:copy -Dartifact=dev.langchain4j:langchain4j-ollama:%VERSION% -DoutputDirectory="%LIB_DIR%"

echo.
echo Done! JAR files downloaded to lib directory.
pause
