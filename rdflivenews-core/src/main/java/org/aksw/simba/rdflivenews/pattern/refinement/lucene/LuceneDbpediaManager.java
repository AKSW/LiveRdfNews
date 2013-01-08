/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.index.LowerCaseWhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import com.github.gerbsen.lucene.LuceneManager;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneDbpediaManager {

    private Directory INDEX;
    private IndexSearcher searcher;
    
    public static final String NO_URI_FOUND = "not found";
    
    private QueryParser parser = null;
    
    private Map<String,Set<String>> surfaceFormsCache = new HashMap<>();
    
    public LuceneDbpediaManager() {
        
        INDEX = LuceneManager.openLuceneIndex(RdfLiveNews.CONFIG.getStringSetting("general", "dbpedia"));
        this.searcher = new IndexSearcher(LuceneManager.openIndexReader(INDEX));
        
        this.parser = new QueryParser(Version.LUCENE_40, "sentence", new LowerCaseWhitespaceAnalyzer(Version.LUCENE_40));
    }
    
    /**
     * 
     */
    public void close() {

        LuceneManager.closeIndexSearcher(this.searcher);
    }
    
    /**
     * 
     * @param uri
     * @return
     */
    public Set<String> getTypesOfResource(String uri){
       
        Set<String> types = new HashSet<String>();
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(100, false);
        LuceneManager.query(this.searcher, new TermQuery(new Term(Constants.DBPEDIA_LUCENE_FIELD_URI, uri)), collector);
        
        for ( ScoreDoc hit : collector.topDocs().scoreDocs )
            for (String type : LuceneManager.getDocumentByNumber(this.searcher.getIndexReader(), hit.doc).getValues(Constants.DBPEDIA_LUCENE_FIELD_TYPES))
                // we want to exclude schema.org stuff or owl:thing
                if ( type.startsWith(Constants.DBPEDIA_ONTOLOGY_PREFIX) ) types.add(type);
                
        return types;
    }
    
    public List<String> getUriForSurfaceForm(String label) {
    	
//    	PhraseQuery phraseQuery = new PhraseQuery();
//    	for ( String part : label.split(" ") ) phraseQuery.add(new Term(Constants.DBPEDIA_LUCENE_FIELD_SURFACE_FORM,  part.toLowerCase()));
    	
    	Query phraseQuery = new TermQuery(new Term(Constants.DBPEDIA_LUCENE_FIELD_SURFACE_FORM,  label));
    	
    	TopFieldDocs docs = LuceneManager.query(this.searcher, phraseQuery, new Sort(new SortField(Constants.DBPEDIA_LUCENE_FIELD_DISAMBIGUATION_SCORE, SortField.Type.DOUBLE, true)), 100);
    	List<String> uriCandidates = new ArrayList<String>();
    	
        for ( ScoreDoc hit : docs.scoreDocs ) 
        	uriCandidates.add(LuceneManager.getDocumentByNumber(this.searcher.getIndexReader(), hit.doc).get(Constants.DBPEDIA_LUCENE_FIELD_URI));
        	
        return uriCandidates;
	}
    
    public Set<String> getSurfaceFormsForUri(String uri) {
    	
    	if ( !this.surfaceFormsCache.containsKey(uri) ) {
    	
    		TopScoreDocCollector collector = TopScoreDocCollector.create(1_000, true);
        	LuceneManager.query(this.searcher, new TermQuery(new Term(Constants.DBPEDIA_LUCENE_FIELD_URI, uri)), collector);
        	Set<String> surfaceForms = new HashSet<String>();
        	
            for ( ScoreDoc hit : collector.topDocs().scoreDocs ) 
            	surfaceForms.addAll(Arrays.asList(LuceneManager.getDocumentByNumber(this.searcher.getIndexReader(), hit.doc).getValues(Constants.DBPEDIA_LUCENE_FIELD_SURFACE_FORM)));
            
            this.surfaceFormsCache.put(uri, surfaceForms);
    	}
        return this.surfaceFormsCache.get(uri);
	}

    /**
     * 
     * @param uri
     * @return
     */
	public double getAprioriScoreForUri(String uri) {
		
    	TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        LuceneManager.query(this.searcher, new TermQuery(new Term(Constants.DBPEDIA_LUCENE_FIELD_URI, uri)), collector);

        return Double.valueOf(LuceneManager.getDocumentByNumber(this.searcher.getIndexReader(), collector.topDocs().scoreDocs[0].doc)
        		.get(Constants.DBPEDIA_LUCENE_FIELD_DISAMBIGUATION_SCORE));
	}
	
	/**
	 * For DBPedia 3.8 this is 12.256780018299173
	 * 
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public double findMaximumAprioriScore() throws NumberFormatException, IOException{
		
		double max = 0D;
		
		int maxDocs = this.searcher.getIndexReader().numDocs();
		for ( int i = 0; i < maxDocs ; i++) {
			
			double d = Double.valueOf(this.searcher.getIndexReader().document(i).get(Constants.DBPEDIA_LUCENE_FIELD_DISAMBIGUATION_SCORE));
			if ( d > max ) {
				max = d;
				System.out.println(max);
				System.out.println(this.searcher.getIndexReader().document(i).get(Constants.DBPEDIA_LUCENE_FIELD_URI));
			}
		}
		
		return max;
	}

	public static void main(String[] args) {

		RdfLiveNews.init();
		
		long start = System.currentTimeMillis();
		
		LuceneDbpediaManager dbpediaManager = new LuceneDbpediaManager();
		
		System.out.println(dbpediaManager.getUriForSurfaceForm("GM"));
		
//		for ( String uriCandidate : dbpediaManager.getUriForSurfaceForm("Barack Obama")) {
//			System.out.println("Score("+uriCandidate.replace("http://dbpedia.org/resource", "dbr:")+") = "+ dbpediaManager.getAprioriScoreForUri(uriCandidate));
//		}
//		
//		System.out.println("Took " + (System.currentTimeMillis() - start) + "ms.");
	}
}
