/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.jena;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.chainsaw.Main;

import com.github.gerbsen.maven.MavenUtil;
import com.github.gerbsen.rdf.JenaUtil;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SubclassChecker {
    
    private static Map<String, OntModel> classUriToClassHierarchy = new HashMap<String, OntModel>();
    private static OntModel ontologyModel = null; 
    
    static {
        
        init();
    }

    /**
     * Returns the uris of all super class of classUri except:
     *  
     *  - http://www.w3.org/2000/01/rdf-schema#Resource
     *  - http://www.w3.org/2002/07/owl#Thing 
     * 
     * @param classUri
     * @param language
     * @return
     */
    public static Set<String> getSuperClassUrisForClassUri(String classUri){
        
        OntModel classHierarchie = classUriToClassHierarchy.get(classUri);
        Set<String> superClassUris = new HashSet<String>();
        
        if ( classHierarchie != null ) {
            
            Set<OntClass> classes = classHierarchie.listClasses().toSet();
            
            // try to get labels for the given language
            for ( OntClass clazz : classes) {
                
                if ( !clazz.getURI().equals("http://www.w3.org/2002/07/owl#Thing") && !clazz.getURI().equals("http://www.w3.org/2000/01/rdf-schema#Resource") ) {
                    
                    superClassUris.add(clazz.getURI());
                }
            }
        }
        return superClassUris;
    }
    
    public static boolean hasSubclass(String uri) {
        
        return ontologyModel.getOntResource(uri).asClass().hasSubClass();
    }
    
    public static String getDeepestSubclass(Set<String> urisOfClasses) {
        
        // we dont need to check anything if we have only one class
        if ( urisOfClasses.size() == 1 ) return urisOfClasses.iterator().next();

        Set<String> uriBlackList = new HashSet<>(Arrays.asList("http://www.w3.org/2000/01/rdf-schema#Resource", "http://dbpedia.org/ontology/Agent", "http://www.w3.org/2002/07/owl#Thing")); 
        
        int maximumDepth = -1;
        String deepestSubclass = "";
        
        for ( String clazz : urisOfClasses ) {
            
            OntClass ontClass = ontologyModel.getOntResource(clazz).asClass();
            OntClass superClass = null;
            int level = 0;
            
            do {
                
                superClass = ontClass.getSuperClass();
                
                if ( superClass != null && !uriBlackList.contains(superClass.getURI()) ) {
                    
                    level++;
                    
                    if ( maximumDepth < level ) {
                        
                        maximumDepth = level;
                        deepestSubclass = clazz;
                    }
                    ontClass = superClass;
                }
            }
            while ( superClass != null && !uriBlackList.contains(superClass.getURI()) );
        }
        return deepestSubclass;
    }

    /**
     * 
     */
    private static void init() {
        
        ontologyModel = JenaUtil.loadModelFromFile(MavenUtil.loadFile("/dbpedia_3.8.owl").getAbsolutePath());
        
        for (OntClass cl : ontologyModel.listClasses().toSet()) 
            classUriToClassHierarchy.put(cl.getURI(), new Tree(cl).toModel());
    }
    
    /**
     * A simple Helper Class to convert the hierarchy
     */
    private static class Tree {
        final String uri;
        List<Tree> parents;

        public Tree(OntClass me) {
            this.uri = me.getURI();
            parents = new ArrayList<Tree>();

            Set<OntClass> superClasses = me.listSuperClasses(true).toSet();
            for (OntClass s : superClasses) {
                //this is were complex classes are skipped
                if (s.isAnon()) {
                    continue;
                }
                parents.add(new Tree(s));
            }
        }

        public OntModel toModel() {

            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
            OntClass me = model.createClass(uri);
            for (Tree p : parents) {
                OntClass superClass = model.createClass(p.uri);
                me.addSuperClass(superClass);
                model.add(p.toModel());
            }
            return model;
        }
    }
}
