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


package org.apache.oodt.cas.workflow.metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Core metadata key names for the Workflow Manager
 * </p>.
 */
public interface CoreMetKeys {

    String TASK_ID = "TaskId";

    String WORKFLOW_INST_ID = "WorkflowInstId";
    
    String WORKFLOW_ID = "WorkflowId";

    String WORKFLOW_NAME = "WorkflowName";

    String JOB_ID = "JobId";

    String PROCESSING_NODE = "ProcessingNode";

    String WORKFLOW_MANAGER_URL = "WorkflowManagerUrl";

    String QUEUE_NAME = "QueueName";
    
    String TASK_LOAD = "TaskLoad";

}
