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

package org.apache.oodt.opendapps.config;

//JDK imports
import java.net.URL;

/**
 * 
 * A set of root {@link URL} information for OPeNDAP/THREDDS catalogs to crawl
 * and obtain dataset metadata from.
 * 
 */
public class DapRoot {

  private URL datasetUrl;

  private URL catalogUrl;

  private String filter;

  public DapRoot() {
    this.datasetUrl = null;
    this.catalogUrl = null;
    this.filter = null;
  }

  /**
   * @return the datasetUrl
   */
  public URL getDatasetUrl() {
    return datasetUrl;
  }

  /**
   * @param datasetUrl
   *          the datasetUrl to set
   */
  public void setDatasetUrl(URL datasetUrl) {
    this.datasetUrl = datasetUrl;
  }

  /**
   * @return the catalogUrl
   */
  public URL getCatalogUrl() {
    return catalogUrl;
  }

  /**
   * @param catalogUrl
   *          the catalogUrl to set
   */
  public void setCatalogUrl(URL catalogUrl) {
    this.catalogUrl = catalogUrl;
  }

  /**
   * @return the filter
   */
  public String getFilter() {
    return filter;
  }

  /**
   * @param filter
   *          the filter to set
   */
  public void setFilter(String filter) {
    this.filter = filter;
  }

}
