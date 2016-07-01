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
package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.net.URL;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * Updates the Workflow Manager and notifies it that the crawled {@link Product}
 * has been ingested successfully.
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class WorkflowMgrStatusUpdate extends CrawlerAction implements
      CoreMetKeys {

   private String ingestSuffix;
   private String workflowMgrUrl;

   public WorkflowMgrStatusUpdate() {
      ingestSuffix = "Ingest";
   }

   public boolean performAction(File product, Metadata productMetadata)
         throws CrawlerActionException {
      try {
         WorkflowManagerClient wClient = RpcCommunicationFactory.createClient(new URL(this.workflowMgrUrl));
         String ingestSuffix = this.ingestSuffix;
         return wClient.sendEvent(productMetadata.getMetadata(PRODUCT_TYPE)
               + ingestSuffix, productMetadata);
      } catch (Exception e) {
         throw new CrawlerActionException(
               "Failed to update workflow manager : " + e.getMessage(), e);
      }
   }

   @Override
   public void validate() throws CrawlerActionException {
      super.validate();
      try {
         Validate.notNull(ingestSuffix, "Must specify ingestSuffix");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
   }

   public void setIngestSuffix(String ingestSuffix) {
      this.ingestSuffix = ingestSuffix;
   }

   @Required
   public void setWorkflowMgrUrl(String workflowMgrUrl) {
      this.workflowMgrUrl = workflowMgrUrl;
   }
}
