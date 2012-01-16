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

import com.hp.hpl.jena.ontology.OntModel;

import java.util.Set;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */
public interface URIGenerator {

    /**
     * Depending on the implementation, it might be necessary to call init before calling this function
     *
     * @param prefix
     * @param text
     * @param span
     * @return
     */
    public String makeUri(String prefix, String text, Span span);

    /**
     * Additionally creates the uri in the model and  assigns the recipe class
     *
     * @param prefix
     * @param text
     * @param span
     * @param model
     * @return
     */
    public String makeUri(String prefix, String text, Span span, OntModel model);

    public Span getSpanFor(String prefix, String uri, String text);

    /**
     * This returns the recipe class form the String ontology:
     * http://nlp2rdf.lod2.eu/schema/string/
     *
     * @return a URI from http://nlp2rdf.lod2.eu/schema/string/
     */
    public String getRecipeUri();

    /**
     * adds an rdf:type statement to model for the respective recipe
     *
     * @param uri
     * @param model
     */
    public void assignRecipeClass(String uri, OntModel model);


}
