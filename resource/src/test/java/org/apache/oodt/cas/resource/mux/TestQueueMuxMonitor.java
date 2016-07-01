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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.oodt.cas.resource.mux.mocks.MockMonitor;
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

//JUnit imports
import junit.framework.TestCase;

/**
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * Test Suite for the {@link QueueBatchMonitor} service
 * </p>.
 */
public class TestQueueMuxMonitor extends TestCase {

    private QueueMuxMonitor monitor;
    private MockMonitor mock1;
    private MockMonitor mock2;
    private ResourceNode superfluous;
    private QueueManager qm;
    Map<MockMonitor,List<ResourceNode>> map;

    protected void setUp() {
        try {
            //Map monitor to nodes list
            map = new ConcurrentHashMap<MockMonitor,List<ResourceNode>>();
            List<ResourceNode> nodes1 = getNodesList("mock-1");
            List<ResourceNode> nodes2 = getNodesList("mock-2");
            //Backend Manager setup
            BackendManager back = new StandardBackendManager();
            back.addSet("queue-1",(mock1 = addMonitor(0,map,nodes1)), null, null);
            back.addSet("queue-2",(mock2 = addMonitor(5,map,nodes2)), null, null);
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
            qm.addNodeToQueue((superfluous = new ResourceNode("superfluous-1",new URL("http://superfluous-1"),-2)).getNodeId(), "queue-3");
            monitor = new QueueMuxMonitor(back, qm);
        } catch (QueueManagerException e) {
            TestCase.fail("Unanticipated queue manager exception caught: "+e.getMessage());
        } catch (MalformedURLException e) {
            TestCase.fail("Unanticipated URL exception caught: "+e.getMessage());
        }
    }

    public void testGetLoad() {
        try {
            TestCase.assertEquals(mock1.load,monitor.getLoad(map.get(mock1).get(0)));
            TestCase.assertEquals(mock2.load,monitor.getLoad(map.get(mock2).get(0)));

            /*try {
                monitor.getLoad(superfluous);
                TestCase.fail("Exception not thrown for unknown queue.");
            } catch (MonitorException e) {
            }*/
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        }
    }

    public void testGetNodes() {
        try {
            List<ResourceNode> nodes = monitor.getNodes();
            for (ResourceNode rn :map.get(mock1))
                TestCase.assertTrue("Node: "+rn.getNodeId()+ " not found.", nodes.contains(rn));
            for (ResourceNode rn :map.get(mock2))
                TestCase.assertTrue("Node: "+rn.getNodeId()+ " not found.", nodes.contains(rn));
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        }
    }

    public void testGetNodeById() {
        try {
            TestCase.assertEquals(map.get(mock1).get(0),monitor.getNodeById("mock-1-1"));
            TestCase.assertEquals(map.get(mock2).get(0),monitor.getNodeById("mock-2-1"));
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        }
    }
    public void testGetNodeByURL() {
        try {
            TestCase.assertEquals(map.get(mock1).get(1),monitor.getNodeByURL(new URL("http://mock-1-2")));
            TestCase.assertEquals(map.get(mock2).get(1),monitor.getNodeByURL(new URL("http://mock-2-2")));
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        } catch (MalformedURLException e1) {
            TestCase.fail("Unanticipated URL exception caught: "+e1.getMessage());
        }
    }

    public void testReduceLoad() {
        try {
            TestCase.assertTrue(monitor.reduceLoad(map.get(mock1).get(2), 5));
            TestCase.assertTrue(monitor.reduceLoad(map.get(mock2).get(2), 3));
            TestCase.assertEquals(map.get(mock1).get(2).getCapacity(),25);
            TestCase.assertEquals(map.get(mock2).get(2).getCapacity(),27);
            try {
                monitor.reduceLoad(superfluous, 2);
                TestCase.fail("Exception not thrown for unknown queue.");
            } catch (MonitorException ignored) {}
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        }
    }

    public void testAssignLoad() {
        try {
            TestCase.assertTrue(monitor.assignLoad(map.get(mock1).get(2), 5));
            TestCase.assertTrue(monitor.assignLoad(map.get(mock2).get(2), 3));
            TestCase.assertEquals(map.get(mock1).get(2).getCapacity(),5);
            TestCase.assertEquals(map.get(mock2).get(2).getCapacity(),3);
            try {
                monitor.assignLoad(superfluous, 2);
                TestCase.fail("Exception not thrown for unknown queue.");
            } catch (MonitorException ignored) {}
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        }
    }

    public void testAddNode() {
        try {
            ResourceNode node = new ResourceNode("a-new-node",null,2);
            qm.addNodeToQueue(node.getNodeId(), "queue-1");
            monitor.addNode(node);
            TestCase.assertEquals(node,mock1.getAdded());
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        } catch (QueueManagerException e1) {
            TestCase.fail("Unanticipated queue manager exception caught: "+e1.getMessage());
        }
    }
    public void removeNodeById() {
        try {
            ResourceNode node = new ResourceNode("a-new-node",null,2);
            qm.addNodeToQueue(node.getNodeId(), "queue-1");
            monitor.addNode(node);
            TestCase.assertEquals(node,mock1.getAdded());
            monitor.removeNodeById(node.getNodeId());
            TestCase.assertEquals(null,mock1.getAdded());
        } catch(MonitorException e) {
            TestCase.fail("Unanticipated monitor exception caught: "+e.getMessage());
        } catch (QueueManagerException e1) {
            TestCase.fail("Unanticipated queue manager exception caught: "+e1.getMessage());
        }
    }

    private MockMonitor addMonitor(int load,Map<MockMonitor, List<ResourceNode>> map, List<ResourceNode> list) {
        MockMonitor mon = new MockMonitor(load, list, list.get(0), list.get(1), list.get(2));
        map.put(mon, list);
        return mon;
    }
    private List<ResourceNode> getNodesList(String prefix) {
        List<ResourceNode> nodes = new LinkedList<ResourceNode>();
        try {
            nodes.add(new ResourceNode(prefix+"-1",new URL("http://"+prefix+"-1"),10));
            nodes.add(new ResourceNode(prefix+"-2",new URL("http://"+prefix+"-2"),20));
            nodes.add(new ResourceNode(prefix+"-3",new URL("http://"+prefix+"-3"),30));
            nodes.add(new ResourceNode(prefix+"-4",new URL("http://"+prefix+"-4"),40));
        } catch (MalformedURLException e) {
            TestCase.fail("Unanticipated URL exception caught: "+e.getMessage());
        }
        return nodes;
    }
}
