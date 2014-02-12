/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer.DETERMINER_TYPE;
import org.aksw.simba.rdflivenews.pattern.refinement.type.TypeDeterminer;
import org.aksw.simba.rdflivenews.util.JenaUtil;
import org.aksw.simba.rdflivenews.util.MavenUtil;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
    
    public static void main(String[] args) {

        Set<String> uris = new HashSet<String>(Arrays.asList("http://dbpedia.org/ontology/ComicsCharacter", "http://dbpedia.org/ontology/FictionalCharacter", "http://dbpedia.org/ontology/Person"));
        
        TypeDeterminer typer = new DefaultTypeDeterminer();
        System.out.println(typer.getTypeClass(uris, DETERMINER_TYPE.SUPER_CLASS));
        System.out.println(typer.getTypeClass(uris, DETERMINER_TYPE.SUB_CLASS));
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
