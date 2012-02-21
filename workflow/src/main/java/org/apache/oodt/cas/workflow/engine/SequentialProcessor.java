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

package org.apache.oodt.cas.workflow.engine;

//OODT imports
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.TaskJobInput;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * An instance of the {@link WorkflowProcessor} that processes through an
 * iterative {@link this.workflowInstance}. This class keeps an
 * {@llink Iterator} that allows it to move from one end of a sequential
 * {@link Workflow} processing pipeline to another. This class should only be
 * used to process science pipeline style {@link Workflow}s, i.e., those which
 * resemble an iterative processing pipelines, with no forks, or concurrent task
 * executions.
 * 
 * @author mattmann
 * 
 */

public class SequentialProcessor extends WorkflowProcessor implements
    WorkflowStatus, CoreMetKeys {

  private Iterator<WorkflowTask> taskIterator;

  /* our log stream */
  private static Logger LOG = Logger
      .getLogger(SequentialProcessor.class.getName());
  
  private ConditionProcessor conditionEvaluator;

  public SequentialProcessor(WorkflowInstance wInst,
      WorkflowInstanceRepository instRep, URL wParentUrl, long conditionWait) {
    super(wInst, instRep, wParentUrl, conditionWait);
    taskIterator = this.workflowInstance.getWorkflow().getTasks().iterator();
    this.conditionEvaluator = new ConditionProcessor();
  }


  public synchronized void stop() {
    running = false;
    //something with resource manager client here

    this.workflowInstance.setStatus(FINISHED);
    String isoEndDateTimeStr = DateConvert.isoFormat(new Date());
    this.workflowInstance.setEndDateTimeIsoStr(isoEndDateTimeStr);
    this.persistWorkflowInstance();
  }

  public synchronized void resume() {
    //this.paused = false;
    this.workflowInstance.setStatus(STARTED);
    this.persistWorkflowInstance();
  }

  public synchronized void pause() {
    //this.paused = true;
    this.workflowInstance.setStatus(PAUSED);
    this.persistWorkflowInstance();
  }


  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowProcessor#getRunnableSubProcessors()
   */
  @Override
  protected List<WorkflowProcessor> getRunnableSubProcessors() {
    // TODO Auto-generated method stub
    return null;
  }

}
