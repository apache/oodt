/**
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

package org.apache.oodt.pcs.metadata;

/**
 * 
 * A Collection of Metadata field names for PCS.
 * 
 */
public interface PCSMetadata {

  /* Met Fields */
  String APPLICATION_SUCCESS_FLAG = "ApplicationSuccessFlag";

  String ON_DISK = "OnDisk";

  String TAPE_LOCATION = "TapeLocation";

  String PRODUCTION_LOCATION = "ProductionLocation";

  String PRODUCTION_LOCATION_CODE = "ProductionLocationCode";

  String DATA_VERSION = "DataVersion";

  String DATA_PROVIDER = "DataProvider";

  String COLLECTION_LABEL = "CollectionLabel";

  String COMMENTS = "Comments";

  String EXECUTABLE_PATHNAMES = "ExecutablePathnames";

  String EXECUTABLE_VERSIONS = "ExecutableVersions";

  String PROCESSING_LEVEL = "ProcessingLevel";

  String JOB_ID = "JobId";

  String TASK_ID = "TaskId";

  String PRODUCTION_DATE_TIME = "ProductionDateTime";

  String INPUT_FILES = "InputFiles";

  String PGE_NAME = "PGEName";

  String OUTPUT_FILES = "OutputFiles";
  
  String TEST_TAG = "TestTag";

  String SUB_TEST_TAG = "SubTestTag";

  String TEST_LOCATION = "TestLocation";

  String TEST_COUNTER = "TestCounter";

  String TEST_DATE = "TestDate";
  
  String START_DATE_TIME = "StartDateTime";

  String END_DATE_TIME = "EndDateTime";

}
