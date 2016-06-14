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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rajith
 * @version $Revision$
 *
 * Configuration element for the Ganglia Metric mapping.
 *
 *  <METRIC NAME="" VAL="" TYPE="" UNITS="" TN="" TMAX="" DMAX="" SLOPE="" SOURCE="">
 *      <EXTRA_DATA>
 *          <EXTRA_ELEMENT NAME="" VAL=""/>
 *          <EXTRA_ELEMENT NAME="" VAL=""/>
 *          ........
 *      </EXTRA_DATA>
 *  </METRIC>
 */
public class Metric {

    private String name;
    private String value;
    private String type;
    private String units;
    private String tn;
    private String tmax;
    private String dmax;
    private String slope;
    private String source;
    private ConcurrentHashMap<String, String> extraData;

    public Metric(String name, String value, String valueType, String units,
                  String tn, String tmax, String dmax, String slope, String source) {
        this.name = name;
        this.value = value;
        this.type = valueType;
        this.units = units;
        this.tn = tn;
        this.tmax = tmax;
        this.dmax = dmax;
        this.slope = slope;
        this.source = source;
    }


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getUnits() {
        return units;
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

    public String getSlope() {
        return slope;
    }

    public String getSource() {
        return source;
    }

    public ConcurrentHashMap<String, String> getExtraData() {
        return extraData;
    }

    public void setExtraData(ConcurrentHashMap<String, String> extraData) {
        this.extraData = extraData;
    }
}
