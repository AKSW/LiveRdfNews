/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator.concurrency;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.util.ReflectionManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class CachedSimilarityGeneratorCallable implements Callable<Set<Similarity>> {

    private int progress                        = 0;
    private String name                         = null;
    private SimilarityMetric similarityMetric   = null;
    private List<Pattern> patterns              = null;
    private Pattern pattern                     = null;
    private Set<Similarity> results             = null;
    
    public CachedSimilarityGeneratorCallable(List<Pattern> patterns, Pattern pattern, String similarityMetric, Set<Similarity> results, String name) {

        this.name = name;
        this.similarityMetric = (SimilarityMetric) ReflectionManager.newInstance(similarityMetric);
        this.patterns = patterns;
        this.pattern = pattern;
        this.results = results;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Set<Similarity> call() throws Exception {

        for ( Pattern pattern2 : patterns ) {
            // this greatly reduces the number of comparisons since the patterns are zipf distributed with a very long tail
//            if ( pattern2.isAboveThresholds() ) {
                
                Similarity sim = new Similarity(this.pattern, pattern2);
                
                synchronized(this.results) {
                    
                    // avoid recalculation in every iteration and avoid having identities in the set
                    // and make sure that we only generate similaritiesAboveThreshold for identical type patterns
                    if ( !this.results.contains(sim) && this.pattern != pattern2 ) {
                        
                        if ( RdfLiveNews.CONFIG.getBooleanSetting("similarity", "checkDomainAndRange") ) {
                            if ( this.domainAndRangeMatch(this.pattern, pattern2) ) {

                                sim.setSimilarity(this.similarityMetric.calculateSimilarity(this.pattern, pattern2));
                                this.results.add(sim);
                            }
                        }
                        else {
                            
                            sim.setSimilarity(this.similarityMetric.calculateSimilarity(this.pattern, pattern2));
                            this.results.add(sim);
                        }
                    }
                    // we only count the patterns we actually create similarities for
                    progress++;
                }
//            }
        }
        return this.results;
    }
    
    /**
     * 
     * @param pattern1
     * @param pattern2
     * @return
     */
    private boolean domainAndRangeMatch(Pattern pattern1, Pattern pattern2) {

        Set<String> typesEntityOne = new HashSet<String>(Arrays.asList(pattern1.getFavouriteTypeFirstEntity(), pattern1.getFavouriteTypeSecondEntity()));
        Set<String> typesEntityTwo = new HashSet<String>(Arrays.asList(pattern2.getFavouriteTypeFirstEntity(), pattern2.getFavouriteTypeSecondEntity()));
        
        typesEntityOne.removeAll(typesEntityTwo);
        
        return typesEntityOne.isEmpty();
    }
    
    /* ########################################################### */
    /* ################# Statistics and Monitoring ############### */
    /* ########################################################### */
    
    /**
     * @return the number of sentences this threads needs to process
     */
    public int getNumberTotal() {
        
        int count = 0;
        for ( Pattern p : this.patterns ) if ( p.isAboveThresholds() ) count++;
        return count;
    }

    /**
     * @return how many sentence have been processed already
     */
    public int getNumberDone() {

        return this.progress;
    }

    /**
     * @return the progress as a value between 0 and 1
     */
    public double getProgress() {

        double progress = (double) this.progress / getNumberTotal();
        return Double.isNaN(progress) || Double.isInfinite(progress) ? 0 : progress;
    }

    /**
     * @return the name of this pattern searcher
     */
    public String getName() {

        return this.name;
    }
}
