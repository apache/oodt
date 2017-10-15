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

import org.apache.avro.ipc.Responder;
import org.apache.avro.reflect.AvroName;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.structs.avrotypes.*;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class AvroTypeFactory {

    public static Job getJob(AvroJob avroJob) {
        Job job = new Job();
        job.setId(avroJob.getId());
        job.setName(avroJob.getName());
        job.setJobInstanceClassName(avroJob.getJobInstanceClassName());
        job.setJobInputClassName(avroJob.getJobInputClassName());
        job.setQueueName(avroJob.getQueueName());
        job.setLoadValue(avroJob.getLoadValue());
        job.setStatus(avroJob.getStatus());

        return job;
    }

    public static AvroJob getAvroJob(Job job) {
        AvroJob avroJob = new AvroJob();
        avroJob.setId(job.getId());
        avroJob.setName(job.getName());
        avroJob.setJobInstanceClassName(job.getJobInstanceClassName());
        avroJob.setJobInputClassName(job.getJobInputClassName());
        avroJob.setQueueName(job.getQueueName());
        avroJob.setLoadValue(avroJob.getLoadValue());
        avroJob.setStatus(avroJob.getStatus());

        return avroJob;
    }

    //

    public static JobInput getJobInput(AvroJobInput avroJobInput){
        JobInput jobInput = GenericResourceManagerObjectFactory
                .getJobInputFromClassName(avroJobInput.getClassName());

        return setJobInputInplementation(jobInput,avroJobInput);
    }

    public static AvroJobInput getAvroJobInput(JobInput jobInput){
        AvroJobInput avroJobInput = new AvroJobInput();
        avroJobInput.setClassName(jobInput.getClass().getCanonicalName());

        return setAvroJobInputInplementation(avroJobInput,jobInput);
    }

    private static JobInput setJobInputInplementation(JobInput jobInput,AvroJobInput avroJobInput){

        if(jobInput instanceof NameValueJobInput){
            NameValueJobInput nameValueJobInput = (NameValueJobInput)jobInput;
            AvroNameValueJobInput avroNameValueJobInput = (AvroNameValueJobInput) avroJobInput.getImple();
            setPropertiesToNameValueJobInput(getHashtable(avroNameValueJobInput.getProps()), nameValueJobInput);
            return nameValueJobInput;
        }

        return jobInput;
    }

    private static NameValueJobInput setPropertiesToNameValueJobInput(Hashtable hashProp, NameValueJobInput nameValueJobInput){
        for (Object key : hashProp.keySet()){
            nameValueJobInput.setNameValuePair((String)key,(String)hashProp.get(key));
        }
        return nameValueJobInput;
    }



    private static AvroJobInput setAvroJobInputInplementation(AvroJobInput avroJobInput,JobInput jobInput){

        if (jobInput instanceof NameValueJobInput){
            NameValueJobInput nameValueJobInput = (NameValueJobInput) jobInput;

            AvroNameValueJobInput avroNameValueJobInput = new AvroNameValueJobInput();
            avroNameValueJobInput.setProps(getMap(nameValueJobInput.getProps()));
            avroJobInput.setImple(avroNameValueJobInput);
            return avroJobInput;
        }
        return avroJobInput;
    }

    private static Hashtable getHashtable(Map<String,String> map){
        Hashtable hashtable = new Hashtable();

        for (String s : map.keySet()){
            hashtable.put(s,map.get(s));
        }
        return hashtable;
    }

    private static Map<String,String> getMap(Hashtable hashtable){
        Map<String,String> map = new HashMap<String, String>();
        for (Object o : hashtable.keySet()){
            map.put((String)o,(String)hashtable.get(o));
        }
        return map;
    }

    //

    public static ResourceNode getResourceNode(AvroResourceNode avroResourceNode){
        ResourceNode resourceNode = new ResourceNode();
        resourceNode.setId(avroResourceNode.getNodeId());
        try {
            resourceNode.setIpAddr(new URL(avroResourceNode.getIpAddr()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        resourceNode.setCapacity(avroResourceNode.getCapacity());
        return resourceNode;
    }

    public static AvroResourceNode getAvroResourceNode(ResourceNode resourceNode){
        AvroResourceNode avroResourceNode = new AvroResourceNode();
        avroResourceNode.setNodeId(resourceNode.getNodeId());
        avroResourceNode.setIpAddr(resourceNode.getIpAddr().toString());
        avroResourceNode.setCapacity(resourceNode.getCapacity());
        return avroResourceNode;
    }


    public static List<AvroResourceNode> getListAvroResourceNode(List<ResourceNode> resourceNodes){
        List<AvroResourceNode> avroResourceNodes = new ArrayList<AvroResourceNode>();

        for (ResourceNode rn : resourceNodes){
            avroResourceNodes.add(getAvroResourceNode(rn));
        }
        return avroResourceNodes;
    }

    public static List<ResourceNode> getListResourceNode(List<AvroResourceNode> avroResourceNodes){
        List<ResourceNode> resourceNodes = new ArrayList<ResourceNode>();

        for (AvroResourceNode arn : avroResourceNodes){
            resourceNodes.add(getResourceNode(arn));
        }

        return resourceNodes;
    }


}
