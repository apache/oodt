/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.curation.configuration;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
    public static final String ARCHIVE_AREA_CONFIG = "org.apache.oodt.cas.curator.archive.area";
    public static final String METADATA_AREA_CONFIG = "org.apache.oodt.cas.curator.metadata.area";
    public static final String EXTRACTOR_AREA_CONFIG = "org.apache.oodt.cas.curator.extractor.area";
    public static final String FILEMANAGER_URL_CONFIG = "org.apache.oodt.cas.curator.filemanager.url";
    public static final String FILEMANAGER_PROP_CONFIG = "org.apache.oodt.cas.curator.filemanager.prop";
    public static final String DIRECTORYBACKEND_VALIDATOR = "org.apache.oodt.cas.curator.directory.validator";
    public static final String POLICY_UPLOAD_PATH = "org.apache.oodt.cas.curator.dataDefinition.uploadPath";
    public static final String AWS_BUCKET_CONFIG = "org.apache.oodt.cas.curator.bucket.name";

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

    public static Map<String,String> getAllProperties() {
        Enumeration e = config.propertyNames();
        Map<String,String> generatedproperties = new HashMap<String, String>();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            generatedproperties.put(key, getWithReplacement(key));
        }
        return generatedproperties;
    }

}
