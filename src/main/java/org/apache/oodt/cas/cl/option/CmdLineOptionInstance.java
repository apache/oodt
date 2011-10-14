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

import org.apache.commons.lang.Validate;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionInstance {

	private CmdLineOption option;
	private List<String> values;
	private List<CmdLineOptionInstance> subOptions;

	public CmdLineOptionInstance() {
		values = new ArrayList<String>();
		subOptions = new ArrayList<CmdLineOptionInstance>();
	}

	public CmdLineOptionInstance(CmdLineOption option, List<String> values) {
		Validate.notNull(option);
		Validate.notNull(values);

		this.option = option; 
		this.values = values;
		subOptions = new ArrayList<CmdLineOptionInstance>();
	}

	public void setOption(CmdLineOption option) {
		this.option = option;
	}

	public CmdLineOption getOption() {
		return option;
	}

	public boolean isGroup() {
		return option instanceof GroupCmdLineOption;
	}

	public boolean isAction() {
		return option instanceof ActionCmdLineOption;
	}

	public boolean isHelp() {
		return option instanceof HelpCmdLineOption;
	}

	public boolean isValidatable() {
		return option instanceof ValidatableCmdLineOption;
	}

	public boolean isHandleable() {
		return option instanceof HandleableCmdLineOption;
	}

	public void setValues(List<String> values) {
		Validate.notNull(values);

		this.values = new ArrayList<String>(values);
	}

	public void addValue(String value) {
		values.add(value);
	}

	public List<String> getValues() {
		return values;
	}

	public void setSubOptions(List<CmdLineOptionInstance> subOptions) {
		Validate.isTrue(isGroup(), "Must be group option to have subOptions");
		Validate.notNull(subOptions, "Cannot set subOptions to NULL");

		this.subOptions = new ArrayList<CmdLineOptionInstance>(subOptions);
	}

	public void addSubOption(CmdLineOptionInstance subOption) {
		Validate.isTrue(isGroup(), "Must be group option to have subOptions");
		Validate.notNull(subOption, "Cannot add NULL subOption");

		this.subOptions.add(subOption);
	}

	public List<CmdLineOptionInstance> getSubOptions() {
		return subOptions;
	}

	public boolean equals(Object obj) {
		if (obj instanceof CmdLineOptionInstance) {
			CmdLineOptionInstance compareObj = (CmdLineOptionInstance) obj;
			return compareObj.option.equals(this.option)
					&& compareObj.values.equals(this.values);
		} else {
			return false;
		}
	}
}
