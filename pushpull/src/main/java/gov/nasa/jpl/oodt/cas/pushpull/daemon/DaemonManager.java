//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.daemon;

//JDK imports
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DaemonManager implements DaemonListener {

    private LinkedList<Daemon> waitingList;

    private HashSet<Integer> usedIDs;

    private Daemon runningDaemon;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(DaemonManager.class
            .getName());

    public DaemonManager() {
        waitingList = new LinkedList<Daemon>();
        usedIDs = new HashSet<Integer>();
        runningDaemon = null;
    }

    public synchronized boolean hasRunningDaemons() {
        return this.waitingList.size() > 0 || this.runningDaemon != null;
    }

    public synchronized void clearWaitingList() {
        this.waitingList.clear();
    }

    public synchronized void wasRegisteredWith(Daemon daemon) {
        this.usedIDs.add(new Integer(daemon.getDaemonID()));
    }

    public synchronized void wasUnregisteredWith(Daemon daemon) {
        this.usedIDs.remove(new Integer(daemon.getDaemonID()));
    }

    public void daemonStarting(Daemon daemon) {
        if (!this.setAsRunning(daemon)) {
            daemon.pauseDaemon();
        }
    }

    public void daemonFinished(Daemon daemon) {
        if (daemon.equals(this.runningDaemon))
            this.startNextOnWaitingList();
        else
            this.waitingList.remove(daemon);
    }

    public synchronized HashSet<Integer> getUsedIDs() {
        return this.usedIDs;
    }

    private synchronized boolean setAsRunning(Daemon daemon) {
        if (runningDaemon == null) {
            runningDaemon = daemon;
            LOG.log(Level.INFO, "Daemon with ID = " + daemon.getDaemonID()
                    + " was given permission to run");
            return true;
        } else {
            LOG.log(Level.INFO, "Daemon with ID = " + daemon.getDaemonID()
                    + " was added to the DaemonManager's waiting list");
            waitingList.add(daemon);
            return false;
        }
    }

    private synchronized void startNextOnWaitingList() {
        if (waitingList.size() > 0) {
            runningDaemon = waitingList.removeFirst();
            LOG.log(Level.INFO, "Daemon with ID = "
                    + runningDaemon.getDaemonID()
                    + " was given permission to run");
            runningDaemon.resume();
        } else {
            runningDaemon = null;
        }
    }

    public synchronized String[] getQueueList() {
        String[] queue = new String[this.waitingList.size()];
        int counter = 0;
        for (Daemon daemon : this.waitingList) {
            queue[counter++] = daemon.getName();
        }
        return queue;
    }

}
