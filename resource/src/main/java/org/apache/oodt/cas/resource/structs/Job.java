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
 * <p>
 * The unit of computation in the resource manager.
 * </p>
 * 
 */
public class Job {

    /* our job id */
    private String id = null;

    /* job name */
    private String name = null;

    /* the instance class */
    private String jobInstanceClassName = null;

    /* the job input class */
    private String jobInputClassName = null;

    /* the queue that this job belongs to */
    private String queueName = null;

    /* the load value for this job */
    private Integer loadValue = null;

    /* the status of this job */
    private String status = null;
    
    /* ready or not ? */
    private boolean ready = true;

    /**
     * Default Constructor.
     * 
     */
    public Job() {
    }

    /**
     * @param id
     *            The unique identifier for this job.
     * @param name
     *            The name for this job.
     * @param jobInstanceClassName
     *            The class name of the {@link JobInstance} class that this Job
     *            should run.
     * @param jobInputClassName
     *            The class name of the {@link JobInput} class that should be
     *            fed as input to the running {@link JobInstance}.
     */
    public Job(String id, String name, String jobInstanceClassName,
            String jobInputClassName, String queueName, Integer loadValue) {
        this.id = id;
        this.name = name;
        this.jobInstanceClassName = jobInstanceClassName;
        this.jobInputClassName = jobInputClassName;
        this.loadValue = loadValue;
        this.queueName = queueName;
        this.ready = true;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the jobInputClassName
     */
    public String getJobInputClassName() {
        return jobInputClassName;
    }

    /**
     * @param jobInputClassName
     *            the jobInputClassName to set
     */
    public void setJobInputClassName(String jobInputClassName) {
        this.jobInputClassName = jobInputClassName;
    }

    /**
     * @return the jobInstanceClassName
     */
    public String getJobInstanceClassName() {
        return jobInstanceClassName;
    }

    /**
     * @param jobInstanceClassName
     *            the jobInstanceClassName to set
     */
    public void setJobInstanceClassName(String jobInstanceClassName) {
        this.jobInstanceClassName = jobInstanceClassName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public Integer getLoadValue() {
        return loadValue;
    }

    public void setLoadValue(Integer loadValue) {
        this.loadValue = loadValue;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean getReady(){
      return this.ready;
    }
  
    public void setReady(boolean readyValue){
      this.ready = readyValue;
    }

}
