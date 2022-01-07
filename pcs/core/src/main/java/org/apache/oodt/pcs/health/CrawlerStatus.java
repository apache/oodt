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
 * Provides status about a Crawler to the {@link PCSHealthMonitor}.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class CrawlerStatus implements Serializable {

    private CrawlInfo info;

    private String status;

    private String crawlHost;

    /**
     * Default Constructor.
     * 
     */
    public CrawlerStatus() {

    }

    /**
     * Constructs a new CrawlerStatus with the given parameters.
     * 
     * @param info
     *            The {@link CrawlerInfo} describing this Crawler.
     * @param status
     *            One of {@link PCSHealthMonitorMetKeys#STATUS_UP} , or
     *            {@link PCSHealthMonitorMetKeys#STATUS_DOWN}.
     * 
     * @param crawlHost
     *            The host that the Crawler is running on.
     */
    public CrawlerStatus(CrawlInfo info, String status, String crawlHost) {
        this.info = info;
        this.status = status;
        this.crawlHost = crawlHost;
    }

    /**
     * @return the info
     */
    public CrawlInfo getInfo() {
        return info;
    }

    /**
     * @param info
     *            the info to set
     */
    public void setInfo(CrawlInfo info) {
        this.info = info;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the crawlHost
     */
    public String getCrawlHost() {
        return crawlHost;
    }

    /**
     * @param crawlHost
     *            the crawlHost to set
     */
    public void setCrawlHost(String crawlHost) {
        this.crawlHost = crawlHost;
    }

}
