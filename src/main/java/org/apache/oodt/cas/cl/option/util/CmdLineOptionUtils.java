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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.oodt.cas.cl.option.GroupCmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.HandleableCmdLineOption;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.ValidatableCmdLineOption;
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.formatter.StdCmdLineOptionHelpFormatter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.option.require.RequirementRule;
import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;
import org.apache.oodt.cas.cl.parser.CmdLineOptionParser;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionUtils {

	private CmdLineOptionUtils() {}

//	public static <T> Set<CmdLineOption<?>> getOptionallyRequiredOptions(
//			Set<CmdLineOption<?>> options, CmdLineOptionInstance<T> specifiedOption) {
//		HashSet<CmdLineOption<?>> optionalOptions = new HashSet<CmdLineOption<?>>();
//		for (CmdLineOption<T> option : (Set<CmdLineOption<T>>)(Set<?>) options) {
//			if (isOptionallyRequiredOption(option, specifiedOption)) {
//				optionalOptions.add(option);
//			}
//		}
//		return optionalOptions;
//	}
//
//	public static <T> boolean isOptionallyRequiredOption(CmdLineOption<T> option,
//			CmdLineAction selectedAction) {
//		if (!isConditionallyRequired(option, selectedAction)) {
//			if (option.hasHandler()) {
//				return option.getHandler().isInterested(selectedAction);
//			}
//		}
//		return false;
//	}

//	public static Set<CmdLineOption<?>> getConditionallyRequiredOptions(
//			Set<CmdLineOption<?>> options, CmdLineOptionInstance<?> specifiedOption) {
//		HashSet<CmdLineOption<?>> requiredOptions = new HashSet<CmdLineOption<?>>();
//		for (CmdLineOption<?> option : options) {
//			if (isConditionallyRequired(option, specifiedOption)) {
//				requiredOptions.add(option);
//			}
//		}
//		return requiredOptions;
//	}

	public static Set<CmdLineOption> getRequiredOptions(CmdLineAction action,
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

		for (RequirementRule requirementRule : option.getRequirementRules()) {
			if (requirementRule.isRequired(action)) {
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
//
//	public static Set<CmdLineOption<?>> getRequiredOptions(
//			Set<CmdLineOption<?>> supportedOptions,
//			Set<CmdLineOptionInstance<?>> specifiedOptions) {
//		Set<CmdLineOption<?>> reqOptions = getAlwaysRequiredOptions(supportedOptions);
//		reqOptions.addAll(getConditionallyRequiredOptions(supportedOptions,
//				specifiedOptions));
//		return reqOptions;
//	}

//	public static Set<CmdLineOption<?>> getRequiredOptionsNotSet(
//			Set<CmdLineOption<?>> supportedOptions, Set<CmdLineOptionInstance<?>> setOptions) {
//		return getOptionsNotSet(getRequiredOptions(supportedOptions, setOptions),
//				setOptions);
//	}

//	public static Set<CmdLineOption<?>> getOptionsNotSet(
//			Set<CmdLineOption<?>> expectedOptions, Set<CmdLineOptionInstance<?>> setOptions) {
//		HashSet<CmdLineOption<?>> nonSetRequiredOptions = new HashSet<CmdLineOption<?>>();
//		TOP:
//		for (CmdLineOption<?> reqOption : expectedOptions) {
//			for (CmdLineOptionInstance<?> optionInst : setOptions) {
//				if (reqOption.equals(optionInst.getOption()))
//					continue TOP;
//			}
//			nonSetRequiredOptions.add(reqOption);
//		}
//		return nonSetRequiredOptions;
//	}

	public static List<CmdLineOption> sortOptionsByRequiredStatus(Set<CmdLineOption> options) {
		ArrayList<CmdLineOption> optionsList = new ArrayList<CmdLineOption>(options);
		Collections.sort(optionsList);
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

	public static Set<CmdLineOptionInstance> parseArgs(Set<CmdLineOption> supportedOptions, String[] args) throws IOException {
		CmdLineOptionParser parser = new StdCmdLineOptionParser();
		return parser.parse(args, supportedOptions);
	}

	public static Set<CmdLineOptionInstance> validateOptions(Set<CmdLineOptionInstance> options) throws IOException {
		Validate.notNull(options);

		HashSet<CmdLineOptionInstance> optionsFailed = new HashSet<CmdLineOptionInstance>();
		for (CmdLineOptionInstance optionInst : options) {
			if (!validateOption(optionInst)) {
				optionsFailed.add(optionInst);
			}
		}
		return optionsFailed;
	}

	public static boolean validateOption(CmdLineOptionInstance option) {
		if (isValidatable(option.getOption())) {
			for (CmdLineOptionValidator validator : ((ValidatableCmdLineOption) option.getOption()).getValidators()) {
				if (!validator.validate(option)) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isValidatable(CmdLineOption option) {
		return option instanceof ValidatableCmdLineOption;
	}

	public static void handleOptions(CmdLineAction action, Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			handleOption(action, option);
		}
	}

	public static void handleOption(CmdLineAction action, CmdLineOptionInstance option) {
		if (isHandleable(option.getOption())) {
			((HandleableCmdLineOption) option.getOption()).getHandler().handleOption(action, option);
		}
	}

	public static boolean isHandleable(CmdLineOption option) {
		return option instanceof HandleableCmdLineOption;
	}

	public static void validateAndHandleInstances(Set<CmdLineOption> supportedOptions,
			Set<CmdLineOptionInstance> specifiedOptions) throws IOException {


		// check that required args have been specified
		Set<CmdLineOption> requiredOptions = getRequiredOptions();
		Set<CmdLineOption> unsetReqOptions = getRequiredOptionsNotSet(supportedOptions, specifiedOptions);
		if (!unsetReqOptions.isEmpty()) {
			throw new IOException("Miss required options: " + sortOptionsByRequiredStatus(unsetReqOptions));
		}

		// validate options
		Set<CmdLineOptionInstance> optionsFailedVal = validateOptions(specifiedOptions);
		if (!optionsFailedVal.isEmpty()) {
			throw new IOException("Options failed validations: " + optionsFailedVal);
		}

		// Check for perform and quit options and handle them if specified then return.
		Set<CmdLineOptionInstance> performAndQuitOptions = findPerformAndQuitOptions(specifiedOptions);
		if (!performAndQuitOptions.isEmpty()) {
			handleOptions(performAndQuitOptions);
			return;
		}

		// if all looks good . . . handle options
		handleOptions(specifiedOptions);
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
	}

	public static CmdLineOptionInstance findSpecifiedActionOption(Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			if (isActionOption(option.getOption())) {
				return option;
			}
		}
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

	public static void printHelp(Set<CmdLineOption> options) {
		new StdCmdLineOptionHelpPresenter()
				.presentHelp(new StdCmdLineOptionHelpFormatter().format(
						new StdCmdLineOptionHelpPrinter(), options));
	}

	public static void printSpecificHelp(Set<CmdLineOption> options,
			CmdLineOptionInstance specifiedOption) {
		new StdCmdLineOptionHelpPresenter()
				.presentSpecificHelp(new StdCmdLineOptionHelpFormatter()
						.format(new StdCmdLineOptionSpecificHelpPrinter(), options,
								specifiedOption));
	}

//	public static <T> Iterable<CmdLineOption<T>> safeCast(Iterable<CmdLineOption<?>> options) {
//		return new GenericsSafeIterable<CmdLineOption<T>>(options);
//	}
//
//	public static <T> List<T> iterableToList(Iterable<T> iterable) {
//		ArrayList<T> safeItems = new ArrayList<T>();
//		for (T item : iterable) {
//			safeItems.add(item);
//		}
//		return safeItems;
//	}
//
//	public static <T> HashSet<T> iterableToSet(Iterable<T> iterable) {
//		HashSet<T> safeItems = new HashSet<T>();
//		for (T item : iterable) {
//			safeItems.add(item);
//		}
//		return safeItems;
//	}

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
