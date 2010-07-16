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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.cas.workflow.engine.IterativeWorkflowProcessorThread;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.engine.NonBlockingShepardThread;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//java.util.concurrent imports
import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * @author woollard
 * @version $Revsion$
 * 
 * <p>
 * This is a non-blocking implementation of the Thread-pool based workflow engine.
 * </p>
 */
public class NonBlockingThreadPoolWorkflowEngine implements WorkflowEngine, WorkflowStatus {

    /* our thread pool */
    private PooledExecutor pool = null;
    
    /*our workflow queue*/
    private Vector workflowQueue = null;

    /* our worker thread hash mapping worker threads to workflow instance ids */
    private HashMap workerMap = null;
    
    public Map CONDITION_CACHE = new HashMap();

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(ThreadPoolWorkflowEngine.class.getName());

    /* our instance repository */
    private WorkflowInstanceRepository instRep = null;

    /* our resource manager client */
    private XmlRpcResourceManagerClient rClient = null;

    /* the URL pointer to the parent Workflow Manager */
    private URL wmgrUrl = null;

    /**
     * Default Constructor.
     * 
     * @param instRep
     *            The WorkflowInstanceRepository to be used by this engine.
     * @param queueSize
     *            The size of the queue that the workflow engine should use
     *            (irrelevant if unlimitedQueue is set to true)
     * @param maxPoolSize
     *            The minimum thread pool size.
     * @param minPoolSize
     *            The maximum thread pool size.
     * @param threadKeepAliveTime
     *            The amount of minutes that each thread in the pool should be
     *            kept alive.
     * @param unlimitedQueue
     *            Whether or not to use a queue whose bounds are dictated by the
     *            physical memory of the underlying hardware.
     * @param resUrl
     *            A URL pointer to a resource manager. If this is set Tasks will
     *            be wrapped as Resource Manager {@link Job}s and sent through
     *            the Resource Manager. If this parameter is not set, local
     *            execution (the default) will be used
     */
    public NonBlockingThreadPoolWorkflowEngine(WorkflowInstanceRepository instRep,
            int queueSize, int maxPoolSize, int minPoolSize,
            long threadKeepAliveTime, boolean unlimitedQueue, URL resUrl) {

        this.instRep = instRep;
        Channel c = null;
        if (unlimitedQueue) {
            c = new LinkedQueue();
        } else {
            c = new BoundedBuffer(queueSize);
        }

        pool = new PooledExecutor(c, maxPoolSize);
        pool.setMinimumPoolSize(minPoolSize);
        pool.setKeepAliveTime(1000 * 60 * threadKeepAliveTime);

        workerMap = new HashMap();
        workflowQueue = new Vector();

        if (resUrl != null)
            rClient = new XmlRpcResourceManagerClient(resUrl);
        
        
        //start shepard thread
        NonBlockingShepardThread thread = new NonBlockingShepardThread(this);
        try {
        	pool.execute(thread);
        } catch (InterruptedException e){
        	LOG.log(Level.SEVERE, "Error starting shepard thread.");
        }
        
        
    }
    
    
    public Vector getWorkflowQueue(){
    	return workflowQueue;
    }
    
    public void submitWorkflowInstancetoPool(WorkflowInstance wInst){
    	IterativeWorkflowProcessorThread worker = new IterativeWorkflowProcessorThread(
                wInst, instRep, this.wmgrUrl);
        worker.setRClient(rClient);
        workerMap.put(wInst.getId(), worker);

        try {
            pool.execute(worker);
        } catch (InterruptedException e) {
        	LOG.log(Level.WARNING, "Error running workflow: "
                    + wInst.getWorkflow().getId());
        }
    }
    
   /* public void run() {
    	System.out.println("Did the run method get called?");
		while(true){
			for(int i=0;i<workflowQueue.size();i++){
				WorkflowInstance wInst = (WorkflowInstance)workflowQueue.get(i);	
				WorkflowTask task = getTaskById(wInst.getWorkflow(),wInst.getCurrentTaskId());
			
				//check required metadata on current task
				if (!checkTaskRequiredMetadata(task, wInst.getSharedContext())) {
	                wInst.setStatus(METADATA_MISSING);
	                try{
	                  persistWorkflowInstance(wInst);
	                } catch (EngineException e){
	                	LOG.log(Level.WARNING, "Error running task: "
	                            + task.getTaskName());
	                }
	                break;
				}
				
				//check preconditions on current task
				if (task.getConditions() != null) {
	                if(!satisfied(wInst)) {

	                    LOG.log(Level.FINEST, "Pre-conditions for task: "
	                            + task.getTaskName() + " unsatisfied.");
	                
	                } else {
	                	//create thread, remove from queue, then if there is another task, increment and read to the queue
	                	
	                	IterativeWorkflowProcessorThread worker = new IterativeWorkflowProcessorThread(
	                            wInst, instRep, this.wmgrUrl);
	                    worker.setRClient(rClient);
	                    workerMap.put(wInst.getId(), worker);

	                    try {
	                        pool.execute(worker);
	                    } catch (InterruptedException e) {
	                    	LOG.log(Level.WARNING, "Error running task: "
		                            + task.getTaskName());
	                    }
	                    
	                    workflowQueue.remove(wInst);
	                    i--;
	                    
	                    List tasks = wInst.getWorkflow().getTasks();
	                    for(int j=0;j<tasks.size();j++){
	                    	if( ((WorkflowTask)tasks.get(j)).getTaskId().equals(wInst.getCurrentTaskId()) && j<(tasks.size()-1)){
	                    		wInst.setCurrentTaskId( ((WorkflowTask)tasks.get(j+1)).getTaskId() );
	                    		workflowQueue.add(wInst);
	                    	}
	                    }
	                }
	            }		
			}
			
			
		}
	}*/


    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#pauseWorkflowInstance(java.lang.String)
     */
    public synchronized void pauseWorkflowInstance(String workflowInstId) {
        // okay, try and look up that worker thread in our hash map
        IterativeWorkflowProcessorThread worker = (IterativeWorkflowProcessorThread) workerMap
                .get(workflowInstId);
        if (worker == null) {
            LOG
                    .log(
                            Level.WARNING,
                            "WorkflowEngine: Attempt to pause workflow instance id: "
                                    + workflowInstId
                                    + ", however, this engine is not tracking its execution");
            return;
        }

        // otherwise, all good
        worker.pause();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#resumeWorkflowInstance(java.lang.String)
     */
    public synchronized void resumeWorkflowInstance(String workflowInstId) {
        // okay, try and look up that worker thread in our hash map
        IterativeWorkflowProcessorThread worker = (IterativeWorkflowProcessorThread) workerMap
                .get(workflowInstId);
        if (worker == null) {
            LOG.log(Level.WARNING,
                    "WorkflowEngine: Attempt to resume workflow instance id: "
                            + workflowInstId + ", however, this engine is "
                            + "not tracking its execution");
            return;
        }

        // also check to make sure that the worker is currently paused
        // only can resume WorkflowInstances that are paused, right?
        if (!worker.isPaused()) {
            LOG.log(Level.WARNING,
                    "WorkflowEngine: Attempt to resume a workflow that "
                            + "isn't paused currently: instance id: "
                            + workflowInstId);
            return;
        }

        // okay, all good
        worker.resume();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#startWorkflow(org.apache.oodt.cas.workflow.structs.Workflow,
     *      org.apache.oodt.cas.metadata.Metadata)
     */
    public synchronized WorkflowInstance startWorkflow(Workflow workflow,
            Metadata metadata) throws EngineException {
        // to start the workflow, we create a default workflow instance
        // populate it
        // persist it
    	// add it to the workflowQueue

        WorkflowInstance wInst = new WorkflowInstance();
        wInst.setWorkflow(workflow);
        wInst.setCurrentTaskId(((WorkflowTask) workflow.getTasks().get(0))
                .getTaskId());
        wInst.setSharedContext(metadata);
        wInst.setStatus(CREATED);
        persistWorkflowInstance(wInst);

        wInst.setStatus(QUEUED);
        String startDateTimeIsoStr = DateConvert.isoFormat(new Date());
        wInst.setStartDateTimeIsoStr(startDateTimeIsoStr);

        workflowQueue.add(wInst);
        persistWorkflowInstance(wInst);

        return wInst;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getInstanceRepository()
     */
    public WorkflowInstanceRepository getInstanceRepository() {
        return this.instRep;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#updateMetadata(java.lang.String,
     *      org.apache.oodt.cas.metadata.Metadata)
     */
    public synchronized boolean updateMetadata(String workflowInstId,
            Metadata met) {
        // okay, try and look up that worker
    	WorkflowInstance wInst = null;
    	try{
    	   wInst = this.instRep.getWorkflowInstanceById(workflowInstId);
           if (wInst != null) {
              wInst.setSharedContext(met);
              persistWorkflowInstance(wInst);
           } else {
        	   LOG.log(Level.WARNING,
                       "WorkflowEngine: Attempt to update metadata context "
                               + "for workflow instance id: " + workflowInstId
                               + ", however, this engine is "
                               + "not tracking its execution");
               return false;
           }
          
        } catch (Exception e) {
            	LOG.log(Level.WARNING, "Exception persisting workflow instance: ["
                    + wInst.getId() + "]: Message: "
                    + e.getMessage());
            	return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#setWorkflowManagerUrl(java.net.URL)
     */
    public void setWorkflowManagerUrl(URL url) {
        this.wmgrUrl = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#stopWorkflow(java.lang.String)
     */
    public synchronized void stopWorkflow(String workflowInstId) {
        // okay, try and look up that worker thread in our hash map
        IterativeWorkflowProcessorThread worker = (IterativeWorkflowProcessorThread) workerMap
                .get(workflowInstId);
        if (worker == null) {
            LOG.log(Level.WARNING,
                    "WorkflowEngine: Attempt to stop workflow instance id: "
                            + workflowInstId + ", however, this engine is "
                            + "not tracking its execution");
            return;
        }

        worker.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getCurrentTaskWallClockMinutes(java.lang.String)
     */
    public double getCurrentTaskWallClockMinutes(String workflowInstId) {
        // get the workflow instance that we're talking about
        WorkflowInstance inst = safeGetWorkflowInstanceById(workflowInstId);
        return getCurrentTaskWallClockMinutes(inst);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWorkflowInstanceMetadata(java.lang.String)
     */
    public Metadata getWorkflowInstanceMetadata(String workflowInstId) {
    	// okay, try and look up that worker
    	WorkflowInstance wInst = null;
    	
    	try{
    		wInst = this.instRep.getWorkflowInstanceById(workflowInstId);
            if (wInst != null) {
            // try and get the metadata
            // from the workflow instance repository (as it was persisted)
                return wInst.getSharedContext();
            }
        } catch (InstanceRepositoryException e) {
                LOG.log(Level.FINEST,
                        "WorkflowEngine: Attempt to get metadata "
                                + "for workflow instance id: " + workflowInstId
                                + ", however, this engine is "
                                + "not tracking its execution and the id: ["
                                + workflowInstId + "] "
                                + "was never persisted to "
                                + "the instance repository");
                e.printStackTrace();
                return new Metadata();
        }

        return wInst.getSharedContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWallClockMinutes(java.lang.String)
     */
    public double getWallClockMinutes(String workflowInstId) {
        // get the workflow instance that we're talking about
        WorkflowInstance inst = safeGetWorkflowInstanceById(workflowInstId);
        return getWallClockMinutes(inst);
    }

    protected static double getWallClockMinutes(WorkflowInstance inst) {
        if (inst == null) {
            return 0.0;
        }

        Date currentDateOrStopTime = (inst.getEndDateTimeIsoStr() != null
                && !inst.getEndDateTimeIsoStr().equals("") && !inst
                .getEndDateTimeIsoStr().equals("null")) ? safeDateConvert(inst
                .getEndDateTimeIsoStr()) : new Date();

        Date workflowStartDateTime = null;

        if (inst.getStartDateTimeIsoStr() == null
                || (inst.getStartDateTimeIsoStr() != null && (inst
                        .getStartDateTimeIsoStr().equals("") || inst
                        .getStartDateTimeIsoStr().equals("null")))) {
            return 0.0;
        }

        try {
            workflowStartDateTime = DateConvert.isoParse(inst
                    .getStartDateTimeIsoStr());
        } catch (ParseException e) {
            return 0.0;
        }

        long diffMs = currentDateOrStopTime.getTime()
                - workflowStartDateTime.getTime();
        double diffSecs = (diffMs * 1.0 / 1000.0);
        double diffMins = diffSecs / 60.0;
        return diffMins;

    }

    protected static double getCurrentTaskWallClockMinutes(WorkflowInstance inst) {
        if (inst == null) {
            return 0.0;
        }

        Date currentDateOrStopTime = (inst.getCurrentTaskEndDateTimeIsoStr() != null
                && !inst.getCurrentTaskEndDateTimeIsoStr().equals("") && !inst
                .getCurrentTaskEndDateTimeIsoStr().equals("null")) ? safeDateConvert(inst
                .getCurrentTaskEndDateTimeIsoStr())
                : new Date();

        Date workflowTaskStartDateTime = null;

        if (inst.getCurrentTaskStartDateTimeIsoStr() == null
                || (inst.getCurrentTaskStartDateTimeIsoStr() != null && (inst
                        .getCurrentTaskStartDateTimeIsoStr().equals("") || inst
                        .getCurrentTaskStartDateTimeIsoStr().equals("null")))) {
            return 0.0;
        }

        try {
            workflowTaskStartDateTime = DateConvert.isoParse(inst
                    .getCurrentTaskStartDateTimeIsoStr());
        } catch (ParseException e) {
            return 0.0;
        }

        // should never be in this state, so return 0
        if (workflowTaskStartDateTime.after(currentDateOrStopTime)) {
            LOG.log(Level.WARNING, "Start date time: ["
                    + DateConvert.isoFormat(workflowTaskStartDateTime)
                    + " of workflow inst [" + inst.getId() + "] is AFTER "
                    + "End date time: ["
                    + DateConvert.isoFormat(currentDateOrStopTime)
                    + "] of workflow inst.");
            return 0.0;
        }

        long diffMs = currentDateOrStopTime.getTime()
                - workflowTaskStartDateTime.getTime();
        double diffSecs = (diffMs * 1.0 / 1000.0);
        double diffMins = diffSecs / 60.0;
        return diffMins;
    }

    public synchronized void persistWorkflowInstance(WorkflowInstance wInst)
            throws EngineException {

        try {
            if (wInst.getId() == null
                    || (wInst.getId() != null && wInst.getId().equals(""))) {
                // we have to persist it by adding it
                // rather than updating it
                instRep.addWorkflowInstance(wInst);

            } else {
                // persist by update
                instRep.updateWorkflowInstance(wInst);
            }
        } catch (InstanceRepositoryException e) {
            e.printStackTrace();
            throw new EngineException(e.getMessage());
        }

    }

    private WorkflowInstance safeGetWorkflowInstanceById(String workflowInstId) {
        try {
            return instRep.getWorkflowInstanceById(workflowInstId);
        } catch (Exception e) {
            return null;
        }
    }

    private static Date safeDateConvert(String isoTimeStr) {
        try {
            return DateConvert.isoParse(isoTimeStr);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }
    
    private boolean checkTaskRequiredMetadata(WorkflowTask task,
            Metadata dynMetadata) {
        if (task.getRequiredMetFields() == null
                || (task.getRequiredMetFields() != null && task
                        .getRequiredMetFields().size() == 0)) {
            LOG.log(Level.INFO, "Task: [" + task.getTaskName()
                    + "] has no required metadata fields");
            return true; /* no required metadata, so we're fine */
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
    }

    private boolean satisfied(WorkflowInstance wInst) {
    	String taskId = wInst.getCurrentTaskId();
    	WorkflowTask task = getTaskById(wInst.getWorkflow(), taskId);
    	List conditionList = task.getConditions(); 
    	
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
            if (!cInst.evaluate(wInst.getSharedContext(), c.getTaskConfig())) {
                return false;
            }
        }

        return true;
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

