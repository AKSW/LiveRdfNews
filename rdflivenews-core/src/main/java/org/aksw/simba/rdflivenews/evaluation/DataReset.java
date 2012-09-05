/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.time.TimeUtil;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DataReset {

    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        long start = System.currentTimeMillis();
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        System.out.println("Resetting documents in index took " + TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start));
    }
}