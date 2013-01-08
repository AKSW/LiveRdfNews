package org.aksw.simba.rdflivenews;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Constants {

    /**
     * name of the primary key for the index
     */
    public static final String LUCENE_FIELD_ID = "id";
    
    /**
     * use this value as the lucene field name for a pos tagged sentence
     */
    public static final String LUCENE_FIELD_POS_TAGGED_SENTENCE = "pos";
    
    /**
     * use this value as the lucene field name for a ner tagged sentence
     */
    public static final String LUCENE_FIELD_NER_TAGGED_SENTENCE = "ner";

    /**
     * use this value as the lucene field name for article the sentence comes from
     */
    public static final String LUCENE_FIELD_URL = "url";

    /**
     * use this value as the lucene field name for a normal sentence
     */
    public static final String LUCENE_FIELD_TEXT = "text";

    /**
     * use this value as the lucene field name for the sentence time slice (int values from 0 .. n)
     */
    public static final String LUCENE_FIELD_TIME_SLICE = "timeslice";
    
    /**
     * use this value as the lucene field name if a sentence is supposed to be a duplicate in a time slice n
     */
    public static final String LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE = "duplicate";

    /**
     * use this value as the lucene field name for a sentence extractions date
     */
    public static final String LUCENE_FIELD_EXTRACTION_DATE = "date";
    
    /**
     * use this field to search for sentences which are not duplicate
     */
    public static final int NOT_DUPLICATE_SENTENCE = -1;
    
    /* ################################################################ */
    /* ################################################################ */
    /* ################################################################ */
    
    /**
     * this is the lucene field name of the dbpedia index for the rdf:type(s) of a given resource
     */
    public static final String DBPEDIA_LUCENE_FIELD_TYPES = "types";
    
    /**
     * this is the lucene field name of the dbpedia index for the uri of a given resource
     */
    public static final String DBPEDIA_LUCENE_FIELD_URI = "uri";
    
    /**
     * this is the lucene field name of the dbpedia index for the label of a given resource
     */
    public static final String DBPEDIA_LUCENE_FIELD_LABEL = "label";
    
    /**
     * this is the lucene field name of the dbpedia index for the page rank of a given resource
     */
    public static final String DBPEDIA_LUCENE_FIELD_PAGE_RANK = "pagerank";
    
    /**
     * this is the lucene field name of the dbpedia index for the surface form of a given resource
     */
    public static final String DBPEDIA_LUCENE_FIELD_SURFACE_FORM = "surfaceForms";
    
    /**
     * this is the lucene field name of the dbpedia index for the disambiguationScore of a given resource
     */
    public static final String DBPEDIA_LUCENE_FIELD_DISAMBIGUATION_SCORE = "disambiguationScore";
    
    /* ################################################################ */
    /* ################################################################ */
    /* ################################################################ */
    
    /**
     * 
     */
    public static final String BOA_LUCENE_FIELD_ENTITY = "entity";
    
    /**
     * 
     */
    public static final String BOA_LUCENE_FIELD_URI = "uri";
    
    /* ################################################################ */
    /* ################################################################ */
    /* ################################################################ */
    
    /**
     * the regular dbpedia ontology prefix
     */
    public static final String DBPEDIA_ONTOLOGY_PREFIX = "http://dbpedia.org/ontology/";
    
    /**
     * the regular dbpedia ontology prefix
     */
    public static final String DBPEDIA_RESOURCE_PREFIX = "http://dbpedia.org/resource/";
    
    /**
     * 
     */
    public static final String RDF_LIVE_NEWS_RESOURCE_PREFIX = "http://rdflivenews.aksw.org/resource/";
    
    /**
     * 
     */
    public static final String RDF_LIVE_NEWS_ONTOLOGY_PREFIX = "http://rdflivenews.aksw.org/ontology/";
    
    /* ################################################################ */
    /* ################################################################ */
    /* ################################################################ */
    
    public static final Set<String> STOP_WORDS = 
            new HashSet<String>(Arrays.asList( "i", "a", "about", "an", "and", "are", "as", "at", "be", "by", "for", "from",
                                                                "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to", "``",
                                                                "was", "what", "when", "where", "who", "will", "with", "the", "'s", "did", "&",
                                                                "have", "has", "had", "were", "'", "'ll", ",", "-LRB-", "-RRB-","-lrb-", "-rrb-", "''", "--", "-", ":", ";", "..."));
    
    public static final Set<String> WEEK_DAYS = 
            new HashSet<String>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));

    public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    /**
     * 
     */
    public static final String RDFS_PREFIX  = "http://www.w3.org/2000/01/rdf-schema#";
    
    public static final String RDFS_LABEL = RDFS_PREFIX + "label";
    
    /**
     * 
     */
    public static final String OWL_PREFIX   = "http://www.w3.org/2002/07/owl#";
    
    /**
     * 
     */
    public static final String OWL_THING = OWL_PREFIX + "Thing";


    public static final String RDF_LIVE_NEWS_ONTOLOGY_SUBJECT = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "subject";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_RESOURCE_OBJECT = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "resourceObject";

    public static final String RDF_LIVE_NEWS_EXTRACTION_PREFIX = "http://rdflivenews.aksw.org/extraction/";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_PROPERTY = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "property";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_FOUND_IN_SENTENCE = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "foundInSentence";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_DATATYPE_OBJECT = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "datatypeObject";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_HAS_SOURCE = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "hasSourceSentence";

    public static final String RDF_LIVE_NEWS_EXTRACTION_SAY_PREFIX = "http://rdflivenews.aksw.org/extraction/say/";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_HAS_SOURCE_URL = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "sourceUrl";

    public static final String RDF_LIVE_NEWS_ONTOLOGY_EXTRACTION_DATE = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "extractionDate";

    public static final Object RDF_LIVE_NEWS_ONTOLOGY_MENTIONS = RDF_LIVE_NEWS_ONTOLOGY_PREFIX + "mentions";

	public static final String NON_GOOD_URL_FOUND = "no url with score above threshold found";
}
