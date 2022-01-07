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

package org.apache.oodt.cas.workflow.system.rpc;

import org.apache.oodt.cas.workflow.system.WorkflowManager;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author radu
 * <p>
 * Create instaces server/client for {@link WorkflowManager} and {@link WorkflowManagerClient}
 */

public class RpcCommunicationFactory {

    private static final Logger logger = LoggerFactory.getLogger(RpcCommunicationFactory.class);

    private static String getRpcServerFactoryClassName() {
        return loadProperties().getProperty(WorkflowManager.WORKFLOW_SERVER_FACTORY_PROPERTY,
                AvroRpcWorkflowManagerFactory.class.getName());
    }
    
    private static String getRpcClientFactoryClassName() {
        return loadProperties().getProperty(WorkflowManager.WORKFLOW_CLIENT_FACTORY_PROPERTY,
                AvroRpcWorkflowManagerFactory.class.getName());
    }

    public static WorkflowManager createServer(int port) {
        try {
            WorkflowManagerFactory workflowManagerFactory =
                    (WorkflowManagerFactory) Class.forName(getRpcServerFactoryClassName()).newInstance();
            workflowManagerFactory.setPort(port);
            logger.debug("Using workflow manager server factory : {}", workflowManagerFactory.getClass());
            return workflowManagerFactory.createServer();
        } catch (Exception e) {
            logger.error("Error creating server", e);
            throw new IllegalStateException("Unable to create server", e);
        }
    }

    public static WorkflowManagerClient createClient(URL url) {
        try {
            WorkflowManagerFactory workflowManagerFactory =
                    (WorkflowManagerFactory) Class.forName(getRpcClientFactoryClassName()).newInstance();
            workflowManagerFactory.setUrl(url);
            logger.debug("Using workflow manager client factory : {}", workflowManagerFactory.getClass());
            return workflowManagerFactory.createClient();
        } catch (Exception e) {
            logger.error("Unable to create client", e);
            throw new IllegalStateException("Unable to create client", e);
        }
    }
    
    /**
     * Loads workflow manager properties
     *
     * @return
     */
    private static Properties loadProperties() {
        String propertiesFile = System.getProperty(WorkflowManager.PROPERTIES_FILE_PROPERTY, WorkflowManager.DEFAULT_PROPERTIES_FILE);
        InputStream prpFileStream = RpcCommunicationFactory.class.getResourceAsStream(propertiesFile);
        Properties properties = new Properties();
    
        if (prpFileStream != null) {
            try {
                properties.load(prpFileStream);
            } catch (IOException e) {
                logger.error("An error occurred when loading properties file: {}", propertiesFile, e);
            }
        } else {
            logger.warn("Properties file: '{}' could not be found. Skipped loading properties", propertiesFile);
        }
        
        return properties;
    }
}
