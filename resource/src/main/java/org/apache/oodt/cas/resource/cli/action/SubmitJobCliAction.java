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
package org.apache.oodt.cas.resource.cli.action;

//JDK imports
import java.net.URL;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.util.JobBuilder;

/**
 * A {@link CmdLineAction} which submits a job.
 * 
 * @author bfoster (Brian Foster)
 */
public class SubmitJobCliAction extends ResourceCliAction {

   public String jobDefinitionFile;
   public URL url;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(jobDefinitionFile, "Must specify jobDefinitionFile");

         JobSpec spec = JobBuilder.buildJobSpec(jobDefinitionFile);
         if (url == null) {
            printer.println("Successful submit job with jobId '"
                  + getClient().submitJob(spec.getJob(), spec.getIn()) + "'");
         } else {
            if (getClient().submitJob(spec.getJob(), spec.getIn(), url)) {
               printer.println("Successfully submitted job to url '"
                     + url + "'");
            } else {
               throw new Exception("Job submit returned false");
            }
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to submit job for job"
               + " definition file '" + jobDefinitionFile + "' : "
               + e.getMessage(), e);
      }
   }

   public void setJobDefinitionFile(String jobDefinitionFile) {
      this.jobDefinitionFile = jobDefinitionFile;
   }

   public void setUrl(URL url) {
      this.url = url;
   }
}
