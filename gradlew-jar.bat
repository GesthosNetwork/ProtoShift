@echo off
call .\gradlew jar
pause
taskkill /F /IM java.exe
