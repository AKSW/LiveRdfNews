/**
 * 
 */
package org.aksw.simba.rdflivenews;

import java.io.File;

import org.aksw.simba.rdflivenews.config.Config;
import org.ini4j.Ini;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class RdfLiveNews {

    private static Config CONFIG;

    public static void main(String[] args) {

        // load the config, we dont need to configure logging because the log4j config is on the classpath
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/config.ini")));
    }
}
