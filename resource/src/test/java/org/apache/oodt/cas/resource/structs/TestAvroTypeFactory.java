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

package org.apache.oodt.cas.resource.structs;

import junit.framework.TestCase;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class TestAvroTypeFactory extends TestCase {

    public void testAvroJobInput(){
        JobInput jobInput = GenericResourceManagerObjectFactory
                .getJobInputFromClassName("org.apache.oodt.cas.resource.structs.NameValueJobInput");

        assertNotNull(jobInput);
        Properties properties = new Properties();
        properties.setProperty("key","prop1");
        jobInput.configure(properties);

        JobInput afterJobInput = AvroTypeFactory.getJobInput(AvroTypeFactory.getAvroJobInput(jobInput));

        assertNotNull(afterJobInput);
        assertEquals(afterJobInput.getId(),jobInput.getId());


    }

    //Disabled until API impl can be finished  
    public void XtestAvroJob(){
        Job initJob = new Job();

        initJob.setId("id");
        initJob.setJobInputClassName("classname");
        initJob.setJobInstanceClassName("instClassName");
        initJob.setLoadValue(42);
        initJob.setQueueName("queueName");
        initJob.setStatus("status");
        initJob.setName("name");

        Job afterJob = AvroTypeFactory.getJob(AvroTypeFactory.getAvroJob(initJob));



        assertEquals("id",afterJob.getId());

        assertEquals("classname",afterJob.getJobInputClassName());

        assertEquals("instClassName",afterJob.getJobInstanceClassName());

        assertEquals(new Integer(42),afterJob.getLoadValue());

        assertEquals("name",afterJob.getName());

        assertEquals("queueName",afterJob.getQueueName());

        assertEquals("status",afterJob.getStatus());
    }

    public void testNameValueJobInput(){
        NameValueJobInput initNameValueJobInput = new NameValueJobInput();

        initNameValueJobInput.setNameValuePair("name","value");

        NameValueJobInput afterNameValueJobInput =(NameValueJobInput) AvroTypeFactory.getJobInput(
                AvroTypeFactory.getAvroJobInput(
                        initNameValueJobInput));

        assertEquals(initNameValueJobInput.getId(),afterNameValueJobInput.getId());
        assertEquals("value", afterNameValueJobInput.getProps().getProperty("name"));

    }

    public void testAvroResourceNode(){
        ResourceNode initResourceNode = new ResourceNode();

        initResourceNode.setCapacity(42);

        initResourceNode.setId("id");

        try {
            initResourceNode.setIpAddr(new URL("http://localhost"));
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }

        ResourceNode afterResourceNode = AvroTypeFactory.getResourceNode(AvroTypeFactory.getAvroResourceNode(initResourceNode));

        assertEquals(initResourceNode.getCapacity(),afterResourceNode.getCapacity());

        assertEquals(initResourceNode.getIpAddr(),afterResourceNode.getIpAddr());

        assertEquals(initResourceNode.getNodeId(),afterResourceNode.getNodeId());

    }
}
