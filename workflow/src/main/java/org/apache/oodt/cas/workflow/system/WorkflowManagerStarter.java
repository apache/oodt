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

package org.apache.oodt.cas.workflow.system;

import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkflowManagerStarter {

    private static final Logger LOG = Logger.getLogger(WorkflowManagerStarter.class.getName());

    public static void loadProperties() throws FileNotFoundException, IOException {
        String configFile = System.getProperty(WorkflowManager.PROPERTIES_FILE_PROPERTY);
        if (configFile != null) {
            LOG.log(Level.INFO,
                    "Loading Workflow Manager Configuration Properties from: ["
                            + configFile + "]");
            System.getProperties().load(new FileInputStream(new File(
                    configFile)));
        }
    }


    public static void main(String[] args) throws Exception {
        int portNum = -1;
        String usage = "WorkflowManager --portNum <port number for avro rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }

        loadProperties();
        final WorkflowManager manager = RpcCommunicationFactory.createServer(portNum);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                manager.shutdown();
            }
        });

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, String.format("Interrupted while executing: %s", e.getMessage()));
            manager.shutdown();
        }
    }
}
