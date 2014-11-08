#!/bin/sh
#
# This script copies Eclipse project files configured for Classic or Maven (default) mode to the relevant projects.
#
# Usage:
#   setup-eclipse.sh [classic]
#
export ECLIPSE_TYPE=classic
if [ "x$1" = "x" ]; then 
export ECLIPSE_TYPE=maven
fi
echo "Set up $ECLIPSE_TYPE Eclipse"

echo "as_compatibility-v7-appcompat" 
cd ../as_compatibility-v7-appcompat
cp eclipse/$ECLIPSE_TYPE/project ./.project
echo "classyfy-application"
cd ../classyfy/classyfy-application
cp eclipse/$ECLIPSE_TYPE/project ./.project
cp eclipse/$ECLIPSE_TYPE/.classpath .
cp eclipse/$ECLIPSE_TYPE/.factorypath .
echo "classyfy-tests"
cd ../../classyfy/classyfy-tests
cp eclipse/$ECLIPSE_TYPE/project ./.project
cp eclipse/$ECLIPSE_TYPE/.classpath .
cp eclipse/$ECLIPSE_TYPE/.factorypath .
cd ../../install

