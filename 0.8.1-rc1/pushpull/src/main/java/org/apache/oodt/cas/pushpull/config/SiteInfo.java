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


package org.apache.oodt.cas.pushpull.config;

//JDK imports
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

//OODT imports
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;

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
