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


package org.apache.oodt.cas.workflow.examples;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>A simple condition that always returns true when invoked.</p>
 */
public class TrueCondition implements WorkflowConditionInstance {

	/**
	 * 
	 */
	public TrueCondition() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance#evaluate(org.apache.oodt.cas.metadata.Metadata)
	 */
    public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		return true;
	}

}
