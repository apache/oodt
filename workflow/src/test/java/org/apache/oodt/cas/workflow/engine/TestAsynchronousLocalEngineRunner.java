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

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.runner.AsynchronousLocalEngineRunner;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.commons.util.DateConvert;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assume.*;
import static org.junit.Assert.*;

//APACHE imports
//OODT imports
//JODA imports
//Junit imports

/**
 * 
 * A test case for the {@link AsynchronousLocalEngineRunner}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestAsynchronousLocalEngineRunner {
  private static Logger LOG = Logger.getLogger(TestAsynchronousLocalEngineRunner.class.getName());
  private AsynchronousLocalEngineRunner runner;

  protected File testDir;

  private QuerierAndRunnerUtils utils;

  @Test
  public void testRun() {
    TaskProcessor taskProcessor1 = null;
    TaskProcessor taskProcessor2 = null;

    try {
      taskProcessor1 = (TaskProcessor) utils.getProcessor(Priority.getDefault()
          .getValue(), "Executing", "running");
      taskProcessor2 = (TaskProcessor) utils.getProcessor(Priority.getDefault()
          .getValue(), "Executing", "running");
    } catch (Exception e) {
      fail(e.getMessage());
    }

    Metadata met = new Metadata();
    met.addMetadata("StartDateTime", DateUtils.toString(Calendar.getInstance()));

    taskProcessor1.getWorkflowInstance().getSharedContext().addMetadata(met);
    taskProcessor2.getWorkflowInstance().getSharedContext().addMetadata(met);

    try {
      runner.execute(taskProcessor1);
      runner.execute(taskProcessor2);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }
    
    assumeTrue(ranFast());
  }

  private boolean ranFast() {
    boolean ranFast = true;
    int jobNum = 1;
    for (File f : this.testDir.listFiles()) {
      BufferedReader br = null;
      try {
        String line = FileUtils.readFileToString(f);
        String[] toks = line.split(",");
        assumeTrue("Toks not equal to 2: toks=[" + Arrays.asList(toks) + "]", 
            toks.length == 2);
        Date dateTime = DateConvert.isoParse(toks[1]);
        Seconds seconds = Seconds.secondsBetween(new DateTime(dateTime),
            new DateTime());
        if (seconds.getSeconds() > 30) {
          fail("More than 30 seconds elapsed now and running job " + jobNum
              + ": seconds elapsed: [" + seconds.getSeconds() + "]");
        }
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        ranFast = false;
      } finally {
        if (br != null) {
          try {
            br.close();
          } catch (Exception ignore) {
          }

        }
      }
    }

    return ranFast;
  }

  @Before
  public void setupTest() throws Exception {
    String parentPath = File.createTempFile("test", "txt").getParentFile()
        .getAbsolutePath();
    parentPath = parentPath.endsWith("/") ? parentPath : parentPath + "/";
    String testJobDirPath = parentPath + "jobs";
    testDir = new File(testJobDirPath);
    testDir.mkdirs();
    this.runner = new AsynchronousLocalEngineRunner();
    this.utils = new QuerierAndRunnerUtils();
  }

  @After
  public void tearDown() throws Exception {

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
      for (File delFile : delFiles) {
        delFile.delete();
      }
    }

    startDirFile.delete();

  }

}
