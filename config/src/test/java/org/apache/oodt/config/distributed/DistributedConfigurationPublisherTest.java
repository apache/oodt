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
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;

public class DistributedConfigurationPublisherTest {

    private static final String DISTRIBUTED_CONFIG_PUBLISHER_SPRING_CONFIG = "distributed-config-publisher.xml";

    private static TestingServer zookeeper;
    private static CuratorFramework client;

    @BeforeClass
    public static void setUp() throws Exception {
        zookeeper = new TestingServer();
        zookeeper.start();

        client = CuratorFrameworkFactory.builder()
                .connectString(zookeeper.getConnectString())
                .retryPolicy(new RetryNTimes(3, 1000))
                .build();
        client.start();

        System.setProperty(ZK_CONNECT_STRING, zookeeper.getConnectString());
    }

    @Test
    public void publishConfiguration() throws Exception {
        DistributedConfigurationPublisher.main(new String[]{DISTRIBUTED_CONFIG_PUBLISHER_SPRING_CONFIG});

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(DISTRIBUTED_CONFIG_PUBLISHER_SPRING_CONFIG);
        Map distributedConfigurationPublisher = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

        for (Object bean : distributedConfigurationPublisher.values()) {
            DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;
            ZNodePaths zNodePaths = publisher.getZNodePaths();

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getPropertiesFiles().entrySet()) {
                String zNodePath = zNodePaths.getPropertiesZNodePath(entry.getValue());

                Assert.assertNotNull(client.checkExists().forPath(zNodePath));

                String storedContent = new String(client.getData().forPath(zNodePath));

                URI file = Thread.currentThread().getContextClassLoader().getResource(entry.getKey()).toURI();
                String fileContent = FileUtils.readFileToString(new File(file));

                Assert.assertEquals(fileContent, storedContent);
            }

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String zNodePath = zNodePaths.getConfigurationZNodePath(entry.getValue());

                Assert.assertNotNull(client.checkExists().forPath(zNodePath));

                String storedContent = new String(client.getData().forPath(zNodePath));

                URI file = Thread.currentThread().getContextClassLoader().getResource(entry.getKey()).toURI();
                String fileContent = FileUtils.readFileToString(new File(file));

                Assert.assertEquals(fileContent, storedContent);
            }
        }

//        DistributedConfigurationManager configurationManager = new DistributedConfigurationManager(Constants.Components.FILE_MANAGER);
//        configurationManager.loadConfiguration();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        client.close();
        zookeeper.stop();
    }
}