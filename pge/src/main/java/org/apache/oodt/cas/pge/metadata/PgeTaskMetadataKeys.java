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
 * PGETaskInstance Reserved Metadata keys.
 *
 * @author bfoster (Brian Foster)
 */
public interface PgeTaskMetadataKeys {

    public static final String NAME = "PGETask_Name";

    /** @deprecated Never used. */
    @Deprecated
    public static final String SCI_EXE_PATH = "PGETask_SciExe_Path";

    /** @deprecated Never used. */
    @Deprecated
    public static final String SCI_EXE_VERSION = "PGETask_SciExe_Version";

    /** @deprecated Never used. */
    @Deprecated
    public static final String PRODUCT_PATH = "PGETask_ProductPath";

    public static final String CONFIG_FILE_PATH = "PGETask_ConfigFilePath";
    
    public static final String LOG_FILE_PATTERN = "PGETask_LogFilePattern";

    /** @deprecated Use {@link #PROPERTY_ADDERS} instead. */
    @Deprecated
    public static final String PROPERTY_ADDER_CLASSPATH = "PGETask_PropertyAdderClasspath";

    public static final String PROPERTY_ADDERS = "PGETask_PropertyAdders";

    public static final String PGE_RUNTIME = "PGETask_Runtime";

    public static final String ATTEMPT_INGEST_ALL = "PGETask_AttemptIngestAll";

    
    /* PGE task statuses */
    public static final String STAGING_INPUT = "PGETask_Staging_Input";

    public static final String CONF_FILE_BUILD = "PGETask_Building_Config_File";

    public static final String RUNNING_PGE = "PGETask_Running";

    public static final String CRAWLING = "PGETask_Crawling";

}
