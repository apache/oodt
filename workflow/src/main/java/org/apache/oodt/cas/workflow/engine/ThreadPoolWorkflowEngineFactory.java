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
package org.apache.oodt.cas.workflow.engine;

//JDK imports
import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

/**
 * A Factory class for creating {@link ThreadPoolWorkflowEngine}s.
 * 
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class ThreadPoolWorkflowEngineFactory implements WorkflowEngineFactory {

  private static final Logger LOG = Logger
      .getLogger(ThreadPoolWorkflowEngineFactory.class.getName());

  private static final String INSTANCE_REPO_FACTORY_PROPERTY = "workflow.engine.instanceRep.factory";
  private static final String QUEUE_SIZE_PROPERTY = "org.apache.oodt.cas.workflow.engine.queueSize";
  private static final String MAX_POOL_SIZE_PROPERTY = "org.apache.oodt.cas.workflow.engine.maxPoolSize";
  private static final String MIN_POOL_SIZE_PROPERTY = "org.apache.oodt.cas.workflow.engine.minPoolSize";
  private static final String THREAD_KEEP_ALIVE_PROPERTY = "org.apache.oodt.cas.workflow.engine.threadKeepAlive.minutes";
  private static final String UNLIMITED_QUEUE_PROPERTY = "org.apache.oodt.cas.workflow.engine.unlimitedQueue";
  private static final String RESMGR_URL_PROPERTY = "org.apache.oodt.cas.workflow.engine.resourcemgr.url";

  private static final int DEFAULT_QUEUE_SIZE = 10;
  private static final int DEFAULT_MAX_POOL_SIZE = 10;
  private static final int DEFAULT_MIN_POOL_SIZE = 4;
  private static final long DEFAULT_THREAD_KEEP_ALIVE_MINS = 5;

  @Override
  public WorkflowEngine createWorkflowEngine() {
    return new ThreadPoolWorkflowEngine(getWorkflowInstanceRepository(),
        getQueueSize(), getMaxPoolSize(), getMinPoolSize(),
        getThreadKeepAliveMinutes(), isUnlimitedQueue(), getResmgrUrl());
  }

  protected WorkflowInstanceRepository getWorkflowInstanceRepository() {
    return GenericWorkflowObjectFactory
        .getWorkflowInstanceRepositoryFromClassName(System
            .getProperty(INSTANCE_REPO_FACTORY_PROPERTY));
  }

  protected URL getResmgrUrl() {
    try {
      return new URL(PathUtils.replaceEnvVariables(System.getProperty(RESMGR_URL_PROPERTY)));
    } catch (Exception e) {
      LOG.log(
          Level.INFO,
          "No Resource Manager URL provided or malformed URL: executing jobs " +
          "locally. URL: ["+PathUtils.replaceEnvVariables(System.getProperty(RESMGR_URL_PROPERTY))+"]");
      return null;
    }
  }

  protected int getQueueSize() {
    return getInteger(QUEUE_SIZE_PROPERTY, DEFAULT_QUEUE_SIZE);
  }

  protected int getMaxPoolSize() {
    return getInteger(MAX_POOL_SIZE_PROPERTY, DEFAULT_MAX_POOL_SIZE);
  }

  protected int getMinPoolSize() {
    return getInteger(MIN_POOL_SIZE_PROPERTY, DEFAULT_MIN_POOL_SIZE);
  }

  protected long getThreadKeepAliveMinutes() {
    return getLong(THREAD_KEEP_ALIVE_PROPERTY, DEFAULT_THREAD_KEEP_ALIVE_MINS);
  }

  protected boolean isUnlimitedQueue() {
    return getBoolean(UNLIMITED_QUEUE_PROPERTY);
  }
}
