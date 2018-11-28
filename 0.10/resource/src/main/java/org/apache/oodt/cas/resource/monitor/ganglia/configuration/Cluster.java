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

package org.apache.oodt.cas.resource.monitor.ganglia.configuration;

import java.util.List;

/**
 * @author rajith
 * @version $Revision$
 *
 * Configuration element for the Ganglia cluster mapping. Contains hosts' status information
 * of a particular cluster.
 *
 *  <CLUSTER NAME="" LOCALTIME="" OWNER="" LATLONG="" URL="">
 *      <HOST NAME=....
 *      ...
 *      <HOST NAME=..
 *  </CLUSTER>
 */
public class Cluster {

    private String name;
    private String localtime;
    private String owner;
    private String latLong;
    private String url;
    private List<Host> hosts;

    public Cluster(String name, String localtime, String owner, String latLong, String url) {
        this.name = name;
        this.localtime = localtime;
        this.owner = owner;
        this.latLong = latLong;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getLocaltime() {
        return localtime;
    }

    public String getLatLong() {
        return latLong;
    }

    public String getUrl() {
        return url;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }
}
