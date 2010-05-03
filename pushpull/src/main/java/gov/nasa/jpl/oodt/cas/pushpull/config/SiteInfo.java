//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.config;

//JDK imports
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.RemoteSite;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class SiteInfo {

    private HashMap<String, RemoteSite> aliasAndRemoteSite;

    public SiteInfo() {
        aliasAndRemoteSite = new HashMap<String, RemoteSite>();
    }

    public void addSite(RemoteSite rs) {
        this.aliasAndRemoteSite.put(rs.getAlias(), rs);
    }

    public RemoteSite getSiteByAlias(String alias) {
        return this.aliasAndRemoteSite.get(alias);
    }

    public LinkedList<RemoteSite> getPossibleRemoteSites(String alias, URL url,
            String username, String password) {
        LinkedList<RemoteSite> remoteSites = new LinkedList<RemoteSite>();
        if (alias != null) {
            RemoteSite rs = this.aliasAndRemoteSite.get(alias);
            if (rs != null)
                remoteSites.add(rs);
            else if (url != null && username != null & password != null)
                remoteSites.add(new RemoteSite(alias, url, username, password));
        } else if (url != null) {
            Set<Entry<String, RemoteSite>> set = this.aliasAndRemoteSite
                    .entrySet();
            for (Entry<String, RemoteSite> entry : set) {
                RemoteSite rs = entry.getValue();
                if (rs.getURL().equals(url)
                        && (username == null || rs.getUsername().equals(
                                username))
                        && (password == null || rs.getPassword().equals(
                                password)))
                    remoteSites.add(rs);
            }
            if (remoteSites.size() == 0) {
                if (url != null && username != null && password != null)
                    remoteSites.add(new RemoteSite(url.toString(), url,
                            username, password));
            }
        } else if (username != null) {
            Set<Entry<String, RemoteSite>> set = this.aliasAndRemoteSite
                    .entrySet();
            for (Entry<String, RemoteSite> entry : set) {
                RemoteSite rs = entry.getValue();
                if (rs.getUsername().equals(username)
                        && (password == null || rs.getPassword().equals(
                                password)))
                    remoteSites.add(rs);
            }
        } else if (password != null) {
            Set<Entry<String, RemoteSite>> set = this.aliasAndRemoteSite
                    .entrySet();
            for (Entry<String, RemoteSite> entry : set) {
                RemoteSite rs = entry.getValue();
                if (rs.getPassword().equals(password))
                    remoteSites.add(rs);
            }
        }
        return remoteSites;
    }
}