/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.QGramSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.WordnetSimilarityMetric;
import org.apache.commons.io.FileUtils;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.similarity.wordnet.SimilarityAssessor;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimilarityAnnotationGenerator {

    public static void main(String[] args) throws IOException {

        generateCompleteSimilarities();
//        generateSampleSimilarities();
    }
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void generateSampleSimilarities() throws IOException {

        SimilarityMetric sim = new WordnetSimilarityMetric();
        
        List<String> patterns = FileUtils.readLines(new File("/Users/gerb/test/patterns-nlr.txt"));
        Collections.shuffle(patterns);
        
        Set<String> usedPatterns = new HashSet<>();
        
        for ( int i = 0 ; i < 500 ; i++) {
            
            String pattern1 = patterns.get(i);
            String pattern2 = "";
            
            do {
                
                pattern2 = patterns.get(new Random().nextInt(patterns.size()));
            } 
            while ( usedPatterns.contains(pattern2));
            
            System.out.println(String.format("%s\t%s\t%s", pattern1, pattern2, 
                    sim.calculateSimilarity(new DefaultPattern(pattern1), new DefaultPattern(pattern2))));
        }
    }
    
    public static void generateCompleteSimilarities() throws IOException {

        double threshold = 0.7;
        
        SimilarityMetric sim = new QGramSimilarityMetric();
        List<String> patterns = FileUtils.readLines(new File("/Users/gerb/test/patterns-nlr.txt"));
        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/test/patterns-nlr-"+threshold+".tsv", "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
        
        int i = 1;
        for ( String pattern1 : patterns ) {
            System.out.println(i++ + "/" + patterns.size());
            for ( String pattern2 : patterns ) {
                
                if ( !pattern1.equals(pattern2) ) {

                    double similarity = sim.calculateSimilarity(new DefaultPattern(pattern1), new DefaultPattern(pattern2));
                    if ( similarity > threshold ) writer.write(String.format("%s\t%s\t%s", pattern1, pattern2, similarity));
                }
            }
        }
        writer.close();
    }
}