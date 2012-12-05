/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.ObjectPropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;

import com.github.gerbsen.rdf.JenaUtil;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimpleRdfExtraction implements RdfExtraction {

    @Override
    public List<Triple> extractRdf(Set<Cluster<Pattern>> clusters) {

        List<Triple> triples = new ArrayList<>();
        
        for ( Cluster<Pattern> cluster : clusters) {
            
            // we need to process quotations differently
            if ( !cluster.getName().matches("") ) {
                
                for ( Pattern pattern : cluster ) {
                    for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
                        if ( pair.hasValidUris() ) {

//                            if ( pair.getFirstEntity().getType().equals(cluster.getRdfsDomain()) && 
//                                    pair.getSecondEntity().getType().equals(cluster.getRdfsRange())) {

                                Triple t = new ObjectPropertyTriple(pair.getFirstEntity().getLabel(), pair.getFirstEntity().getUri(), 
                                                     pattern.getNaturalLanguageRepresentation(),
                                                     pair.getSecondEntity().getLabel(), pair.getSecondEntity().getUri(),
                                                     pair.getLuceneSentenceIds());
                                
                                triples.add(t);
//                            }
//                            else {
                                
//                                System.out.println("WRONG D/R: " + pattern.getNaturalLanguageRepresentationWithTags());
//                            }
                        }
                        else {
                            
                            System.out.println("NON VALID URIS: \n" + pair);
                        }
                    }
                }
            }
            // this is only for quotes
            else {
                
            }
        }
        
        return triples;
    }
}
