//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.url.handlers.sftp;

//JDK imports
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class SftpURLConnection extends URLConnection {

    protected SftpURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        // do nothing!!!
    }

}
