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

package org.apache.oodt.config.distributed;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.config.distributed.cli.ConfigPublisher;
import org.apache.oodt.config.test.AbstractDistributedConfigurationTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Testing the functionality of {@link DistributedConfigurationPublisher} and its CLI
 *
 * @author Imesha Sudasingha
 */
public class DistributedConfigurationPublisherTest extends AbstractDistributedConfigurationTest {

    private static final String CONFIG_PUBLISHER_XML = "config-publisher.xml";

    @Before
    public void setUpTest() {
        System.setProperty("org.apache.oodt.cas.cli.action.spring.config", "src/main/resources/cmd-line-actions.xml");
        System.setProperty("org.apache.oodt.cas.cli.option.spring.config", "src/main/resources/cmd-line-options.xml");
    }

    @After
    public void tearDownTest() {
        System.clearProperty("org.apache.oodt.cas.cli.action.spring.config");
        System.clearProperty("org.apache.oodt.cas.cli.option.spring.config");
    }

    @Test
    public void publishConfigurationTest() throws Exception {
        // Publishing configuration through CLI and verifying whether they were stored correctly
        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-a", "publish"
        });

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_PUBLISHER_XML);
        Map distributedConfigurationPublishers = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

        List<DistributedConfigurationPublisher> publishers = new ArrayList<>(distributedConfigurationPublishers.values().size());
        for (Object bean : distributedConfigurationPublishers.values()) {
            publishers.add((DistributedConfigurationPublisher) bean);
        }

        for (DistributedConfigurationPublisher publisher : publishers) {
            Assert.assertTrue(publisher.verifyPublishedConfiguration());

            ZNodePaths zNodePaths = publisher.getZNodePaths();

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getPropertiesFiles().entrySet()) {
                String zNodePath = zNodePaths.getPropertiesZNodePath(entry.getValue());
                Assert.assertNotNull(client.checkExists().forPath(zNodePath));

                String storedContent = new String(client.getData().forPath(zNodePath));
                String fileContent = FileUtils.readFileToString(new File(entry.getKey()));
                Assert.assertEquals(fileContent, storedContent);
            }

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String zNodePath = zNodePaths.getConfigurationZNodePath(entry.getValue());
                Assert.assertNotNull(client.checkExists().forPath(zNodePath));

                String storedContent = new String(client.getData().forPath(zNodePath));
                String fileContent = FileUtils.readFileToString(new File(entry.getKey()));
                Assert.assertEquals(fileContent, storedContent);
            }
        }

        // Clearing configuration through CLI and checking whether the configuration has actually been gone
        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-a", "clear"
        });

        for (DistributedConfigurationPublisher publisher : publishers) {
            Assert.assertFalse(publisher.verifyPublishedConfiguration());

            ZNodePaths zNodePaths = publisher.getZNodePaths();

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getPropertiesFiles().entrySet()) {
                String zNodePath = zNodePaths.getPropertiesZNodePath(entry.getValue());
                Assert.assertNull(client.checkExists().forPath(zNodePath));
            }

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String zNodePath = zNodePaths.getConfigurationZNodePath(entry.getValue());
                Assert.assertNull(client.checkExists().forPath(zNodePath));
            }
        }

        for (DistributedConfigurationPublisher publisher : publishers) {
            publisher.destroy();
        }
    }
}