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


package org.apache.oodt.cas.workflow.instrepo;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface WorkflowInstanceMetMapKeys {
    
    String DEFAULT_WORKFLOW_MAP = "default";

    /* this is a RESERVED keyword: DON'T give your workflows this ID! */
    String DEFAULT_WORKFLOW_ID = "__default__";
    
    String FIELD_TAG = "field";
    
    String FIELD_TAG_NAME_ATTR = "name";
    
    String WORKFLOW_TAG_NAME = "workflow";
    
    String WORKFLOW_TAG_ID_ATTR = "id";

}
