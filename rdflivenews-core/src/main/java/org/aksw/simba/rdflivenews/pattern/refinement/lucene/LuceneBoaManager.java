/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.lucene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;

import com.github.gerbsen.lucene.LuceneManager;
import com.github.gerbsen.math.Frequency;
/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneBoaManager {

    private Directory INDEX;
    private IndexSearcher searcher;
    
    public static final String NO_URI_FOUND = "not found";
    
    public LuceneBoaManager() {
        
        INDEX = LuceneManager.openLuceneIndex(RdfLiveNews.CONFIG.getStringSetting("general", "boa"));
        this.searcher         = new IndexSearcher(LuceneManager.openIndexReader(INDEX));
    }
    
    /**
     * 
     */
    public void close() {

        LuceneManager.closeIndexSearcher(this.searcher);
    }
    
    public Map<String,Integer> getContextNamedEntities(String uri) {
    	
    	TopScoreDocCollector collector = TopScoreDocCollector.create(1_000, true);
    	LuceneManager.query(this.searcher, new TermQuery(
    			new Term(Constants.BOA_LUCENE_FIELD_URI, uri.replace("http://dbpedia.org/resource/", "http://en.wikipedia.org/wiki/"))), collector);
        
    	Frequency f = new Frequency();
    	
    	for ( ScoreDoc hit : collector.topDocs().scoreDocs ) 
    		for ( String entity : LuceneManager.getDocumentByNumber(this.searcher.getIndexReader(), hit.doc).getValues(Constants.BOA_LUCENE_FIELD_ENTITY))
    			if ( !entity.contains("`") ) f.addValue(entity);

    	Map<String,Integer> namedEntities = new HashMap<String,Integer>();
    	Iterator<Comparable<?>> iter = f.valuesIterator();
    	while ( iter.hasNext() ) {
    		String value = (String) iter.next();
    		namedEntities.put(value, new Long(f.getCount(value)).intValue());
    	}
    	
        return namedEntities;
	}
    
    public static void main(String[] args) {
		
//    	RdfLiveNews.init();
//    	
//    	System.out.println("ere");
//    	for ( Map.Entry<String, Integer> s : LuceneBoaManager.getInstance().getContextNamedEntities("http://dbpedia.org/resource/Mitt_Romney").entrySet())
//    		System.out.println(s.getKey() + s.getValue());
	}
}
