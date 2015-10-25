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


package org.apache.oodt.cas.resource.jobqueue;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.jobrepo.MemoryJobRepository;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobSpec;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test Suite for the {@link JobStack} class.
 * </p>.
 */
public class TestJobStack extends TestCase {

    private JobSpec job1 = null;

    private JobSpec job2 = null;

    private final static int waitTime = 20;

    private final static JobRepository repo = new MemoryJobRepository();

    public TestJobStack() {
        Job j1 = new Job();
        j1.setId("booger");
        j1.setName("pick it");

        JobInput in = null;

        job1 = new JobSpec(in, j1);

        Job j2 = new Job();
        j2.setId("booger2");
        j2.setName("pick some more");
        job2 = new JobSpec(in, j2);
    }

    protected void setUp() {

    }

    public void testAddJob() {
        JobStack stack = new JobStack(waitTime, repo);

        List queuedJobs = null;

        try {
            queuedJobs = stack.getQueuedJobs();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(queuedJobs);
        assertEquals(0, queuedJobs.size());

        try {
            stack.addJob(job1);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals(1, queuedJobs.size());
    }

    public void testIsEmpty() {
        JobStack stack = new JobStack(waitTime, repo);
        assertTrue(stack.isEmpty());
    }

    public void testPurge() {
        JobStack stack = new JobStack(waitTime, repo);
        assertNotNull(stack);

        try {
            stack.addJob(job1);
            stack.addJob(job2);
            stack.addJob(job2);

            List queuedJobs = stack.getQueuedJobs();
            assertNotNull(queuedJobs);
            assertEquals(3, queuedJobs.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            stack.purge();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(stack.isEmpty());

    }

    public void testQueuedJobs() {
        JobStack stack = new JobStack(waitTime, repo);
        List queuedJobs = null;

        try {
            stack.addJob(job1);
            queuedJobs = stack.getQueuedJobs();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(queuedJobs);
        assertEquals(1, queuedJobs.size());

    }

    public void testGetNextJob() {
        JobStack stack = new JobStack(waitTime, repo);
        JobSpec nextJob;

        try {
            stack.addJob(job1);
            stack.addJob(job2);
            stack.addJob(job2);

            List queuedJobs = stack.getQueuedJobs();

            nextJob = stack.getNextJob();
            assertEquals(nextJob.getJob().getId(), job1.getJob().getId());
            assertEquals(queuedJobs.size(), 2);
            nextJob = stack.getNextJob();
            assertEquals(nextJob.getJob().getId(), job2.getJob().getId());
            assertEquals(queuedJobs.size(), 1);
            nextJob = stack.getNextJob();
            assertEquals(nextJob.getJob().getId(), job2.getJob().getId());
            assertEquals(queuedJobs.size(), 0);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
