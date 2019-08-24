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

package org.apache.oodt.cas.resource.batchmgr;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.resource.structs.AvroTypeFactory;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.system.extern.AvroRpcBatchStub;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvroRpcBatchMgrProxy extends Thread implements Runnable {

    private static final Logger LOG = Logger.getLogger(XmlRpcBatchMgrProxy.class.getName());

    private JobSpec jobSpec;

    private ResourceNode remoteHost;

    private transient Transceiver client;

    private transient AvroRpcBatchStub proxy;

    private AvroRpcBatchMgr parent;

    public AvroRpcBatchMgrProxy(JobSpec jobSpec, ResourceNode remoteHost,
                               AvroRpcBatchMgr par) {
        this.jobSpec = jobSpec;
        this.remoteHost = remoteHost;
        this.parent = par;
    }

    public boolean nodeAlive() {

        try {
            this.client = new NettyTransceiver(new InetSocketAddress(remoteHost.getIpAddr().getHost(), remoteHost.getIpAddr().getPort()));
            this.proxy = (AvroRpcBatchStub) SpecificRequestor.getClient(AvroRpcBatchStub.class, client);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Failed connection with the server.", e);
        }



        boolean alive = false;

        try {
            alive = proxy.isAlive();
        } catch (AvroRemoteException e) {
            alive = false;
        }
        return alive;

    }

    public boolean killJob() {

        try {
            this.client = new NettyTransceiver(new InetSocketAddress(remoteHost.getIpAddr().getHost(), remoteHost.getIpAddr().getPort()));
            this.proxy = (AvroRpcBatchStub) SpecificRequestor.getClient(AvroRpcBatchStub.class, client);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed connection with the server.", e);
        }


        boolean result = false;
        try {
            result = proxy.killJob(AvroTypeFactory.getAvroJob(jobSpec.getJob()));
        } catch (AvroRemoteException e) {
            e.printStackTrace();
            result = false;
        }

        if (result) {
            parent.jobKilled(jobSpec);
        }

        return result;
    }

    public void run() {
        try {
            this.client = new NettyTransceiver(new InetSocketAddress(remoteHost.getIpAddr().getHost(), remoteHost.getIpAddr().getPort()));
            this.proxy = (AvroRpcBatchStub) SpecificRequestor.getClient(AvroRpcBatchStub.class, client);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed connection with the server.", e);
        }

        boolean result = false;
        try {
            parent.jobExecuting(jobSpec);
            result = proxy.executeJob(AvroTypeFactory.getAvroJob(jobSpec.getJob()),
                    AvroTypeFactory.getAvroJobInput(jobSpec.getIn()));
            if (result)
                parent.jobSuccess(jobSpec);
            else
                throw new Exception("batchstub.executeJob returned false");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Job execution failed for jobId '" + jobSpec.getJob().getId() + "' : " + e.getMessage(), e);
            parent.jobFailure(jobSpec);
        }finally {
            parent.notifyMonitor(remoteHost, jobSpec);
        }

    }






}
