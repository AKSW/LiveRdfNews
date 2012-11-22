/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.uri;

import java.util.List;
import java.util.Map;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface UriRetrieval {

    public Map<String, String> getUris(String text, List<String> entityLabels);
}
