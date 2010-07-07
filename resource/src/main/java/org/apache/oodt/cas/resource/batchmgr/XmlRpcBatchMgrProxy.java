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

//JDK imports
import java.io.IOException;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;
import org.apache.oodt.cas.resource.structs.ResourceNode;

//APACHE imports
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A {@link Runnable} proxy to an XmlRpcBatchStub that allows the call to the
 * XmlRpcBatchStub to be asynchronous rather than synchronous. This allows a
 * {@link Scheduler} that calls the {@link XmlRpcBatchMgr} to not be stuck
 * waiting for each job to complete before scheduling the next {@link Job}
 * </p>.
 */
public class XmlRpcBatchMgrProxy extends Thread implements Runnable {

    private JobSpec jobSpec;

    private ResourceNode remoteHost;

    private XmlRpcClient client;

    private XmlRpcBatchMgr parent;

    public XmlRpcBatchMgrProxy(JobSpec jobSpec, ResourceNode remoteHost,
            XmlRpcBatchMgr par) {
        this.jobSpec = jobSpec;
        this.remoteHost = remoteHost;
        this.parent = par;
    }

    public boolean nodeAlive() {
        client = new XmlRpcClient(remoteHost.getIpAddr());
        Vector argList = new Vector();

        boolean alive = false;

        try {
            alive = ((Boolean) client.execute("batchstub.isAlive", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            alive = false;
        } catch (IOException e) {
            alive = false;
        }

        return alive;

    }

    public boolean killJob() {
        client = new XmlRpcClient(remoteHost.getIpAddr());
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(jobSpec.getJob()));

        boolean result = false;
        try {
            result = ((Boolean) client.execute("batchstub.killJob", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        if (result) {
            parent.jobKilled(jobSpec);
        }

        return result;
    }

    public void run() {
        client = new XmlRpcClient(remoteHost.getIpAddr());
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(jobSpec.getJob()));
        argList.add(jobSpec.getIn().write());

        boolean result = false;
        try {
            parent.jobExecuting(jobSpec);
            result = ((Boolean) client
                    .execute("batchstub.executeJob", argList)).booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            // throw new JobExecutionException(e);
        } catch (IOException e) {
            e.printStackTrace();
            // throw new JobExecutionException(e);
        }

        // notify the monitor job has finished;
        parent.notifyMonitor(remoteHost, jobSpec);

        // notify the job repository that the job has finished
        parent.jobComplete(jobSpec);
    }

}
