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
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.TaskJobInput;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An instance of the {@link WorkflowProcessorThread} that works with the 
 * non-blocking thread pool engine.
 * </p>
 * 
 */

public class NonBlockingIterativeWorkflowProcessorThread implements
        WorkflowProcessorThread, WorkflowStatus, CoreMetKeys {

    /* the default queue name if we're using resmgr job submission */
    private static final String DEFAULT_QUEUE_NAME = "high";

    /* an id representing the current task that we are on in the workflow */
    private String taskId = null;

    /* the workflow instance that this processor thread is processing */
    private WorkflowInstance workflowInst = null;

    /* should our workflow processor thread start running? */
    private boolean running = false;

    /* our instance repository used to persist workflow instance info */
    private WorkflowInstanceRepository instanceRepository = null;

    /*
     * our client to a resource manager: if null, local task execution will be
     * performed
     */
    private XmlRpcResourceManagerClient rClient = null;

    /* polling wait for res mgr */
    private long pollingWaitTime = 10L;

    /*
     * should our workflow processor thread pause, and not move onto the next
     * task?
     */
    private boolean pause = false;

    /* our log stream */
    private static Logger LOG = Logger
            .getLogger(IterativeWorkflowProcessorThread.class.getName());

    private Map CONDITION_CACHE = new HashMap();

    /* the parent workflow manager url that executed this processor thread */
    private URL wmgrParentUrl = null;

    /* the currently executing jobId if we're using the resource manager */
    private String currentJobId = null;

    public NonBlockingIterativeWorkflowProcessorThread(WorkflowInstance wInst,
            WorkflowInstanceRepository instRep, URL wParentUrl) {
        workflowInst = wInst;
        taskId = workflowInst.getCurrentTaskId();
        this.instanceRepository = instRep;

        /* start out the gates running */
        running = true;

        pollingWaitTime = Long
                .getLong(
                        "org.apache.oodt.cas.workflow.engine.resourcemgr.pollingWaitTime",
                        10).longValue();

        wmgrParentUrl = wParentUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        /*
         * okay, we got into the run method, mark the start date time for the
         * workflow instance here
         */
        //String startDateTimeIsoStr = DateConvert.isoFormat(new Date());
       //workflowInst.setStartDateTimeIsoStr(startDateTimeIsoStr);
        // persist it
        //persistWorkflowInstance();

        while (running) {
            if (pause) {
                LOG.log(Level.FINE,
                        "IterativeWorkflowProcessorThread: Skipping execution: Paused: CurrentTask: "
                                + getTaskNameById(workflowInst
                                        .getCurrentTaskId()));
                continue;
            }

            WorkflowTask task = getTaskById(workflowInst.getWorkflow(),workflowInst.getCurrentTaskId());
            //WorkflowTask task = (WorkflowTask) taskIterator.next();
            //workflowInst.setCurrentTaskId(task.getTaskId());
            
            
            
            
            // now persist it
            //persistWorkflowInstance();

            // check to see if req met fields are present
            // if they aren't, set the status to METERROR, and then fail
            //if (!checkTaskRequiredMetadata(task, this.workflowInst
            //        .getSharedContext())) {
            //    this.workflowInst.setStatus(METADATA_MISSING);
            //    persistWorkflowInstance();
                // now break out of this run loop
            //    return;
            //}

            // this is where the pre-conditions come in
            // only execute the below code when it's passed all of its
            // pre-conditions
            /*if (task.getConditions() != null) {
                while (!satisfied(task.getConditions(), task.getTaskId()) && !isStopped()) {

                    // if we're not paused, go ahead and pause us now
                    if (!isPaused()) {
                        pause();
                    }

                    LOG.log(Level.FINEST, "Pre-conditions for task: "
                            + task.getTaskName() + " unsatisfied: waiting: "
                            + waitForConditionSatisfy
                            + " seconds before checking again.");
                    try {
                        Thread.currentThread().sleep(
                                waitForConditionSatisfy * 1000);
                    } catch (InterruptedException ignore) {
                    }

                    // check to see if we've been resumed, if so, break
                    // the loop and start
                    if (!isPaused()) {
                        break;
                    }
                }
                
                // check to see if we've been killed
                if (isStopped()){
                    break;
                }
                

                // un pause us (if needed)
                if (isPaused()) {
                    resume();
                }
            }*/

            // task execution
            LOG.log(Level.FINEST,
                    "IterativeWorkflowProcessorThread: Executing task: "
                            + task.getTaskName());

            WorkflowTaskInstance taskInstance = GenericWorkflowObjectFactory
                    .getTaskObjectFromClassName(task.getTaskInstanceClassName());
            // add the TaskId and the JobId and ProcessingNode
            // TODO: unfake the JobId
            workflowInst.getSharedContext().replaceMetadata(TASK_ID,
                    task.getTaskId());
            workflowInst.getSharedContext().replaceMetadata(WORKFLOW_INST_ID,
                    workflowInst.getId());
            workflowInst.getSharedContext().replaceMetadata(JOB_ID,
                    workflowInst.getId());
            workflowInst.getSharedContext().replaceMetadata(PROCESSING_NODE,
                    getHostname());
            workflowInst.getSharedContext().replaceMetadata(
                    WORKFLOW_MANAGER_URL, this.wmgrParentUrl.toString());

            if (rClient != null) {
                // build up the Job
                // and the Job Input
                Job taskJob = new Job();
                taskJob.setName(task.getTaskId());
                taskJob
                        .setJobInstanceClassName("org.apache.oodt.cas.workflow.structs.TaskJob");
                taskJob
                        .setJobInputClassName("org.apache.oodt.cas.workflow.structs.TaskJobInput");
                taskJob.setLoadValue(new Integer(2));
                taskJob.setQueueName(task.getTaskConfig().getProperty(
                        QUEUE_NAME) != null ? task.getTaskConfig()
                        .getProperty(QUEUE_NAME) : DEFAULT_QUEUE_NAME);

                TaskJobInput in = new TaskJobInput();
                in.setDynMetadata(workflowInst.getSharedContext());
                in.setTaskConfig(task.getTaskConfig());
                in.setWorkflowTaskInstanceClassName(task
                        .getTaskInstanceClassName());

                workflowInst.setStatus(RESMGR_SUBMIT);
                persistWorkflowInstance();

                try {
                    // this is * NOT * a blocking operation so when it returns
                    // the job may not actually have finished executing
                    // so we go into a waiting/sleep behavior using the passed
                    // back job id to wait until the job has actually finished
                    // executing

                    this.currentJobId = rClient.submitJob(taskJob, in);

                    while (!safeCheckJobComplete(this.currentJobId) && !isStopped()) {
                        // sleep for 5 seconds then come back
                        // and check again
                        try {
                            Thread.currentThread()
                                    .sleep(pollingWaitTime * 1000);
                        } catch (InterruptedException ignore) {
                        }
                    }

                    // okay job is done: TODO: fix this hack
                    // the task update time was set remotely
                    // by remote task, so let's read it now
                    // from the instRepo (which will have the updated
                    // time)

                    if (isStopped()) {
                        // this means that this workflow was killed, so
                        // gracefully exit
                        break;
                    }

                    WorkflowInstance updatedInst = null;
                    try {
                        updatedInst = instanceRepository
                                .getWorkflowInstanceById(workflowInst.getId());
                        workflowInst = updatedInst;
                    } catch (InstanceRepositoryException e) {
                        e.printStackTrace();
                        LOG.log(Level.WARNING, "Unable to get "
                                + "updated workflow " + "instance record "
                                + "when executing remote job: Message: "
                                + e.getMessage());
                    }

                } catch (JobExecutionException e) {
                    LOG.log(Level.WARNING,
                            "Job execution exception using resource manager to execute job: Message: "
                                    + e.getMessage());
                }
            } else {
                // we started, so mark it
                workflowInst.setStatus(STARTED);
                // go ahead and persist the workflow instance, after we
                // save the current task start date time
                String currentTaskIsoStartDateTimeStr = DateConvert
                        .isoFormat(new Date());
                workflowInst
                        .setCurrentTaskStartDateTimeIsoStr(currentTaskIsoStartDateTimeStr);
                workflowInst.setCurrentTaskEndDateTimeIsoStr(null); /*
                                                                     * clear
                                                                     * this out
                                                                     * until
                                                                     * it's
                                                                     * ready
                                                                     */
                persistWorkflowInstance();
                executeTaskLocally(taskInstance, workflowInst
                        .getSharedContext(), task.getTaskConfig(), task
                        .getTaskName());
                String currentTaskIsoEndDateTimeStr = DateConvert
                        .isoFormat(new Date());
                workflowInst
                        .setCurrentTaskEndDateTimeIsoStr(currentTaskIsoEndDateTimeStr);
                persistWorkflowInstance();
            }

            LOG.log(Level.FINEST,
                    "IterativeWorkflowProcessorThread: Completed task: "
                            + task.getTaskName());

        }

        LOG.log(Level.FINEST,
                "IterativeWorkflowProcessorThread: Completed workflow: "
                        + workflowInst.getWorkflow().getName());
        if (!isStopped()) {
            stop();
        }

    }

    public WorkflowInstance getWorkflowInstance() {
        return workflowInst;
    }

    public synchronized void stop() {
        running = false;
        // if the resource manager is active
        // then kill the current job there
        if (this.rClient != null && this.currentJobId != null) {
            if (!this.rClient.killJob(this.currentJobId)) {
                LOG.log(Level.WARNING, "Attempt to kill "
                        + "current resmgr job: [" + this.currentJobId
                        + "]: failed");
            }
        }

        workflowInst.setStatus(FINISHED);
        String isoEndDateTimeStr = DateConvert.isoFormat(new Date());
        workflowInst.setEndDateTimeIsoStr(isoEndDateTimeStr);
        persistWorkflowInstance();
    }

    public synchronized void resume() {
        pause = false;
        workflowInst.setStatus(STARTED);
        persistWorkflowInstance();
    }

    public synchronized void pause() {
        pause = true;
        workflowInst.setStatus(PAUSED);
        persistWorkflowInstance();
    }

    /**
     * @return True if the WorkflowInstance managed by this processor is paused.
     */
    public boolean isPaused() {
        return pause == true;
    }

    public boolean isStopped() {
        return !running;
    }

    /**
     * @return Returns the fCurrentTaskId.
     */
    public String getCurrentTaskId() {
        return workflowInst.getCurrentTaskId();
    }

    /**
     * @param workflowInst
     *            The fWorkflowInst to set.
     */
    public void setWorkflowInst(WorkflowInstance workflowInst) {
        workflowInst = workflowInst;
    }

    /**
     * @return Returns the waitForConditionSatisfy.
     */
    //public long getWaitforConditionSatisfy() {
    //    return waitForConditionSatisfy;
    //}

    /**
     * @param waitforConditionSatisfy
     *            The waitForConditionSatisfy to set.
     */
    //public void setWaitforConditionSatisfy(long waitforConditionSatisfy) {
    //    waitForConditionSatisfy = waitforConditionSatisfy;
    //}

    /**
     * @return the instRep
     */
    public WorkflowInstanceRepository getInstanceRepository() {
        return instanceRepository;
    }

    /**
     * @param instRep
     *            the instRep to set
     */
    public void setInstanceRepository(WorkflowInstanceRepository instRep) {
        this.instanceRepository = instRep;
    }

    /**
     * @return the rClient
     */
    public XmlRpcResourceManagerClient getRClient() {
        return rClient;
    }

    /**
     * @param client
     *            the rClient to set
     */
    public void setRClient(XmlRpcResourceManagerClient client) {
        rClient = client;
        if (rClient != null) {
            LOG.log(Level.INFO, "Resource Manager Job Submission enabled to: ["
                    + rClient.getResMgrUrl() + "]");
        }
    }

    /**
     * @return the wmgrParentUrl
     */
    public URL getWmgrParentUrl() {
        return wmgrParentUrl;
    }

    /**
     * @param wmgrParentUrl
     *            the wmgrParentUrl to set
     */
    public void setWmgrParentUrl(URL wmgrParentUrl) {
        this.wmgrParentUrl = wmgrParentUrl;
    }

    /*private boolean checkTaskRequiredMetadata(WorkflowTask task,
            Metadata dynMetadata) {
        if (task.getRequiredMetFields() == null
                || (task.getRequiredMetFields() != null && task
                        .getRequiredMetFields().size() == 0)) {
            LOG.log(Level.INFO, "Task: [" + task.getTaskName()
                    + "] has no required metadata fields");
            return true; // no required metadata, so we're fine
        }

        for (Iterator i = task.getRequiredMetFields().iterator(); i.hasNext();) {
            String reqField = (String) i.next();
            if (!dynMetadata.containsKey(reqField)) {
                LOG.log(Level.SEVERE, "Checking metadata key: [" + reqField
                        + "] for task: [" + task.getTaskName()
                        + "]: failed: aborting workflow");
                return false;
            }
        }

        LOG.log(Level.INFO, "All required metadata fields present for task: ["
                + task.getTaskName() + "]");

        return true;
    }*/

    private String getTaskNameById(String taskId) {
        for (Iterator i = workflowInst.getWorkflow().getTasks().iterator(); i
                .hasNext();) {
            WorkflowTask task = (WorkflowTask) i.next();
            if (task.getTaskId().equals(taskId)) {
                return task.getTaskName();
            }
        }

        return null;
    }

    private boolean satisfied(List conditionList, String taskId) {
        for (Iterator i = conditionList.iterator(); i.hasNext();) {
            WorkflowCondition c = (WorkflowCondition) i.next();
            WorkflowConditionInstance cInst = null;

            // see if we've already cached this condition instance
            if (CONDITION_CACHE.get(taskId) != null) {
                HashMap conditionMap = (HashMap) CONDITION_CACHE.get(taskId);

                /*
                 * okay we have some conditions cached for this task, see if we
                 * have the one we need
                 */
                if (conditionMap.get(c.getConditionId()) != null) {
                    cInst = (WorkflowConditionInstance) conditionMap.get(c
                            .getConditionId());
                }
                /* if not, then go ahead and create it and cache it */
                else {
                    cInst = GenericWorkflowObjectFactory
                            .getConditionObjectFromClassName(c
                                    .getConditionInstanceClassName());
                    conditionMap.put(c.getConditionId(), cInst);
                }
            }
            /* no conditions cached yet, so set everything up */
            else {
                HashMap conditionMap = new HashMap();
                cInst = GenericWorkflowObjectFactory
                        .getConditionObjectFromClassName(c
                                .getConditionInstanceClassName());
                conditionMap.put(c.getConditionId(), cInst);
                CONDITION_CACHE.put(taskId, conditionMap);
            }

            // actually perform the evaluation
            if (!cInst.evaluate(workflowInst.getSharedContext(), c.getTaskConfig())) {
                return false;
            }
        }

        return true;
    }

    private String getHostname() {
        try {
            // Get hostname by textual representation of IP address
            InetAddress addr = InetAddress.getLocalHost();
            // Get the host name
            String hostname = addr.getHostName();
            return hostname;
        } catch (UnknownHostException e) {
        }
        return null;
    }

    private void persistWorkflowInstance() {
        try {
            instanceRepository.updateWorkflowInstance(workflowInst);
        } catch (InstanceRepositoryException e) {
            LOG.log(Level.WARNING, "Exception persisting workflow instance: ["
                    + workflowInst.getId() + "]: Message: " + e.getMessage());
        }
    }

    private void executeTaskLocally(WorkflowTaskInstance instance,
            Metadata met, WorkflowTaskConfiguration cfg, String taskName) {
        try {
            LOG.log(Level.INFO, "Executing task: [" + taskName + "] locally");
            instance.run(met, cfg);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception executing task: [" + taskName
                    + "] locally: Message: " + e.getMessage());
        }
    }

    private boolean safeCheckJobComplete(String jobId) {
        try {
            return rClient.isJobComplete(jobId);
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Exception checking completion status for job: [" + jobId
                            + "]: Messsage: " + e.getMessage());
            return false;
        }
    }
    
    private WorkflowTask getTaskById(Workflow w, String Id){
    	List tasks = w.getTasks();
    	for(int i=0;i<tasks.size();i++){
    		if(((WorkflowTask)tasks.get(i)).getTaskId().equals(Id)){
    			return (WorkflowTask)tasks.get(i);
    		}
    	}
    	
    	return null;
    }

}

