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

package org.apache.oodt.pcs.services.config;

/**
 * 
 * Config file met keys for the PCS services <code>context.xml</code> file.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface PCSServiceConfMetKeys {

  String FM_URL = "org.apache.oodt.cas.fm.url";

  String WM_URL = "org.apache.oodt.cas.wm.url";

  String RM_URL = "org.apache.oodt.cas.rm.url";

  String PCS_LL_CONF_FILE_PATH = "org.apache.oodt.pcs.ll.conf.filePath";

  String PCS_HEALTH_CRAWLER_CONF_PATH = "org.apache.oodt.pcs.health.crawler.conf.filePath";

  String PCS_HEALTH_WORKFLOW_STATUS_PATH = "org.apache.oodt.pcs.health.workflow.statuses.filePath";

  String PCS_TRACE_ENABLE_NON_CAT = "org.apache.oodt.pcs.trace.enableNonCat";
  
  String PCS_TRACE_PTYPE_EXCLUDE_LIST = "org.apache.oodt.pcs.trace.productTypeExcludeList";

}
