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

package org.apache.oodt.config.distributed.cli;

import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.config.ConfigEventType;
import org.apache.oodt.config.distributed.DistributedConfigurationPublisher;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

import static org.apache.oodt.config.Constants.DEFAULT_CONFIG_PUBLISHER_XML;
import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;

/**
 * {@link CmdLineAction} specifying the verify, publish and clear tasks of distributed configuration management.
 *
 * @see ConfigPublisher
 * @author Imesha Sudasingha
 */
public class CLIAction extends CmdLineAction {

    private String connectString;
    private String configFile = DEFAULT_CONFIG_PUBLISHER_XML;
    private boolean notify = false;

    private Action action;

    public CLIAction(Action action) {
        this.action = action;
    }

    @Override
    public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(configFile);
            Map distributedConfigurationPublisher = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

            for (Object bean : distributedConfigurationPublisher.values()) {
                DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;
                switch (action) {
                    case PUBLISH:
                        publish(publisher);
                        if (notify) {
                            publisher.notifyConfigEvent(ConfigEventType.PUBLISH);
                        }
                        break;
                    case VERIFY:
                        verify(publisher);
                        break;
                    case CLEAR:
                        clear(publisher);
                        if (notify) {
                            publisher.notifyConfigEvent(ConfigEventType.CLEAR);
                        }
                        break;
                }
                publisher.destroy();
            }
        } catch (BeansException e) {
            System.out.println(String.format("Error occurred when obtaining configuration publisher beans: '%s'", e.getMessage()));
        } catch (Exception e) {
            System.out.println(String.format("Error occurred when publishing configuration to zookeeper: '%s'", e.getMessage()));
        }

        System.out.println("Exiting CLI ...");
    }

    /**
     * Publishes configuration files (which are stored locally at the moment) specified in {@link #configFile} to
     * zookeeper.
     *
     * @param publisher {@link DistributedConfigurationPublisher} instance
     * @throws Exception
     */
    private void publish(DistributedConfigurationPublisher publisher) throws Exception {
        System.out.println();
        System.out.println(String.format("Publishing configuration for : %s", publisher.getComponent()));
        publisher.publishConfiguration();
        System.out.println(String.format("Published configuration for : %s", publisher.getComponent()));
        System.out.println();
    }

    /**
     * Verifies whether the content in the local files (which were published to zookeeper) are identical to the content
     * that has actually been published. The file mapping is obtained from {@link #configFile}. Will print error
     * messages accordingly if the verification fails.
     *
     * @param publisher {@link DistributedConfigurationPublisher} instance
     * @throws Exception
     */
    private void verify(DistributedConfigurationPublisher publisher) throws Exception {
        System.out.println();
        System.out.println(String.format("Verifying configuration for : %s", publisher.getComponent()));
        if (publisher.verifyPublishedConfiguration()) {
            System.out.println("OK... Configuration verified");
            System.out.println(String.format("Verified configuration for : %s", publisher.getComponent()));
        } else {
            System.err.println("ERROR... Published configuration doesn't match the local files. Please check above logs");
        }
        System.out.println();
    }

    /**
     * Clears all configuration published to zookeeper This will simply delete {@link
     * org.apache.oodt.config.distributed.ZNodePaths#configurationZNodePath} and {@link
     * org.apache.oodt.config.distributed.ZNodePaths#propertiesZNodePath} along with its children from zookeeper.
     *
     * @param publisher {@link DistributedConfigurationPublisher} instance
     * @throws Exception
     */
    private void clear(DistributedConfigurationPublisher publisher) throws Exception {
        System.out.println();
        System.out.println(String.format("Clearing configuration for : %s", publisher.getComponent()));
        publisher.clearConfiguration();
        System.out.println(String.format("Cleared configuration for : %s", publisher.getComponent()));
        System.out.println();
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        System.setProperty(ZK_CONNECT_STRING, connectString);
        this.connectString = connectString;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public enum Action {
        PUBLISH, VERIFY, CLEAR
    }
}
