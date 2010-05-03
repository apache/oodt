//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.engine;

//OODT imports
import java.net.MalformedURLException;
import java.net.URL;

import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import gov.nasa.jpl.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating {@link NonBlockingThreadPoolWorkflowEngine}s.
 * </p>.
 */
public class NonBlockingThreadPoolWorkflowEngineFactory implements WorkflowEngineFactory {

	/* default queue size */
	private int queueSize = -1;

	/* default max pool size */
	private int maxPoolSize = -1;

	/* the min pool size */
	private int minPoolSize = -1;

	/*
	 * the amount of minutes to keep each worker thread alive
	 */
	private long threadKeepAliveTime = -1L;

	/*
	 * whether or not an unlimited queue of worker threads should be allowed
	 */
	private boolean unlimitedQueue = false;
	
	/* the workflow instance repository */
	private WorkflowInstanceRepository workflowInstRep = null;

 /* res mgr url */
 private URL resMgrUrl = null;

	public NonBlockingThreadPoolWorkflowEngineFactory() throws InstantiationException{
		// need to get the values for some of the default thread pool values
		queueSize = Integer.getInteger(
				"gov.nasa.jpl.oodt.cas.workflow.engine.queueSize", 10)
				.intValue();
		maxPoolSize = Integer.getInteger(
				"gov.nasa.jpl.oodt.cas.workflow.engine.maxPoolSize", 10)
				.intValue();
		minPoolSize = Integer.getInteger(
				"gov.nasa.jpl.oodt.cas.workflow.engine.minPoolSize", 4)
				.intValue();
		threadKeepAliveTime = Long
				.getLong(
						"gov.nasa.jpl.oodt.cas.workflow.engine.threadKeepAlive.minutes",
						5).longValue();
		unlimitedQueue = Boolean
				.getBoolean("gov.nasa.jpl.oodt.cas.workflow.engine.unlimitedQueue");
		
		String instRepClazzName = System
				.getProperty("workflow.engine.instanceRep.factory");
		if (instRepClazzName == null) {
			throw new InstantiationException(
					"instance repository factory not specified in workflow properties: failing!");
		}

    this.workflowInstRep = GenericWorkflowObjectFactory
        .getWorkflowInstanceRepositoryFromClassName(instRepClazzName);

    // see if we are using a resource manager or not
    String resMgrUrlStr = System
        .getProperty("gov.nasa.jpl.oodt.cas.workflow.engine.resourcemgr.url");
    if (resMgrUrlStr != null) {
      resMgrUrlStr = PathUtils.replaceEnvVariables(resMgrUrlStr);
      resMgrUrl = safeGetUrlFromString(resMgrUrlStr);
    }
  }

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.workflow.engine.WorkflowEngineFactory#createWorkflowEngine()
	 */
	public WorkflowEngine createWorkflowEngine() {
		NonBlockingThreadPoolWorkflowEngine engine = new NonBlockingThreadPoolWorkflowEngine(workflowInstRep, queueSize, maxPoolSize,
				minPoolSize, threadKeepAliveTime, unlimitedQueue, resMgrUrl);
		return engine;
	}

 private URL safeGetUrlFromString(String urlStr) {
    try {
      return new URL(urlStr);
    } catch (MalformedURLException e) {
      return null;
    }
  }

}

