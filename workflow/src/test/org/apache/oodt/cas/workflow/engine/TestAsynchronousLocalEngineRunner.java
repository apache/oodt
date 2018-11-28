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
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.commons.util.DateConvert;

//JODA imports
import org.joda.time.DateTime;
import org.joda.time.Seconds;

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
    task.setTaskId("urn:cas:workflow:tester");
    task.setTaskInstanceClassName(SimpleTester.class.getName());
    task.setTaskName("Tester");
    WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
    config.addConfigProperty("TestDirPath",
        testDir.getAbsolutePath().endsWith("/") ? testDir.getAbsolutePath()
            : testDir.getAbsolutePath() + "/");
    task.setTaskConfig(config);
    Metadata met = new Metadata();
    met.addMetadata("StartDateTime", DateUtils.toString(Calendar.getInstance()));
    try {
      runner.execute(task, met);
      runner.execute(task, met);
      assertTrue(ranFast());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private boolean ranFast() {
    boolean ranFast = true;
    int jobNum = 1;
    for (File f : this.testDir.listFiles()) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line = null;
        while ((line = br.readLine()) != null) {

        }

        String[] toks = line.split(",");
        Date dateTime = DateConvert.isoParse(toks[1]);
        Seconds seconds = Seconds.secondsBetween(new DateTime(dateTime),
            new DateTime());
        if (seconds.getSeconds() > 30) {
          fail("More than 30 seconds elapsed now and running job " + jobNum
              + ": seconds elapsed: [" + seconds.getSeconds() + "]");
        }
      } catch (Exception e) {
        e.printStackTrace();
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

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    String parentPath = File.createTempFile("test", "txt").getParentFile().getAbsolutePath();
    parentPath = parentPath.endsWith("/") ? parentPath:parentPath + "/";
    String testJobDirPath = parentPath + "jobs";
    testDir = new File(testJobDirPath);
    testDir.mkdirs();
    this.runner = new AsynchronousLocalEngineRunner();
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
    this.runner = null;
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
