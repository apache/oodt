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
package org.apache.oodt.cas.resource.util;

import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.NameValueJobInput;


import org.apache.oodt.cas.resource.util.MesosUtilities;

//JUnit imports
import junit.framework.TestCase;

/**
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * Test Suite for the {@link MesosUtilities} class
 * </p>.
 */
public class TestMesosUtilities extends TestCase {

    public void testSerialization() {
        JobSpec js = new JobSpec();

        Job job = new Job();
        job.setId("crazy-id");
        job.setJobInputClassName(NameValueJobInput.class.getCanonicalName());
        job.setJobInstanceClassName("Instance Class");
        job.setLoadValue(new Integer(352));
        job.setName("A Name");
        job.setQueueName("Queue");
        job.setStatus("Status");

        String[] props = {"prop-1","prop-2","prop-3"};
        NameValueJobInput nvji = new NameValueJobInput();
        for (String str : props)
            nvji.setNameValuePair(str, str+"val");

        js.setIn(nvji);
        js.setJob(job);

        JobSpec ns;
        try {
            ns = MesosUtilities.byteStringToJobSpec(MesosUtilities.jobSpecToByteString(js));
            TestCase.assertEquals(js.getJob().getId(),ns.getJob().getId());
            TestCase.assertEquals(js.getJob().getJobInputClassName(),ns.getJob().getJobInputClassName());
            TestCase.assertEquals(js.getJob().getJobInstanceClassName(),ns.getJob().getJobInstanceClassName());
            TestCase.assertEquals(js.getJob().getLoadValue(),ns.getJob().getLoadValue());
            TestCase.assertEquals(js.getJob().getName(),ns.getJob().getName());
            TestCase.assertEquals(js.getJob().getQueueName(),ns.getJob().getQueueName());
            TestCase.assertEquals(js.getJob().getStatus(),ns.getJob().getStatus());
            TestCase.assertEquals(js.getIn().getId(),ns.getIn().getId());
            for (String str : props)
                TestCase.assertEquals(nvji.getValue(str),((NameValueJobInput)ns.getIn()).getValue(str));
        } catch (ClassNotFoundException e) {
            TestCase.fail("Unexpected exception:"+e.getLocalizedMessage());
        } catch (InstantiationException e) {
            TestCase.fail("Unexpected exception:"+e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            TestCase.fail("Unexpected exception:"+e.getLocalizedMessage());
        }

    }

}
