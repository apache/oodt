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


package org.apache.oodt.cas.metadata.extractors;

//JDK imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;

//OODT imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Met Extractor that assumes that the .met file has already been generated.
 * This extractor assumes the presence of the first parameter in
 * {@link #extractMetadata(File, File)}, File.getAbsolutePath()+".met"
 * </p>.
 */
public class MetReaderExtractor extends CmdLineMetExtractor {

    /* the extension (e.g., .met) for the met file */
    private String metFileExt;

    public static final String DEFAULT_MET_FILE_EXT = "met";

    protected static MetReaderConfigReader reader = new MetReaderConfigReader();

    /**
     * Default Constructor
     */
    public MetReaderExtractor() {
        this(DEFAULT_MET_FILE_EXT);
    }

    /**
     * Constructs a new MetReaderExtractor that looks for met files with the
     * given <code>metFileExt</code>.
     * 
     * @param metFileExt
     *            The met file extension.
     */
    public MetReaderExtractor(String metFileExt) {
        super(reader);
        this.metFileExt = metFileExt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.AbstractMetExtractor#extractMetadata(java.io.File)
     */
    public Metadata extrMetadata(File file) throws MetExtractionException {
        // don't really need conf file, we're assuming that there is an existing
        // .met file
        // for the given product file
    	String extension = this.metFileExt;
    	if (this.config != null) {
            extension = ((MetReaderConfig) this.config)
                .getProperty(
                    "org.apache.oodt.cas.metadata.extractors.MetReader.metFileExt",
                    this.metFileExt);
        }
        String metFileFullPath = file.getAbsolutePath() + "." + extension;
    	LOG.log(Level.INFO, "Reading metadata from " + metFileFullPath);
        // now read the met file and return it
        if (!new File(metFileFullPath).exists()) {
            throw new MetExtractionException("Met file: [" + metFileFullPath
                    + "] does not exist: failing!");
        }

        try {
            SerializableMetadata met = new SerializableMetadata("UTF-8", false);
            met.loadMetadataFromXmlStream(new FileInputStream(metFileFullPath));
            return met;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new MetExtractionException(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        processMain(args, new MetReaderExtractor());
    }

}
