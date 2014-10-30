#!/bin/sh
#

echo "** Copy unpacked-libs/as_compatibility-v7-appcompat files to Support Library project"
cp -a classytreenav/target/unpacked-libs/as_compatibility-v7-appcompat/* as_compatibility-v7-appcompat
echo "** Copy library jars to project libs directory"
cd as_compatibility-v7-appcompat
mvn dependency:copy-dependencies 
cd ..
echo "** Done!"

