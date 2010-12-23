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
package org.apache.oodt.cas.workflow.server.action;

//JDK imports
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for trigger an event on the server workflow engine
 * <p>
 */
public class TriggerEvent extends WorkflowEngineServerAction {
	
	private static final Logger LOG = Logger.getLogger(TriggerEvent.class.getName());

	private String eventId;
	private Metadata inputMetadata;
	
	public TriggerEvent() {
		this.inputMetadata = new Metadata();
	}
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		weClient.triggerEvent(this.eventId, this.inputMetadata);
		LOG.log(Level.INFO, "Successfully triggered event '" + eventId + "' with input metadata '" + this.inputMetadata.getHashtable() + "'");
	}
	
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void replaceInputMetadata(List<String> keyValues) {
		this.inputMetadata.replaceMetadata(keyValues.get(0), keyValues.subList(1, keyValues.size()));
	}
	
}
