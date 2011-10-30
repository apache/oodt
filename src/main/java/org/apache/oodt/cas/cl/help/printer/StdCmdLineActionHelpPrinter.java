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
package org.apache.oodt.cas.cl.help.printer;

//OODT static imports
import static org.apache.oodt.cas.cl.util.CmdLineUtils.determineOptional;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.determineRequired;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;

/**
 * Standard help printer for printing help for a {@link CmdLineAction}.
 *
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineActionHelpPrinter implements
		CmdLineActionHelpPrinter {

	/**
	 * {@inheritDoc}
	 */
	public String printHelp(CmdLineAction action, Set<CmdLineOption> options) {
		StringBuffer sb = new StringBuffer("");
		sb.append(getHeader(action)).append("\n");

		sb.append(getRequiredSubHeader()).append("\n");
		Set<CmdLineOption> requiredOptions = determineRequired(action, options);
		List<CmdLineOption> sortedRequiredOptions = sortOptionsByRequiredStatus(requiredOptions);
		for (CmdLineOption option : sortedRequiredOptions) {
			sb.append(getRequiredOptionHelp(option)).append("\n");
		}

		sb.append(getOptionalSubHeader()).append("\n");
		Set<CmdLineOption> optionalOptions = determineOptional(action, options);
		List<CmdLineOption> sortedOptionalOptions = sortOptionsByRequiredStatus(optionalOptions);
		for (CmdLineOption option : sortedOptionalOptions) {
			sb.append(getOptionalOptionHelp(option)).append("\n");
		}

		sb.append(getFooter(action)).append("\n");
		return sb.toString();
	}

	protected String getHeader(CmdLineAction action) {
		return "Action Help for '" + action.getName() + "'";
	}

	protected String getRequiredSubHeader() {
		return " - Required:";
	}

	protected String getRequiredOptionHelp(CmdLineOption option) {
		return getOptionHelp(option);
	}

	protected String getOptionalSubHeader() {
		return " - Optional:";
	}

	protected String getOptionalOptionHelp(CmdLineOption option) {
		return getOptionHelp(option);
	}

	protected String getFooter(CmdLineAction action) {
		return "";
	}

	protected String getOptionHelp(CmdLineOption option) {
		return "    -" + option.getShortOption() + " [--" + option.getLongOption()
				+ "] "
				+ (option.hasArgs() ? "<" + option.getArgsDescription() + ">" : "");
	}
}
