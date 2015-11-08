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
package org.apache.oodt.cas.pge.config;

/**
 * Met Keys used when reading a {@link PgeConfig} XML file.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public interface PgeConfigMetKeys {

    String IMPORT_TAG = "import";

    String NAMESPACE_ATTR = "namespace";

    String FILE_ATTR = "file";

    String CUSTOM_METADATA_TAG = "customMetadata";

    String METADATA_TAG = "metadata";

    String KEYREF_ATTR = "key-ref";

    String KEY_GEN_ATTR = "key-gen";

    String KEY_ATTR = "key";

    String VAL_ATTR = "val";

    String ENV_REPLACE_ATTR = "envReplace";

    String ENV_REPLACE_NO_RECUR_ATTR = "envReplace-NoRecur";

    String SPLIT_ATTR = "split";

    String WORKFLOW_MET_ATTR = "workflowMet";

    String DYN_INPUT_FILES_TAG = "dynInputFiles";

    String FILE_TAG = "file";

    String PATH_ATTR = "path";

    String WRITER_CLASS_ATTR = "writerClass";

    String ARGS_ATTR = "args";

    String EXE_TAG = "exe";

    String DIR_ATTR = "dir";

    String SHELL_TYPE_ATTR = "shellType";

    String CMD_TAG = "cmd";

    String OUTPUT_TAG = "output";

    String DIR_TAG = "dir";

    String CREATE_BEFORE_EXEC_ATTR = "createBeforeExe";
    
    String FILES_TAG = "files";

    String REGEX_ATTR = "regExp";

    String NAME_ATTR = "name";

    String MET_FILE_WRITER_CLASS_ATTR = "metFileWriterClass";

    String RENAMING_CONV_TAG = "renamingConv";

    String NAMING_EXPR_ATTR = "namingExpr";

    String FILE_STAGING_TAG = "fileStaging";

    String STAGE_FILES_TAG = "stageFiles";

    String METADATA_KEY_ATTR = "metadataKey";

    String FORCE_ATTR = "force";
}
