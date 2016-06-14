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

//OODT imports
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

//JUnit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @author bfoster
 * @author rajith
 * @version $Revision$
 * 
 * <p>
 * Test Suite for the {@link AssignmentMonitor} service
 * </p>.
 */
public class TestAssignmentMonitor extends TestCase {

    private AssignmentMonitor assgnMon = null;

    protected void setUp() throws IOException {
        generateTestConfig();
        assgnMon = new AssignmentMonitorFactory().createMonitor();
    }

    public void testGetNodes() {
        List resNodes = null;

        try {
            resNodes = assgnMon.getNodes();
        } catch (MonitorException e) {
            fail(e.getMessage());
        }

        assertNotNull(resNodes);
        assertEquals(1, resNodes.size());
    }

    public void testGetNodeById() {
        ResourceNode node = null;

        try {
            node = assgnMon.getNodeById("localhost");
        } catch (MonitorException e) {
            fail(e.getMessage());
        }

        assertNotNull(node);
        assertEquals("localhost", node.getNodeId());
    }

    public void testGetNodeInfo() {
        List resNodes = null;

        try {
            resNodes = assgnMon.getNodes();
        } catch (MonitorException e) {
            fail(e.getMessage());
        }

        assertNotNull(resNodes);

        boolean hasNode1 = false;

        for (Object resNode : resNodes) {
            ResourceNode node = (ResourceNode) resNode;
            assertNotNull(node);
            if (node.getNodeId().equals("localhost")) {
                hasNode1 = true;
                assertEquals(node.getIpAddr().toExternalForm(),
                    "http://localhost:2001");
            }
            assertEquals(node.getCapacity(), 8);
        }

        assertTrue(hasNode1);
    }

    public void testNodeModification() throws MonitorException,
            MalformedURLException {
        List<ResourceNode> nodes = new Vector<ResourceNode>(this.assgnMon
                .getNodes());
        ResourceNode test1 = new ResourceNode("Test1", new URL(
                "http://localhost:1111"), 9);
        ResourceNode test2 = new ResourceNode("Test2", new URL(
                "http://localhost:2222"), 9);
        ResourceNode test3 = new ResourceNode("Test3", new URL(
                "http://localhost:3333"), 9);
        this.assgnMon.addNode(test1);
        nodes.add(test1);
        this.assgnMon.addNode(test2);
        nodes.add(test2);
        this.assgnMon.addNode(test3);
        nodes.add(test3);

        assertTrue(nodes.containsAll(this.assgnMon.getNodes())
                && this.assgnMon.getNodes().containsAll(nodes));
        
        this.assgnMon.removeNodeById(test1.getNodeId());
        nodes.remove(test1);
        
        assertTrue(nodes.containsAll(this.assgnMon.getNodes())
                && this.assgnMon.getNodes().containsAll(nodes));
    }


    private void generateTestConfig() throws IOException {
        String propertiesFile = "." + File.separator + "src" + File.separator +
                "test" + File.separator + "resources" + File.separator + "test.resource.properties";

        System.getProperties().load(new FileInputStream(new File(propertiesFile)));

        System.setProperty("org.apache.oodt.cas.resource.nodes.dirs",
                "file:" + new File("." + File.separator + "src" + File.separator +
                        "main" + File.separator + "resources" + File.separator +
                        "examples").getAbsolutePath());
    }

}
