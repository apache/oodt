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

//OODT imports
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.xmlrpc.WebServer;

import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports
//APACHE imports

/**
 * @author mattmann
 * @version $Revision$
 * @deprecated soon be replaced by avro-rpc
 * <p>
 * A daemon utility class for {@link ProductCrawler}s that allows a regular
 * ProductCrawler to be run as a daemon, and statistics about crawling to be
 * kept. The daemon is an XML-RPC accessible web service.
 * </p>.
 */
@Deprecated
public class CrawlDaemon {

    public static final double DOUBLE = 1000.0;
    /* our log stream */
    private static Logger LOG = Logger.getLogger(CrawlDaemon.class.getName());

    /* are we running or not? */
    private boolean running = true;

    /* wait interval in seconds between crawls */
    private long waitInterval = -1;

    /* number of times that the crawler has been called */
    private int numCrawls = 0;

    /* the amount of miliseconds spent crawling */
    private long milisCrawling = 0L;

    /* the product crawler that this daemon should use */
    private ProductCrawler crawler = null;

    /* the port that this crawl daemon should run on */
    private int daemonPort = 9999;

    public CrawlDaemon(int wait, ProductCrawler crawler, int port) {
        this.waitInterval = wait;
        this.crawler = crawler;
        this.daemonPort = port;
    }

    public void startCrawling() {
        // start up the web server
        WebServer server = new WebServer(this.daemonPort);
        server.addHandler("crawldaemon", this);
        server.start();

        LOG.log(Level.INFO, "Crawl Daemon started by "
                + System.getProperty("user.name", "unknown"));

        while (running) {
            // okay, time to crawl
            long timeBefore = System.currentTimeMillis();
            crawler.crawl();
            long timeAfter = System.currentTimeMillis();
            milisCrawling += (timeAfter - timeBefore);
            numCrawls++;

            LOG.log(Level.INFO, "Sleeping for: [" + waitInterval + "] seconds");
            // take a nap
            try {
                Thread.currentThread().sleep(waitInterval * 1000);
            } catch (InterruptedException ignore) {
            }
        }

        LOG.log(Level.INFO, "Crawl Daemon: Shutting down gracefully");
        LOG.log(Level.INFO, "Num Crawls: [" + this.numCrawls + "]");
        LOG.log(Level.INFO, "Total time spent crawling: ["
                + (this.milisCrawling / DOUBLE) + "] seconds");
        LOG.log(Level.INFO, "Average Crawl Time: ["
                + (this.getAverageCrawlTime() / DOUBLE) + "] seconds");
        server.shutdown();
    }

    public double getAverageCrawlTime() {
        return (1.0 * milisCrawling) / (1.0 * numCrawls);
    }

    /**
     * @return the crawler
     */
    public ProductCrawler getCrawler() {
        return crawler;
    }

    /**
     * @param crawler
     *            the crawler to set
     */
    public void setCrawler(ProductCrawler crawler) {
        this.crawler = crawler;
    }

    /**
     * @return the milisCrawling
     */
    public int getMilisCrawling() {
        return (int) milisCrawling;
    }

    /**
     * @param milisCrawling
     *            the milisCrawling to set
     */
    public void setMilisCrawling(long milisCrawling) {
        this.milisCrawling = milisCrawling;
    }

    /**
     * @return the numCrawls
     */
    public int getNumCrawls() {
        return numCrawls;
    }

    /**
     * @param numCrawls
     *            the numCrawls to set
     */
    public void setNumCrawls(int numCrawls) {
        this.numCrawls = numCrawls;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     */
    public boolean stop() {
        this.running = false;
        return false;
    }

    /**
     * @return the waitInterval
     */
    public int getWaitInterval() {
        return (int) waitInterval;
    }

    /**
     * @param waitInterval
     *            the waitInterval to set
     */
    public void setWaitInterval(long waitInterval) {
        this.waitInterval = waitInterval;
    }

    private static void main(String[] args) throws InstantiationException {
        throw new InstantiationException(
                "Don't call a crawl daemon by its main function!");
    }

}
