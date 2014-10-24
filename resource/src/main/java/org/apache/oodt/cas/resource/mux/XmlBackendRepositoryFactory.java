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

package org.apache.oodt.cas.resource.mux;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * The XML Backend Repository Factory interface.
 * </p>
 */
public class XmlBackendRepositoryFactory implements BackendRepositoryFactory {

	private static final Logger LOG = Logger.getLogger(XmlBackendRepositoryFactory.class.getName());
	/**
	 * Create the backend repository (xml)
	 * @return the newly minted backend repository
	 */
	public XmlBackendRepository createBackendRepository() {
		try {
			String uri = System.getProperty("resource.backend.mux.xmlrepository.queuetobackend");
			/* do env var replacement */
			uri = PathUtils.replaceEnvVariables(uri);
			return new XmlBackendRepository(uri);
		} catch (NullPointerException e) {
			LOG.log(	Level.SEVERE,"Failed to create XmlBackendRepository: "+ e.getMessage(), e);
			return null;
		}
	}

}
