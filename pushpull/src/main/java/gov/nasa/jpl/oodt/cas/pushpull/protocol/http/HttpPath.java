//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.http;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolPath;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class HttpPath extends ProtocolPath {

    private static final long serialVersionUID = -7780059889413081800L;

    private URL link;

    private HttpPath parent;

    /**
     * Constructor
     * 
     * @param url
     *            The URL for this Path
     * @param isDir
     *            Tells whether this Path is a directory
     * @throws MalformedURLException
     */
    protected HttpPath(String virtualPath, boolean isDir, URL link,
            HttpPath parent) throws MalformedURLException {
        super(virtualPath, isDir);
        this.link = link;
        this.parent = parent;
    }

    protected URL getLink() {
        return this.link;
    }

    public ProtocolPath getParentPath() throws MalformedURLException {
        return this.parent;
    }
}
