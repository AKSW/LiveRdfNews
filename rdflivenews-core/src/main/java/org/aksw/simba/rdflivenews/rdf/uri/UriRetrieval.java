/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.uri;

import java.util.List;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface UriRetrieval {

    public String getUri(String text, List<String> entityLabels);
}
