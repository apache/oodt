package org.apache.oodt.cas.curation.configuration;

import java.util.Properties;

/**
 * Singleton to hold configuration for the curation
 * @author starchmd
 * TODO: un-hardcode this
 */
public class Configuration {
    //Config names
    public static final String UPLOAD_AREA_CONFIG = "upload.area";
    public static final String STAGING_AREA_CONFIG = "staging.area";

    //Stores the configuration object as a Properties object
    public static Properties config = new Properties();
    
    static
    {
        config.put(Configuration.STAGING_AREA_CONFIG,"/Users/mstarch/Documents");
        config.put(Configuration.UPLOAD_AREA_CONFIG, config.get(Configuration.STAGING_AREA_CONFIG));
    }
    /**
     * Accessor to wrap properties object implementation
     * @param key - configuration key to access
     * @return value of configuration
     */
    public static String get(String key) {
        return config.getProperty(key);
    }
}
