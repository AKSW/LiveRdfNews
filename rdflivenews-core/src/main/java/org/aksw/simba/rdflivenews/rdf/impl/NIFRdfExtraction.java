/**
 *
 */
package org.aksw.simba.rdflivenews.rdf.impl;

import com.github.gerbsen.rdf.JenaUtil;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.entity.Entity;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.ObjectPropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import virtuoso.jena.driver.VirtGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NIFRdfExtraction implements RdfExtraction {
    
    public static final String BASE = "http://rdflivenews.aksw.org/extraction/";
    
    public OntModel model = ModelFactory.createOntologyModel();
    public final String output_file;
    public boolean testing = false;
    
    
    private static Map<String, String> prefixes = new HashMap<String, String>();
    
    public NIFRdfExtraction() {
        
        output_file = (RdfLiveNews.DATA_DIRECTORY != null) ? RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl" : "normal.ttl";
        
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("dbpedia", "http://dbpedia.org/resource/");
        model.setNsPrefix("rln-ont", "http://rdflivenews.aksw.org/ontology/");
        model.setNsPrefix("dbpedia", "http://rdflivenews.aksw.org/resource/");
        NIFNamespaces.addNifPrefix(model);
        
        /*  prefixes.put();
         prefixes.put("rdfs", Constants.RDFS_PREFIX);
         prefixes.put("owl", Constants.OWL_PREFIX);
         prefixes.put("rdflivenews-ext",
         Constants.RDF_LIVE_NEWS_EXTRACTION_PREFIX);
         prefixes.put("rdflivenews-res", Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX);
         prefixes.put("rdflivenews-ont", Constants.RDF_LIVE_NEWS_ONTOLOGY_PREFIX);
         prefixes.put("rdflivenews-say",
         Constants.RDF_LIVE_NEWS_EXTRACTION_SAY_PREFIX);
         prefixes.put("dbpedia-owl", Constants.DBPEDIA_ONTOLOGY_PREFIX);
         prefixes.put("dbpedia-res", Constants.DBPEDIA_RESOURCE_PREFIX);
         */
    }
    
    @Override
    public List<Triple> extractRdf(Set<Cluster<Pattern>> clusters) {
        List<Triple> triples = new ArrayList<Triple>();
        
        for (Cluster<Pattern> cluster : clusters) {
            extractRdfNormalCluster(cluster);
            
        }
        
        try {
            model.write(new FileWriter(output_file), "N3");
        } catch (IOException ioe) {
            System.out.println("couldn't write to " + output_file);
            ioe.printStackTrace();
        }
        return triples;
        
    }
    
    public void extractRdfNormalCluster(Cluster<Pattern> cluster) {
        for (Pattern pattern : cluster) {
            for (EntityPair pair : pattern.getLearnedFromEntities()) {
                if (!pair.hasValidUris()) {
                    System.out.println("NON VALID URIS: \n" + pair);
                    continue;
                }
                
                ObjectPropertyTriple t = null;
                
                
                if (testing || check(pair, cluster, pattern)) {
                    
                    
                    Individual subject = model.createIndividual(pair.getFirstEntity().getUri(), model.createClass(cluster.getRdfsDomain()));
                    Individual object = model.createIndividual(pair.getSecondEntity().getUri(), model.createClass(cluster.getRdfsRange()));
                    ObjectProperty op = model.createObjectProperty(cluster.getUri());
                    
                    //TODO refinded labels?
                    subject.setLabel(pair.getFirstEntity().getLabel(), "en");
                    object.setLabel(pair.getSecondEntity().getLabel(), "en");
                    
                    
                    subject.addProperty(op, object);
                    
                    
                    Set<String[]> extractions = new HashSet<String[]>();
                    if (testing) {
                        extractions.add(new String[]{"This is an example sentence!", "http://www.usatoday.com/money/industries/energy/environment/2010-02-03-windpower_N.htm", ""});
                    } else {
                        IndexManager.getInstance()
                        .getTextArticleDateAndArticleUrl(pair.getLuceneSentenceIds());
                    }
                    // extraction
                    for (String[] extraction : extractions) {
                        
                        String text = extraction[0];
                        String sourceUrl = extraction[1];
                        String date = extraction[2];
                        
                        if (sourceUrl.contains("#")) {
                            sourceUrl = sourceUrl.substring(0, sourceUrl.indexOf('#'));
                        }
                        
                        //cut http
                        String sourceUrlNoHttp = sourceUrl.substring("http://".length());
                        
                        String extractionUri = BASE + sourceUrlNoHttp + "#char=0,";
                        Individual context = model.createIndividual(extractionUri, NIFOntClasses.RFC5147String.getOntClass(model));
                        context.addOntClass(NIFOntClasses.Context.getOntClass(model));
                        context.addOntClass(NIFOntClasses.String.getOntClass(model));
                        context.addProperty(NIFObjectProperties.referenceContext.getObjectProperty(model), context);
                        
                        context.addProperty(NIFDatatypeProperties.isString.getDatatypeProperty(model), text);
                        
                        
                        /*try {
                         extractionUri = BASE + MessageDigest.getInstance("MD5").digest(sourceUrl.getBytes()) + "#char=0,";
                         } catch (NoSuchAlgorithmException nsa) {
                         System.out.println("MD5 is wrong");
                         System.exit(0);
                         } */
                        
                        
                    }
                }
            }
        }
    }
    
    private boolean check(EntityPair pair, Cluster<Pattern> cluster, Pattern pattern) {
        
        if (testing) {
            return true;
        }
        
        boolean check = false;
        //enable option
        check = (RdfLiveNews.CONFIG.getBooleanSetting("extraction", "enforceCorrectTypes")) ? check : true;
        //if enabled do the check
        check = (check || pair.getFirstEntity().getType()
                 .equals(cluster.getRdfsDomain())
                 && pair.getSecondEntity().getType()
                 .equals(cluster.getRdfsRange()));
        
        if (!check) {
            System.out.println("WRONG D/R: " + pattern.getNaturalLanguageRepresentationWithTags());
        }
        return check;
    }
    
    
    public static void main(String[] args) {
        
        RdfLiveNews.init();
        EntityPair pair = new EntityPair(new Entity("Houston Airport", "http://dbpedia.org/ontology/Organisation"), new Entity("Marlene McClinton", "http://dbpedia.org/ontology/Person"), 82400);
        pair.getFirstEntity().setUri("http://dbpedia.org/resource/George_Bush_Intercontinental_Airport");
        pair.getSecondEntity().setUri("http://rdflivenews.aksw.org/resource/Marlene_McClinton");
        
        ObjectPropertyTriple t = new ObjectPropertyTriple(pair.getFirstEntity().getLabel(), pair.getFirstEntity().getUri(),
                                                          "spokesperson", pair.getSecondEntity().getLabel(), pair.getSecondEntity().getUri(), pair.getLuceneSentenceIds());
        //
        t.setRefinedSubjectLabel("Houston Airport");
        t.setRefinedObjectLabel("Marlene McClinton");
        t.setPropertyType("http://rdflivenews.aksw.org/ontology/spokesman");
        
    }
    
    
    @Override
    public void uploadRdf() {
        
        String graph = RdfLiveNews.CONFIG.getStringSetting("sparql", "uploadServer");
        String server = RdfLiveNews.CONFIG.getStringSetting("sparql", "username");
        String username = RdfLiveNews.CONFIG.getStringSetting("sparql", "password");
        String password = RdfLiveNews.CONFIG.getStringSetting("sparql", "type");
        
        VirtGraph remoteGraph = new VirtGraph(graph, server, username, password);
        
        OntModel model = JenaUtil.loadModelFromFile(RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl");
        StmtIterator iter = model.listStatements();
        
        while (iter.hasNext()) {
            
            com.hp.hpl.jena.graph.Triple t = iter.next().asTriple();
            remoteGraph.add(t);
        }
    }
}
