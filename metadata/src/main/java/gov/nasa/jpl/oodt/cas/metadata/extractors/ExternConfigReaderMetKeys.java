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


package gov.nasa.jpl.oodt.cas.metadata.extractors;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met keys for the {@link ExternConfigReader}
 * </p>.
 */
public interface ExternConfigReaderMetKeys {

    public static final String EXEC_TAG = "exec";

    public static final String WORKING_DIR_ATTR = "workingDir";

    public static final String MET_FILE_EXT_ATTR = "metFileExt";

    public static final String EXTRACTOR_BIN_PATH_TAG = "extractorBinPath";

    public static final String ENV_REPLACE_ATTR = "envReplace";

    public static final String ARGS_TAG = "args";

    public static final String ARG_TAG = "arg";

    public static final String IS_DATA_FILE_ATTR = "isDataFile";

    public static final String IS_MET_FILE_ATTR = "isMetFile";

    public static final String APPEND_EXT_ATTR = "appendExt";

    public static final String IS_PATH_ATTR = "isPath";

}
