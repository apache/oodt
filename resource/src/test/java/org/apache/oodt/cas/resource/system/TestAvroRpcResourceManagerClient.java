/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.resource.system;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for the XmlRpcResourceManagerClient to ensure communications between client and server operate correctly.
 */
public class TestAvroRpcResourceManagerClient {

    private static final int RM_PORT = 50001;

    private static AvroRpcResourceManagerClient rmc;
    private static AvroRpcResourceManager rm;

    @BeforeClass
    public static void setUp() throws Exception {
        generateTestConfiguration();
        rm = new AvroRpcResourceManager(RM_PORT);
        rm.startUp();
        rmc = new AvroRpcResourceManagerClient(new URL("http://localhost:" + RM_PORT));
    }

    private static void generateTestConfiguration() throws IOException {
        Properties config = new Properties();

        String propertiesFile = "." + File.separator + "src" + File.separator +
                "test" + File.separator + "resources" + File.separator + "test.resource.properties";
        System.getProperties().load(new FileInputStream(new File(propertiesFile)));

        // stage policy
        File tmpPolicyDir = null;
        try {
            tmpPolicyDir = File.createTempFile("test", "ignore").getParentFile();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (File policyFile : new File("./src/test/resources/policy")
                .listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().endsWith(".xml");
                    }
                })) {
            try {
                FileUtils.copyFileToDirectory(policyFile, tmpPolicyDir);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        config.setProperty("org.apache.oodt.cas.resource.nodes.dirs", tmpPolicyDir
                .toURI().toString());
        config.setProperty("org.apache.oodt.cas.resource.nodetoqueues.dirs",
                tmpPolicyDir.toURI().toString());

        System.getProperties().putAll(config);

    }

    @Test
    public void testGetNodes() throws MonitorException {
        List<Hashtable> nodes = rmc.getNodes();

        assertThat(nodes, is(not(nullValue())));
        assertThat(nodes, hasSize(1));

    }

    @Test
    public void testGetExecutionReport() throws JobRepositoryException {
        String execreport = rmc.getExecReport();
        assertThat(execreport, is(not(nullValue())));
        //TODO make it return more than an empty string;
    }


    @Test
    public void testJobQueueCapacity() throws JobRepositoryException {
        int capacity = rmc.getJobQueueCapacity();
        assertThat(capacity, equalTo(1000));
    }

    @Test
    public void testGetJobQueueSize() throws JobRepositoryException {
        int size = rmc.getJobQueueSize();
        assertThat(size, equalTo(0));
        //TODO Make it change queue size
    }

    @Test
    public void testGetNodeById() throws MonitorException {
        List<ResourceNode> nodelist = rmc.getNodes();

        ResourceNode node = rmc.getNodeById(nodelist.get(0).getNodeId());

        assertThat(node, is(not(nullValue())));

        assertThat(node.getNodeId(), equalTo("localhost"));
    }


    @Test
    public void testGetNodeLoad() throws MonitorException {

        List<ResourceNode> nodelist = rmc.getNodes();

        String node = rmc.getNodeLoad(nodelist.get(0).getNodeId());

        assertNotNull(node);

        assertThat(node, equalTo("0/8"));

    }

    @Test
    public void testNodeReport() throws MonitorException {
        String report = rmc.getNodeReport();

        assertThat(report, is(not(nullValue())));
    }

    @Test
    public void testGetNodesInQueue() throws QueueManagerException {
        List<String> nodes = rmc.getNodesInQueue("long");

        assertThat(nodes, is(not(nullValue())));

        assertThat(nodes, hasSize(1));

    }


    @Test
    public void testQueuedJobs() throws JobQueueException {
        List jobs = rmc.getQueuedJobs();

        assertThat(jobs, is(not(nullValue())));

        //TODO queue a job
    }

    @Test
    public void testQueuesWithNode() throws MonitorException, QueueManagerException {
        List<ResourceNode> nodelist = rmc.getNodes();


        List<String> queues = rmc.getQueuesWithNode(nodelist.get(0).getNodeId());
        assertThat(queues, hasSize(3));

        assertThat(queues, containsInAnyOrder("high", "quick", "long"));
    }

    @Test
    public void testQueues() throws QueueManagerException {
        List<String> queues = rmc.getQueues();

        assertThat(queues, hasSize(3));

        assertThat(queues, containsInAnyOrder("high", "quick", "long"));
    }

    @AfterClass
    public static void tearDown() {
        rm.shutdown();
    }
}
