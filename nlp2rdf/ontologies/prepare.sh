#!/bin/sh
#clean up
rm -r "target"
FOLDER=string
FILE=string-v1.0
SCHEMA="target/schema"
ONTOLOGIES="string sso topic"

STRINGVERSION=string-v1.0
SSOVERSION=sso-v1.0
TOPICVERSION=topic-v0.8
ERRORVERSION=error-v0.1


#################
#this updates the target/schema folder, which will be on nlp2rdf.lod2.eu
#################
./utilities/scripts/generate_ttl_xml_nt_owldoc.sh string $STRINGVERSION   $SCHEMA
./utilities/scripts/generate_ttl_xml_nt_owldoc.sh sso  $SSOVERSION $SCHEMA
./utilities/scripts/generate_ttl_xml_nt_owldoc.sh topic $TOPICVERSION    $SCHEMA
./utilities/scripts/generate_ttl_xml_nt_owldoc.sh error $ERRORVERSION    $SCHEMA

./utilities/scripts/generate_htaccess.sh "string sso topic error" 

###################
#Generates core/generated 
##################
#copy ontologies in classpath
ONTPATH="target/src/main/resources/eu/lod2/nlp2rdf/schema/"
for i in $ONTOLOGIES
do
mkdir -p "$ONTPATH/$i"
cp "target/schema/$i/$i"".owl" "$ONTPATH/$i/"
done

#download and fix olia
TARGETPATH="target/src/main/resources/org/purl/olia/"
##ONTOLOGYURLS="http://purl.org/olia/system.owl http://purl.org/olia/penn.owl http://purl.org/olia/penn-link.rdf http://purl.org/olia/olia-top.owl http://purl.org/olia/olia.owl http://purl.org/olia/stanford-link.rdf http://purl.org/olia/stanford.owl http://nachhalt.sfb632.uni-potsdam.de/owl/penn-syntax.owl http://nachhalt.sfb632.uni-potsdam.de/owl/penn-syntax-link.rdf http://nachhalt.sfb632.uni-potsdam.de/owl/brown-link.rdf http://nachhalt.sfb632.uni-potsdam.de/owl/brown.owl"
mkdir -p $TARGETPATH 
./utilities/scripts/update_olia.sh $TARGETPATH  "http://purl.org/olia/system.owl http://purl.org/olia/penn.owl http://purl.org/olia/penn-link.rdf http://purl.org/olia/olia-top.owl http://purl.org/olia/olia.owl http://purl.org/olia/stanford-link.rdf http://purl.org/olia/stanford.owl http://nachhalt.sfb632.uni-potsdam.de/owl/penn-syntax.owl http://nachhalt.sfb632.uni-potsdam.de/owl/penn-syntax-link.rdf http://nachhalt.sfb632.uni-potsdam.de/owl/brown-link.rdf http://nachhalt.sfb632.uni-potsdam.de/owl/brown.owl"

#updating java classes
VERSIONS="string/$STRINGVERSION"".ttl"" sso/$SSOVERSION"".ttl"" topic/$TOPICVERSION"".ttl"" error/$ERRORVERSION"".ttl"
java -cp utilities/owl2java/owl2java-1.1-SNAPSHOT-jar-with-dependencies.jar:utilities/owl2java/owl2java-1.1-SNAPSHOT.jar org.nlp2rdf.scripts.ClassGenerator "target/src/main/java" "$VERSIONS" "str::http://nlp2rdf.lod2.eu/schema/string/ sso::http://nlp2rdf.lod2.eu/schema/sso/ topic::http://nlp2rdf.lod2.eu/schema/topic/ error::http://nlp2rdf.lod2.eu/schema/error/" "eu.lod2.nlp2rdf.schema" "http://nlp2rdf.lod2.eu/schema/"

cp -r utilities/resources/nl target/src/main/java

rm velocity.log
rm velocity.log.1
