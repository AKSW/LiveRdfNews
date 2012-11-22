/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.uri.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultUriRetrieval implements UriRetrieval {

    private final LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager();
    
    @Override
    public Map<String, String> getUris(String text, List<String> labels) {
        HashMap<String, String> output = new HashMap<>();
        for(String l: labels)
        output.put(l, luceneRefinementManager.getPossibleUri(l));
        return output;
    }
    
    public static void main(String[] args) {

        try {
            
            RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        }
        catch (InvalidFileFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(new LuceneRefinementManager().getPossibleUri("Mississippi"));
        System.out.println(new LuceneRefinementManager().getPossibleUri("Environmental Protection"));
        System.out.println(new LuceneRefinementManager().getPossibleUri("Prince Edward Island"));
    }
}
