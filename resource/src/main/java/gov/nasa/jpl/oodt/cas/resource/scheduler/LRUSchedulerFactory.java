//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.scheduler;

//JAVA imports
import java.util.Arrays;
import java.util.List;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.resource.jobqueue.JobQueue;
import gov.nasa.jpl.oodt.cas.resource.monitor.Monitor;
import gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A Least recently used scheduler factory interface.
 * </p>
 * 
 */
public class LRUSchedulerFactory implements SchedulerFactory {

  /*
   * a list of URIs pointing to directories that have the
   * node-to-queue-mapping.xml files
   */
  private List queuesDirList;

  /* our monitor */
  private Monitor mon = null;

  /* our batchmgr */
  private Batchmgr batcher = null;
  
  /* our job queue */
  private JobQueue queue = null;

  public LRUSchedulerFactory() {
    String queuesDirUris = System
        .getProperty("gov.nasa.jpl.oodt.cas.resource.scheduler.nodetoqueues.dirs");

    if (queuesDirUris != null) {
      /* do env var replacement */
      queuesDirUris = PathUtils.replaceEnvVariables(queuesDirUris);
      String[] dirUris = queuesDirUris.split(",");
      queuesDirList = Arrays.asList(dirUris);
    }

    String batchmgrClassStr = System.getProperty("resource.batchmgr.factory",
        "gov.nasa.jpl.oodt.cas.resource.batchmgr.XmlRpcBatchmgrFactory");
    String monitorClassStr = System.getProperty("resource.monitor.factory",
        "gov.nasa.jpl.oodt.cas.resource.monitor.AssignmentMonitorFactory");
    
    String jobQueueClassStr = System.getProperty("resource.jobqueue.factory",
        "gov.nasa.jpl.oodt.cas.resource.jobqueue.JobStackJobQueueFactory");

    batcher = GenericResourceManagerObjectFactory
        .getBatchmgrServiceFromFactory(batchmgrClassStr);
    mon = GenericResourceManagerObjectFactory
        .getMonitorServiceFromFactory(monitorClassStr);
    
    queue = GenericResourceManagerObjectFactory.getJobQueueServiceFromFactory(
        jobQueueClassStr);
    
    // set the monitor for this batcher
    batcher.setMonitor(mon);
    
    // set the job repo for this batcher
    batcher.setJobRepository(queue.getJobRepository());


  }

  public Scheduler createScheduler() {
    if (queuesDirList != null) {
      return new LRUScheduler(queuesDirList, mon, batcher, queue);
    } else {
      return null;
    }
  }

}