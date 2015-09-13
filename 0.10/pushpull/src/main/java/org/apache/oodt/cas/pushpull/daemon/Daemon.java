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

//OODT imports
import org.apache.oodt.cas.pushpull.config.Config;
import org.apache.oodt.cas.pushpull.config.DaemonInfo;
import org.apache.oodt.cas.pushpull.config.SiteInfo;
import org.apache.oodt.cas.pushpull.daemon.DaemonMBean;
import org.apache.oodt.cas.pushpull.daemon.DaemonRmiInterface;
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;
import org.apache.oodt.cas.pushpull.retrievalsystem.RetrievalSetup;

//JDK imports
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

//JMX imports
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Controls the execution times of the Crawler it is given. The Crawler is
 * specified by the properties file passed in. A Crawler will be created per the
 * properties file and executed at six hour intervals. This class can be
 * controlled by CrawlDaemonController after is has been started up.
 * 
 * @author bfoster
 */
public class Daemon extends UnicastRemoteObject implements DaemonRmiInterface,
        DaemonMBean {

    private static final long serialVersionUID = 7660972939723142802L;

    private DaemonListener daemonListener;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(Daemon.class.getName());

    /**
     * Keeps track of whether the Crawler is running or not
     */
    private boolean isRunning;

    /**
     * If set to false the CrawlDaemon will terminate after the Crawler finishes
     * crawling its current site.
     */
    private boolean keepRunning;

    /**
     * The time at which the Constructor is called
     */
    private long daemonCreationTime;

    /**
     * The total time during which the Crawl is actually running -- wait() time
     * is not included.
     */
    private long daemonTotalRuntime;

    /**
     * Total number of times the Crawler has been run
     */
    private int numberOfCrawls;

    private File propFilesDir;

    private int daemonID;

    private RetrievalSetup rs;

    private Config config;

    private DaemonInfo daemonInfo;

    private MBeanServer mbs;

    private int rmiRegPort;

    /**
     * Constructor
     * 
     * @throws RemoteException
     * @throws RemoteException
     * @throws InstantiationException
     * @throws IOException
     * @throws SecurityException
     */
    public Daemon(int rmiRegPort, int daemonID, Config config,
            DaemonInfo daemonInfo, SiteInfo siteInfo) throws RemoteException,
            InstantiationException {
        super();

        this.rmiRegPort = rmiRegPort;
        this.daemonID = daemonID;
        rs = new RetrievalSetup(config, siteInfo);
        this.config = config;
        this.daemonInfo = daemonInfo;

        daemonCreationTime = System.currentTimeMillis();
        daemonTotalRuntime = 0;
        numberOfCrawls = 0;
        isRunning = false;

        try {
            registerRMIServer();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to bind to RMI server : "
                    + e.getMessage());
        }

        try {
            // registry CrawlDaemon as MBean so it can be used with jconsole
            mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(
                    "org.apache.oodt.cas.pushpull.daemon:type=Daemon"
                            + this.getDaemonID());
            mbs.registerMBean(this, name);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Failed to register CrawlDaemon as a MBean Object : "
                            + e.getMessage());
        }
    }

    public String getName() {
        return "Daemon" + this.getDaemonID();
    }

    private void registerRMIServer() throws RemoteException,
            MalformedURLException, NotBoundException, AlreadyBoundException {
        try {
            Naming.bind("//localhost:" + this.rmiRegPort + "/daemon"
                    + this.getDaemonID(), this);
            LOG.log(Level.INFO, "Created Daemon ID = " + this.getDaemonID()
                    + " on RMI registry port " + this.rmiRegPort);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to bind Daemon with ID = "
                    + this.getDaemonID() + " to RMI registry at port "
                    + this.rmiRegPort);
        }
    }

    public void registerDaemonListener(DaemonListener daemonListener) {
        this.daemonListener = daemonListener;
        this.daemonListener.wasRegisteredWith(this);
    }

    /**
     * Loads and executes the Crawler specified by the properties file. It will
     * crawl the URLs specified in the properties file in the sequence
     * given--one at a time.
     * 
     * @param property
     *            The system property whose value is the path to a java
     *            .properties file that is be used to create the Crawler
     * @throws DirStructException
     */
    public void startDaemon() {
        new Thread(new Runnable() {

            public void run() {

                // check if Daemon should sleep first
                long timeTilNextRun;
                if ((timeTilNextRun = Daemon.this.calculateTimeTilNextRun()) != 0
                        && !(Daemon.this.beforeToday(daemonInfo
                                .getFirstRunDateTime()) && daemonInfo
                                .runOnReboot()))
                    sleep(timeTilNextRun);

                for (keepRunning = true; keepRunning;) {
                    long startTime = System.currentTimeMillis();

                    // get permission to run
                    Daemon.this.notifyDaemonListenerOfStart();
                    if (!keepRunning) {
                        Daemon.this.notifyDaemonListenerOfFinish();
                        System.out.println("BREAKING OUT");
                        break;
                    }

                    // run
                    Daemon.this.isRunning = true;

                    try {
                        rs.retrieveFiles(daemonInfo.getPropFilesInfo(),
                                daemonInfo.getDataFilesInfo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        numberOfCrawls++;
                    }

                    Daemon.this.isRunning = false;

                    // calculate performance and sleep
                    Daemon.this.notifyDaemonListenerOfFinish();
                    Daemon.this.calculateAndStoreElapsedTime(startTime);
                    if (Daemon.this.keepRunning
                            && daemonInfo.getTimeIntervalInMilliseconds() >= 0) {
                        sleep(Daemon.this.calculateTimeTilNextRun());
                    } else {
                        break;
                    }
                }
                LOG.log(Level.INFO, "Daemon with ID = "
                        + Daemon.this.getDaemonID() + " on RMI registry port "
                        + Daemon.this.rmiRegPort + " is shutting down");
                Daemon.this.unregister();
            }
        }).start();
    }

    private void unregister() {
        try {
            // unregister CrawlDaemon from RMI registry
            Naming.unbind("//localhost:" + this.rmiRegPort + "/daemon"
                    + this.getDaemonID());
            this.mbs.unregisterMBean(new ObjectName(
                    "org.apache.oodt.cas.pushpull.daemon:type=Daemon"
                            + this.getDaemonID()));
            UnicastRemoteObject.unexportObject(this, true);
            this.daemonListener.wasUnregisteredWith(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDaemonID() {
        return Integer.parseInt(this.rmiRegPort + "" + this.daemonID);
    }

    private long calculateTimeTilNextRun() {
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar gcStartDateTime = new GregorianCalendar();
        gcStartDateTime.setTime(daemonInfo.getFirstRunDateTime());
        long diff = now.getTimeInMillis() - gcStartDateTime.getTimeInMillis();
        if (Math.abs(diff) <= daemonInfo.getEpsilonInMilliseconds())
            return 0;
        else if (diff < 0)
            return gcStartDateTime.getTimeInMillis() - now.getTimeInMillis();
        else if (daemonInfo.getTimeIntervalInMilliseconds() == 0) {
            return 0;
        } else {
            int numOfPeriods = (int) (diff / daemonInfo
                    .getTimeIntervalInMilliseconds());
            long nextRunTime = gcStartDateTime.getTimeInMillis()
                    + ((numOfPeriods + 1) * daemonInfo
                            .getTimeIntervalInMilliseconds());
            return nextRunTime - now.getTimeInMillis();
        }
    }

    private boolean beforeToday(Date date) {
        return date.before(new Date(System.currentTimeMillis()));
    }

    private void notifyDaemonListenerOfStart() {
        if (this.daemonListener != null)
            this.daemonListener.daemonStarting(this);
    }

    private void notifyDaemonListenerOfFinish() {
        if (this.daemonListener != null)
            this.daemonListener.daemonFinished(this);
    }

    private void sleep(long length) {
        if (length > 0) {
            LOG.log(Level.INFO, "Daemon with ID = " + this.getDaemonID()
                    + " on RMI registry port " + this.rmiRegPort
                    + " is going to sleep until "
                    + new Date(System.currentTimeMillis() + length));
            synchronized (this) {
                try {
                    wait(length);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private long calculateAndStoreElapsedTime(long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        daemonTotalRuntime += elapsedTime;
        return elapsedTime;
    }

    public synchronized void pauseDaemon() {
        try {
            LOG.log(Level.INFO, "Daemon with ID = " + this.getDaemonID()
                    + " on RMI registry port " + this.rmiRegPort
                    + " has been stopped");
            this.wait(0);
        } catch (Exception e) {
        }
        LOG.log(Level.INFO, "Daemon with ID = " + this.getDaemonID()
                + " on RMI registry port " + this.rmiRegPort + " has resumed");
    }

    /**
     * Wakes up the CrawlDaemon if it is sleeping
     */
    public synchronized void resume() {
        notify();
    }

    /**
     * Will terminate the CrawlDaemon. If its Crawler is crawling a site when
     * this method is called, the terminate won't take place until after the
     * Crawler has complete crawling that site.
     */
    public synchronized void quit() {
        keepRunning = false;
        resume();
    }

    /**
     * Can be used to determine if Crawler is presently running
     * 
     * @return true if Crawler is runnning
     * @uml.property name="isRunning"
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Average runtime for the Crawler
     * 
     * @return average runtime for the Crawler
     */
    public long getAverageRunTime() {
        return daemonTotalRuntime / numberOfCrawls;
    }

    /**
     * Gets the total crawling time of the Crawler
     * 
     * @return Total crawling time of Crawler
     */
    public long getMillisCrawling() {
        return daemonTotalRuntime;
    }

    /**
     * Gets the time between the start of Crawler executions
     * 
     * @return Time interval between Crawler start times
     */
    public long getTimeInterval() {
        return daemonInfo.getTimeIntervalInMilliseconds();
    }

    /**
     * Gets the total number of times the Crawler has been run
     * 
     * @return The number of times Crawler has run
     */
    public int getNumCrawls() {
        return numberOfCrawls;
    }

    public String[] downloadedFilesInStagingArea() {
        return this.daemonInfo.getDataFilesInfo().getDownloadInfo()
                .getStagingArea().list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return !name.startsWith("Downloading_")
                                && !(name.endsWith("info.tmp") || name
                                        .endsWith("cas"));
                    }
                });
    }

    public String[] downloadingFilesInStagingArea() {
        return this.daemonInfo.getDataFilesInfo().getDownloadInfo()
                .getStagingArea().list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith("Downloading_")
                                && !(name.endsWith("info.tmp") || name
                                        .endsWith("cas"));
                    }
                });
    }

    public int numberOfFilesDownloadingInStagingArea() {
        return this.downloadingFilesInStagingArea().length;
    }

    public int numberOfFilesDownloadedInStagingArea() {
        return this.downloadedFilesInStagingArea().length;
    }

    // ***************DaemonInfo******************
    public long getTimeIntervalInMilliseconds() {
        return this.daemonInfo.getTimeIntervalInMilliseconds();
    }

    public long getEpsilonInMilliseconds() {
        return this.daemonInfo.getEpsilonInMilliseconds();
    }

    public boolean getRunOnReboot() {
        return this.daemonInfo.runOnReboot();
    }

    public Date getFirstRunDateTime() {
        return this.daemonInfo.getFirstRunDateTime();
    }

    // ***************DaemonInfo******************

    // ***************DataFilesInfo*******************
    public String getDataFilesRemoteSite() {
        RemoteSite remoteSite = this.daemonInfo.getDataFilesInfo()
                .getDownloadInfo().getRemoteSite();
        return (remoteSite == null) ? "" : remoteSite.toString();
    }

    public String getDataFilesRenamingConv() {
        return this.daemonInfo.getDataFilesInfo().getDownloadInfo()
                .getRenamingConv();
    }

    public boolean getDeleteDataFilesFromServer() {
        return this.daemonInfo.getDataFilesInfo().getDownloadInfo()
                .deleteFromServer();
    }

    public String getQueryMetadataElementName() {
        String element = this.daemonInfo.getDataFilesInfo()
                .getQueryMetadataElementName();
        if (element == null || element.equals(""))
            element = "Filename";
        return this.daemonInfo.getDataFilesInfo().getQueryMetadataElementName();
    }

    public File getDataFilesStagingArea() {
        return this.daemonInfo.getDataFilesInfo().getDownloadInfo()
                .getStagingArea();
    }

    public boolean getAllowAliasOverride() {
        return this.daemonInfo.getDataFilesInfo().getDownloadInfo()
                .isAllowAliasOverride();
    }

    // **************DataFilesInfo********************

    // **************PropFilesInfo********************
    public String getPropertyFilesRemoteSite() {
        RemoteSite remoteSite = this.daemonInfo.getPropFilesInfo()
                .getDownloadInfo().getRemoteSite();
        return (remoteSite == null) ? "" : remoteSite.toString();
    }

    public String getPropertyFilesRenamingConv() {
        return this.daemonInfo.getPropFilesInfo().getDownloadInfo()
                .getRenamingConv();
    }

    public boolean getDeletePropertyFilesFromServer() {
        return this.daemonInfo.getPropFilesInfo().getDownloadInfo()
                .deleteFromServer();
    }

    public String getPropertyFilesOnSuccessDir() {
        File successDir = this.daemonInfo.getPropFilesInfo().getOnSuccessDir();
        return successDir == null ? "" : successDir.getAbsolutePath();
    }

    public String getPropertyFilesOnFailDir() {
        File failDir = this.daemonInfo.getPropFilesInfo().getOnFailDir();
        return failDir == null ? "" : failDir.getAbsolutePath();
    }

    public File getPropertyFilesLocalDir() {
        return this.daemonInfo.getPropFilesInfo().getLocalDir();
    }

    // **************PropFilesInfo********************

    /**
     * Gets the time in milliseconds for when the CrawlDaemon constructor was
     * invoked.
     * 
     * @return
     * @uml.property name="daemonCreationTime"
     */
    public long getDaemonCreationTime() {
        return daemonCreationTime;
    }

    public boolean getHasBeenToldToQuit() {
        return !this.keepRunning;
    }

    public String toString() {
        return this.getName();
    }

    /**
     * Starts the program
     * 
     * @param args
     *            Not Used
     * @throws IOException
     * @throws SecurityException
     */
    public static void main(String[] args) {
        try {

            int rmiPort = -1;
            boolean waitForCrawlNotification = false;

            for (int i = 0; i < args.length; ++i) {
                if (args[i].equals("--rmiPort"))
                    rmiPort = Integer.parseInt(args[++i]);
                else if (args[i].equals("--waitForNotification"))
                    waitForCrawlNotification = true;
            }

            LocateRegistry.createRegistry(rmiPort);

            try {
                // registry CrawlDaemon as MBean so it can be used with jconsole
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                ObjectName name = new ObjectName(
                        "org.apache.oodt.cas.pushpull.daemon:type=Daemon");
            } catch (Exception e) {
                LOG.log(Level.SEVERE,
                        "Failed to register CrawlDaemon as a MBean Object : "
                                + e.getMessage());
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create CrawlDaemon : "
                    + e.getMessage());
        } finally {
            // terminate the CrawlDaemon
            LOG.log(Level.INFO, "Terminating CrawlDaemon");
        }

    }

}
