/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.gerbsen.map.MapUtil;
import com.github.gerbsen.maven.MavenUtil;
import com.github.gerbsen.rdf.JenaUtil;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultTypeDeterminer implements TypeDeterminer {

    /**
     * 
     */
    protected static final OntModel ontologyModel = JenaUtil.loadModelFromFile(MavenUtil.loadFile("/dbpedia_3.8.owl").getAbsolutePath());
    
    /**
     * avoid those super class
     */
    protected Set<String> uriBlackList = new HashSet<>(Arrays.asList("http://www.w3.org/2000/01/rdf-schema#Resource", "http://dbpedia.org/ontology/Agent", "http://www.w3.org/2002/07/owl#Thing"));
    
    /**
     * 
     * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
     *
     */
    public enum DETERMINER_TYPE {
        
        SUPER_CLASS,
        SUB_CLASS
    }
    
    /**
     * 
     * @param classesToHierarchyDepth
     * @param type
     * @return
     */
    public String getTypeClass(Set<String> urisOfClasses, DETERMINER_TYPE type) {
        
        Map<String, Integer> urisToSteps = new HashMap<>();
        
        for ( String clazz : urisOfClasses ) {
            
            int counter       = 0;
            OntClass ontClass = ontologyModel.getOntClass(clazz); 
            
            while ( ontClass.hasSuperClass() && !uriBlackList.contains(ontClass.getURI()) ) {
                
                counter++;
                ontClass = ontClass.getSuperClass();
            }
            
            if ( !uriBlackList.contains(clazz)) urisToSteps.put(clazz, counter);
        }
        
        List<Entry<String,Integer>> sortedDepths = MapUtil.sortEntiesByValues(urisToSteps);
        
        if ( type.equals(DETERMINER_TYPE.SUPER_CLASS) ) return sortedDepths.get(0).getKey();
        else return sortedDepths.get(sortedDepths.size() - 1).getKey();
    }
}
