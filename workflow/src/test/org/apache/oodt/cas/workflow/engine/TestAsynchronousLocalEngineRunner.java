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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.commons.util.DateConvert;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * A test case for the {@link AsynchronousLocalEngineRunner}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestAsynchronousLocalEngineRunner extends TestCase {

  private AsynchronousLocalEngineRunner runner;

  protected File testDir;

  public void testRun() {
    WorkflowTask task = new WorkflowTask();
    task.setConditions(Collections.emptyList());
    task.setRequiredMetFields(Collections.emptyList());
    task.setTaskConfig(new WorkflowTaskConfiguration());
    task.setTaskId("urn:cas:workflow:tester");
    task.setTaskInstanceClassName(SimpleTester.class.getName());
    task.setTaskName("Tester");
    Metadata met = new Metadata();
    met.addMetadata("StartDateTime", DateUtils.toString(Calendar.getInstance()));
    met.addMetadata("TestDir", testDir.getAbsolutePath());
    try {
      runner.execute(task, met);
      runner.execute(task, met);
      assertTrue(ranFast());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private boolean ranFast() {
    boolean ranFast = true;
    for (File f : this.testDir.listFiles()) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line = null;
        while ((line = br.readLine()) != null) {

        }

        String[] toks = line.split(",");
        Date dateTime = DateConvert.isoParse(toks[1]);
        // FIXME: compare the date time with the current
        // date time and make sure that it's not larger
        // than 30 seconds for any of the files (should be 2)
        // in there
      } catch (Exception e) {
        fail(e.getMessage());
        ranFast = false;
      } finally {
        if (br != null) {
          try {
            br.close();
          } catch (Exception ignore) {
          }

          br = null;
        }
      }
    }

    return ranFast;
  }

  private class SimpleTester implements WorkflowTaskInstance {
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
        pw = new PrintWriter(new FileOutputStream(testDir.getAbsolutePath()
            + "/" + "task-" + metadata.getMetadata("StartDateTime")));
        pw.println("StartDateTime=" + metadata.getMetadata("StartDateTime"));
      } catch (FileNotFoundException e) {
        throw new WorkflowTaskInstanceException(e.getMessage());
      } finally {
        if (pw != null) {
          try {
            pw.close();
          } catch (Exception ignore) {
          }
          pw = null;
        }
      }

    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    testDir = File.createTempFile("test", "txt").getParentFile();
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {

    // blow away test file
    deleteAllFiles(testDir.getAbsolutePath());
    testDir.delete();
    testDir = null;
  }

  private void deleteAllFiles(String startDir) {
    File startDirFile = new File(startDir);
    File[] delFiles = startDirFile.listFiles();

    if (delFiles != null && delFiles.length > 0) {
      for (int i = 0; i < delFiles.length; i++) {
        delFiles[i].delete();
      }
    }

    startDirFile.delete();

  }

}
