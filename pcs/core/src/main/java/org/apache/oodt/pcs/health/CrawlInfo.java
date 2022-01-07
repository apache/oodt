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

import java.io.Serializable;

/**
 * Information about a crawler: its <code>crawlerName</code> and
 * <code>crawlerPort</code>.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class CrawlInfo implements Serializable {

  private String crawlerName;

  private String crawlerPort;

  /**
   * Default Constructor.
   * 
   */
  public CrawlInfo() {

  }

  /**
   * Constructs a new CrawlInfo with the specified parameters.
   * 
   * @param name
   *          The name of the Crawler.
   * @param port
   *          The port that the Crawler was running on.
   */
  public CrawlInfo(String name, String port) {
    this.crawlerName = name;
    this.crawlerPort = port;
  }

  /**
   * @return the crawlerName
   */
  public String getCrawlerName() {
    return crawlerName;
  }

  /**
   * @param crawlerName
   *          the crawlerName to set
   */
  public void setCrawlerName(String crawlerName) {
    this.crawlerName = crawlerName;
  }

  /**
   * @return the crawlerPort
   */
  public String getCrawlerPort() {
    return crawlerPort;
  }

  /**
   * @param crawlerPort
   *          the crawlerPort to set
   */
  public void setCrawlerPort(String crawlerPort) {
    this.crawlerPort = crawlerPort;
  }

}
