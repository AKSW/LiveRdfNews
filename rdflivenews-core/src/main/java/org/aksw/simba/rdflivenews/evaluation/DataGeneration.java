/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.comparator.PatternSupportSetComparator;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;
import org.aksw.simba.rdflivenews.pattern.filter.impl.DefaultPatternFilter;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefinementManager;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.util.ReflectionManager;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DataGeneration {

    private static int pairs = 0;
    private static int ids = 0;
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        DataGeneration.generatePatterns();
    }
    
    public static void generatePatterns() throws InvalidFileFormatException, IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        
//        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        
//        Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
//        deduplication.runDeduplication(0, 37, 37);
        
        Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentences();
        System.out.print("Starting pattern search in "+currentNonDuplicateSentenceIds.size()+" sentences ...  ");
        
        PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
        List<Pattern> patternsOfIteration = patternSearchManager.startPatternSearchCallables(new ArrayList<Integer>(currentNonDuplicateSentenceIds));
        System.out.println("DONE");

        // filter the patterns and merge the old and the new patterns
        PatternFilter patternFilter = new DefaultPatternFilter();
        patternsOfIteration         = patternFilter.filter(patternSearchManager.mergeNewFoundPatterns(patternsOfIteration));
        
        // cut of most of the crap
        Collections.sort(patternsOfIteration, new PatternSupportSetComparator());
        List<Pattern> top1PercentPattern = patternsOfIteration.size() > 100000 ? patternsOfIteration.subList(0, 1000) : patternsOfIteration.subList(0, patternsOfIteration.size() / 100);
        
        // refines the domain and range of the patterns
        PatternRefinementManager refinementManager = new PatternRefinementManager();
        refinementManager.startPatternRefinement(top1PercentPattern);
        
        List<String> lines = new ArrayList<String>();   
        System.out.println("Found " + top1PercentPattern.size()+ " patterns");
        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/Desktop/patterns1percent5occ.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter writer1 = new BufferedFileWriter("/Users/gerb/Desktop/patterns1percent5occ.pattern", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter writer2 = new BufferedFileWriter("/Users/gerb/Desktop/pattern_counts.pattern", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        
        for ( Pattern pattern : top1PercentPattern ) {
        	
        	writer2.write(pattern.getTotalOccurrence() + "\t"+ pattern.getNaturalLanguageRepresentation());
        }
        writer2.close();
        
        int count = 0;
        Collections.shuffle(top1PercentPattern);
        top1PercentPattern = top1PercentPattern.subList(0, 150);
        
        for ( Pattern pattern : top1PercentPattern ) {
//            if ( pattern.getTotalOccurrence() >= 5 ) {
                
                count += pattern.getTotalOccurrence();
                patternToString(pattern, lines);
//            }
        }
        System.out.println("pairs: " + pairs + " ids: " + ids);
        System.out.println("Lines in file: "+ lines.size() + " totalOcc: " + count);
        Collections.shuffle(lines);
        lines = lines.subList(0, 1000);
        
        Set<String> posTags = new HashSet<String>();
        
        for ( String line : lines ) {
        	
        	for (Pattern pattern : top1PercentPattern) if ( pattern.getNaturalLanguageRepresentation().equals(line.split("___")[1] )) {
        		posTags.add(pattern.getNaturalLanguageRepresentation()+ "___" + pattern.getNaturalLanguageRepresentationWithTags());
        	}
        	writer.write(line);
        }
        writer.close();
        for ( String patternAndPos : posTags ) writer1.write(patternAndPos);
        writer.close();
        writer1.close();
    }
    
    public static void patternToString(Pattern pattern, List<String> lines) {
        
        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            pairs++;
            
            for ( Integer id : pair.getLuceneSentenceIds()) {
                
                ids++;

                String sentence = IndexManager.getInstance().getStringValueFromDocument(id, Constants.LUCENE_FIELD_TEXT);
                String articleUrl = IndexManager.getInstance().getStringValueFromDocument(id, Constants.LUCENE_FIELD_URL);
                
                lines.add(pair.getFirstEntity().getLabel() + "___" +
                                pattern.getNaturalLanguageRepresentation() + "___" + 
                                pair.getSecondEntity().getLabel() + "___" +
                                id + "___" +
                                sentence + "___" +
                                articleUrl);
            }
        }
    }
}
