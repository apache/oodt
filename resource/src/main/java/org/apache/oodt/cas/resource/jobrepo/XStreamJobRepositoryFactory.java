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

package org.apache.oodt.cas.resource.jobrepo;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * Factory for creating XStream based JobRepository
 */
public class XStreamJobRepositoryFactory implements JobRepositoryFactory {

	private static final Logger LOG = Logger.getLogger(XStreamJobRepositoryFactory.class.getName());
	
	public XStreamJobRepository createRepository() {
		try {
			String workingDirPropVal = System.getProperty("org.apache.oodt.cas.resource.jobrepo.xstream.working.dir");
			if (workingDirPropVal == null) {
			  return null;
			} else {
			  workingDirPropVal = PathUtils.doDynamicReplacement(workingDirPropVal);
			}
			File working = new File(workingDirPropVal);
			if (!working.exists()) {
			  working.mkdirs();
			}
			int maxHistory = Integer.parseInt(System.getProperty("org.apache.oodt.cas.resource.jobrepo.xstream.max.history", "-1"));
			return new XStreamJobRepository(working, maxHistory);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to loaded XStreamJobRepository : " + e.getMessage(), e);
			return null;
		}
	}

}
