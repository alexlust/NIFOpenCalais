#!/bin/sh

######
#This script generates output in the folder target/schema
#

#the ontologies which are used
ONTOLOGIES=$1
TARGET="target/schema"
FILE="target/.htaccess"

#delete
echo "" > $FILE


echo "# From http://www.w3.org/TR/swbp-vocab-pub/#recipe4 
Options -MultiViews
AddType application/rdf+xml .rdf .owl
RewriteEngine On

##################
# Rewrite rule if *.owl files are requested
##################" >> $FILE

#RewriteRule ^schema/string/string.owl$ - [L]
for i in $ONTOLOGIES
do 
echo "RewriteRule ^schema/$i/$i.owl""$ - [L] " >> $FILE
done

echo "
##################
# Rewrite rule to serve HTML content from the vocabulary URI if requested
##################" >> $FILE
for i in $ONTOLOGIES
do 
echo "RewriteCond %{HTTP_ACCEPT} !application/rdf\+xml.*(text/html|application/xhtml\+xml)
RewriteCond %{HTTP_ACCEPT} text/html [OR]
RewriteCond %{HTTP_ACCEPT} application/xhtml\+xml [OR]
RewriteCond %{HTTP_USER_AGENT} ^Mozilla/.*
RewriteRule ^schema/$i/.*$ /schema/doc/$i/index.html [R=303]
" >> $FILE
done

echo "
##################
# Rewrite rule to serve RDF/XML content if requested
##################">> $FILE
 
for i in $ONTOLOGIES
do 
echo "RewriteCond %{HTTP_ACCEPT} application/rdf\+xml
RewriteRule ^schema/$i/[a-zA-Z]*$ /schema/$i/$i.owl [R=303,L] ">> $FILE
done

echo "
##################
# Choose the default response
# Rewrite rule to serve RDF/XML content by default
##################">> $FILE
for i in $ONTOLOGIES
do 
echo "RewriteRule ^schema/$i/[a-zA-Z]*$ /schema/$i/$i.owl [R=303,L] ">> $FILE
done











