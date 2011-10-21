#!/bin/sh

if [ -n "$1" ]
then
  BRANCH=$1
else
  BRANCH=HEAD
fi

cd qcadoo-maven-plugin
git pull origin master
git checkout $BRANCH
mvn clean install
cd ../qcadoo
git pull origin master
git checkout $BRANCH
mvn clean install
cd ../mes
git pull origin master
git checkout $BRANCH
mvn clean install -Dprofile=tomcat -Ptomcat

exit 0

