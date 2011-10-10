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
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.RequirementRule;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.formatter.StdCmdLineOptionHelpFormatter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.CmdLineOptionParser;
import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionUtils {

	private CmdLineOptionUtils() {}

	public static Set<CmdLineOption> getOptionalOptions(
			Set<CmdLineOption> options, CmdLineOptionInstance specifiedOption) {
		HashSet<CmdLineOption> optionalOptions = new HashSet<CmdLineOption>();
		for (CmdLineOption option : options) {
			if (isOptionalOption(option, specifiedOption)) {
				optionalOptions.add(option);
			}
		}
		return optionalOptions;
	}

	public static boolean isOptionalOption(CmdLineOption option,
			CmdLineOptionInstance specifiedOption) {
		return !isConditionallyRequired(option, specifiedOption)
				&& option.getHandler().affectsOption(specifiedOption);
	}

	public static Set<CmdLineOption> getConditionallyRequiredOptions(
			Set<CmdLineOption> options, CmdLineOptionInstance specifiedOption) {
		HashSet<CmdLineOption> requiredOptions = new HashSet<CmdLineOption>();
		for (CmdLineOption option : options) {
			if (isConditionallyRequired(option, specifiedOption)) {
				requiredOptions.add(option);
			}
		}
		return requiredOptions;
	}

	public static Set<CmdLineOption> getConditionallyRequiredOptions(
			Set<CmdLineOption> options, Set<CmdLineOptionInstance> specifiedOptions) {
		HashSet<CmdLineOption> requiredOptions = new HashSet<CmdLineOption>();
		for (CmdLineOption option : options) {
			for (CmdLineOptionInstance specifiedOption : specifiedOptions) {
				if (isConditionallyRequired(option, specifiedOption)) {
					requiredOptions.add(option);
					break;
				}
			}
		}
		return requiredOptions;
	}

	public static boolean isConditionallyRequired(CmdLineOption option,
			CmdLineOptionInstance specifiedOption) {
		Validate.notNull(option);
		Validate.notNull(specifiedOption);

		for (RequirementRule requirementRule : option.getRequirementRules()) {
			if (((requirementRule.isRequireAllValues() && specifiedOption.getValues()
					.containsAll(requirementRule.getOptionValues())) || (!requirementRule
							.isRequireAllValues() && !Collections.disjoint(
									specifiedOption.getValues(), requirementRule.getOptionValues())))) {
				return true;
			}
		}
		return false;
	}

	public static Set<CmdLineOption> getAlwaysRequiredOptions(
			Set<CmdLineOption> options) {
		HashSet<CmdLineOption> requiredOptions = new HashSet<CmdLineOption>();
		for (CmdLineOption option : options) {
			if (option.isRequired()) {
				requiredOptions.add(option);
			}
		}
		return requiredOptions;
	}

	public static Set<CmdLineOption> getRequiredOptions(
			Set<CmdLineOption> supportedOptions,
			Set<CmdLineOptionInstance> specifiedOptions) {
		Set<CmdLineOption> reqOptions = getAlwaysRequiredOptions(supportedOptions);
		reqOptions.addAll(getConditionallyRequiredOptions(supportedOptions,
				specifiedOptions));
		return reqOptions;
	}

	public static Set<CmdLineOption> getRequiredOptionsNotSet(
			Set<CmdLineOption> supportedOptions, Set<CmdLineOptionInstance> setOptions) {
		return getOptionsNotSet(getRequiredOptions(supportedOptions, setOptions),
				setOptions);
	}

	public static Set<CmdLineOption> getOptionsNotSet(
			Set<CmdLineOption> expectedOptions, Set<CmdLineOptionInstance> setOptions) {
		HashSet<CmdLineOption> nonSetRequiredOptions = new HashSet<CmdLineOption>();
		TOP:
		for (CmdLineOption reqOption : expectedOptions) {
			for (CmdLineOptionInstance optionInst : setOptions) {
				if (reqOption.equals(optionInst.getOption()))
					continue TOP;
			}
			nonSetRequiredOptions.add(reqOption);
		}
		return nonSetRequiredOptions;
	}

	public static List<CmdLineOption> sortOptionsByRequiredStatus(Set<CmdLineOption> options) {
		ArrayList<CmdLineOption> optionsList = new ArrayList<CmdLineOption>(options);
		Collections.sort(optionsList);
		Collections.reverse(optionsList);
		return optionsList;
	}
	
	public static CmdLineOption getOptionByName(String optionName,
			Set<CmdLineOption> options) {
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

	public static List<String> getOptionValues(String optionName,
			Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance optionInst : options)
			if (optionInst.getOption().getLongOption().equals(optionName)
					|| optionInst.getOption().getShortOption().equals(optionName))
				return optionInst.getValues();
		return null;
	}


	public static Set<CmdLineOptionInstance> parseArgs(Set<CmdLineOption> supportedOptions, String[] args) throws IOException {
		CmdLineOptionParser parser = new CmdLineOptionParser();
		parser.setValidOptions(supportedOptions);
		return parser.parse(args);
	}

	public static Set<CmdLineOptionInstance> validateOptions(Set<CmdLineOptionInstance> options) throws IOException {
		Validate.notNull(options);

		HashSet<CmdLineOptionInstance> optionsFailed = new HashSet<CmdLineOptionInstance>();
		for (CmdLineOptionInstance optionInst : options) {
			for (CmdLineOptionValidator validator : optionInst.getOption()
					.getValidators())
				if (!validator.validate(optionInst)) {
					optionsFailed.add(optionInst);
				}
		}
		return optionsFailed;
	}

	public static void handleOptions(Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			option.getOption().getHandler()
					.handleOption(option.getOption(), option.getValues());
		}
	}

	public static Set<CmdLineOptionInstance> validateAndHandleInstances(
			Set<CmdLineOption> supportedOptions, String[] args) throws IOException {

		// parse args
		Set<CmdLineOptionInstance> setOptions = parseArgs(supportedOptions, args);

		// check that required args have been specified
		Set<CmdLineOption> unsetReqOptions = getRequiredOptionsNotSet(supportedOptions, setOptions);
		if (!unsetReqOptions.isEmpty()) {
			throw new IOException("Miss required options: " + sortOptionsByRequiredStatus(unsetReqOptions));
		}

		// validate options
		Set<CmdLineOptionInstance> optionsFailedVal = validateOptions(setOptions);
		if (!optionsFailedVal.isEmpty()) {
			throw new IOException("Options failed validations: " + optionsFailedVal);
		}

		// if all looks good . . . handle options
		handleOptions(setOptions);

		return setOptions;
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
