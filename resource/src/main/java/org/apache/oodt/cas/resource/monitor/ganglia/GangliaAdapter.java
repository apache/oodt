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

//OODT imports
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Cluster;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Host;
import org.apache.oodt.cas.resource.monitor.ganglia.configuration.Metric;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

//JDK imports
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rajith
 * @author mattmann
 * @version $Revision$
 */
public class GangliaAdapter {

    private static final String ENCODING = "ISO-8859-1";
    public static final int PORT = -9999;

    private String host;
    
    private int port;
    
    
    public GangliaAdapter(String host, int port){
       	this.host = host;
       	this.port = port;
    }

    protected GangliaAdapter(){
    	this(null, PORT);
    }
    
    /**
     * Get resource nodes' status.
     * @return Map that contains status of resource nodes
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.MonitorException if an error occurred.
     */
    public Map<String, Map<String,String>> getResourceNodeStatus()
            throws MonitorException {
        List<Cluster> gridStatus = parseConfiguration(readXMLDump(this.host, this.port));
        return filterNodes(gridStatus);
    }
    
    /**
     * 
     * @return A string representation of the {@link #host}:{@link #port}
     */
    public String getUrlString(){
    	return this.host+":"+this.port;
    }

    /**
     * Filter out the nodes from the grid state ganglia configuration
     * @param gridStatus Ganglia meta daemon parsed grid status
     * @return resource node Map
     */
    private Map<String, Map<String,String>> filterNodes (List<Cluster> gridStatus){

        Map<String, Map<String,String>> nodes = new ConcurrentHashMap<String, Map<String,String>>();
        for (Cluster cluster : gridStatus) {
            for (Host host : cluster.getHosts()) {
                    Map<String, String> metrics = new ConcurrentHashMap<String, String>();
                    for (Metric metric : host.getMetrics()) {
                        metrics.put(metric.getName(), metric.getValue());
                    }
                    metrics.put(GangliaMetKeys.TN,host.getTn());
                    metrics.put(GangliaMetKeys.TMAX, host.getTmax());
                    metrics.put(GangliaMetKeys.IP, host.getIp());
                    metrics.put(GangliaMetKeys.NAME, host.getName());
                    nodes.put(host.getName(), metrics);
            }
        }
        return nodes;
    }

    /**
     * Get a XML dump from a ganglia meta daemon.
     * @return A String that contains all the dump
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.MonitorException {@link org.apache.oodt.cas.resource.monitor.exceptions.MonitorException}
     * if an error occurred during the read.
     */
    private String readXMLDump(String host, int port) throws MonitorException {
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
            throw new MonitorException
                    ("Unknown host: " + host + ":" + port + "-" + e.getMessage());
        } catch (IOException e) {
            throw new MonitorException
                    ("Unable to get the monitoring report from the GMeta daemon: "
                            + e.getMessage());
        }
        return buffer.toString().trim();
    }

    /**
     * Parse a configuration from a XML output of a Ganglia meta daemon.
     * @param buffer the XML buffer
     * @return a Configuration
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.MonitorException {@link org.apache.oodt.cas.resource.monitor.exceptions.MonitorException} if an error occurred
     */
    private List<Cluster> parseConfiguration(String buffer)
            throws MonitorException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser;
        GangliaXMLParser gangliaXMLParser;
        try {
            parser = factory.newSAXParser();
            gangliaXMLParser = new GangliaXMLParser();
            parser.parse(new InputSource(new StringReader(buffer)), gangliaXMLParser);

        } catch (ParserConfigurationException e) {
            throw new MonitorException("Error while parsing: " + e.getMessage());
        } catch (SAXException e) {
            throw new MonitorException("Error while parsing the XML: " + e.getMessage());
        } catch (IOException e) {
            throw new MonitorException("I/O error: " + e.getMessage());
        }
        return gangliaXMLParser.getGridConfiguration();
    }
}
