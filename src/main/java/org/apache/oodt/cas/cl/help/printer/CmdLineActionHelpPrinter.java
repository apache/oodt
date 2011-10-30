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

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;

/**
 * Help printer responsible for generating help message for using
 * a given {@link CmdLineAction}.
 *
 * @author bfoster (Brian Foster)
 */
public interface CmdLineActionHelpPrinter {

	/**
	 * Should generate help message for action specified by
	 * {@link #CmdLineArgs.getSpecifiedAction()}.
	 *
	 * @param action {@link CmdLineAction} for which help will be generate
	 * @param options Supported {@link CmdLineOption}s.
	 * @return Help message for specified action.
	 */
	public String printHelp(CmdLineAction action, Set<CmdLineOption> options);

}
