//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol;

//JDK imports
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
public class RemoteSite {
    private String alias, username, password, cdTestDir;

    private URL url;

    public RemoteSite(String alias, URL url, String username, String password) {
        this.alias = alias;
        this.username = username;
        this.password = password;
        this.url = url;
    }
    
    public RemoteSite(String alias, URL url, String username, String password, String cdTestDir) {
        this(alias, url, username, password);
        this.cdTestDir = cdTestDir;
    }

    public String getAlias() {
        return this.alias;
    }

    public URL getURL() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
    
    public String getCdTestDir() {
        return this.cdTestDir;
    }

    public void copy(RemoteSite rs) {
        this.alias = rs.alias;
        this.url = rs.url;
        this.username = rs.username;
        this.password = rs.password;
        this.cdTestDir = rs.cdTestDir;
    }

    public boolean equals(Object obj) {
        if (obj instanceof RemoteSite) {
            RemoteSite rs = (RemoteSite) obj;
            return (rs.alias.equals(this.alias) && rs.url.equals(this.url)
                    && rs.username.equals(this.username) && rs.password
                    .equals(this.password));
        } else
            return false;
    }

    public String toString() {
        return "RemoteSite: alias = '" + this.alias + "'  url = '" + this.url
                + "'  username = '" + this.username + "' cdTestDir = '" 
                + this.cdTestDir + "'";
    }
}
