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
  public static final String APPLICATION_SUCCESS_FLAG = "ApplicationSuccessFlag";

  public static final String ON_DISK = "OnDisk";

  public static final String TAPE_LOCATION = "TapeLocation";

  public static final String PRODUCTION_LOCATION = "ProductionLocation";

  public static final String PRODUCTION_LOCATION_CODE = "ProductionLocationCode";

  public static final String DATA_VERSION = "DataVersion";

  public static final String DATA_PROVIDER = "DataProvider";

  public static final String COLLECTION_LABEL = "CollectionLabel";

  public static final String COMMENTS = "Comments";

  public static final String EXECUTABLE_PATHNAMES = "ExecutablePathnames";

  public static final String EXECUTABLE_VERSIONS = "ExecutableVersions";

  public static final String PROCESSING_LEVEL = "ProcessingLevel";

  public static final String JOB_ID = "JobId";

  public static final String TASK_ID = "TaskId";

  public static final String PRODUCTION_DATE_TIME = "ProductionDateTime";

  public static final String INPUT_FILES = "InputFiles";

  public static final String PGE_NAME = "PGEName";

  public static final String OUTPUT_FILES = "OutputFiles";
  
  public static final String TEST_TAG = "TestTag";

  public static final String SUB_TEST_TAG = "SubTestTag";

  public static final String TEST_LOCATION = "TestLocation";

  public static final String TEST_COUNTER = "TestCounter";

  public static final String TEST_DATE = "TestDate";
  
  public static final String START_DATE_TIME = "StartDateTime";

  public static final String END_DATE_TIME = "EndDateTime";

}
