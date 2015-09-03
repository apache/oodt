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

package org.apache.oodt.cas.crawl.daemon;


import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.xmlrpc.WebServer;

public class XmlRpcCrawlerDaemon extends CrawlDaemon {

    public XmlRpcCrawlerDaemon(int wait, ProductCrawler crawler, int port) {
        super(wait,crawler,port);
    }

    public void startCrawling() {
        WebServer server = new WebServer(this.getDaemonPort());
        server.addHandler("crawldaemon", this);
        server.start();
        this.crawl();
        server.shutdown();
    }

    public double rpcGetAverageCrawlTime() {
        return this.getAverageCrawlTime();
    }

    /**
     * @return the milisCrawling
     */

    public int rpcGetMilisCrawling() {
        return this.getMilisCrawling();
    }

    /**
     * @return the waitInterval
     */

    public int rpcGetWaitInterval() {
        return (int) this.getWaitInterval();
    }

    /**
     * @return the running
     */

    public boolean rpcIsRunning() {
        return this.isRunning();
    }


    public boolean rpcStop() {
        return this.stop();
    }

    /**
     * @param waitInterval
     *            the waitInterval to set
     */

    public void rpcSetWaitInterval(long waitInterval){
        this.setWaitInterval(waitInterval);
    }

}
