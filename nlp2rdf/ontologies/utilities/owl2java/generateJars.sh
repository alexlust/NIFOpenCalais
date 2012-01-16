#! /bin/sh
mvn clean 
#mvn assembly:single -DdescriptorId=jar-with-dependencies
mvn -Drelease package 
mv target/*.jar .
