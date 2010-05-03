//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.daemon;

//JDK imports
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface DaemonRmiInterface extends Remote {

    /**
     * Wakes up the CrawlDaemon if it is sleeping
     * 
     * @return Always true -- done because of XmlRpc communication
     */
    public void resume() throws RemoteException;

    /**
     * Will terminate the CrawlDaemon. If its Crawler is crawling a site when
     * this method is called, the terminate won't take place until after the
     * Crawler has complete crawling that site.
     * 
     * @return
     */
    public void quit() throws RemoteException;

    /**
     * Can be used to determine if Crawler is presently running
     * 
     * @return true if Crawler is runnning
     * @uml.property name="isRunning"
     */
    public boolean isRunning() throws RemoteException;

    /**
     * Average runtime for the Crawler
     * 
     * @return average runtime for the Crawler
     */
    public long getAverageRunTime() throws RemoteException;

    /**
     * Gets the total crawling time of the Crawler
     * 
     * @return Total crawling time of Crawler
     */
    public long getMillisCrawling() throws RemoteException;

    /**
     * Gets the time between the start of Crawler executions
     * 
     * @return Time interval between Crawler start times
     */
    public long getTimeInterval() throws RemoteException;

    /**
     * Gets the total number of times the Crawler has been run
     * 
     * @return The number of times Crawler has run
     */
    public int getNumCrawls() throws RemoteException;

    /**
     * Gets the time in milliseconds for when the CrawlDaemon constructor was
     * invoked.
     * 
     * @return
     * @uml.property name="daemonCreationTime"
     */
    public long getDaemonCreationTime() throws RemoteException;

    public boolean getHasBeenToldToQuit() throws RemoteException;

}
