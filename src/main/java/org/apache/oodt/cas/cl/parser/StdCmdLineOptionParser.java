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
package org.apache.oodt.cas.cl.parser;

//JDK imports
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findHelpOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.getOptionByName;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.isSubOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

//OODT imports
import org.apache.oodt.cas.cl.help.OptionHelpException;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;

/**
 * @author bfoster
 * @version $Revision$
 */
public class StdCmdLineOptionParser extends CmdLineOptionParser {

	public Set<CmdLineOptionInstance> parse(Args args,
			Set<CmdLineOption> validOptions) throws IOException {
		HashSet<CmdLineOptionInstance> optionInstances = new HashSet<CmdLineOptionInstance>();

		CmdLineOption helpOption = findHelpOption(validOptions);
		if (helpOption == null) {
			throw new OptionHelpException(
					"Must specify a help option in set of valid options");
		}

		if (args.numArgs() < 1) {
			throw new OptionHelpException("Must specify options : type -"
					+ helpOption.getShortOption() + " or --" + helpOption.getLongOption()
					+ " for info");
		}

		Stack<CmdLineOptionInstance> groupOptions = new Stack<CmdLineOptionInstance>();
		for (String arg : args) {

			if (isOption(arg)) {

				// check if option is a valid one
				CmdLineOption option = getOptionByName(getOptionName(arg), validOptions);
				if (option == null) {
					throw new IOException("Invalid option: '" + arg + "'");
				}

				args.incrementIndex();
				if (option instanceof GroupCmdLineOption) {
					CmdLineOptionInstance groupInstance = new CmdLineOptionInstance();
					groupInstance.setOption(option);

					// Check if we are currently loading subOptions.
					if (!groupOptions.isEmpty()) {

						CmdLineOptionInstance currentGroup = groupOptions.peek();

						// Verify option is a valid subOption for current group.
						if (!isSubOption(currentGroup, option)) {
							throw new IOException("Option " + option + " is not a subOption for " + currentGroup.getOption());
						}

						// Add option to current group values.
						currentGroup.addSubOption(groupInstance);
					}

					// Push group as current group.
					groupOptions.push(groupInstance);
				} else if (option.hasArgs()) {
					List<String> values = getValues(args, option);
					if (values.isEmpty()) {
						throw new IOException("Option " + option
								+ " requires argument values");
					}
					CmdLineOptionInstance specifiedOption = new CmdLineOptionInstance();
					specifiedOption.setOption(option);
					specifiedOption.setValues(values);

					// Check if we are currently loading subOptions.
					if (!groupOptions.isEmpty()) {

						CmdLineOptionInstance currentGroup = groupOptions.peek();

						// Verify option is a valid subOption for current group.
						if (!isSubOption(currentGroup, option)) {
							throw new IOException("Option " + option + " is not a subOption for " + currentGroup.getOption());
						}

						// Add option to current group values.
						currentGroup.addSubOption(specifiedOption);
					} else {
						optionInstances.add(specifiedOption);
					}
				}
			} else {
				throw new IOException("Invalid argument: '" + arg + "'");
			}
		}
		return optionInstances;
	}

	private List<String> getValues(Args args, CmdLineOption option) {
		List<String> values = new ArrayList<String>();
		String nextValue = args.getCurrentArg();
		while (!isOption(nextValue)) {
			values.add(nextValue);
		}
		return values;
	}

//	private List<CmdLineOptionInstance<?>> findOptions(Args args,
//			Set<CmdLineSubOption> subOptions) {
//		HashSet<CmdLineOption<?>> allOptions = new HashSet<CmdLineOption<?>>();
//		HashSet<CmdLineOption<?>> requiredOptions = new HashSet<CmdLineOption<?>>();
//		for (CmdLineSubOption subOption : subOptions) {
//			allOptions.add(subOption.getOption());
//			if (subOption.isRequired()) {
//				requiredOptions.add(subOption.getOption());
//			}
//		}
//
//		HashSet<CmdLineOption<?>> setOptions = new HashSet<CmdLineOption<?>>();
//		for (String arg : args) {
//			if (isOption(arg)) {
//				CmdLineOption<?> option = CmdLineOptionUtils.getOptionByName(
//						getOptionName(arg), allOptions);
//				
//			}
//		}
//		return null;
//	}

	private static boolean isOption(String arg) {
		return (arg.startsWith("-"));
	}

	private static String getOptionName(String arg) {
		if (arg.startsWith("-")) {
			return arg.substring(1);
		} else if (arg.startsWith("--")) {
			return arg.substring(2);
		} else {
			return null;
		}
	}

//	private CmdLineOption<?> getHelpOption(Set<CmdLineOption<?>> options) {
//		for (CmdLineOption<?> option : options) {
//			if (option instanceof GroupCmdLineOption) {
//				if (((GroupCmdLineOption) option).isHelp()) {
//					return option;
//				}
//			}
//		}
//		return null;
//	}
}
