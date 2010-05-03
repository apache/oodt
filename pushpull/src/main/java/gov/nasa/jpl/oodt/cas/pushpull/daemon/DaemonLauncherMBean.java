//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.daemon;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface DaemonLauncherMBean {

    public void killAllDaemons();

    public void refreshDaemons();

    public void quit();

    public String[] viewDaemonWaitingList();

}
