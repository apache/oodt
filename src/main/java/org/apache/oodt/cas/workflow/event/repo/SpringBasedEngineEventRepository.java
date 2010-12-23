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
package org.apache.oodt.cas.workflow.event.repo;

//JDK imports
import java.util.List;
import java.util.Map;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.event.WorkflowEngineEvent;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 *	Spring based Event repo - READ ONLY
 *	
 */
public class SpringBasedEngineEventRepository implements
		WorkflowEngineEventRepository {

	private Map<String, WorkflowEngineEvent> events;
	
	public SpringBasedEngineEventRepository(String beanRepo) {
		events = new FileSystemXmlApplicationContext(new String[] { beanRepo }).getBeansOfType(WorkflowEngineEvent.class);
	}
	
	public WorkflowEngineEvent getEventById(String id) throws Exception {
		return events.get(id);
	}

	public List<String> getEventIds() throws Exception {
		return new Vector<String>(events.keySet());
	}

	public void storeEvent(WorkflowEngineEvent event) throws Exception {
		throw new Exception("Modification not allowed during runtime");
	}

}
