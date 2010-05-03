//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.url.handlers.imaps;

//JDK imports
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ImapsURLConnection(url);
    }

}
