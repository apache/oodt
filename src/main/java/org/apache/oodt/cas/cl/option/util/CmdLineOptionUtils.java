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

package org.apache.oodt.cas.cl.option.util;

//JDK imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.require.RequirementRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cl.action.CmdLineAction;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionUtils {

	private CmdLineOptionUtils() {}

	public static Set<CmdLineOption> determineRequired(CmdLineAction action,
			Set<CmdLineOption> options) {
		Set<CmdLineOption> requiredOptions = getRequiredOptions(options);
		for (CmdLineOption option : options) {
			if (isRequired(action, option)) {
				requiredOptions.add(option);
			}
		}
		return requiredOptions;
	}

	public static boolean isRequired(CmdLineAction action, CmdLineOption option) {
		Validate.notNull(option);
		Validate.notNull(action);

		if (option instanceof ActionCmdLineOption) {
			return false;
		}

		for (RequirementRule requirementRule : option.getRequirementRules()) {
			if (requirementRule.getRelation(action) == Relation.REQUIRED) {
				return true;
			}
		}
		return false;
	}

	public static Set<CmdLineOption> determineOptional(CmdLineAction action, Set<CmdLineOption> options) {
		Set<CmdLineOption> optionalOptions = new HashSet<CmdLineOption>();
		for (CmdLineOption option : options) {
			if (isOptional(action, option)) {
				optionalOptions.add(option);
			}
		}
		return optionalOptions;
	}

	public static boolean isOptional(CmdLineAction action, CmdLineOption option) {
		Validate.notNull(option);
		Validate.notNull(action);

		if (option instanceof ActionCmdLineOption) {
			return false;
		}

		for (RequirementRule requirementRule : option.getRequirementRules()) {
			if (requirementRule.getRelation(action) == Relation.OPTIONAL) {
				return true;
			}
		}
		return false;
	}

	public static Set<CmdLineOption> getRequiredOptions(Set<CmdLineOption> options) {
		HashSet<CmdLineOption> requiredOptions = new HashSet<CmdLineOption>();
		for (CmdLineOption option : options) {
			if (option.isRequired()) {
				requiredOptions.add(option);
			}
		}
		return requiredOptions;
	}

	public static List<CmdLineOption> sortOptionsByRequiredStatus(
			Set<CmdLineOption> options) {
		ArrayList<CmdLineOption> optionsList = new ArrayList<CmdLineOption>(options);
		Collections.sort(optionsList, new Comparator<CmdLineOption>() {
			public int compare(CmdLineOption option1, CmdLineOption option2) {
				int thisScore = (option1.isRequired() ? 2 : 0)
						+ (!option1.getRequirementRules().isEmpty() ? 1 : 0);
				int compareScore = (option2.isRequired() ? 2 : 0)
						+ (!option2.getRequirementRules().isEmpty() ? 1 : 0);
				return new Integer(thisScore).compareTo(compareScore);
			}
		});
		Collections.reverse(optionsList);
		return optionsList;
	}

	public static CmdLineOption getOptionByName(String optionName, Set<CmdLineOption> options) {
		for (CmdLineOption option : options)
			if (option.getLongOption().equals(optionName)
					|| option.getShortOption().equals(optionName))
				return option;
		return null;
	}

	public static CmdLineOptionInstance getOptionInstanceByName(
			String optionName, Set<CmdLineOptionInstance> optionInsts) {
		for (CmdLineOptionInstance optionInst : optionInsts)
			if (optionInst.getOption().getLongOption().equals(optionName)
					|| optionInst.getOption().getShortOption().equals(optionName))
				return optionInst;
		return null;
	}

	public static boolean isSubOption(CmdLineOptionInstance specifiedOption, CmdLineOption option) {
		if (specifiedOption.isGroup()) {
			for (GroupCmdLineOption.SubOption subOption : ((GroupCmdLineOption) specifiedOption.getOption()).getSubOptions()) {
				if (subOption.getOption().equals(option)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Set<CmdLineOptionInstance> findPerformAndQuitOptions(
			Set<CmdLineOptionInstance> options) {
		HashSet<CmdLineOptionInstance> performAndQuitOptions = new HashSet<CmdLineOptionInstance>();
		for (CmdLineOptionInstance option : options) {
			if (isPerformAndQuitOption(option.getOption())) {
				performAndQuitOptions.add(option);
			}
		}
		return performAndQuitOptions;
	}

	public static boolean isPerformAndQuitOption(CmdLineOption option) {
		if (option instanceof AdvancedCmdLineOption) {
			return ((AdvancedCmdLineOption) option).isPerformAndQuit();
		}
		return false;
	}

	public static CmdLineOptionInstance findSpecifiedOption(CmdLineOption option, Set<CmdLineOptionInstance> specifiedOptions) {
		Validate.notNull(option);
		Validate.notNull(specifiedOptions);

		for (CmdLineOptionInstance specifiedOption : specifiedOptions) {
			if (specifiedOption.getOption().equals(option)) {
				return specifiedOption;
			}
		}
		return null;
	}

	public static boolean isActionOption(CmdLineOption option) {
		return option instanceof ActionCmdLineOption;
	}

	public static ActionCmdLineOption findActionOption(Set<CmdLineOption> options) {
		for (CmdLineOption option : options) {
			if (isActionOption(option)) {
				return (ActionCmdLineOption) option;
			}
		}
		return null;
	}

	public static CmdLineOptionInstance findSpecifiedActionOption(Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			if (isActionOption(option.getOption())) {
				return option;
			}
		}
		return null;
	}

	public static boolean isHelpOption(CmdLineOption option) {
		return option instanceof HelpCmdLineOption;
	}

	public static HelpCmdLineOption findHelpOption(Set<CmdLineOption> options) {
		for (CmdLineOption option : options) {
			if (isHelpOption(option)) {
				return (HelpCmdLineOption) option;
			}
		}
		return null;
	}

	public static CmdLineOptionInstance findSpecifiedHelpOption(
			Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			if (isHelpOption(option.getOption())) {
				return option;
			}
		}
		return null;
	}

	public static CmdLineAction findAction(CmdLineOptionInstance actionOption,
			Set<CmdLineAction> supportedActions) {
		Validate.isTrue(actionOption.isAction());
		Validate.notEmpty(actionOption.getValues());

		String actionName = actionOption.getValues().get(0);
		for (CmdLineAction action : supportedActions) {
			if (action.getName().equals(actionName)) {
				return action;
			}
		}
		return null;
	}

	public static String getFormattedString(String string, int startIndex,
			int endIndex) {
		StringBuffer outputString = new StringBuffer("");
		String[] splitStrings = StringUtils.split(string, " ");
		StringBuffer curLine = null;
		for (int i = 0; i < splitStrings.length; i++) {
			curLine = new StringBuffer("");
			curLine.append(splitStrings[i] + " ");

			for (; i + 1 < splitStrings.length
					&& curLine.length() + splitStrings[i + 1].length() <= (endIndex - startIndex); i++)
				curLine.append(splitStrings[i + 1] + " ");

			outputString.append(StringUtils.repeat(" ", startIndex)
					+ curLine.toString() + "\n");
		}
		return outputString.toString();
	}
}
