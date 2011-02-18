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
package org.apache.oodt.cas.workflow.server.channel.xmlrpc;

//JDK imports
import java.util.Comparator;
import java.util.Map;

//APACHE imports
import org.apache.xmlrpc.WebServer;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.event.WorkflowEngineEvent;
import org.apache.oodt.cas.workflow.model.WorkflowGraph;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.page.QueryPage;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.server.channel.AbstractCommunicationChannelServer;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * XML-RPC communication channel server
 * <p>
 */
public class XmlRpcCommunicationChannelServer extends
		AbstractCommunicationChannelServer implements XmlRpcCommunicationServerInterface {

	private WebServer webServer;
	
	public XmlRpcCommunicationChannelServer() {
		super();
	}

	public void startup() throws Exception {
		this.webServer = new WebServer(this.port);
		this.webServer.addHandler(this.getClass().getSimpleName(), this);
		this.webServer.start();
	}

	public boolean xmlrpc_shutdown() throws Exception {
		this.shutdown();
		this.webServer.shutdown();
		this.webServer = null;
		return true;
	}

	public boolean xmlrpc_pauseRunner() throws Exception {
		this.pauseRunner();
		return true;
	}
		
	public boolean xmlrpc_resumeRunner() throws Exception {
		this.resumeRunner();
		return true;
	}
	
	public String xmlrpc_getLaunchDate() throws Exception {
		return this.serializer.serializeObject(this.getLaunchDate());
	}
	
	public String xmlrpc_deleteWorkflow(String instanceId) throws Exception {
		this.deleteWorkflow(instanceId);
		return this.serializer.serializeObject(Boolean.TRUE);
	}

	public String xmlrpc_getInstanceMetadata(String jobId) throws Exception {
		return this.serializer.serializeObject(this.getInstanceMetadata(jobId));
	}

	public String xmlrpc_getInstanceRepository() throws Exception {
		return this.serializer.serializeObject(this.getInstanceRepository());
	}

	public String xmlrpc_getModel(String modelId) throws Exception {
		return this.serializer.serializeObject(this.getModel(modelId));
	}

	public String xmlrpc_getModels() throws Exception {
		return this.serializer.serializeObject(this.getModels());
	}
	
	public String xmlrpc_getWorkflowGraph(String modelId) throws Exception {
		return this.serializer.serializeObject(this.getWorkflowGraph(modelId));
	}

	public String xmlrpc_getWorkflowGraphs() throws Exception {
		return this.serializer.serializeObject(this.getWorkflowGraphs());
	}
	
	public String xmlrpc_getSupportedProcessorIds() throws Exception {
		return this.serializer.serializeObject(this.xmlrpc_getSupportedProcessorIds());
	}

    public String xmlrpc_getProcessorInfo(String instanceId, String modelId) throws Exception {
		return this.serializer.serializeObject(this.getProcessorInfo(instanceId, modelId));
    }
    
	public String xmlrpc_getWorkflowMetadata(String instanceId, String modelId) throws Exception {
		return this.serializer.serializeObject(this.getWorkflowMetadata(instanceId, modelId));
	}

	public String xmlrpc_pauseWorkflow(String instanceId) throws Exception {
		this.pauseWorkflow(instanceId);
		return this.serializer.serializeObject(Boolean.TRUE);
	}

	public String xmlrpc_resumeWorkflow(String instanceId) throws Exception {
		this.resumeWorkflow(instanceId);
		return this.serializer.serializeObject(Boolean.TRUE);
	}

	public String xmlrpc_setWorkflowState(String instanceId, String modelId, String state) throws Exception {
		this.setWorkflowState(instanceId, modelId, this.serializer.deserializeObject(WorkflowState.class, state));
		return this.serializer.serializeObject(Boolean.TRUE);
	}
	
    public String xmlrpc_setWorkflowPriority(String instanceId, String modelId, String priority) throws Exception {
    	this.setWorkflowPriority(instanceId, modelId, this.serializer.deserializeObject(Priority.class, priority));
		return this.serializer.serializeObject(Boolean.TRUE);
    }

	public String xmlrpc_startWorkflow(String workflow, String metadata) throws Exception {
		return this.startWorkflow(this.serializer.deserializeObject(WorkflowGraph.class, workflow), this.serializer.deserializeObject(Metadata.class, metadata));
	}

	public String xmlrpc_startWorkflow(String workflow, String metadata, String priority) throws Exception {
		return this.startWorkflow(this.serializer.deserializeObject(WorkflowGraph.class, workflow), this.serializer.deserializeObject(Metadata.class, metadata), this.serializer.deserializeObject(Priority.class, priority));
	}
	
	public String xmlrpc_startWorkflow2(String modelId, String inputMetadata) throws Exception {
		return this.startWorkflow(modelId, this.serializer.deserializeObject(Metadata.class, inputMetadata));
	}
	
	public String xmlrpc_startWorkflow2(String modelId, String inputMetadata, String priority) throws Exception {
		return this.startWorkflow(modelId, this.serializer.deserializeObject(Metadata.class, inputMetadata), this.serializer.deserializeObject(Priority.class, priority));
	}

	public String xmlrpc_stopWorkflow(String instanceId) throws Exception {
		this.stopWorkflow(instanceId);
		return this.serializer.serializeObject(Boolean.TRUE);
	}

	public String xmlrpc_updateInstanceMetadata(String jobId, String metadata) throws Exception {
		this.updateInstanceMetadata(jobId, this.serializer.deserializeObject(Metadata.class, metadata));
		return this.serializer.serializeObject(Boolean.TRUE);
	}
	
	public String xmlrpc_updateWorkflowMetadata(String instanceId, String modelId, String metadata)  throws Exception {
		this.updateWorkflowMetadata(instanceId, modelId, this.serializer.deserializeObject(Metadata.class, metadata));
		return this.serializer.serializeObject(Boolean.TRUE);
	}
	
	public String xmlrpc_updateWorkflowAndInstance(String instanceId, String modelId, String state, String metadata, String jobId, String instanceMetadata)  throws Exception {
		this.updateWorkflowAndInstance(instanceId, modelId, this.serializer.deserializeObject(WorkflowState.class, state), this.serializer.deserializeObject(Metadata.class, metadata), jobId, this.serializer.deserializeObject(Metadata.class, instanceMetadata));
		return this.serializer.serializeObject(Boolean.TRUE);
	}

    public String xmlrpc_registerEvent(String event) throws Exception {
    	this.registerEvent(this.serializer.deserializeObject(WorkflowEngineEvent.class, event));
		return this.serializer.serializeObject(Boolean.TRUE);
    }
    
    public String xmlrpc_triggerEvent(String eventId, String inputMetadata) throws Exception {
    	this.triggerEvent(eventId, this.serializer.deserializeObject(Metadata.class, inputMetadata));
		return this.serializer.serializeObject(Boolean.TRUE);
    }

	public String xmlrpc_getRegisteredEvents() throws Exception {
		return this.serializer.serializeObject(this.getRegisteredEvents());
	}
	
	public String xmlrpc_getSupportedStates() throws Exception {
		return this.serializer.serializeObject(this.getSupportedStates());
	}    

    public String xmlrpc_getNumOfLoadedProcessors() throws Exception {
		return this.serializer.serializeObject(Integer.valueOf(this.getNumOfLoadedProcessors()));
    }
    
    public String xmlrpc_getNumOfWorkflows() throws Exception {
    	return this.serializer.serializeObject(Integer.valueOf(this.getNumOfWorkflows()));
    }
    
    public String xmlrpc_getExecutingPage(String pageInfo) throws Exception {
    	return this.serializer.serializeObject(this.getExecutingPage(this.serializer.deserializeObject(PageInfo.class, pageInfo)));
    }
    
    public String xmlrpc_getRunnablesPage(String pageInfo) throws Exception {
    	return this.serializer.serializeObject(this.getRunnablesPage(this.serializer.deserializeObject(PageInfo.class, pageInfo)));
    }
    
    public String xmlrpc_getPage(String pageInfo) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo)));
    }

    public String xmlrpc_getPageWithFilterAndComparator(String pageInfo, String pageFilter, String comparator) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(PageFilter.class, pageFilter), this.serializer.deserializeObject(Comparator.class, comparator)));
    }
    
    public String xmlrpc_getPage_filtered(String pageInfo, String pageFilter) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(PageFilter.class, pageFilter)));
    }
    
    public String xmlrpc_getPage2(String pageInfo, String comparator) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(Comparator.class, comparator)));
    }
    
    public String xmlrpc_getPage3(String pageInfo, String state) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(WorkflowState.class, state)));
    }

    public String xmlrpc_getPage4(String pageInfo, String category) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(WorkflowState.Category.class, category)));
    }

    public String xmlrpc_getPage5(String pageInfo, String modelId) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), modelId));
    }

    public String xmlrpc_getPage7(String pageInfo, String keyValPairs) throws Exception {
    	return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(Map.class, keyValPairs)));
    }
    
    public String xmlrpc_getNextPage(String page) throws Exception {
    	return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueuePage.class, page)));
    }
	
	public String xmlrpc_getWorkflow(String instanceId) throws Exception {
		return this.serializer.serializeObject(this.getWorkflow(instanceId));
	}

	public String xmlrpc_getMetadata(String page) throws Exception {
		return this.serializer.serializeObject(this.getMetadata(this.serializer.deserializeObject(QueryPage.class, page)));
	}

	public String xmlrpc_getNextPage2(String page) throws Exception {
		return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueryPage.class, page)));
	}

	public String xmlrpc_getPage6(String pageInfo, String queryExpression)
			throws Exception {
		return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(QueryExpression.class, queryExpression)));
	}
    
}
