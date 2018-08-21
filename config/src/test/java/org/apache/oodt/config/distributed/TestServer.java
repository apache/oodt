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

import org.apache.curator.test.TestingServer;

import java.io.IOException;

public class TestServer {

    public static void main(String[] args) throws Exception {
        @SuppressWarnings("resource")
        final TestingServer zookeeper = new TestingServer(2181);
        zookeeper.start();
        System.out.printf(zookeeper.getConnectString());

        zookeeper.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    zookeeper.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread.currentThread().join();
    }
}
