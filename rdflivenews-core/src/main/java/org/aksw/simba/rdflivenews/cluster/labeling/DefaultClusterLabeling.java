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
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.apache.commons.lang3.text.WordUtils;

import com.github.gerbsen.map.DistributionMap;
import com.github.gerbsen.map.MapUtil;

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
            
            DistributionMap<String> distribution = new DistributionMap<String>();
            
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
                
                distribution.addElement(nameSuggestion);
            }
            
            // the name of the cluster will be the most common string of all patterns
            // if this will every throw an error (indexoutofbounds) there is something wrong with the cluster
            String name = distribution.sort().get(distribution.size() - 1).getKey();
            
            cluster.setName(name);
            cluster.setUri(Constants.RDF_LIVE_NEWS_ONTOLOGY_PREFIX + WordUtils.uncapitalize(WordUtils.capitalize(name).replace(" ", "")));
            cluster.setRdfsRange(range);
            cluster.setRdfsDomain(domain);
        }
    }
}
