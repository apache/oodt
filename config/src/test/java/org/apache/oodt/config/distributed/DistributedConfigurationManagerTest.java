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
import org.apache.oodt.config.distributed.cli.ConfigPublisher;
import org.apache.oodt.config.distributed.utils.FilePathUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.oodt.config.Constants.SEPARATOR;
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

        publishers = new ArrayList<>(distributedConfigurationPublishers.values().size());
        for (Object bean : distributedConfigurationPublishers.values()) {
            DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;

            System.setProperty(publisher.getComponent().getHome(), ".");
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
                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }

                for (String key : properties.stringPropertyNames()) {
                    Assert.assertEquals(properties.getProperty(key), System.getProperty(key));
                }

                String fileName = entry.getValue();
                fileName = fileName.startsWith(SEPARATOR) ? fileName.substring(SEPARATOR.length()) : fileName;
                fileName = FilePathUtils.fixForComponentHome(publisher.getComponent(), fileName);
                File downloadedFile = new File(fileName);
                Assert.assertNotNull(downloadedFile);
                Assert.assertTrue(downloadedFile.exists());
            }

            // Checking for configuration files
            for (Map.Entry<String, String> entry : publisher.getConfigFiles().entrySet()) {
                String fileName = entry.getValue();
                fileName = fileName.startsWith(SEPARATOR) ? fileName.substring(SEPARATOR.length()) : fileName;
                fileName = FilePathUtils.fixForComponentHome(publisher.getComponent(), fileName);
                File file = new File(fileName);
                Assert.assertTrue(file.exists());
            }
        }
    }

    @After
    public void tearDownTest() throws Exception {
        for (DistributedConfigurationPublisher publisher : publishers) {
            publisher.destroy();

            // deleting all locally created conf file directories
            Set<Map.Entry<String, String>> files = new HashSet<>(publisher.getConfigFiles().entrySet());
            files.addAll(publisher.getPropertiesFiles().entrySet());

            for (Map.Entry<String, String> entry : files) {
                String fileName = entry.getValue();
                fileName = fileName.startsWith(SEPARATOR) ? fileName.substring(1) : fileName;

                String prefixPath = System.getProperty(publisher.getComponent().getHome());
                if (prefixPath == null) {
                    prefixPath = System.getenv(publisher.getComponent().getHome());
                }
                String confDir = prefixPath != null && !prefixPath.trim().isEmpty() ?
                        prefixPath.trim() + SEPARATOR + fileName.split(SEPARATOR)[0] : fileName.split(SEPARATOR)[0];

                File dir = new File(confDir);
                FileUtils.deleteDirectory(dir);
            }
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
