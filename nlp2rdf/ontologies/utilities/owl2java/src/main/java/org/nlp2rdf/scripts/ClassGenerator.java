package org.nlp2rdf.scripts;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import nl.tudelft.tbm.eeni.owl2java.JenaGenerator;
import nl.tudelft.tbm.eeni.owlstructure.processor.PropertyRangeSimplifier;

import java.io.File;

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */
class ClassGenerator {
    public static String sep = "::";

    public static String USAGE = "This script needs the following arguments:\n" +
            "1. the path the java classes shall be written to, normally this should be target/src/main/java\n" +
            "2. the path where the ontologies are. Ontologies are passed in as a whitespace separated list.\n" +
            " Use java ... \"ont1 ont2\"" +
            "3. the prefixes  separated by "+sep+" as a white space separated list: \n" +
            "\"str"+sep+"http://nlp2rdf.lod2.eu/schema/string/ sso"+sep+"http://nlp2rdf.lod2.eu/schema/sso/\"\n" +
            "4. the target package, e.g. eu.lod2.nlp2rdf.schema\n" +
            "5. the ontology uri of the basic ontology\n";

    public static void main(String[] args) {

        String[] test = new String[]{
                "target/test",
                "../../string/string-v1.0.ttl ../../sso/sso-v1.0.ttl",
                "str|http://nlp2rdf.lod2.eu/schema/string/ sso|http://nlp2rdf.lod2.eu/schema/sso/",
                "eu.lod2.nlp2rdf.schema",
                "http://nlp2rdf.lod2.eu/schema/"
        };
        //args=test;
        try {

            if (args.length != 5) {
                System.out.println(USAGE);
                System.out.println("input was however:");
                for (String s : args) {
                    System.out.println(s);
                }
                System.exit(0);
            }

            String targetpath = args[0];
            String[] ontologies = args[1].split(" ");
            String[] prefixStrings = args[2].split(" ");
            String targetpackage = args[3];
            String ontologyUri = args[4];

            System.out.println("received parameters:");

            System.out.println("targetpath: " + targetpath);
            System.out.println("ontologies: " + args[1]);
            for (String imports : ontologies) {
                System.out.println("\t->importing: " + imports);
            }
            System.out.println("prefixes: " + args[2]);
            for (String prefix : prefixStrings) {
                System.out.println("\tprefix: " + prefix);
                String[] p = prefix.split(sep);
                System.out.println("\t->@prefix " + p[0] + ": <" + p[1] + "> .");
            }
            System.out.println("targetpackage: " + targetpackage);
            System.out.println("ontologyUri: " + ontologyUri);

            new File(targetpath).mkdirs();

            //URI ontologyuri = new File(ont).toURI();

            // System.out.println(ont);
            //System.exit(0);


            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
            System.out.println("createOntology " + ontologyUri);
            Ontology o = ontModel.createOntology(ontologyUri);
            for (String imports : ontologies) {
                System.out.println("adding import " + imports);
                o.addImport(ontModel.createResource(new File(imports).toURI().toString()));
            }
            ontModel.loadImports();
            for (String prefix : prefixStrings) {
                String[] p = prefix.split(sep);
                System.out.println("adding prefix: " + p[0] + " " + p[1]);
                ontModel.setNsPrefix(p[0], p[1]);
            }
            // Simplify the definition of property ranges
            // This is necessary because Owl2Java chokes on complex range
            // definitions (i.e. those containing anonymous classes)
            (new PropertyRangeSimplifier()).process(ontModel);

            // Generate classes that provide access to ontology instances
            JenaGenerator generator = new JenaGenerator();
            generator.generate(ontModel, targetpath, targetpackage);

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println(USAGE);
        }
    }
}
