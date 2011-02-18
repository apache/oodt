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
package org.apache.oodt.cas.workflow.server.action;

//OODT imports
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for print out a page of queued up WorkflowProcessors
 * <p>
 */
public class GetPage extends FilteredAction {

	private int pageNum;
	private int pageSize;
	private boolean reverse;
	private boolean showMessage;
	
	public GetPage() {
		super();
		this.pageNum = 1;
		this.pageSize = 10;
		this.showMessage = false;
		this.reverse = false;
	}
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		PageInfo pageInfo = new PageInfo(pageSize, pageNum);
		QueuePage page = weClient.getPage(pageInfo, this.createFilter(weClient), this.reverse ? Collections.reverseOrder(this.comparator.getComparator()) : this.comparator.getComparator());
		System.out.println("Workflows " + getFilterAsString() + " (Page: " + page.getPageInfo().getPageNum() + "/" + page.getPageInfo().getTotalPages() +  "; Total: " + page.getPageInfo().getNumOfHits() + "):");
		for (ProcessorStub stub : page.getStubs()) {
			System.out.print("  - InstanceId = '" + stub.getInstanceId() + "', ModelId = '" + stub.getModelId() +"', State = '" + stub.getState().getName() + "'");
			if (this.comparator == null)
				System.out.println();
			else if (this.comparator.name().equals(COMPARATOR.CreationDate.name()))
				System.out.println(", CreationDate = '" + stub.getProcessorInfo().getCreationDate() + "'");
			else if (this.comparator.name().equals(COMPARATOR.ExecutionDate.name()))
				System.out.println(", ExecutionDate = '" + stub.getProcessorInfo().getExecutionDate() + "'");
			else if (this.comparator.name().equals(COMPARATOR.CompletionDate.name()))
				System.out.println(", CompletionDate = '" + stub.getProcessorInfo().getCompletionDate() + "'");
			else if (this.comparator.name().equals(COMPARATOR.AliveTime.name())) 
				System.out.println(", AliveTime = '" + (stub.getProcessorInfo().getCompletionDate() != null ? ((stub.getProcessorInfo().getCompletionDate().getTime() - stub.getProcessorInfo().getCreationDate().getTime()) / 1000 / 60) : ((System.currentTimeMillis() - stub.getProcessorInfo().getCreationDate().getTime()) / 1000 / 60)) + " mins'");
			else if (this.comparator.name().equals(COMPARATOR.TimesBlocked.name())) 
				System.out.println(", TimesBlocked = '" + stub.getTimesBlocked() + "'");
			if (this.showMessage)
				System.out.println("      (Message = '" + stub.getState().getMessage() + "')");
		}
		System.out.println();
	}
	
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public void showMessage(boolean showMessage) {
		this.showMessage = showMessage;
	}

	public void setComparator(String comparator) {
		this.comparator = COMPARATOR.valueOf(COMPARATOR.class, comparator);
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
	protected String getFilterAsString() {
		String filter = "[";
		if (this.stateName != null)
			filter += "state='" + this.stateName + "'";
		if (this.categoryName != null)
			filter += "category='" + this.categoryName + "'";
		if (this.modelId != null)
			filter += "modelId='" + this.modelId + "'";
		if (this.filterKeys.size() > 0)
			filter += this.filterKeys.toString();
		filter += "]";
		return filter;
	}
	
	public static enum COMPARATOR { 
		
		CreationDate {
			@Override
			public Comparator<ProcessorStub> getComparator() {
				return new Comparator<ProcessorStub>() {
					public int compare(ProcessorStub o1,
							ProcessorStub o2) {
						return o1.getProcessorInfo().getCreationDate()
							.compareTo(o2.getProcessorInfo().getCreationDate());
					}
				};
			}
		},
		ExecutionDate {
			@Override
			public Comparator<ProcessorStub> getComparator() {
				return new Comparator<ProcessorStub>() {
					public int compare(ProcessorStub o1,
							ProcessorStub o2) {
						if (o1.getProcessorInfo().getExecutionDate() != null
								&& o2.getProcessorInfo().getExecutionDate() != null) {
							return o1.getProcessorInfo().getExecutionDate()
								.compareTo(o2.getProcessorInfo().getExecutionDate());
						}else if (o1.getProcessorInfo().getExecutionDate() != null) {
							return -1;
						}else if (o2.getProcessorInfo().getExecutionDate() != null) {
							return 1;
						}else {
							return 0;
						}
					}
				};
			}
		},
		CompletionDate {
			@Override
			public Comparator<ProcessorStub> getComparator() {
				return new Comparator<ProcessorStub>() {
					public int compare(ProcessorStub o1,
							ProcessorStub o2) {
						if (o1.getProcessorInfo().getCompletionDate() != null
								&& o2.getProcessorInfo().getCompletionDate() != null) {
							return o1.getProcessorInfo().getCompletionDate()
								.compareTo(o2.getProcessorInfo().getCompletionDate());
						}else if (o1.getProcessorInfo().getCompletionDate() != null) {
							return -1;
						}else if (o2.getProcessorInfo().getCompletionDate() != null) {
							return 1;
						}else {
							return 0;
						}
					}
				};
			}
		},
		AliveTime {
			@Override
			public Comparator<ProcessorStub> getComparator() {
				return new Comparator<ProcessorStub>() {
					public int compare(ProcessorStub o1,
							ProcessorStub o2) {
						Date nowDate = new Date();
						Date o1CompDate = o1.getProcessorInfo().getCompletionDate() != null ? o1.getProcessorInfo().getCompletionDate() : nowDate;
						Date o2CompDate = o2.getProcessorInfo().getCompletionDate() != null ? o2.getProcessorInfo().getCompletionDate() : nowDate;
						return Long.valueOf(o1CompDate.getTime() - o1.getProcessorInfo().getCreationDate().getTime())
							.compareTo(o2CompDate.getTime() - o2.getProcessorInfo().getCreationDate().getTime());
					}
				};
			}
		},
		TimesBlocked {
			@Override
			public Comparator<ProcessorStub> getComparator() {
				return new Comparator<ProcessorStub>() {
					public int compare(ProcessorStub o1,
							ProcessorStub o2) {
						return Integer.valueOf(o2.getTimesBlocked()).compareTo(Integer.valueOf(o1.getTimesBlocked()));
					}
				};
			}
		};
		
		public abstract Comparator<ProcessorStub> getComparator();
		
	};
	private COMPARATOR comparator;
	
}
