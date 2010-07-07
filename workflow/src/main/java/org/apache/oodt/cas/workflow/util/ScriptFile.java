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

//JDK imports
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A script file represents a set of commands (which are just
 * <code>String</code>s) to be run through a command shell.
 * </p>
 * 
 */
public class ScriptFile {

    private String commandShell = null;

    private List commands = null;

    /**
     * 
     */
    public ScriptFile() {
        commandShell = "/bin/sh";
        commands = new Vector();
    }

    public ScriptFile(String shell) {
        commandShell = shell;
        commands = new Vector();
    }

    public ScriptFile(String shell, List cmds) {
        commandShell = shell;
        commands = cmds;
    }

    /**
     * @return Returns the commands.
     */
    public List getCommands() {
        return commands;
    }

    /**
     * @param commands
     *            The commands to set.
     */
    public void setCommands(List commands) {
        this.commands = commands;
    }

    /**
     * @return Returns the commandShell.
     */
    public String getCommandShell() {
        return commandShell;
    }

    /**
     * @param commandShell
     *            The commandShell to set.
     */
    public void setCommandShell(String commandShell) {
        this.commandShell = commandShell;
    }

    public String toString() {
        String rStr = "";

        rStr += "#!" + commandShell + "\n";

        for (Iterator i = commands.iterator(); i.hasNext();) {
            String cmd = (String) i.next();
            rStr += cmd + "\n";
        }

        return rStr;
    }

    public void writeScriptFile(String filePath) throws Exception {
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(filePath))));
            pw.println(toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error writing script file!: " + e.getMessage());
        } finally {
            try {
                pw.close();
                pw = null;
            } catch (Exception ignore) {
            }

        }

    }

}
