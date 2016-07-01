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


package org.apache.oodt.cas.workflow.examples;

//Java imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

//OODT imports

/**
 * @author davoodi
 * @version $Revsion$
 * @since OODT-226
 * 
 * <p>
 * A task that takes in the static configuration parameter, and metadata
 * &quot;args&quot;, then runs an external script.
 * </p>
 */
public class ExternScriptTaskInstance implements WorkflowTaskInstance {

    /**
     * 
     */
    public ExternScriptTaskInstance() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(java.util.Map,
     *      org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
     */
    public void run(Metadata metadata, WorkflowTaskConfiguration config) {
        List args = metadata.getAllMetadata("Args"); // command line
        // arguments for a
        // specific script.
        String pathToScript = config.getProperty("PathToScript"); // should
        // include
        // the file
        // name as
        // well.
        String shellType = config.getProperty("ShellType"); // e.g. /bin/sh/

        // joining the argument list's elements to a string
        StringBuilder buffer = new StringBuilder();
        Iterator iter = args.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(" ");
            }
        }
        String cmdLine = shellType + " " + pathToScript + " "
                + buffer.toString();

        // executing the external script on the command line
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec(cmdLine);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        InputStream inputstream = null;
        if (proc != null) {
            inputstream = proc.getInputStream();
        }
        InputStreamReader inputstreamreader = null;
        if (inputstream != null) {
            inputstreamreader = new InputStreamReader(inputstream);
        }
        BufferedReader bufferedreader = null;
        if (inputstreamreader != null) {
            bufferedreader = new BufferedReader(inputstreamreader);
        }

        // sending the script's output result to System.out to be printed
        String cmdlnOutput;
        try {
            if (bufferedreader != null) {
                while ((cmdlnOutput = bufferedreader.readLine()) != null) {
                    System.out.println(cmdlnOutput);
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            if (proc != null) {
                if (proc.waitFor() != 0) {
                    System.err
                            .println("the process did not terminate normally exit code: ["
                                     + proc.exitValue() + "]");
                }
            }
        } catch (InterruptedException e) {
            System.err.println(e.getLocalizedMessage());
        } finally {
            try {
                if (bufferedreader != null) {
                    bufferedreader.close();
                }
            } catch (Exception ignore) {
            }

        }

    }
}
