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

	/* XML specific keys */
    String CLUSTER = "CLUSTER";
    String HOST = "HOST";
    String METRIC = "METRIC";

    String NAME = "NAME";
    String VAL = "VAL";
    String TN = "TN";
    String TMAX = "TMAX";
    String DMAX = "DMAX";

    /*Metric specific keys*/
    String TYPE = "TYPE";
    String UNITS = "UNITS";
    String SLOPE = "SLOPE";
    String SOURCE = "SOURCE";
    String EXTRA_ELEMENT = "EXTRA_ELEMENT";
    String GROUP = "GROUP";
    String DESC = "DESC";
    String TITLE = "TITLE";

    /*Host specific keys*/
    String IP = "IP";
    String REPORTED = "REPORTED";
    String LOCATION = "LOCATION";
    String GMOND_STARTED = "GMOND_STARTED";

    /*Cluster specific keys*/
    String LOCALTIME = "LOCALTIME";
    String OWNER = "OWNER";
    String LATLONG = "LATLONG";
    String URL = "URL";
    
    /*Ganglia metric keys*/
    String LOAD_ONE = "load_one";
    String LOAD_FIVE = "load_five";
    String LOAD_FIFTEEN = "load_fifteen";
    String CPU_NUM = "cpu_num";

    /* Various needed keys */
    int MAXIMUM_FRACTION_DIGITS = 3;



}
