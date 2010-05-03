//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * A class that holds the complete specification for how to run a {@link Job}.
 * This includes the {@link Job} definition itself, along with its
 * {@link JobInput}.
 */
public class JobSpec {

  private JobInput in = null;

  private Job job = null;

  /**
   * Default Constructor.
   * 
   */
  public JobSpec() {
  }

  /**
   * Constructs a new JobSpec with the given {@link JobInput} and {@link Job}.
   * 
   * @param in
   *          The {@link Job}'s input.
   * @param job
   *          The {@link Job}'s definition.
   */
  public JobSpec(JobInput in, Job job) {
    this.in = in;
    this.job = job;
  }

  /**
   * @return the in
   */
  public JobInput getIn() {
    return in;
  }

  /**
   * @param in
   *          the in to set
   */
  public void setIn(JobInput in) {
    this.in = in;
  }

  /**
   * @return the job
   */
  public Job getJob() {
    return job;
  }

  /**
   * @param job
   *          the job to set
   */
  public void setJob(Job job) {
    this.job = job;
  }

}
