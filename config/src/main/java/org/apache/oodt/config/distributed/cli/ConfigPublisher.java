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

import org.apache.oodt.config.Constants;
import org.apache.oodt.config.distributed.DistributedConfigurationPublisher;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;

public class ConfigPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ConfigPublisher.class);

    public static void main(String[] args) throws Exception {
        CmdLineOptions cmdLineOptions = new CmdLineOptions();
        CmdLineParser parser = new CmdLineParser(cmdLineOptions);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("There's an error in your command");
            parser.printUsage(System.err);
            return;
        }

        if (cmdLineOptions.getConnectString() == null && System.getProperty(ZK_CONNECT_STRING) == null) {
            System.err.println("Zookeeper connect string is not found");
            parser.printUsage(System.err);
            return;
        } else {
            System.setProperty(ZK_CONNECT_STRING, cmdLineOptions.getConnectString());
        }

        System.out.println("Starting configuration publishing");

        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(Constants.CONFIG_PUBLISHER_XML);
            Map distributedConfigurationPublisher = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

            for (Object bean : distributedConfigurationPublisher.values()) {
                DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;
                System.out.println(String.format("\nProcessing commands for component : %s", publisher.getComponent()));

                if (cmdLineOptions.isPublish()) {
                    System.out.println(String.format("Publishing configuration for : %s", publisher.getComponent()));
                    publisher.publishConfiguration();
                    System.out.println(String.format("Published configuration for : %s", publisher.getComponent()));
                    System.out.println();
                }

                if (cmdLineOptions.isVerify()) {
                    System.out.println(String.format("Verifying configuration for : %s", publisher.getComponent()));
                    if (publisher.verifyPublishedConfiguration()) {
                        System.out.println("OK... Configuration verified");
                        System.out.println(String.format("Verified configuration for : %s", publisher.getComponent()));
                    } else {
                        System.err.println("ERROR... Published configuration doesn't match the local files. Please check above logs");
                    }
                    System.out.println();
                }

                if (cmdLineOptions.isClear()) {
                    System.out.println(String.format("Clearing configuration for : %s", publisher.getComponent()));
                    publisher.clearConfiguration();
                    System.out.println(String.format("Cleared configuration for : %s", publisher.getComponent()));
                    System.out.println();
                }

                publisher.destroy();
            }
        } catch (BeansException e) {
            logger.error("Error occurred when obtaining configuration publisher beans", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error occurred when publishing configuration to zookeeper", e);
            throw e;
        }

        logger.info("Exiting CLI ...");
    }
}
