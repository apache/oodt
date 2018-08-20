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

package org.apache.oodt.cas.workflow.util;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.struct.avrotypes.AvroWorkflowCondition;
import org.apache.oodt.cas.workflow.struct.avrotypes.AvroWorkflow;
import org.apache.oodt.cas.workflow.struct.avrotypes.AvroWorkflowTask;
import org.apache.oodt.cas.workflow.struct.avrotypes.AvroWorkflowInstance;
import org.apache.oodt.cas.workflow.struct.avrotypes.AvroWorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.Priority;

import java.util.*;


/**
 * @author radu
 *
 * Avro types factory for rpc communication between {@link org.apache.oodt.cas.workflow.system.AvroRpcWorkflowManager}
 * and {@link org.apache.oodt.cas.workflow.system.AvroRpcWorkflowManagerClient}
 *
 */
public class AvroTypeFactory {

    public static AvroWorkflowCondition getAvroWorkflowCondition(WorkflowCondition workflowCondition){
        AvroWorkflowCondition avroWorkflowCondition = new AvroWorkflowCondition();
        avroWorkflowCondition.setConditionName(workflowCondition.getConditionName());
        avroWorkflowCondition.setConditionId(workflowCondition.getConditionId());
        avroWorkflowCondition.setConditionInstanceClassName(workflowCondition.getConditionInstanceClassName());
        avroWorkflowCondition.setOrder(workflowCondition.getOrder());
        if (workflowCondition.getCondConfig() != null)
            avroWorkflowCondition.setCondConfig(getAvroCondConfig(workflowCondition.getCondConfig()));
        avroWorkflowCondition.setTimeoutSeconds(workflowCondition.getTimeoutSeconds());
        avroWorkflowCondition.setOptional(workflowCondition.isOptional());
        return avroWorkflowCondition;
    }

    private static Map getAvroCondConfig(WorkflowConditionConfiguration workflowConditionConfiguration){
        Map avroCondConfig = new HashMap<String,String>();
        if (workflowConditionConfiguration.getProperties().size() > 0)
            for(Object key : workflowConditionConfiguration.getProperties().keySet()){
                avroCondConfig.put((String)key,workflowConditionConfiguration.getProperty((String)key));
            }
        return avroCondConfig;
    }

    public static WorkflowCondition getWorkflowCondition(AvroWorkflowCondition avroWorkflowCondition){
        WorkflowCondition workflowCondition = new WorkflowCondition();
        workflowCondition.setConditionName(avroWorkflowCondition.getConditionName());
        workflowCondition.setConditionId(avroWorkflowCondition.getConditionId());
        workflowCondition.setConditionInstanceClassName(avroWorkflowCondition.getConditionInstanceClassName());
        workflowCondition.setOrder(avroWorkflowCondition.getOrder());
        if(avroWorkflowCondition.getCondConfig() != null)
            workflowCondition.setCondConfig(getWorkflowConditionConfiguration(avroWorkflowCondition.getCondConfig()));
        workflowCondition.setTimeoutSeconds(avroWorkflowCondition.getTimeoutSeconds());
        workflowCondition.setOptional(avroWorkflowCondition.getOptional());
        return workflowCondition;
    }

    private static WorkflowConditionConfiguration getWorkflowConditionConfiguration(Map avroCondConfig){
        WorkflowConditionConfiguration workflowConditionConfiguration = new WorkflowConditionConfiguration();
        if (avroCondConfig.size() > 0)
            for (Object key : avroCondConfig.keySet()){
                workflowConditionConfiguration.addConfigProperty((String)key,(String)avroCondConfig.get(key));
            }
        return workflowConditionConfiguration;
    }
    // TO DO sa scot date dint avroWorkflowInstance

    public static AvroWorkflowTask getAvroWorkflowTask(WorkflowTask workflowTask){
        AvroWorkflowTask avroWorkflowTask = new AvroWorkflowTask();
        avroWorkflowTask.setTaskId(workflowTask.getTaskId());
        avroWorkflowTask.setTaskName(workflowTask.getTaskName());
        if (workflowTask.getTaskConfig() != null)
            avroWorkflowTask.setTaskConfig(getAvroTaskConfig(workflowTask.getTaskConfig()));
        if (workflowTask.getPreConditions() != null)
            avroWorkflowTask.setPreConditions(getAvroWorkflowConditions(workflowTask.getPreConditions()));
        if (workflowTask.getPostConditions() != null)
            avroWorkflowTask.setPostConditions(getAvroWorkflowConditions(workflowTask.getPostConditions()));
        avroWorkflowTask.setTaskInstanceClassName(workflowTask.getTaskInstanceClassName());
        avroWorkflowTask.setOrder(workflowTask.getOrder());
        if(workflowTask.getRequiredMetFields() != null)
        avroWorkflowTask.setRequiredMetFields(workflowTask.getRequiredMetFields());
        return avroWorkflowTask;

    }

    private static Map getAvroTaskConfig(WorkflowTaskConfiguration workflowConditionConfiguration){
        Map avroCondConfig = new HashMap<String,String>();
        if (workflowConditionConfiguration.getProperties().size() > 0)
            for(Object key : workflowConditionConfiguration.getProperties().keySet()){
                avroCondConfig.put((String) key, workflowConditionConfiguration.getProperty((String) key));
            }
        return avroCondConfig;
    }

    private static List<AvroWorkflowCondition> getAvroWorkflowConditions(List<WorkflowCondition> workflowConditions){
        ArrayList<AvroWorkflowCondition> avroWorkflowConditions = new ArrayList<AvroWorkflowCondition>();
        if (workflowConditions.size() > 0 )
            for (WorkflowCondition wfc : workflowConditions){
                avroWorkflowConditions.add(getAvroWorkflowCondition(wfc));
            }
        return avroWorkflowConditions;
    }

    public static WorkflowTask getWorkflowTask(AvroWorkflowTask avroWorkflowTask){
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setTaskId(avroWorkflowTask.getTaskId());
        workflowTask.setTaskName(avroWorkflowTask.getTaskName());
        if (workflowTask.getTaskConfig() != null)
            workflowTask.setTaskConfig(getTaskConfig(avroWorkflowTask.getTaskConfig()));
        if (workflowTask.getPreConditions() != null)
            workflowTask.setPreConditions(getWorkflowConditions(avroWorkflowTask.getPreConditions()));
        if (workflowTask.getPostConditions() != null)
            workflowTask.setPostConditions(getWorkflowConditions(avroWorkflowTask.getPostConditions()));
        workflowTask.setTaskInstanceClassName(avroWorkflowTask.getTaskInstanceClassName());
        workflowTask.setOrder(avroWorkflowTask.getOrder());
        if(workflowTask.getRequiredMetFields() != null)
            workflowTask.setRequiredMetFields(avroWorkflowTask.getRequiredMetFields());
        return workflowTask;
    }


    private static WorkflowTaskConfiguration getTaskConfig(Map map){
        WorkflowTaskConfiguration workflowTaskConfiguration = new WorkflowTaskConfiguration();
        if(map.size() > 0){
            for (Object key : map.keySet()){
                workflowTaskConfiguration.addConfigProperty((String)key,(String)map.get(key));
            }
        }
        return workflowTaskConfiguration;
    }

    private static List<WorkflowCondition> getWorkflowConditions(List<AvroWorkflowCondition> avroWorkflowConditions) {
        List<WorkflowCondition> workflowConditions = new ArrayList<WorkflowCondition>();
        if (avroWorkflowConditions.size() > 0)
            for (AvroWorkflowCondition awc : avroWorkflowConditions) {
                workflowConditions.add(getWorkflowCondition(awc));
            }
        return workflowConditions;
    }

    public static AvroWorkflow getAvroWorkflow(Workflow workflow){
        AvroWorkflow avroWorkflow = new AvroWorkflow();
        avroWorkflow.setName(workflow.getName());
        avroWorkflow.setId(workflow.getId());
        if (workflow.getTasks() != null)
            avroWorkflow.setTasks(getAvroWorkflowTasks(workflow.getTasks()));
        if (workflow.getPreConditions() != null)
            avroWorkflow.setPreConditions(getAvroWorkflowConditions(workflow.getPreConditions()));
        if (workflow.getPostConditions() != null)
            avroWorkflow.setPostConditions(getAvroWorkflowConditions(workflow.getPostConditions()));
        return avroWorkflow;
    }

    private static List<AvroWorkflowTask> getAvroWorkflowTasks(List<WorkflowTask> workflowTasks){
        List<AvroWorkflowTask> avroWorkflowTasks = new ArrayList<AvroWorkflowTask>();
        if(workflowTasks.size() > 0)
            for (WorkflowTask wt : workflowTasks){
                avroWorkflowTasks.add(getAvroWorkflowTask(wt));
            }
        return avroWorkflowTasks;
    }

    public static Workflow getWorkflow(AvroWorkflow avroWorkflow){
        Workflow workflow = new Workflow();
        workflow.setName(avroWorkflow.getName());
        workflow.setId(avroWorkflow.getId());
        if(avroWorkflow.getTasks() != null)
            workflow.setTasks(getWorkflowTasks(avroWorkflow.getTasks()));
        if (avroWorkflow.getPreConditions() != null)
            workflow.setPreConditions(getWorkflowConditions(avroWorkflow.getPreConditions()));
        if (avroWorkflow.getPostConditions() != null)
            workflow.setPostConditions(getWorkflowConditions(avroWorkflow.getPostConditions()));
        return workflow;
    }

    private static List<WorkflowTask> getWorkflowTasks(List<AvroWorkflowTask> avroWorkflowTasks){
        List<WorkflowTask> workflowTasks = new ArrayList<WorkflowTask>();
        if(avroWorkflowTasks.size() > 0)
            for (AvroWorkflowTask awt : avroWorkflowTasks){
                workflowTasks.add(getWorkflowTask(awt));
            }
        return workflowTasks;
    }

    public static AvroWorkflowInstance getAvroWorkflowInstance(WorkflowInstance workflowInstance){
        AvroWorkflowInstance avroWorkflowInstance = new AvroWorkflowInstance();
        if (workflowInstance.getWorkflow() != null)
            avroWorkflowInstance.setWorkflow(getAvroWorkflow(workflowInstance.getWorkflow()));
        avroWorkflowInstance.setId(workflowInstance.getId());
        avroWorkflowInstance.setStatus(workflowInstance.getStatus());
        avroWorkflowInstance.setCurrentTaskId(workflowInstance.getCurrentTaskId());
        avroWorkflowInstance.setStartDateTimeIsoStr(workflowInstance.getStartDateTimeIsoStr());
        avroWorkflowInstance.setEndDateTimeIsoStr(workflowInstance.getEndDateTimeIsoStr());
        avroWorkflowInstance.setCurrentTaskStartDateTimeIsoStr(workflowInstance.getCurrentTaskStartDateTimeIsoStr());
        avroWorkflowInstance.setCurrentTaskEndDateTimeIsoStr(workflowInstance.getCurrentTaskEndDateTimeIsoStr());
        if (workflowInstance.getSharedContext() != null)
            avroWorkflowInstance.setSharedContext(getAvroMetadata(workflowInstance.getSharedContext()));
        if (workflowInstance.getPriority() != null)
            avroWorkflowInstance.setPriority(workflowInstance.getPriority().getValue());
        return avroWorkflowInstance;
    }

    public static Map<String, Object> getAvroMetadata(Metadata metadata) {
        Map<String, Object> avroMetadata = new HashMap<>();
        if (metadata.getHashTable().size() > 0)
            for (String key : metadata.getAllKeys()) {
                avroMetadata.put(key, metadata.getAllMetadata(key));
            }
        return avroMetadata;
    }

    public static WorkflowInstance getWorkflowInstance(AvroWorkflowInstance avroWorkflowInstance){
        WorkflowInstance workflowInstance = new WorkflowInstance();
        if(avroWorkflowInstance.getWorkflow() != null)
            workflowInstance.setWorkflow(getWorkflow(avroWorkflowInstance.getWorkflow()));
        workflowInstance.setId(avroWorkflowInstance.getId());
        workflowInstance.setStatus(avroWorkflowInstance.getStatus());
        workflowInstance.setCurrentTaskId(avroWorkflowInstance.getCurrentTaskId());
        workflowInstance.setStartDateTimeIsoStr(avroWorkflowInstance.getStartDateTimeIsoStr());
        workflowInstance.setEndDateTimeIsoStr(avroWorkflowInstance.getEndDateTimeIsoStr());
        workflowInstance.setCurrentTaskStartDateTimeIsoStr(avroWorkflowInstance.getCurrentTaskStartDateTimeIsoStr());
        workflowInstance.setCurrentTaskEndDateTimeIsoStr(avroWorkflowInstance.getCurrentTaskEndDateTimeIsoStr());
        if (avroWorkflowInstance.getSharedContext() != null)
            workflowInstance.setSharedContext(getMetadata(avroWorkflowInstance.getSharedContext()));
        if (avroWorkflowInstance.getPriority() != null)
            workflowInstance.setPriority(Priority.getPriority(avroWorkflowInstance.getPriority()));
        return workflowInstance;
    }

    public static Metadata getMetadata(Map<String, Object> avroMetadata) {
        Metadata metadata = new Metadata();
        if (avroMetadata.size() > 0)
            for (String key : avroMetadata.keySet()) {
                if (avroMetadata.get(key) instanceof List) {
                    metadata.addMetadata(key, (List) avroMetadata.get(key));
                } else {
                    metadata.addMetadata(key, (String) avroMetadata.get(key));
                }
            }
        return metadata;
    }

    public static AvroWorkflowInstancePage getAvroWorkflowInstancePage(WorkflowInstancePage workflowInstancePage){
        AvroWorkflowInstancePage avroWorkflowInstancePage = new AvroWorkflowInstancePage();
        avroWorkflowInstancePage.setPageNum(workflowInstancePage.getPageNum());
        avroWorkflowInstancePage.setTotalPages(workflowInstancePage.getTotalPages());
        avroWorkflowInstancePage.setPageSize(workflowInstancePage.getPageSize());
        if (workflowInstancePage.getPageWorkflows() != null)
            avroWorkflowInstancePage.setPageWorkflows(getAvroWorkflowInstances(workflowInstancePage.getPageWorkflows()));

        return avroWorkflowInstancePage;
    }

    public static List<AvroWorkflow> getAvroWorkflows(List<Workflow> workflows){
        List<AvroWorkflow> avroWorkflows = new ArrayList<AvroWorkflow>();
        if (workflows != null && workflows.size() > 0) {
            for (Workflow w : workflows){
                avroWorkflows.add(getAvroWorkflow(w));
            }
        }
        return avroWorkflows;
    }

    public static WorkflowInstancePage getWorkflowInstancePage(AvroWorkflowInstancePage avroWorkflowInstancePage){
        WorkflowInstancePage workflowInstancePage = new WorkflowInstancePage();
        workflowInstancePage.setPageNum(avroWorkflowInstancePage.getPageNum());
        workflowInstancePage.setTotalPages(avroWorkflowInstancePage.getTotalPages());
        if (avroWorkflowInstancePage.getPageWorkflows() != null)
            workflowInstancePage.setPageWorkflows(getWorkflowInstances(avroWorkflowInstancePage.getPageWorkflows()));
        return workflowInstancePage;
    }

    public static List<Workflow> getWorkflows(List<AvroWorkflow> avroWorkflow){
        List<Workflow> workflows = new ArrayList<Workflow>();
        if (avroWorkflow.size() > 0)
            for (AvroWorkflow w : avroWorkflow){
                workflows.add(getWorkflow(w));
            }
        return workflows;
    }


    public static List<AvroWorkflowInstance> getAvroWorkflowInstances(List<WorkflowInstance> workflowInstances){
        List<AvroWorkflowInstance> avroWorkflowInstances = new ArrayList<AvroWorkflowInstance>();
        if (workflowInstances != null && workflowInstances.size() > 0)
            for (WorkflowInstance awi : workflowInstances){
                avroWorkflowInstances.add(AvroTypeFactory.getAvroWorkflowInstance(awi));
            }
        return avroWorkflowInstances;
    }

    public static List<WorkflowInstance> getWorkflowInstances(List<AvroWorkflowInstance> avroWorkflowInstances){
        List<WorkflowInstance> workflowInstances = new ArrayList<WorkflowInstance>();
        if (avroWorkflowInstances != null && avroWorkflowInstances.size() > 0)
            for (AvroWorkflowInstance wi : avroWorkflowInstances){
                workflowInstances.add(AvroTypeFactory.getWorkflowInstance(wi));
            }
        return workflowInstances;
    }



}
