#!/bin/bash
echo $1
for i in `find $1 -name "*.java" | grep -v "owl2java"` 
do
echo $i
headache -c headache.cfg -h license-header.txt  $i
done
