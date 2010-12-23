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
package org.apache.oodt.cas.workflow.processor.repo;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Stores WorkflowProcessors for later retrieval
 * </p>.
 */
public abstract class WorkflowProcessorRepository {

	/**
	 * Takes a WorkflowProcessor and indexes it on its 
	 * instance ID. If store in memory only, then at least 
	 * the WorkflowProcessor should be cloned. Multiple 
	 * calls to this method for the same WorkflowProcessor 
	 * should be treated as a replace method.
	 * @param workflowProcessor The WorkflowProcessor to be saved
	 */
	public abstract void store(WorkflowProcessor workflowProcessor) throws Exception;	

	public abstract void delete(String instanceId) throws Exception;	

	public abstract WorkflowProcessor load(String instanceId) throws Exception;

	public abstract List<String> getStoredInstanceIds() throws Exception;
	
}
