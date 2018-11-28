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


package org.apache.oodt.cas.resource.structs;

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
