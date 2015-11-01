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


package org.apache.oodt.cas.metadata;

//OODT imports
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//JDK imports
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A core {@link MetExtractor} implementation, implementing all methods but {{@link #extractMetadata(File)}
 * </p>.
 */
public abstract class AbstractMetExtractor implements MetExtractor {

    protected MetExtractorConfig config;

    protected MetExtractorConfigReader reader;

    protected static final Logger LOG = Logger
            .getLogger(AbstractMetExtractor.class.getName());

    public AbstractMetExtractor(MetExtractorConfigReader reader) {
        this.reader = reader;
    }

    /**
     * Extracts {@link Metadata} from the given {@link File}
     * 
     * @param file
     *            The {@link File} from which {@link Metadata} will be extracted
     * @return The {@link Metadata} extracted
     * @throws MetExtractionException
     *             If any error occurs
     */
    protected abstract Metadata extrMetadata(File file)
            throws MetExtractionException;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File)
     */
    public Metadata extractMetadata(File f) throws MetExtractionException {
        if (f == null || !f.exists()) {
            throw new MetExtractionException("File '" + f + "' does not exist");
        }
        return this.extrMetadata(this.safeGetCanonicalFile(f));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.lang.String)
     */
    public Metadata extractMetadata(String filePath)
            throws MetExtractionException {
        return extractMetadata(new File(filePath));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.net.URL)
     */
    public Metadata extractMetadata(URL fileUrl) throws MetExtractionException {
        try {
            return this.extractMetadata(this.safeGetFileFromUri(fileUrl));
        } catch (Exception e) {
            throw new MetExtractionException(
                    "Failed to extract metadata from URL '" + fileUrl + "' : "
                            + e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File,
     *      java.io.File)
     */
    public Metadata extractMetadata(File f, File configFile)
            throws MetExtractionException {
        this.setConfigFile(configFile);
        return this.extractMetadata(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File,
     *      java.lang.String)
     */
    public Metadata extractMetadata(File f, String configFilePath)
            throws MetExtractionException {
        return extractMetadata(f, new File(configFilePath));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File,
     *      org.apache.oodt.cas.metadata.MetExtractorConfig)
     */
    public Metadata extractMetadata(File f, MetExtractorConfig config)
            throws MetExtractionException {
        this.setConfigFile(config);
        return this.extractMetadata(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#extractMetadata(java.net.URL,
     *      org.apache.oodt.cas.metadata.MetExtractorConfig)
     */
    public Metadata extractMetadata(URL fileUrl, MetExtractorConfig config)
            throws MetExtractionException {
        this.setConfigFile(config);
        return this.extractMetadata(fileUrl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#setConfigFile(java.io.File)
     */
    public void setConfigFile(File f) throws MetExtractionException {
        try {
            this.setConfigFile(this.reader.parseConfigFile(this
                    .safeGetCanonicalFile(f)));
        } catch (Exception e) {
            throw new MetExtractionException("Failed to parse config file : "
                    + e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#setConfigFile(org.apache.oodt.cas.metadata.MetExtractorConfig)
     */
    public void setConfigFile(MetExtractorConfig config) {
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractor#setConfigFile(java.lang.String)
     */
    public void setConfigFile(String filePath) throws MetExtractionException {
        setConfigFile(new File(filePath));
    }

    private File safeGetFileFromUri(URL url) {
        try {
            return new File(new URI(url.toExternalForm()));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception constructing file from uri: ["
                    + url + "]: Message: " + e.getMessage());
            return null;
        }
    }

    private File safeGetCanonicalFile(File f) {
        try {
            return f.getCanonicalFile();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception get canonical file for file : ["
                    + f + "]: Message: " + e.getMessage());
            return null;
        }
    }
}
