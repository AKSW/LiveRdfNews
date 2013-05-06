/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.uri;

import java.util.List;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface Disambiguation {

	public String getUri(String label, String refinedDabel, String secondEntity, List<String> contextEntities, boolean isSubject);
    public String getUri(String label, String secondEntity);
}