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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.oodt.config.distributed.cli.DistributedConfigurationPublisher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Map;

public class DistributedConfigurationPublisherTest {

    private static final String DISTRIBUTED_CONFIG_PUBLISHER_SPRING_CONFIG = "etc/config-publisher.xml";

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
    }

    @Test
    public void publishConfigurationTest() throws Exception {
        DistributedConfigurationPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-publish"
        });

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(DISTRIBUTED_CONFIG_PUBLISHER_SPRING_CONFIG);
        Map distributedConfigurationPublishers = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

        for (Object bean : distributedConfigurationPublishers.values()) {
            DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;
            Assert.assertTrue(publisher.verifyPublishedConfiguration());
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        client.close();
        zookeeper.stop();
    }
}