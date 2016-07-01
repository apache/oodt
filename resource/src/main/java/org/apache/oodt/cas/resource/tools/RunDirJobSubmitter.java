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


package org.apache.oodt.cas.resource.tools;

//JDK imports
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.NameValueJobInput;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.resource.util.JobBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author cecilia
 * @version $Revision$
 * 
 * <p>
 * A tool that creates a job and send it to the Resource Manager. The list of
 * jobs created has a 1-to-1 correspondence to the list of directory names
 * </p>.
 */
public final class RunDirJobSubmitter {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(RunDirJobSubmitter.class
            .getName());

    /* our res mgr client */
    private XmlRpcResourceManagerClient client = null;

    public RunDirJobSubmitter(URL rUrl) {
        client = new XmlRpcResourceManagerClient(rUrl);
    }

    public void submitRunDirJobFile(String jobFname, String inputFname)
            throws JobExecutionException {

        // -------------------------------------------------------------
        // check validity of given job filename and input filename
        // -------------------------------------------------------------

        File jobFile = new File(jobFname);
        if (!jobFile.exists()) {
            // file doesn't exist
            throw new JobExecutionException("RunDirJobSubmitter: input file "
                    + jobFname + " does not exist.");
        } else if (!jobFile.isFile()) {
            // file is a directory
            throw new JobExecutionException("RunDirJobSubmitter: input file "
                    + jobFname + " is not a file.");
        }

        File f = new File(inputFname);
        if (!f.exists()) {
            // file doesn't exist
            throw new JobExecutionException("RunDirJobSubmitter: input file "
                    + inputFname + " does not exist.");
        } else if (!f.isFile()) {
            // file is a directory
            throw new JobExecutionException("RunDirJobSubmitter: input file "
                    + inputFname + " is not a file.");
        }

        // ----------------------------------------------------------------
        // create a default JobSpec
        // ----------------------------------------------------------------
        JobSpec spec = JobBuilder.buildJobSpec(jobFile.getAbsolutePath());
        Job job = spec.getJob();
        NameValueJobInput jobInput = (NameValueJobInput) spec.getIn();

        // ----------------------------------------------------------------
        // open the file to read. traverse through the list of directories
        // name given & override the default Job's runDirName value with the
        // directory name. then submit the Job.
        // ----------------------------------------------------------------

        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFname));
            if (!in.ready()) {
                throw new IOException();
            }

            String line;
            String jobId;
            while ((line = in.readLine()) != null) {

                // overwrite the runDirName
                jobInput.setNameValuePair("runDirName", line);

                jobId = submitJob(job, jobInput);
                LOG.log(Level.INFO, "Job Submitted: id: [" + jobId + "]");
            }

            in.close();
        } catch (IOException e) {
            throw new JobExecutionException("RunDirJobSubmitter: " + e);
        }

    }

    public static void main(String[] args) throws JobExecutionException, MalformedURLException {
        String resMgrUrlStr = null;
        String jobFileName = null;
        String runDirFileName = null;

        String usage = "RunDirJobSubmitter --rUrl <resource mgr url> "
                + "--jobFile <input job file> "
                + "--runDirFile <input running directories file> \n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--rUrl")) {
                resMgrUrlStr = args[++i];
            } else if (args[i].equals("--jobFile")) {
                jobFileName = args[++i];
            } else if (args[i].equals("--runDirFile")) {
                runDirFileName = args[++i];
            }
        }

        if (resMgrUrlStr == null || jobFileName == null
                || runDirFileName == null) {
            System.err.println(usage);
            System.exit(1);
        }

        RunDirJobSubmitter submitter = new RunDirJobSubmitter(new URL(
                resMgrUrlStr));
        submitter.submitRunDirJobFile(jobFileName, runDirFileName);
    }

    private String submitJob(Job job, JobInput jobInput)
            throws JobExecutionException {
        return client.submitJob(job, jobInput);
    }

}
