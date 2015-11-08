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

import org.apache.oodt.cas.resource.monitor.ganglia.GangliaResourceMonitor;
import org.apache.oodt.cas.resource.monitor.ganglia.GangliaResourceMonitorFactory;
import org.apache.oodt.cas.resource.monitor.utils.MockGmetad;
import org.apache.oodt.cas.resource.structs.ResourceNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//OODT imports
//JDK imports

/**
 * @author rajith
 * @author mattmann
 * @version $Revision$
 *
 * Test Suite for the {@link org.apache.oodt.cas.resource.monitor.ganglia.GangliaResourceMonitor}
 */
public class TestGangliaResourceMonitor extends TestCase {
    private static Logger LOG = Logger.getLogger(TestGangliaResourceMonitor.class.getName());

    private GangliaResourceMonitor gangliaResourceMonitor;
    private ThreadLocal<MockGmetad> mockGmetad = new ThreadLocal<MockGmetad>();

    @Override
    protected void setUp() throws IOException {
        generateTestConfig();
        runMockGmetad();

        gangliaResourceMonitor = (GangliaResourceMonitor)
                new GangliaResourceMonitorFactory().createMonitor();
    }

    @Override
    protected void tearDown(){
        mockGmetad.remove();
    }

    public void testGetLoad() {
        try {
            ResourceNode resourceNode = new ResourceNode();
            resourceNode.setId("localhost");
            assertEquals(1, gangliaResourceMonitor.getLoad(resourceNode));
        } catch (Exception e) {
        	LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }
    }

    public void testRemoveNodeById(){
        try {
            gangliaResourceMonitor.removeNodeById("localhost");
            assertNull(gangliaResourceMonitor.getNodeById("remotenode"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }
    }
    
    public void testGetNodes(){
    	try{
    		List<ResourceNode> nodes = gangliaResourceMonitor.getNodes();
    		assertNotNull(nodes);
    		assertEquals(3, nodes.size());
    		boolean hasLocal = false;
    		boolean hasLocal2 = false;
    		boolean hasRemote = false;
    		
    		for(ResourceNode node: nodes){
    			if(node.getNodeId().equals("localhost")){
    				hasLocal = true;
    			}
    			else if(node.getNodeId().equals("localhost2")){
    				hasLocal2 = true;
    			}
    			else if(node.getNodeId().equals("remotenode")){
    				hasRemote = true;
    			}
    			
    		}
    		assertTrue(hasLocal&&hasLocal2&&hasRemote);
    	}
    	catch (Exception e){
    		LOG.log(Level.SEVERE, e.getMessage());
    		fail(e.getMessage());
    	}
    }
    
    public void testGetNodeById(){
    	try{
    		ResourceNode node = gangliaResourceMonitor.getNodeById("localhost");
    		assertNotNull(node);
    		assertEquals("localhost", node.getNodeId());
    		node = gangliaResourceMonitor.getNodeById("localhost2");
    		assertNotNull(node);
    		assertEquals("localhost2", node.getNodeId());
    		node = gangliaResourceMonitor.getNodeById("remotenode");
    		assertNotNull(node);
    		assertEquals("remotenode", node.getNodeId());
    	}
    	catch(Exception e){
    		LOG.log(Level.SEVERE, e.getMessage());
    		fail(e.getMessage());
    	}
    }

    private void runMockGmetad() {
        int port = Integer.valueOf(System
                .getProperty("org.apache.oodt.cas.resource.monitor.ganglia.gemtad.host.port"));
        String sampleXMLfilePath = "." + File.separator + "src" + File.separator +
                "test" + File.separator + "resources" + File.separator + "resourcemon" + File.separator + "gangliaXMLdump.xml";
        mockGmetad.set(new MockGmetad(port, sampleXMLfilePath));
        Thread mockGmetadServer = new Thread(mockGmetad.get());
        mockGmetadServer.start();
    }

    private void generateTestConfig() throws IOException {
        String propertiesFile = "." + File.separator + "src" + File.separator +
                "test" + File.separator + "resources" + File.separator + "test.resource.properties";
        System.getProperties().load(new FileInputStream(new File(propertiesFile)));
    }

}
