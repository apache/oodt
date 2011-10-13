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

package org.apache.oodt.cas.cl.option;

//JDK imports
import java.util.ArrayList;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cl.option.require.RequirementRule;

/**
 *
 * @author bfoster
 * @version $Revision$
 */
public class SimpleCmdLineOption implements CmdLineOption {

	private String shortOption;

	private String longOption;

	private String description;

	private String argDescription;

	private boolean required;

	private List<RequirementRule> requirementRules;

	private boolean hasArgs;

	private boolean performAndQuit;

	private Class<?> type;

	public SimpleCmdLineOption() {
		argDescription = "arg";
		required = false;
		hasArgs = false;
		performAndQuit = false;
		type = String.class;
		requirementRules = new ArrayList<RequirementRule>();
	}

	public SimpleCmdLineOption(String shortOption, String longOption,
			String description, boolean hasArgs) {
		this();
		this.shortOption = shortOption;
		this.longOption = longOption;
		this.description = description;
		this.hasArgs = hasArgs;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getShortOption() {
		return shortOption;
	}

	public void setShortOption(String shortOption) {
		this.shortOption = shortOption;
	}

	public String getLongOption() {
		return longOption;
	}

	public void setLongOption(String longOption) {
		this.longOption = longOption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean hasArgs() {
		return hasArgs;
	}

	public void setHasArgs(boolean hasArgs) {
		this.hasArgs = hasArgs;
	}

	public void setArgsDescription(String argDescription) {
		this.argDescription = argDescription;
	}

	public String getArgsDescription() {
		return argDescription;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}

	public List<RequirementRule> getRequirementRules() {
		return this.requirementRules;
	}

	public void setRequirementRules(List<RequirementRule> requirementRules) {
		this.requirementRules = requirementRules;
	}

	public boolean isPerformAndQuit() {
		return performAndQuit;
	}

	public void setPerformAndQuit(boolean performAndQuit) {
		this.performAndQuit = performAndQuit;
	}

	public int compareTo(CmdLineOption cmdLineOption) {
		int thisScore = (this.required ? 2 : 0)
				+ (!requirementRules.isEmpty() ? 1 : 0);
		int compareScore = (cmdLineOption.isRequired() ? 2 : 0)
				+ (!cmdLineOption.getRequirementRules().isEmpty() ? 1 : 0);
		return new Integer(thisScore).compareTo(compareScore);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CmdLineOption) {
			SimpleCmdLineOption compareObj = (SimpleCmdLineOption) obj;
			return compareObj.shortOption.equals(this.shortOption)
					|| compareObj.longOption.equals(this.longOption);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return shortOption.hashCode();
	}
}
