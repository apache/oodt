//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem;

//JDK imports
import java.io.File;
import java.util.LinkedList;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DownloadingFileInfo {

    private File downloadingFile;

    private long startTimeInMillis;

    private LinkedList<TimeAndThreadCount> timeAndThreadCountList;

    public DownloadingFileInfo(File downloadingFile, long startTimeInMillis,
            int currentThreadCount) {
        timeAndThreadCountList = new LinkedList<TimeAndThreadCount>();
        this.downloadingFile = downloadingFile;
        this.startTimeInMillis = startTimeInMillis;
        this.timeAndThreadCountList.add(new TimeAndThreadCount(
                startTimeInMillis, currentThreadCount));
    }

    public void updateThreadCount(long timeInMillis, int threadCount) {
        this.timeAndThreadCountList.add(new TimeAndThreadCount(timeInMillis,
                threadCount));
    }

    public File getDownloadingFile() {
        return this.downloadingFile;
    }

    public long getStartTimeInMillis() {
        return this.startTimeInMillis;
    }

    public LinkedList<TimeAndThreadCount> getTimeAndThreadInfo() {
        return this.timeAndThreadCountList;
    }
}