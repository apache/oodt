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

  public static final String PGE_TASK_TYPE = "PCS_PGETaskType";

  public static final String NUM_JAVA_EXT_DIRS = "PCS_NumJavaExtDirs";

  // use this key like: JAVA_EXT_DIR+"1", or JAVA_EXT_DIR+"2"
  public static final String JAVA_EXT_DIR = "PCS_JavaExtDir";

  public static final String JAVA_MAIN_CLASS = "PCS_JavaMainClass";

  public static final String FILE_MANAGER_URL = "PCS_FileManagerUrl";

  public static final String SCF_FILE_MANAGER_URL = "SCF_FileManagerUrl";

  public static final String WORKFLOW_MANAGER_URL = "PCS_WorkflowManagerUrl";

  public static final String PGE_CONFIG_FILE_PROPERTY_ADDER_CLASS = "PCS_PGEConfigPropertyAdderClass";

  public static final String CLIENT_TRANSFER_SERVICE_FACTORY = "PCS_ClientTransferServiceFactory";

  public static final String CRAWLER_CLEANUP = "PCS_CrawlerCleanup";

  public static final String CRAWLER_CRAWLDIRS = "PCS_CrawlerCrawlForDirs";

  public static final String CONFIG_FILE_SCHEMA_PATH = "PCS_ConfigFileSchemaPath";

  public static final String PGE_CONFIG_FILE_NAME = "PCS_PGEConfigFileName";

  public static final String PGE_LOG_FILE_NAME = "PCS_PGELogFileName";

  public static final String PGE_TYPE_GDS_ADAPTOR = "GDSAdaptor";

  public static final String PGE_TYPE_GDS_PGE = "GDSPge";

  public static final String UNKNOWN = "UNKNOWN";

  public static final String WORKFLOW_MGR_URL = "WorkflowManagerUrl";

  public static final String WORKFLOW_INST_ID = "WorkflowInstId";

  public static final String MAX_ALLOWABLE_GAP_MINUTES = "PCS_MaxAllowableTimeGapMinutes";

  /* PGE task statuses */
  public static final String STAGING_INPUT = "STAGING INPUT";

  public static final String CONF_FILE_BUILD = "BUILDING CONFIG FILE";

  public static final String RUNNING_PGE = "PGE EXEC";

  public static final String CRAWLING = "CRAWLING";

}
