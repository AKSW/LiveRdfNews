/**
 * 
 */
package org.aksw.simba.rdflivenews.config;

import java.io.File;

import org.ini4j.Ini;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Config {
    
    private Ini defactoConfig;
    public static String RDF_LIVE_NEWS_DATA_DIRECTORY;

    public Config(Ini config) {
        
        this.defactoConfig =  config;
        
        String dataDir = this.getStringSetting("general", "data-directory");
        RDF_LIVE_NEWS_DATA_DIRECTORY = dataDir.endsWith("/") ? dataDir : dataDir + System.getProperty("file.separator");

        if ( !new File(RDF_LIVE_NEWS_DATA_DIRECTORY).exists() ) 
            throw new RuntimeException("Data directory does not exist: "+ RDF_LIVE_NEWS_DATA_DIRECTORY);
        
        if ( !new File(RDF_LIVE_NEWS_DATA_DIRECTORY + this.getStringSetting("general", "index")).exists() )
            new File(RDF_LIVE_NEWS_DATA_DIRECTORY + this.getStringSetting("general", "index")).mkdir();
    }
    
    /**
     * returns boolean values from the config file
     * 
     * @param section
     * @param key
     * @return
     */
    public boolean getBooleanSetting(String section, String key) {

        return Boolean.valueOf(defactoConfig.get(section, key));
    }
    
    /**
     * returns string values from defacto config
     * 
     * @param section
     * @param key
     * @return
     */
    public String getStringSetting(String section, String key) {
        
        return defactoConfig.get(section, key);
    }

    /**
     * this should overwrite a config setting, TODO make sure that it does
     * 
     * @param string
     * @param string2
     */
    public void setStringSetting(String section, String key, String value) {

        this.defactoConfig.add(section, key, value);
    }

    /**
     * returns integer values for defacto setting
     * 
     * @param section
     * @param key
     * @return
     */
    public Integer getIntegerSetting(String section, String key) {

        return Integer.valueOf(this.defactoConfig.get(section, key));
    }

    /**
     * returns double values from the config
     * 
     * @param section
     * @param key
     * @return
     */
    public Double getDoubleSetting(String section, String key) {

        return Double.valueOf(this.defactoConfig.get(section, key));
    }

    /**
     * 
     * @param string
     * @param string2
     * @return
     */
    public long getLongSetting(String section, String key) {

        return Long.valueOf(this.defactoConfig.get(section, key));
    }
}
