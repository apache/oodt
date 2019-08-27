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
package org.apache.oodt.cas.workflow.engine.runner;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

//Google imports
import com.google.common.base.Preconditions;


/**
 * Factory which creates {@link ResourceRunner}s.
 *
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class ResourceRunnerFactory implements EngineRunnerFactory{

   private static final Logger LOG = Logger.getLogger(ResourceRunnerFactory.class.getName());

   private static final String RESOURCE_MANAGER_URL_PROPERTY = "org.apache.oodt.cas.workflow.engine.resourcemgr.url";

   private static final String INSTANCE_REPO_FACTORY_PROPERTY = "workflow.engine.instanceRep.factory";
   
   private String resUrl;

   public ResourceRunnerFactory() {
      resUrl = PathUtils.replaceEnvVariables(System.getProperty(RESOURCE_MANAGER_URL_PROPERTY));
   }

   @Override
   public ResourceRunner createEngineRunner() {
      try {
         Preconditions.checkNotNull(resUrl,
               "Must specify Resource Manager URL [property = "
                     + RESOURCE_MANAGER_URL_PROPERTY + "]");
         return new ResourceRunner(new URL(resUrl), getWorkflowInstanceRepository());
      } catch (MalformedURLException e) {
         LOG.log(Level.SEVERE, "Failed to load ResourceRunner : " + e.getMessage(), e);
         return null;
      }
   }

   public void setResourceManagerUrl(String resUrl) {
      this.resUrl = resUrl;
   }
   
   protected WorkflowInstanceRepository getWorkflowInstanceRepository() {
     return GenericWorkflowObjectFactory
         .getWorkflowInstanceRepositoryFromClassName(System
             .getProperty(INSTANCE_REPO_FACTORY_PROPERTY));
   }   
}
