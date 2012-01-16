#!/bin/sh

######
#This script generates output for one ontology in the folder target/schema


FOLDER=$1
FILE=$2
TARGET=$3

#creates folder in target/schema 
#makes ttl, nt and owl files
mkdir -p "$TARGET/$FOLDER"

TMPSOURCE="$FOLDER/$FILE"".ttl"
TMPTARGET="$TARGET/$FOLDER/$FOLDER"
#cp -v $TMPSOURCE "$TARGET/$FOLDER/$FOLDER"".ttl"
rapper -i turtle $TMPSOURCE -o turtle >  $TMPTARGET".ttl"
rapper -i turtle $TMPSOURCE -o rdfxml >  $TMPTARGET".owl"
rapper -i turtle $TMPSOURCE -o ntriples >  $TMPTARGET".nt"

#make owldoc
mkdir -p "$TARGET"/doc/"$FOLDER"
java -cp utilities/owldoc-cli-0.2/owldoc-cli-0.2.jar  org.coode.html.OntologyExporter "$TARGET/$FOLDER/$FOLDER"".owl"  out="$TARGET/doc/$FOLDER"
find "$TARGET/doc/$FOLDER" -name "*.html" | xargs sed -i 's/src\/main\//..\//g'


