//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.sftp.j2ssh;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.Protocol;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFactory;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.sftp.j2ssh.SftpClient;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class SftpClientFactory implements ProtocolFactory {

    public Protocol newInstance() {
        return new SftpClient();
    }

}
