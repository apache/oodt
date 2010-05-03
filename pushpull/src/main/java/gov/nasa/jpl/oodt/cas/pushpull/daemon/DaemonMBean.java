//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.daemon;

//JDK imports
import java.io.File;
import java.util.Date;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface DaemonMBean {

    /**
     * Will terminate the CrawlDaemon. If its Crawler is crawling a site when
     * this method is called, the terminate won't take place until after the
     * Crawler has complete crawling that site.
     * 
     * @return
     */
    public void quit();

    /**
     * Can be used to determine if Crawler is presently running
     * 
     * @return true if Crawler is runnning
     * @uml.property name="isRunning"
     */
    public boolean isRunning();

    public boolean getHasBeenToldToQuit();

    public long getTimeIntervalInMilliseconds();

    public long getEpsilonInMilliseconds();

    public boolean getRunOnReboot();

    public Date getFirstRunDateTime();

    public String[] downloadedFilesInStagingArea();

    public String[] downloadingFilesInStagingArea();

    public int numberOfFilesDownloadedInStagingArea();

    public int numberOfFilesDownloadingInStagingArea();

    public String getDataFilesRemoteSite();

    public String getDataFilesRenamingConv();

    public boolean getDeleteDataFilesFromServer();

    public String getQueryMetadataElementName();

    public File getDataFilesStagingArea();

    public boolean getAllowAliasOverride();

    public String getPropertyFilesRemoteSite();

    public String getPropertyFilesRenamingConv();

    public boolean getDeletePropertyFilesFromServer();

    public String getPropertyFilesOnSuccessDir();

    public String getPropertyFilesOnFailDir();

    public File getPropertyFilesLocalDir();

}
