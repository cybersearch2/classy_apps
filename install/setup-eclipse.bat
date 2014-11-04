@echo off
setlocal
set TYPE=maven
if "%1"=="" goto start_op
set TYPE=classic
:start_op
@echo Set up %TYPE% Eclipse
@echo as_compatibility-v7-appcompat
pushd ..\as_compatibility-v7-appcompat
xcopy /Y eclipse\%TYPE%\project .\.project
popd
@echo classyfy-application 
pushd ..\classyfy\classyfy-application
xcopy /Y eclipse\%TYPE%\project .\.project
xcopy /Y eclipse\%TYPE%\.classpath .
xcopy /Y eclipse\%TYPE%\.factorypath .
popd
@echo classyfy-tests
pushd ..\classyfy\classyfy-tests
xcopy /Y eclipse\%TYPE%\project .\.project
xcopy /Y eclipse\%TYPE%\.classpath .
xcopy /Y eclipse\%TYPE%\.factorypath .
popd
