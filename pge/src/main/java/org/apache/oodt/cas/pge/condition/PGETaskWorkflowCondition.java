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
package org.apache.oodt.cas.pge.condition;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.metadata.PGETaskMetKeys;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.conv.VersionConverter;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.util.QueryUtils;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.processor.ProcessorInfo;
import org.apache.oodt.cas.workflow.state.results.ResultsBailState;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;

//JDK imports
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public class PGETaskWorkflowCondition extends TaskInstance implements PGETaskMetKeys {
    
    private static Logger LOG = Logger.getLogger(PGETaskWorkflowCondition.class.getName());
    
    public static enum QueryType { START, END, START_INCL, END_INCL, TERM };
    
	public static final String ALL_PRODUCT_TYPES_GROUP = "AllProductTypes";
	
	public static final String ALL_PGES_GROUP = "AllPGEs";
	
	@Override
	protected ResultsState performExecution(ControlMetadata ctrlMetadata) {
        try {        	
        	Metadata metadata = ctrlMetadata.asMetadata();
        	
        	String filemgrUrl = PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(QUERY_FILE_MANAGER_URL), metadata);

            // query metadata keys
            String filterAlgor = ctrlMetadata.getMetadata(FILTER_ALGOR) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(FILTER_ALGOR), metadata) : null;
            String sortByKey = ctrlMetadata.getMetadata(SORY_BY_KEY) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(SORY_BY_KEY), metadata) : null;
            String epsilon = ctrlMetadata.getMetadata(EPSILON_IN_MILLIS) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(EPSILON_IN_MILLIS), metadata) : null;
            String versionConverter = ctrlMetadata.getMetadata(VERSION_CONVERTER) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(VERSION_CONVERTER), metadata) : null;
            String productTypes = ctrlMetadata.getMetadata(PRODUCT_TYPES) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(PRODUCT_TYPES), metadata) : null;
            String resultKeyFormats = ctrlMetadata.getMetadata(RESULT_KEY_FORMATS) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(RESULT_KEY_FORMATS), metadata) : null;
        	String startDateTimeKey = ctrlMetadata.getMetadata(START_DATE_TIME_KEY) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(START_DATE_TIME_KEY), metadata) : null;
        	String endDateTimeKey = ctrlMetadata.getMetadata(END_DATE_TIME_KEY) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(END_DATE_TIME_KEY), metadata) : null;
            String sqlQueryKey = ctrlMetadata.getMetadata(SQL_QUERY_KEY) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(SQL_QUERY_KEY), metadata) : null;
            String versioningKey = ctrlMetadata.getMetadata(VERSIONING_KEY) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(VERSIONING_KEY), metadata) : null;

            // load metadata from property adders
//            this.update(new ExecutingState("Loading Condition Properties Adders"), ctrlMetadata);
            String workflowConditionPropAddersString = ctrlMetadata.getMetadata(PROP_ADDERS) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(PROP_ADDERS), metadata) : null;
            if (workflowConditionPropAddersString != null) {
	            String[] workflowConditionPropAdders = workflowConditionPropAddersString.split(",");
	            for (String workflowConditionPropAdder : workflowConditionPropAdders) {
	            	try {
	            		workflowConditionPropAdder = workflowConditionPropAdder.trim();
	            		LOG.log(Level.INFO, "Running WorkflowConditionPropAdder '" + workflowConditionPropAdder + "'");
	            		((WorkflowConditionPropAdder) Class.forName(workflowConditionPropAdder).newInstance()).addMetadata(ctrlMetadata);
	            	}catch (Exception e) {
	            		LOG.log(Level.WARNING, "Failed to run WorkflowConditionPropAdder '" + workflowConditionPropAdder + "' : " + e.getMessage(), e);
	                    return new ResultsBailState("Precondition Bailed : Failed to run WorkflowConditionPropAdder '" + workflowConditionPropAdder + "' : " + e.getMessage());
	            	}
	            }
            }
                       
            // create SQL query
//            this.update(new ExecutingState("Building Query"), ctrlMetadata);
            ComplexQuery complexQuery = new ComplexQuery();
            complexQuery.setReducedProductTypeNames(Collections.unmodifiableList(Arrays.asList(productTypes.split(","))));
            if (sortByKey != null)
            	complexQuery.setSortByMetKey(sortByKey);
            
            if (filterAlgor != null) {
            	complexQuery.setQueryFilter(new QueryFilter(startDateTimeKey, endDateTimeKey, versioningKey, (FilterAlgor) Class.forName(filterAlgor).newInstance()));
            	if (epsilon != null)
            		complexQuery.getQueryFilter().getFilterAlgor().setEpsilon(Integer.parseInt(epsilon));
            	if (versionConverter != null)
            		complexQuery.getQueryFilter().setConverter((VersionConverter) Class.forName(versionConverter).newInstance());
            }
            
            List<QueryCriteria> criteria = new LinkedList<QueryCriteria>();
            List<String> productTypeNames = new Vector<String>(complexQuery.getReducedProductTypeNames());
            productTypeNames.add("AllProductTypes");
            HashSet<String> queriedOnElementNames = new HashSet<String>();
            for (String productType : productTypeNames) {
            	for (String key : ctrlMetadata.asMetadata().getAllKeys()) {
            		if (key.startsWith(productType + "/")) {
            			String[] splitMetKey = key.split("\\/");
            			String elementName = splitMetKey[1];
            			String queryTypeString = splitMetKey[2];
            			String elementNameAndQueryType = elementName + "/" + queryTypeString;
            			if (!queriedOnElementNames.contains(elementNameAndQueryType) && queryTypeString != null) {
	            			QueryType queryType = QueryType.valueOf(queryTypeString.toUpperCase());
	            			if (queryType.equals(QueryType.START_INCL)) {
			            		RangeQueryCriteria startRangeQC = new RangeQueryCriteria();
			            		startRangeQC.setElementName(elementName);
			            		startRangeQC.setStartValue(ctrlMetadata.getMetadata(key));
			            		startRangeQC.setEndValue(null);
			            		startRangeQC.setInclusive(true);
			            		criteria.add(startRangeQC);
			            		queriedOnElementNames.add(elementNameAndQueryType);
	            			}else if (queryType.equals(QueryType.START)) {
			            		RangeQueryCriteria startRangeQC = new RangeQueryCriteria();
			            		startRangeQC.setElementName(elementName);
			            		startRangeQC.setStartValue(ctrlMetadata.getMetadata(key));
			            		startRangeQC.setEndValue(null);
			            		startRangeQC.setInclusive(false);
			            		criteria.add(startRangeQC);
			            		queriedOnElementNames.add(elementNameAndQueryType);
	            			}else if (queryType.equals(QueryType.END_INCL)) {
			            		RangeQueryCriteria endRangeQC = new RangeQueryCriteria();
			            		endRangeQC.setElementName(elementName);
			            		endRangeQC.setStartValue(null);
			            		endRangeQC.setEndValue(ctrlMetadata.getMetadata(key));
			            		endRangeQC.setInclusive(true);
			            		criteria.add(endRangeQC);
			            		queriedOnElementNames.add(elementNameAndQueryType);
	            			}else if (queryType.equals(QueryType.END)) {
			            		RangeQueryCriteria endRangeQC = new RangeQueryCriteria();
			            		endRangeQC.setElementName(elementName);
			            		endRangeQC.setStartValue(null);
			            		endRangeQC.setEndValue(ctrlMetadata.getMetadata(key));
			            		endRangeQC.setInclusive(false);
			            		criteria.add(endRangeQC);
			            		queriedOnElementNames.add(elementNameAndQueryType);
	            			}else if (queryType.equals(QueryType.TERM)) {
								TermQueryCriteria termQC = new TermQueryCriteria();
								termQC.setElementName(elementName);
								termQC.setValue(ctrlMetadata.getMetadata(key));
								criteria.add(termQC);
			            		queriedOnElementNames.add(elementNameAndQueryType);
	            			}
            			}
            		} 
            			
            	}
            }
            metadata = ctrlMetadata.asMetadata();
            
            // add all QueryCriteria to final query
            complexQuery.setCriteria(criteria);

            int minNumOfFiles = 1;
            String minNumOfFilesString = ctrlMetadata.getMetadata(MIN_NUM_OF_FILES) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(MIN_NUM_OF_FILES), metadata) : null;
            if (minNumOfFilesString != null)
                minNumOfFiles = Integer.parseInt(minNumOfFilesString);
            
            long maxGapSize = -1;
            String maxGapString = ctrlMetadata.getMetadata(MAX_GAP_SIZE) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(MAX_GAP_SIZE), metadata) : null;
            if (maxGapString != null) 
            	maxGapSize = Long.parseLong(maxGapString);
            String maxGapStartDateTime = ctrlMetadata.getMetadata(MAX_GAP_START_DATE_TIME) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(MAX_GAP_START_DATE_TIME), metadata) : null;
            String maxGapEndDateTime = ctrlMetadata.getMetadata(MAX_GAP_END_DATE_TIME) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(MAX_GAP_END_DATE_TIME), metadata) : null;
            
            String timeoutString = ctrlMetadata.getMetadata(TIMEOUT) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(TIMEOUT), metadata) : null;
            double timeout = -1;
            if (timeoutString != null)
                timeout = Double.parseDouble(timeoutString);
            
            int expectedNumOfFiles = -1;
            String expectedNumOfFilesString = ctrlMetadata.getMetadata(EXPECTED_NUM_OF_FILES) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(EXPECTED_NUM_OF_FILES), metadata) : null;
            if (expectedNumOfFilesString != null)
                expectedNumOfFiles = Integer.parseInt(expectedNumOfFilesString);
            else
            	return new ResultsFailureState("Precondition Failed : Must set metadata field '" + EXPECTED_NUM_OF_FILES + "'");
                        
//            this.update(new ExecutingState("Performing query '" + SqlParser.unparseSqlQuery(complexQuery) + "'"), ctrlMetadata);
            List<QueryResult> results = new XmlRpcFileManagerClient(new URL(filemgrUrl)).complexQuery(complexQuery);
            
            // Add Results to Metadata
            if (resultKeyFormats != null) {
	        	Pattern pattern = Pattern.compile("\\{.*?\\|.*?\\}");
	        	Matcher matcher = pattern.matcher(resultKeyFormats);
	        	while(matcher.find()) {
	        		String keyFormatSet = resultKeyFormats.substring(matcher.start() + 1, matcher.end() - 1);
	        		String key = keyFormatSet.substring(0, keyFormatSet.indexOf("|")).trim();
	        		String format = keyFormatSet.substring(keyFormatSet.indexOf("|") + 1).trim();
	        		ctrlMetadata.replaceLocalMetadata(key, Arrays.asList(QueryUtils.getQueryResultsAsFormattedString(results, format, ",").split(",")));
	        		ctrlMetadata.setAsWorkflowMetadataKey(key);
	        	}
            }
            if (sqlQueryKey != null) {
            	ctrlMetadata.replaceLocalMetadata(sqlQueryKey, SqlParser.unparseSqlQuery(complexQuery));
            	ctrlMetadata.setAsWorkflowMetadataKey(sqlQueryKey);
            }
            
            ctrlMetadata.commitWorkflowMetadataKeys();
            metadata = ctrlMetadata.asMetadata();
            
            // Run post-property-adders
            String postWorkflowConditionPropAddersString = ctrlMetadata.getMetadata(POST_PROP_ADDERS) != null ? PathUtils.doDynamicReplacement(ctrlMetadata.getMetadata(POST_PROP_ADDERS), metadata) : null;
            if (postWorkflowConditionPropAddersString != null) {
	            String[] workflowConditionPropAdders = postWorkflowConditionPropAddersString.split(",");
	            for (String workflowConditionPropAdder : workflowConditionPropAdders) {
	            	try {
	            		workflowConditionPropAdder = workflowConditionPropAdder.trim();
	            		LOG.log(Level.INFO, "Running Post-WorkflowConditionPropAdder '" + workflowConditionPropAdder + "'");
	            		((WorkflowConditionPropAdder) Class.forName(workflowConditionPropAdder).newInstance()).addMetadata(ctrlMetadata);
	            	}catch (Exception e) {
	            		LOG.log(Level.WARNING, "Failed to run PostWorkflowConditionPropAdder '" + workflowConditionPropAdder + "' : " + e.getMessage(), e);
	                    return new ResultsBailState("Precondition Bailed : Failed to run PostWorkflowConditionPropAdder '" + workflowConditionPropAdder + "' : " + e.getMessage());
	            	}
	            }
            }
            
            ProcessorInfo processorInfo = this.getProcessorInfo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(processorInfo.getExecutionDate());
            long startTime = calendar.getTimeInMillis();
            boolean timeoutHit = (int) timeout == 0 || (timeout != -1 && (System.currentTimeMillis() - startTime) / 1000.0 >= timeout);
            if (timeoutHit && results.size() < minNumOfFiles)
            	return new ResultsFailureState("Precondition Failed : Timeout reached, min number of results, " + minNumOfFiles + ", not found : only found '" + results.size() + "' results");
            if (timeoutHit && results.size() >= minNumOfFiles) {
                return new ResultsSuccessState("Precondition : Timeout reached min number of files, " + expectedNumOfFiles + ", found : found '" + results.size() + "' results");
            }else if (results.size() >= expectedNumOfFiles) {
            	if (maxGapSize != -1) {
            		long largestGapFound = -1;
            		if ((largestGapFound = findLargestGap(results, startDateTimeKey, endDateTimeKey, maxGapStartDateTime, maxGapEndDateTime)) <= maxGapSize)
            			return new ResultsSuccessState("Precondition : Expected number of files, '" + expectedNumOfFiles + ", found : Passed Gap Analysis [Size=" + maxGapSize + ";StartDateTime=" + maxGapStartDateTime + ";EndDateTime=" + maxGapEndDateTime + ";LargestGap=" + largestGapFound + "]");
            		else
            			return new ResultsBailState("Precondition : Expected number of files, '" + expectedNumOfFiles + ", found : Failed Gap Analysis [Size=" + maxGapSize + ";StartDateTime=" + maxGapStartDateTime + ";EndDateTime=" + maxGapEndDateTime + ";LargestGap=" + largestGapFound + "]");
            	}else {
            		return new ResultsSuccessState("Precondition Bailed : Expected number of results, " + expectedNumOfFiles + ", found : Gap Analysis turned off");
            	}
            }else {
        		return new ResultsBailState("Precondition Bailed : Expected number of results, " + expectedNumOfFiles + " not found : only found '" + results.size() + "' results");
            }
            
        }catch (Exception e) {
            LOG.log(Level.SEVERE, "Precondition Failed : " + e.getMessage(), e);
            return new ResultsBailState("Precondition Bailed : " + e.getMessage());
        }
    }
    
    private static long findLargestGap(List<QueryResult> results, final String startDateTimeKey, final String endDateTimeKey, String startDateTimeRange, String endDateTimeRange) throws Exception {
    	List<QueryResult> timeSortedList = new Vector<QueryResult>(results);
    	Collections.sort(timeSortedList, new Comparator<QueryResult>() {
			public int compare(QueryResult o1, QueryResult o2) {
				try {
					long firstStartDateTime = DateUtils.toCalendar(o1.getMetadata().getMetadata(startDateTimeKey), DateUtils.FormatType.UTC_FORMAT).getTimeInMillis();
					long secondStartDateTime = DateUtils.toCalendar(o2.getMetadata().getMetadata(startDateTimeKey), DateUtils.FormatType.UTC_FORMAT).getTimeInMillis();
					return new Long(firstStartDateTime).compareTo(secondStartDateTime);
				}catch (Exception e) {
					LOG.log(Level.SEVERE, "Error occurred during results time sort for gap analysis : " + e.getMessage(), e);
					return 0;
				}
			}
    	});
    	if (startDateTimeKey == null || endDateTimeKey == null)
    		throw new Exception("You must specify the metadata fields '" + START_DATE_TIME_KEY + "' and '" + END_DATE_TIME_KEY + "' to use gap analysis");
    	if (startDateTimeRange == null)
    		LOG.log(Level.WARNING, "You should specify the metadata field '" + MAX_GAP_START_DATE_TIME + "' if you want gap analysis to be tied to a start time range");
    	if (endDateTimeRange == null)
    		LOG.log(Level.WARNING, "You should specify the metadata field '" + MAX_GAP_END_DATE_TIME + "' if you want gap analysis to be tied to an end time range");
    	
        if (timeSortedList.size() > 0) {
            long largestGapSize = 0;
            if (startDateTimeRange != null) {
            	largestGapSize = DateUtils.toCalendar(timeSortedList.get(0).getMetadata().getMetadata(startDateTimeKey), DateUtils.FormatType.UTC_FORMAT).getTimeInMillis() 
            					- DateUtils.toCalendar(startDateTimeRange, DateUtils.FormatType.UTC_FORMAT).getTimeInMillis();
            }
            for (int i = 1; i < timeSortedList.size(); i++) {
                long previousEventEndTime = DateUtils.toCalendar(
                		timeSortedList.get(i-1).getMetadata().getMetadata(endDateTimeKey), 
                        DateUtils.FormatType.UTC_FORMAT).getTimeInMillis();
                long currentEventStartTime = DateUtils.toCalendar(
                		timeSortedList.get(i).getMetadata().getMetadata(startDateTimeKey), 
                        DateUtils.FormatType.UTC_FORMAT).getTimeInMillis();
                long curGapSize = currentEventStartTime - previousEventEndTime;
                if (curGapSize > largestGapSize)
                	largestGapSize = curGapSize;
            }
            if (endDateTimeRange != null) {
            	long lastGapSize = DateUtils.toCalendar(endDateTimeRange, DateUtils.FormatType.UTC_FORMAT).getTimeInMillis() 
            					- DateUtils.toCalendar(timeSortedList.get(timeSortedList.size() - 1).getMetadata().getMetadata(endDateTimeKey), DateUtils.FormatType.UTC_FORMAT).getTimeInMillis();
            	if (lastGapSize > largestGapSize)
            		largestGapSize = lastGapSize;
            }
            return largestGapSize;
        }else {
            return 0;
        }
    }
    
}
