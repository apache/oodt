//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TimeAndThreadCount {

    private long startTimeInMillis;

    private int threadCount;

    public TimeAndThreadCount(long startTimeInMillis, int threadCount) {
        this.startTimeInMillis = startTimeInMillis;
        this.threadCount = threadCount;
    }

    public long getStartTimeInMillis() {
        return this.startTimeInMillis;
    }

    public int getThreadCount() {
        return this.threadCount;
    }

}