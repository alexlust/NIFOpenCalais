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

package eu.lod2.nlp2rdf.schema.topic;

import java.util.List;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;

/**
 * Interface http://nlp2rdf.lod2.eu/schema/topic/Topic
 */

public interface ITopic extends Individual, eu.lod2.nlp2rdf.schema.IThing {

	/**
	 * Domain property OccursIn
	 * with uri http://nlp2rdf.lod2.eu/schema/topic/occursIn
	 */

	public boolean existsOccursIn();

	public boolean hasOccursIn(eu.lod2.nlp2rdf.schema.str.IDocument documentValue);

	public int countOccursIn();

	public Iterator<eu.lod2.nlp2rdf.schema.str.Document> iterateOccursIn();

	public List<eu.lod2.nlp2rdf.schema.str.Document> listOccursIn();

	public void addOccursIn(eu.lod2.nlp2rdf.schema.str.IDocument documentValue);

	public void addAllOccursIn(List<? extends eu.lod2.nlp2rdf.schema.str.IDocument> documentList);

	public void removeOccursIn(eu.lod2.nlp2rdf.schema.str.IDocument documentValue);

	public void removeAllOccursIn();

	/**
	 * Domain property CharacterticLemma
	 * with uri http://nlp2rdf.lod2.eu/schema/topic/characteristicLemma
	 */

	public boolean existsCharacterticLemma();

	public boolean hasCharacterticLemma(java.lang.String stringValue);

	public int countCharacterticLemma();

	public Iterator<java.lang.String> iterateCharacterticLemma();

	public List<java.lang.String> listCharacterticLemma();

	public void addCharacterticLemma(java.lang.String stringValue);

	public void addAllCharacterticLemma(List<java.lang.String> stringList);

	public void removeCharacterticLemma(java.lang.String stringValue);

	public void removeAllCharacterticLemma();

}