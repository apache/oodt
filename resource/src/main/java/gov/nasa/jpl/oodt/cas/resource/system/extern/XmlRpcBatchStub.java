//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.system.extern;

//JDK imports
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.structs.Job;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInput;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInstance;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobInputException;
import gov.nasa.jpl.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import gov.nasa.jpl.oodt.cas.resource.util.XmlRpcStructFactory;

//APACHE imports
import org.apache.xmlrpc.WebServer;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An XML RPC-based Batch Submission System.
 * </p>
 * 
 */
public class XmlRpcBatchStub {

    /* the port to run the XML RPC web server on, default is 2000 */
    private int webServerPort = 2000;

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(XmlRpcBatchStub.class
            .getName());

    private static Map jobThreadMap = null;

    public XmlRpcBatchStub(int port) throws Exception {
        webServerPort = port;

        // start up the web server
        webServer = new WebServer(webServerPort);
        webServer.addHandler("batchstub", this);
        webServer.start();

        jobThreadMap = new HashMap();

        LOG.log(Level.INFO, "XmlRpc Batch Stub started by "
                + System.getProperty("user.name", "unknown"));
    }

    public boolean isAlive() {
        return true;
    }

    public boolean executeJob(Hashtable jobHash, Hashtable jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Hashtable jobHash, Date jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Hashtable jobHash, double jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, new Double(jobInput));
    }

    public boolean executeJob(Hashtable jobHash, int jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, new Integer(jobInput));
    }

    public boolean executeJob(Hashtable jobHash, boolean jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, new Boolean(jobInput));
    }

    public boolean executeJob(Hashtable jobHash, Vector jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public boolean executeJob(Hashtable jobHash, byte[] jobInput)
            throws JobException {
        return genericExecuteJob(jobHash, jobInput);
    }

    public synchronized boolean killJob(Hashtable jobHash) {
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

    private boolean genericExecuteJob(Hashtable jobHash, Object jobInput)
            throws JobException {
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

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
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

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }

    private class RunnableJob implements Runnable {

        private JobInput in;

        private JobInstance job;

        public RunnableJob(JobInstance job, JobInput in) {
            this.job = job;
            this.in = in;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                job.execute(in);
            } catch (JobInputException e) {
                e.printStackTrace();
            }

        }

    }
}