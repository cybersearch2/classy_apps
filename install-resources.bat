@echo off

echo ** Copy unpacked-libs/as_compatibility-v7-appcompat files to Support Library project
xcopy /S /Y /Q classytreenav\target\unpacked-libs\as_compatibility-v7-appcompat\*.* as_compatibility-v7-appcompat
echo ** Copy library jars to project libs directory
pushd as_compatibility-v7-appcompat
call mvn dependency:copy-dependencies 
if not errorlevel 0 goto error_exit
popd
echo ** Done!
goto :EOF
:error_exit 
popd
echo ** Installation failed
