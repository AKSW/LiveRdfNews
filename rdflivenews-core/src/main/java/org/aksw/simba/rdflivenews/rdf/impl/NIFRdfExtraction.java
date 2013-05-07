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
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.apache.log4j.Logger;
import virtuoso.jena.driver.VirtGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class NIFRdfExtraction implements RdfExtraction {
    private Logger logger = Logger.getLogger(getClass());


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
    }

    @Override
    public List<Triple> extractRdf(Set<Cluster<Pattern>> clusters) {
        List<Triple> triples = new ArrayList<Triple>();

        for (Cluster<Pattern> cluster : clusters) {
            for (Pattern pattern : cluster) {
                for (EntityPair pair : pattern.getLearnedFromEntities()) {
                    extractRdfFromEntityPair(pair, cluster, pattern);
                }
            }
        }

        try {
            model.write(new FileWriter(output_file), "N3");
        } catch (IOException ioe) {
            System.out.println("couldn't write to " + output_file);
            ioe.printStackTrace();
        }
        return triples;
    }

    public void extractRdfFromEntityPair(EntityPair pair, Cluster<Pattern> cluster, Pattern pattern) {
        if (!pair.hasValidUris()) {
            logger.warn("NON VALID URIS: \n" + pair);
            return;
        }

        if (testing || check(pair, cluster, pattern)) {

            Individual subject = model.createIndividual(pair.getFirstEntity().getUri(), model.createClass(cluster.getRdfsDomain()));
            Individual object = model.createIndividual(pair.getSecondEntity().getUri(), model.createClass(cluster.getRdfsRange()));
            ObjectProperty op = model.createObjectProperty(cluster.getUri());

            //TODO what are refined labels?
            subject.setLabel(pair.getFirstEntity().getLabel(), "en");
            object.setLabel(pair.getSecondEntity().getLabel(), "en");

            //add the connection between subject and object
            subject.addProperty(op, object);


            Set<String[]> extractions = new HashSet<String[]>();
            if (testing) {
                extractions.add(new String[]{"... costs of the Wi-Fi system , '' explains Houston Airports spokesperson Marlene McClinton , `` And charges ...", "http://www.usatoday.com/money/industries/energy/environment/2010-02-03-windpower_N.htm", ""});
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
                    logger.info("contains #: " + sourceUrl);
                    sourceUrl = sourceUrl.substring(0, sourceUrl.indexOf('#'));
                }
                //cut http:// and use it as the context URI
                String prefix = BASE + sourceUrl.substring("http://".length()) + "#";

                //context
                Individual context = model.createIndividual(prefix + "char=0,", NIFOntClasses.RFC5147String.getOntClass(model));
                context.addOntClass(NIFOntClasses.Context.getOntClass(model));
                context.addOntClass(NIFOntClasses.String.getOntClass(model));
                context.addProperty(NIFObjectProperties.referenceContext.getObjectProperty(model), context);
                context.addProperty(NIFDatatypeProperties.isString.getDatatypeProperty(model), text);

                //generate URI for the object and subject
                Individual subjectString = assignURItoString(text, prefix, pair.getFirstEntity().getLabel(), context, model);
                Individual objectString = assignURItoString(text, prefix, pair.getSecondEntity().getLabel(), context, model);

                //TODO get Label for the property
                //Individual subjectString = assignURItoString(text, prefix, pair.getFirstEntity().getLabel(), context, model);
            }
        }
    }


    public static int doubletteCounter = 0;
    public static int totalCounter = 0;
    public static int notFoundCounter = 0;

    private Individual assignURItoString(String text, String prefix, String surfaceform, Individual context, OntModel m) {
        if (text.split(surfaceform).length > 2) {
            logger.error("several occurrence within the same text, happened " + (++doubletteCounter) + " times already: " + context.getURI());
        } else if (!text.contains(surfaceform)) {
            logger.error("no occurrence within the text, happened " + (++notFoundCounter) + " times already: " + context.getURI());
        }
        logger.info("Total: " + (++totalCounter) + " notfound: " + notFoundCounter + " doublettes: " + doubletteCounter);

        int beginIndex = text.indexOf(surfaceform);
        int endIndex = beginIndex + surfaceform.length();
        Individual retval = m.createIndividual(prefix + "char=" + beginIndex + "," + endIndex, NIFOntClasses.RFC5147String.getOntClass(m));
        retval.addOntClass(NIFOntClasses.String.getOntClass(m));

        retval.addProperty(NIFObjectProperties.referenceContext.getObjectProperty(m), context);

        retval.addLiteral(NIFDatatypeProperties.anchorOf.getDatatypeProperty(m), surfaceform);
        retval.addLiteral(NIFDatatypeProperties.beginIndex.getDatatypeProperty(m), beginIndex);
        retval.addLiteral(NIFDatatypeProperties.endIndex.getDatatypeProperty(m), endIndex);
        return retval;
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
