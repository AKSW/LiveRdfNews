/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.QGramSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.TakelabSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.WordnetSimilarityMetric;
import org.apache.commons.io.FileUtils;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.similarity.wordnet.SimilarityAssessor;

import edu.cmu.lti.ws4j.util.MatrixCalculator;
import edu.stanford.nlp.process.Morphology;

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

        double threshold = 0.5;
        
//        SimilarityMetric sim = new QGramSimilarityMCetric();
        SimilarityMetric sim = new WordnetSimilarityMetric();
//        SimilarityMetric sim = new TakelabSimilarityMetric();
        List<String> patterns = FileUtils.readLines(new File("/Users/gerb/test/patterns-nlr.txt")).subList(0, 100);
        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/test/test-patterns-nlr-"+sim.getClass().getSimpleName()+"-"+threshold+".tsv", "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
        
        double max = 1; 
        List<Similarity> sims = new ArrayList<>();
        
        int i = 1;
        for ( String pattern1 : patterns ) {
            
            String[] parts = pattern1.split("___");
            
            String nlr1 = parts[0];
            String pos1 = parts[1];
            
            System.out.println(i++ + "/" + patterns.size());
            for ( String pattern2 : patterns ) {
                
                String[] parts2 = pattern2.split("___");
                
                String nlr2 = parts2[0];
                String pos2 = parts2[1];
                
                if ( !pattern1.equals(pattern2) ) { 

                    Pattern p1 = new DefaultPattern(nlr1, pos1);
                    Pattern p2 = new DefaultPattern(nlr2, pos2);
                    
                    double similarity = sim.calculateSimilarity(p1,p2);
                    System.out.println(similarity);
                    if ( similarity > max && similarity != Double.MAX_VALUE ) max = similarity;
                    
                    sims.add(new Similarity(p1, p2, similarity));
                }
            }
        }
        
        ((WordnetSimilarityMetric) sim).writer.close();
        System.out.println( "Maximum-Score: " + max);
        for ( Similarity simi : sims ) {
            
            double value = 0;
            
            if ( simi.getSimilarity() == Double.MAX_VALUE ) value = 1; 
            else {
                    value = simi.getSimilarity() / max;
            }
            if ( value > 0.1 )          
                writer.write(String.format("%s\t%s\t%s", simi.getPattern1().getNaturalLanguageRepresentation(), simi.getPattern2().getNaturalLanguageRepresentation(), value));
        }
        
        writer.close();
    }
}