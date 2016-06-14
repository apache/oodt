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

package org.apache.oodt.pcs.health;

//OODT imports
import org.apache.oodt.pcs.input.PGEConfigurationFile;
import org.apache.oodt.pcs.input.PGEGroup;
import org.apache.oodt.pcs.input.PGEConfigFileReader;
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * Properties used by the {@link PCSHealthMonitor} tool to determine
 * {@link ProductCrawler} status.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class CrawlPropertiesFile implements CrawlerPropertiesMetKeys {

  private PGEConfigurationFile file;

  /**
   * Constructs a new CrawlPropertiesFile.
   * 
   * @param filePath
   *          The path to the CrawlPropertiesFile to load.
   * @throws InstantiationException
   *           If there is some error reading the file.
   */
  public CrawlPropertiesFile(String filePath) throws InstantiationException {
    try {
      this.file = new PGEConfigFileReader().read(new FileInputStream(filePath));
    } catch (Exception e) {
      throw new InstantiationException(e.getMessage());
    }
  }

  /**
   * 
   * @return A {@link List} of {@link CrawlInfo} objects describing a Crawler.
   */
  public List getCrawlers() {
    PGEGroup crawlInfo = (PGEGroup) this.file.getPgeSpecificGroups().get(
        CRAWLER_INFO_GROUP);

    Map scalars = crawlInfo.getScalars();
    List crawlers = new Vector(scalars.keySet().size());
    for (Object o : scalars.keySet()) {
      String crawlerName = (String) o;
      String crawlerPort = crawlInfo.getScalar(crawlerName).getValue();
      CrawlInfo info = new CrawlInfo(crawlerName, crawlerPort);
      crawlers.add(info);
    }

    return crawlers;
  }

  /**
   * 
   * @return The String hostname that the Crawlers run on. This is used by the
   *         {@link PCSHealthMonitor} tool to communicate with the Crawlers and
   *         to check their status.
   */
  public String getCrawlHost() {
    String crawlHost = ((PGEGroup) this.file.getPgeSpecificGroups().get(
        CRAWLER_PROPERTIES_GROUP)).getScalar(CRAWLER_HOST_NAME).getValue();
    crawlHost = PathUtils.replaceEnvVariables(crawlHost);
    return crawlHost;
  }

}
