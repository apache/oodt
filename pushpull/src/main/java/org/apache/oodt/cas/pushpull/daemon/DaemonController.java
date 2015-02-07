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


package org.apache.oodt.cas.pushpull.daemon;

//JDK imports
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * @author bfoster
 * 
 * <p>
 * After the CrawlDaemon has been started, this class can control the
 * CrawlDaemon through webserver communication, thus it can be control from
 * computers other than the one it is executing on. This class has control over
 * pausing, resuming, and killing the CrawlDaemon process. It also has the
 * ability of getting the status on several variables within the CrawlDaemon
 * class. See CrawlDaemon for more info.
 * </p>.
 */
public class DaemonController {

    private DaemonRmiInterface daemon;

    /**
     * Constructor -- initializes the XmlRpcClient
     * 
     * @param crawlUrlStr
     *            The URL location where the CrawlDaemon server is running
     * @throws InstantiationException
     */
    public DaemonController(String rmiUrl) throws RemoteException {
        try {
            daemon = (DaemonRmiInterface) Naming.lookup(rmiUrl);
            System.out.println(!daemon.getHasBeenToldToQuit());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the average time for each run of the Crawler controlled by the
     * CrawlDaemons
     * 
     * @return Average runtime of Crawler
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public long getAverageRunTime() throws RemoteException {
        return daemon.getAverageRunTime();
    }

    /**
     * Gets the total milliseconds for which the Crawler in CrawlDaemon has been
     * crawling since the CrawlDaemon was created.
     * 
     * @return The total milliseconds which the Crawler has been crawling
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public long getMillisCrawling() throws RemoteException {
        return daemon.getMillisCrawling();
    }

    /**
     * Gets the time between the start of each scheduled run.
     * 
     * @return The time between the start of each execution of the Crawler
     *         controlled by the CrawlDaemon
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public long getWaitInterval() throws RemoteException {
        return daemon.getTimeInterval();
    }

    /**
     * Gets the numbers of times the Crawler has been run by the CrawlDaemon
     * 
     * @return The of times Crawler has executed
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public int getNumCrawls() throws RemoteException {
        return daemon.getNumCrawls();
    }

    /**
     * Used to determine if the Crawler in the CrawlDaemon is running
     * 
     * @return true is Crawler is running
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public boolean isRunning() throws RemoteException {
        return daemon.isRunning();
    }

    /**
     * Wakes the CrawlDaemon up and tells it to start crawling again. If stop()
     * was called, this method will have the CrawlDaemon continue from where it
     * left off. This method will also wake up the CrawlDaemon from its sleep
     * between scheduled runs.
     * 
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public void resume() throws RemoteException {
        daemon.resume();
    }

    public void quit() throws RemoteException {
        daemon.quit();
    }

    /**
     * Gets the time in milliseconds of when the CrawlDaemon was created
     * 
     * @return The time the CrawlDaemon was created
     * @throws RemoteException
     * @throws XmlRpcCommunicationException
     *             Error communicating with server
     */
    public long getDaemonCreationTime() throws RemoteException {
        return daemon.getDaemonCreationTime();
    }

    /**
     * Driver method -- starts the CrawlDaemonController
     * 
     * @param args
     *            See documentation
     * @throws Exception
     *             On error! :)
     */
    public static void main(String[] args) throws Exception {
        String avgCrawlOperation = "--getAverageRunTime\n";
        String getMilisCrawlOperation = "--getMilisCrawling\n";
        String getNumCrawlsOperation = "--getNumCrawls\n";
        String getWaitIntervalOperation = "--getWaitInterval\n";
        String isRunningOperation = "--isRunning\n";
        String stopOperation = "--stop\n";
        String usage = "CrawlController --url <url to xml rpc service> "
                + "--operation [<operation> [params]]\n" + "operations:\n"
                + avgCrawlOperation + getMilisCrawlOperation
                + getNumCrawlsOperation + getWaitIntervalOperation
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
        DaemonController controller = new DaemonController(url);
        if (operation.equals("--getAverageRunTime")) {
            double avgCrawlTime = controller.getAverageRunTime();
            System.out.println("Average Crawl Time: [" + avgCrawlTime + "]");
        } else if (operation.equals("--getMilisCrawling")) {
            long crawlTime = controller.getMillisCrawling();
            System.out.println("Total Crawl Time: [" + crawlTime
                    + "] miliseconds");
        } else if (operation.equals("--getNumCrawls")) {
            int numCrawls = controller.getNumCrawls();
            System.out.println("Num Crawls: [" + numCrawls + "]");
        } else if (operation.equals("--getWaitInterval")) {
            long waitInterval = controller.getWaitInterval();
            System.out.println("Wait Interval: [" + waitInterval + "]");
        } else if (operation.equals("--isRunning")) {
            boolean running = controller.isRunning();
            System.out.println(running ? "Yes" : "No");
        } else
            throw new IllegalArgumentException("Unknown Operation!");
    }

}
