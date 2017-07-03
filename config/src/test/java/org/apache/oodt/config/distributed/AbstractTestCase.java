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
import org.apache.curator.test.TestingServer;
import org.apache.oodt.config.distributed.utils.CuratorUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;

public abstract class AbstractTestCase {

    protected static TestingServer zookeeper;
    protected static CuratorFramework client;

    @BeforeClass
    public static void setUp() throws Exception {
        zookeeper = new TestingServer();
        zookeeper.start();

        System.setProperty(ZK_CONNECT_STRING, zookeeper.getConnectString());

        client = CuratorUtils.newCuratorFrameworkClient(zookeeper.getConnectString());
        client.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        client.close();
        zookeeper.stop();
    }
}
