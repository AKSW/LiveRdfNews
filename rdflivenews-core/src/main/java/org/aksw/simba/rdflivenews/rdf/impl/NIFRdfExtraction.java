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
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.index.Extraction;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.apache.commons.io.FileUtils;
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
    public final String data_dir;
    public final String output_file;
    public boolean testing = false;

    public Map<String, OntModel> source2ModelMap = new HashMap<String, OntModel>();

    public NIFRdfExtraction() {

        output_file = (RdfLiveNews.DATA_DIRECTORY != null) ? RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl" : "normal.ttl";
        data_dir = (RdfLiveNews.DATA_DIRECTORY != null) ? RdfLiveNews.DATA_DIRECTORY + "rdf/" : "results/";


    }

    public static int errorCount = 0;
    public static int totalPairs = 0;

    private boolean isSayCluster(Cluster<Pattern> cluster) {

        return cluster.getName().startsWith("said")
                || cluster.getName().contains("said");
    }

    @Override
    public List<Triple> extractRdf(Set<Cluster<Pattern>> clusters) {
        List<Triple> triples = new ArrayList<Triple>();

        for (Cluster<Pattern> cluster : clusters) {
            if (!isSayCluster(cluster)) {
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
        }

        OntModel total = ModelFactory.createOntologyModel();
        for (String sourceUrlNoHttp : source2ModelMap.keySet()) {

            OntModel m = source2ModelMap.get(sourceUrlNoHttp);
            total.add(m);
            File f = new File(data_dir + sourceUrlNoHttp);
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
            logger.debug("NON VALID URIS: \n" + pair);
            return;
        }

        if (testing || check(pair, cluster, pattern)) {

            Set<Extraction> extractions = new HashSet<Extraction>();
            if (testing) {

                String url = "http://www.usatoday.com/money/industries/energy/environment/2010-02-03-windpower_N.htm";
                String text = "... costs of the Wi-Fi system , '' explains Houston Airports spokesperson Marlene McClinton , `` And charges ...";
                String date = "1307916000000";

                extractions.add(new Extraction(url, text, date));
            } else {
                extractions = IndexManager.getInstance().getTextArticleDateAndArticleUrl(pair.getLuceneSentenceIds());
            }

            // extraction
            for (Extraction extraction : extractions) {


                //get basic info
                String sourceUrl = extraction.url;
                if (sourceUrl.contains("#")) {
                    logger.info("contains #: " + sourceUrl);
                    sourceUrl = sourceUrl.substring(0, sourceUrl.indexOf('#'));
                }
                String date = extraction.date;
                String sentence = extraction.text;


                String normSent = (sentence.length() > 200) ? sentence.substring(0, 200) : sentence;
                String sourceUrlNoHttpWithSentence = sourceUrl.substring("http://".length()) + "/" + URLEncoder.encode(normSent, "UTF-8");

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

                /*******************************************************************
                 * Finally, we succeed in creating ISO conform dateTime strings!
                 *******************************************************************/
                DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
                XSDDateTime now = new XSDDateTime(Calendar.getInstance());
                try {
                    sourceUrlIndividual.addProperty(DCTerms.created, formatter.print(new Long(date).longValue()), now.getNarrowedDatatype());

                } catch (Exception exception) {
                    logger.debug("date parsing not working: " + date, exception);
                }
                DateTime dt = new DateTime();
                context.addProperty(DCTerms.created, dt.toString(formatter), now.getNarrowedDatatype());


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
            logger.error("WRONG D/R: " + pattern.getNaturalLanguageRepresentationWithTags());
        }
        return check;
    }

    @Override
    public void uploadRdf() {

        String graph = RdfLiveNews.CONFIG.getStringSetting("sparql", "graph");
        String server = RdfLiveNews.CONFIG.getStringSetting("sparql", "uploadServer");
        String username = RdfLiveNews.CONFIG.getStringSetting("sparql", "username");
        String password = RdfLiveNews.CONFIG.getStringSetting("sparql", "password");

        VirtGraph remoteGraph = new VirtGraph(graph, server, username, password);

        for (File file : FileUtils.listFiles(FileUtils.getFile(RdfLiveNews.DATA_DIRECTORY + "rdf/"), new String[]{"n3"}, true)) {

            OntModel model = JenaUtil.loadModelFromFile(file.getAbsolutePath());
            StmtIterator iter = model.listStatements();

            while (iter.hasNext()) {

                com.hp.hpl.jena.graph.Triple t = iter.next().asTriple();
                remoteGraph.add(t);
            }
        }
    }
}
