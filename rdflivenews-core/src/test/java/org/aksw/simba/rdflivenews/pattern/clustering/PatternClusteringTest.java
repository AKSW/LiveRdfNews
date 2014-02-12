/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.clustering;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.ClusterLabeler;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.BorderFlowPatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.merging.ClusterMerger;
import org.aksw.simba.rdflivenews.pattern.clustering.merging.DefaultClusterMerger;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternClusteringTest extends TestCase {

    /**
    * Create the test case
    * 
    * @param testName
    *            name of the test case
    */
   public PatternClusteringTest(String testName) {

       super(testName);
   }

   /**
    * @return the suite of tests being tested
    */
   public static Test suite() {

       return new TestSuite(PatternClusteringTest.class);
   }

   public void testPatternClustering() throws InvalidFileFormatException, IOException {
       
       RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
       RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
       
       PatternClustering clustering = new BorderFlowPatternClustering();
       Set<Similarity> similarities = new HashSet<Similarity>();
       
	   for ( String line : FileUtil.readFileInList(RdfLiveNews.DATA_DIRECTORY + "/Users/gerb/test/pattern-sim.txt", null, null)){
//           for ( String line : FileUtils.readLines(new File("/Users/gerb/test/10percent/sim-patterns-nlr-QGramSimilarityMetric-0.5.tsv"))) {
           
           String[] lineParts = line.split("\t");
           similarities.add(new Similarity(new DefaultPattern(lineParts[0]), new DefaultPattern(lineParts[1]), Double.valueOf(lineParts[2])));
       }
       
       System.out.println("Found " + similarities.size() + " similarities!");
       Set<Cluster<Pattern>> clusters = clustering.clusterPatterns(similarities, 0.3D);
       System.out.println("We found " + clusters.size() + " clusters!");
       int  i = 0;
       
       ClusterLabeler clusterLabeler = new DefaultClusterLabeling();
       clusterLabeler.labelCluster(clusters);
       
//       ClusterMerger clusterMerger = new DefaultClusterMerger();
//       clusterMerger.mergeCluster(clusters);
       
       System.out.println("We found " + clusters.size() + " clusters after merging!");
       
       for (Cluster<Pattern> cluster : clusters) {
           
           System.out.println(cluster.getUri() + ": " +cluster.getName());

           for (Pattern p : cluster) {
                   i++;
                   System.out.println(p.getNaturalLanguageRepresentation());
           }
           
           System.out.println();
       }
       
//       System.out.println(i);
   }
}
