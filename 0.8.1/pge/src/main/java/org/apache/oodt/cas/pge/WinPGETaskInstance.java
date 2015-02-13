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


package org.apache.oodt.cas.pge;

import org.apache.oodt.cas.workflow.util.CygwinScriptFile;
import org.apache.oodt.cas.workflow.util.ScriptFile;

/**
 * Runs a CAS-style Product Generation Executive based on the PCS Wrapper
 * Architecture from mattmann et al. on OCO specifically on a Windows Cygwin-running
 * machine
 *
 * @author riverma (Rishi Verma)
 */
public class WinPGETaskInstance extends PGETaskInstance {

	  /**
	   * Override buildPgeRunScript method to call a windows specific script file
	   */
	  @Override
	  protected ScriptFile buildPgeRunScript() {
		  CygwinScriptFile sf = new CygwinScriptFile(this.pgeConfig.getShellType());
	      sf.setCommands(this.pgeConfig.getExeCmds());
	   
	      return sf;
	  }	
	
}
