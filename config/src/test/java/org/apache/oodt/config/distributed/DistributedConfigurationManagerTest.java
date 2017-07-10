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
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.distributed.cli.DistributedConfigurationPublisher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.oodt.config.Constants.CONFIG_PUBLISHER_XML;
import static org.apache.oodt.config.Constants.SEPARATOR;

/**
 * Testing the {@link DistributedConfigurationManager} whether it is downloading and storing the configuration correctly
 * in local files
 *
 * @author Imesha Sudasingha
 */
public class DistributedConfigurationManagerTest extends AbstractDistributedConfigurationTest {

    protected static List<DistributedConfigurationPublisher> publishers;

    @BeforeClass
    public static void setUp() throws Exception {
        AbstractDistributedConfigurationTest.setUp();

        DistributedConfigurationPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-publish"
        });

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_PUBLISHER_XML);
        Map distributedConfigurationPublishers = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

        publishers = new ArrayList<>(distributedConfigurationPublishers.values().size());
        for (Object bean : distributedConfigurationPublishers.values()) {
            DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;
            publishers.add(publisher);
        }
    }

    @Test
    public void loadConfigurationTest() throws Exception {
        for (DistributedConfigurationPublisher publisher : publishers) {
            ConfigurationManager configurationManager = new DistributedConfigurationManager(publisher.getComponent());
            configurationManager.loadConfiguration();

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getPropertiesFiles().entrySet()) {
                File originalFile = new File(entry.getKey());
                Properties properties = new Properties();
                try (InputStream in = new FileInputStream(originalFile)) {
                    properties.load(in);
                }

                for (String key : properties.stringPropertyNames()) {
                    Assert.assertEquals(properties.getProperty(key), System.getProperty(key));
                }

                String fileName = entry.getValue();
                fileName = fileName.startsWith(SEPARATOR) ? fileName.substring(1) : fileName;
                File downloadedFile = new File(fileName);
                Assert.assertTrue(downloadedFile.exists());
            }

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String fileName = entry.getValue();
                fileName = fileName.startsWith(SEPARATOR) ? fileName.substring(1) : fileName;
                File file = new File(fileName);
                Assert.assertTrue(file.exists());
            }
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        for (DistributedConfigurationPublisher publisher : publishers) {
            publisher.destroy();

            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String fileName = entry.getValue();
                fileName = fileName.startsWith(SEPARATOR) ? fileName.substring(1) : fileName;
                String confDir = fileName.split(SEPARATOR)[0];
                File dir = new File(confDir);
                FileUtils.deleteDirectory(dir);
            }
        }

        DistributedConfigurationPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-clear"
        });

        AbstractDistributedConfigurationTest.tearDown();
    }
}
