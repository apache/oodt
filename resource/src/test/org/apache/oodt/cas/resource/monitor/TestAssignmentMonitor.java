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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//JUnit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test Suite for the {@link AssignmentMonitor} service
 * </p>.
 */
public class TestAssignmentMonitor extends TestCase {

    private AssignmentMonitor assgnMon = null;

    protected void setUp() {
        List uris = new Vector();
        uris.add(new File("./src/main/resources/examples").toURI().toString());
        assgnMon = new AssignmentMonitor(uris);
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

        for (Iterator i = resNodes.iterator(); i.hasNext();) {
            ResourceNode node = (ResourceNode) i.next();
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

}
