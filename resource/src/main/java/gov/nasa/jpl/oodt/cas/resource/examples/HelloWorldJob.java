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


package gov.nasa.jpl.oodt.cas.resource.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.metadata.JobMetadata;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInput;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInstance;
import gov.nasa.jpl.oodt.cas.resource.structs.NameValueJobInput;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobInputException;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * The classic programming example: the hello world job.
 * </p>
 */
public class HelloWorldJob implements JobInstance, JobMetadata {

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.structs.JobInstance#execute(gov.nasa.jpl.oodt.cas.resource.structs.JobInput)
   */
  public boolean execute(JobInput in) throws JobInputException {
    if (!(in instanceof NameValueJobInput)) {
      throw new JobInputException(
          "Only know how to handle NameValueInput: unknown input type: ["
              + in.getClass().getName() + "]");
    }

    NameValueJobInput input = (NameValueJobInput) in;

    System.out.println("Hello world! How are you "
        + input.getValue("user.name") + "!");
    return true;
  }

}
