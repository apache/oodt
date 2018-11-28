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
package org.apache.oodt.opendapps.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * Class that holds special configuration instructions for generating an Opendap profile.
 * Instructions are stored as (name, values) pairs: each named instruction can have multiple values.
 * An instructions may be specified in the Opendap XML configuration file as a single element with comma-separated values,
 * or as multiple elements with the same name (and one or more values each).
 * 
 * @author Luca Cinquini
 *
 */
public class ProcessingInstructions {
	
	/**
	 * Local storage for processing instructions.
	 * Note that the order of the XML elements is not preserved (on purpose).
	 */
	private final Map<String, Set<String>> instructions = new HashMap<String, Set<String>>();
	
	/**
	 * Returns all values for a named instruction,
	 * or an empty set if the instruction was not specified.
	 * @param key
	 * @return
	 */
	public Set<String> getInstructionValues(String key) {
		if (instructions.containsKey(key)) {
			return Collections.unmodifiableSet( instructions.get(key) );
		} else {
			return Collections.unmodifiableSet( new HashSet<String>() );
		}
	}
	
	/**
	 * If an instruction contains a single value, it is returned.
	 * If the instruction has no values or multiple values, returns null instead.
	 * @param key
	 * @return
	 */
	public String getInstructionValue(String key) {
		if (instructions.get(key)!=null &&  instructions.get(key).size()==1) {
			for (String value : instructions.get(key)) {
				return value;
			}
		}
		return null;
	}
	
	/**
	 * Returns all instructions.
	 * @return
	 */
	public Map<String, Set<String>> getInstructions() {
		return Collections.unmodifiableMap(instructions);
	}
	
	/**
	 * Method to add a value to a named instruction.
	 * Value can be a comma separated list of values.
	 * Leading and trailing spaces for each value are removed.
	 * @param key
	 * @param value
	 */
	public void addInstruction(String key, String value) {
		if (StringUtils.hasText(key)) {
			if (!instructions.containsKey(key)) {
				instructions.put(key, new HashSet<String>());
			}
			String[] vals = value.split(",");
			for (String val : vals) {
				if (StringUtils.hasText(val)) {
					instructions.get(key).add(val.trim());
				}
			}
		}
	}

}