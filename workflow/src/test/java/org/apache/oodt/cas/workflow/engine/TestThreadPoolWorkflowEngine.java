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


package org.apache.oodt.cas.workflow.engine;

//OODT imports
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogManager;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the ThreadPoolWorkflowEngine.
 * </p>.
 */
public class TestThreadPoolWorkflowEngine extends TestCase {

    public TestThreadPoolWorkflowEngine() {
        // suppress WARNING level and below because we don't want
        // the warning message where we test if start date time is AFTER
        // end date time below
        LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
    }

    public void testCurrentTaskWallClockTime() {
        // at first, there is no start date time
        WorkflowInstance inst = new WorkflowInstance();
        WorkflowTask task = new WorkflowTask();
        task.setTaskId("urn:oodt:testTask");
        ParentChildWorkflow workflow = new ParentChildWorkflow(new Graph());
        workflow.getTasks().add(task);
        inst.setParentChildWorkflow(workflow);
        inst.setCurrentTaskId("urn:oodt:testTask");
        assertEquals(0.0, ThreadPoolWorkflowEngine
            .getCurrentTaskWallClockMinutes(inst));

        // now set start date time, and assert that wall clock minutes > 0
        inst.setCurrentTaskStartDateTimeIsoStr(DateConvert
                .isoFormat(new Date()));
        System.out.println(ThreadPoolWorkflowEngine.getCurrentTaskWallClockMinutes(inst));
        assertTrue(ThreadPoolWorkflowEngine
                .getCurrentTaskWallClockMinutes(inst) > 0.0);

        // set end date time to "" and make sure wall clock mins still greater
        // than 0
        inst.setCurrentTaskEndDateTimeIsoStr("");
        assertTrue(ThreadPoolWorkflowEngine
                .getCurrentTaskWallClockMinutes(inst) > 0.0);

        // set the end date time, compute it, and make sure it stays the same
        String endDateTimeIsoStr = DateConvert.isoFormat(new Date());
        inst.setCurrentTaskEndDateTimeIsoStr(endDateTimeIsoStr);
        double wallClockMins = ThreadPoolWorkflowEngine
                .getCurrentTaskWallClockMinutes(inst);
        assertEquals(wallClockMins, ThreadPoolWorkflowEngine
            .getCurrentTaskWallClockMinutes(inst));
        assertEquals(wallClockMins, ThreadPoolWorkflowEngine
            .getCurrentTaskWallClockMinutes(inst));

        // set the start date time after the end date time
        // make sure that the wall cock time is 0.0
        inst.setCurrentTaskStartDateTimeIsoStr(DateConvert
                .isoFormat(new Date()));
        assertEquals(0.0, ThreadPoolWorkflowEngine
            .getCurrentTaskWallClockMinutes(inst));

    }
}
