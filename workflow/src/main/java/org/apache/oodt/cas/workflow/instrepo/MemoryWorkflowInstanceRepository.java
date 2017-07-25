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
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * A simple {@link WorkflowInstanceRepository} that does not require the use of
 * a database to track the status of the Workflow executions it manages.
 * </p>
 */
public class MemoryWorkflowInstanceRepository extends
        AbstractPaginatibleInstanceRepository {

    /* our workflow instance map: maps workfllowInstId to WorkflowInstance */
    private ConcurrentHashMap workflowInstMap = null;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(MemoryWorkflowInstanceRepository.class.getName());

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public MemoryWorkflowInstanceRepository(int pageSize) {
        workflowInstMap = new ConcurrentHashMap();
        this.pageSize = pageSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#addWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void addWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        String instId = "urn:" + DateConvert.isoFormat(new Date());
        wInst.setId(instId);
        workflowInstMap.put(instId, wInst);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#removeWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void removeWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        workflowInstMap.remove(wInst.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#updateWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void updateWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        WorkflowInstance inst = (WorkflowInstance) workflowInstMap.get(wInst
                .getId());

        if (inst == null) {
            LOG
                    .log(
                            Level.WARNING,
                            "Attempt to update workflow instance id: "
                                    + wInst.getId()
                                    + " workflow instance is not being tracked by this engine!");
            return;
        }

        workflowInstMap.put(wInst.getId(), wInst);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getWorkflowInstanceById(java.lang.String)
     */
    public WorkflowInstance getWorkflowInstanceById(String workflowInstId)
            throws InstanceRepositoryException {
        return (WorkflowInstance) workflowInstMap.get(workflowInstId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getWorkflowInstances()
     */
    public List getWorkflowInstances() throws InstanceRepositoryException {
        return Arrays.asList(workflowInstMap.values().toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getWorkflowInstancesByStatus(java.lang.String)
     */
    public List getWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException {
        List instances = new Vector();

        for (Object o : workflowInstMap.keySet()) {
            String workflowInstId = (String) o;
            WorkflowInstance inst = (WorkflowInstance) workflowInstMap
                .get(workflowInstId);
            if (inst.getStatus().equals(status)) {
                instances.add(inst);
            }
        }

        return instances;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.AbstractPaginatibleInstanceRepository#paginateWorkflows(int,
     *      java.lang.String)
     */
    protected List paginateWorkflows(int pageNum, String status)
            throws InstanceRepositoryException {
        // first sort insts by startDateTime
        List allInsts = Arrays.asList(this.workflowInstMap.keySet().toArray());
        Collections.sort(allInsts, new Comparator() {

            public int compare(Object o1, Object o2) {
                WorkflowInstance inst1 = (WorkflowInstance) o1;
                WorkflowInstance inst2 = (WorkflowInstance) o2;

                return inst1.getStartDateTimeIsoStr().compareTo(
                        inst2.getStartDateTimeIsoStr());
            }

        });

        int startNum = (pageNum - 1) * pageSize;
        if (startNum > allInsts.size()) {
            startNum = 0;
        }

        List instIds = new Vector(pageSize);

        for (int i = startNum; i < Math.min(allInsts.size(),
                (startNum + pageSize)); i++) {
            WorkflowInstance wInst = (WorkflowInstance)allInsts.get(i);
            instIds.add(wInst.getId());
        }

        return instIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getNumWorkflowInstances()
     */
    public int getNumWorkflowInstances() throws InstanceRepositoryException {
        return this.workflowInstMap.keySet().size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getNumWorkflowInstancesByStatus(java.lang.String)
     */
    public int getNumWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException {
        int cnt = 0;

        if (this.workflowInstMap != null && this.workflowInstMap.keySet().size() > 0) {
            for (Object o : this.workflowInstMap.keySet()) {
                String wInstId = (String) o;
                WorkflowInstance inst = (WorkflowInstance) this.workflowInstMap
                    .get(wInstId);
                if (inst.getStatus().equals(status)) {
                    cnt++;
                }
            }
        }

        return cnt;
    }

    @Override
    public synchronized boolean clearWorkflowInstances() throws InstanceRepositoryException {
      this.workflowInstMap.clear();
      return true;
    }

}
