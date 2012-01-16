/***************************************************************************/
/*  Copyright (C) 2010-2011, Sebastian Hellmann                            */
/*  Note: If you need parts of NLP2RDF in another licence due to licence   */
/*  incompatibility, please mail hellmann@informatik.uni-leipzig.de        */
/*                                                                         */
/*  This file is part of NLP2RDF.                                          */
/*                                                                         */
/*  NLP2RDF is free software; you can redistribute it and/or modify        */
/*  it under the terms of the GNU General Public License as published by   */
/*  the Free Software Foundation; either version 3 of the License, or      */
/*  (at your option) any later version.                                    */
/*                                                                         */
/*  NLP2RDF is distributed in the hope that it will be useful,             */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           */
/*  GNU General Public License for more details.                           */
/*                                                                         */
/*  You should have received a copy of the GNU General Public License      */
/*  along with this program. If not, see <http://www.gnu.org/licenses/>.   */
/***************************************************************************/

package org.nlp2rdf.implementation.lexo;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.lod2.nlp2rdf.schema.IThing;
import eu.lod2.nlp2rdf.schema.sso.IPhrase;
import eu.lod2.nlp2rdf.schema.sso.IWord;
import eu.lod2.nlp2rdf.schema.sso.Phrase;
import eu.lod2.nlp2rdf.schema.sso.Word;
import eu.lod2.nlp2rdf.schema.str.IOffsetBasedString;
import eu.lod2.nlp2rdf.schema.str.IString;
import eu.lod2.nlp2rdf.schema.tools.Factory;
import org.mindswap.pellet.Individual;
import org.nlp2rdf.core.impl.OffsetBased;
import org.nlp2rdf.core.util.URIComparator;
import org.nlp2rdf.implementation.stanfordcore.StanfordCoreNLPWrapper;
import org.nlp2rdf.ontology.olia.OLiAManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * @author
 */

public class LExOAdditions {
    private static Logger log = LoggerFactory.getLogger(LExOAdditions.class);

    private String lexoprefix = "http://lexo.org/ontology/";
    private String normUriString = "http://nlp2rdf.lod2.eu/schema/sso/normUri";

    private Set<String> filterLemma = new HashSet<String>(Arrays.asList(new String[]{"the", "a", ",", "an"}));

    private Set<String> filterClasses = new HashSet<String>(Arrays
            .asList(new String[]{}));

    static {
        /***************************
         * Important requirement...
         */
        Factory.registerCustomClasses();
    }

    StanfordCoreNLPWrapper scw;

    public LExOAdditions(OLiAManager oLiAManager) {
        scw = new StanfordCoreNLPWrapper(oLiAManager);
    }

    public static void main(String[] args) {
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        new LExOAdditions(new OLiAManager()).processText("http://test/test/", "This is a sentence. ", "offset", m);
        System.out.println(m);
    }

    public void processText(String prefix, String text, String urirecipe, OntModel diff) {
        scw.processText(prefix, text, urirecipe, diff);

        ObjectProperty normUri = diff.createObjectProperty(normUriString);
        for (IWord w : Word.list(diff)) {
            standardize(w, normUri);
        }
        for (IPhrase p : Phrase.list(diff)) {
            standardize(p, normUri, prefix, text, diff);
        }
    }
    /* Map<String, String> changes = new HashMap<String, String>();
 OntModel documentModel = d.getModel();

 OntClass tokenClass = documentModel.getOntClass(BackboneVocabulary.tokenClass);

 Property lemmaProperty = documentModel.getProperty(BackboneVocabulary.hasLemmaProperty);

 // first all individuals selected by selectionClass:
 Set<Individual> allTokens = documentModel.listIndividuals(tokenClass).toSet();

 int count = 0;

 for (Individual token : allTokens) {
     Monitor wmon = MonitorFactory.getTimeMonitor("words");
     Monitor pmon = MonitorFactory.getTimeMonitor("phrase");
     Progress.toLog("Processing token\nwords: " + Time.neededMs(wmon.getTotal()) + "\nphrases: "
             + Time.neededMs(pmon.getTotal()), count++, allTokens.size(), 50);

     if (TokenHelper.isWord(token)) {
         wmon.start();
         standardizeWord(lemmaProperty, token, changes);
         wmon.stop();
     } else if (TokenHelper.isPhrase(token)) {
         pmon.start();
         standardizePhrase(lemmaProperty, token, changes);
         pmon.stop();
     } else if (TokenHelper.isSentence(token)) {
         //no standardization
     }
 }

 for (String change : changes.keySet()) {
     addStandardizedUri(documentModel, change, changes.get(change));
 }
 return null;  */


    // creates the triple for the norm URI
    private void standardize(IWord word, ObjectProperty op) {
        List<String> lemmas = word.listLemma();
        if (lemmas.size() == 0) {
            log.warn("no lemma found for " + word.getURI());
        } else {
            String lemmaString = lemmas.get(0);
            String uri = lexoprefix + normalizeLemma(lemmaString);
            word.addProperty(op, uri);
            log.info("assigning standard URI(" + uri + ") to Word " + word.getURI());
        }
    }


    private void standardize(IPhrase phrase, ObjectProperty op, String prefix, String text, OntModel diff) {
        //System.out.println(phrase.listProperties().toList());

        //queried is used to test whether the transitive closure has been reached, especially refelxive properties are problematic.
        Set<IThing> queried = new HashSet<IThing>();
        Set<IThing> current = new HashSet<IThing>();
        //TODO FIXME only works for offsetbased
        TreeSet<IWord> wordsInOrder = new TreeSet<IWord>(new URIComparator(prefix, text, new OffsetBased()));

        current.addAll(phrase.listChild());
        current.addAll(phrase.listSubString());
        queried.add(phrase);


        boolean repeat = true;
        while (repeat) {
            repeat = false;

            Set<IThing> next = new HashSet<IThing>();
            for (IThing i : current) {
                if (queried.contains(i)) {
                    //do nothing
                } else if (diff.getIndividual(i.getURI()).canAs(Word.class)) {
                    Word w = Word.get(i.getURI(), diff);
                    next.addAll(w.listSubString());
                    //repeat = (repeat || queried.addAll(next));
                    repeat = true;
                    wordsInOrder.add(w);
                } else if (diff.getIndividual(i.getURI()).canAs(Phrase.class)) {
                    Phrase p = Phrase.get(i.getURI(), diff);
                    next.addAll(p.listSubString());
                    next.addAll(p.listChild());
                    repeat = true;
                    //repeat = (repeat || queried.addAll(next));
                }
                queried.add(i);
                current = next;
            }
        }


        System.out.println("in Order " + wordsInOrder);


        //filter stuff
        //filterPassiveAuxilliary(wordsInOrder);

        StringBuilder uri = new StringBuilder(prefix);
        uri.append("lexo");
        boolean minimumOfOne = false;


        for (Iterator<IWord> it = wordsInOrder.iterator(); it.hasNext(); ) {
            IWord w = it.next();
            List<String> lemmas = w.listLemma();
            if (!lemmas.isEmpty()) {
                String lemmaString = lemmas.get(0);
                uri.append("_");
                uri.append(lemmaString);
                minimumOfOne = true;
            }
        }

        if (minimumOfOne) {
            log.info("assigning standard URI(" + uri.toString() + ") to Phrase " + phrase.getURI());
        } else {
            log.error("no lemma found for any word of phrase: " + phrase);
        }
    }


    // allow the lemma to appear in the URL
    private String normalizeLemma(String lemma) {
        try {
            return URLEncoder.encode(lemma.trim().toLowerCase(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    /*private void addStandardizedUri(OntModel m, String stringUri, String standardizedUri) {

        Individual token = m.getIndividual(stringUri);
        ObjectProperty standardizedUriProperty = m.createObjectProperty(normUriString);

        Individual normalized = m.createIndividual(standardizedUri, OWL.Thing);
        token.addProperty(standardizedUriProperty, normalized);

    } */

    /**
     * @return null, if the word should be filtered out
     */
    /*public String getLemma(ISentence s) {

        // get the lemma for this token
        String lemma = null;

        //
        try {
            lemma = word.getProperty(lemmaProperty).getLiteral().getLexicalForm();

            String lemmaString = (lemma.toLowerCase().trim());

            // FIlter out certain lemmas
            if (filterLemmas.contains(lemmaString)) {
                logger.trace("filtered out: " + lemmaString);
                lemma = null;
            } else if (lemmaString.length() == 0) {
                logger.warn("no lemma found for token " + word);
                lemma = null;
            }

        } catch (Exception e) {
            logger.warn("no lemma found for token " + word);
            e.printStackTrace();
            lemma = null;
        }
        return lemma;
    } */


}


