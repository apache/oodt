//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFile;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface DownloadListener {

    public void downloadStarted(ProtocolFile pFile);

    public void downloadFinished(ProtocolFile pFile);

    public void downloadFailed(ProtocolFile pFile, String errorMsg);

}
