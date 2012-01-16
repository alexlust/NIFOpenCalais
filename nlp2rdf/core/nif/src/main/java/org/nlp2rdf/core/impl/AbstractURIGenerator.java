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
import org.nlp2rdf.core.Span;
import org.nlp2rdf.core.URIGenerator;

import java.util.Set;

/**
 * User: @author Sebastian Hellmann
 * Date: 10/13/11
 * Time: 6:15 AM
 */
public abstract class AbstractURIGenerator implements URIGenerator {

    protected int firstCharLength = 20;

    public String makeUri(String prefix, String text, Span span, OntModel model) {
        String uri = makeUri(prefix, text, span);
        assignRecipeClass(uri, model);
        return uri;
    }

    @Override
    public Span getSpanFor(String prefix, String uri, String text) {
        return null;
    }

}
