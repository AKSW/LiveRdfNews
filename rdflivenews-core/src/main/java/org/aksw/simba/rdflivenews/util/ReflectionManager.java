/**
 * 
 */
package org.aksw.simba.rdflivenews.util;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ReflectionManager {

    /**
     * 
     * @param className
     * @return
     */
    public static Object newInstance(String className) {
        
        try {
            
            return Class.forName(className).newInstance();
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
            throw new RuntimeException("Error while loading default constructor of: " + className, e);
        }
    }
}
