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

  String RECORDED_AUX_INPUT_FILES_GROUP = "RecordedAuxiliaryInputFiles";

  String DYNAMIC_AUX_INPUT_FILES_GROUP = "DynamicAuxiliaryInputFiles";

  String INPUT_PRODUCT_FILES_GROUP = "InputProductFiles";

  String PRODUCT_PATH_GROUP = "ProductPathGroup";

  String PGE_NAME_GROUP = "PGENameGroup";

  String MODE_GROUP = "ModeGroup";

  String GEOMETRY_GROUP = "Geometry";

  String PRIMARY_EXECUTABLE_GROUP = "PrimaryExecutable";

  String SFIF_FILE_GROUP = "StaticFileIdentificationFiles";

  String JOB_IDENTIFICATION_GROUP = "JobIdentification";

  String SCF_IDENTIFICATION_GROUP = "SCFIdentification";

  String MONITOR_GROUP = "MonitorGroup";

  String MONITOR_LEVEL_GROUP = "MonitorLevel";

  String LOG_METADATA_GROUP = "LogMetadata";

  String COMMAND_LINE_PARAMETERS_GROUP = "CommandLineParameters";

}
