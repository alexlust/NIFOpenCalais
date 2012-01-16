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

package eu.lod2.nlp2rdf.schema.error;

import java.util.List;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;

/**
 * Interface http://nlp2rdf.lod2.eu/schema/error/Error
 */

public interface IError extends Individual, eu.lod2.nlp2rdf.schema.IThing {

	/**
	 * Domain property Fatal
	 * with uri http://nlp2rdf.lod2.eu/schema/error/fatal
	 */

	public boolean existsFatal();

	public boolean hasFatal(java.lang.Boolean booleanValue);

	public java.lang.Boolean getFatal();

	public void setFatal(java.lang.Boolean booleanValue);

	public void removeFatal();

	/**
	 * Domain property Source
	 * with uri http://nlp2rdf.lod2.eu/schema/error/source
	 */

	public boolean existsSource();

	public boolean hasSource(java.lang.String stringValue);

	public int countSource();

	public Iterator<java.lang.String> iterateSource();

	public List<java.lang.String> listSource();

	public void addSource(java.lang.String stringValue);

	public void addAllSource(List<java.lang.String> stringList);

	public void removeSource(java.lang.String stringValue);

	public void removeAllSource();

	/**
	 * Domain property Message
	 * with uri http://nlp2rdf.lod2.eu/schema/error/hasMessage
	 */

	public boolean existsMessage();

	public boolean hasMessage(java.lang.String stringValue);

	public int countMessage();

	public Iterator<java.lang.String> iterateMessage();

	public List<java.lang.String> listMessage();

	public void addMessage(java.lang.String stringValue);

	public void addAllMessage(List<java.lang.String> stringList);

	public void removeMessage(java.lang.String stringValue);

	public void removeAllMessage();

}