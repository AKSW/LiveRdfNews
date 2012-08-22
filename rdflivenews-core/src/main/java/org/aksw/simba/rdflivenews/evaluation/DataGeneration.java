/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.lucene.LuceneManager;
import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.search.PatternSearcher;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.pattern.search.impl.NamedEntityTagPatternSearcher;
import org.apache.lucene.index.IndexReader;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DataGeneration {

    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        DataGeneration.generatePatterns();
    }
    
    public static void generatePatterns() throws InvalidFileFormatException, IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        String indexDir = RdfLiveNews.CONFIG.RDF_LIVE_NEWS_DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "index");
        IndexReader reader = LuceneManager.openIndexReader(LuceneManager.openLuceneIndex(indexDir));
        
        StanfordNLPNamedEntityRecognition neTagger = new StanfordNLPNamedEntityRecognition();
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        
        for ( int i = 0; i < reader.maxDoc() && i < 100000 ; i++ ) {
            
            String sentence = reader.document(i).get(Constants.LUCENE_FIELD_TEXT);
            
            PatternSearcher patternSearcher = new NamedEntityTagPatternSearcher();
            for (Pattern pattern : patternSearcher.extractPatterns(neTagger.getAnnotatedSentence(sentence), i)){
                
                List<String> tokens = new ArrayList<String>(Arrays.asList(pattern.getNaturalLanguageRepresentation().toLowerCase().split(" ")));
                
                if ( tokens.size() > 0 && tokens.size() < 10 ) {

                    tokens.removeAll(Constants.STOP_WORDS);
                    
                    if ( !tokens.isEmpty() ) patterns.add(pattern);
                }
            }
        }
        
        PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
        List<Pattern> mergedPatterns = patternSearchManager.mergeNewFoundPatterns(patterns);
        for ( Pattern p : mergedPatterns ) {
            
            System.out.println(p.getNaturalLanguageRepresentation());
        }
    }
}
