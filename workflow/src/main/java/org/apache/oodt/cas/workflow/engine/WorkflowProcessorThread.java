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
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Threaded interface for processing a {@link WorkflowInstance}. The job of
 * this class is to actually take the WorkflowInstance and execute its jobs. The
 * class should maintain the state of the instance, such as the currentTaskId,
 * and so forth.
 * </p>
 * 
 */
public interface WorkflowProcessorThread extends Runnable {

    /**
     * @return The {@link WorkflowInstance} that this Thread is processing.
     */
    public WorkflowInstance getWorkflowInstance();

    /**
     * <p>
     * Stops once and for all the thread from processing the workflow. This
     * method should not maintain the state of the workflow, it should
     * gracefully shut down the WorkflowProcessorThread and any of its
     * subsequent resources.
     * </p>
     * 
     */
    public void stop();

    /**
     * <p>
     * Resumes execution of a {@link #pause}d {@link WorkflowInstace} by this
     * WorkflowProcessorThread.
     * </p>
     * 
     */
    public void resume();

    /**
     * <p>
     * Pauses exectuion of a {@link WorkflowInstace} being handled by this
     * WorkflowProcessorThread.
     * </p>
     * 
     */
    public void pause();

}
