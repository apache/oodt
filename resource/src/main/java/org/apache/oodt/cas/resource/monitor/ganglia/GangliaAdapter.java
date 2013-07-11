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

import org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Cluster;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Host;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Metric;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author rajith
 * @version $Revision$
 */
public class GangliaAdapter {

    private static String ENCODING = "ISO-8859-1";

    /**
     * Get resource nodes' status.
     * @return List that contains status of resource nodes
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException {@link org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException} if an error occurred.
     */
    public static HashMap<String, HashMap> getResourceNodeStatus(Set requiredNodes)
            throws GangliaMonitorException {
        String host = System
                .getProperty("org.apache.oodt.cas.resource.monitor.ganglia.gemtad.host.address");
        int port = Integer.valueOf(System
                .getProperty("org.apache.oodt.cas.resource.monitor.ganglia.gemtad.host.port"));

        List<Cluster> gridStatus = parseConfiguration(readXMLDump(host, port));
        return filterRequiredNodes(requiredNodes, gridStatus);
    }

    /**
     * Filter out the required nodes from the grid state ganglia configuration
     * @param requiredNodes the required nodes
     * @param gridStatus Ganglia meta daemon parsed grid status
     * @return filtered resource node HashMap
     */
    private static HashMap<String, HashMap> filterRequiredNodes (Set requiredNodes,
                                                                 List<Cluster> gridStatus){

        HashMap<String, HashMap> filteredNodes = new HashMap<String, HashMap>();
        for (Cluster cluster : gridStatus) {
            for (Host host : cluster.getHosts()) {
                if(requiredNodes.contains(host.getName())){
                    HashMap<String, String> metrics = new HashMap<String, String>();
                    for (Metric metric : host.getMetrics()) {
                        metrics.put(metric.getName(), metric.getValue());
                    }
                    metrics.put(GangliaMetKeys.TN,host.getTn());
                    metrics.put(GangliaMetKeys.TMAX, host.getTmax());
                    filteredNodes.put(host.getName(), metrics);
                }
            }
        }
        return filteredNodes;
    }

    /**
     * Get a XML dump from a ganglia meta daemon.
     * @return A String that contains all the dump
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException {@link org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException}
     * if an error occurred during the read.
     */
    private static String readXMLDump(String host, int port) throws GangliaMonitorException {
        StringBuilder buffer = new StringBuilder();

        try {
            Socket s = new Socket(host, port);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(s.getInputStream(), ENCODING));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (UnknownHostException e) {
            throw new GangliaMonitorException
                    ("Unknown host: " + host + ":" + port + "-" + e.getMessage());
        } catch (IOException e) {
            throw new GangliaMonitorException
                    ("Unable to get the monitoring report from the GMeta daemon: "
                            + e.getMessage());
        }
        return buffer.toString().trim();
    }

    /**
     * Parse a configuration from a XML output of a Ganglia meta daemon.
     * @param buffer the XML buffer
     * @return a Configuration
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException {@link org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException} if an error occurred
     */
    private static List<Cluster> parseConfiguration(String buffer)
            throws GangliaMonitorException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser;
        GangliaXMLParser gangliaXMLParser;
        try {
            parser = factory.newSAXParser();
            gangliaXMLParser = new GangliaXMLParser();
            parser.parse(new InputSource(new StringReader(buffer)), gangliaXMLParser);

        } catch (ParserConfigurationException e) {
            throw new GangliaMonitorException("Error while parsing: " + e.getMessage());
        } catch (SAXException e) {
            throw new GangliaMonitorException("Error while parsing the XML: " + e.getMessage());
        } catch (IOException e) {
            throw new GangliaMonitorException("I/O error: " + e.getMessage());
        }
        return gangliaXMLParser.getGridConfiguration();
    }
}
