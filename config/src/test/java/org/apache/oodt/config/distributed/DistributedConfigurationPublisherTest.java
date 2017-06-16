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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.apache.oodt.config.Constants.Components.FILE_MANAGER;
import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;

public class DistributedConfigurationPublisherTest {

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
        String args[] = new String[]{
                FILE_MANAGER,
                "filemgr.properties=/etc/filemgr.properties",
                "mime-types.xml=/etc/mime-types.xml"
        };
        DistributedConfigurationPublisher.main(args);

        String[] fileMappings = Arrays.copyOfRange(args, 1, args.length);
        ZPaths zPaths = new ZPaths(FILE_MANAGER);
        for (String mapping : fileMappings) {
            String[] parts = mapping.split("=");
            String zNodePath = zPaths.getPropertiesZNodePath(parts[1]);

            Assert.assertNotNull(client.checkExists().forPath(zNodePath));

            String storedContent = new String(client.getData().forPath(zNodePath));

            URI file = Thread.currentThread().getContextClassLoader().getResource(parts[0]).toURI();
            String fileContent = FileUtils.readFileToString(new File(file));

            Assert.assertEquals(fileContent, storedContent);
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        client.close();
        zookeeper.stop();
    }
}