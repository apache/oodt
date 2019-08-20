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
 * 
 * Health of a PCS Crawler in terms of the number of crawls performed, and
 * average crawl time provided by a {@link org.apache.oodt.cas.crawl.daemon.CrawlDaemon}
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class CrawlerHealth implements Serializable {

  private String crawlerName;

  private int numCrawls;

  private double avgCrawlTime;

  /**
   * Default Constructor.
   * 
   */
  public CrawlerHealth() {

  }

  /**
   * Constructs a new CrawlerHealth with the given parameters.
   * 
   * @param crawlerName
   *          Name of the Crawler.
   * @param numCrawls
   *          The number of crawls performed.
   * @param avgCrawlTime
   *          The average time (in seconds) that a Crawler spends during each
   *          crawl.
   */
  public CrawlerHealth(String crawlerName, int numCrawls, double avgCrawlTime) {
    this.crawlerName = crawlerName;
    this.numCrawls = numCrawls;
    this.avgCrawlTime = avgCrawlTime;
  }

  /**
   * @return the avgCrawlTime
   */
  public double getAvgCrawlTime() {
    return avgCrawlTime;
  }

  /**
   * @param avgCrawlTime
   *          the avgCrawlTime to set
   */
  public void setAvgCrawlTime(double avgCrawlTime) {
    this.avgCrawlTime = avgCrawlTime;
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
   * @return the numCrawls
   */
  public int getNumCrawls() {
    return numCrawls;
  }

  /**
   * @param numCrawls
   *          the numCrawls to set
   */
  public void setNumCrawls(int numCrawls) {
    this.numCrawls = numCrawls;
  }

}
