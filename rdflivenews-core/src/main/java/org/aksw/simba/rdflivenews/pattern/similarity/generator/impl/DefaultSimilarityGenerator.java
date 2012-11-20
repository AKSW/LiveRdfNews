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
    private List<Pattern> patterns;

    public DefaultSimilarityGenerator(SimilarityMetric similarityMetric, List<Pattern> patterns) {

        this.similarityMetric = similarityMetric;
        this.patterns = patterns;
    }

    @Override
    public Set<Similarity> calculateSimilarities() {

        Set<Similarity> similarities = new HashSet<>();
        
        for ( Pattern pattern1 : this.patterns ) { 
            if ( pattern1.getScore() > RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") ) {

                for ( Pattern pattern2 : this.patterns ) {

                    // avoid having identities in the set
                    if ( !pattern1.equals(pattern2) ) {
                        
                        if ( domainAndRangeMatch(pattern1, pattern2) ) {
                            
                            double similarity = this.similarityMetric.calculateSimilarity(pattern1, pattern2);
                            if ( similarity > 0 ) similarities.add(new Similarity(pattern1, pattern2, similarity));
                        }
                    }
                }
            }
        }
        
        if ( RdfLiveNews.CONFIG.getBooleanSetting("similarity", "writeFile") ) {
            
            String fileName = RdfLiveNews.DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "similarity");
            fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
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
