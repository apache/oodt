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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlException;
import org.apache.oodt.cas.crawler.structs.avrotypes.IntAvroCrawlDaemon;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

public class AvroCrawlDaemonController implements CrawlDaemonController {

    private Transceiver client = null;

    private IntAvroCrawlDaemon proxy;

    private URL url;

    public AvroCrawlDaemonController(String crawlUrlStr)
            throws InstantiationException, InstanceRepositoryException {
        try {
            url = new URL(crawlUrlStr);

            this.client = new NettyTransceiver(new InetSocketAddress(url.getHost(),url.getPort()));
            proxy = SpecificRequestor.getClient(IntAvroCrawlDaemon.class, client);

        } catch (MalformedURLException e) {
            throw new InstantiationException(e.getMessage());
        } catch (IOException e) {
            throw new InstanceRepositoryException(e.getMessage());
        }
    }


    @Override
    public double getAverageCrawlTime() throws CrawlException {
        double avgCrawlTime = -1.0d;
        try {
            avgCrawlTime = proxy.rpcGetAverageCrawlTime();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        return avgCrawlTime;
    }

    @Override
    public int getMilisCrawling() throws CrawlException {
        int milisCrawling = -1;
        try {
            milisCrawling = proxy.rpcGetMilisCrawling();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        return milisCrawling;
    }

    @Override
    public int getWaitInterval() throws CrawlException {
        int waitInterval = -1;
        try {
            waitInterval = proxy.rpcGetWaitInterval();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        return waitInterval;
    }

    @Override
    public int getNumCrawls() throws CrawlException {
        int numCrawls = -1;
        try {
            numCrawls = proxy.rpcGetNumCrawls();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        return numCrawls;
    }

    @Override
    public boolean isRunning() throws CrawlException {
        boolean running = false;
        try {
            running = proxy.rpcIsRunning();
        }catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        return running;
    }

    @Override
    public void stop() throws CrawlException {
        boolean running = false;
        try {
            running = proxy.rpcStop();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        if (running) {
            throw new CrawlException("Stop attempt failed: crawl daemon: ["
                    + this.url + "] still running");
        }
    }

    @Override
    public URL getUrl() {
        return url;
    }

    public static void main(String[] args) throws Exception {
        String avgCrawlOperation = "--getAverageCrawlTime\n";
        String getMilisCrawlOperation = "--getMilisCrawling\n";
        String getNumCrawlsOperation = "--getNumCrawls\n";
        String getWaitIntervalOperation = "--getWaitInterval\n";
        String isRunningOperation = "--isRunning\n";
        String stopOperation = "--stop\n";

        String usage = "CrawlController --url <url to avro rpc service> --operation [<operation> [params]]\n"
                + "operations:\n"
                + avgCrawlOperation
                + getMilisCrawlOperation
                + getNumCrawlsOperation
                + getWaitIntervalOperation
                + isRunningOperation + stopOperation;

        String operation = null, url = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--operation")) {
                operation = args[++i];
            } else if (args[i].equals("--url")) {
                url = args[++i];
            }
        }

        if (operation == null) {
            System.err.println(usage);
            System.exit(1);
        }

        // create the controller
        CrawlDaemonController controller = new AvroCrawlDaemonController(url);

        if (operation.equals("--getAverageCrawlTime")) {
            double avgCrawlTime = controller.getAverageCrawlTime();
            System.out.println("Average Crawl Time: [" + avgCrawlTime + "]");
        } else if (operation.equals("--getMilisCrawling")) {
            int crawlTime = controller.getMilisCrawling();
            System.out.println("Total Crawl Time: [" + crawlTime
                    + "] miliseconds");
        } else if (operation.equals("--getNumCrawls")) {
            int numCrawls = controller.getNumCrawls();
            System.out.println("Num Crawls: [" + numCrawls + "]");
        } else if (operation.equals("--getWaitInterval")) {
            int waitInterval = controller.getWaitInterval();
            System.out.println("Wait Interval: [" + waitInterval + "]");
        } else if (operation.equals("--isRunning")) {
            boolean running = controller.isRunning();
            System.out.println(running ? "Yes" : "No");
        } else if (operation.equals("--stop")) {
            controller.stop();
            System.out.println("Crawl Daemon: [" + controller.getUrl()
                    + "]: shutdown successful");
        } else
            throw new IllegalArgumentException("Unknown Operation!");

    }


}
