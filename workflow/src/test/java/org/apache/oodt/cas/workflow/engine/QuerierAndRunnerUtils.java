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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;

//OODT imports
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessorBuilder;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;

/**
 * 
 * Utilities for testing the {@link TaskQuerier} and {@link TaskRunner} thread
 * classes.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class QuerierAndRunnerUtils {

  private int dateGen;
  
  public QuerierAndRunnerUtils() {
    this.dateGen = 0;
  }
  
  public WorkflowTask getTask(File testDir){
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
    return task;
  }

  public WorkflowProcessor getProcessor(double priority, String stateName,
      String categoryName) throws InstantiationException,
      IllegalAccessException, IOException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
    WorkflowLifecycleManager lifecycleManager = new WorkflowLifecycleManager(
        "./src/main/resources/examples/wengine/wengine-lifecycle.xml");
    WorkflowInstance inst = new WorkflowInstance();
    Date sd = new Date();
    sd.setTime(sd.getTime() + (this.dateGen * 5000));
    this.dateGen++;
    inst.setStartDate(sd);
    inst.setId("task-winst-" + priority);
    ParentChildWorkflow workflow = new ParentChildWorkflow(new Graph());
    workflow.getTasks().add(getTask(getTmpPath()));
    inst.setParentChildWorkflow(workflow);
    inst.setPriority(Priority.getPriority(priority));
    inst.setCurrentTaskId(workflow.getTasks().get(0).getTaskId());
    inst.setParentChildWorkflow(workflow);
    WorkflowProcessorBuilder builder = WorkflowProcessorBuilder
        .aWorkflowProcessor().withLifecycleManager(lifecycleManager)
        .withPriority(priority).withInstance(inst);
    TaskProcessor taskProcessor = (TaskProcessor) builder
        .build(TaskProcessor.class);
    taskProcessor.getWorkflowInstance().setState(lifecycleManager.getDefaultLifecycle().createState(
        stateName, categoryName, ""));    
    return taskProcessor;
  }
  
  private File getTmpPath() throws IOException{
    File testDir;
    String parentPath = File.createTempFile("test", "txt").getParentFile().getAbsolutePath();
    parentPath = parentPath.endsWith("/") ? parentPath:parentPath + "/";
    String testJobDirPath = parentPath + "jobs";
    testDir = new File(testJobDirPath);
    testDir.mkdirs();
    return testDir;
  }

}
