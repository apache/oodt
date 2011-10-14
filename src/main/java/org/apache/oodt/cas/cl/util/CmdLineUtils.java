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

package org.apache.oodt.cas.cl.util;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.apache.oodt.cas.cl.option.HandleableCmdLineOption;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cl.option.ValidatableCmdLineOption;
import org.apache.oodt.cas.cl.option.require.RequirementRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;
import org.apache.oodt.cas.cl.action.CmdLineAction;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineUtils {

	private CmdLineUtils() {}

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
			if (!(option instanceof ActionCmdLineOption) && option.isRequired()) {
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

	public static boolean isPrintSupportedActionsOption(CmdLineOption option) {
		return option instanceof PrintSupportedActionsCmdLineOption;
	}

	public static PrintSupportedActionsCmdLineOption findPrintSupportedActionsOption(Set<CmdLineOption> options) {
		for (CmdLineOption option : options) {
			if (isPrintSupportedActionsOption(option)) {
				return (PrintSupportedActionsCmdLineOption) option;
			}
		}
		return null;
	}

	public static CmdLineOptionInstance findSpecifiedPrintSupportedActionsOption(Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			if (isPrintSupportedActionsOption(option.getOption())) {
				return option;
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

	public static boolean validate(CmdLineOptionInstance option) {
		if (option.isValidatable()) {
			for (CmdLineOptionValidator validator : ((ValidatableCmdLineOption) option.getOption()).getValidators()) {
				if (!validator.validate(option)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void handle(CmdLineAction action, CmdLineOptionInstance option) {
		if (option.isHandleable()) {
			((HandleableCmdLineOption) option.getOption()).getHandler().handleOption(action, option);
		}
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

	public static Object[] convertToType(List<String> values, Class<?> type)
			throws MalformedURLException, ClassNotFoundException {
		if (type.equals(File.class)) {
			List<Object> files = new LinkedList<Object>();
			for (String value : values)
				files.add(new File(value));
			return files.toArray(new Object[files.size()]);
		} else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
			List<Object> booleans = new LinkedList<Object>();
			for (String value : values)
				booleans.add(value.toLowerCase().trim().equals("true"));
			return booleans.toArray(new Object[booleans.size()]);
		} else if (type.equals(URL.class)) {
			List<Object> urls = new LinkedList<Object>();
			for (String value : values)
				urls.add(new URL(value));
			return urls.toArray(new Object[urls.size()]);
		} else if (type.equals(Class.class)) {
			List<Object> classes = new LinkedList<Object>();
			for (String value : values)
				classes.add(Class.forName(value));
			return classes.toArray(new Object[classes.size()]);
		} else if (type.equals(List.class)) {
			return new Object[] { values };
		} else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
			List<Object> ints = new LinkedList<Object>();
			for (String value : values)
				ints.add(new Integer(value));
			return ints.toArray(new Object[ints.size()]);
		} else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
			List<Object> longs = new LinkedList<Object>();
			for (String value : values)
				longs.add(new Long(value));
			return longs.toArray(new Object[longs.size()]);
		} else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
			List<Object> doubles = new LinkedList<Object>();
			for (String value : values)
				doubles.add(new Double(value));
			return doubles.toArray(new Object[doubles.size()]);
		} else if (type.equals(String.class)) {
			StringBuffer combinedString = new StringBuffer("");
			for (String value : values)
				combinedString.append(value + " ");
			return new String[] { combinedString.toString().trim() };
		} else {
			return values.toArray(new Object[values.size()]);
		}
	}
}
