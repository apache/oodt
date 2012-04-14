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

package org.apache.oodt.cas.workflow.engine;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.commons.util.DateConvert;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PrioritizedQueueBasedWorkflowEngine implements WorkflowEngine {

  private WorkflowInstanceRepository repo;
  
  private PrioritySorter prioritizer;
  
  private URL wmgrUrl;
  
  private long conditionWait;
  
  public PrioritizedQueueBasedWorkflowEngine(WorkflowInstanceRepository repo, PrioritySorter prioritizer, long conditionWait){
    this.repo = repo;
    this.prioritizer = prioritizer;
    this.wmgrUrl = null;
    this.conditionWait = conditionWait;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#startWorkflow(org.apache
   * .oodt.cas.workflow.structs.Workflow, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public WorkflowInstance startWorkflow(Workflow workflow, Metadata metadata)
      throws EngineException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#stopWorkflow(java.lang
   * .String)
   */
  @Override
  public void stopWorkflow(String workflowInstId) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#pauseWorkflowInstance
   * (java.lang.String)
   */
  @Override
  public void pauseWorkflowInstance(String workflowInstId) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#resumeWorkflowInstance
   * (java.lang.String)
   */
  @Override
  public void resumeWorkflowInstance(String workflowInstId) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getInstanceRepository()
   */
  @Override
  public WorkflowInstanceRepository getInstanceRepository() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#updateMetadata(java.
   * lang.String, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public boolean updateMetadata(String workflowInstId, Metadata met) {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#setWorkflowManagerUrl
   * (java.net.URL)
   */
  @Override
  public void setWorkflowManagerUrl(URL url) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWallClockMinutes(
   * java.lang.String)
   */
  @Override
  public double getWallClockMinutes(String workflowInstId) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#
   * getCurrentTaskWallClockMinutes(java.lang.String)
   */
  @Override
  public double getCurrentTaskWallClockMinutes(String workflowInstId) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWorkflowInstanceMetadata
   * (java.lang.String)
   */
  @Override
  public Metadata getWorkflowInstanceMetadata(String workflowInstId) {
    // TODO Auto-generated method stub
    return null;
  }
  
  class QueueWorker implements Runnable{
    
    private boolean work;
    
    public QueueWorker(){
      this.work = true;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      while(work) {
        try {
          List<WorkflowInstance> instances = null;
          synchronized(repo){
            instances = repo.getWorkflowInstances();
          }
          
          List<WorkflowProcessor> runnableProcessors = new Vector<WorkflowProcessor>();
          for (WorkflowInstance instance : instances) {
            if (!work)
              break;
            if (isRunnableInstance(instance)) {
              synchronized(repo){
                instance.setStatus(WorkflowStatus.STARTED);
                repo.updateWorkflowInstance(instance);
              }
              
              synchronized(runnableProcessors){
                WorkflowInstance inst = new WorkflowInstance();
                Workflow workflow = new Workflow();
                workflow.setId(instance.getId()+"-"+instance.getCurrentTaskId());
                WorkflowTask task = getTask(instance.getWorkflow().getTasks(), instance.getCurrentTaskId());
                workflow.setName(task.getTaskName());
                workflow.getTasks().add(task);
                inst.setId(UUID.randomUUID().toString());
                inst.setWorkflow(workflow);
                inst.setCurrentTaskStartDateTimeIsoStr(DateConvert.isoFormat(new Date()));
                inst.setPriority(instance.getPriority());
                inst.setSharedContext(instance.getSharedContext());
                
                SequentialProcessor processor = 
                  new SequentialProcessor(inst, repo, wmgrUrl, conditionWait);
                runnableProcessors.add(processor);
              }
            }

            prioritizer.sort(runnableProcessors);
            
            //take a breather
            try {
              synchronized(this) {
                this.wait(1);
              }
            }catch (Exception e){}
          }
        }catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      try {
        synchronized(this) {
          this.wait(2000);
        }
      }catch (Exception e){}
    }
    
    private WorkflowTask getTask(List<WorkflowTask> tasks, String id){
      if(tasks != null && tasks.size() > 0){
        for(WorkflowTask task: tasks){
          if(task.getTaskId().equals(id)){
            return task;
          }
        }
      }
      
      return null;
    }
    
    private boolean isRunnableInstance(WorkflowInstance instance){
       return !instance.getStatus().equals(WorkflowStatus.ERROR) && 
       !instance.getStatus().equals(WorkflowStatus.FINISHED) && 
       !instance.getStatus().equals(WorkflowStatus.METADATA_MISSING) && 
       !instance.getStatus().equals(WorkflowStatus.PAUSED);
    }
  }


}
