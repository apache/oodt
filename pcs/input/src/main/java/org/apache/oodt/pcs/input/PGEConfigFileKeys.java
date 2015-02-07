// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.pcs.input;

/**
 * 
 * <p>
 * Metadata keys used when building a {@link PGEConfigurationFile}
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 */
public interface PGEConfigFileKeys {

  public static final String RECORDED_AUX_INPUT_FILES_GROUP = "RecordedAuxiliaryInputFiles";

  public static final String DYNAMIC_AUX_INPUT_FILES_GROUP = "DynamicAuxiliaryInputFiles";

  public static final String INPUT_PRODUCT_FILES_GROUP = "InputProductFiles";

  public static final String PRODUCT_PATH_GROUP = "ProductPathGroup";

  public static final String PGE_NAME_GROUP = "PGENameGroup";

  public static final String MODE_GROUP = "ModeGroup";

  public static final String GEOMETRY_GROUP = "Geometry";

  public static final String PRIMARY_EXECUTABLE_GROUP = "PrimaryExecutable";

  public static final String SFIF_FILE_GROUP = "StaticFileIdentificationFiles";

  public static final String JOB_IDENTIFICATION_GROUP = "JobIdentification";

  public static final String SCF_IDENTIFICATION_GROUP = "SCFIdentification";

  public static final String MONITOR_GROUP = "MonitorGroup";

  public static final String MONITOR_LEVEL_GROUP = "MonitorLevel";

  public static final String LOG_METADATA_GROUP = "LogMetadata";

  public static final String COMMAND_LINE_PARAMETERS_GROUP = "CommandLineParameters";

}
