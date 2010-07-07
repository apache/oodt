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


package org.apache.oodt.cas.pushpull.config;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met Keys used when parsing {@link Config} xml files
 * </p>.
 */
public interface ConfigParserMetKeys {

    public static final String RETRIEVAL_METHOD_TAG = "rtvlMethod";

    public static final String CLASS_ATTR = "class";

    public static final String PARSER_TAG = "parser";

    public static final String PROTOCOL_TAG = "protocol";

    public static final String TYPE_ATTR = "type";

    public static final String PROTOCOL_FACTORY_TAG = "protocolFactory";

    public static final String ALIAS_SPEC_TAG = "aliasSpec";

    public static final String FILE_ATTR = "file";

    public static final String DAEMON_TAG = "daemon";

    public static final String ACTIVE_ATTR = "active";

    public static final String ALIAS_ATTR = "alias";

    public static final String RUN_INFO_TAG = "runInfo";

    public static final String FIRSTRUN_DATETIME_ATTR = "firstRunDateTime";

    public static final String PERIOD_ATTR = "period";

    public static final String RUNONREBOOT_ATTR = "runOnReboot";

    public static final String EPSILON_ATTR = "epsilon";

    public static final String PROP_INFO_TAG = "propInfo";

    public static final String DIR_ATTR = "dir";

    public static final String PROP_FILES_TAG = "propFiles";

    public static final String REG_EXP_ATTR = "regExp";

    public static final String PARSER_ATTR = "parser";

    public static final String DOWNLOAD_INFO_TAG = "downloadInfo";

    public static final String RENAMING_CONV_ATTR = "renamingConv";

    public static final String ALLOW_ALIAS_OVERRIDE_ATTR = "allowAliasOverride";

    public static final String DELETE_FROM_SERVER_ATTR = "deleteFromServer";

    public static final String PROP_FILE_TAG = "propFile";

    public static final String PATH_ATTR = "path";

    public static final String AFTER_USE_TAG = "afterUse";

    public static final String MOVEON_TO_SUCCESS_ATTR = "moveToOnSuccess";

    public static final String MOVEON_TO_FAIL_ATTR = "moveToOnFail";

    public static final String DATA_INFO_TAG = "dataInfo";

    public static final String QUERY_ELEM_ATTR = "queryElement";

    public static final String STAGING_AREA_ATTR = "stagingArea";

    public static final String SOURCE_TAG = "source";

    public static final String HOST_ATTR = "host";

    public static final String LOGIN_ATTR = "login";

    public static final String USERNAME_TAG = "username";

    public static final String PASSWORD_TAG = "password";
    
    public static final String CD_TEST_DIR_TAG = "cdTestDir";

}
