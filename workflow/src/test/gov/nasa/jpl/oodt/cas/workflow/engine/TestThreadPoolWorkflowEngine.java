//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.engine;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import jpl.eda.util.DateConvert;

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
        assertEquals(Double.valueOf(0.0), Double
                .valueOf(ThreadPoolWorkflowEngine
                        .getCurrentTaskWallClockMinutes(inst)));

        // now set start date time, and assert that wall clock minutes > 0
        inst.setCurrentTaskStartDateTimeIsoStr(DateConvert
                .isoFormat(new Date()));
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
        assertEquals(Double.valueOf(wallClockMins), Double
                .valueOf(ThreadPoolWorkflowEngine
                        .getCurrentTaskWallClockMinutes(inst)));
        assertEquals(Double.valueOf(wallClockMins), Double
                .valueOf(ThreadPoolWorkflowEngine
                        .getCurrentTaskWallClockMinutes(inst)));

        // set the start date time after the end date time
        // make sure that the wall cock time is 0.0
        inst.setCurrentTaskStartDateTimeIsoStr(DateConvert
                .isoFormat(new Date()));
        assertEquals(Double.valueOf(0.0), Double
                .valueOf(ThreadPoolWorkflowEngine
                        .getCurrentTaskWallClockMinutes(inst)));

    }
}
