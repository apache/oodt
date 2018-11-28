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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.opendapps.config.OpendapConfig;
import org.apache.oodt.xmlquery.XMLQuery;

//NetCDF-Java imports
import thredds.catalog.crawl.CatalogCrawler;
import ucar.nc2.util.CancelTask;
import opendap.dap.DConnect;
import opendap.dap.DataDDS;

/**
 * 
 * This class takes in a query and the main catalog url and returns a list of
 * relevant dataset urls
 * 
 */
public class DatasetExtractor {

  private static Logger LOG = Logger
      .getLogger(DatasetExtractor.class.getName());

  public static final String FINDALL = "PFunction=findall";

  public static final String FINDSOME = "PFunction=findsome";

  public static final String FINDQUERY = "PFunction=findquery";

  private String q;

  private String mainCatalogURL;

  private String datasetURL;

  private List<String> allUrls;

  private Map<String, Metadata> datasetMet;
  
  private OpendapConfig conf;

  public DatasetExtractor(XMLQuery q, String mainCatalogURL, String datasetURL, OpendapConfig conf) {
    this.q = q.getKwdQueryString().trim();
    this.mainCatalogURL = mainCatalogURL;
    this.datasetURL = datasetURL;
    this.conf = conf;
    this.initExtraction();
  }

  public List<String> getDapUrls() {
    List<String> urls = null;

    if (this.q.contains(FINDALL))
      urls = this.allUrls;
    else if (this.q.contains(FINDSOME))
      urls = this.getFindSome();
    else if (this.q.contains(FINDQUERY))
      urls = this.getFindQuery();

    return urls;
  }

  public Metadata getDatasetMet(String opendapUrl){
    return this.datasetMet.get(opendapUrl);
  }

  private void initExtraction() {
    DatasetCrawler listener = new DatasetCrawler(this.datasetURL, this.conf);
    CancelTask ignore = new CancelTask() {
      public boolean isCancel() {
        return false;
      }

      public void setError(String msg) {
        LOG.log(Level.WARNING, msg);
      }

    };

    LOG.log(Level.FINE, "catalogURL: " + this.mainCatalogURL);
    // Note: look for all datasets, that have either a urlPath="" attribute, or a <access> subelement
    CatalogCrawler crawler = new CatalogCrawler(CatalogCrawler.USE_ALL, false, listener);
    crawler.crawl(this.mainCatalogURL, ignore, System.out, this);
    this.allUrls = listener.getURLs();
    this.datasetMet = listener.getDatasetMet();
  }

  private List<String> getFindQuery() {
    LOG.log(Level.FINE, "PFunction: findquery selected: orig query: [" + this.q
        + "]");
    String queryExpression = "";
    Pattern parameterPattern = Pattern.compile("PParameter=\"(.+?)\"");
    Matcher urlsMatch = parameterPattern.matcher(this.q);
    while (urlsMatch.find()) {
      queryExpression = urlsMatch.group(1);
    }

    List<String> datasetUrls = new Vector<String>();

    for (String datasetUrl : this.allUrls) {
      DConnect dConn = null;
      try {
        dConn = new DConnect(datasetUrl, true);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        LOG.log(Level.WARNING, datasetUrl
            + " is neither a valid URL nor a filename.");
      }
      try {
        DataDDS dds = dConn.getData(queryExpression, null);

        if (dds != null) {
          datasetUrls.add(datasetUrl);
        }
      } catch (Exception e) {
        e.printStackTrace();
        LOG.log(Level.SEVERE, " Some DAP2Exception or not a validate DDS", e);
      }
    }

    return datasetUrls;

  }

  private List<String> getFindSome() {
    LOG.log(Level.FINE, "PFunction: findsome selected");
    String urlsString = "";
    Pattern parameterPattern = Pattern.compile("PParameter=\"(.+?)\"");
    Matcher urlsMatch = parameterPattern.matcher(this.q);
    while (urlsMatch.find()) {
      urlsString = urlsMatch.group(1);
    }

    LOG.log(Level.FINE, "PParameter: [" + urlsString
        + "] parsed from original string query: [" + this.q + "]");

    List<String> openDapUrls = new ArrayList<String>();

    StringTokenizer tokens = new StringTokenizer(urlsString, ",");
    while (tokens.hasMoreTokens()) {
      openDapUrls.add(tokens.nextToken());
    }

    LOG.log(Level.FINE, "OPeNDAP urls: [" + openDapUrls + "]");
    return openDapUrls;
  }

}
