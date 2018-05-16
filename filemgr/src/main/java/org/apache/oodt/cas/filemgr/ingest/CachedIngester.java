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

//JDK imports

import org.apache.oodt.cas.filemgr.structs.exceptions.CacheException;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An extension of the {@link StdIngester} that uses a {@link Cache} to keep
 * track of {@link org.apache.oodt.cas.filemgr.structs.Product} ingestion status. If the existing {@link Cache} used
 * is already sync'ed to the requested File Manager (specified by the
 * <code>fmUrl</code> parameter in {@link #hasProduct(URL, File)} or
 * {@link #hasProduct(URL, String)}), then the {@link Cache} will simply return
 * the value of {@link Cache#contains(String)}. Otherwise the {@link Cache}
 * will be re-{@link Cache#sync()}'ed to the given File Manager before the
 * contains method is invoked.
 * </p>.
 */
public class CachedIngester extends StdIngester {

    private Cache cache;

    private static final Logger LOG = Logger.getLogger(CachedIngester.class
            .getName());

    /**
     * @param transferService
     *            The underlying data transfer service to use to ingest
     *            {@link org.apache.oodt.cas.filemgr.structs.Product}s.
     * @param cacheServiceFactory
     *            The {@link CacheFactory} to use to construct this
     *            {@link Ingester}'s {@link Cache}.
     * @param cachePropFile
     *            The file path to the cache properties file to load to
     *            configure the {@link Cache}.
     */
    public CachedIngester(String transferService, String cacheServiceFactory,
            String cachePropFile) throws InstantiationException {
        super(transferService);

        try {
            InputStream is = new FileInputStream(cachePropFile);
          try {
            System.getProperties().load(is);
          }
          finally{
            is.close();
          }
        } catch (Exception e) {
            throw new InstantiationException(
                    "Unable to load cache properties from file: ["
                            + cachePropFile + "]");
        }

        this.cache = GenericFileManagerObjectFactory
                .getCacheFromFactory(cacheServiceFactory);
        init(this.cache);

    }

    /**
     * 
     * @param transferService
     *            The underlying data transfer service to use to ingest
     *            {@link org.apache.oodt.cas.filemgr.structs.Product}s.
     * @param cache
     *            The {@link Cache} that this {@link Ingester} will use.
     * @throws InstantiationException
     *             If any error occurs.
     */
    public CachedIngester(String transferService, Cache cache)
            throws InstantiationException {
        super(transferService);
        init(cache);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.StdIngester#hasProduct(java.net.URL,
     *      java.io.File)
     */
    public boolean hasProduct(URL fmUrl, File prodFile) throws CatalogException {
        return hasProduct(fmUrl, prodFile.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.StdIngester#hasProduct(java.net.URL,
     *      java.lang.String)
     */
    public boolean hasProduct(URL fmUrl, String productName)
            throws CatalogException {
      try {
        if (cache.getFileManagerUrl().toURI().equals(fmUrl.toURI())) {
            return cache.contains(productName);
        } else {
            // need to re-sync
            cache.setFileManager(fmUrl);
            try {
                cache.sync();
            } catch (CacheException e) {
                LOG.log(Level.WARNING,
                        "Exception re-syncing cache to file manager: [" + fmUrl
                                + "]: Message: " + e.getMessage());
                throw new CatalogException(
                        "Exception re-syncing cache to file manager: [" + fmUrl
                                + "]: Message: " + e.getMessage(), e);
            }
            return cache.contains(productName);
        }
      } catch (URISyntaxException e) {
        LOG.log(Level.SEVERE, "Exception getting URI from URL");
        throw new CatalogException("Exception getting URL from URL: Message: " + e.getMessage(), e);
      }
    }

    /**
     * 
     * @throws CacheException
     */
    public void resynsc() throws CacheException {
        cache.sync();
    }

    private void init(Cache cache) throws InstantiationException {
        this.cache = cache;
        // upon inception, do an initial sync
        try {
            cache.sync();
        } catch (CacheException e) {
            throw new InstantiationException(
                    "Unable to sync cache for CachedIngester: Message: "
                            + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        cache.clear();
        super.close();
    }
}
