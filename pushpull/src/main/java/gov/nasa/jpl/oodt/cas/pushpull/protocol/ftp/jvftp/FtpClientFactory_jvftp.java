//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.ftp.jvftp;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.Protocol;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFactory;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class FtpClientFactory_jvftp implements ProtocolFactory {

    public Protocol newInstance() {
        return new FtpClient_jvftp();
    }

}
