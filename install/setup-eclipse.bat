@echo off
setlocal
@echo Set up Eclipse ADT classpaths
@echo classyfy-application 
xcopy /Y adt-classpath ..\classyfy\classyfy-application\.classpath
@echo classyfy-tests
xcopy /Y adt-classpath ..\classyfy\classyfy-tests\.classpath
@echo android-hello-two-dbs
xcopy /Y adt-classpath ..\example\android-hello-two-dbs\.classpath
@echo android-hello-two-dbs-v2
xcopy /Y adt-classpath ..\example\android-hello-two-dbs-v2\.classpath
@echo db-upgrade
xcopy /Y adt-classpath ..\example\db-upgrade\.classpath