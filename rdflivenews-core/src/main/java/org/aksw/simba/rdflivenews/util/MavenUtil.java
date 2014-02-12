/**
 * 
 */
package org.aksw.simba.rdflivenews.util;

import java.io.File;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class MavenUtil {

    /**
     * 
     * @param path
     * @return
     */
    public static File loadFile(String path) {
        
        return new File(MavenUtil.class.getResource(path).getFile());
    }
}