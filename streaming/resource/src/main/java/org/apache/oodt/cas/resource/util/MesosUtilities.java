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
package org.apache.oodt.cas.resource.util;

import java.util.Collection;

import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.Value.Range;
import org.apache.mesos.Protos.Value.Type;
import org.apache.commons.lang.SerializationUtils;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.JobSpecSerializer;
import org.apache.mesos.protobuf.ByteString;
/**
 * @author starchmd
 * @version $Revision$
 */
public class MesosUtilities {
    /**
     * Get a ByteString serialization of a JobSpec to send over the wire to the mesos-backend.
     * @param jobSpec - JobSpec to serialize
     * @return bytestring containing byte[] of jobspec
     */
    public static ByteString jobSpecToByteString(JobSpec jobSpec)
    {
        return ByteString.copyFrom(SerializationUtils.serialize(new JobSpecSerializer(jobSpec)));
    }
    /**
     * Build a JobSpec from a ByteString off the wire
     * @param data - ByteString to deserialize
     * @return newly minted JobSpec
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static JobSpec byteStringToJobSpec(ByteString data) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return ((JobSpecSerializer)SerializationUtils.deserialize(data.toByteArray())).getJobSpec();
    }

    /**
     * Makes a string message from resources list.
     * @param resources - resource list to make into string.
     * @return string representing the resource list.
     */
    public static String getResourceMessage(Collection<Resource> resources) {
        String ret = "";
        for (Resource res : resources)
            ret += "\n\t"+getResourceMessage(res);
        return ret;
    }

    /**
     * Creates string out an offer in a nice format.
     * @param resource - mesos resource to make into string.
     * @return string representing a resource.
     */
    public static String getResourceMessage(Resource resource) {
        Type type = resource.getType();
        String ret = resource.getName() +" "+resource.getRole()+ ": ";
        switch (type) {
            case SCALAR:
                ret += resource.getScalar().getValue();
                break;
            case RANGES:
                for (Range range : resource.getRanges().getRangeList())
                    ret += range.getBegin() + " - "+range.getEnd()+",";
                break;
            case TEXT:
                ret += " TEXT type...cannot find.";
                break;
            case SET:
                for (String string : resource.getSet().getItemList())
                    ret += string + ",";
                break;
        }
        return ret;
    }
}