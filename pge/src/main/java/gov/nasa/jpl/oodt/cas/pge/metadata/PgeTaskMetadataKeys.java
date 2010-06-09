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


package gov.nasa.jpl.oodt.cas.pge.metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface PgeTaskMetadataKeys {

    public static final String NAME = "PGETask_Name";

    public static final String SCI_EXE_PATH = "PGETask_SciExe_Path";

    public static final String SCI_EXE_VERSION = "PGETask_SciExe_Version";

    public static final String PRODUCT_PATH = "PGETask_ProductPath";

    public static final String CONFIG_FILE_PATH = "PGETask_ConfigFilePath";
    
    public static final String LOG_FILE_PATTERN = "PGETask_LogFilePattern";

    public static final String PROPERTY_ADDER_CLASSPATH = "PGETask_PropertyAdderClasspath";

    public static final String PGE_RUNTIME = "PGETask_Runtime";
    
    /* PGE task statuses */
    public static final String STAGING_INPUT = "PGETask_Staging_Input";

    public static final String CONF_FILE_BUILD = "PGETask_Building_Config_File";

    public static final String RUNNING_PGE = "PGETask_Running";

    public static final String CRAWLING = "PGETask_Crawling";

}
