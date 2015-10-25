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
import java.io.File;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.engine.runner.AsynchronousLocalEngineRunner;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunner;
import org.apache.oodt.cas.workflow.structs.FILOPrioritySorter;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Exercises the {@link TaskRunner}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestTaskRunner extends TestCase {

  private File testDir;

  private EngineRunner runner;

  private TaskRunner taskRunner;

  private TaskQuerier querier;

  public void testExecuteTasks() {
    FILOPrioritySorter prioritizer = new FILOPrioritySorter();
    MockProcessorQueue processorQueue = new MockProcessorQueue();
    querier = new MetSetterTaskQuerier(processorQueue, prioritizer);
    Thread querierThread = new Thread(querier);
    querierThread.start();
    while (querier.getRunnableProcessors().size() != 2) {
      assertNotNull(querier.getRunnableProcessors());
    }
    List<WorkflowProcessor> runnables = querier.getRunnableProcessors();
    assertNotNull(runnables);
    assertEquals(2, runnables.size());
    runner = new AsynchronousLocalEngineRunner();
    taskRunner = new TaskRunner(querier, runner);
    assertNotNull(taskRunner);
    Thread runnerThread = new Thread(taskRunner);
    WorkflowTask task = taskRunner
        .extractTaskFromProcessor((TaskProcessor) runnables.get(0));
    assertNotNull(task);
    testDir = new File(task.getTaskConfig().getProperty("TestDirPath"));
    assertNotNull(testDir);
    runnerThread.start();

    while (!testDir.exists()
        || (testDir.exists() && testDir.listFiles().length != 2)) {
    }

    querier.setRunning(false);
    runnerThread.interrupt();

    // get the test dir path
    assertTrue(testDir.exists());
    assertNotNull(testDir.listFiles());
    assertEquals(2, testDir.listFiles().length);
    taskRunner.setRunning(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

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
    this.querier = null;
    this.taskRunner = null;
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
