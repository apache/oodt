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

package org.apache.oodt.cas.resource.mux;

//OODT imports

import junit.framework.TestCase;
import org.apache.oodt.cas.resource.mux.mocks.MockMonitor;
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

//JUnit imports

/**
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * Test Suite for the {@link QueueMuxMonitor} service
 * </p>.
 */
public class TestQueueMuxMonitor extends TestCase {

    private static int LOAD_1 = 5;
    private static int LOAD_2 = 10;
    private static int SUPERFLUOUS_CAPACITY = -2;
    private QueueMuxMonitor monitor;
    private ResourceNode superfluous;
    private QueueManager qm;
    private List<ResourceNode> nodes1;
    private List<ResourceNode> nodes2;

    protected void setUp() {
        try {
            nodes1 = getNodesList("mock-1");
            nodes2 = getNodesList("mock-2");
            //Backend Manager setup
            BackendManager back = new StandardBackendManager();
            back.addSet("queue-1", new MockMonitor(nodes1), null, null);
            back.addSet("queue-2", new MockMonitor(nodes2), null, null);
            //Make sure the queue manager is setup
            qm = new QueueManager();
            qm.addQueue("queue-1");
            qm.addQueue("queue-2");
            qm.addQueue("queue-3");
            for (ResourceNode rn : nodes1)
                qm.addNodeToQueue(rn.getNodeId(), "queue-1");
            for (ResourceNode rn : nodes2)
                qm.addNodeToQueue(rn.getNodeId(), "queue-2");
            //Add an extra node to test "unknown queue"
            qm.addNodeToQueue((superfluous = new ResourceNode("superfluous-1", new URL("http://superfluous-1"), SUPERFLUOUS_CAPACITY)).getNodeId(), "queue-3");
            monitor = new QueueMuxMonitor(back, qm);
        } catch (QueueManagerException e) {
            TestCase.fail("Unanticipated queue manager exception caught: " + e.getMessage());
        } catch (MalformedURLException e) {
            TestCase.fail("Unanticipated URL exception caught: " + e.getMessage());
        }
    }

    public void testGetLoad() {
        try {
            ResourceNode n1 = nodes1.get(0);
            ResourceNode n2 = nodes2.get(0);
            TestCase.assertEquals(0, monitor.getLoad(n1));
            TestCase.assertEquals(0, monitor.getLoad(n2));
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        }
    }

    public void testGetNodes() {
        try {
            List<ResourceNode> nodes = monitor.getNodes();
            for (ResourceNode rn : nodes1)
                TestCase.assertTrue("Node: " + rn.getNodeId() + " not found.", nodes.contains(rn));
            for (ResourceNode rn : nodes2)
                TestCase.assertTrue("Node: " + rn.getNodeId() + " not found.", nodes.contains(rn));
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        }
    }

    public void testGetNodeById() {
        try {
            ResourceNode n1 = nodes1.get(1);
            ResourceNode n2 = nodes2.get(1);
            TestCase.assertEquals(n1, monitor.getNodeById(n1.getNodeId()));
            TestCase.assertEquals(n2, monitor.getNodeById(n2.getNodeId()));
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        }
    }

    public void testGetNodeByURL() {
        try {
            ResourceNode n1 = nodes1.get(2);
            ResourceNode n2 = nodes2.get(2);
            TestCase.assertEquals(n1, monitor.getNodeByURL(n1.getIpAddr()));
            TestCase.assertEquals(n2, monitor.getNodeByURL(n2.getIpAddr()));
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        }
    }

    public void testReduceLoad() {
        try {
            ResourceNode n1 = nodes1.get(3);
            ResourceNode n2 = nodes2.get(3);
            int c1_exp = n1.getCapacity() + LOAD_1;
            int c2_exp = n2.getCapacity() + LOAD_2;
            TestCase.assertTrue(monitor.reduceLoad(n1, LOAD_1));
            TestCase.assertTrue(monitor.reduceLoad(n2, LOAD_2));
            TestCase.assertEquals(n1.getCapacity(), c1_exp);
            TestCase.assertEquals(n2.getCapacity(), c2_exp);

            try {
                monitor.reduceLoad(superfluous, SUPERFLUOUS_CAPACITY);
                TestCase.fail("Exception not thrown for unknown queue.");
            } catch (MonitorException ignored) {
            }
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        }
    }

    public void testAssignLoad() {
        try {
            ResourceNode n1 = nodes1.get(3);
            ResourceNode n2 = nodes2.get(3);
            int c1_exp = n1.getCapacity() - LOAD_1;
            int c2_exp = n2.getCapacity() - LOAD_2;
            TestCase.assertTrue(monitor.assignLoad(n1, LOAD_1));
            TestCase.assertTrue(monitor.assignLoad(n2, LOAD_2));
            TestCase.assertEquals(n1.getCapacity(), c1_exp);
            TestCase.assertEquals(n2.getCapacity(), c2_exp);

            try {
                monitor.assignLoad(superfluous, SUPERFLUOUS_CAPACITY);
                TestCase.fail("Exception not thrown for unknown queue.");
            } catch (MonitorException ignored) {
            }
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        }
    }

    public void testAddNode() {
        try {
            ResourceNode node = new ResourceNode("a-new-node", null, 2);
            qm.addNodeToQueue(node.getNodeId(), "queue-1");
            monitor.addNode(node);
            TestCase.assertTrue(monitor.getNodes().contains(node));
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        } catch (QueueManagerException e1) {
            TestCase.fail("Unanticipated queue manager exception caught: " + e1.getMessage());
        }
    }

    public void testRemoveNodeById() {
        try {
            ResourceNode node = new ResourceNode("a-new-node", null, 2);
            qm.addNodeToQueue(node.getNodeId(), "queue-1");
            monitor.addNode(node);
            monitor.removeNodeById(node.getNodeId());
            TestCase.assertFalse(monitor.getNodes().contains(node));
        } catch (MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: " + e.getMessage());
        } catch (QueueManagerException e1) {
            TestCase.fail("Unanticipated queue manager exception caught: " + e1.getMessage());
        }
    }

    private List<ResourceNode> getNodesList(String prefix) {
        List<ResourceNode> nodes = new LinkedList<>();
        try {
            nodes.add(new ResourceNode(prefix + "-1", new URL("http://" + prefix + "-1"), 10));
            nodes.add(new ResourceNode(prefix + "-2", new URL("http://" + prefix + "-2"), 20));
            nodes.add(new ResourceNode(prefix + "-3", new URL("http://" + prefix + "-3"), 30));
            nodes.add(new ResourceNode(prefix + "-4", new URL("http://" + prefix + "-4"), 40));
        } catch (MalformedURLException e) {
            TestCase.fail("Unanticipated URL exception caught: " + e.getMessage());
        }
        return nodes;
    }
}
