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
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public abstract class AbstractCacheServerFactory implements CacheFactory {

    protected String rangeQueryElementName;

    protected String rangeStartDateTime;

    protected String rangeEndDateTime;

    protected String uniqueElementName;

    protected List<String> productTypeNames;

    protected URL fmUrl;

    public AbstractCacheServerFactory() throws InstantiationException {
        rangeQueryElementName = System
                .getProperty("org.apache.oodt.cas.filemgr.ingest.cache.rangeQueryElementName");

        // before we replace env vars, try to replace date vars
        rangeStartDateTime = System
                .getProperty("org.apache.oodt.cas.filemgr.ingest.cache.range.start");
        rangeEndDateTime = System
                .getProperty("org.apache.oodt.cas.filemgr.ingest.cache.range.end");

        try {
            rangeStartDateTime = PathUtils
                    .doDynamicReplacement(rangeStartDateTime);
            rangeEndDateTime = PathUtils.doDynamicReplacement(rangeEndDateTime);
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }

        uniqueElementName = System
                .getProperty("org.apache.oodt.cas.filemgr.ingest.cache.uniqueElementName");
        productTypeNames = Arrays.asList(PathUtils.replaceEnvVariables(
                System.getProperty("org.apache.oodt.cas.filemgr.ingest."
                        + "cache.productType")).split(","));

        try {
            fmUrl = new URL(
                    System
                            .getProperty("org.apache.oodt.cas.filemgr.ingest.cache.filemgr.url"));
        } catch (MalformedURLException e) {
            throw new InstantiationException(
                    "Unable to construct file manager url for: ["
                            + System
                                    .getProperty("org.apache.oodt.cas.filemgr.ingest.cache.filemgr.url")
                            + "]: malformed URL exception.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.CacheFactory#createCache()
     */
    public abstract Cache createCache() throws InstantiationException;

}
