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


package org.apache.oodt.cas.workflow.structs;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * The actual evaluation method for the condition should be defined in any class
 * that implements this interface.
 * </p>
 */
public interface WorkflowConditionInstance {

	/**
	 * <p>
	 * The actual conditional: this method should return <code>true</code> if
	 * the condition is satisfied, otherwise, false.
	 * </p>
	 * 
	 * @param metadata
	 *            Any metadata needed by the conditional to determine
	 *            satisfaction.
	 * @return true if the condition is satisfied, otherwise, false.
	 */
	boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config);

}
