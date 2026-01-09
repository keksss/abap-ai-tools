@echo off
REM Script to download LangChain4j JARs using Maven dependency:get

echo Downloading LangChain4j dependencies...

REM Set the output directory
set LIBDIR=com.keksss.abap.ai.core\lib

REM Download core LangChain4j
call mvn dependency:get -Dartifact=dev.langchain4j:langchain4j:1.0.0-beta1 -Ddest=%LIBDIR%\langchain4j-1.0.0-beta1.jar

REM Download Google AI Gemini integration
call mvn dependency:get -Dartifact=dev.langchain4j:langchain4j-google-ai-gemini:1.0.0-beta1 -Ddest=%LIBDIR%\langchain4j-google-ai-gemini-1.0.0-beta1.jar

echo Download complete!
pause
