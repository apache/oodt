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
package org.apache.oodt.cas.workflow.precondition;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowEngine;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Event/Action Precondtion (NOT WORKFLOW PRECONDITIONS) which ensures all WorkflowProcessors are cached
 * </p>.
 */
public class EnsureServerFullyLoaded extends WorkflowPreCondition {

	private static final Logger LOG = Logger.getLogger(EnsureServerFullyLoaded.class.getName());
	
	public boolean passes(WorkflowEngine engine) {
		try {
			return engine.getNumOfWorkflows() == engine.getNumOfLoadedProcessors();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to check precondition '" + this.getId() + "' : " + e.getMessage(), e);
			return false;
		}
	}

}
