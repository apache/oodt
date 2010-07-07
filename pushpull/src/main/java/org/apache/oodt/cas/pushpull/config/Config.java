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


package org.apache.oodt.cas.pushpull.config;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ConfigException;
import org.apache.oodt.cas.pushpull.objectfactory.PushPullObjectFactory;
import org.apache.oodt.cas.pushpull.retrievalsystem.RemoteFileMetKeys;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.metadata.util.PropertiesUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class will parse a java .properties file for FileRetrievalSystem so that
 * information can be easily accessed through getter methods.
 * 
 * @author bfoster
 * 
 */
public class Config implements ConfigMetKeys {

    private ProtocolInfo pi;

    private ParserInfo parserInfo;

    private Ingester ingester;

    private URL fmUrl;

    private String productTypeDetectionFile;

    private boolean useTracker;

    private boolean onlyDefinedTypes;

    private int recommendedThreadCount;

    private int maxFailedDownloads;

    private boolean writeMetFile;
    
    private String metFileExtension;

    private String[] listOfMetadataToOutput;

    private File baseStagingArea;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(Config.class.getName());

    /**
     * Constructor
     * 
     * @param fileName
     *            Name of the configuration file to parse
     */
    public Config() {
        pi = new ProtocolInfo();
        parserInfo = new ParserInfo();
        this.maxFailedDownloads = 10;
        this.recommendedThreadCount = 8;
        listOfMetadataToOutput = new String[] { RemoteFileMetKeys.PRODUCT_NAME,
                RemoteFileMetKeys.RETRIEVED_FROM_LOC,
                RemoteFileMetKeys.DATA_PROVIDER, RemoteFileMetKeys.FILE_SIZE,
                RemoteFileMetKeys.PRODUCT_TYPE };
        int i = 1;
        do {
            this.baseStagingArea = new File("./" + STAGING_AREA_PREFIX + "_"
                    + i++);
        } while (this.baseStagingArea.exists());
    }

    /**
     * Opens the configuration file and adds its properties to System properties
     * 
     * @return
     */
    public void loadConfigFile(File configFile) throws ConfigException {

        // load properties from configuration file
        try {
            System.getProperties().load(new FileInputStream(configFile));
        } catch (Exception e) {
            throw new ConfigException("Failed to load properties file : "
                    + e.getMessage());
        }

        // parse properties and xml file specified in config file
        try {
            loadProperties();
        } catch (Exception e) {
            throw new ConfigException(
                    "Failed to get properties from properties file : "
                            + e.getMessage());
        }
    }

    public void setProtocolInfo(ProtocolInfo pi) {
        this.pi = pi;
    }

    public void setUseTracker(boolean useTracker) {
        this.useTracker = useTracker;
    }

    public void setIngester(Ingester ingester) {
        this.ingester = ingester;
    }
    
    public void setFmUrl(URL fmUrl){
        this.fmUrl = fmUrl;
    }

    public void setTempInfoFileExtension(String extension) {
        this.metFileExtension = extension;
    }

    public void setRecommendedThreadCount(int count) {
        this.recommendedThreadCount = count;
    }

    public void setMaxAllowedFailedDownloads(int max) {
        this.maxFailedDownloads = max;
    }

    public void setOnlyDownloadDefinedTypes(boolean onlyDefinedTypes) {
        this.onlyDefinedTypes = onlyDefinedTypes;
    }

    public void setBaseStagingArea(File baseStagingArea) {
        this.baseStagingArea = baseStagingArea;
    }

    public void setProductTypeDetectionFile(String filePath) {
        this.productTypeDetectionFile = filePath;
    }
    
    public void setWriteMetFile(boolean writeMetFile) {
    	this.writeMetFile = writeMetFile;
    }

    /**
     * Parses the properties which were added to System properties by
     * loadConfigFile(). Stores info for access by getter methods
     * 
     * @throws ConfigException
     * @throws InstantiationException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws ClassNotFoundException
     */
    void loadProperties() throws ConfigException, InstantiationException,
            FileNotFoundException, IOException, ClassNotFoundException {
        this.loadExternalConfigFiles();
        this.loadProtocolTypes();
        this.loadParserInfo();
        this.loadIngester();
        this.loadMiscVariables();
        this.loadProductTypeDetection();
        this.loadMetadataListToOutput();
    }

    void loadExternalConfigFiles() throws ConfigException {
        String[] externalConfigs = PropertiesUtils
                .getProperties(EXTERNAL_PROPERTIES_FILES);
        for (String externalConfig : externalConfigs) {
            try {
                System.getProperties().load(
                        new FileInputStream(new File(externalConfig)));
            } catch (Exception e) {
                throw new ConfigException("Failed to load default config file "
                        + externalConfig + " : " + e.getMessage());
            }
        }
    }

    void loadProtocolTypes() throws ConfigException {
        LOG.log(Level.INFO,
                "Associating protocol types with ProtocolFactories . . .");
        String[] protocolFactoryInfoFiles = PropertiesUtils
                .getProperties(PROTOCOL_FACTORY_INFO_FILES);
        for (String file : protocolFactoryInfoFiles) {
            try {
                pi.loadProtocolFactoryInfoFromFile(new File(file));
            } catch (Exception e) {
                throw new ConfigException(
                        "Failed to load ProtocolFactory config file " + file
                                + " : " + e.getMessage());
            }
        }
    }

    void loadParserInfo() throws ConfigException {
        LOG.log(Level.INFO, "Associating parsers with RetrievalMethods . . .");
        String[] parserInfoFiles = PropertiesUtils
                .getProperties(PARSER_INFO_FILES);
        for (String file : parserInfoFiles) {
            try {
                parserInfo.loadParserInfo(new File(file));
            } catch (Exception e) {
                throw new ConfigException("Failed to load parser info file "
                        + file + " : " + e.getMessage());
            }
        }
    }

    void loadIngester() throws InstantiationException, ConfigException {
        try {
            String fmUrlStr = PropertiesUtils.getProperties(INGESTER_FM_URL,
                    new String[] { NO_FM_SPECIFIED })[0];

            if (fmUrlStr.equals(NO_FM_SPECIFIED)) {
                LOG
                        .log(Level.INFO,
                                "No file manager url specified: no ingester will be used");
            } else {
                String ingesterClass = PropertiesUtils
                        .getProperties(INGESTER_CLASS)[0];
                String dataTransferClass = PropertiesUtils
                        .getProperties(INGESTER_DATA_TRANSFER)[0];
                String cacheFactoryClass = System.getProperty(CACHE_FACTORY_CLASS);

                LOG.log(Level.INFO, "Configuring and building ingester: ["
                        + ingesterClass + "]: data transfer: ["
                        + dataTransferClass + "]: to ingest to file manager: ["
                        + fmUrlStr + "]");

                if (cacheFactoryClass != null) {
                    LOG.log(Level.INFO, "Configuring Ingester cache: ["
                            + cacheFactoryClass + "]");
                }

                this.ingester = PushPullObjectFactory.createIngester(
                        ingesterClass, cacheFactoryClass);
                
                this.fmUrl = safeGetUrlFromString(fmUrlStr);

            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigException("Failed to load Ingester : "
                    + e.getMessage());
        }
    }

    void loadProductTypeDetection() throws ConfigException {
        try {
            this.productTypeDetectionFile = PropertiesUtils
                    .getProperties(TYPE_DETECTION_FILE)[0];
            LOG.log(Level.INFO, "Loading product type detection file: "
                    + productTypeDetectionFile);
        } catch (Exception e) {
            throw new ConfigException(
                    "Failed to load ProductTypeDetection file '"
                            + productTypeDetectionFile + "' : "
                            + e.getMessage());
        }
    }

    void loadMetadataListToOutput() {
        listOfMetadataToOutput = PropertiesUtils.getProperties(
                MET_LIST_TO_PRINT, new String[] {
                        RemoteFileMetKeys.PRODUCT_NAME,
                        RemoteFileMetKeys.RETRIEVED_FROM_LOC,
                        RemoteFileMetKeys.DATA_PROVIDER,
                        RemoteFileMetKeys.FILE_SIZE,
                        RemoteFileMetKeys.PRODUCT_TYPE });
    }

    void loadMiscVariables() {
        onlyDefinedTypes = (PropertiesUtils.getProperties(
                ALLOW_ONLY_DEFINED_TYPES, new String[] { "false" })[0]
                .toLowerCase().equals("true"));
        useTracker = (PropertiesUtils.getProperties(USE_TRACKER,
                new String[] { "false" })[0].toLowerCase().equals("true"));
        this.recommendedThreadCount = Integer.parseInt(PropertiesUtils
                .getProperties(FILE_RET_SYSTEM_REC_THREAD_COUNT,
                        new String[] { "8" })[0]);
        this.maxFailedDownloads = Integer.parseInt(PropertiesUtils
                .getProperties(FILE_RET_SYSTEM_MAX_ALLOWED_FAIL_DOWNLOADS,
                        new String[] { "10" })[0]);
        metFileExtension = PropertiesUtils.getProperties(MET_FILE_EXT,
                new String[] { "info.tmp" })[0];
        this.writeMetFile = Boolean.getBoolean(WRITE_MET_FILE);
        String timeoutString = PropertiesUtils.getProperties(
                PROTOCOL_TIMEOUT_MS, new String[] { "600000" })[0];
        if (timeoutString == null)
            timeoutString = "0";
        pi.setDownloadTimeout(Long.parseLong(timeoutString));
        pi.setPageSize(Integer.parseInt(PropertiesUtils.getProperties(
                PROTOCOL_PAGE_SIZE, new String[] { "8" })[0]));
        this.baseStagingArea = new File(PropertiesUtils.getProperties(
                DATA_FILE_BASE_STAGING_AREA,
                new String[] { this.baseStagingArea.getPath() })[0]);
    }

    public ProtocolInfo getProtocolInfo() {
        return this.pi;
    }

    public Ingester getIngester() {
        return this.ingester;
    }
    
    public URL getFmUrl(){
        return this.fmUrl;
    }

    public boolean useTracker() {
        return useTracker;
    }

    public String getMetFileExtension() {
        return this.metFileExtension;
    }

    public String getProductTypeDetectionFile() {
        return this.productTypeDetectionFile;
    }

    public int getRecommendedThreadCount() {
        return this.recommendedThreadCount;
    }

    public int getMaxFailedDownloads() {
        return this.maxFailedDownloads;
    }

    public boolean onlyDownloadDefinedTypes() {
        return this.onlyDefinedTypes;
    }

    public String[] getListOfMetadataToOutput() {
        return this.listOfMetadataToOutput;
    }

    public ParserInfo getParserInfo() {
        return this.parserInfo;
    }

    public File getBaseStagingArea() {
        return this.baseStagingArea;
    }
    
    public boolean getWriteMetFile() {
    	return this.writeMetFile;
    }

    public Config clone() {
        Config config = new Config();
        config.baseStagingArea = this.baseStagingArea;
        config.ingester = this.ingester;
        config.fmUrl = this.fmUrl;
        config.listOfMetadataToOutput = this.listOfMetadataToOutput;
        config.maxFailedDownloads = this.maxFailedDownloads;
        config.metFileExtension = this.metFileExtension;
        config.onlyDefinedTypes = this.onlyDefinedTypes;
        config.parserInfo = this.parserInfo;
        config.pi = this.pi;
        config.productTypeDetectionFile = this.productTypeDetectionFile;
        config.recommendedThreadCount = this.recommendedThreadCount;
        config.useTracker = this.useTracker;
        config.writeMetFile = this.writeMetFile;
        return config;
    }

    private static URL safeGetUrlFromString(String urlStr) {
        URL url = null;

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            LOG.log(Level.WARNING, "Unable to generate url from url string: ["
                    + urlStr + "]: Message: " + e.getMessage());
        }

        return url;
    }

}
