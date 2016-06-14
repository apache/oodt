/**
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

package org.apache.oodt.cas.workflow.engine;

//JDK imports

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

import org.junit.Ignore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * A simple workflow task instance that writes its start date time to a file.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
@Ignore
public class SimpleTester implements WorkflowTaskInstance {

  private static final Logger LOG = Logger.getLogger(SimpleTester.class
      .getName());

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(org.apache
   * .oodt.cas.metadata.Metadata,
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
   */
  @Override
  public void run(Metadata metadata, WorkflowTaskConfiguration config)
      throws WorkflowTaskInstanceException {
    PrintWriter pw = null;
    try {
      String jobFilePath = config.getProperty("TestDirPath") + "task-"
          + metadata.getMetadata("StartDateTime") + ".job";
      int n=0;
      while(new File(jobFilePath).exists()){
        jobFilePath = config.getProperty("TestDirPath") + "task-" 
        + n + metadata.getMetadata("StartDateTime") + ".job";
        n++;
      }
      LOG.log(Level.INFO, "Creating job file: [" + jobFilePath + "]");
      pw = new PrintWriter(new FileOutputStream(jobFilePath));
      pw.println("StartDateTime=" + metadata.getMetadata("StartDateTime"));
    } catch (FileNotFoundException e) {
      throw new WorkflowTaskInstanceException(e.getMessage());
    } finally {
      if (pw != null) {
        try {
          pw.close();
        } catch (Exception ignore) {
        }
      }
    }

  }
}
