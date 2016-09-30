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


package org.apache.oodt.cas.resource.system.extern;

//JDK imports
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobInstance;
import org.apache.oodt.cas.resource.structs.exceptions.JobException;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;
import org.apache.xmlrpc.WebServer;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
//APACHE imports

/**
 * @author woollard
 * @version $Revision$
 * @deprecated soon be replaced by avro-rpc
 * <p>
 * An XML RPC-based Batch Submission System.
 * </p>
 *
 */
@Deprecated
public class XmlRpcBatchStub {

    /* the port to run the XML RPC web server on, default is 2000 */
    private int webServerPort = 2000;

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(XmlRpcBatchStub.class
        .getName());

    private Map jobThreadMap = null;

    public XmlRpcBatchStub(int port) {
        webServerPort = port;

        // start up the web server
        webServer = new WebServer(webServerPort);
        webServer.addHandler("batchstub", this);
        webServer.start();

        jobThreadMap = new ConcurrentHashMap();

        LOG.log(Level.INFO, "XmlRpc Batch Stub started by "
                            + System.getProperty("user.name", "unknown"));
    }

    public boolean isAlive() {
        return true;
    }

    public boolean executeJob(Map jobHash, Map jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Map jobHash, Date jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Map jobHash, double jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Map jobHash, int jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Map jobHash, boolean jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Map jobHash, Vector jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Map jobHash, byte[] jobInput)
        throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public synchronized boolean killJob(Map jobHash) {
        Job job = XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
        Thread jobThread = (Thread) jobThreadMap.get(job.getId());
        if (jobThread == null) {
            LOG.log(Level.WARNING, "Job: [" + job.getId()
                                   + "] not managed by this batch stub");
            return false;
        }

        // okay, so interrupt it, which should cause it to stop
        jobThread.interrupt();
        return true;
    }

    private boolean genericExecuteJob(Map jobHash, Object jobInput) {
        JobInstance exec = null;
        JobInput in = null;
        try {
            Job job = XmlRpcStructFactory.getJobFromXmlRpc(jobHash);

            LOG.log(Level.INFO, "stub attempting to execute class: ["
                                + job.getJobInstanceClassName() + "]");

            exec = GenericResourceManagerObjectFactory
                .getJobInstanceFromClassName(job.getJobInstanceClassName());
            in = GenericResourceManagerObjectFactory
                .getJobInputFromClassName(job.getJobInputClassName());

            // load the input obj
            in.read(jobInput);

            // create threaded job
            // so that it can be interrupted
            RunnableJob runner = new RunnableJob(exec, in);
            Thread threadRunner = new Thread(runner);
            /* save this job thread in a map so we can kill it later */
            jobThreadMap.put(job.getId(), threadRunner);
            threadRunner.start();

            try {
                threadRunner.join();
            } catch (InterruptedException e) {
                LOG.log(Level.INFO, "Current job: [" + job.getName()
                                    + "]: killed: exiting gracefully");
                synchronized (jobThreadMap) {
                    Thread endThread = (Thread) jobThreadMap.get(job.getId());
                    if (endThread != null) {
                        endThread = null;
                    }
                }
                return false;
            }

            synchronized (jobThreadMap) {
                Thread endThread = (Thread) jobThreadMap.get(job.getId());
                if (endThread != null) {
                    endThread = null;
                }
            }

            return runner.wasSuccessful();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public static void main(String[] args)  {
        int portNum = -1;
        String usage = "XmlRpcBatchStub --portNum <port number for xml rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }

        XmlRpcBatchStub stub = new XmlRpcBatchStub(portNum);

        for (;;) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
        }
    }

    private class RunnableJob implements Runnable {

        private JobInput in;

        private JobInstance job;

        private boolean successful;

        public RunnableJob(JobInstance job, JobInput in) {
            this.job = job;
            this.in = in;
            this.successful = false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                this.successful = job.execute(in);
            } catch (JobInputException e) {
                LOG.log(Level.SEVERE, e.getMessage());
                this.successful = false;
            }

        }

        public boolean wasSuccessful() {
            return this.successful;
        }
    }
}