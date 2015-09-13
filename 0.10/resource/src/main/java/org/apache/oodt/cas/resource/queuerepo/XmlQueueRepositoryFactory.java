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

package org.apache.oodt.cas.resource.queuerepo;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The XML Queue Repository Factory interface.
 * </p>
 */
public class XmlQueueRepositoryFactory implements QueueRepositoryFactory {

	private static final Logger LOG = Logger
			.getLogger(XmlQueueRepositoryFactory.class.getName());

	public XmlQueueRepository createQueueRepository() {
		try {
			String queuesDirUris = System
					.getProperty("org.apache.oodt.cas.resource.nodetoqueues.dirs");

			/* do env var replacement */
			queuesDirUris = PathUtils.replaceEnvVariables(queuesDirUris);
			String[] dirUris = queuesDirUris.split(",");
			return new XmlQueueRepository(Arrays.asList(dirUris));
		} catch (Exception e) {
			LOG
					.log(
							Level.SEVERE,
							"Failed to create XML Queue Repository (make sure you specify node-to-queue mapping java property 'org.apache.oodt.cas.resource.scheduler.nodetoqueues.dirs') : "
									+ e.getMessage(), e);
			return null;
		}
	}

}
