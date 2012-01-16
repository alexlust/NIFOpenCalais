####################################
OWLdoc-cli 0.2 based on OWLDoc v1.1.6-protege-4.1-owldoc
There are still a lot of bugs, I am sure!
http://bis.informatik.uni-leipzig.de/SebastianHellmann
####################################

This is a fork of http://code.google.com/p/ontology-browser/
see: http://code.google.com/p/ontology-browser/issues/detail?id=52
I just made some minor modifications and fixes which I needed for my project http://nlp2rdf.org and I really hope to merge it back some day.

You can generate OWL docs in a javadoc like fashion.

The output of OWLdoc-cli can be seen here:
http://nlp2rdf.lod2.eu/schema/string/
http://nlp2rdf.lod2.eu/schema/sso/

Run:
#delete old folder
rm -rv owldoc
#run the java program , usage see below
java -cp owldoc-cli-0.2.jar  org.coode.html.OntologyExporter test.owl  out=owldoc
#You will have to adjust the path to the default.css:
find owldoc -name "*.html" | xargs sed -i 's/src\/main\//..\//g'

Build:
1. install maven
2. run the following:
mvn install 
cd owlhtml
mvn clean compile assembly:single
(the jar will be in the owlhtml/target folder)

********************************
KNOWN BUGS
********************************
I usually run this line over the generated docs:
find "$GENPATH""doc/string" -name "*.html" | xargs sed -i 's/src\/main\//..\//g'

********************************
USAGE
********************************
java -cp owldoc-cli-0.1.jar  org.coode.html.OntologyExporter test.owl  out=owldoc

"yourOntology.owl"
    a path to an ontology has to be given, only files currently: URI ontLoc = new File(arg).toURI();
"out=owldoc"
    change output folder (default is owldoc)
"-t"
    optionally a parameter "-t" makes mini hierarchies
"-l"
    Rendering labels
"-c"
    Switching ontology summary cloud on
"-v"
    Verbose

