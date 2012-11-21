/**
 * 
 */
package org.aksw.simba.rdflivenews.cluster.labeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.apache.commons.lang3.text.WordUtils;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.math.Frequency;

import edu.stanford.nlp.util.StringUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultClusterLabeling implements ClusterLabeler {

    @Override
    public void labelCluster(Set<Cluster<Pattern>> clusters) {

        // we need to label each cluster
        for ( Cluster<Pattern> cluster : clusters) {
            
            if ( cluster.isEmpty() ) throw new RuntimeException("A cluster may not be empty!");
            
            Frequency frequency = new Frequency();
            
            String domain = "";
            String range  = "";
            
            // we look at every cluster and remove trash and count the number of the cleaned pattern 
            for ( Pattern pattern : cluster ) {
                
                String nlr = pattern.getNaturalLanguageRepresentation();
                
                if ( domain.isEmpty() ) domain = pattern.getFavouriteTypeFirstEntity();
                if ( range.isEmpty() ) range   = pattern.getFavouriteTypeSecondEntity();

                // remove all the trash from the pattern
                List<String> nlrTokens = new ArrayList<String>(Arrays.asList(nlr.toLowerCase().split(" ")));
                nlrTokens.removeAll(Constants.STOP_WORDS);
                String nameSuggestion = StringUtils.join(nlrTokens, " ");
                
//                System.out.println(nlr +  " -> " + nameSuggestion);
                
                frequency.addValue(nameSuggestion);
            }
            
            // the name of the cluster will be the most common string of all patterns
            // if this will every throw an error (indexoutofbounds) there is something wrong with the cluster
            String name = (String) frequency.sortByValue().get(0).getKey();
            
            cluster.setName(name);
            cluster.setUri(Constants.RDF_LIVE_NEWS_ONTOLOGY_PREFIX + WordUtils.uncapitalize(WordUtils.capitalize(name).replace(" ", "")));
            cluster.setRdfsRange(range);
            cluster.setRdfsDomain(domain);
        }
        
        if ( RdfLiveNews.CONFIG.getBooleanSetting("clustering", "writeFile") ) {
            
            String fileName = RdfLiveNews.DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "clusters");
            fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
            fileName += "iter-#" + RdfLiveNews.ITERATION + "-";
            fileName += this.getClass().getSimpleName() + "-" + RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
            fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") + ".clstr";
            
            BufferedFileWriter writer = new BufferedFileWriter(fileName, "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
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
