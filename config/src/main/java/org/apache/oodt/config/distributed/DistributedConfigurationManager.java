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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.Constants.Properties;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;

/**
 * Distributed configuration manager implementation. This class make use of a {@link CuratorFramework} instance to connect
 * to zookeeper
 *
 * @author Imesha Sudasingha.
 */
public class DistributedConfigurationManager extends ConfigurationManager {

  private static final Logger logger = Logger.getLogger(DistributedConfigurationManager.class.getName());

  private static String environment = Constants.DEFAULT_ENVIRONMENT;
  private static String nodeName;

  /** Variables required to connect to zookeeper */
  private String connectString;
  private CuratorFramework client;

  public DistributedConfigurationManager(String component, List<String> propertiesFiles, List<String> otherFiles) {
    super(component, propertiesFiles, otherFiles);
    loadZookeeperProperties();
    startZookeeper();
  }

  /**
   * Loads zookeeper related properties from ZK_PROPERTIES_FILE. WIll throw a {@link RuntimeException} if that file is
   * not available or couldn't be opened.
   */
  private void loadZookeeperProperties() {
    if (System.getProperty(ZK_PROPERTIES_FILE) != null) {
      throw new IllegalArgumentException("DistributedCOnfigurationManager requires " + ZK_PROPERTIES_FILE + " to be set");
    }

    try {
      System.getProperties().load(new FileInputStream(System.getProperty(ZK_PROPERTIES_FILE)));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read ZK_PROPERTIES_FILE " + System.getProperty(ZK_PROPERTIES_FILE));
      throw new IllegalStateException("Couldn't load Zookeeper configuration");
    }

    if (System.getProperty(Properties.ZK_CONNECT_STRING) == null) {
      throw new IllegalArgumentException("Zookeeper requires a proper connect string to connect to zookeeper ensemble");
    }

    connectString = System.getProperty(Properties.ZK_CONNECT_STRING);
    logger.log(Level.CONFIG, String.format("Using zookeeper connect string : %s", connectString));
  }

  /**
   * Creates a {@link CuratorFramework} instance and start it. This method will wait a maximum amount of
   * {@link Properties#ZK_STARTUP_TIMEOUT} milli-seconds until the client connects to the zookeeper ensemble.
   */
  private void startZookeeper() {
    int connectionTimeoutMs = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "15"));
    int sessionTimeoutMs = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "60"));
    int retryInitialWaitMs = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "1000"));
    int maxRetryCount = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "3"));
    int startupTimeOutMs = Integer.parseInt(System.getProperty(Properties.ZK_STARTUP_TIMEOUT, "30000"));

    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
        .connectString(connectString)
        .retryPolicy(new ExponentialBackoffRetry(retryInitialWaitMs, maxRetryCount))
        .connectionTimeoutMs(connectionTimeoutMs)
        .sessionTimeoutMs(sessionTimeoutMs);

    /*
     * If authorization information is available, those will be added to the client. NOTE: These auth info are
     * for access control, therefore no authentication will happen when the client is being started. These
     * info will only be required whenever a client is accessing an already create ZNode. For another client of
     * another node to make use of a ZNode created by this node, it should also provide the same auth info.
     */
    if (System.getProperty(Properties.ZK_USERNAME) != null && System.getProperty(Properties.ZK_PASSWORD) != null) {
      String authenticationString = System.getProperty(Properties.ZK_USERNAME) + ":" + System.getProperty(Properties.ZK_PASSWORD);
      builder.authorization("digest", authenticationString.getBytes())
          .aclProvider(new ACLProvider() {
            public List<ACL> getDefaultAcl() {
              return ZooDefs.Ids.CREATOR_ALL_ACL;
            }

            public List<ACL> getAclForPath(String path) {
              return ZooDefs.Ids.CREATOR_ALL_ACL;
            }
          });
    }

    client = builder.build();
    logger.log(Level.CONFIG, String.format("CuratorFramework client built successfully with " +
        "connectString: %s, sessionTimeout: %d and connectionTimeout: %d", connectString, sessionTimeoutMs, connectionTimeoutMs));

    client.start();
    logger.log(Level.CONFIG, "Curator framework start operation invoked");

    try {
      logger.info(String.format("Waiting to connect to zookeeper, startupTimeout : %d", startupTimeOutMs));
      client.blockUntilConnected(startupTimeOutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      logger.severe(String.format("Interrupted while waiting to connect zookeeper (connectString : %s) : %s", ex.getMessage(), connectString));
    }

    if (!client.getZookeeperClient().isConnected()) {
      throw new IllegalStateException("Could not connect to ZooKeeper : " + connectString);
    }

    logger.info("CuratorFramework client started successfully");
  }

  @Override
  public String getProperty(String key) {
    // Todo Implement using curator
    return null;
  }

  @Override
  public void loadProperties() {
    // todo Implement the logic with Curator
  }

  public File getPropertiesFile(String filePath) {
    return null;
  }

  public File getConfigurationFile(String filePath) {
    return null;
  }
}
