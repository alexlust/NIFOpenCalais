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
import eu.lod2.nlp2rdf.schema.str.*;
import org.nlp2rdf.core.Span;
import org.nlp2rdf.core.URIGenerator;
import org.nlp2rdf.core.util.URIGeneratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.String;
import java.security.InvalidParameterException;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Sebastian Hellmann
 *         This class implements the NIF Offset URI Scheme.
 *         http://nlp2rdf.org/nif-1-0#toc-nif-recipe-offset-based-uris
 *         calling init() is not necessary
 */
public class OffsetBased extends AbstractURIGenerator implements URIGenerator {
    private static Logger log = LoggerFactory.getLogger(OffsetBased.class);


    public static final String identifier = "offset";

    @Override
    public String getRecipeUri() {
        return "http://nlp2rdf.lod2.eu/schema/string/OffsetBasedString";
    }

    @Override
    public void assignRecipeClass(String uri, OntModel model) {
        OffsetBasedString.create(uri, model);
    }

    @Override
    public String makeUri(String base, String text, Span span) {

        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(identifier);
        sb.append("_");
        sb.append(span.getStart());
        sb.append("_");
        sb.append(span.getEnd());
        sb.append("_");
        sb.append(URIGeneratorHelper.getFirstCharacters(span.getCoveredText(text).toString(), firstCharLength));
        log.trace(sb.toString());
        return sb.toString();
    }

    @Override
    public Span getSpanFor(String prefix, String uri, String text) {
        StringTokenizer st = new StringTokenizer(uri.substring(prefix.length()), "_");
        if (!(st.nextToken().equalsIgnoreCase(identifier))) {
            throw new InvalidParameterException("The span could not be recognized correctly: " + uri + " with prefix " + prefix);
        }
        return new Span(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
    }
}
