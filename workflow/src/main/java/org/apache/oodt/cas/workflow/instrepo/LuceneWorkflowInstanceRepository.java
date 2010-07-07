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
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//Lucene imports
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;

//JUG imports
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An implementation of the {@link WorkflowEngine} interface that is backed by
 * <a href="http://lucene.apache.org">Apache Lucene</a>.
 * </p>.
 */
public class LuceneWorkflowInstanceRepository extends
        AbstractPaginatibleInstanceRepository {

    /* path to lucene index directory to store wInst info */
    private String idxFilePath = null;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(LuceneWorkflowInstanceRepository.class.getName());

    /* our workflow inst id generator */
    private static UUIDGenerator generator = UUIDGenerator.getInstance();

    /**
     * 
     */
    public LuceneWorkflowInstanceRepository(String idxPath, int pageSize) {
        this.idxFilePath = idxPath;
        this.pageSize = pageSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getNumWorkflowInstances()
     */
    public int getNumWorkflowInstances() throws InstanceRepositoryException {
        IndexSearcher searcher = null;
        int numInsts = -1;

        try {
            searcher = new IndexSearcher(idxFilePath);
            Term instIdTerm = new Term("myfield", "myvalue");
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.STRING, true));
            Hits hits = searcher.search(query, sort);

            numInsts = hits.length();

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception ignore) {
                }
                searcher = null;
            }
        }

        return numInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getNumWorkflowInstancesByStatus(java.lang.String)
     */
    public int getNumWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException {
        IndexSearcher searcher = null;
        int numInsts = -1;

        try {
            searcher = new IndexSearcher(idxFilePath);
            Term instIdTerm = new Term("workflow_inst_status", status);
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.STRING, true));
            Hits hits = searcher.search(query, sort);

            numInsts = hits.length();

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception ignore) {
                }
                searcher = null;
            }
        }

        return numInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#addWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void addWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        // generate UUID for inst
        UUID uuid = UUIDGenerator.getInstance().generateTimeBasedUUID();
        wInst.setId(uuid.toString());

        addWorkflowInstanceToCatalog(wInst);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#removeWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void removeWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        removeWorkflowInstanceDocument(wInst);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#updateWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void updateWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        removeWorkflowInstanceDocument(wInst);
        addWorkflowInstanceToCatalog(wInst);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getWorkflowInstanceById(java.lang.String)
     */
    public WorkflowInstance getWorkflowInstanceById(String workflowInstId)
            throws InstanceRepositoryException {
        IndexSearcher searcher = null;
        WorkflowInstance wInst = null;

        try {
            searcher = new IndexSearcher(idxFilePath);
            Term instIdTerm = new Term("workflow_inst_id", workflowInstId);
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Hits hits = searcher.search(query);

            if (hits.length() != 1) {
                LOG.log(Level.WARNING, "The workflow instance: ["
                        + workflowInstId + "] is not being "
                        + "managed by this " + "workflow engine, or "
                        + "is not unique in the catalog!");
                return null;
            } else {
                Document instDoc = hits.doc(0);
                wInst = toWorkflowInstance(instDoc);
            }

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception ignore) {
                }
                searcher = null;
            }
        }

        return wInst;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getWorkflowInstances()
     */
    public List getWorkflowInstances() throws InstanceRepositoryException {
        IndexSearcher searcher = null;
        List wInsts = null;

        try {
            searcher = new IndexSearcher(idxFilePath);
            Term instIdTerm = new Term("myfield", "myvalue");
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.STRING, true));
            Hits hits = searcher.search(query, sort);

            if (hits.length() > 0) {
                wInsts = new Vector(hits.length());

                for (int i = 0; i < hits.length(); i++) {
                    Document doc = hits.doc(i);
                    WorkflowInstance wInst = toWorkflowInstance(doc);
                    wInsts.add(wInst);
                }
            }

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception ignore) {
                }
                searcher = null;
            }
        }

        return wInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getWorkflowInstancesByStatus(java.lang.String)
     */
    public List getWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException {
        IndexSearcher searcher = null;
        List wInsts = null;

        try {
            searcher = new IndexSearcher(idxFilePath);
            Term instIdTerm = new Term("workflow_inst_status", status);
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.STRING, true));
            Hits hits = searcher.search(query, sort);

            if (hits.length() > 0) {
                wInsts = new Vector(hits.length());

                for (int i = 0; i < hits.length(); i++) {
                    Document doc = hits.doc(i);
                    WorkflowInstance wInst = toWorkflowInstance(doc);
                    wInsts.add(wInst);
                }
            }

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception ignore) {
                }
                searcher = null;
            }
        }

        return wInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.AbstractPaginatibleInstanceRepository#paginateWorkflows(int,
     *      java.lang.String)
     */
    protected List paginateWorkflows(int pageNum, String status)
            throws InstanceRepositoryException {
        List instIds = null;
        IndexSearcher searcher = null;

        try {
            searcher = new IndexSearcher(idxFilePath);

            // construct a Boolean query here
            BooleanQuery booleanQuery = new BooleanQuery();

            Term instIdTerm = new Term("myfield", "myvalue");
            if (status != null) {
                Term statusTerm = new Term("workflow_inst_status", status);
                booleanQuery.add(new TermQuery(statusTerm),
                        BooleanClause.Occur.MUST);
            }
            booleanQuery.add(new TermQuery(instIdTerm),
                    BooleanClause.Occur.MUST);

            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.STRING, true));
            LOG.log(Level.FINE,
                    "Querying LuceneWorkflowInstanceRepository: q: ["
                            + booleanQuery + "]");
            Hits hits = searcher.search(booleanQuery, sort);
            if (hits.length() > 0) {

                int startNum = (pageNum - 1) * pageSize;
                if (startNum > hits.length()) {
                    startNum = 0;
                }

                instIds = new Vector(pageSize);

                for (int i = startNum; i < Math.min(hits.length(),
                        (startNum + pageSize)); i++) {
                    Document instDoc = hits.doc(i);
                    WorkflowInstance inst = toWorkflowInstance(instDoc);
                    instIds.add(inst.getId());

                }
            } else {
                LOG.log(Level.WARNING, "No workflow instances found "
                        + "when attempting to paginate!");
            }

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception ignore) {
                }
                searcher = null;
            }
        }

        return instIds;
    }

    private synchronized void removeWorkflowInstanceDocument(
            WorkflowInstance inst) throws InstanceRepositoryException {
        IndexReader reader = null;

        try {
            reader = IndexReader.open(idxFilePath);
            LOG.log(Level.FINE,
                    "LuceneWorkflowEngine: remove document from index for workflow instance: ["
                            + inst.getId() + "]");
            reader.deleteDocuments(new Term("workflow_inst_id", inst.getId()));
        } catch (IOException e) {
            LOG
                    .log(Level.WARNING,
                            "Exception removing workflow instance: ["
                                    + inst.getId() + "] from index: Message: "
                                    + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }

                reader = null;
            }

        }
    }

    private synchronized void addWorkflowInstanceToCatalog(
            WorkflowInstance wInst) throws InstanceRepositoryException {
        IndexWriter writer = null;

        File indexDir = new File(idxFilePath);

        boolean createIndex = false;

        if (indexDir.exists() && indexDir.isDirectory()) {
            createIndex = false;
        } else
            createIndex = true;

        try {
            writer = new IndexWriter(idxFilePath, new StandardAnalyzer(),
                    createIndex);
            writer.setMergeFactor(20);

            Document doc = toDoc(wInst);
            writer.addDocument(doc);
            writer.optimize();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to index workflow instance: ["
                    + wInst.getId() + "]: Message: " + e.getMessage());
            throw new InstanceRepositoryException(
                    "Unable to index workflow instance: [" + wInst.getId()
                            + "]: Message: " + e.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception ignore) {
            }
            writer = null;
        }

    }

    private Document toDoc(WorkflowInstance workflowInst) {
        Document doc = new Document();

        // store the workflow instance info first
        doc.add(new Field("workflow_inst_id", workflowInst.getId(),
                Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("workflow_inst_status", workflowInst.getStatus(),
                Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc
                .add(new Field("workflow_inst_current_task_id", workflowInst
                        .getCurrentTaskId(), Field.Store.YES,
                        Field.Index.UN_TOKENIZED));

        doc
                .add(new Field(
                        "workflow_inst_currenttask_startdatetime",
                        workflowInst.getCurrentTaskStartDateTimeIsoStr() != null ? workflowInst
                                .getCurrentTaskStartDateTimeIsoStr()
                                : "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("workflow_inst_currenttask_enddatetime", workflowInst
                .getCurrentTaskEndDateTimeIsoStr() != null ? workflowInst
                .getCurrentTaskEndDateTimeIsoStr() : "", Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(new Field("workflow_inst_startdatetime", workflowInst
                .getStartDateTimeIsoStr() != null ? workflowInst
                .getStartDateTimeIsoStr() : "", Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(new Field("workflow_inst_enddatetime", workflowInst
                .getEndDateTimeIsoStr() != null ? workflowInst
                .getEndDateTimeIsoStr() : "", Field.Store.YES,
                Field.Index.UN_TOKENIZED));

        // add all metadata
        addInstanceMetadataToDoc(doc, workflowInst.getSharedContext());

        // store the workflow info too
        doc.add(new Field("workflow_id", workflowInst.getWorkflow().getId(),
                Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("workflow_name",
                workflowInst.getWorkflow().getName(), Field.Store.YES,
                Field.Index.NO));

        // store the tasks
        addTasksToDoc(doc, workflowInst.getWorkflow().getTasks());

        // add the default field (so that we can do a query for *)
        doc.add(new Field("myfield", "myvalue", Field.Store.YES,
                Field.Index.UN_TOKENIZED));

        return doc;
    }

    private void addInstanceMetadataToDoc(Document doc, Metadata met) {
        if (met != null && met.getHashtable().keySet().size() > 0) {
            for (Iterator i = met.getHashtable().keySet().iterator(); i
                    .hasNext();) {
                String metKey = (String) i.next();
                List metVals = met.getAllMetadata(metKey);
                if (metVals != null && metVals.size() > 0) {
                    for (Iterator j = metVals.iterator(); j.hasNext();) {
                        String metVal = (String) j.next();
                        doc.add(new Field(metKey, metVal, Field.Store.YES,
                                Field.Index.UN_TOKENIZED));
                    }

                    // now index the field name so that we can use it to
                    // look it up when converting from doc to
                    // WorkflowInstance
                    doc.add(new Field("workflow_inst_met_flds", metKey,
                            Field.Store.YES, Field.Index.NO));

                }
            }
        }
    }

    private void addTasksToDoc(Document doc, List tasks) {
        if (tasks != null && tasks.size() > 0) {
            for (Iterator i = tasks.iterator(); i.hasNext();) {
                WorkflowTask task = (WorkflowTask) i.next();
                doc.add(new Field("task_id", task.getTaskId(), Field.Store.YES,
                        Field.Index.UN_TOKENIZED));
                doc.add(new Field("task_name", task.getTaskName(),
                        Field.Store.YES, Field.Index.NO));
                doc.add(new Field("task_order",
                        String.valueOf(task.getOrder()), Field.Store.YES,
                        Field.Index.NO));
                doc.add(new Field("task_class",
                        task.getTaskInstanceClassName(), Field.Store.YES,
                        Field.Index.NO));

                addConditionsToDoc(task.getTaskId(), task.getConditions(), doc);
                addTaskConfigToDoc(task.getTaskId(), task.getTaskConfig(), doc);
            }
        }
    }

    private void addTaskConfigToDoc(String taskId,
            WorkflowTaskConfiguration config, Document doc) {
        if (config != null) {
            for (Iterator i = config.getProperties().keySet().iterator(); i
                    .hasNext();) {
                String propName = (String) i.next();
                String propValue = config.getProperty(propName);

                doc.add(new Field(taskId + "_config_property_name", propName,
                        Field.Store.YES, Field.Index.NO));
                doc.add(new Field(taskId + "_config_property_value", propValue,
                        Field.Store.YES, Field.Index.NO));
            }
        }
    }

    private void addConditionsToDoc(String taskId, List conditionList,
            Document doc) {
        if (conditionList != null && conditionList.size() > 0) {
            for (Iterator i = conditionList.iterator(); i.hasNext();) {
                WorkflowCondition cond = (WorkflowCondition) i.next();
                doc.add(new Field(taskId + "_condition_name", cond
                        .getConditionName(), Field.Store.YES, Field.Index.NO));
                doc.add(new Field(taskId + "_condition_id", cond
                        .getConditionId(), Field.Store.YES,
                        Field.Index.UN_TOKENIZED));
                doc.add(new Field(taskId + "_condition_class", cond
                        .getConditionInstanceClassName(), Field.Store.YES,
                        Field.Index.NO));
                doc.add(new Field(taskId + "_condition_order", String
                        .valueOf(cond.getOrder()), Field.Store.YES,
                        Field.Index.NO));
            }
        }
    }

    private WorkflowInstance toWorkflowInstance(Document doc) {
        WorkflowInstance inst = new WorkflowInstance();

        // first read all the instance info
        inst.setId(doc.get("workflow_inst_id"));
        inst.setStatus(doc.get("workflow_inst_status"));
        inst.setCurrentTaskId(doc.get("workflow_inst_current_task_id"));
        inst.setCurrentTaskStartDateTimeIsoStr(doc
                .get("workflow_inst_currenttask_startdatetime"));
        inst.setCurrentTaskEndDateTimeIsoStr(doc
                .get("workflow_inst_currenttask_enddatetime"));
        inst.setStartDateTimeIsoStr(doc.get("workflow_inst_startdatetime"));
        inst.setEndDateTimeIsoStr(doc.get("workflow_inst_enddatetime"));

        // read the workflow instance metadata
        Metadata sharedContext = new Metadata();
        String[] instMetFields = doc.getValues("workflow_inst_met_flds");
        if (instMetFields != null && instMetFields.length > 0) {
            for (int i = 0; i < instMetFields.length; i++) {
                String fldName = instMetFields[i];
                String[] vals = doc.getValues(fldName);
                if (vals != null && vals.length > 0) {
                    for (int j = 0; j < vals.length; j++) {
                        sharedContext.addMetadata(fldName, vals[j]);
                    }
                }
            }
        }

        inst.setSharedContext(sharedContext);

        // now read all of the workflow info

        Workflow workflow = new Workflow();

        workflow.setId(doc.get("workflow_id"));
        workflow.setName(doc.get("workflow_name"));
        workflow.setTasks(toTasks(doc));

        inst.setWorkflow(workflow);

        return inst;
    }

    private List toTasks(Document doc) {
        List taskList = new Vector();

        String[] taskIds = doc.getValues("task_id");
        String[] taskNames = doc.getValues("task_name");
        String[] taskOrders = doc.getValues("task_order");
        String[] taskClasses = doc.getValues("task_class");

        if (taskIds.length != taskNames.length
                || taskIds.length != taskOrders.length
                || taskIds.length != taskClasses.length) {
            LOG.log(Level.WARNING,
                    "task arrays are not of same size when rebuilding "
                            + "task list from Document!");
            return null;
        }

        for (int i = 0; i < taskIds.length; i++) {
            WorkflowTask task = new WorkflowTask();
            task.setOrder(Integer.parseInt(taskOrders[i]));
            task.setTaskName(taskNames[i]);
            task.setTaskId(taskIds[i]);
            task.setTaskInstanceClassName(taskClasses[i]);

            task.setConditions(toConditions(task.getTaskId(), doc));
            task.setTaskConfig(toTaskConfig(task.getTaskId(), doc));
            taskList.add(task);
        }

        return taskList;
    }

    private WorkflowTaskConfiguration toTaskConfig(String taskId, Document doc) {
        WorkflowTaskConfiguration taskConfig = new WorkflowTaskConfiguration();

        String[] propNames = doc.getValues(taskId + "_config_property_name");
        String[] propValues = doc.getValues(taskId + "_config_property_value");

        if (propNames == null) {
            return taskConfig;
        }

        if (propNames.length != propValues.length) {
            LOG.log(Level.WARNING,
                    "Task Config prop name and value arrays are not "
                            + "of same size!");
            return null;
        }

        for (int i = 0; i < propNames.length; i++) {
            taskConfig.addConfigProperty(propNames[i], propValues[i]);
        }

        return taskConfig;
    }

    private List toConditions(String taskId, Document doc) {
        List condList = new Vector();

        String[] condNames = doc.getValues(taskId + "_condition_name");
        String[] condClasses = doc.getValues(taskId + "_condition_class");
        String[] condOrders = doc.getValues(taskId + "_condition_order");
        String[] condIds = doc.getValues(taskId + "_condition_id");

        if (condNames == null) {
            return condList;
        }

        if (condNames.length != condClasses.length
                || condNames.length != condOrders.length
                || condNames.length != condIds.length) {
            LOG.log(Level.WARNING,
                    "Condition arrays are not of same size when "
                            + "rebuilding from given Document");
            return null;
        }

        for (int i = 0; i < condNames.length; i++) {
            WorkflowCondition cond = new WorkflowCondition();
            cond.setConditionId(condIds[i]);
            cond.setConditionInstanceClassName(condClasses[i]);
            cond.setConditionName(condNames[i]);
            cond.setOrder(Integer.parseInt(condOrders[i]));
        }

        return condList;
    }

}
