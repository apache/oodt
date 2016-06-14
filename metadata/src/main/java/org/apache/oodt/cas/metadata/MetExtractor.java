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
import java.net.URL;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An interface for {@link Metadata} extraction. This interface expects the
 * definition of the following two parameters:
 * 
 * <ul>
 * <li><b>file</b> - the file to extract {@link Metadata} from.</li>
 * <li><b>config file</b> - a pointer to the config file for this MetExtractor</li>
 * </ul>
 * </p>
 * 
 */
public interface MetExtractor {

    /**
     * Extracts {@link Metadata} from a given {@link File}.
     * 
     * @param f
     *            File object to extract Metadata from.
     * @return Extracted {@link Metadata} from the given {@link File}.
     * @throws MetExtractionException
     *             If any error occurs.
     */
    Metadata extractMetadata(File f) throws MetExtractionException;

    /**
     * Extracts {@link Metadata} from a given <code>/path/to/some/file</code>.
     * 
     * @param filePath
     *            Path to a given file to extract Metadata from.
     * @return Extracted {@link Metadata} from the given <code>filePath</code>.
     * @throws MetExtractionException
     *             If any error occurs.
     */
    Metadata extractMetadata(String filePath)
            throws MetExtractionException;

    /**
     * Extracts {@link Metadata} from a given {@link URL} pointer to a
     * {@link File}.
     * 
     * @param fileUrl
     *            The URL pointer to a File.
     * @return Extracted {@link Metadata} from the given File {@link URL}.
     * @throws MetExtractionException
     *             If any error occurs.
     */
    Metadata extractMetadata(URL fileUrl) throws MetExtractionException;

    /**
     * Sets the config file for this MetExtractor to the specified {@link File}
     * <code>f</code>.
     * 
     * @param f
     *            The config file for this MetExtractor.
     * @throws MetExtractionException
     */
    void setConfigFile(File f) throws MetExtractionException;

    /**
     * Sets the config file for this MetExtractor to the specified {@link File}
     * identified by <code>filePath</code>.
     * 
     * @param filePath
     *            The config file path for this MetExtractor.
     * @throws MetExtractionException
     */
    void setConfigFile(String filePath) throws MetExtractionException;

    /**
     * Sets the MetExtractorConfig for the MetExtractor
     * 
     * @param config
     *            The MetExtractorConfig
     */
    void setConfigFile(MetExtractorConfig config);

    /**
     * Extracts {@link Metadata} from the given {@link File} using the specified
     * config file.
     * 
     * @param f
     *            The File to extract Metadata from.
     * @param configFile
     *            The config file for this MetExtractor.
     * @return Extracted {@link Metadata} from the given {@link File} using the
     *         specified config file.
     * @throws MetExtractionException
     *             If any error occurs.
     */
    Metadata extractMetadata(File f, File configFile)
            throws MetExtractionException;

    /**
     * Extracts {@link Metadata} from the given {@link File} using the specified
     * config file path.
     * 
     * @param f
     *            The File to extract Metadata from.
     * @param configFilePath
     *            The path to the config file for this MetExtractor.
     * @return Extracted {@link Metadata} from the given {@link File} using the
     *         specified config file path.
     * @throws MetExtractionException
     *             If any error occurs.
     */
    Metadata extractMetadata(File f, String configFilePath)
            throws MetExtractionException;

    /**
     * Extracts {@link Metadata} from the given {@link File} using the specified
     * {@link MetExtractorConfig}.
     * 
     * @param f
     *            The {@link File} from which {@link Metadata} will be extracted
     *            from
     * @param config
     *            The config file for the extractor
     * @return {@link Metadata} extracted from the {@link File}
     * @throws MetExtractionException
     *             If any error occurs
     */
    Metadata extractMetadata(File f, MetExtractorConfig config)
            throws MetExtractionException;

    /**
     * Extracts {@link Metadata} from the given {@link URL} using the specified
     * {@link MetExtractorConfig}.
     * 
     * @param fileUrl
     *            The {@link URL} from which {@link Metadata} will be extracted
     *            from
     * @param config
     *            The config file for the extractor
     * @return {@link Metadata} extracted from the {@link URL}
     * @throws MetExtractionException
     *             If any error occurs
     */
    Metadata extractMetadata(URL fileUrl, MetExtractorConfig config)
            throws MetExtractionException;
}
