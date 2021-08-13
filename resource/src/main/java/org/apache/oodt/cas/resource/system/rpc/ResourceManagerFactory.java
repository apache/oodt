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

package org.apache.oodt.cas.resource.system.rpc;

import org.apache.oodt.cas.resource.system.AvroRpcResourceManager;
import org.apache.oodt.cas.resource.system.AvroRpcResourceManagerClient;
import org.apache.oodt.cas.resource.system.ResourceManager;
import org.apache.oodt.cas.resource.system.ResourceManagerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

public class ResourceManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerFactory.class);
    
    private static void loadProperties() {
        // set up the configuration, if there is any
        if (System.getProperty(ResourceManager.RESMGR_PROPERTIES_FILE_SYSTEM_PROPERTY) != null) {
            String configFile = System.getProperty(ResourceManager.RESMGR_PROPERTIES_FILE_SYSTEM_PROPERTY);

            logger.info("Loading resource manager configuration properties from: [{}]", configFile);
            try {
                System.getProperties().load(new FileInputStream(new File(configFile)));
            } catch (IOException e) {
                logger.error("Error loading configuration properties from: [{}]", configFile);
            }
        }
    }

    public static ResourceManager getResourceManager(int port) throws Exception {
        loadProperties();
        String resourceManagerClass = System.getProperty(ResourceManager.RESMGR_SYSTEM_PROPERTY,
                AvroRpcResourceManager.class.getName());

        logger.info("Creating resource manager {} at port: {}", resourceManagerClass, port);

        ResourceManager manager;
        try {
            Constructor<?> constructor = Class.forName(resourceManagerClass).getConstructor(Integer.TYPE);
            manager = (ResourceManager) constructor.newInstance(port);
        } catch (Exception e) {
            logger.error("Unable to create resource manager", e);
            throw e;
        }

        return manager;
    }

    public static ResourceManagerClient getResourceManagerClient(URL url) throws IllegalStateException {
        loadProperties();
        String resMgrClientClass = System.getProperty(ResourceManager.RESMGR_CLIENT_SYSTEM_PROPERTY,
                AvroRpcResourceManagerClient.class.getName());

        logger.info("Creating resource manager client {}", resMgrClientClass);

        ResourceManagerClient client;
        try {
            Constructor<?> constructor = Class.forName(resMgrClientClass).getConstructor(URL.class);
            client = (ResourceManagerClient) constructor.newInstance(url);
        } catch (Exception e) {
            logger.error("Unable to create resource manager", e);
            throw new IllegalStateException("Unable to create client", e);
        }

        return client;
    }
}
