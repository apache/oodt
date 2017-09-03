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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleStage;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


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
    Directory indexDir = null;
    private DirectoryReader reader;
    /* the path to the index directory for this catalog */

    public static final int MERGE_FACTOR = 20;
    /* path to lucene index directory to store wInst info */
    private String idxFilePath = null;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(LuceneWorkflowInstanceRepository.class.getName());

    /* our workflow inst id generator */
    private static UUIDGenerator generator = UUIDGenerator.getInstance();
    private int mergeFactor = 20;

    /**
     * 
     */
    public LuceneWorkflowInstanceRepository(String idxPath, int pageSize) {
        this.idxFilePath = idxPath;
        this.pageSize = pageSize;
        try {
            indexDir = FSDirectory.open(new File( idxFilePath ).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);
            Term instIdTerm = new Term("myfield", "myvalue");
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.Type.STRING, true));
            TopDocs topDocs = searcher.search(query, 1, sort);

            numInsts = topDocs.totalHits;

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    //TODO Shutdown searcher
                } catch (Exception ignore) {
                }
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
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);
            Term instIdTerm = new Term("workflow_inst_status", status);
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.Type.STRING, true));
            TopDocs topDocs = searcher.search(query, 1, sort);

            numInsts = topDocs.totalHits;

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: [" + idxFilePath
                            + "] for search: Message: " + e.getMessage());
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    //TODO Shutdown searcher
                } catch (Exception ignore) {
                }
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
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);
            Term instIdTerm = new Term("workflow_inst_id", workflowInstId);
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            TopDocs check = searcher.search(query, 1);

            if (check.totalHits != 1) {
                LOG.log(Level.WARNING, "The workflow instance: ["
                        + workflowInstId + "] is not being "
                        + "managed by this " + "workflow engine, or "
                        + "is not unique in the catalog: num hits: ["+check.totalHits+"]");
                return null;
            } else {
                TopDocs topDocs = searcher.search(query, check.totalHits);
                ScoreDoc[] hits = topDocs.scoreDocs;
                Document instDoc = searcher.doc(hits[0].doc);
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
                    //TODO Shutdown searcher
                } catch (Exception ignore) {
                }
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
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);
            Term instIdTerm = new Term("myfield", "myvalue");
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.Type.STRING, true));
            TopDocs check = searcher.search(query, 1, sort);
            if(check.totalHits>0) {
                TopDocs topDocs = searcher.search(query, check.totalHits, sort);
                ScoreDoc[] hits = topDocs.scoreDocs;
                if (topDocs.totalHits > 0) {
                    wInsts = new Vector(hits.length);

                    for (ScoreDoc hit : hits) {
                        Document doc = searcher.doc(hit.doc);
                        WorkflowInstance wInst = toWorkflowInstance(doc);
                        wInsts.add(wInst);
                    }
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
                    //TODO Shutdown searcher
                } catch (Exception ignore) {
                }
            }
        }

        return wInsts;
    }
    
    @Override
    public synchronized boolean clearWorkflowInstances() throws InstanceRepositoryException {
      IndexWriter writer = null;
      try {
          IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
          config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
          LogMergePolicy lmp =new LogDocMergePolicy();
          lmp.setMergeFactor(mergeFactor);
          config.setMergePolicy(lmp);

          writer = new IndexWriter(indexDir, config);
          LOG.log(Level.FINE,
                  "LuceneWorkflowEngine: remove all workflow instances");
          writer.deleteDocuments(new Term("myfield", "myvalue"));
      } catch (IOException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          LOG
                  .log(Level.WARNING,
                          "Exception removing workflow instances from index: Message: "
                                  + e.getMessage());
          throw new InstanceRepositoryException(e.getMessage());
      } finally {
        if (writer != null){
          try{
            writer.close();
          }
          catch(Exception ignore){}
          
          writer = null;
        }

      }
      
      return true;
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
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);
            Term instIdTerm = new Term("workflow_inst_status", status);
            org.apache.lucene.search.Query query = new TermQuery(instIdTerm);
            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.Type.STRING, true));
            TopDocs check = searcher.search(query, 1, sort);
            if(check.totalHits>0) {
                TopDocs topDocs = searcher.search(query, check.totalHits, sort);
                ScoreDoc[] hits = topDocs.scoreDocs;
                if (hits.length > 0) {
                    wInsts = new Vector(hits.length);

                    for (ScoreDoc hit : hits) {
                        Document doc = searcher.doc(hit.doc);
                        WorkflowInstance wInst = toWorkflowInstance(doc);
                        wInsts.add(wInst);
                    }
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
                    //TODO Shutdown searcher
                } catch (Exception ignore) {
                }
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
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);

            // construct a Boolean query here
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

            Term instIdTerm = new Term("myfield", "myvalue");
            if (status != null) {
                Term statusTerm = new Term("workflow_inst_status", status);
                booleanQuery.add(new TermQuery(statusTerm),
                        BooleanClause.Occur.MUST);
            }
            booleanQuery.add(new TermQuery(instIdTerm),
                    BooleanClause.Occur.MUST);

            Sort sort = new Sort(new SortField("workflow_inst_startdatetime",
                    SortField.Type.STRING, true));
            LOG.log(Level.FINE,
                    "Querying LuceneWorkflowInstanceRepository: q: ["
                            + booleanQuery + "]");
            TopDocs check = searcher.search(booleanQuery.build(), 1, sort);
            if(check.totalHits>0) {
                TopDocs topDocs = searcher.search(booleanQuery.build(), check.totalHits, sort);
                ScoreDoc[] hits = topDocs.scoreDocs;

                if (hits.length > 0) {

                    int startNum = (pageNum - 1) * pageSize;
                    if (startNum > hits.length) {
                        startNum = 0;
                    }

                    instIds = new Vector(pageSize);

                    for (int i = startNum; i < Math.min(hits.length,
                            (startNum + pageSize)); i++) {
                        Document instDoc = searcher.doc(hits[i].doc);
                        WorkflowInstance inst = toWorkflowInstance(instDoc);
                        instIds.add(inst.getId());

                    }
                } else {
                    LOG.log(Level.WARNING, "No workflow instances found "
                            + "when attempting to paginate!");
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
                    //TODO Shutdown searcher
                } catch (Exception ignore) {
                }
            }
        }

        return instIds;
    }

    private synchronized void removeWorkflowInstanceDocument(
            WorkflowInstance inst) throws InstanceRepositoryException {
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader = DirectoryReader.open(indexDir);
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            LogMergePolicy lmp =new LogDocMergePolicy();
            lmp.setMergeFactor(mergeFactor);
            config.setMergePolicy(lmp);

            IndexWriter writer = new IndexWriter(indexDir, config);
            LOG.log(Level.FINE,
                    "LuceneWorkflowEngine: remove document from index for workflow instance: ["
                            + inst.getId() + "]");
            writer.deleteDocuments(new Term("workflow_inst_id", inst.getId()));
            writer.close();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
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

            }

        }
    }

    private synchronized void addWorkflowInstanceToCatalog(
            WorkflowInstance wInst) throws InstanceRepositoryException {
        IndexWriter writer = null;

        try {
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            LogMergePolicy lmp =new LogDocMergePolicy();
            lmp.setMergeFactor(mergeFactor);
            config.setMergePolicy(lmp);

            writer = new IndexWriter(indexDir, config);
            Document doc = toDoc(wInst);
            writer.addDocument(doc);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to index workflow instance: ["
                    + wInst.getId() + "]: Message: " + e.getMessage());
            throw new InstanceRepositoryException(
                    "Unable to index workflow instance: [" + wInst.getId()
                            + "]: Message: " + e.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    private Document toDoc(WorkflowInstance workflowInst) {
        Document doc = new Document();

        // store the workflow instance info first
        doc.add(new Field("workflow_inst_id", workflowInst.getId(),
                StringField.TYPE_STORED));
        
        doc.add(new Field("workflow_inst_timesblocked", 
            String.valueOf(workflowInst.getTimesBlocked()), StringField.TYPE_STORED));
        
        // will leave this for back compat, but will also store 
        // category 
        doc.add(new Field("workflow_inst_status", workflowInst.getStatus(),
                StringField.TYPE_STORED));
        
        if(workflowInst.getState() != null){
          WorkflowState state = workflowInst.getState();
        
          if(state.getDescription() != null){
            doc.add(new Field("workflow_inst_state_desc",
                state.getDescription(), StringField.TYPE_STORED));
          }
          
          if(state.getMessage() != null){
            doc.add(new Field("workflow_inst_state_message",
                state.getMessage(), StringField.TYPE_STORED));
          }
          
          if(state.getCategory() != null && state.getCategory().getName() != null){
            doc.add(new Field("workflow_inst_state_category",
                state.getCategory().getName(), StringField.TYPE_STORED));
          }
        }        
        
        doc
                .add(new Field("workflow_inst_current_task_id", workflowInst
                        .getCurrentTaskId(), StringField.TYPE_STORED));

        doc
                .add(new Field(
                        "workflow_inst_currenttask_startdatetime",
                        workflowInst.getCurrentTaskStartDateTimeIsoStr() != null ? workflowInst
                                .getCurrentTaskStartDateTimeIsoStr()
                                : "", StringField.TYPE_STORED));

        doc.add(new SortedDocValuesField("workflow_inst_currenttask_startdatetime", new BytesRef(workflowInst.getCurrentTaskStartDateTimeIsoStr() != null ? workflowInst
                .getCurrentTaskStartDateTimeIsoStr()
                : "")));

        doc.add(new Field("workflow_inst_currenttask_enddatetime", workflowInst
                .getCurrentTaskEndDateTimeIsoStr() != null ? workflowInst
                .getCurrentTaskEndDateTimeIsoStr() : "", StringField.TYPE_STORED));
        doc.add(new SortedDocValuesField("workflow_inst_currenttask_enddatetime", new BytesRef(workflowInst
                .getCurrentTaskEndDateTimeIsoStr() != null ? workflowInst
                .getCurrentTaskEndDateTimeIsoStr() : "")));

        doc.add(new Field("workflow_inst_startdatetime", workflowInst
                .getStartDateTimeIsoStr() != null ? workflowInst
                .getStartDateTimeIsoStr() : "", StringField.TYPE_STORED));
        doc.add(new SortedDocValuesField("workflow_inst_startdatetime", new BytesRef(workflowInst
                .getStartDateTimeIsoStr() != null ? workflowInst
                .getStartDateTimeIsoStr() : "")));

        doc.add(new Field("workflow_inst_enddatetime", workflowInst
                .getEndDateTimeIsoStr() != null ? workflowInst
                .getEndDateTimeIsoStr() : "", StringField.TYPE_STORED));
        doc.add(new SortedDocValuesField("workflow_inst_enddatetime", new BytesRef(workflowInst
                .getEndDateTimeIsoStr() != null ? workflowInst
                .getEndDateTimeIsoStr() : "")));

        doc.add(new Field("workflow_inst_priority",
            workflowInst.getPriority() != null ? 
                String.valueOf(workflowInst.getPriority().getValue()):
                  String.valueOf(Priority.getDefault().getValue()),
                StringField.TYPE_STORED));

        // add all metadata
        addInstanceMetadataToDoc(doc, workflowInst.getSharedContext());

        // store the workflow info too
        doc.add(new Field("workflow_id", workflowInst.getWorkflow().getId(),
                StringField.TYPE_STORED));
        doc.add(new Field("workflow_name",
                workflowInst.getWorkflow().getName(), StringField.TYPE_STORED));

        // store the tasks
        addTasksToDoc(doc, workflowInst.getWorkflow().getTasks());
        
        // store workflow conditions
        addConditionsToDoc("workflow_condition_"+workflowInst.getWorkflow().getId(), 
            workflowInst.getWorkflow().getConditions()
            , doc);

        // add the default field (so that we can do a query for *)
        doc.add(new Field("myfield", "myvalue", StringField.TYPE_STORED));

        return doc;
    }

    private void addInstanceMetadataToDoc(Document doc, Metadata met) {
        if (met != null && met.getMap().keySet().size() > 0) {
            for (String metKey : met.getMap().keySet()) {
                List metVals = met.getAllMetadata(metKey);
                if (metVals != null && metVals.size() > 0) {
                    for (Object metVal1 : metVals) {
                        String metVal = (String) metVal1;
                        doc.add(new Field(metKey, metVal, StringField.TYPE_STORED));
                    }

                    // now index the field name so that we can use it to
                    // look it up when converting from doc to
                    // WorkflowInstance
                    doc.add(new Field("workflow_inst_met_flds", metKey,
                            StringField.TYPE_STORED));

                }
            }
        }
    }

    private void addTasksToDoc(Document doc, List tasks) {
        if (tasks != null && tasks.size() > 0) {
            for (Object task1 : tasks) {
                WorkflowTask task = (WorkflowTask) task1;
                doc.add(new Field("task_id", task.getTaskId(), StringField.TYPE_STORED));
                doc.add(new Field("task_name", task.getTaskName(),
                        StringField.TYPE_STORED));
                doc.add(new Field("task_order",
                    String.valueOf(task.getOrder()), StringField.TYPE_STORED));
                doc.add(new Field("task_class",
                    task.getTaskInstanceClassName(), StringField.TYPE_STORED));

                addConditionsToDoc(task.getTaskId(), task.getConditions(), doc);
                addTaskConfigToDoc(task.getTaskId(), task.getTaskConfig(), doc);
            }
        }
    }

    private void addTaskConfigToDoc(String taskId,
            WorkflowTaskConfiguration config, Document doc) {
        if (config != null) {
            for (Object o : config.getProperties().keySet()) {
                String propName = (String) o;
                String propValue = config.getProperty(propName);

                doc.add(new Field(taskId + "_config_property_name", propName,
                        StringField.TYPE_STORED));
                doc.add(new Field(taskId + "_config_property_value", propValue,
                        StringField.TYPE_STORED));
            }
        }
    }

  private void addConditionsToDoc(String taskId, List conditionList,
      Document doc) {
    if (conditionList != null && conditionList.size() > 0) {
        for (Object aConditionList : conditionList) {
            WorkflowCondition cond = (WorkflowCondition) aConditionList;
            doc.add(new Field(taskId + "_condition_name", cond.getConditionName(),
                    StringField.TYPE_STORED));
            doc.add(new Field(taskId + "_condition_id", cond.getConditionId(),
                    StringField.TYPE_STORED));
            doc.add(new Field(taskId + "_condition_class", cond
                .getConditionInstanceClassName(),StringField.TYPE_STORED));
            doc.add(new Field(taskId + "_condition_order", String.valueOf(cond
                .getOrder()), StringField.TYPE_STORED));
            doc.add(new Field(taskId + "_condition_timeout", String.valueOf(cond
                .getTimeoutSeconds()), StringField.TYPE_STORED));
            doc.add(new Field(taskId + "_condition_optional", String.valueOf(cond.isOptional()),
                    StringField.TYPE_STORED));
        }
    }
  }

    private WorkflowInstance toWorkflowInstance(Document doc) {
        WorkflowInstance inst = new WorkflowInstance();

        // first read all the instance info
        inst.setId(doc.get("workflow_inst_id"));
        
        inst.setTimesBlocked(Integer.parseInt(doc.get("workflow_inst_timesblocked") != 
          null ? doc.get("workflow_inst_timesblocked"):"0"));
        
        // try and construct a state
        WorkflowState state = new WorkflowState();
        state.setName(doc.get("workflow_inst_status"));
        if(doc.get("workflow_inst_state_category") != null){
          WorkflowLifecycleStage category = new WorkflowLifecycleStage();
          category.setName(doc.get("workflow_inst_state_category"));
          state.setCategory(category);
        }
        
        if(doc.get("workflow_inst_state_desc") != null){
          state.setDescription(doc.get("workflow_inst_state_desc"));
        }
        
        if(doc.get("workflow_inst_state_message") != null){
          state.setMessage(doc.get("workflow_inst_state_message"));
        }        
        inst.setState(state);
        inst.setCurrentTaskId(doc.get("workflow_inst_current_task_id"));
        inst.setCurrentTaskStartDateTimeIsoStr(doc
                .get("workflow_inst_currenttask_startdatetime"));
        inst.setCurrentTaskEndDateTimeIsoStr(doc
                .get("workflow_inst_currenttask_enddatetime"));
        inst.setStartDateTimeIsoStr(doc.get("workflow_inst_startdatetime"));
        inst.setEndDateTimeIsoStr(doc.get("workflow_inst_enddatetime"));
        inst.setPriority(Priority.getPriority(doc.get("workflow_inst_priority") != null ? 
            Double.valueOf(doc.get("workflow_inst_priority")):Priority.getDefault().getValue()));

        // read the workflow instance metadata
        Metadata sharedContext = new Metadata();
        String[] instMetFields = doc.getValues("workflow_inst_met_flds");
        if (instMetFields != null && instMetFields.length > 0) {
            for (String fldName : instMetFields) {
                String[] vals = doc.getValues(fldName);
                if (vals != null && vals.length > 0) {
                    for (String val : vals) {
                        sharedContext.addMetadata(fldName, val);
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
        workflow.setConditions(toConditions("workflow_condition_"+workflow.getId(), doc));

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
        String[] condTimeouts = doc.getValues(taskId+"_condition_timeout");
        String[] condOptionals = doc.getValues(taskId+"_condition_optional");

        if (condNames == null) {
            return condList;
        }
        
        if (condNames.length != condClasses.length
                || condNames.length != condOrders.length
                || condNames.length != condIds.length 
                || (condTimeouts != null && condNames.length != condTimeouts.length)
                || (condOptionals != null && condNames.length != condOptionals.length)) {
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
            if(condTimeouts != null){
              cond.setTimeoutSeconds(Long.parseLong(condTimeouts[i]));
            }
            if(condOptionals != null){
              cond.setOptional(Boolean.valueOf(condOptionals[i]));
            }
            condList.add(cond);
        }
        
        return condList;
    }

}
