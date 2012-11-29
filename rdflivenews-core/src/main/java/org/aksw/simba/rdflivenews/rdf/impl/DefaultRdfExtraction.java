/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.impl;

import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;

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
public class DefaultRdfExtraction implements RdfExtraction {

    @Override
    public void extractRdf(Set<Cluster<Pattern>> clusters) {

        OntModel model = ModelFactory.createOntologyModel();
        
        OntClass person         = model.createClass(Constants.RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "Person");
        person.addEquivalentClass(model.createResource("http://dbpedia.org/ontology/Person"));
        
        OntClass organisation   = model.createClass(Constants.RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "Organization");
        organisation.addEquivalentClass(model.createResource("http://dbpedia.org/ontology/Organization"));
        
        OntClass place          = model.createClass(Constants.RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "Place");
        place.addEquivalentClass(model.createResource("http://dbpedia.org/ontology/Place"));
        
        for ( Cluster<Pattern> cluster : clusters) {
            
            // we need to process quotations differently
            if ( !cluster.getName().matches("") ) {
                
                OntProperty property = model.createObjectProperty(cluster.getUri());
                if ( !cluster.getRdfsDomain().isEmpty() ) property.setDomain(model.createClass(cluster.getRdfsDomain()));
                if ( !cluster.getRdfsRange().isEmpty() ) property.setRange(model.createClass(cluster.getRdfsRange()));
                property.setLabel(cluster.getName(), "en");

                for ( Pattern pattern : cluster ) {
                    for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
                        if ( pair.hasValidUris() ) {

                            if ( pair.getFirstEntity().getType().equals(cluster.getRdfsDomain()) && 
                                    pair.getSecondEntity().getType().equals(cluster.getRdfsRange())) {
                                
                                OntResource domain = model.createOntResource(pair.getFirstEntity().getUri());
                                OntResource range = model.createOntResource(pair.getSecondEntity().getUri());
                                
                                model.add(domain, property, range);
                            }
                            else {
                                
//                                System.out.println("WRONG D/R: " + pattern.getNaturalLanguageRepresentationWithTags());
                            }
                        }
                        else {
                            
                            System.out.println("NON VALID URIS: \n" + pattern);
                        }
                    }
                }
            }
            // this is only for quotes
            else {
                
            }
        }
        
        String fileName = RdfLiveNews.DATA_DIRECTORY + "rdf/iter-#" + RdfLiveNews.ITERATION + "-";
        fileName += RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
        fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") + ".ttl";
        JenaUtil.writeModelToFile(fileName, "TURTLE", "http://rdflivenews.org", model);
        JenaUtil.writeModelToRemoteStore();
    }
}
