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

package org.apache.oodt.cas.resource.system.distributed;

import org.apache.oodt.cas.resource.system.AvroRpcResourceManager;
import org.apache.oodt.cas.resource.system.ResourceManager;
import org.apache.oodt.cas.resource.system.TestAvroRpcResourceManager;
import org.apache.oodt.config.distributed.cli.ConfigPublisher;
import org.apache.oodt.config.test.AbstractDistributedConfigurationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.oodt.config.Constants.Properties.ENABLE_DISTRIBUTED_CONFIGURATION;
import static org.junit.Assert.fail;

/**
 * Test the operation of Resource Manager under distributed configuration management enabled
 *
 * @author Imesha Sudasingha
 */
public class TestDistributedAvroRpcResourceManager extends AbstractDistributedConfigurationTest {

    private static final int RM_PORT = 50001;
    private static final String CONF_PUBLISHER_XML = "config/distributed/config-publisher.xml";

    private ResourceManager resourceManager;

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
            resourceManager = new AvroRpcResourceManager(RM_PORT);
            resourceManager.startUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDynSetNodeCapacity() {
        new TestAvroRpcResourceManager().testDynSetNodeCapacity();
    }

    @After
    public void tearDownTest() throws Exception {
        if (resourceManager != null) {
            resourceManager.shutdown();
        }

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONF_PUBLISHER_XML,
                "-a", "clear"
        });

        System.clearProperty("org.apache.oodt.cas.cli.action.spring.config");
        System.clearProperty("org.apache.oodt.cas.cli.option.spring.config");
        System.clearProperty(ENABLE_DISTRIBUTED_CONFIGURATION);
    }
}
