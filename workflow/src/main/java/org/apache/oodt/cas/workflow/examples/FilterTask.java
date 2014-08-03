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

package org.apache.oodt.cas.workflow.examples;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

/**
 * Filters dynamic {@link Metadata} from the provided metadata in the
 * {@link #run(Metadata, WorkflowTaskConfiguration)} method.
 * 
 * The config parameter in {@link #run(Metadata, WorkflowTaskConfiguration)}
 * defines metadata parameters to remove and rename via the following
 * directives:
 * 
 * <ul>
 * <li>Remove_Key - a comma separated list of keys to remove</li>
 * <li>Rename_[Key] - set the value of this property to the new name of the
 * [Key] parameter. E.g., having a key in the task configuration named
 * Rename_Filename with value set to Prior_Filename will rename the key in the
 * dynamic metadata named Filename to the new name of Prior_Filename, preserving
 * its prior value.</li>
 * </ul>
 * 
 * Note that the Rename keys will be evaluated first, before the Remove_Key
 * configuration parameter is evaluated.
 * 
 */
public class FilterTask implements WorkflowTaskInstance {

	private final static String REMOVE_KEY = "Remove_Key";
	private static final Logger LOG = Logger.getLogger(FilterTask.class
			.getName());

	@Override
	public void run(Metadata metadata, WorkflowTaskConfiguration config)
			throws WorkflowTaskInstanceException {

		// evaluate renames
		for (Object configKey : config.getProperties().keySet()) {
			String configKeyName = (String) configKey;
			if (configKeyName.startsWith("Rename")) {
				String renameOrigKeyName = configKeyName.split("_")[1];
				String renameKeyName = config.getProperty(configKeyName);
				// check to see if key exists
				if (metadata.containsKey(renameOrigKeyName)) {
					LOG.log(Level.INFO, "Renaming key: [" + renameOrigKeyName
							+ "] to [" + renameKeyName + "]: values: "
							+ metadata.getAllMetadata(renameOrigKeyName));
					metadata.replaceMetadata(renameKeyName,
							metadata.getAllMetadata(renameOrigKeyName));
					metadata.removeMetadata(renameOrigKeyName);
				} else {
					LOG.log(Level.WARNING, "Request to rename key: ["
							+ renameOrigKeyName + "] to [" + renameKeyName
							+ "]: orig key does not exist in dynamic metadata!");
				}
			}
		}

		// evaluate removes
		if (config.getProperties().containsKey(REMOVE_KEY)) {
			String removeMetKeyNames = config.getProperty(REMOVE_KEY);
			for (String keyName : Arrays.asList(removeMetKeyNames.split(","))) {
				// handle whitespace
				keyName = keyName.trim();
				if (metadata.containsKey(keyName)) {
					LOG.log(Level.INFO,
							"Removing key from workflow metadata: [" + keyName
									+ "]: values: "
									+ metadata.getAllMetadata(keyName));
					metadata.removeMetadata(keyName);
				} else {
					LOG.log(Level.WARNING, "Request to remove key: [" + keyName
							+ "]: key does not exist in workflow metadata!");  
				}
			}
		}

	}

}
