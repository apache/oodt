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
package org.apache.oodt.cas.pge.metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          Describe your class here
 *          </p>
 *          .
 */
public interface PGETaskMetKeys {
	
	public static final String PGE_TASK_GROUP = "PGETask";	
	
	public static final String NAME = PGE_TASK_GROUP + "/Name";

	public static final String PGE_CONFIG_BUILDER = PGE_TASK_GROUP + "/PgeConfigBuilder";

	public static final String LOG_FILE_PATTERN = PGE_TASK_GROUP + "/LogFilePattern";

	public static final String PROPERTY_ADDERS = PGE_TASK_GROUP + "/PropertyAdders";

	public static final String PGE_RUNTIME = PGE_TASK_GROUP + "/Runtime";

	public static final String DUMP_METADATA = PGE_TASK_GROUP + "/DumpMetadata";

	
	public static final String QUERY_GROUP = PGE_TASK_GROUP + "/Query";

    public static final String QUERY_FILE_MANAGER_URL = QUERY_GROUP + "/FileManagerUrl";

	public static final String QUERY_CLIENT_TRANSFER_SERVICE_FACTORY = QUERY_GROUP + "/ClientTransferServiceFactory";

	
	public static final String INGEST_GROUP = PGE_TASK_GROUP + "/Ingest";

    public static final String INGEST_FILE_MANAGER_URL = INGEST_GROUP + "/FileManagerUrl";

	public static final String INGEST_CLIENT_TRANSFER_SERVICE_FACTORY = INGEST_GROUP + "/ClientTransferServiceFactory";

	public static final String CRAWLER_CRAWL_FOR_DIRS = INGEST_GROUP + "/CrawlerCrawlForDirs";

	public static final String CRAWLER_RECUR = INGEST_GROUP + "/CrawlerRecur";

	public static final String MET_FILE_EXT = INGEST_GROUP + "/MetFileExtension";

	public static final String REQUIRED_METADATA = INGEST_GROUP + "/RequiredMetadata";

	public static final String ACTION_IDS = INGEST_GROUP + "/ActionsIds";

	public static final String ACTION_REPO_FILE = INGEST_GROUP + "/ActionRepoFile";

	public static final String ATTEMPT_INGEST_ALL = INGEST_GROUP + "/AttemptIngestAll";

	
	public static final String CONDITION_GROUP = PGE_TASK_GROUP + "/Condition";
	    
    public static final String TIMEOUT = CONDITION_GROUP + "/Timeout";

    public static final String PROP_ADDERS = CONDITION_GROUP + "/PropertyAdders";

    public static final String POST_PROP_ADDERS = CONDITION_GROUP + "/PostPropertyAdders";
    
    public static final String START_DATE_TIME_KEY = CONDITION_GROUP + "/StartDateTimeKey";

    public static final String END_DATE_TIME_KEY = CONDITION_GROUP + "/EndDateTimeKey";
    
    public static final String FILTER_ALGOR = CONDITION_GROUP + "/FilterAlgorClass";
    
    public static final String PRODUCT_TYPES = CONDITION_GROUP + "/ProductTypeNames";
    
    public static final String EXPECTED_NUM_OF_FILES = CONDITION_GROUP + "/ExpectedNumOfFiles";
    
    public static final String EPSILON_IN_MILLIS = CONDITION_GROUP + "/EpsilonInMillis";

    public static final String VERSIONING_KEY = CONDITION_GROUP + "/VersioningKey";
    
    public static final String SORY_BY_KEY = CONDITION_GROUP + "/SortByKey";

    public static final String MIN_NUM_OF_FILES = CONDITION_GROUP + "/MinNumOfFiles";
    
    public static final String MAX_GAP_SIZE = CONDITION_GROUP + "/MaxGap/Size";

    public static final String MAX_GAP_START_DATE_TIME = CONDITION_GROUP + "/MaxGap/StartDateTime";
    
    public static final String MAX_GAP_END_DATE_TIME = CONDITION_GROUP + "/MaxGap/EndDateTime";

    public static final String VERSION_CONVERTER = CONDITION_GROUP + "/VersionConverter";

    public static final String RESULT_KEY_FORMATS = CONDITION_GROUP + "/ResultKeyFormats";
    
    public static final String SQL_QUERY_KEY = CONDITION_GROUP + "/SqlQueryKey";



}
