//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.daemon;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface DaemonListener {

    public void wasRegisteredWith(Daemon daemon);

    public void wasUnregisteredWith(Daemon daemon);

    public void daemonStarting(Daemon daemon);

    public void daemonFinished(Daemon daemon);

}
