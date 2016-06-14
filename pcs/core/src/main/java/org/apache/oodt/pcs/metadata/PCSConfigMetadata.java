/**
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

package org.apache.oodt.pcs.metadata;

/**
 * 
 * A set of Met Keys for the Task Metadata required by the PCS.
 * 
 * @author mattmann
 * @version $Revision$
 */
public interface PCSConfigMetadata {

  String PGE_TASK_TYPE = "PCS_PGETaskType";

  String NUM_JAVA_EXT_DIRS = "PCS_NumJavaExtDirs";

  // use this key like: JAVA_EXT_DIR+"1", or JAVA_EXT_DIR+"2"
  String JAVA_EXT_DIR = "PCS_JavaExtDir";

  String JAVA_MAIN_CLASS = "PCS_JavaMainClass";

  String FILE_MANAGER_URL = "PCS_FileManagerUrl";

  String SCF_FILE_MANAGER_URL = "SCF_FileManagerUrl";

  String WORKFLOW_MANAGER_URL = "PCS_WorkflowManagerUrl";

  String PGE_CONFIG_FILE_PROPERTY_ADDER_CLASS = "PCS_PGEConfigPropertyAdderClass";

  String CLIENT_TRANSFER_SERVICE_FACTORY = "PCS_ClientTransferServiceFactory";

  String CRAWLER_CLEANUP = "PCS_CrawlerCleanup";

  String CRAWLER_CRAWLDIRS = "PCS_CrawlerCrawlForDirs";

  String CONFIG_FILE_SCHEMA_PATH = "PCS_ConfigFileSchemaPath";

  String PGE_CONFIG_FILE_NAME = "PCS_PGEConfigFileName";

  String PGE_LOG_FILE_NAME = "PCS_PGELogFileName";

  String PGE_TYPE_GDS_ADAPTOR = "GDSAdaptor";

  String PGE_TYPE_GDS_PGE = "GDSPge";

  String UNKNOWN = "UNKNOWN";

  String WORKFLOW_MGR_URL = "WorkflowManagerUrl";

  String WORKFLOW_INST_ID = "WorkflowInstId";

  String MAX_ALLOWABLE_GAP_MINUTES = "PCS_MaxAllowableTimeGapMinutes";

  /* PGE task statuses */
  String STAGING_INPUT = "STAGING INPUT";

  String CONF_FILE_BUILD = "BUILDING CONFIG FILE";

  String RUNNING_PGE = "PGE EXEC";

  String CRAWLING = "CRAWLING";

}
