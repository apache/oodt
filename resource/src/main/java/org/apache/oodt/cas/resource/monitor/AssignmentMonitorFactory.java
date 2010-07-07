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


package org.apache.oodt.cas.resource.monitor;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.util.Arrays;
import java.util.List;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * Creates implementations of {@link AssignmentMonitor}s.
 * </p>
 * 
 */
public class AssignmentMonitorFactory implements MonitorFactory {

	private List nodesDirList;

	public AssignmentMonitorFactory() {
		String nodesDirUris = System
				.getProperty("org.apache.oodt.cas.resource.monitor.nodes.dirs");

		if (nodesDirUris != null) {
			/* do env var replacement */
			nodesDirUris = PathUtils.replaceEnvVariables(nodesDirUris);
			String[] dirUris = nodesDirUris.split(",");
			nodesDirList = Arrays.asList(dirUris);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.oodt.cas.resource.monitor.MonitorFactory#createMonitor()
	 */
	public Monitor createMonitor() {
		if (nodesDirList != null) {
			return new AssignmentMonitor(nodesDirList);
		} else {
			return null;
		}
	}

}
