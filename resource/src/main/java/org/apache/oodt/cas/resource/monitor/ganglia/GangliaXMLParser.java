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

import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Cluster;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Host;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Metric;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * @author rajith
 * @version $Revision$
 *
 * SAX parser to create a grid configuration from a XML stream.
 * XML schema corresponds to a GMetad XML output.
 */
public class GangliaXMLParser extends DefaultHandler implements GangliaMetKeys {

    private List<Cluster> grid;
    private Cluster currentCluster;
    private List<Host> currentClusterHosts;
    private Host currentHost;
    private List<Metric> currentHostMetrics;
    private Metric currentMetric;
    private ConcurrentHashMap<String, String> extraData;

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals(METRIC)) {
            this.currentMetric.setExtraData(extraData);
            this.currentHostMetrics.add(currentMetric);

        } else if (name.equals(HOST)) {
            this.currentHost.setMetrics(currentHostMetrics);
            this.currentClusterHosts.add(currentHost);

        } else if (name.equals(CLUSTER)) {
            this.currentCluster.setHosts(currentClusterHosts);
            this.grid.add(currentCluster);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startDocument() throws SAXException {
        this.grid = new ArrayList<Cluster>();
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (name.equals(METRIC)) {
            this.currentMetric = new Metric(atts.getValue(NAME), atts.getValue(VAL), atts.getValue(TYPE),
                    atts.getValue(UNITS), atts.getValue(TN), atts.getValue(TMAX), atts.getValue(DMAX),
                    atts.getValue(SLOPE), atts.getValue(SOURCE));
            this.extraData = new ConcurrentHashMap<String, String>();

        } else if (name.equals(EXTRA_ELEMENT)) {
            this.extraData.put(atts.getValue(NAME), atts.getValue(VAL));

        } else if (name.equals(HOST)) {
            this.currentHost = new Host(atts.getValue(NAME), atts.getValue(IP), atts.getValue(REPORTED),
                    atts.getValue(TN), atts.getValue(TMAX), atts.getValue(DMAX), atts.getValue(LOCATION),
                    atts.getValue(GMOND_STARTED));
            this.currentHostMetrics = new ArrayList<Metric>();

        } else if (name.equals(CLUSTER)) {
            this.currentCluster = new Cluster(atts.getValue(NAME), atts.getValue(LOCALTIME), atts.getValue(OWNER),
                    atts.getValue(LATLONG), atts.getValue(URL));
            this.currentClusterHosts = new ArrayList<Host>();
        }
    }

    /**
     * Get the configuration after parsing the XML Stream.
     *
     * @return a grid configuration
     */
    public List<Cluster> getGridConfiguration() {
        return this.grid;
    }
}
