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

package org.apache.oodt.pcs.health;

/**
 * Met keys for the {@link PCSHealthMonitor} tool
 * 
 * @author mattmann
 * @version $Revision$
 */
public interface PCSHealthMonitorMetKeys {

  public static final String HEADER_AND_FOOTER = "--------------------------------------";

  public static final String SECTION_SEPARATOR = "--------";

  public static final String REPORT_BANNER = "PCS Health Monitor Report";

  public static final String FILE_MANAGER_DAEMON_NAME = "File Manager";

  public static final String WORKFLOW_MANAGER_DAEMON_NAME = "Workflow Manager";

  public static final String RESOURCE_MANAGER_DAEMON_NAME = "Resource Manager";

  public static final String BATCH_STUB_DAEMON_NAME = "batch stub";

  public static final String STATUS_UP = "UP";

  public static final String STATUS_DOWN = "DOWN";

  public static final int TOP_N_PRODUCTS = 20;

  public static final int CRAWLER_DOWN_INT = -1;

  public static final double CRAWLER_DOWN_DOUBLE = -1.0;

}
