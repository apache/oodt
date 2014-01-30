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

package org.apache.oodt.cas.resource.monitor.ganglia;

/**
 * @author rajith
 * @version $Revision$
 */
public interface GangliaMetKeys {

    public static final String CLUSTER = "CLUSTER";
    public static final String HOST = "HOST";
    public static final String METRIC = "METRIC";

    public static final String NAME = "NAME";
    public static final String VAL = "VAL";
    public static final String TN = "TN";
    public static final String TMAX = "TMAX";
    public static final String DMAX = "DMAX";

    /*Metric specific keys*/
    public static final String TYPE = "TYPE";
    public static final String UNITS = "UNITS";
    public static final String SLOPE = "SLOPE";
    public static final String SOURCE = "SOURCE";
    public static final String EXTRA_ELEMENT = "EXTRA_ELEMENT";
    public static final String GROUP = "GROUP";
    public static final String DESC = "DESC";
    public static final String TITLE = "TITLE";

    /*Host specific keys*/
    public static final String IP = "IP";
    public static final String REPORTED = "REPORTED";
    public static final String LOCATION = "LOCATION";
    public static final String GMOND_STARTED = "GMOND_STARTED";

    /*Cluster specific keys*/
    public static final String LOCALTIME = "LOCALTIME";
    public static final String OWNER = "OWNER";
    public static final String LATLONG = "LATLONG";
    public static final String URL = "URL";


}
