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

package org.apache.oodt.cas.resource.system;

import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public interface ResourceManagerClient extends Serializable {

    boolean isJobComplete(String jobId) throws JobRepositoryException;

    Job getJobInfo(String jobId) throws JobRepositoryException;

    boolean isAlive();

    int getJobQueueSize() throws JobRepositoryException;

    int getJobQueueCapacity() throws JobRepositoryException;

    boolean killJob(String jobId);

    String getExecutionNode(String jobId);

    String getNodeReport() throws MonitorException;

    String getExecReport() throws JobRepositoryException;

    String submitJob(Job exec, JobInput in) throws JobExecutionException;

    boolean submitJob(Job exec, JobInput in, URL hostUrl) throws JobExecutionException;

    List getNodes() throws MonitorException;

    ResourceNode getNodeById(String nodeId) throws MonitorException;

    URL getResMgrUrl();

    void setResMgrUrl(URL resMgrUrl);

    void addQueue(String queueName) throws QueueManagerException;

    void removeQueue(String queueName) throws QueueManagerException;

    void addNode(ResourceNode node) throws MonitorException;

    void removeNode(String nodeId) throws MonitorException;

    void setNodeCapacity(String nodeId, int capacity) throws MonitorException;

    void addNodeToQueue(String nodeId, String queueName) throws QueueManagerException;

    void removeNodeFromQueue(String nodeId, String queueName) throws QueueManagerException;

    List<String> getQueues() throws QueueManagerException;

    List<String> getNodesInQueue(String queueName) throws QueueManagerException;

    List<String> getQueuesWithNode(String nodeId) throws QueueManagerException;

    String getNodeLoad(String nodeId) throws MonitorException;

    List getQueuedJobs() throws JobQueueException;
}
