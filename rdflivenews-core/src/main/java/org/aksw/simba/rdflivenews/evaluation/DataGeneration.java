/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.comparator.PatternOccurrenceComparator;
import org.aksw.simba.rdflivenews.pattern.search.PatternSearcher;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.pattern.search.impl.NamedEntityTagPatternSearcher;
import org.aksw.simba.rdflivenews.pattern.search.impl.PartOfSpeechTagPatternSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.lucene.LuceneManager;


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
        
        StanfordNLPPartOfSpeechTagger posTagger = new StanfordNLPPartOfSpeechTagger();
        
        List<Pattern> goodPatterns = new ArrayList<Pattern>();
        List<Pattern> badPatterns = new ArrayList<Pattern>();
        
        for ( int i = 0; i < reader.maxDoc() && i < 100000 ; i++ ) {
            
            System.out.println(i);
            
            Document doc = reader.document(i);
            String sentence = doc.get(Constants.LUCENE_FIELD_TEXT);
            int luceneId = Integer.valueOf(doc.get(Constants.LUCENE_FIELD_ID));
            PatternSearcher patternSearcher = new PartOfSpeechTagPatternSearcher();
            
            for (Pattern pattern : patternSearcher.extractPatterns(posTagger.getAnnotatedSentence(sentence), luceneId)){
                
                List<String> tokens = new ArrayList<String>(Arrays.asList(pattern.getNaturalLanguageRepresentation().toLowerCase().split(" ")));
                
                if ( tokens.size() > 0 && tokens.size() < 15 ) {

                    tokens.removeAll(Constants.STOP_WORDS);
                    
                    if ( !tokens.isEmpty() ) goodPatterns.add(pattern);
                    else badPatterns.add(pattern);
                }
                else badPatterns.add(pattern);
            }
        }
        
//        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/test/annotation/patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
//        for ( Pattern pattern : goodPatterns ) writer.write(patternToString(pattern));
//        writer.close();
        
//        writer = new BufferedFileWriter("/Users/gerb/test/annotation/bad_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
//        for ( Pattern pattern : badPatterns ) writer.write(patternToString(pattern));
//        writer.close();
        
        System.out.println("Good: " + goodPatterns.size());
        System.out.println("Bad: " + badPatterns.size());
        
//        for ( Pattern p: badPatterns) System.out.println(p.getNaturalLanguageRepresentation());
        System.out.println("\n\n##################################################################\n##################################################################\n\n");
//        for ( Pattern p: goodPatterns) System.out.println(p.getNaturalLanguageRepresentation());
        
        
        PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
        List<Pattern> mergedPatterns = patternSearchManager.mergeNewFoundPatterns(goodPatterns);
        Collections.sort(mergedPatterns, new PatternOccurrenceComparator());
        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/test/patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
        for ( Pattern pattern : mergedPatterns ) {
            
            System.out.println(pattern.getNaturalLanguageRepresentation() + ": "+  pattern.getTotalOccurrence());
            writer.write(pattern.getNaturalLanguageRepresentation());
        }
        writer.close();
    }
    
    public static String patternToString(Pattern pattern) {
        
        return pattern.getLearnedFromEntities().get(0).getFirstEntity().getLabel() + "___" +
                pattern.getNaturalLanguageRepresentation() + "___" + 
                pattern.getLearnedFromEntities().get(0).getSecondEntity().getLabel() + "___" +
                pattern.getFoundInSentencesIds().iterator().next();
    }
}
