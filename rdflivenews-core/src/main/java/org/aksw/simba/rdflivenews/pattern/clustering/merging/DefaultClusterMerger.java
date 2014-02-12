/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.clustering.merging;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.simba.rdflivenews.util.Encoder.Encoding;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultClusterMerger implements ClusterMerger {

    private final double threshold = 0.85;
    @Override
    public void mergeCluster(Set<Cluster<Pattern>> clusters) {
        boolean merged = true;
//        while(merged) {
            merged = false;
            Set<Cluster<Pattern>> mergedCluster = new HashSet<Cluster<Pattern>>();
            
            for ( Iterator<Cluster<Pattern>> iter1 = new HashSet<Cluster<Pattern>>(clusters).iterator(); iter1.hasNext();) {
                Cluster<Pattern> clusterOne = iter1.next();
                
//                if ( clusterOne.size() <= 1 ) continue;
                
                for ( Iterator<Cluster<Pattern>> iter2 = new HashSet<Cluster<Pattern>>(clusters).iterator(); iter2.hasNext();) {
                    Cluster<Pattern> clusterTwo = iter2.next();
                    
//                    if ( clusterTwo.size() <= 1 ) continue;
                    
//                    System.out.println("Comparing: " + clusterOne.getName() + " & " + clusterTwo.getName());
                    
                    if ( !clusterOne.equals(clusterTwo) ) {
                        
                        double sim = 0D;

                        // comparing the same names will result in double max similarity
                        if ( clusterOne.getName().equals(clusterTwo.getName() ) ) sim = 1D; 
                        else     
                            sim = Wordnet.getInstance().getWordnetSimilarity(
                                    clusterOne.getName(), clusterTwo.getName(), WordnetSimilarity.PATH);
                        if(sim > threshold) {
                           merged = true; 
                           clusterOne.addAll(clusterTwo);
                           mergedCluster.add(clusterOne);
                           clusters.remove(clusterOne);
                           clusters.remove(clusterTwo);
//                           System.out.println("Merging " + clusterOne.getName() + " and " + clusterTwo.getName() + 
//                                   "\nResult:\n" + clusterOne);
                           for ( Pattern p : clusterOne ) {
//                               System.out.println(p.getNaturalLanguageRepresentation());
                           }
                           
                        } else {
//                            System.out.println(sim);
                        }
                        
                    }
                }
            }
            clusters.addAll(mergedCluster);
//        }
        
        if ( RdfLiveNews.CONFIG.getBooleanSetting("clustering", "writeFile") ) {
            
            String fileName = RdfLiveNews.DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "clusters");
            fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
            fileName += "merged-iter-#" + RdfLiveNews.ITERATION + "-";
            fileName += this.getClass().getSimpleName() + "-" + RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
            fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") + ".clstr";
            
            BufferedFileWriter writer = new BufferedFileWriter(fileName, Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
            for ( Cluster<Pattern> cluster : clusters) {
             
                writer.write(cluster.getName() + " ("+ cluster.getUri() +") ");
                writer.write("rdfs:domain > " + cluster.getRdfsDomain() );
                writer.write("rdfs:range > " + cluster.getRdfsRange() + "\n\n");
                
                for ( Pattern pattern : cluster ) {

                    writer.write("\t" + pattern.getNaturalLanguageRepresentation());
                }
                
                writer.write("\n");
            }
            writer.close();
        }
    }
}
