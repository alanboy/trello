@echo OFF
setlocal

echo =================
echo Publishing started.
echo =================
echo.


SET GeneratedJava=src\main\java\About.java

for /f "delims=\ tokens=1,2" %%i in ('whoami') do set Machine=%%i
for /f "delims=\ tokens=1,2" %%i in ('whoami') do set User=%%j
for /f "delims==" %%i in ('time /t') do set Time=%%i
for /f "delims==" %%i in ('date /t') do set Date=%%i

echo public class About {  > %GeneratedJava%
echo     static final String BuiltByMachine = "%Machine%"; >>  %GeneratedJava%
echo     static final String BuiltByUser = "%User%"; >>  %GeneratedJava%
echo     static final String Date = "%Time%"; >>  %GeneratedJava%
echo     static final String Time = "%Date%"; >>  %GeneratedJava%
echo } >> %GeneratedJava%

type %GeneratedJava%

call gradle build

copy /y  build\libs\* dist\latest
copy /y  build\libs\lib\* dist\latest\lib

