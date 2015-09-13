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
package org.apache.oodt.cas.pge.metadata;

//Google imports
import com.google.common.annotations.VisibleForTesting;

/**
 * Workflow Statuses for {@link PGETaskInstance}.
 *
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public enum PgeTaskStatus {
	
	/**
	 * The PGE is staging its input to the job working directory.
	 */
   STAGING_INPUT("PGETask_Staging_Input", "STAGING INPUT"),
   
   /**
    * The PGE is building its internal configuration file.
    */
   CONF_FILE_BUILD("PGETask_Building_Config_File", "BUILDING CONFIG FILE"),
   
   /**
    * The PGE is executing.
    */
   RUNNING_PGE("PGETask_Running", "PGE EXEC"),
   
   /**
    * The ingest crawler has been created, either a StdProductCrawler
    * or if {@link PgeTaskMetKeys#MIME_EXTRACTOR_REPO} has been specified
    * an AutoDetectProductCrawler.
    */
   CRAWLING("PGETask_Crawling", "CRAWLING");

   public static final String USE_LEGACY_STATUS_PROPERTY = "org.apache.oodt.cas.pge.task.status.legacyMode";
   
   @VisibleForTesting private String workflowStatusName;
   @VisibleForTesting private String legacyName;

   PgeTaskStatus(String workflowStatusName, String legacyName) {
      this.workflowStatusName = workflowStatusName;
      this.legacyName = legacyName;
   }

   public String getWorkflowStatusName() {
      return Boolean.getBoolean(USE_LEGACY_STATUS_PROPERTY) ? this.legacyName:this.workflowStatusName;
   }
   
   @Override
   public String toString() {
      return getWorkflowStatusName();
   }
}
