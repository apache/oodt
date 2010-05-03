//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class ProtocolFile {

    protected RemoteSite remoteSite;

    protected ProtocolPath path;

    public ProtocolFile(RemoteSite remoteSite, ProtocolPath path) {
        this.path = path;
        this.remoteSite = remoteSite;
    }

    public ProtocolFile getParentFile() throws MalformedURLException {
        return new ProtocolFile(this.remoteSite, path.getParentPath());
    }

    public String getName() {
        return path.getFileName();
    }

    public boolean isDirectory() {
        return path.isDirectory();
    }

    public boolean isRelativeToHOME() {
        return this.path.isRelativeToHOME();
    }

    public String getHostName() {
        return remoteSite.getURL().getHost();
    }

    public URL getURL() {
        return this.remoteSite.getURL();
    }

    public ProtocolPath getProtocolPath() {
        return path;
    }

    public void setPath(ProtocolPath path) {
        this.path = path;
    }

    public RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public boolean equals(Object protocolFile) {
        if (protocolFile instanceof ProtocolFile) {
            ProtocolFile pf = (ProtocolFile) protocolFile;
            return pf.remoteSite.equals(this.remoteSite)
                    && (pf.getProtocolPath().equals(this.getProtocolPath()));
        }
        return false;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public String toString() {
        return remoteSite.getURL() + path.toString();
    }

}
