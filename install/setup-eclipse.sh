#!/bin/sh
#
# This script copies Eclipse ADT classpath file to the relevant Android application projects.
#
# Usage:
#   setup-eclipse.sh
#
echo "Set up Eclipse ADT classpaths"

echo "classyfy-application"
cp adt-classpath ../classyfy/classyfy-application/.classpath
echo "classyfy-tests"
cp adt-classpath ../../classyfy/classyfy-tests/.classpath
echo "android-hello-two-dbs"
cp adt-classpath ../../example/android-hello-two-dbs/.classpath
echo "android-hello-two-dbs-v2"
cp adt-classpath ../../example/android-hello-two-dbs-v2/.classpath
echo "db-upgrade"
cp adt-classpath ../../example/db-upgrade/.classpath