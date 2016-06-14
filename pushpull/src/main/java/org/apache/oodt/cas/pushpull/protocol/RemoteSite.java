/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.pushpull.protocol;

//JDK imports
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /* our log stream */
    private final static Logger LOG = Logger.getLogger(RemoteSite.class
        .getName());

    private String alias, username, password, cdTestDir;
    private int maxConnections;
    private URL url;

    public RemoteSite(String alias, URL url, String username, String password) {
        this.alias = alias;
        this.username = username;
        this.password = password;
        this.url = url;
        this.maxConnections = -1;
    }
    
    public RemoteSite(String alias, URL url, String username, String password, String cdTestDir) {
        this(alias, url, username, password);
        this.cdTestDir = cdTestDir;
    }
    
    public RemoteSite(String alias, URL url, String username, String password, String cdTestDir, int maxConnections) {
        this(alias, url, username, password, cdTestDir);
        this.maxConnections = maxConnections;
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

    public int getMaxConnections() {
    	return this.maxConnections;
    }
    
    public void copy(RemoteSite rs) {
        this.alias = rs.alias;
        this.url = rs.url;
        this.username = rs.username;
        this.password = rs.password;
        this.cdTestDir = rs.cdTestDir;
        this.maxConnections = rs.maxConnections;
    }

    public boolean equals(Object obj) {
        if (obj instanceof RemoteSite) {
            RemoteSite rs = (RemoteSite) obj;
            try {
                return (rs.alias.equals(this.alias) && rs.url.toURI().equals(this.url.toURI())
                        && rs.username.equals(this.username) && rs.password
                        .equals(this.password) && rs.maxConnections == this.maxConnections);
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "Could not convert URL to URL: Message: "+e.getMessage());
            }
        } else {
            return false;
        }
        return false;
    }

    public String toString() {
        return "RemoteSite: alias = '" + this.alias + "'  url = '" + this.url
                + "'  username = '" + this.username + "' cdTestDir = '" 
                + this.cdTestDir + "' maxConnections = '" + this.maxConnections + "'";
    }

    @Override
    public int hashCode() {
        int result = alias != null ? alias.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (cdTestDir != null ? cdTestDir.hashCode() : 0);
        result = 31 * result + maxConnections;
        try {
            result = 31 * result + (url != null ? url.toURI().hashCode() : 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return result;
    }
}
