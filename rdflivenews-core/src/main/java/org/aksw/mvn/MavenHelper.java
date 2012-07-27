/**
 * 
 */
package org.aksw.mvn;

import java.io.File;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class MavenHelper {

    public static File loadFile(String path) {
        
        return new File(MavenHelper.class.getResource(path).getFile());
    }
}