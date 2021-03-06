/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.lucene;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.util.Encoder;
import org.aksw.simba.rdflivenews.util.Encoder.Encoding;
import org.aksw.simba.rdflivenews.util.LuceneManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneRefinementManager {

    private Directory INDEX;
    private IndexSearcher searcher;
    
    public static final String NO_URI_FOUND = "not found";
    
    public LuceneRefinementManager() {
        
        INDEX = LuceneManager.openLuceneIndex(RdfLiveNews.CONFIG.getStringSetting("general", "dbpedia"));
        this.searcher         = new IndexSearcher(LuceneManager.openIndexReader(INDEX));
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

    /**
     * @param label for which a uri should searched
     * @return a uri for a given resource label or LuceneRefinementManager.NO_URI_FOUND 
                if no uri could be found
     */
    public String getPossibleUri(String label) {

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        Query query = new TermQuery(new Term(Constants.DBPEDIA_LUCENE_FIELD_LABEL, label));
        
        String uri = Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX + Encoder.urlEncode(label.replace(" ", "_"), Encoding.UTF_8);
        
        if ( query != null ) {
            
            LuceneManager.query(this.searcher, query, collector);

            if ( collector.getTotalHits() > 0 ) 
                uri = LuceneManager.getDocumentByNumber(this.searcher.getIndexReader(), collector.topDocs().scoreDocs[0].doc).get(Constants.DBPEDIA_LUCENE_FIELD_URI);
        }
        return uri;
    }

    /**
     * 
     */
    public void close() {

        LuceneManager.closeIndexSearcher(this.searcher);
    }
}
