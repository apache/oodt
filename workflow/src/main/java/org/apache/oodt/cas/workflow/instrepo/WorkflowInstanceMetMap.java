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

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Arrays;
import java.util.List;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public final class WorkflowInstanceMetMap implements WorkflowInstanceMetMapKeys{
    private Metadata map;

    public WorkflowInstanceMetMap() {
        map = new Metadata();
    }
    
    public void addDefaultField(String fld){
        map.addMetadata(DEFAULT_WORKFLOW_ID, fld);
    }
    
    public void addDefaultFields(List flds){
        addWorkflowToMap(DEFAULT_WORKFLOW_ID, flds);
    }

    public void addWorkflowToMap(String id, List fields) {
        if (fields != null && fields.size() > 0) {
            for (Object field : fields) {
                String fld = (String) field;
                addFieldToWorkflow(id, fld);
            }
        }
    }
    
    public List getDefaultFields(){
        return getFieldsForWorkflow(DEFAULT_WORKFLOW_ID);
    }

    public List getFieldsForWorkflow(String id) {
        return map.getAllMetadata(id);
    }

    public void addFieldToWorkflow(String id, String fld) {
        map.addMetadata(id, fld);
    }

    public List getWorkflows() {
        return Arrays.asList(map.getMap().keySet().toArray());
    }

}
