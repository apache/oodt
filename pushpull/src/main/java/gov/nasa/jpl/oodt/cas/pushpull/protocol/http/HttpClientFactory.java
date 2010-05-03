//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.http;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.Protocol;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFactory;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.http.HttpClient;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class HttpClientFactory implements ProtocolFactory {

    public Protocol newInstance() {
        try {
            return new HttpClient();
        } catch (Exception e) {
            System.out
                    .println("ERROR: creating HttpClient - check httpclient.properties");
            return null;
        }
    }

}
