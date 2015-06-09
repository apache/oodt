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
import org.apache.oodt.cas.pushpull.config.RemoteSpecs;

//JDK imports
import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

//JMX imports
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class DaemonLauncher implements DaemonLauncherMBean {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(DaemonLauncher.class
            .getName());

    private Config config;

    private RemoteSpecs rs;

    private int rmiRegistryPort;

    private int nextDaemonId;

    private DaemonManager dm;

    private LinkedList<Daemon> activeDaemonList;

    private LinkedList<File> sitesFiles;

    private File propertiesFile;

    private MBeanServer mbs;

    public DaemonLauncher(int rmiRegistryPort, File propertiesFile,
            LinkedList<File> sitesFiles) {
        this.rmiRegistryPort = rmiRegistryPort;
        nextDaemonId = 0;
        dm = new DaemonManager();
        activeDaemonList = new LinkedList<Daemon>();
        this.sitesFiles = sitesFiles;
        this.propertiesFile = propertiesFile;
        registerJMX();
        this.configure();
    }

    private void registerJMX() {
        try {
            LocateRegistry.createRegistry(this.rmiRegistryPort);
            mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(
                    "org.apache.oodt.cas.pushpull.daemon:type=DaemonLauncher"
                            + this.rmiRegistryPort);
            mbs.registerMBean(this, name);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Failed to register DaemonLauncher as a MBean Object : "
                            + e.getMessage());
        }
    }

    private synchronized void configure() {
        try {
            LOG.log(Level.INFO, "Configuring DaemonLauncher. . .");
            config = new Config();
            config.loadConfigFile(propertiesFile);
            rs = new RemoteSpecs();
            for (File sitesFile : sitesFiles) {
                LOG
                        .log(Level.INFO, "Loading SiteInfo file '" + sitesFile
                                + "'");
                rs.loadRemoteSpecs(sitesFile);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to configure DaemonLauncher : "
                    + e.getMessage());
        }
    }

    public synchronized void launchDaemons() {
        LOG.log(Level.INFO, "Launching Daemons . . .");
        for (DaemonInfo daemonInfo : rs.getDaemonInfoList()) {
            int curDaemonID = this.getNextDaemonId();
            try {
                Daemon daemon = new Daemon(this.rmiRegistryPort, curDaemonID,
                        this.config, daemonInfo, rs.getSiteInfo());
                LOG.log(Level.INFO, "Creating Daemon with ID = " + curDaemonID);
                daemon.registerDaemonListener(dm);
                daemon.startDaemon();
                activeDaemonList.add(daemon);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to create Daemon with ID = "
                        + curDaemonID + " : " + e.getMessage());
            }
        }
    }

    private synchronized int getNextDaemonId() {
        while (this.dm.getUsedIDs().contains(++this.nextDaemonId))
            ;
        return this.nextDaemonId;
    }

    public void refreshDaemons() {
        this.killAllDaemons();

        LOG.log(Level.INFO, "Refreshing Daemons . . .");
        this.configure();
        launchDaemons();
    }

    public synchronized void killAllDaemons() {
        LOG.log(Level.INFO, "Killing current Daemons . . .");
        this.nextDaemonId = 0;
        for (Daemon daemon : this.activeDaemonList) {
            if (!daemon.getHasBeenToldToQuit())
                daemon.quit();
        }
        activeDaemonList.clear();
    }

    public String[] viewDaemonWaitingList() {
        return this.dm.getQueueList();
    }

    public synchronized void quit() {
        this.killAllDaemons();
        this.notify();
    }

    public static void main(String[] args) {

        int rmiRegPort = -1;
        File propertiesFile = null;
        LinkedList<File> sitesFiles = new LinkedList<File>();

        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--rmiRegistryPort"))
                rmiRegPort = Integer.parseInt(args[++i]);
            else if (args[i].equals("--propertiesFile"))
                propertiesFile = new File(args[++i]);
            else if (args[i].equals("--remoteSpecsFile"))
                sitesFiles.add(new File(args[++i]));
        }

        DaemonLauncher daemonLauncher = new DaemonLauncher(rmiRegPort,
                propertiesFile, sitesFiles);

        daemonLauncher.launchDaemons();
        synchronized (daemonLauncher) {
            try {
                daemonLauncher.wait();
            } catch (Exception e) {
            }
        }
    }
}
