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

    public static final String IMPORT_TAG = "import";

    public static final String NAMESPACE_ATTR = "namespace";

    public static final String FILE_ATTR = "file";

    public static final String CUSTOM_METADATA_TAG = "customMetadata";

    public static final String METADATA_TAG = "metadata";

    public static final String KEYREF_ATTR = "key-ref";

    public static final String KEY_GEN_ATTR = "key-gen";

    public static final String KEY_ATTR = "key";

    public static final String VAL_ATTR = "val";

    public static final String ENV_REPLACE_ATTR = "envReplace";

    public static final String ENV_REPLACE_NO_RECUR_ATTR = "envReplace-NoRecur";

    public static final String SPLIT_ATTR = "split";

    public static final String WORKFLOW_MET_ATTR = "workflowMet";

    public static final String DYN_INPUT_FILES_TAG = "dynInputFiles";

    public static final String FILE_TAG = "file";

    public static final String PATH_ATTR = "path";

    public static final String WRITER_CLASS_ATTR = "writerClass";

    public static final String ARGS_ATTR = "args";

    public static final String EXE_TAG = "exe";

    public static final String DIR_ATTR = "dir";

    public static final String SHELL_TYPE_ATTR = "shellType";

    public static final String CMD_TAG = "cmd";

    public static final String OUTPUT_TAG = "output";

    public static final String DIR_TAG = "dir";

    public static final String CREATE_BEFORE_EXEC_ATTR = "createBeforeExe";
    
    public static final String FILES_TAG = "files";

    public static final String REGEX_ATTR = "regExp";

    public static final String NAME_ATTR = "name";

    public static final String MET_FILE_WRITER_CLASS_ATTR = "metFileWriterClass";

    public static final String RENAMING_CONV_TAG = "renamingConv";

    public static final String NAMING_EXPR_ATTR = "namingExpr";    

    public static final String FILE_STAGING_TAG = "fileStaging";

    public static final String STAGE_FILES_TAG = "stageFiles";

    public static final String METADATA_KEY_ATTR = "metadataKey";

    public static final String FORCE_ATTR = "force";
}
