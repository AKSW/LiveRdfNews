/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.label.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.evaluation.DisambiguationEvaluation;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.index.LowerCaseWhitespaceAnalyzer;
import org.aksw.simba.rdflivenews.pattern.refinement.label.LabelRefiner;
import org.aksw.simba.rdflivenews.pattern.search.impl.NamedEntityTagPatternSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EntityLabelRefiner implements LabelRefiner {
	
	private NamedEntityTagPatternSearcher searcher = new NamedEntityTagPatternSearcher();
	private Map<String,String> refinedLabelCache = new HashMap<>();
	
//	public static String INDEX_DIRECTORY = "/Users/gerb/Development/workspaces/experimental/rdflivenews/100percent/index";
//    public static Directory INDEX;
//    private IndexWriter writer;
//    private final Analyzer analyzer = new LowerCaseWhitespaceAnalyzer(Version.LUCENE_40);

    public EntityLabelRefiner() {
    	
//    	createIndex();
    }
    
    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.refinement.label.LabelRefiner#refineLabel(java.lang.String, java.lang.Integer)
     */
    @Override
    public String refineLabel(String label, Integer sentenceId) {
        
    	String key = label + sentenceId;
    	if ( !this.refinedLabelCache.containsKey(key) ) {

    		// grab the article url for the current sentence and get all other sentences ner tagged
            String url = IndexManager.getInstance().getStringValueFromDocument(sentenceId, Constants.LUCENE_FIELD_URL);
            Set<String> nerTaggedSentences = IndexManager.getInstance().getAllNerTaggedSentencesFromArticle(url);
//            Set<String> nerTaggedSentences = getAllNerTaggedSentencesFromArticle(url);
            if ( nerTaggedSentences.isEmpty() ) System.err.println("We did not find any NER tagged sentences for this article: " + url);
            
            Map<String,String> entities = new HashMap<>();
            for ( String taggedSentence : nerTaggedSentences) entities.putAll(getEntities(searcher.mergeTagsInSentences(taggedSentence)));
            this.refinedLabelCache.put(key, this.findLongestMatch(label, entities));
    	}
    	return this.refinedLabelCache.get(key);
    }
    
    /**
     * 
     * @param label
     * @param entities
     * @return
     */
    public String findLongestMatch(String label, Map<String, String> entities) {

    	DisambiguationEvaluation.DEBUG_WRITER.write(label);
    	DisambiguationEvaluation.DEBUG_WRITER.write(entities.toString());
		DisambiguationEvaluation.DEBUG_WRITER.flush();
		
		String labelType = entities.get(label);
    	
        String match = label;
        for ( Map.Entry<String, String> entity : entities.entrySet() ) {
        	if ( entity.getKey().contains(label) && !entity.getKey().contains("Mr.") && !entity.getKey().contains("Mrs.") ) {
        		if ( entity.getKey().startsWith(label) || entity.getKey().endsWith(label)) {
        			if ( entity.getKey().contains(" ") || !entity.getKey().contains("-"))
        				match = entity.getKey();
        		}
        	}
        }
        match = match.replaceAll("`", "");
        match = match.replaceAll(" ' ", " ");
        for ( String word : Arrays.asList("Rev.", "When", "Chief", "What", "American", "Education Secretary", "Because", "Mr.", "Mrs.", "Capt.", "ex-", "Former", "Col.", "Army General", "Army Gen.", "Sen.", "Sgt.", "Lt.", "the ", "The ", "Dr.", "Rep.")) {
        	
        	if ( match.startsWith(word) || match.endsWith(word) ) {
        		match = match.replace(word, "");
        	}
        }
        	
//         match.replace("'s", "s");

        DisambiguationEvaluation.DEBUG_WRITER.write(label + ": " + match);
        // fail safe
        // debug  TODO return the type of the label so that we can use it in the uri getCandidates query
        match = match.replace("  ", " ");
        return match.trim();
    }

    /**
     * 
     * @param mergedTaggedSentence
     * @return
     */
    private Map<String,String> getEntities(List<String> mergedTaggedSentence){
        
        Map<String,String> entities = new HashMap<String,String>();
        for (String entity : mergedTaggedSentence) {

            if ( RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("ALL") ) {
                
                if (entity.endsWith("_PERSON") ) entities.put(entity.replace("_PERSON", ""), "PERSON");
                if (entity.endsWith("_MISC")) entities.put(entity.replace("_MISC", ""), "MISC");
                if (entity.endsWith("_PLACE")) entities.put(entity.replace("_PLACE", ""), "PLACE");
                if (entity.endsWith("_ORGANIZATION")) entities.put(entity.replace("_ORGANIZATION", ""), "ORGANIZATION");
            }
            else if (RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("PERSON")) {
                
                if (entity.endsWith("_PERSON") ) entities.put(entity.replace("_PERSON", ""), "PERSON");
            }
        }
        
        return entities;
    }
    
//    /**
//     * Opens and closes an index in the index directory
//     */
//    public void createIndex() {
//        
//        // create the index normalTripleWriter configuration and create a new index normalTripleWriter
//        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
//        indexWriterConfig.setRAMBufferSizeMB(1024);
//        indexWriterConfig.setOpenMode(LuceneManager.isIndexExisting(INDEX_DIRECTORY) ? OpenMode.APPEND : OpenMode.CREATE);
//        writer = LuceneManager.openIndexWriter(INDEX_DIRECTORY, indexWriterConfig);
//        INDEX = writer.getDirectory();
//        LuceneManager.closeIndexWriter(this.writer);
//    }
    
//    public Set<String> getAllNerTaggedSentencesFromArticle(String articleUrl) {
//    	
//    	Query articles = new TermQuery(new Term(Constants.LUCENE_FIELD_URL, articleUrl));
//        
//        IndexSearcher searcher = LuceneManager.openIndexSearcher(INDEX);
//        TopScoreDocCollector collector = TopScoreDocCollector.create(1000, false);
//        LuceneManager.query(searcher, articles, collector);
//        
//        Set<String> sentences = new HashSet<>();
//        
//        // add the primary key of each document to the list
//        for ( ScoreDoc doc : collector.topDocs().scoreDocs )
//            sentences.add(
//                    LuceneManager.getDocumentByNumber(searcher.getIndexReader(), doc.doc).get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE));
//        
//        LuceneManager.closeIndexReader(searcher.getIndexReader());
//        LuceneManager.closeIndexSearcher(searcher);
//        
//        return sentences;
//	}
    
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        
//        Hernandez pitched Seattle Mariners 49016
        EntityLabelRefiner refiner = new EntityLabelRefiner();
        for ( String s : IndexManager.getInstance().getAllNerTaggedSentencesFromArticle("http://wvgazette.com/rssFeeds/201208150133")) {
        	System.out.println(s);
        }
    }
}
