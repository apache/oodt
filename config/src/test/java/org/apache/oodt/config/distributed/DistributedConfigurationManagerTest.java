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

import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.distributed.cli.ConfigPublisher;
import org.apache.oodt.config.distributed.utils.ConfigUtils;
import org.apache.oodt.config.test.AbstractDistributedConfigurationTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.oodt.config.Constants.Properties.OODT_PROJECT;
import static org.junit.Assert.fail;

/**
 * Testing the {@link DistributedConfigurationManager} whether it is downloading and storing the configuration correctly
 * in local files
 *
 * @author Imesha Sudasingha
 */
public class DistributedConfigurationManagerTest extends AbstractDistributedConfigurationTest {

    private static final String CONFIG_PUBLISHER_XML = "config-publisher.xml";

    private List<DistributedConfigurationPublisher> publishers;
    private Map<Component, ConfigurationManager> configurationManagers;

    @Before
    public void setUpTest() throws Exception {
        System.setProperty("org.apache.oodt.cas.cli.action.spring.config", "src/main/resources/cmd-line-actions.xml");
        System.setProperty("org.apache.oodt.cas.cli.option.spring.config", "src/main/resources/cmd-line-options.xml");

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-a", "publish"
        });

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_PUBLISHER_XML);
        Map distributedConfigurationPublishers = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

        publishers = new ArrayList<>();
        configurationManagers = new HashMap<>();
        for (Object bean : distributedConfigurationPublishers.values()) {
            DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;

            System.setProperty(OODT_PROJECT, publisher.getProject());
            System.setProperty(publisher.getComponent().getHome(), ".");
            publishers.add(publisher);
            configurationManagers.put(publisher.getComponent(), new DistributedConfigurationManager(publisher.getComponent()));
            System.clearProperty(OODT_PROJECT);
        }
    }

    @Test
    public void loadConfigurationTest() throws Exception {
        for (DistributedConfigurationPublisher publisher : publishers) {

            ConfigurationManager configurationManager = configurationManagers.get(publisher.getComponent());
            configurationManager.loadConfiguration();

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getPropertiesFiles().entrySet()) {
                File originalFile = new File(entry.getKey());
                Properties properties = new Properties();
                try (InputStream in = new FileInputStream(originalFile)) {
                    properties.load(in);
                } catch (Exception e) {
                    fail(e.getMessage());
                }

                for (String key : properties.stringPropertyNames()) {
                    Assert.assertEquals(properties.getProperty(key), System.getProperty(key));
                }

                String fileName = entry.getValue();
                fileName = ConfigUtils.fixForComponentHome(publisher.getComponent(), fileName);
                File downloadedFile = new File(fileName);
                Assert.assertNotNull(downloadedFile);
                Assert.assertTrue(downloadedFile.exists());
            }

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String fileName = entry.getValue();
                fileName = ConfigUtils.fixForComponentHome(publisher.getComponent(), fileName);
                File file = new File(fileName);
                Assert.assertTrue(file.exists());
            }

            List<String> localFiles = configurationManager.getSavedFiles();
            configurationManager.clearConfiguration();
            for (String localFile : localFiles) {
                File file = new File(localFile);
                Assert.assertFalse(file.exists());
            }
        }
    }

    @Test
    public void notifyConfigurationChangeTest() throws Exception {
        // First publish config. Then check if config has downloaded locally.
        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-notify",
                "-a", "publish"
        });
        Thread.sleep(5000);
        checkFileExistence(true);

        // Now clear config. Then check if config has deleted locally.
        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-notify",
                "-a", "clear"
        });
        Thread.sleep(5000);
        checkFileExistence(false);

        // First publish config. Then check if config has downloaded locally.
        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-notify",
                "-a", "publish"
        });
        Thread.sleep(5000);
        checkFileExistence(true);
    }

    private void checkFileExistence(boolean exists) {
        for (DistributedConfigurationPublisher publisher : publishers) {
            for (String fileName : publisher.getPropertiesFiles().values()) {
                fileName = ConfigUtils.fixForComponentHome(publisher.getComponent(), fileName);
                File file = new File(fileName);
                if (exists) {
                    Assert.assertTrue(file.exists());
                } else {
                    Assert.assertFalse(file.exists());
                }
            }

            for (String fileName : publisher.getConfigFiles().values()) {
                fileName = ConfigUtils.fixForComponentHome(publisher.getComponent(), fileName);
                File file = new File(fileName);
                if (exists) {
                    Assert.assertTrue(file.exists());
                } else {
                    Assert.assertFalse(file.exists());
                }
            }
        }
    }

    @After
    public void tearDownTest() throws Exception {
        for (DistributedConfigurationPublisher publisher : publishers) {
            publisher.destroy();
        }

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONFIG_PUBLISHER_XML,
                "-a", "clear"
        });

        System.clearProperty("org.apache.oodt.cas.cli.action.spring.config");
        System.clearProperty("org.apache.oodt.cas.cli.option.spring.config");
    }
}
