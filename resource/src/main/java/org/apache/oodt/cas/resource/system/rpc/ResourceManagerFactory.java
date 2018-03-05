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

import org.apache.oodt.cas.resource.system.ResourceManager;
import org.apache.oodt.cas.resource.system.ResourceManagerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.URL;

public class ResourceManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerFactory.class);

    public static ResourceManager getResourceManager(int port) throws Exception {
        String resourceManagerClass = System.getProperty("resmgr.manager",
                "org.apache.oodt.cas.resource.system.AvroRpcResourceManager");

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

    public static ResourceManagerClient getResourceManagerClient(URL url) throws Exception {
        String resMgrClientClass = System.getProperty("resmgr.manager.client",
                "org.apache.oodt.cas.resource.system.AvroRpcResourceManagerClient");

        logger.info("Creating resource manager client {}", resMgrClientClass);

        ResourceManagerClient client;
        try {
            Constructor<?> constructor = Class.forName(resMgrClientClass).getConstructor(URL.class);
            client = (ResourceManagerClient) constructor.newInstance(url);
        } catch (Exception e) {
            logger.error("Unable to create resource manager", e);
            throw e;
        }

        return client;
    }
}
