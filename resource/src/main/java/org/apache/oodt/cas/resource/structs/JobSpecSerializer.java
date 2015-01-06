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

import java.io.Serializable;

/**
 * A class used to serialize and de-serialize a job spec
 * @author starchmd
 */
public class JobSpecSerializer implements java.io.Serializable {
    private static final long serialVersionUID = -8246199042863932667L;
    //Variables needed to serialize
    String id;
    String jobInputClassName;
    String jobInstanceClassName;
    Integer loadValue;
    String name;
    String queueName;
    String status;
    String jobInputId;
    Serializable jobInput;
    /**
     * Set the variables to serialize them.
     * @param spec - job spec to serialize
     */
    public JobSpecSerializer(JobSpec spec) {
        //Job
        Job tmp = spec.getJob();
        id = tmp.getId();
        jobInputClassName = tmp.getJobInputClassName();
        jobInstanceClassName = tmp.getJobInstanceClassName();
        loadValue = tmp.getLoadValue();
        name = tmp.getName();
        queueName = tmp.getQueueName();
        status = tmp.getStatus();
        //Input of spec
        JobInput input = spec.getIn();
        jobInputId = input.getId();
        jobInput = (Serializable)input.write();
    }
    /**
     * Get the JobSpec back.
     * @return newly constructed job spec
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public JobSpec getJobSpec() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Job tmp = new Job();
        tmp.setId(id);
        tmp.setJobInputClassName(jobInputClassName);
        tmp.setJobInstanceClassName(jobInstanceClassName);
        tmp.setLoadValue(loadValue);
        tmp.setName(name);
        tmp.setQueueName(queueName);
        tmp.setStatus(status);
        //Read in job input, using proper class
        Class<?> clazz = Class.forName(jobInputClassName);
        JobInput input = ((JobInput)clazz.newInstance());
        input.read(jobInput);
        JobSpec spec = new JobSpec();
        spec.setIn(input);
        spec.setJob(tmp);
        return spec;
    }
}