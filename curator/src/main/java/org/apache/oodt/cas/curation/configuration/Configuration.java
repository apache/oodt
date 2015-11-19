package org.apache.oodt.cas.curation.configuration;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * Singleton to hold configuration for the curation
 * @author starchmd
 */
public class Configuration {
    //Config names
    public static final String FILLER_METDATA_KEY = "fill";
    public static final String UPLOAD_AREA_CONFIG = "org.apache.oodt.cas.curator.upload.area";
    public static final String STAGING_AREA_CONFIG = "org.apache.oodt.cas.curator.staging.area";
    public static final String METADATA_AREA_CONFIG = "org.apache.oodt.cas.curator.metadata.area";
    public static final String EXTRACTOR_AREA_CONFIG = "org.apache.oodt.cas.curator.extractor.area";
    public static final String FILEMANAGER_URL_CONFIG = "org.apache.oodt.cas.curator.filemanager.url";
    public static final String FILEMANAGER_PROP_CONFIG = "org.apache.oodt.cas.curator.filemanager.prop";
    //Stores the configuration object as a Properties object
    private static Properties config = new Properties();
    
    /**
     * Loads a configuration given a servlet context
     * @param context - servlet context
     */
    @SuppressWarnings("unchecked")
    public static void loadConfiguration(ServletContext context) {
        //TODO: make sure all above values are loaded
        Enumeration<String> inits = context.getInitParameterNames();
        while(inits.hasMoreElements()) {
            String key = inits.nextElement();
            set(key,context.getInitParameter(key));
        }
    }
    /**
     * Accessor to wrap properties object implementation
     * @param key - configuration key to access
     * @return value of configuration
     */
    public static String get(String key) {
        String ret = config.getProperty(key);
        return ret;
    }
    /**
     * Get key with replacement of env vars
     * @param key - key to get
     * @return value with [VAR] replaced 
     */
    public static String getWithReplacement(String key) {
        String ret = PathUtils.replaceEnvVariables(get(key));
        return ret;
    }
    /**
     * Modifier for global configuration, private to prevent tampering
     * @param key - key to modify
     * @param val - value to modify
     */
    private static void set(String key,String val) {
        config.setProperty(key, val);
    }

}
