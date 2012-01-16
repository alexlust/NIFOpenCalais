In this readme you can find basic information on how to get started.
For general information see: http://nlp2rdf.org
For more demo and development tips for clients, see: http://nlp2rdf.org/demo-development
 
##Overview
This project has 3 main modules:
core - all the code that's reusable and the generated Java Classes from the ontology
implementation - NIF Components, Wrappers and Webservices (quite large)
ontology - the ontologies as well as scripts and code for maintaining this project (OWL2Java, owldoc-cli)

###Core
The most important class is probably Text2RDF as it contains the basic code on how to create a model and use uriGenerators
Second to that the "generated" module provides classes for the data structures of NIF
It  is important to include this code to make it usable:
```java
static {
   Factory.registerCustomClasses();
}
```
After that the usage of the data in nif is straight forward, when build on Jena:

```java
for (Word w : Word.list(model)) {
   System.out.println(w.getAnchorOf());
   w.addStem(stem(w.getAnchorOf()).toLowerCase());
   if (stopWords.contains(w.getAnchorOf())) {
        StopWord.create(w.getURI(), model);
   }
}
```


##Building
For building Maven2 has to be installed.
Then just type:
mvn clean install 


##Web services
Only the web services in implementation can be executed.
The configuration of the web services can be found in the web.xml files, which are in src/main/webapp/WEB-INF/web.xml

Go to the respective folder such as implementation/demo 
and type "mvn -Djetty.port=8080 jetty:run"
and point your browser to http://localhost:8080/$name/$url-pattern
where $name is the name of the package and $url-pattern is given in the web.xml
For demo this would be:
http://localhost:8080/demo/NIFStanfordCore?input-type=text&input=This+is+a+city+called+Berlin.
http://localhost:8080/demo/NIFStemmer?input-type=text&input=This+is+a+city+called+Berlin.
http://localhost:8080/demo/NIFOpenNLP?input-type=text&input=This+is+a+city+called+Berlin.

Often there is an extra separation between the component and the 
web service so the components do not have any extra dependencies and 
you need to go one deeper to run it:
cd implementation/snowball/snowball-webservice
mvn -Djetty.port=8080 jetty:run
