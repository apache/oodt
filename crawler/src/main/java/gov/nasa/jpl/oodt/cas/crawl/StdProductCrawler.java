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


package gov.nasa.jpl.oodt.cas.crawl;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.extractors.MetReaderExtractor;

//JDK imports
import java.io.File;
import java.util.logging.Level;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A generic Product Crawler for Products. The Crawler is given a root Product
 * Path and it searches through all directories and sub-directories for .met
 * files, which it uses to determine products to ingest into the file manger.
 * The important .met file fields that this crawler requires are:
 * 
 * <ul>
 * <li><code>FileLocation</code>: directory absolute path to location of
 * product file</li>
 * <li><code>Filename</code>: name of the product file to ingest</li>
 * <li><code>ProductType</code>: the ProductType that will be sent to the
 * file manager for the product file described by the .met file.</li>
 * </ul>
 * 
 * </p>
 * 
 */
public class StdProductCrawler extends ProductCrawler {

    String metFileExtension;

    public StdProductCrawler() {
    	this.metFileExtension = "met";
    }

    protected Metadata getMetadataForProduct(File product) {
        try {
            MetReaderExtractor extractor = new MetReaderExtractor(this.metFileExtension);
            return extractor.extractMetadata(product);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get metadata for " + product
                    + " : " + e.getMessage());
            return new Metadata();
        }
    }

    protected boolean passesPreconditions(File product) {
        return new File(product.getAbsolutePath() + "." + this.metFileExtension)
                .exists();
    }

    public void setMetFileExtension(String metFileExtension) {
    	this.metFileExtension = metFileExtension;
    }

}
