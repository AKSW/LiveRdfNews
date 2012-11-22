/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.SimilarityGenerator;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultSimilarityGenerator implements SimilarityGenerator {

    private SimilarityMetric similarityMetric;
    private Set<Similarity> similarities;

    public DefaultSimilarityGenerator(SimilarityMetric similarityMetric) {

        this.similarityMetric   = similarityMetric;
        this.similarities       = new HashSet<>();
    }

    @Override
    public Set<Similarity> calculateSimilarities(List<Pattern> patterns) {
        
        System.out.println(this.similarities.size() + " similarities cached! Worst case: " + patterns.size() * patterns.size() + " comparisons!");

        for ( Pattern pattern1 : patterns ) { 
            if ( pattern1.getScore() > RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") ) {
                for ( Pattern pattern2 : patterns ) {

                    Similarity sim = new Similarity(pattern1, pattern2); 
                    
                    // avoid recalculation in every iteration and avoid having identities in the set
                    // and make sure that we only generate similarities for identical type patterns
                    if ( !this.similarities.contains(sim) && !pattern1.equals(pattern2) ) {
                        
                        if ( RdfLiveNews.CONFIG.getBooleanSetting("similarity", "checkDomainAndRange") && 
                                domainAndRangeMatch(pattern1, pattern2) ) {
                            
                            sim.setSimilarity(this.similarityMetric.calculateSimilarity(pattern1, pattern2));
                            
                            if ( sim.getSimilarity() >= RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") ) 
                                similarities.add(sim);
                        }
                        else {
                            
                            sim.setSimilarity(this.similarityMetric.calculateSimilarity(pattern1, pattern2));
                            
                            if ( sim.getSimilarity() >= RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") ) 
                                similarities.add(sim);
                        }
                    }
                }
            }
        }
        
        if ( RdfLiveNews.CONFIG.getBooleanSetting("similarity", "writeFile") ) {
            
            String fileName = RdfLiveNews.DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "similarity");
            fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
            fileName += "iter-#" + RdfLiveNews.ITERATION + "-";
            fileName += "sim-" + RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
            fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") + ".tsv";
                    
            List<String> lines = new ArrayList<String>();
            
            for ( Similarity sim : similarities ) {
                
                String line = sim.getPattern1().getNaturalLanguageRepresentation() + "\t";
                line += sim.getPattern2().getNaturalLanguageRepresentation() + "\t";
                line += sim.getSimilarity();
                
                lines.add(line);
            }

            Collections.sort(lines);
            BufferedFileWriter writer = new BufferedFileWriter(fileName, "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
            for (String line : lines ) writer.write(line); 
            writer.close();
        }
        
        return similarities; 
    }

    private boolean domainAndRangeMatch(Pattern pattern1, Pattern pattern2) {

        Set<String> typesEntityOne = new HashSet<String>(Arrays.asList(pattern1.getFavouriteTypeFirstEntity(), pattern1.getFavouriteTypeSecondEntity()));
        Set<String> typesEntityTwo = new HashSet<String>(Arrays.asList(pattern2.getFavouriteTypeFirstEntity(), pattern2.getFavouriteTypeSecondEntity()));
        
        typesEntityOne.removeAll(typesEntityTwo);
        
        return typesEntityOne.isEmpty();
    }
    
//    public static void main(String[] args) {
//
//        Pattern one = new DefaultPattern("one");
//        one.setFavouriteTypeFirstEntity("firstType");
//        one.setFavouriteTypeSecondEntity("secondType");
//        
//        Pattern two = new DefaultPattern("two");
//        two.setFavouriteTypeFirstEntity("firstType");
//        two.setFavouriteTypeSecondEntity("secondType");
//        System.out.println(domainAndRangeMatch(one, two));
//        
//        two.setFavouriteTypeFirstEntity("secondType");
//        two.setFavouriteTypeSecondEntity("firstType");
//        System.out.println(domainAndRangeMatch(one, two));
//        
//        two.setFavouriteTypeFirstEntity("first1Type");
//        two.setFavouriteTypeSecondEntity("secondType");
//        System.out.println(domainAndRangeMatch(one, two));
//        
//        two.setFavouriteTypeFirstEntity("firstType");
//        two.setFavouriteTypeSecondEntity("second1Type");
//        System.out.println(domainAndRangeMatch(one, two));
//    }
}
