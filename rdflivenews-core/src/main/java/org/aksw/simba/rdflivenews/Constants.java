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
     * use this value as the lucene field name for a sentence extractions date
     */
    public static final String LUCENE_FIELD_EXTRACTION_DATE = "date";
    
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
    
    /* ################################################################ */
    /* ################################################################ */
    /* ################################################################ */
    
    /**
     * the regular dbpedia ontology prefix
     */
    public static final String DBPEDIA_ONTOLOGY_PREFIX = "http://dbpedia.org/ontology/";
    
    /* ################################################################ */
    /* ################################################################ */
    /* ################################################################ */
    
    public static final Set<String> STOP_WORDS = new HashSet<String>(Arrays.asList( "i", "a", "about", "an", "and", "are", "as", "at", "be", "by", "for", "from",
                                                                "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to", "``",
                                                                "was", "what", "when", "where", "who", "will", "with", "the", "'s", "did",
                                                                "have", "has", "had", "were", "'ll", ",", "-LRB-", "-RRB-", "''", "--", "-", ";"));
}
