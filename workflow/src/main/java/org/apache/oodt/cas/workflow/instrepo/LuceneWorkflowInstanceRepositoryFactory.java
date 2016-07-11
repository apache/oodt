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

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

//OODT imports
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.oodt.cas.metadata.util.PathUtils;

//Lucene imports
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating {@link LuceneWorkflowEngine}s.
 * </p>.
 */
public class LuceneWorkflowInstanceRepositoryFactory implements
        WorkflowInstanceRepositoryFactory {

  public static final int VAL = 20;
  /* the path to the lucene index directory */
    private String indexFilePath = null;

    private int pageSize = -1;
    
	/* our log stream */
    private static final Logger LOG = Logger.getLogger(LuceneWorkflowInstanceRepositoryFactory.class.getName());

    public LuceneWorkflowInstanceRepositoryFactory()
            throws InstantiationException {
        indexFilePath = System
                .getProperty("org.apache.oodt.cas.workflow.instanceRep.lucene.idxPath");

        if (indexFilePath == null) {
            throw new InstantiationException(
                    "Index File property: [org.apache.oodt.cas.workflow."
                            + "engine.lucene.idxPath] not set!");
        }

        // do env variable replacement
        indexFilePath = PathUtils.replaceEnvVariables(indexFilePath);
        pageSize = Integer.getInteger(
            "org.apache.oodt.cas.workflow.instanceRep.pageSize", VAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory#createInstanceRepository()
     */
    public WorkflowInstanceRepository createInstanceRepository() {
		Directory indexDir = null;
		try {
			indexDir = FSDirectory.open(new File( indexFilePath ).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	    // Create the index if it does not already exist
	    IndexWriter writer = null;
		try {
				IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

				config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
				LogMergePolicy lmp =new LogDocMergePolicy();
				config.setMergePolicy(lmp);

				writer = new IndexWriter(indexDir, config);
	        } catch (Exception e) {
	            LOG.severe("Unable to create index: " + e.getMessage());
	        } finally {
	            if (writer != null) {
	                try {
	                    writer.close();
	                } catch (Exception e) {
	                    LOG.severe("Unable to close index: " + e.getMessage());
	                }
	            }
	        }

        return new LuceneWorkflowInstanceRepository(indexFilePath, pageSize);
    }

}
