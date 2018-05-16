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

package org.apache.oodt.cas.filemgr.ingest;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.Closeable;
import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An interface for ingesting {@link Product}s
 * </p>.
 */
public interface Ingester extends Closeable {

    /**
     * Ingests a {@link Product} to the file manager service object identified
     * by the given {@link URL} parameter. The product {@link Metadata} is
     * extracted dynamically using the provided {@link MetExtractor} interface.
     * 
     * @param fmUrl
     *            The {@link URL} pointer to the file manager service.
     * @param prodFile
     *            The {@link File} pointer to the product file.
     * @param extractor
     *            The given {@link MetExtractor} to use to extract
     *            {@link Metadata} from the {@link Product}.
     * @param metConfFile
     *            A Config{@link File} for the {@link MetExtractor}.
     * @return The ID returned by the file manager for the newly ingested
     *         product.
     * @throws IngestException
     *             If there is an error ingesting the {@link Product}
     */
    String ingest(URL fmUrl, File prodFile, MetExtractor extractor,
                  File metConfFile) throws IngestException;

    /**
     * Ingests a {@link Product} to the file manager service object identified
     * by the given {@link URL} parameter. The product {@link Metadata} is
     * provided a priori.
     * 
     * @param fmUrl
     *            The {@link URL} pointer to the file manager service.
     * @param prodFile
     *            The {@link File} pointer to the product file.
     * @param met
     *            The given {@link Metadata} object already extracted from the
     *            {@link Product}.
     * @return The ID returned by the file manager for the newly ingested
     *         product.
     * @throws IngestException
     *             If there is an error ingesting the {@link Product}
     */
    String ingest(URL fmUrl, File prodFile, Metadata met)
            throws IngestException;

    /**
     * 
     * @param fmUrl
     *            The {@link URL} pointer to the file manager service.
     * @param prodFiles
     *            A {@link List} of {@link String} filePaths pointing to
     *            {@link Product} files to ingest.
     * @param extractor
     *            The given {@link MetExtractor} to use to extract
     *            {@link Metadata} from the {@link Product}s.
     * @param metConfFile
     *            A Config{@link File} for the {@link MetExtractor}.
     * @throws IngestException
     *             If there is an error ingesting the {@link Product}s.
     */
    void ingest(URL fmUrl, List<String> prodFiles, MetExtractor extractor,
                File metConfFile);

    /**
     * Checks the file manager at the given {@link URL} to see whether or not it
     * knows about the provided {@link Product} {@link File} parameter. To do
     * this, it uses {@link File#getName()} as the {@link Metadata} key
     * <code>Filename</code>.
     * 
     * @param prodFile
     *            The {@link File} to check for existance of within the file
     *            manager at given {@link URL}.
     * @url The {@link URL} pointer to the file manager service.
     * @return
     */
    boolean hasProduct(URL fmUrl, File prodFile) throws CatalogException;

    /**
     * Checks the file manager at the given {@link URL} to see whether or not it
     * knows about the provided {@link Product} with the given
     * <code>productName</code> parameter. To do this, it uses the provided
     * <code>productName</code> key as the {@link Metadata} key to search for
     * in the catalog.
     * 
     * @param fmUrl
     *            The {@link URL} pointer to the file manager service.
     * @param productName
     *            The {@link Product} to search for, identified by its (possibly
     *            not unique) name.
     * @return True if the file manager has the product, false otherwise.
     */
    boolean hasProduct(URL fmUrl, String productName) throws CatalogException;

}
