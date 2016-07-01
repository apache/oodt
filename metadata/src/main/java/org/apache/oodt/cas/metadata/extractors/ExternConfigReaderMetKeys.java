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


package org.apache.oodt.cas.metadata.extractors;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met keys for the {@link ExternConfigReader}
 * </p>.
 */
public interface ExternConfigReaderMetKeys {

    String EXEC_TAG = "exec";

    String WORKING_DIR_ATTR = "workingDir";

    String MET_FILE_EXT_ATTR = "metFileExt";

    String EXTRACTOR_BIN_PATH_TAG = "extractorBinPath";

    String ENV_REPLACE_ATTR = "envReplace";

    String ARGS_TAG = "args";

    String ARG_TAG = "arg";

    String IS_DATA_FILE_ATTR = "isDataFile";

    String IS_MET_FILE_ATTR = "isMetFile";

    String APPEND_EXT_ATTR = "appendExt";

    String IS_PATH_ATTR = "isPath";

}
