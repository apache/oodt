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

//OODT imports
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;

//APACHE imports
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

//JDK imports
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A {@link Runnable} proxy to an XmlRpcBatchStub that allows the call to the
 * XmlRpcBatchStub to be asynchronous rather than synchronous. This allows a
 * {@link org.apache.oodt.cas.resource.scheduler.Scheduler} that calls the {@link XmlRpcBatchMgr} to not be stuck
 * waiting for each job to complete before scheduling the next {@link org.apache.oodt.cas.resource.structs.Job}
 * </p>.
 */
public class XmlRpcBatchMgrProxy extends Thread implements Runnable {

	private static final Logger LOG = Logger.getLogger(XmlRpcBatchMgrProxy.class.getName());
	
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

        boolean alive;

        try {
            alive = (Boolean) client.execute("batchstub.isAlive", argList);
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

        boolean result;
        try {
            result = (Boolean) client.execute("batchstub.killJob", argList);
        } catch (XmlRpcException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            result = false;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
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

        boolean result;
        try {
            parent.jobExecuting(jobSpec);
            result = (Boolean) client
                .execute("batchstub.executeJob", argList);
            if (result) {
                parent.jobSuccess(jobSpec);
            } else {
                throw new Exception("batchstub.executeJob returned false");
            }
        } catch (Exception e) {
        	LOG.log(Level.SEVERE, "Job execution failed for jobId '" + jobSpec.getJob().getId() + "' : " + e.getMessage(), e);
            parent.jobFailure(jobSpec);
        }finally {
            parent.notifyMonitor(remoteHost, jobSpec);
        }

   }

}
