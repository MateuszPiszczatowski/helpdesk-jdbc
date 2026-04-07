@echo off
for /f "tokens=2 delims=:" %%a in ('chcp') do set "c=%%a"
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -jar ..\out\artifacts\ProgramowanieObiektoweProjekt_jar\ProgramowanieObiektoweProjekt.jar
chcp %c: =% > nul