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

package org.nlp2rdf.core.impl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import eu.lod2.nlp2rdf.schema.str.ContextHashBasedString;
import org.apache.commons.codec.digest.DigestUtils;
import org.nlp2rdf.core.Span;
import org.nlp2rdf.core.URIGenerator;
import org.nlp2rdf.core.util.URIGeneratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Sebastian Hellmann
 *         <p/>
 *         <p/>
 *         This class implements the NIF Context-Hash URI Scheme.
 *         http://nlp2rdf.org/nif-1-0#toc-nif-recipe-context-hash-based-uris
 *         The initial contextLength is set to 10
 *         <p/>
 *         <p/>
 *         To change this either call init(), which calculates the required minimal contextlength for all uris to be unique for this document.
 *         or
 *         use the constructor
 *         <p/>
 *         There is no reason, why this would be threaded, so it is not threadsafe
 */
public class MD5Based extends AbstractURIGenerator implements URIGenerator {
    private static Logger log = LoggerFactory.getLogger(MD5Based.class);
    public static final String IDENTIFIER = "hash";
    public static final String BRA = "(";
    public static final String KET = ")";
    protected int contextLength = 10;

    public MD5Based() {
        this(10);
    }

    public MD5Based(int contextLength) {
        this.contextLength = contextLength;
    }

    public MD5Based(String text, Set<Span> allSpans) {
        setMinimalContextLength(text, allSpans);
    }

    public MD5Based(String prefix, OntModel model) {
        String delimiter = "_";
        StringTokenizer st = new StringTokenizer(ContextHashBasedString.list(model).get(0).getURI().substring(prefix.length()), delimiter);
        if (!(st.nextToken().equalsIgnoreCase(IDENTIFIER))) {
            throw new InvalidParameterException("The span could not be recognized correctly: " + ContextHashBasedString.list(model).get(0) + " with prefix " + prefix);
        }
        contextLength = Integer.parseInt(st.nextToken());
    }

    @Override
    public String getRecipeUri() {
        return "http://nlp2rdf.lod2.eu/schema/string/ContextHashBasedString";
    }

    @Override
    public void assignRecipeClass(String uri, OntModel model) {
        ContextHashBasedString.create(uri, model);
    }

    @Override
    public String makeUri(String prefix, String text, Span span) {

        //the substring
        String anchoredPart = span.getCoveredText(text).toString();

        StringBuilder message = new StringBuilder();
        //calculate the context boundaries
        message.append(URIGeneratorHelper.getContextBefore(span, text, contextLength));
        message.append(BRA);
        message.append(anchoredPart);
        message.append(KET);
        message.append(URIGeneratorHelper.getContextAfter(span, text, contextLength));

        String digest = DigestUtils.md5Hex(message.toString());
        String firstChars = URIGeneratorHelper.getFirstCharacters(anchoredPart, firstCharLength);
        StringBuilder uri = new StringBuilder();
        uri.append(prefix);
        uri.append(IDENTIFIER).append("_");
        uri.append(contextLength).append("_");
        uri.append(anchoredPart.length()).append("_");
        uri.append(digest).append("_");
        uri.append(firstChars);

        if (log.isTraceEnabled()) {
            log.trace("Text (" + text.length() + " chars): " + text);
            log.trace("Word (" + span.getCoveredText(text).length() + " chars): " + span.getCoveredText(text));
            log.trace("Span: " + span.getStart() + "|" + span.getEnd());
            //log.trace("Before|After: " + before + "|" + after);
            log.trace("Context (" + contextLength + ") before: |" + URIGeneratorHelper.getContextBefore(span, text, contextLength));
            log.trace("Context (" + contextLength + ") after: |" + URIGeneratorHelper.getContextAfter(span, text, contextLength) + "|");
            log.trace("Message: |" + message.toString() + "|");
            log.trace("URI: " + uri.toString());
        }

        return uri.toString();
    }


    public void setMinimalContextLength(String text, Set<Span> spans) {
        Monitor mon = MonitorFactory.getTimeMonitor(this.getClass().getSimpleName() + "init").start();
        repeat(text, spans);
        log.info("Minimal context calculated: " + contextLength + " needed: " + mon.stop().getLastValue() + " ms. ");
    }

    private void repeat(String text, Set<Span> allSpans) {
        Set<String> collision = new HashSet<String>();
        for (Span span : allSpans) {
            if (false == collision.add(makeUri("", text, span))) {
                contextLength++;
                repeat(text, allSpans);
                return;
            }
        }
    }


    @Override
    public Span getSpanFor(String prefix, String uri, String text) {
        String delimiter = "_";
        StringTokenizer st = new StringTokenizer(uri.substring(prefix.length()), delimiter);
        if (!(st.nextToken().equalsIgnoreCase(IDENTIFIER))) {
            throw new InvalidParameterException("The span could not be recognized correctly: " + uri + " with prefix " + prefix);
        }

        int contextLength = Integer.parseInt(st.nextToken());
        int anchoredPartLength = Integer.parseInt(st.nextToken());
        String digest = st.nextToken();

        StringBuilder humanReadablePart = new StringBuilder();
        while (st.hasMoreTokens()) {
            humanReadablePart.append(st.nextToken());
            //test if the string might have "_" in the human readable part
            if (st.hasMoreTokens()) {
                humanReadablePart.append(delimiter);
            }

        }

        int offset = 0;
        int index;
        while ((index = text.indexOf(humanReadablePart.toString(), offset)) != -1) {
            StringBuilder message = new StringBuilder();

            Span spanCandidate = new Span(index, index + anchoredPartLength);
            //calculate the context boundaries
            message.append(URIGeneratorHelper.getContextBefore(spanCandidate, text, contextLength));
            message.append(BRA);
            message.append(spanCandidate.getCoveredText(text));
            message.append(KET);
            message.append(URIGeneratorHelper.getContextAfter(spanCandidate, text, contextLength));

            String digestNew = DigestUtils.md5Hex(message.toString());
            if (digest.equals(digestNew)) {
                return spanCandidate;
            } else {
                //try the next one
                offset = index;
            }
        }
        throw new RuntimeException("No matching string has been found in text");
    }

    public int getContextLength() {
        return contextLength;
    }

    public void setContextLength(int contextLength) {
        this.contextLength = contextLength;
    }


    /*
   // the uri has been used in the same text already
   if (false == collision.add(uri)) {
    // if the context covers the whole text there is no sense in expanding anything
    if (before == 0 && after == text.length()) {
        log.warn("A non-unique String URI was discovered: " + uri + ". Anchored part was: " + anchoredPart + ". This normally only happens, because the code calling this object uses the same parameters for a second time.");
        return uri;

    } else {
        //make the context bigger, this will guarantee uniqueness
        contextLength++;
        throw new StartOverException("found a duplicate URI (hash collision), increasing context to: " + contextLength);
    }
   } */

}
