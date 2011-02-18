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
package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.exec.ExecUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Execute some external command as a {@link CrawlerAction} reponse
 * </p>.
 */
public class ExternAction extends CrawlerAction {

	private String executeCommand;
	private String workingDir;
	
	@Override
	public boolean performAction(File product, Metadata productMetadata)
			throws CrawlerActionException {
		String currentExcecuteCommand = this.executeCommand;
		try {
			if (currentExcecuteCommand == null)
				throw new Exception("Must specify execute command");
			return ExecUtils.callProgram(currentExcecuteCommand = PathUtils.doDynamicReplacement(currentExcecuteCommand, productMetadata), LOG, new File(workingDir != null ? workingDir : product.getParent())) == 0;
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to execute extern command '" + currentExcecuteCommand + "' : " + e.getMessage(), e);
			return false;
		}
	}
	
	public void setExecuteCommand(String executeCommand) {
		this.executeCommand = executeCommand;
	}
	
	public void setWorkingDir(String workingDir) throws Exception {
		this.workingDir = PathUtils.doDynamicReplacement(workingDir);
	}

}
