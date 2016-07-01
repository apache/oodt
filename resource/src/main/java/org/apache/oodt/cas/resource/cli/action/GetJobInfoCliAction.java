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

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobStatus;

/**
 * A {@link CmdLineAction} which gets job info.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetJobInfoCliAction extends ResourceCliAction {

   private String jobId;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(jobId, "Must specify jobId");

         Job jobInfo = getClient().getJobInfo(jobId);

         printer.println("Job: [id=" + jobId + ", status="
               + getReadableJobStatus(jobInfo.getStatus()) + ",name="
               + jobInfo.getName() + ",queue=" + jobInfo.getQueueName()
               + ",load=" + jobInfo.getLoadValue() + ",inputClass="
               + jobInfo.getJobInputClassName() + ",instClass="
               + jobInfo.getJobInstanceClassName() + "]");
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get job info for job '"
               + jobId + "' : " + e.getMessage(), e);
      }
   }

   public void setJobId(String jobId) {
      this.jobId = jobId;
   }

   private static String getReadableJobStatus(String status) {
      if (status.equals(JobStatus.SUCCESS)) {
         return "SUCCESS";
      } else if (status.equals(JobStatus.FAILURE)) {
         return "FAILURE";
      } else if (status.equals(JobStatus.EXECUTED)) {
         return "EXECUTED";
      } else if (status.equals(JobStatus.QUEUED)) {
         return "QUEUED";
      } else if (status.equals(JobStatus.SCHEDULED)) {
         return "SCHEDULED";
      } else if (status.equals(JobStatus.KILLED)) {
         return "KILLED";
      } else {
         return null;
      }
   }
}
