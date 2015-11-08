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
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.resource.util.JobBuilder;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A tool to submit {@link Job} files generated in a particular directory to the
 * Resource Manager.
 * </p>.
 */
public final class JobSubmitter {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(JobSubmitter.class
            .getName());

    /* our res mgr client */
    private XmlRpcResourceManagerClient client = null;

    /* our job file filter */
    private static final FileFilter JOB_FILE_FILTER = new FileFilter() {

        public boolean accept(File file) {
            return file.isFile() && file.getName().endsWith(".xml");
        }

    };

    public JobSubmitter(URL rUrl) {
        client = new XmlRpcResourceManagerClient(rUrl);
    }

    public void submitJobFiles(File jobFileDir) {
        File[] jobFiles = jobFileDir.listFiles(JOB_FILE_FILTER);

        if (jobFiles != null && jobFiles.length > 0) {
            for (File jobFile : jobFiles) {
                try {
                    String id = submitJobFile(jobFile);
                    LOG.log(Level.INFO, "Job Submitted: id: [" + id + "]");

                } catch (Exception e) {
                    LOG.log(Level.SEVERE, e.getMessage());
                    LOG.log(Level.WARNING, "Exception submitting job file: ["
                                           + jobFile + "]: Message: " + e.getMessage());
                }
            }
        }

    }

    public void submitJobFiles(String jobFileDirPath) {
        submitJobFiles(new File(jobFileDirPath));
    }

    public String submitJobFile(File jobFile) throws JobExecutionException {
        return submitJobFile(jobFile.getAbsolutePath());
    }

    public String submitJobFile(String jobFilePath)
            throws JobExecutionException {
        JobSpec spec = JobBuilder.buildJobSpec(jobFilePath);
        return submitJob(spec);
    }

    public static void main(String[] args) throws MalformedURLException, JobExecutionException {
        String resMgrUrlStr = null;
        String jobFilePath = null, jobFileDirPath = null;
        String usage = "JobSubmitter --rUrl <resource mgr url> [options]\n"
                + "--file <job file path>\n" + "[--dir <job file dir path>]\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--rUrl")) {
                resMgrUrlStr = args[++i];
            } else if (args[i].equals("--file")) {
                jobFilePath = args[++i];
            } else if (args[i].equals("--dir")) {
                jobFileDirPath = args[++i];
            }
        }

        if (resMgrUrlStr == null
                || (jobFilePath == null && jobFileDirPath == null)) {
            System.err.println(usage);
            System.exit(1);
        }

        JobSubmitter submitter = new JobSubmitter(new URL(resMgrUrlStr));

        // if they specified --dir it takes precedence
        if (jobFileDirPath != null) {
            submitter.submitJobFiles(jobFileDirPath);
        } else {
            String jobId = submitter.submitJobFile(jobFilePath);
            LOG.log(Level.INFO, "Job Submitted: id: [" + jobId + "]");
        }

    }

    private String submitJob(JobSpec spec) throws JobExecutionException {
        return client.submitJob(spec.getJob(), spec.getIn());
    }

}
