#!/bin/sh
tigerxmlfolder=$1
outfile=allTigerTriplesMaterializedInference.nt
tmpoutfile=tmp$outfile
echo "outfile is "$outfile
echo "" > $tmpoutfile

x=5
for i in $(ls $tigerxmlfolder) 
do 
file=$tigerxmlfolder/$i
x=`expr $x + 1` 
rapper -o ntriples -g $file >>! $tmpoutfile

if [ "$(($x % 1000)) " -eq 0 ]
then
   echo $x
   java -Xmx512m   -jar /opt/pellet-2.2.2/lib/pellet-cli.jar extract -s "DefaultStatements" $tmpoutfile | rapper -g - -o ntriples http://tiger.nlp2rdf.org >> $outfile
   echo "" > $tmpoutfile
fi

done

