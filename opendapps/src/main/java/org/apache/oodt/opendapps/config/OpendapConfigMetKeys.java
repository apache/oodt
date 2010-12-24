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

  public static final String RES_ATTR_SPEC_TYPE = "resAttr";

  public static final String PROF_ATTR_SPEC_TYPE = "profAttr";

  public static final String PROF_ELEM_SPEC_TYPE = "profElem";

  public static final String ENUM_ELEMENT_TYPE = "EnumeratedProfileElement";

  public static final String RANGED_ELEMENT_TYPE = "RangedProfileElement";

  public static final String DAP_ROOT_TAG = "root";

  public static final String DATASET_URL_ATTR = "datasetURL";

  public static final String CATALOG_URL_ATTR = "catalogURL";

  public static final String FILTER_ATTR = "filter";

  public static final String REWRITE_ROOT_TAG = "rewrite";

  public static final String REWRITE_VAR_TAG = "var";

  public static final String REWRITE_VAR_NAME_ATTR = "name";

  public static final String REWRITE_VAR_RENAME_ATTR = "rename";

  public static final String REWRITE_VAR_TYPE_ATTR = "type";

  public static final String CONSTANT_ROOT_TAG = "constants";

  public static final String CONSTANT_TAG = "const";

  public static final String CONSTANT_NAME_ATTR = "name";

  public static final String CONSTANT_TYPE_ATTR = "type";

  public static final String CONSTANT_VALUE_ATTR = "value";
  
  public static final String DATASET_MET_ROOT_TAG = "datasetMetadata";
  
  public static final String DATASET_MET_ELEM_TAG = "elem";
  
  public static final String DATASET_MET_NAME_ATTR = "name";
  
  public static final String DATASET_MET_VALUE_ATTR = "value";

}
