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
 * Configuration element for the Ganglia host mapping. Consists with Metrics information
 * of a particular host
 *
 *  <HOST NAME="" IP="" REPORTED="" TN="" TMAX="" DMAX="" LOCATION="" GMOND_STARTED="">
 *      <METRIC NAME="" VAL="" TYPE="" UNITS="" TN="" TMAX="" DMAX="" SLOPE="" SOURCE="">
 *      ............
 *      <METRIC NAME=....
 *  </HOST>
 */
public class Host {

    private String name;
    private String ip;
    private String reported;
    private String tn;
    private String tmax;
    private String dmax;
    private String location;
    private String gmondstarted;
    private List<Metric> metrics;

    public Host(String name, String ip, String reported, String tn, String tmax,
                String dmax, String location, String gmondstarted) {
        this.name = name;
        this.ip = ip;
        this.reported = reported;
        this.tn = tn;
        this.tmax = tmax;
        this.dmax = dmax;
        this.location = location;
        this.gmondstarted = gmondstarted;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getReported() {
        return reported;
    }

    public String getTn() {
        return tn;
    }

    public String getTmax() {
        return tmax;
    }

    public String getDmax() {
        return dmax;
    }

    public String getLocation() {
        return location;
    }

    public String getGmondstarted() {
        return gmondstarted;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
}
