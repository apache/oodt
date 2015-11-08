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
package org.apache.oodt.cas.pge.writers;

//JDK imports

import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * Abstract interface for generating PGE config input files defining the input
 * necessary to run the underlying PGE.
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public abstract class DynamicConfigFileWriter implements SciPgeConfigFileWriter {

	private static final Logger logger = Logger
			.getLogger(DynamicConfigFileWriter.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.oodt.cas.pge.writers.SciPgeConfigFileWriter#createConfigFile
	 * (java.lang.String, org.apache.oodt.cas.metadata.Metadata,
	 * java.lang.Object[])
	 */
	@Override
	public File createConfigFile(String sciPgeConfigFilePath,
			Metadata inputMetadata, Object... customArgs) throws IOException {
		try {
			return this.generateFile(sciPgeConfigFilePath, inputMetadata,
					logger, customArgs);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			throw new IOException(e);
		}
	}

	/**
	 * Generates a config file for CAS-PGE to use as PGE input with the given
	 * default logger.
	 * 
	 * @param filePath
	 *            The name of the config file to generate.
	 * @param metadata
	 *            Input CAS-PGE metadata.
	 * @param logger
	 *            The logger to write any status information to.
	 * @param args
	 *            Any custom parameters needed for the writer to write the input
	 *            config file.
	 * @return The newly generated CAS-PGE input config file.
	 * @throws Exception
	 *             If any error occurs.
	 */
	public abstract File generateFile(String filePath, Metadata metadata,
			Logger logger, Object... args) throws Exception;
}
