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

    String RETRIEVAL_METHOD_TAG = "rtvlMethod";

    String CLASS_ATTR = "class";

    String PARSER_TAG = "parser";

    String PROTOCOL_TAG = "protocol";

    String TYPE_ATTR = "type";

    String PROTOCOL_FACTORY_TAG = "protocolFactory";

    String ALIAS_SPEC_TAG = "aliasSpec";

    String FILE_ATTR = "file";

    String DAEMON_TAG = "daemon";

    String ACTIVE_ATTR = "active";

    String ALIAS_ATTR = "alias";

    String RUN_INFO_TAG = "runInfo";

    String FIRSTRUN_DATETIME_ATTR = "firstRunDateTime";

    String PERIOD_ATTR = "period";

    String RUNONREBOOT_ATTR = "runOnReboot";

    String EPSILON_ATTR = "epsilon";

    String PROP_INFO_TAG = "propInfo";

    String DIR_ATTR = "dir";

    String PROP_FILES_TAG = "propFiles";

    String REG_EXP_ATTR = "regExp";

    String PARSER_ATTR = "parser";

    String DOWNLOAD_INFO_TAG = "downloadInfo";

    String RENAMING_CONV_ATTR = "renamingConv";

    String ALLOW_ALIAS_OVERRIDE_ATTR = "allowAliasOverride";

    String DELETE_FROM_SERVER_ATTR = "deleteFromServer";

    String PROP_FILE_TAG = "propFile";

    String PATH_ATTR = "path";

    String AFTER_USE_TAG = "afterUse";

    String DELETE_ON_SUCCESS_ATTR = "deleteOnSuccess";

    String MOVEON_TO_SUCCESS_ATTR = "moveToOnSuccess";

    String MOVEON_TO_FAIL_ATTR = "moveToOnFail";

    String DATA_INFO_TAG = "dataInfo";

    String QUERY_ELEM_ATTR = "queryElement";

    String STAGING_AREA_ATTR = "stagingArea";

    String SOURCE_TAG = "source";

    String HOST_ATTR = "host";

    String LOGIN_ATTR = "login";

    String USERNAME_TAG = "username";

    String PASSWORD_TAG = "password";
    
    String CD_TEST_DIR_TAG = "cdTestDir";

    String MAX_CONN_TAG = "maxConn";

}
