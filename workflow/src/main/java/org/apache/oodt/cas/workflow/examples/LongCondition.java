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
 * @version $Revision$
 *
 * <p>A Simple condition that evaluates to false as many times as specified
 * by the dynamic metadata parameter "numFalse". After that, the condition returns
 * true.</p>
 *
 */
public class LongCondition implements WorkflowConditionInstance {

	private int timesFalse = 0;
	
	/**
	 * 
	 */
	public LongCondition() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance#evaluate(org.apache.oodt.cas.metadata.Metadata)
	 */
    public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		//simulate that this condition takes a passed in amount of seconds to wait for
		
		int numFalse = (String)metadata.getMetadata("numFalse") != null ? Integer.parseInt((String)metadata.getMetadata("numFalse")):5;
		System.out.println("Condition: Num false: "+numFalse);
		
		if(timesFalse < numFalse){
			timesFalse++;
			return false;			
		}
		else {
		  return true;
		}
	}

}
