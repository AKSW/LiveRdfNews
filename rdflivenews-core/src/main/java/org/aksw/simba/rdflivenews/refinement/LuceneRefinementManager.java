/**
 * 
 */
package org.aksw.simba.rdflivenews.refinement;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.crawler.NewsCrawler;
import org.aksw.simba.rdflivenews.lucene.LuceneManager;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneRefinementManager {

    private Directory INDEX;
    private QueryParser dbpediaLabelQueryParser = new QueryParser(Version.LUCENE_36, Constants.DBPEDIA_LUCENE_FIELD_LABEL, new StandardAnalyzer(Version.LUCENE_36));
    
    public static final String NO_URI_FOUND = "not found";
    
    public LuceneRefinementManager() {
        
        INDEX = LuceneManager.openLuceneIndex(NewsCrawler.CONFIG.getStringSetting("database", "dbpedia"));
    }
    
    /**
     * 
     * @param uri
     * @return
     */
    public Set<String> getTypesOfResource(String uri){
       
        Set<String> types = new HashSet<String>();
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(100, false);
        IndexReader reader = LuceneManager.createIndexReader(INDEX);
        IndexSearcher searcher = new IndexSearcher(reader);
        LuceneManager.query(searcher, new TermQuery(new Term(Constants.DBPEDIA_LUCENE_FIELD_URI, uri)), collector);

        for ( ScoreDoc hit : collector.topDocs().scoreDocs )
            for (String type : LuceneManager.getDocument(reader, hit.doc).getValues(Constants.DBPEDIA_LUCENE_FIELD_TYPES))
                // we want to exclude schema.org stuff or owl:thing
                if ( type.startsWith(Constants.DBPEDIA_ONTOLOGY_PREFIX) ) types.add(type);
                
        LuceneManager.closeSearcher(searcher);
        LuceneManager.closeIndexReader(reader);
        
        return types;
    }

    /**
     * @param label for which a uri should searched
     * @return a uri for a given resource label or LuceneRefinementManager.NO_URI_FOUND 
                if no uri could be found
     */
    public String getPossibleUri(String label) {

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        IndexReader reader             = LuceneManager.createIndexReader(INDEX);
        IndexSearcher searcher         = new IndexSearcher(reader);
        
        LuceneManager.query(searcher, LuceneManager.parse(this.dbpediaLabelQueryParser, label) , collector);

        String uri = LuceneRefinementManager.NO_URI_FOUND;
        
        if ( collector.getTotalHits() > 0 ) 
            uri = LuceneManager.getDocument(reader, collector.topDocs().scoreDocs[0].doc).get(Constants.DBPEDIA_LUCENE_FIELD_URI);
                
        LuceneManager.closeSearcher(searcher);
        LuceneManager.closeIndexReader(reader);
        
        return uri;
    }
}
