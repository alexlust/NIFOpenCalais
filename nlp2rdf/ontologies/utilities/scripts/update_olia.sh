#!/bin/sh
######################
# Download Olia Ontologies
######################

TARGETPATH=$1
ONTOLOGYURLS=$2

cd $TARGETPATH

rm *.owl
rm *.rdf

for i in $ONTOLOGYURLS
do
wget $i
done

for i in `ls`
do
sed -i 's/purl.oclc.org/purl.org/g' $i
sed -i 's/nachhalt.sfb632.uni-potsdam.de\/owl/purl.org\/olia/g' $i
done
