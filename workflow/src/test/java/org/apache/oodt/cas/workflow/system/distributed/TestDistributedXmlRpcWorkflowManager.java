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

package org.apache.oodt.cas.workflow.system.distributed;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManager;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;
import org.apache.oodt.config.distributed.cli.ConfigPublisher;
import org.apache.oodt.config.test.AbstractDistributedConfigurationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.apache.oodt.config.Constants.Properties.ENABLE_DISTRIBUTED_CONFIGURATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestDistributedXmlRpcWorkflowManager extends AbstractDistributedConfigurationTest {

    private static final int WM_PORT = 50002;
    private static final String CONF_PUBLISHER_XML = "config/distributed/config-publisher.xml";

    private XmlRpcWorkflowManager workflowManager;

    @Before
    public void setUpTest() throws Exception {
        System.setProperty("org.apache.oodt.cas.cli.action.spring.config", "../config/src/main/resources/cmd-line-actions.xml");
        System.setProperty("org.apache.oodt.cas.cli.option.spring.config", "../config/src/main/resources/cmd-line-options.xml");
        System.setProperty(ENABLE_DISTRIBUTED_CONFIGURATION, "true");

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONF_PUBLISHER_XML,
                "-a", "publish"
        });

        try {
            workflowManager = new XmlRpcWorkflowManager(WM_PORT);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        startWorkflow();
    }

    private void startWorkflow() {
        XmlRpcWorkflowManagerClient client = null;
        try {
            client = new XmlRpcWorkflowManagerClient(new URL("http://localhost:" + WM_PORT));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            client.sendEvent("long", new Metadata());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testGetWorkflowInstances() {
        List workflowInsts = null;
        int numInsts = -1;
        while (numInsts != 2) {
            try {
                workflowInsts = workflowManager.getWorkflowInstances();
            } catch (Exception e) {
                e.printStackTrace();
            }

            assertNotNull(workflowInsts);
            numInsts = workflowInsts.size();
        }

        assertEquals(2, workflowInsts.size());
    }

    private void deleteAllFiles(String startDir) {
        File startDirFile = new File(startDir);
        File[] delFiles = startDirFile.listFiles();

        if (delFiles != null && delFiles.length > 0) {
            for (File delFile : delFiles) {
                delFile.delete();
            }
        }

        startDirFile.delete();
    }

    @After
    public void tearDownTest() throws Exception {
        if (workflowManager != null) {
            workflowManager.shutdown();
        }

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONF_PUBLISHER_XML,
                "-a", "clear"
        });

        System.clearProperty("org.apache.oodt.cas.cli.action.spring.config");
        System.clearProperty("org.apache.oodt.cas.cli.option.spring.config");
        System.clearProperty(ENABLE_DISTRIBUTED_CONFIGURATION);

        String luceneIdx = System.getProperty("org.apache.oodt.cas.workflow.instanceRep.lucene.idxPath");
        if (luceneIdx != null) {
            luceneIdx = PathUtils.replaceEnvVariables(luceneIdx);
            deleteAllFiles(luceneIdx);
        }
    }
}
