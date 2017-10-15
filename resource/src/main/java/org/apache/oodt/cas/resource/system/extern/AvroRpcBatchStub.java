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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.oodt.cas.resource.structs.AvroTypeFactory;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobInstance;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroIntrBatchmgr;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroJob;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroJobInput;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobException;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;
import org.apache.xmlrpc.WebServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvroRpcBatchStub implements AvroIntrBatchmgr {

    /* the port to run the XML RPC web server on, default is 2000 */
    private int port = 2000;

    /* our avro rpc web server */
    Server server;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(AvroRpcBatchStub.class
            .getName());

    private static Map jobThreadMap = null;

    public AvroRpcBatchStub(int port) throws Exception {


        this.port = port;

        // start up the web server
        server = new NettyServer(new SpecificResponder(AvroIntrBatchmgr.class,this), new InetSocketAddress(this.port));
        server.start();

        jobThreadMap = new HashMap();

        LOG.log(Level.INFO, "AvroRpc Batch Stub started by "
                + System.getProperty("user.name", "unknown"));
    }
    
    @Override
    public List getJobsOnNode(String nodeId) {
      Vector<String> jobIds = new Vector();
      
      if(this.jobThreadMap.size() > 0){
            for (Object o : this.jobThreadMap.keySet()) {
                String jobId = (String) o;
                jobIds.addElement(jobId);
            }
      }
      
      Collections.sort(jobIds); // sort the list to return as a courtesy to the user
      return jobIds;
    }

    @Override
    public boolean isAlive() throws AvroRemoteException {
        return true;
    }

    @Override
    public boolean executeJob(AvroJob avroJob, AvroJobInput jobInput) throws AvroRemoteException {
        try {
            return genericExecuteJob(avroJob,jobInput);
        } catch (JobException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public boolean killJob(AvroJob jobHash) throws AvroRemoteException {
        Job job = AvroTypeFactory.getJob(jobHash);
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

    private boolean genericExecuteJob(AvroJob avroJob, AvroJobInput jobInput)
            throws JobException {
        JobInstance exec = null;
        JobInput in = null;
        try {
            Job job = AvroTypeFactory.getJob(avroJob);

            LOG.log(Level.INFO, "stub attempting to execute class: ["
                    + job.getJobInstanceClassName() + "]");

            exec = GenericResourceManagerObjectFactory
                    .getJobInstanceFromClassName(job.getJobInstanceClassName());
            in = AvroTypeFactory.getJobInput(jobInput);
            // load the input obj
            //

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
                    if (endThread != null)
                        endThread = null;
                }
                return false;
            }

            synchronized (jobThreadMap) {
                Thread endThread = (Thread) jobThreadMap.get(job.getId());
                if (endThread != null)
                    endThread = null;
            }

            return runner.wasSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
                e.printStackTrace();
                this.successful = false;
            }

        }

        public boolean wasSuccessful() {
            return this.successful;
        }
    }

    public static void main(String[] args) throws Exception {
        int portNum = -1;
        String usage = "AvroRpcBatchStub --portNum <port number for xml rpc service>\n";

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

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }


}
