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

package org.nlp2rdf.core;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import eu.lod2.nlp2rdf.schema.sso.Phrase;
import eu.lod2.nlp2rdf.schema.sso.Sentence;
import eu.lod2.nlp2rdf.schema.sso.Word;
import eu.lod2.nlp2rdf.schema.str.ContextHashBasedString;
import eu.lod2.nlp2rdf.schema.str.Document;
import eu.lod2.nlp2rdf.schema.str.IString;
import eu.lod2.nlp2rdf.schema.str.OffsetBasedString;
import eu.lod2.nlp2rdf.schema.tools.Factory;
import org.mindswap.pellet.Individual;
import org.nlp2rdf.core.impl.MD5Based;
import org.nlp2rdf.core.util.URIComparator;
import org.nlp2rdf.core.util.URIGeneratorHelper;
import org.nlp2rdf.ontology.olia.OLiAOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * @author Sebastian Hellmann
 */
public class Text2RDF {
    private static Logger log = LoggerFactory.getLogger(Text2RDF.class);

    public static final String stringOntologyUrl = "http://nlp2rdf.lod2.eu/schema/string/";
    public static final String structuredSentenceOntologyUrl = "http://nlp2rdf.lod2.eu/schema/sso/";

    static {
        Factory.registerCustomClasses();
    }

    /**
     * @param prefix
     * @param text
     * @param uriGenerator
     * @param model
     * @return
     */
    public Document createDocumentAnnotation(String prefix, String text, URIGenerator uriGenerator, OntModel model) {
        //make the uri and add the class for the URI recipe
        String uri = uriGenerator.makeUri(prefix, text, new Span(0, text.length()), model);
        uriGenerator.assignRecipeClass(uri, model);
        //wrap it in a document
        Document d = Document.create(uri, model);
        d.setSourceString(text);
        return d;
    }

    /**
     * This is a convenience function, which does quite a lot:
     * 1. generates the uri
     * 2. add the recipe class, i.e. OffsetBased or ContexthashBased
     * 3. adds the class which is given in class (must mbe from owl2java)
     * 4. adds the anchorOf annotation
     * Note: all changes are also reflected in model
     * Note: if something goes wrong this method catches all exceptions, logs it and then re throws it as a runtime exception
     *
     * @param cl
     * @param prefix
     * @param text         the whole (reference) text
     * @param span         the span for the annotation
     * @param uriGenerator
     * @param model
     * @param <S>
     * @return the Jena Individual with the Type give in cl
     */
    public <S> S createStringAnnotationForClass(Class<S> cl, String prefix, String text, Span span, URIGenerator uriGenerator, OntModel model) {
        Monitor mon = MonitorFactory.getTimeMonitor("createStringAnnotationForClass");
        mon.start();
        try {

            //1. make the uri and add the class for the URI recipe
            String uri = uriGenerator.makeUri(prefix, text, span, model);
            //2.assign class
            uriGenerator.assignRecipeClass(uri, model);
            Class[] argTypes = new Class[]{String.class, OntModel.class};
            Method create = cl.getDeclaredMethod("create", argTypes);

            Object s = create.invoke(null, uri, model);

            String addressedString = (span.getCoveredText(text).toString());
            if (s instanceof Sentence) {
                ((Sentence) s).setAnchorOf(addressedString);
            } else if (s instanceof Phrase) {
                ((Phrase) s).setAnchorOf(addressedString);
            } else if (s instanceof Word) {
                ((Word) s).setAnchorOf(addressedString);
            } else if (s instanceof OffsetBasedString) {
                ((OffsetBasedString) s).setAnchorOf(addressedString);
            } else if (s instanceof ContextHashBasedString) {
                ((ContextHashBasedString) s).setAnchorOf(addressedString);
            } else {
                String message = "Class was not Word, Phrase or Sentence";
                log.error(message);
                throw new InvalidParameterException(message);
            }
            if (log.isTraceEnabled()) {
                log.trace("Added " + cl.getSimpleName() + " for " + uri);
            }
            return (S) s;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            mon.stop();
        }
    }


    /**
     * adds sso:Sentence and sso:Word to it
     * adds the word property and additionally, though optional, the firstWord and lastWord property
     * <p/>
     * Note that the expected tokenizer matches the opennlp tokenizer well,
     * so instead of forcing a tokenizer to match the interface it might be smarter to rewrite this method.
     *
     * @param prefix
     * @param text
     * @param tokenizedText
     * @param uriGenerator
     * @param document      use null if you want to opt out
     * @param model
     * @return the model filled with NIF
     */
    public OntModel generateNIFModel(String prefix, String text, TreeMap<Span, List<Span>> tokenizedText, URIGenerator uriGenerator, Document document, OntModel model) {
        assert tokenizedText != null && text != null && uriGenerator != null && prefix != null;
        //some stats
        Monitor mon = MonitorFactory.getTimeMonitor("generateBasicNIFModel").start();
        int wordCount = 0;
        try {
            //set basic prefixes
            model.setNsPrefix("sso", structuredSentenceOntologyUrl);
            model.setNsPrefix("str", stringOntologyUrl);
            for (Span sentenceSpan : tokenizedText.descendingKeySet()) {
                Sentence sentence = createStringAnnotationForClass(Sentence.class, prefix, text, sentenceSpan, uriGenerator, model);
                //assign str:substring to document
                if (document != null) {
                    document.addSubString(sentence);
                }


                //detect words
                List<Span> wordSpans = new ArrayList<Span>(tokenizedText.get(sentenceSpan));
                wordCount += wordSpans.size();
                for (int i = 0; i < wordSpans.size(); i++) {
                    Span wordSpan = wordSpans.get(i);
                    // Span absoluteWordSpan = new Span(wordSpan, sentenceSpan.getStart());
                    Word word = createStringAnnotationForClass(Word.class, prefix, text, wordSpan, uriGenerator, model);
                    //add the firstWord property (optional and redundant to sso:word)
                    if (i == 0) {
                        sentence.setFirstWord(word);
                    }
                    //add the lasttWord property (optional and redundant to sso:word)
                    if (i == (wordSpans.size() - 1)) {
                        sentence.setLastWord(word);
                    }
                    //this is important for the str:subStringTrans inference
                    sentence.addWord(word);

                    if (log.isTraceEnabled()) {
                        StringBuilder logging = new StringBuilder();
                        logging.append("\nword: " + wordSpan.getCoveredText(text));
                        logging.append("\nabsolute sentence position [start|end]: " + sentenceSpan.getStart() + "|" + sentenceSpan.getEnd());
                        logging.append("\nabsolute word position [start|end]: " + wordSpan.getStart() + "|" + wordSpan.getEnd());
                        log.trace(logging.toString());
                    }
                }
            }
            return model;
        } finally {
            mon.stop();
            log.debug("Finished creating " + tokenizedText.size() + " sentence with " + wordCount + " words, " + mon.getLastValue() + " ms.) ");
        }
    }


    /*public void addAdditionalProperties(String prefix, String text, TreeMap<Span, Span[]> sentencesAndWords, URIGenerator uriGenerator, OntModel m) {

        List<IString> sentences = new ArrayList<IString>(Sentence.list(m));
        List<Span> spans = URIGeneratorHelper.getSpans(sentences, prefix, text, uriGenerator);
        Collections.sort(spans, new Comparator<Span>() {
            @Override
            public int compare(Span span, Span span1) {
                return span.compareTo(span1);
            }
        });
    } */


    public TreeMap<Span, List<Span>> getTokenization(String prefix, String text, URIGenerator uriGenerator, OntModel model) {
        TreeMap<Span, List<Span>> tokenizedText = new TreeMap<Span, List<Span>>();

        for (Sentence sentence : Sentence.list(model)) {
            Span sentenceSpan = uriGenerator.getSpanFor(prefix, sentence.getURI(), text);
            List<Span> wordSpans = new ArrayList<Span>();
            for (Word word : sentence.listWord()) {
                Span wordSpan = uriGenerator.getSpanFor(prefix, word.getURI(), text);
                wordSpans.add(wordSpan);
            }
            tokenizedText.put(sentenceSpan, wordSpans);
        }
        return tokenizedText;
    }


    public void addNextAndPreviousProperties(String prefix, String text, URIGenerator uriGenerator, OntModel model) {
        Monitor mon = MonitorFactory.getTimeMonitor("addNextAndPreviousProperties").start();
        long previous = model.size();
        List<Sentence> sentences = Sentence.list(model);
        Collections.sort(sentences, new URIComparator(prefix, text, uriGenerator));
        for (int x = 0; x < sentences.size(); x++) {
            Sentence sentence = sentences.get(x);
            List<Word> words = sentence.listWord();
            Collections.sort(sentences, new URIComparator(prefix, text, uriGenerator));
            if (x < sentences.size() - 1) {
                //not the last one
                sentence.setNextSentence(sentences.get(x + 1));
            }

            for (int y = 0; y < words.size(); y++) {
                Word word = words.get(y);
                //not the last one
                if (y < words.size() - 1) {
                    word.setNextWord(words.get(y + 1));
                }
            }
        }

        mon.stop();
        log.debug("Finished addition of next/previous properties " + (model.size() - previous) + " triples added, " + mon.getLastValue() + " ms.)");
    }


    public void addCopyOfOLiAClassesAndHierarchy(OLiAOntology olia, OntModel model) {
        Monitor mon = MonitorFactory.getTimeMonitor("addCopyOfOLiAClassesAndHierarchy").start();
        long previous = model.size();
        List<Word> words = Word.list(model);
        for (Word w : words) {
            List<String> posTags = w.listPosTag();
            if (posTags.size() >= 1) {
                //get first ignore the others
                String posTag = posTags.get(0);
                //adding pos classes from olia and olia-top
                Set<String> classes = olia.getClassURIsForTag(posTag);
                for (String classUri : classes) {
                    log.info("found: " + classUri + " for: " + posTag);
                    OntModel hierarchy = olia.getHierarchy(classUri);
                    for (ExtendedIterator<OntClass> it = hierarchy.listClasses(); it.hasNext(); ) {
                        OntClass oc = it.next();
                        //add the type
                        w.addOntClass(model.createResource(oc.getURI()));
                        //use all classes
                        //if (oc.getURI().startsWith("http://purl.org/olia/olia-top.owl") || oc.getURI().startsWith("http://purl.org/olia/olia.owl")) {
                        //}
                    }
                    //Copy the hierarchy
                    model.add(hierarchy);
                }
            }
            if (posTags.size() > 1) {
                log.warn("several posTags " + posTags + " found for " + w.getURI());
            }
        }
        mon.stop();
        log.debug("Finished addition of OLiA Classes and Hierarchy " + (model.size() - previous) + " triples added, " + mon.getLastValue() + " ms.)");

        log.info("added ");
    }
}
