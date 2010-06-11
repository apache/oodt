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


package gov.nasa.jpl.oodt.cas.resource.system;

//APACHE imports
import org.apache.xmlrpc.CommonsXmlRpcTransportFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

//OODTimports
import gov.nasa.jpl.oodt.cas.resource.structs.Job;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInput;
import gov.nasa.jpl.oodt.cas.resource.structs.JobStatus;
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobExecutionException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.MonitorException;
import gov.nasa.jpl.oodt.cas.resource.util.XmlRpcStructFactory;

//JDK imports
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The XML RPC based resource manager client.
 * </p>
 * 
 */
public class XmlRpcResourceManagerClient {

    /* our xml rpc client */
    private XmlRpcClient client = null;

    /* our log stream */
    private static Logger LOG = Logger
            .getLogger(XmlRpcResourceManagerClient.class.getName());

    /* resource manager url */
    private URL resMgrUrl = null;

    /**
     * <p>
     * Constructs a new XmlRpcResourceManagerClient with the given
     * <code>url</code>.
     * </p>
     * 
     * @param url
     *            The url pointer to the xml rpc resource manager service.
     */
    public XmlRpcResourceManagerClient(URL url) {
        // set up the configuration, if there is any
        if (System.getProperty("gov.nasa.jpl.oodt.cas.resource.properties") != null) {
            String configFile = System
                    .getProperty("gov.nasa.jpl.oodt.cas.resource.properties");
            LOG.log(Level.INFO,
                    "Loading Resource Manager Configuration Properties from: ["
                            + configFile + "]");
            try {
                System.getProperties().load(
                        new FileInputStream(new File(configFile)));
            } catch (Exception e) {
                LOG.log(Level.INFO,
                        "Error loading configuration properties from: ["
                                + configFile + "]");
            }

        }

        CommonsXmlRpcTransportFactory transportFactory = new CommonsXmlRpcTransportFactory(
                url);
        int connectionTimeoutMins = Integer
                .getInteger(
                        "gov.nasa.jpl.oodt.cas.resource.system.xmlrpc.connectionTimeout.minutes",
                        20).intValue();
        int connectionTimeout = connectionTimeoutMins * 60 * 1000;
        int requestTimeoutMins = Integer
                .getInteger(
                        "gov.nasa.jpl.oodt.cas.resource.system.xmlrpc.requestTimeout.minutes",
                        60).intValue();
        int requestTimeout = requestTimeoutMins * 60 * 1000;
        transportFactory.setConnectionTimeout(connectionTimeout);
        transportFactory.setTimeout(requestTimeout);
        client = new XmlRpcClient(url, transportFactory);
        resMgrUrl = url;
    }

    public static void main(String[] args) throws Exception {

        String getNodeByIdOperation = "--getNodeById --nodeId <node id>\n";
        String getNodesOperation = "--getNodes\n";
        String submitJobOperation = "--submitJob --def <job def file> --input <job input constructor>\n";
        String submitJobRemoteOperation = "--submitJob --def <job def file> --input <job input constructor> --url <url>\n";
        String getJobInfoOperation = "--getJobInfo --id <job id>\n";
        String killOperation = "--kill --id <job id>\n";
        String getExecutionNodeOperation = "--getExecNode --id <job id>\n";

        String usage = "resmgr-client --url <url to xml rpc service> --operation "
                + "[<operation> [params]]\n"
                + "operations:\n"
                + getNodeByIdOperation
                + getNodesOperation
                + submitJobOperation
                + submitJobRemoteOperation
                + getJobInfoOperation
                + killOperation + getExecutionNodeOperation;

        String operation = null, url = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--operation")) {
                operation = args[++i];
            } else if (args[i].equals("--url")) {
                url = args[++i];
            }
        }

        if (operation == null) {
            System.err.println(usage);
            System.exit(1);
        }

        // create the client
        XmlRpcResourceManagerClient client = new XmlRpcResourceManagerClient(
                new URL(url));

        if (operation.equals("--getNodes")) {
            // no arguments to read, just call getNodes
            List resNodes = client.getNodes();

            if (resNodes != null && resNodes.size() > 0) {
                for (Iterator i = resNodes.iterator(); i.hasNext();) {
                    ResourceNode node = (ResourceNode) i.next();
                    System.out.println("node: [id=" + node.getNodeId()
                            + ",capacity=" + node.getCapacity() + ",url="
                            + node.getIpAddr() + "]");
                }
            }

        } else if (operation.equals("--getExecNode")) {
            String jobId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    jobId = args[++i];
                }
            }

            if (jobId == null) {
                System.err.println(getExecutionNodeOperation);
                System.exit(1);

            }

            String execNode = client.getExecutionNode(jobId);
            if (execNode == null || (execNode != null && execNode.equals(""))) {
                System.out.println("Job: [" + jobId
                        + "] not executing on any known node!");
            } else {
                System.out.println(execNode);
            }
        } else if (operation.equals("--kill")) {
            String jobId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    jobId = args[++i];
                }
            }

            if (jobId == null) {
                System.err.println(killOperation);
                System.exit(1);

            }

            if (client.killJob(jobId)) {
                System.out.println("Job: [" + jobId + "] successfully killed.");
            } else {
                System.out.println("Unable to kill job: [" + jobId + "]");
            }

        } else if (operation.equals("--getNodeById")) {
            String nodeId = null;
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }
            }

            if (nodeId == null) {
                System.err.println(getNodeByIdOperation);
                System.exit(1);
            }

            ResourceNode node = client.getNodeById(nodeId);

            if (node != null) {
                System.out.println("node: [id=" + node.getNodeId()
                        + ",capacity=" + node.getCapacity() + ",url="
                        + node.getIpAddr() + "]");
            }
        } else if (operation.equals("--getJobInfo")) {
            String jobId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    jobId = args[++i];
                }
            }

            if (jobId == null) {
                System.err.println(getJobInfoOperation);
                System.exit(1);
            }

            Job jobInfo = client.getJobInfo(jobId);

            System.out.println("Job: [id=" + jobId + ", status="
                    + getReadableJobStatus(jobInfo.getStatus()) + ",name="
                    + jobInfo.getName() + ",queue=" + jobInfo.getQueueName()
                    + ",load=" + jobInfo.getLoadValue() + ",inputClass="
                    + jobInfo.getJobInputClassName() + ",instClass="
                    + jobInfo.getJobInstanceClassName() + "]");
        } else
            throw new IllegalArgumentException("Unknown Operation!");

    }

    public boolean isJobComplete(String jobId) throws JobRepositoryException {
        Vector argList = new Vector();
        argList.add(jobId);

        boolean complete = false;

        try {
            complete = ((Boolean) client.execute("resourcemgr.isJobComplete",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new JobRepositoryException(e.getMessage());
        } catch (IOException e) {
            throw new JobRepositoryException(e.getMessage());
        }

        return complete;
    }

    public Job getJobInfo(String jobId) throws JobRepositoryException {
        Vector argList = new Vector();
        argList.add(jobId);

        Hashtable jobHash = null;

        try {
            jobHash = (Hashtable) client.execute("resourcemgr.getJobInfo",
                    argList);
        } catch (XmlRpcException e) {
            throw new JobRepositoryException(e.getMessage());
        } catch (IOException e) {
            throw new JobRepositoryException(e.getMessage());
        }

        return XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
    }

    public boolean isAlive() {
        Vector argList = new Vector();

        try {
            return ((Boolean) client.execute("resourcemgr.isAlive", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * Gets the number of Jobs in JobQueue
     * @return Number of Jobs in JobQueue
     * @throws JobRepositoryException On Any Exception
     */
    public int getJobQueueSize() throws JobRepositoryException {
        try {
            Vector argList = new Vector();
            return ((Integer) client.execute("resourcemgr.getJobQueueSize", argList));
        } catch (Exception e) {
            throw new JobRepositoryException("Failed to get JobQueue from server : " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the max number of Jobs allowed in JobQueue
     * @return Max number of Jobs
     * @throws JobRepositoryException On Any Exception
     */
    public int getJobQueueCapacity() throws JobRepositoryException {
        try {
            Vector argList = new Vector();
            return ((Integer) client.execute("resourcemgr.getJobQueueCapacity", argList));
        } catch (Exception e) {
            throw new JobRepositoryException("Failed to get JobQueue capacity from server : " + e.getMessage(), e);
        }
    }
    
    public boolean killJob(String jobId) {
        Vector argList = new Vector();
        argList.add(jobId);

        try {
            return ((Boolean) client.execute("resourcemgr.killJob", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public String getExecutionNode(String jobId) {
        Vector argList = new Vector();
        argList.add(jobId);

        try {
            return (String) client.execute("resourcemgr.getExecutionNode", argList);
        } catch (XmlRpcException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public String submitJob(Job exec, JobInput in) throws JobExecutionException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(exec));
        argList.add(in.write());

        LOG.log(Level.FINEST, argList.toString());

        String jobId = null;

        try {
            jobId = (String) client.execute("resourcemgr.handleJob", argList);
        } catch (XmlRpcException e) {
            throw new JobExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new JobExecutionException(e.getMessage());
        }

        return jobId;

    }

    public boolean submitJob(Job exec, JobInput in, URL hostUrl)
            throws JobExecutionException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(exec));
        argList.add(in.write());
        argList.add(hostUrl.toString());

        boolean success = false;

        try {
            success = ((Boolean) client.execute("resourcemgr.handleJob",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new JobExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new JobExecutionException(e.getMessage());
        }

        return success;

    }

    public List getNodes() throws MonitorException {
        Vector argList = new Vector();

        Vector nodeVector = null;

        try {
            nodeVector = (Vector) client.execute("resourcemgr.getNodes",
                    argList);
        } catch (XmlRpcException e) {
            throw new MonitorException(e.getMessage());
        } catch (IOException e) {
            throw new MonitorException(e.getMessage());
        }

        return XmlRpcStructFactory.getResourceNodeListFromXmlRpc(nodeVector);

    }

    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        Vector argList = new Vector();
        argList.add(nodeId);

        Hashtable resNodeHash = null;

        try {
            resNodeHash = (Hashtable) client.execute("resourcemgr.getNodeById",
                    argList);
        } catch (XmlRpcException e) {
            throw new MonitorException(e.getMessage());
        } catch (IOException e) {
            throw new MonitorException(e.getMessage());
        }

        return XmlRpcStructFactory.getResourceNodeFromXmlRpc(resNodeHash);

    }

    /**
     * @return the resMgrUrl
     */
    public URL getResMgrUrl() {
        return resMgrUrl;
    }

    /**
     * @param resMgrUrl
     *            the resMgrUrl to set
     */
    public void setResMgrUrl(URL resMgrUrl) {
        this.resMgrUrl = resMgrUrl;
    }

    private static String getReadableJobStatus(String status) {
        if (status.equals(JobStatus.COMPLETE)) {
            return "COMPLETE";
        } else if (status.equals(JobStatus.EXECUTED)) {
            return "EXECUTED";
        } else if (status.equals(JobStatus.QUEUED)) {
            return "QUEUED";
        } else if (status.equals(JobStatus.SCHEDULED)) {
            return "SCHEDULED";
        } else if (status.equals(JobStatus.KILLED)) {
            return "KILLED";
        } else
            return null;
    }
}
