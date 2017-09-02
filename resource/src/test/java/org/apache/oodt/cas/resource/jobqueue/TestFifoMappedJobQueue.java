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

import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.jobrepo.MemoryJobRepository;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobSpec;

import junit.framework.TestCase;

public class TestFifoMappedJobQueue extends TestCase {

  FifoMappedJobQueue q;

  private JobSpec[] jobs = new JobSpec[4];

  private final static JobRepository repo = new MemoryJobRepository();

  public TestFifoMappedJobQueue() {
  }

  protected void setUp() {
    q = new FifoMappedJobQueue(2, repo);
    jobs[0] = new JobSpec(null,
        new Job("job0q0", "j0", null, null, "queue0", new Integer(1)));
    jobs[1] = new JobSpec(null,
        new Job("job0q1", "j1", null, null, "queue1", new Integer(1)));
    jobs[2] = new JobSpec(null,
        new Job("job1q0", "j2", null, null, "queue0", new Integer(1)));
    jobs[3] = new JobSpec(null,
        new Job("job2q0", "j3", null, null, "queue0", new Integer(1)));

    try {
      q.addQueue("queue0");
      q.addQueue("queue1");
      q.addQueue("queue2");
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetNextJob() {
    // Ensure that requesting a job from a valid, empty queue returns a null job
    // spec
    try {
      JobSpec s = q.getNextJob("queue2");
      assertNull(s);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testPurge() {
    try {
      q.addJob(this.jobs[0]);
      q.addJob(this.jobs[1]);
      q.purge();
      assertEquals(q.getSize(), 0);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testIsEmpty() {

    try {
      for (String queueName : q.getQueueNames()) {
        assertTrue(q.isEmpty(queueName));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetJobRepository() {
    JobRepository r = q.getJobRepository();
    assertSame(r, this.repo);
  }

  public void testGetCapacity() {
    assertEquals(2, q.getCapacity());
  }
}