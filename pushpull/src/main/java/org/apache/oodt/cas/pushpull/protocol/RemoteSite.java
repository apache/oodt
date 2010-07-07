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
