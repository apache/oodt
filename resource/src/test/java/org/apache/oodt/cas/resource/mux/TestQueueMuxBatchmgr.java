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
import org.apache.oodt.cas.resource.mux.mocks.MockBatchManager;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;

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
public class TestQueueMuxBatchmgr extends TestCase {

    private QueueMuxBatchManager queue;
    private MockBatchManager mock1;
    private MockBatchManager mock2;

    protected void setUp() {
        BackendManager back = new StandardBackendManager();
        back.addSet("queue-1", null,(mock1 = new MockBatchManager()), null);
        back.addSet("queue-2", null,(mock2 = new MockBatchManager()), null);
        queue = new QueueMuxBatchManager(back);       
    }

    public void testExecuteRemotely() {
        try {
            
            //Test that the jobs are put in seperate mock-backends based on queues
            ResourceNode node1 = new ResourceNode();
            ResourceNode node2 = new ResourceNode();
    
            JobSpec spec1 = this.getSpecFromQueue("queue-1");
            queue.executeRemotely(spec1, node1);
    
            JobSpec spec2 = this.getSpecFromQueue("queue-2");
            queue.executeRemotely(spec2, node2);
            //Yes...use reference equality, as these must be the exact same object
            TestCase.assertEquals(spec1,mock1.getCurrentJobSpec());
            TestCase.assertEquals(spec2,mock2.getCurrentJobSpec());
            TestCase.assertEquals(node1,mock1.getCurrentResourceNode());
            TestCase.assertEquals(node2,mock2.getCurrentResourceNode());
            //Throws exception on bad queue
            try {
                queue.executeRemotely(this.getSpecFromQueue("queue-3"),node1);
                TestCase.fail("Failed to throw JobExecutionException on unknown queue.");
            } catch(JobExecutionException ignored) {}
        } catch (JobExecutionException e) {
           TestCase.fail("Unexpected Exception: "+e.getMessage());
        }
    }

    public void testKillJob() {
        try {
            ResourceNode node1 = new ResourceNode();
            ResourceNode node2 = new ResourceNode();
    
            JobSpec spec1 = this.getSpecFromQueue("queue-1");
            queue.executeRemotely(spec1, node1);
    
            JobSpec spec2 = this.getSpecFromQueue("queue-2");
            queue.executeRemotely(spec2, node2);
            //Make sure that one can kill a job, and the other job is running
            TestCase.assertTrue(queue.killJob(spec1.getJob().getId(), node1));
            TestCase.assertEquals(mock1.getCurrentJobSpec(),null);
            TestCase.assertEquals(mock2.getCurrentJobSpec(),spec2);
            //Make sure kill fails with bad queue
            TestCase.assertFalse(queue.killJob(this.getSpecFromQueue("queue-3").getJob().getId(), node1));
        } catch (JobExecutionException e) {
            TestCase.fail("Unexpected Exception: "+e.getMessage());
        }
    }

    public void testGetExecNode() {
        try {
            ResourceNode node1 = new ResourceNode();
            ResourceNode node2 = new ResourceNode();
            node1.setId("Node1-ID");
            node2.setId("Node2-ID");           
            JobSpec spec1 = this.getSpecFromQueue("queue-1");
            queue.executeRemotely(spec1, node1);
    
            JobSpec spec2 = this.getSpecFromQueue("queue-2");
            queue.executeRemotely(spec2, node2);
            //Make that the execution node is same
            TestCase.assertEquals(node1.getNodeId(),queue.getExecutionNode(spec1.getJob().getId()));
            TestCase.assertEquals(node2.getNodeId(),queue.getExecutionNode(spec2.getJob().getId()));
            //Returns null, if bad-queue
            TestCase.assertNull(queue.getExecutionNode(this.getSpecFromQueue("queue-3").getJob().getId()));
        } catch (JobExecutionException e) {
            TestCase.fail("Unexpected Exception: "+e.getMessage());
        }
    }

    private JobSpec getSpecFromQueue(String queue) {
        JobSpec spec1 = new JobSpec();
        Job job1 = new Job();
        job1.setId("000000100000011-"+queue);
        job1.setQueueName(queue);
        spec1.setJob(job1);       
        return spec1;
    }
}
