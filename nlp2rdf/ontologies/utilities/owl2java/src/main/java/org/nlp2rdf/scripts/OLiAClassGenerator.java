package org.nlp2rdf.scripts;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import nl.tudelft.tbm.eeni.owl2java.JenaGenerator;
import nl.tudelft.tbm.eeni.owlstructure.processor.PropertyRangeSimplifier;

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */
class OLiAClassGenerator {
    public static void main(String[] args) {
        try {

            String ont = "http://nachhalt.sfb632.uni-potsdam.de/owl/";
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
            Ontology o = ontModel.createOntology(ont);
            o.addImport(ontModel.createResource(ont + "system.owl"));
            ontModel.loadImports();
            ontModel.setNsPrefix("olia_system", "http://nachhalt.sfb632.uni-potsdam.de/owl/system.owl#");

            // Simplify the definition of property ranges
            // This is necessary because Owl2Java chokes on complex range
            // definitions (i.e. those containing anonymous classes)
            (new PropertyRangeSimplifier()).process(ontModel);

            // Generate classes that provide access to ontology instances
            JenaGenerator generator = new JenaGenerator();
            generator.generate(ontModel, "../nif/src/gen/java", "de.uni-potsdam.sfb632.nachhalt.owl");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
