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

package org.apache.oodt.opendapps.config;

/**
 * 
 * A set of metadata keys for reading the {@link OpendapConfig}.
 * 
 */
public interface OpendapConfigMetKeys {

  String RES_ATTR_SPEC_TYPE = "resAttr";

  String PROF_ATTR_SPEC_TYPE = "profAttr";

  String PROF_ELEM_SPEC_TYPE = "profElem";

  String ENUM_ELEMENT_TYPE = "EnumeratedProfileElement";

  String RANGED_ELEMENT_TYPE = "RangedProfileElement";

  String DAP_ROOT_TAG = "root";

  String DATASET_URL_ATTR = "datasetURL";

  String CATALOG_URL_ATTR = "catalogURL";

  String FILTER_ATTR = "filter";

  String REWRITE_ROOT_TAG = "rewrite";

  String REWRITE_VAR_TAG = "var";

  String REWRITE_VAR_NAME_ATTR = "name";

  String REWRITE_VAR_RENAME_ATTR = "rename";

  String REWRITE_VAR_TYPE_ATTR = "type";

  String CONSTANT_ROOT_TAG = "constants";

  String CONSTANT_TAG = "const";

  String CONSTANT_NAME_ATTR = "name";

  String CONSTANT_TYPE_ATTR = "type";

  String CONSTANT_VALUE_ATTR = "value";
  
  String DATASET_MET_ROOT_TAG = "datasetMetadata";
  
  String DATASET_MET_ELEM_TAG = "elem";
  
  String DATASET_MET_NAME_ATTR = "name";
  
  String DATASET_MET_VALUE_ATTR = "value";
  
  String RES_LOCATION_ATTR = "resLocation";
  
  String PROCESSING_INSTRUCTIONS_TAG = "processingInstructions";
  
  String PROCESSING_INSTRUCTION_TAG = "processingInstruction";
  
  String PROCESSING_INSTRUCTION_NAME_ATTR = "name";
  
  String PROCESSING_INSTRUCTION_VALUE_ATTR = "value";
  
  String EXCLUDE_VARIABLES_ATTR = "excludeVariables";
  
  String DATETIME_FORMAT_ATTR = "datetimeFormat";

}
