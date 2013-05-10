/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.impl;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.rdf.JenaUtil;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.entity.Entity;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.DatatypePropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.ObjectPropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;

import java.util.*;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class SimpleRdfExtraction implements RdfExtraction {

	static int extractionCount = 0;
	static BufferedFileWriter normalTripleWriter = null;
	BufferedFileWriter sayTripleWriter = null;

	private static Map<String, String> prefixes = new HashMap<String, String>();

	public SimpleRdfExtraction() {
		
		normalTripleWriter = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		sayTripleWriter = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);

		prefixes.put("rdf", Constants.RDF_PREFIX);
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
	}
	
	Set<String> tokens = new HashSet<String>(Arrays.asList("asked", "reported", "said", "said that", 
			", '' said", "tells", "noted that", "told", "said of", "said in", "calls", 
			"said ,", "announced", ", told", ", '' says", ", said", "called", "says", "says that", "said on"));

	@Override
	public List<Triple> extractRdf(Set<Cluster<Pattern>> clusters) {

		List<Triple> triples = new ArrayList<Triple>();

		for (Map.Entry<String, String> prefixMapping : this.prefixes.entrySet()) {
			normalTripleWriter.write(String.format("@prefix %s: <%s> .",
					prefixMapping.getKey(), prefixMapping.getValue()));
			sayTripleWriter.write(String.format("@prefix %s: <%s> .",
					prefixMapping.getKey(), prefixMapping.getValue()));
		}
		sayTripleWriter.write("\n");
		normalTripleWriter.write("\n");

		for (Cluster<Pattern> cluster : clusters) {
			// we need to process quotations differently
			for (Pattern pattern : cluster) {
				if (!isSayClusterPattern(pattern)) {
					for (EntityPair pair : pattern.getLearnedFromEntities()) {
						if (pair.hasValidUris()) {

							if (RdfLiveNews.CONFIG.getBooleanSetting("extraction", "enforceCorrectTypes")) {

								if (pair.getFirstEntity().getType()
										.equals(cluster.getRdfsDomain())
										&& pair.getSecondEntity().getType()
												.equals(cluster.getRdfsRange())) {

									ObjectPropertyTriple t = new ObjectPropertyTriple(pair.getFirstEntity().getLabel(), pair.getFirstEntity().getUri(),
											pattern.getNaturalLanguageRepresentation(),
											pair.getSecondEntity().getLabel(), pair.getSecondEntity().getUri(), pair.getLuceneSentenceIds());
									//
									t.setRefinedSubjectLabel(pair.getFirstEntity().getRefinedLabel());
									t.setRefinedObjectLabel(pair.getSecondEntity().getRefinedLabel());
									t.setPropertyType(cluster.getUri());
									t.cluster = cluster;
									//
									printObjectTriple(t);
									triples.add(t);
								}
							}
							else {
								ObjectPropertyTriple t = new ObjectPropertyTriple(pair.getFirstEntity().getLabel(), pair.getFirstEntity().getUri(),
										pattern.getNaturalLanguageRepresentation(),
										pair.getSecondEntity().getLabel(), pair.getSecondEntity().getUri(), pair.getLuceneSentenceIds());
								//
								t.setRefinedSubjectLabel(pair.getFirstEntity().getRefinedLabel());
								t.setRefinedObjectLabel(pair.getSecondEntity().getRefinedLabel());
								t.setPropertyType(cluster.getUri());
								t.cluster = cluster;
								//
								printObjectTriple(t);
								triples.add(t);
							}
						} 
					}
				}
					
//					// this is only for quotes
//					else {
//							for (EntityPair pair : pattern.getLearnedFromEntities()) {
//								if (pair.hasValidUris()) {
//
//									// if (
//									// pair.getFirstEntity().getType().equals(cluster.getRdfsDomain())
//									// &&
//									// pair.getSecondEntity().getType().equals(cluster.getRdfsRange()))
//									// {a
//
//									DatatypePropertyTriple t = new DatatypePropertyTriple(
//											pair.getFirstEntity().getLabel(), pair
//													.getFirstEntity().getUri(),
//											pattern.getNaturalLanguageRepresentation(),
//											pair.getSecondEntity().getLabel(),
//											pair.getLuceneSentenceIds());
//									//
//									t.setRefinedSubjectLabel(pair.getFirstEntity()
//											.getRefinedLabel());
//									t.setPropertyType(cluster.getUri());
//									//
////									printDatatypeTriple(t);
////									triples.add(t);
//									// }
//									// else {
//
//									// System.out.println("WRONG D/R: " +
//									// pattern.getNaturalLanguageRepresentationWithTags());
//									// }
//								} else {
//
////									System.out.println("NON VALID URIS: \n" + pair);
//								}
//							}
//						}
					}
			}
			
//		}
		normalTripleWriter.close();
		sayTripleWriter.close();

		return triples;
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

		printObjectTriple(t);
	}

	private void printDatatypeTriple(DatatypePropertyTriple t) {

		StringBuffer buffer = new StringBuffer();

		String extractionUri = Constants.RDF_LIVE_NEWS_EXTRACTION_PREFIX
				+ this.extractionCount;
		String datatypeObjectUri = Constants.RDF_LIVE_NEWS_EXTRACTION_SAY_PREFIX
				+ this.extractionCount;

		// write the subject uri
		buffer.append(
				String.format("<%s> <%s> <%s> .", extractionUri,
						Constants.RDF_LIVE_NEWS_ONTOLOGY_SUBJECT,
						t.getSubjectUri())).append("\n");
		// write the subject label
		buffer.append(
				String.format("<%s> <%s> \"%s\"@en .", t.getSubjectUri(),
						Constants.RDFS_LABEL, t.getRefinedSubjectLabel()))
				.append("\n");

		// write the property uri
		buffer.append(
				String.format("<%s> <%s> <%s> .", extractionUri,
						Constants.RDF_LIVE_NEWS_ONTOLOGY_PROPERTY,
						t.getPropertyType())).append("\n");

		// write the object uri
		buffer.append(
				String.format("<%s> <%s> <%s> .", extractionUri,
						Constants.RDF_LIVE_NEWS_ONTOLOGY_DATATYPE_OBJECT,
						datatypeObjectUri)).append("\n");

//		for (String[] extraction : IndexManager.getInstance()
//				.getTextArticleDateAndArticleUrl(t.getSentenceId())) {
//
//			// write the object label
//			buffer.append(
//					String.format("<%s> <%s> \"%s\"@en .", datatypeObjectUri,
//							Constants.RDF_LIVE_NEWS_ONTOLOGY_HAS_SOURCE,
//							extraction[0])).append("\n");
//			buffer.append(
//					String.format("<%s> <%s> <%s> .", extractionUri,
//							Constants.RDF_LIVE_NEWS_ONTOLOGY_HAS_SOURCE_URL,
//							extraction[1])).append("\n");
//			buffer.append(
//					String.format("<%s> <%s> \"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .",
//							extractionUri,
//							Constants.RDF_LIVE_NEWS_ONTOLOGY_EXTRACTION_DATE,
//							extraction[2])).append("\n");
//		}

		// every concept in the say sentence except the subject
		for (String mention : t.getMentions()) {

			buffer.append(
					String.format("<%s> <%s> %s .", datatypeObjectUri,
							Constants.RDF_LIVE_NEWS_ONTOLOGY_MENTIONS, mention))
					.append("\n");
		}

		// add article url
		// add article date

		this.extractionCount++;

		// remove the complete uris with prefixes
		String rdf = buffer.toString();
		for (Map.Entry<String, String> prefixMapping : this.prefixes.entrySet())
			rdf = rdf.replace(prefixMapping.getValue(), prefixMapping.getKey()
					+ ":");

		sayTripleWriter.write(rdf);

	}

	private boolean isSayClusterPattern(Pattern pattern) {

		return tokens.contains(pattern.getNaturalLanguageRepresentation());
	}

	private static void printObjectTriple(ObjectPropertyTriple t) {

		StringBuffer buffer = new StringBuffer();

		String extractionUri = Constants.RDF_LIVE_NEWS_EXTRACTION_PREFIX
				+ extractionCount;

		// write the subject uri
		buffer.append(
				String.format("<%s> <%s> <%s> .", extractionUri,
						Constants.RDF_LIVE_NEWS_ONTOLOGY_SUBJECT,
						t.getSubjectUri())).append("\n");
		// write the subject label
		buffer.append(
				String.format("<%s> <%s> \"%s\"@en .", t.getSubjectUri(),
						Constants.RDFS_LABEL, t.getRefinedSubjectLabel()))
				.append("\n");

		// write the property uri
		buffer.append(
				String.format("<%s> <%s> <%s> .", extractionUri,
						Constants.RDF_LIVE_NEWS_ONTOLOGY_PROPERTY,
						t.getPropertyType())).append("\n");

		// write the object uri
		buffer.append(
				String.format("<%s> <%s> <%s> .", extractionUri,
						Constants.RDF_LIVE_NEWS_ONTOLOGY_RESOURCE_OBJECT,
						t.getObject())).append("\n");
		// write the object label
		buffer.append(
				String.format("<%s> <%s> \"%s\"@en .", t.getObject(),
						Constants.RDFS_LABEL, t.getRefinedObjectLabel()))
				.append("\n");

		// add the text, the article url and the extraction date to the
		// extraction
//		for (String[] extraction : IndexManager.getInstance()
//				.getTextArticleDateAndArticleUrl(t.getSentenceId())) {
//
//			// write the object label
//			buffer.append(
//					String.format("<%s> <%s> \"%s\"@en .", extractionUri,
//							Constants.RDF_LIVE_NEWS_ONTOLOGY_FOUND_IN_SENTENCE,
//							extraction[0])).append("\n");
//			buffer.append(
//					String.format("<%s> <%s> <%s> .", extractionUri,
//							Constants.RDF_LIVE_NEWS_ONTOLOGY_HAS_SOURCE_URL,
//							extraction[2])).append("\n");
//			buffer.append(
//					String.format("<%s><%s> \"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .",
//							extractionUri,
//							Constants.RDF_LIVE_NEWS_ONTOLOGY_EXTRACTION_DATE,
//							extraction[1])).append("\n");
//		}

		extractionCount++;

		// remove the complete uris with prefixes
		String rdf = buffer.toString();
		for (Map.Entry<String, String> prefixMapping : prefixes.entrySet())
			rdf = rdf.replace(prefixMapping.getValue(), prefixMapping.getKey()
					+ ":");

		normalTripleWriter.write(rdf);
	}

	@Override
	public void uploadRdf() {
		
		String graph	= RdfLiveNews.CONFIG.getStringSetting("sparql", "uploadServer");
		String server	= RdfLiveNews.CONFIG.getStringSetting("sparql", "username");
		String username = RdfLiveNews.CONFIG.getStringSetting("sparql", "password");
		String password = RdfLiveNews.CONFIG.getStringSetting("sparql", "type");
		
//		VirtGraph remoteGraph = new VirtGraph(graph, server, username, password);
		
//		OntModel model = JenaUtil.loadModelFromFile(RdfLiveNews.DATA_DIRECTORY + "rdf/normal.ttl");
//		StmtIterator iter = model.listStatements();
//		
//		while ( iter.hasNext() ) {
//			
//			com.hp.hpl.jena.graph.Triple t = iter.next().asTriple();
//			remoteGraph.add(t);
//		}
	}
}
