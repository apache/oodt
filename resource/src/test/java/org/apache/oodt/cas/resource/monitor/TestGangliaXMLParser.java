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

package org.apache.oodt.cas.resource.monitor;

//Junit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys;
import org.apache.oodt.cas.resource.monitor.ganglia.GangliaXMLParser;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Cluster;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Host;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Metric;

//JDK imports
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * @author rajith
 * @author mattmann
 * @version $Revision$
 *
 * Test Suite for the {@link GangliaXMLParser}
 */
public class TestGangliaXMLParser extends TestCase {

    private List<Cluster> gridConfiguration;

    /**
     * {@inheritDoc}
     * Read gangliaXMLdump.xml and build the grid configuration
     */
    protected void setUp() throws MonitorException, IOException {
        StringBuilder stringBuffer = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("." + File.separator +
                    "src" + File.separator + "test" + File.separator + "resources" + File.separator + "resourcemon"
                    + File.separator + "gangliaXMLdump.xml"));
            String line = reader.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throw new IOException("Unable to read the sample monitoring report from the file: "
                    + e.getMessage());
        }

        String buffer = stringBuffer.toString().trim();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser;
        GangliaXMLParser gangliaXMLParser;
        try {
            parser = factory.newSAXParser();
            gangliaXMLParser = new GangliaXMLParser();
            parser.parse(new InputSource(new StringReader(buffer)), gangliaXMLParser);
            gridConfiguration = gangliaXMLParser.getGridConfiguration();
        } catch (ParserConfigurationException e) {
            throw new MonitorException("Error while parsing: " + e.getMessage());
        } catch (SAXException e) {
            throw new MonitorException("Error while parsing the XML: " + e.getMessage());
        } catch (IOException e) {
            throw new MonitorException("I/O error: " + e.getMessage());
        }
    }

    /**
     * test the "<CLUSTER>" tag data
     * <CLUSTER NAME="" LOCALTIME="" OWNER="" LATLONG="" URL="">
     */
    public void testClusterTag() {
        Cluster cluster = gridConfiguration.get(0);
        assertEquals("localcluster", cluster.getName());
        assertEquals("1370187645", cluster.getLocaltime());
        assertEquals("oodt", cluster.getOwner());
        assertEquals("N32.87 W117.22", cluster.getLatLong());
        assertEquals("http://www.mycluster.org/", cluster.getUrl());
    }

    /**
     * test the "<HOST>" tag data
     * <HOST NAME="" IP="" REPORTED="" TN="" TMAX="" DMAX="" LOCATION="" GMOND_STARTED="">
     */
    public void testHostTag() {
        Host host = ((gridConfiguration.get(0)).getHosts()).get(0);
        assertEquals("localhost", host.getName());
        assertEquals("127.0.0.1", host.getIp());
        assertEquals("1370187637", host.getReported());
        assertEquals("10", host.getTn());
        assertEquals("20", host.getTmax());
        assertEquals("0", host.getDmax());
        assertEquals("0,0,0", host.getLocation());
        assertEquals("1370186237", host.getGmondstarted());
    }

    /**
     * test the "<METRIC>" tag data
     * <METRIC NAME="" VAL="" TYPE="" UNITS="" TN="" TMAX="" DMAX="" SLOPE="" SOURCE="">
     * <EXTRA_DATA>
     * <EXTRA_ELEMENT NAME="" VAL=""/>
     * <EXTRA_ELEMENT NAME="" VAL=""/>
     * ........
     * </EXTRA_DATA>
     * </METRIC>
     */
    public void testMetricTag() {
        Metric metric = ((((gridConfiguration.get(0)).getHosts()).get(0)).getMetrics()).get(0);
        assertEquals("disk_free", metric.getName());
        assertEquals("307.790", metric.getValue());
        assertEquals("double", metric.getType());
        assertEquals("GB", metric.getUnits());
        assertEquals("143", metric.getTn());
        assertEquals("180", metric.getTmax());
        assertEquals("0", metric.getDmax());
        assertEquals("both", metric.getSlope());
        assertEquals("gmond", metric.getSource());

        //extra data of the metric
        ConcurrentHashMap<String, String> extraData = metric.getExtraData();
        assertEquals("disk", extraData.get(GangliaMetKeys.GROUP));
        assertEquals("Total free disk space", extraData.get(GangliaMetKeys.DESC));
        assertEquals("Disk Space Available", extraData.get(GangliaMetKeys.TITLE));
    }

    /**
     * test the clusters in the grid
     */
    public void testGridClusters(){
        Cluster localCluster = gridConfiguration.get(0);
        Cluster remoteCluster = gridConfiguration.get(1);
        assertEquals("localcluster", localCluster.getName());
        assertEquals("remotecluster", remoteCluster.getName());
    }
}
