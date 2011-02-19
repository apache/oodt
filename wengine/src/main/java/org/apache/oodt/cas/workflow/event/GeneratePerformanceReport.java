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
package org.apache.oodt.cas.workflow.event;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineLocal;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.server.action.GetPage;
import org.apache.oodt.cas.workflow.state.done.SuccessState;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * Event which writes out workflow performance to a file
 *
 */
public class GeneratePerformanceReport extends WorkflowEngineEvent {

	private File reportFile;
	
	@Override
	public void performAction(WorkflowEngineLocal engine, Metadata inputMetadata)
			throws Exception {
		Report overallReport = new Report();
		HashMap<String, Report> workflowBasedReports = new HashMap<String, Report>();
		QueuePage firstWorkflow = engine.getPage(new PageInfo(1, PageInfo.FIRST_PAGE), GetPage.COMPARATOR.CreationDate.getComparator());
		QueuePage page = engine.getPage(new PageInfo(Integer.MAX_VALUE, PageInfo.FIRST_PAGE), new SuccessState(""));
		for (ProcessorStub stub : page.getStubs()) {
			long runtime = (stub.getProcessorInfo().getCompletionDate().getTime() - stub.getProcessorInfo().getCreationDate().getTime()) / 1000 / 60;
			overallReport.minRuntime = Math.min(overallReport.minRuntime, runtime);
			overallReport.maxRuntime = Math.max(overallReport.maxRuntime, runtime);
			overallReport.totalRuntime += runtime; 
			overallReport.totalWorkflows++; 
			Report workflowReport = workflowBasedReports.get(stub.getModelId());
			if (workflowReport == null)
				workflowReport = new Report();
			workflowReport.minRuntime = Math.min(workflowReport.minRuntime, runtime);
			workflowReport.maxRuntime = Math.max(workflowReport.maxRuntime, runtime);
			workflowReport.totalRuntime += runtime; 
			workflowReport.totalWorkflows++; 
			workflowBasedReports.put(stub.getModelId(), workflowReport);
		}
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(this.reportFile));
			ps.println();
			ps.println("Performance Report Generated on: " + new Date());
			if (overallReport.totalWorkflows > 0) {
				double hoursUp = (new Date().getTime() - firstWorkflow.getStubs().get(0).getProcessorInfo().getCreationDate().getTime()) / 1000.0 / 60.0 / 60.0;
				ps.println("Time Up: " + new DecimalFormat("#.###").format(hoursUp) + " hours");
				ps.println();
				ps.println("**** Overall Performance Report ****");
				ps.println(" - Total Workflows Analyzed: " + overallReport.totalWorkflows);
				ps.println(" - Workflow Throughput: " + (int) ((double) overallReport.totalWorkflows / hoursUp) + " workflows per hour");
				ps.println(" - Max Workflow Runtime: " + overallReport.maxRuntime + " mins");
				ps.println(" - Min Workflow Runtime: " + overallReport.minRuntime + " mins");
				ps.println(" - Average Workflow Runtime: " + (overallReport.totalRuntime / overallReport.totalWorkflows) + " mins");
				for (String modelId : workflowBasedReports.keySet()) {
					Report workflowReport = workflowBasedReports.get(modelId);
					ps.println();
					ps.println("**** '" + modelId + "' Performance Report ****");
					ps.println(" - Total Workflows Analyzed: " + workflowReport.totalWorkflows);
					ps.println(" - Workflow Throughput: " + (int) ((double) workflowReport.totalWorkflows / hoursUp) + " workflows per hour");
					ps.println(" - Max Workflow Runtime: " + workflowReport.maxRuntime + " mins");
					ps.println(" - Min Workflow Runtime: " + workflowReport.minRuntime + " mins");
					ps.println(" - Average Workflow Runtime: " + (workflowReport.totalRuntime / workflowReport.totalWorkflows) + " mins");
				}
			}else {
				ps.println("No Workflows In Success State");
			}
			ps.println();
		}catch (Exception e) {
			throw new Exception("Failed generate report file : " + e.getMessage(), e);
		}finally {
			try { ps.close(); } catch(Exception e) {}
		}
	}

	public void setReportFile(String reportFile) {
		this.reportFile = new File(reportFile).getAbsoluteFile();
		if (!this.reportFile.getParentFile().exists())
			this.reportFile.getParentFile().mkdirs();
	}
	
	private class Report {
		long totalWorkflows = 0;
		long totalRuntime = 0;
		long minRuntime = Long.MAX_VALUE;
		long maxRuntime = 0;
	}
	
}
