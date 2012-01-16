#!/bin/sh

echo "../nlp2rdf.lod2.eu/schema"
rm -rIv ../nlp2rdf.lod2.eu/schema
cp -r target/schema ../nlp2rdf.lod2.eu
cp target/.htaccess ../nlp2rdf.lod2.eu

echo "../core/generated/src"
rm -rIv ../core/generated/src
cp -r target/src ../core/generated/src




