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

//JDK imports
import java.util.Calendar;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessorQueue;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.commons.date.DateUtils;

/**
 * 
 * Intercepts the calls to {@link TaskQuerier#getNext()} and injects
 * StartDateTime (and potentially other met fields into the
 * .
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MetSetterTaskQuerier extends TaskQuerier {

  private static final long WAIT_SECS=1;
  
  /**
   * @param processorQueue
   * @param prioritizer
   */
  public MetSetterTaskQuerier(WorkflowProcessorQueue processorQueue,
      PrioritySorter prioritizer) {
    super(processorQueue, prioritizer, null, WAIT_SECS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.engine.TaskQuerier#getNext()
   */
  @Override
  public TaskProcessor getNext() {
    TaskProcessor taskProcessor = super.getNext();
    if(taskProcessor == null) return null;
    Metadata met = new Metadata();
    met.addMetadata("StartDateTime", DateUtils.toString(Calendar.getInstance()));
    taskProcessor.getWorkflowInstance().setSharedContext(met);
    return taskProcessor;
  }

}
