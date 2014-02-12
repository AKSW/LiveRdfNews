/**
 * 
 */
package org.aksw.simba.rdflivenews.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class MapUtil {

    /**
     * 
     * @param map
     * @return
     */
    public static <K,V extends Comparable<? super V>> List<Map.Entry<K,V>> sortEntiesByValues(Map<K,V> map) {
        
        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>();
        sortedEntries.addAll(map.entrySet());
        
        Collections.sort(sortedEntries, new Comparator<Map.Entry<K,V>>() {
                
            public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
            }
        });
        
        return sortedEntries;
    }
}
