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


package org.apache.oodt.cas.resource.examples;

//OODT imports

import org.apache.oodt.cas.resource.metadata.JobMetadata;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobInstance;
import org.apache.oodt.cas.resource.structs.NameValueJobInput;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A Job that will take a variable amount of time to finish.
 * </p>
 */
public class LongJob implements JobInstance, JobMetadata {
    private static Logger LOG = Logger.getLogger(LongJob.class.getName());
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.structs.JobInstance#execute(org.apache.oodt.cas.resource.structs.JobInput)
     */
    public boolean execute(JobInput in) throws JobInputException {
        if (!(in instanceof NameValueJobInput)) {
            throw new JobInputException(
                    "Only know how to handle NameValueInput: unknown input type: ["
                            + in.getClass().getName() + "]");
        }

        NameValueJobInput input = (NameValueJobInput) in;

        int waitTime;
        waitTime = Integer.valueOf(input.getValue("wait"));

        System.out.println("LongJob running for " + waitTime + " sec");

        try {
            Thread.sleep(waitTime * 1000);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }

        System.out.println("LongJob finished.");

        return true;
    }

}
