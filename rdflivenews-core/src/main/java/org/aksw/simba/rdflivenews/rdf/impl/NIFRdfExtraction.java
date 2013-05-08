/**
 *
 */
package org.aksw.simba.rdflivenews.rdf.impl;

import com.github.gerbsen.rdf.JenaUtil;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import virtuoso.jena.driver.VirtGraph;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class NIFRdfExtraction implements RdfExtraction {
    private Logger logger = Logger.getLogger(getClass());


    public static final String BASE = "http://rdflivenews.aksw.org/extraction/";


    public final String output_file;
    public boolean testing = false;

    public Map<String, OntModel> source2ModelMap = new HashMap<String, OntModel>();

    private static Map<String, String> prefixes = new HashMap<String, String>();

    public NIFRdfExtraction() {

        output_file = (RdfLiveNews.DATA_DIRECTORY != null) ? RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl" : "normal.ttl";


    }

    public static int errorCount = 0;
    public static int totalPairs = 0;

    @Override
    public List<Triple> extractRdf(Set<Cluster<Pattern>> clusters) {
        List<Triple> triples = new ArrayList<Triple>();

        for (Cluster<Pattern> cluster : clusters) {
            for (Pattern pattern : cluster) {
                for (EntityPair pair : pattern.getLearnedFromEntities()) {
                    totalPairs++;
                    try {
                        extractRdfFromEntityPair(pair, cluster, pattern);
                    } catch (Exception e) {
                        logger.error("An error (" + (++errorCount) + " of " + totalPairs + ") occurred, continuing", e);
                        System.out.println("An error (" + (++errorCount) + " of " + totalPairs + ") occurred, continuing");
                    }
                }
            }
        }

        OntModel total = ModelFactory.createOntologyModel();
        for (String sourceUrlNoHttp : source2ModelMap.keySet()) {
            OntModel m = source2ModelMap.get(sourceUrlNoHttp);
            total.add(m);
            File f = new File("results/" + sourceUrlNoHttp);
            try {
                if (f.getParent() != null) {
                    new File(f.getParent()).mkdirs();
                }
                m.write(new FileWriter(f), "N3");
            } catch (IOException ioe) {
                logger.error("couldn't write to " + f.toString(), ioe);
            }
        }

        try {
            total.write(new FileWriter(output_file), "N3");
            if (testing) {
                StringWriter sw = new StringWriter();
                total.write(sw, "N3");
                System.out.println(sw.toString());
            }
        } catch (IOException ioe) {
            logger.error("couldn't write to " + output_file, ioe);
        }
        return triples;
    }


    public void extractRdfFromEntityPair(EntityPair pair, Cluster<Pattern> cluster, Pattern pattern) throws UnsupportedEncodingException {
        if (!pair.hasValidUris()) {
            logger.warn("NON VALID URIS: \n" + pair);
            return;
        }

        if (testing || check(pair, cluster, pattern)) {

            Set<String[]> extractions = new HashSet<String[]>();
            if (testing) {
                extractions.add(new String[]{"... costs of the Wi-Fi system , '' explains Houston Airports spokesperson Marlene McClinton , `` And charges ...", "http://www.usatoday.com/money/industries/energy/environment/2010-02-03-windpower_N.htm", "348795349"});
            } else {
                IndexManager.getInstance()
                        .getTextArticleDateAndArticleUrl(pair.getLuceneSentenceIds());
            }

            // extraction
            for (String[] extraction : extractions) {


                //get basic info
                String sentence = extraction[0];
                String sourceUrl = extraction[1];
                if (sourceUrl.contains("#")) {
                    logger.info("contains #: " + sourceUrl);
                    sourceUrl = sourceUrl.substring(0, sourceUrl.indexOf('#'));
                }
                String date = extraction[2];
                String sourceUrlNoHttpWithSentence = sourceUrl.substring("http://".length()) + "/" + URLEncoder.encode(sentence, "UTF-8");

                logger.info(sentence);
                logger.info(sourceUrl);
                logger.info(date);


                //make a model
                OntModel model = ModelFactory.createOntologyModel();
                setPrefixes(model);


                logger.info(cluster.getRdfsDomain());
                logger.info(cluster.getRdfsRange());
                OntClass subjectClass = model.createClass(cluster.getRdfsDomain());
                OntClass objectClass = model.createClass();

                logger.info(pair.getFirstEntity().toString());
                logger.info(pair.getSecondEntity().toString());
                Individual subject = model.createIndividual(pair.getFirstEntity().getUri(), subjectClass);
                Individual object = model.createIndividual(pair.getSecondEntity().getUri(), objectClass);

                ObjectProperty op = model.createObjectProperty(cluster.getUri());

                //assign refined labels if possible
                subject.setLabel((pair.getFirstEntity().getRefinedLabel() == null) ? pair.getFirstEntity().getLabel() : pair.getFirstEntity().getRefinedLabel(), "en");
                object.setLabel((pair.getSecondEntity().getRefinedLabel() == null) ? pair.getSecondEntity().getLabel() : pair.getSecondEntity().getRefinedLabel(), "en");

                //add the connection between subject and object
                subject.addProperty(op, object);


                //use it as the context URI
                String prefix = BASE + sourceUrlNoHttpWithSentence + "#";

                //context
                Individual context = model.createIndividual(prefix + "char=0,", NIFOntClasses.RFC5147String.getOntClass(model));
                context.addOntClass(NIFOntClasses.Context.getOntClass(model));
                context.addOntClass(NIFOntClasses.String.getOntClass(model));
                context.addProperty(NIFObjectProperties.referenceContext.getObjectProperty(model), context);
                context.addProperty(NIFDatatypeProperties.isString.getDatatypeProperty(model), sentence);
                Individual sourceUrlIndividual = model.createIndividual(sourceUrl, OWL.Thing);
                context.addProperty(NIFObjectProperties.sourceUrl.getObjectProperty(model), sourceUrlIndividual);
                try {
                    //TODO
                    sourceUrlIndividual.addProperty(DCTerms.created, new Date(date).toString());
                } catch (Exception exception) {
                    logger.debug("date parsing not working: " + date, exception);
                }

                DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
                DateTime dt = new DateTime();
                XSDDateTime now = new XSDDateTime(Calendar.getInstance());
                context.addProperty(DCTerms.created,  dt.toString(formatter), now.getNarrowedDatatype());
                //String jtdate = "2010-01-01T12:00:00+01:00";
                //System.out.println(parser2.parseDateTime(jtdate));


                //prepare the sentence:
                String propertySurfaceForm = pattern.getNaturalLanguageRepresentation();
                int propertyBeginIndex = sentence.indexOf(propertySurfaceForm);
                int propertyEndIndex = propertyBeginIndex + pattern.getNaturalLanguageRepresentation().length();
                Individual propertyNIFString = assignURI(prefix, propertySurfaceForm, context, model, propertyBeginIndex, propertyEndIndex);


                //generate URI for the object and subject
                int bi = sentence.substring(0, propertyBeginIndex).lastIndexOf(pair.getFirstEntity().getLabel());
                int ei = bi + pair.getFirstEntity().getLabel().length();
                Individual subjectNIFString = assignURI(prefix, pair.getFirstEntity().getLabel(), context, model, bi, ei);


                bi = sentence.substring(propertyBeginIndex).indexOf(pair.getSecondEntity().getLabel());
                ei = bi + pair.getSecondEntity().getLabel().length();
                Individual objectNIFString = assignURI(prefix, pair.getSecondEntity().getLabel(), context, model, bi, ei);

                // connect them
                String itsrdfns = "http://www.w3.org/2005/11/its/rdf#";
                ObjectProperty taIdentRef = model.createObjectProperty(itsrdfns + "taIdentRef");
                AnnotationProperty taClassRef = model.createAnnotationProperty(itsrdfns + "taClassRef");
                AnnotationProperty taPropRef = model.createAnnotationProperty(itsrdfns + "taPropRef");

                subjectNIFString.addProperty(taIdentRef, subject);
                subjectNIFString.addProperty(taClassRef, subjectClass);

                objectNIFString.addProperty(taIdentRef, object);
                objectNIFString.addProperty(taClassRef, objectClass);

                propertyNIFString.addProperty(taPropRef, op);

                if (source2ModelMap.containsKey(sourceUrl)) {
                    source2ModelMap.get(sourceUrlNoHttpWithSentence).add(model);
                } else {
                    source2ModelMap.put(sourceUrlNoHttpWithSentence, model);
                }
            }
        }
    }


    /*
    public static int doubletteCounter = 0;
    public static int totalUriCounter = 0;
    public static int notFoundCounter = 0;

    private Individual assignURItoString(String text, String prefix, String surfaceform, Individual context, OntModel m) {
        if (text.split(surfaceform).length > 2) {
            logger.error("several occurrence within the same text, happened " + (++doubletteCounter) + " times already: " + context.getURI());
        } else if (!text.contains(surfaceform)) {
            logger.error("no occurrence within the text, happened " + (++notFoundCounter) + " times already: " + context.getURI());
        }
        logger.info("Total: " + (++totalUriCounter) + " notfound: " + notFoundCounter + " doublettes: " + doubletteCounter);

        int beginIndex = text.indexOf(surfaceform);
        int endIndex = beginIndex + surfaceform.length();

        return assignURI(prefix, surfaceform, context, m, beginIndex, endIndex);
    }
   */

    private Individual assignURI(String prefix, String surfaceform, Individual context, OntModel m, int beginIndex, int endIndex) {
        Individual retval = m.createIndividual(prefix + "char=" + beginIndex + "," + endIndex, NIFOntClasses.RFC5147String.getOntClass(m));
        retval.addOntClass(NIFOntClasses.String.getOntClass(m));
        retval.addProperty(NIFObjectProperties.referenceContext.getObjectProperty(m), context);
        retval.addLiteral(NIFDatatypeProperties.anchorOf.getDatatypeProperty(m), surfaceform);
        retval.addLiteral(NIFDatatypeProperties.beginIndex.getDatatypeProperty(m), beginIndex);
        retval.addLiteral(NIFDatatypeProperties.endIndex.getDatatypeProperty(m), endIndex);
        return retval;
    }

    private void setPrefixes(OntModel model) {
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("dbpedia", "http://dbpedia.org/resource/");
        model.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
        model.setNsPrefix("rln-ont", "http://rdflivenews.aksw.org/ontology/");
        model.setNsPrefix("rln-res", "http://rdflivenews.aksw.org/resource/");
        model.setNsPrefix("itsrdf", "http://www.w3.org/2005/11/its/rdf#");
        NIFNamespaces.addNifPrefix(model);
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
