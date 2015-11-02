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


package org.apache.oodt.cas.workflow.util;

import org.apache.oodt.cas.workflow.exceptions.WorkflowException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author riverma (Rishi Verma)
 * @version $Revision$
 * 
 * <p>
 * A script file representing a set of commands (which are just
 * <code>String</code>s) to be run through a command shell in Windows cygwin. 
 * </p>
 * 
 */
public class CygwinScriptFile extends ScriptFile {
    private static Logger LOG = Logger.getLogger(CygwinScriptFile.class.getName());
    /**
     * 
     */
    public CygwinScriptFile() {
        super();
    }

    public CygwinScriptFile(String shell) {
        super(shell);
    }

    public CygwinScriptFile(String shell, List cmds) {
        super(shell, cmds);
    }		
	
	@Override
	/**
	 * Override writeScriptFile to ensure Windows (cygwin) compatibility for generated scripts
	 */
    public void writeScriptFile(String filePath) throws WorkflowException {
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(filePath))));
            pw.print(toString()); // Changed println to print for Cygwin compatibility
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new WorkflowException("Error writing script file!: " + e.getMessage());
        } finally {
            try {
                pw.close();
            } catch (Exception ignore) {
            }

        }

    }	
	
}
