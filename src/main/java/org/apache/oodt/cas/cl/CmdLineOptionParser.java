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

package org.apache.oodt.cas.cl;

//JDK imports
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.help.OptionHelpException;
import org.apache.oodt.cas.cl.help.presenter.CmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.formatter.CmdLineOptionHelpFormatter;
import org.apache.oodt.cas.cl.help.formatter.StdCmdLineOptionHelpFormatter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionParser {

	private Set<CmdLineOption> validOptions;
	private CmdLineOptionHelpPrinter helpPrinter;
	private CmdLineOptionSpecificHelpPrinter specificHelpPrinter;
	private CmdLineOptionHelpFormatter helpFormatter;
	private CmdLineOptionHelpPresenter helpPresenter;

	public CmdLineOptionParser() {
		validOptions = new HashSet<CmdLineOption>();
		helpPrinter = new StdCmdLineOptionHelpPrinter();
		specificHelpPrinter = new StdCmdLineOptionSpecificHelpPrinter();
		helpFormatter = new StdCmdLineOptionHelpFormatter();
		helpPresenter = new StdCmdLineOptionHelpPresenter();
	}

	public Set<CmdLineOption> getValidOptions() {
		return validOptions;
	}

	public void setValidOptions(Set<CmdLineOption> validOptions) {
		this.validOptions = validOptions;
	}

	public CmdLineOptionHelpPrinter getHelpPrinter() {
		return helpPrinter;
	}

	public void setHelpPrinter(CmdLineOptionHelpPrinter helpPrinter) {
		this.helpPrinter = helpPrinter;
	}

	public CmdLineOptionSpecificHelpPrinter getSpecificHelpPrinter() {
		return specificHelpPrinter;
	}

	public void setSpecificHelpPrinter(
			CmdLineOptionSpecificHelpPrinter specificHelpPrinter) {
		this.specificHelpPrinter = specificHelpPrinter;
	}

	public CmdLineOptionHelpFormatter getHelpFormatter() {
		return helpFormatter;
	}

	public void setHelpFormatter(CmdLineOptionHelpFormatter helpFormatter) {
		this.helpFormatter = helpFormatter;
	}

	public CmdLineOptionHelpPresenter getHelpPresenter() {
		return helpPresenter;
	}

	public void setHelpPresenter(CmdLineOptionHelpPresenter helpPresenter) {
		this.helpPresenter = helpPresenter;
	}

	public Set<CmdLineOptionInstance> parse(String[] args) throws IOException {
		HashSet<CmdLineOptionInstance> optionInstances = new HashSet<CmdLineOptionInstance>();
		if (args.length < 1)
			throw new OptionHelpException(
					"Must specify options : type -h or --help for info");

		for (int j = 0; j < args.length; j++) {
			String curArg = args[j];

			// print usage and quit if -h or --help is given
			if (curArg.equals("-h") || curArg.equals("--help")) {
				if (args.length > j + 1) {
					String[] helpArgs = new String[args.length - j - 1];
					System.arraycopy(args, j + 1, helpArgs, 0, helpArgs.length);
					helpPresenter.presentSpecificHelp(helpFormatter.format(
							specificHelpPrinter, validOptions, parse(helpArgs).iterator()
									.next()));
				} else {
					helpPresenter.presentHelp(helpFormatter.format(helpPrinter,
							validOptions));
				}
				System.exit(0);
			}

			if (curArg.startsWith("-")) {
				// check if long or short version was used
				if (curArg.startsWith("--"))
					curArg = curArg.substring(2);
				else
					curArg = curArg.substring(1);

				// check if option is a valid one
				CmdLineOption curOption = CmdLineOptionUtils.getOptionByName(curArg,
						validOptions);
				if (curOption == null)
					throw new IOException("Invalid option '" + curArg + "'");

				// check if option has arguments
				List<String> values = new LinkedList<String>();
				if (curOption.hasArgs()) {
					while (j + 1 < args.length && !args[j + 1].startsWith("-"))
						values.add(args[++j]);
					if (values.size() < 1)
						throw new IOException("Option " + curArg
								+ " should have at least one argument");
				}

				// check if is a perform and quit option
				if (curOption.isPerformAndQuit()) {
					curOption.getHandler().handleOption(curOption, values);
					System.exit(0);
				}

				// add to list of option instances
				optionInstances.add(new CmdLineOptionInstance(curOption, values));

			} else {
				throw new IOException("Argument with no option flag '" + curArg + "'");
			}
		}
		return optionInstances;
	}
}
