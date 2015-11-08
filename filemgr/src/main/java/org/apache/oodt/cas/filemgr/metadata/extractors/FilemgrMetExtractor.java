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

package org.apache.oodt.cas.filemgr.metadata.extractors;

//JDK imports
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The core interface for {@link Metadata} extraction on the File Manager server
 * side.
 * </p>.
 */
public interface FilemgrMetExtractor {

    /**
     * Extracts {@link Metadata} from the given {@link Product}.
     * 
     * @param product
     *            The given {@link Product}.
     * @param met
     *            The original {@link Metadata} provided during ingestion.
     * @return Extracted {@link Metadata} derived from the existing
     *         {@link Metadata} and {@link Product} provided.
     */
    Metadata extractMetadata(Product product, Metadata met)
            throws MetExtractionException;

    /**
     * Sets the configuration for this Metadata extractor.
     * 
     * @param props
     *            The {@link Properties} object to configure this Metadata
     *            extractor with.
     */
    void configure(Properties props);
}
