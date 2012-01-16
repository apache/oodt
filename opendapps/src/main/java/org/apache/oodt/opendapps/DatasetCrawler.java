/**
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

package org.apache.oodt.opendapps;

//JDK imports
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.opendapps.extractors.MetadataExtractor;
import org.apache.oodt.opendapps.extractors.ThreddsMetadataExtractor;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvService;
import thredds.catalog.ServiceType;
import thredds.catalog.crawl.CatalogCrawler;

/**
 * Crawls a catalog and returns all the datasets and their references.
 * 
 */
public class DatasetCrawler implements CatalogCrawler.Listener {

  private static Logger LOG = Logger.getLogger(DatasetCrawler.class.getName());

  private List<String> urls = new Vector<String>();

  private Map<String, Metadata> datasetMet;

  private String datasetURL = null;

  public DatasetCrawler(String datasetURL) {
    this.datasetURL = datasetURL.endsWith("/") ? datasetURL : datasetURL + "/";
    this.datasetMet = new HashMap<String, Metadata>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * thredds.catalog.crawl.CatalogCrawler.Listener#getCatalogRef(thredds.catalog
   * .InvCatalogRef, java.lang.Object)
   */
  public boolean getCatalogRef(InvCatalogRef dd, Object context) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * thredds.catalog.crawl.CatalogCrawler.Listener#getDataset(thredds.catalog
   * .InvDataset, java.lang.Object)
   */
  public void getDataset(InvDataset dd, Object context) {
  	String url = this.datasetURL + dd.getCatalogUrl().split("#")[1];
    String id = dd.getID();    
    LOG.log(Level.INFO, url + " is the computed access URL for this dataset");
    // look for an OpenDAP access URL, only extract metadata if it is found
    List<InvAccess> datasets = dd.getAccess();
    if (dd.getAccess() != null && dd.getAccess().size() > 0) {
      Iterator<InvAccess> sets = datasets.iterator();
      while (sets.hasNext()) {
        InvAccess single = sets.next();
        InvService service = single.getService();
        // note: select the OpenDAP access URL based on THREDDS service type
        if (service.getServiceType()==ServiceType.OPENDAP) {
          LOG.log(Level.INFO, "Found OpenDAP access URL: "+ single.getUrlPath());
          String opendapurl = this.datasetURL + single.getUrlPath();
          // extract metadata from THREDDS catalog
          MetadataExtractor extractor = new ThreddsMetadataExtractor(dd);
          Metadata met = new Metadata();
          extractor.extract(met);
          // index metadata by opendap access URL
          this.datasetMet.put(opendapurl, met);
          this.urls.add(opendapurl);
          break;
        }
      }
    }
  }

  /**
   * Gets the set of String {@link URL}s crawled.
   * 
   * @return A {@link List} of {@link String} representations of {@link URL}s.
   */
  public List<String> getURLs() {
    return this.urls;
  }

  /**
   * Returns the exracted THREDDS {@link InvDataset} metadata. The dataset
   * metadata is mapped to the unique THREDDS dataset URL.
   * 
   * @return the exracted THREDDS {@link InvDataset} metadata.
   */
  public Map<String, Metadata> getDatasetMet() {
    return this.datasetMet;
  }

}
