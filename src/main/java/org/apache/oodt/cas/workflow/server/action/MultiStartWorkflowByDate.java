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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;

/**
 * 
 * @author bfoster
 *
 */
public class MultiStartWorkflowByDate extends StartWorkflow {

	private static final Logger LOG = Logger.getLogger(MultiStartWorkflowByDate.class.getName());

	protected String startDateString;
	protected String endDateString;
	protected String dateFormatString;
	protected int incrementMinutes;
	protected List<String> dateMetadataKeys;
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
	    GregorianCalendar startDate = new GregorianCalendar();
	    startDate.setTime(dateFormat.parse(startDateString));
	    GregorianCalendar endDate = new GregorianCalendar();
	    endDate.setTime(dateFormat.parse(endDateString));
	    
	    while (startDate.getTimeInMillis() < endDate.getTimeInMillis()) {
	    	for (String dateMetadataKey : this.dateMetadataKeys)
	    		this.replaceInputMetadata(Arrays.asList(dateMetadataKey, dateFormat.format(startDate.getTime())));
			LOG.log(Level.INFO, "Starting workflow for date '" + dateFormat.format(startDate.getTime()) + "'");	
	    	super.performAction(weClient);
	    	startDate.add(Calendar.MINUTE, this.incrementMinutes);
	    }
	}
	
	public void setDateMetadataKeys(List<String> dateMetadataKeys) {
		this.dateMetadataKeys = dateMetadataKeys;
	}
	
	public void setStartDate(String startDate) {
		this.startDateString = startDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDateString = endDate;
	}
	
	public void setDateFormat(String dateFormat) {
		this.dateFormatString = dateFormat;
	}
	
	public void setIncrementMinutes(int incrementMinutes) {
		this.incrementMinutes = incrementMinutes;
	}

}
