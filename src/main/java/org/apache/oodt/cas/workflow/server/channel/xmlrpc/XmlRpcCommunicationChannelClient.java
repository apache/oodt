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
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

//APACHE imports
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.xmlrpc.CommonsXmlRpcTransport;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransportFactory;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.event.WorkflowEngineEvent;
import org.apache.oodt.cas.workflow.instance.repo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.model.WorkflowGraph;
import org.apache.oodt.cas.workflow.model.WorkflowModel;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.page.QueryPage;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.page.RunnablesPage;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.processor.ProcessorInfo;
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.server.channel.AbstractCommunicationChannelClient;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * XML-RPC communication channel client
 * <p>
 */
public class XmlRpcCommunicationChannelClient extends
		AbstractCommunicationChannelClient {

	protected XmlRpcClient client;
	protected int chunkSize;
	
	public XmlRpcCommunicationChannelClient(final URL serverUrl, final int connectionTimeout, final int requestTimeout, final int chunkSize, final int connectionRetries, final int connectionRetryIntervalSecs) {
		super();
		XmlRpcTransportFactory transportFactory = new XmlRpcTransportFactory() {

			public XmlRpcTransport createTransport()
					throws XmlRpcClientException {
				HttpClient client = new HttpClient();
				client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
						new HttpMethodRetryHandler() {

					public boolean retryMethod(HttpMethod method, IOException e,
							int count) {
			    		if (count < connectionRetries) {
			    			try {
			    				Thread.sleep(connectionRetryIntervalSecs * 1000);
			    				return true;
			    			}catch (Exception e1) {}
			    		}
			    		return false;
					}
		        	
		        });
				CommonsXmlRpcTransport transport = new CommonsXmlRpcTransport(serverUrl, client);
				transport.setConnectionTimeout(connectionTimeout * 60 * 1000);
				transport.setTimeout(requestTimeout * 60 * 1000);

				return transport;
			}

			public void setProperty(String arg0, Object arg1) {}

        };       
		this.client = new XmlRpcClient(serverUrl, transportFactory);
		this.chunkSize = chunkSize;
	}
	
	public void shutdown() throws Exception {
		Vector<Object> args = new Vector<Object>();
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_shutdown", args);
	}
	
	public void pauseRunner() throws Exception {
		Vector<Object> args = new Vector<Object>();
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_pauseRunner", args);
	}
	
	public void resumeRunner() throws Exception {
		Vector<Object> args = new Vector<Object>();
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_resumeRunner", args);
	}
	
	public Date getLaunchDate() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(Date.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getLaunchDate", args));
	}
	
	public void deleteWorkflow(String instanceId)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_deleteWorkflow", args);
	}

	public Metadata getInstanceMetadata(String jobId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(jobId);
		return this.serializer.deserializeObject(Metadata.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getInstanceMetadata", args));
	}

	public WorkflowInstanceRepository getInstanceRepository() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(WorkflowInstanceRepository.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getInstanceRepository", args));
	}
	
	public Set<String> getSupportedProcessorIds() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(Set.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getSupportedProcessorIds", args));
	}

	public WorkflowModel getModel(String modelId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(modelId);
		return this.serializer.deserializeObject(WorkflowModel.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getModel", args));
	}

	public List<WorkflowModel> getModels() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getModels", args));
	}
	
	public WorkflowGraph getWorkflowGraph(String modelId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(modelId);
		return this.serializer.deserializeObject(WorkflowGraph.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getWorkflowGraph", args));
	}

	public List<WorkflowGraph> getWorkflowGraphs() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getWorkflowGraphs", args));
	}
	
    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		args.add(modelId);
		return this.serializer.deserializeObject(ProcessorInfo.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getProcessorInfo", args));
    }

	public Metadata getWorkflowMetadata(String instanceId,
			String modelId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		args.add(modelId);
		return this.serializer.deserializeObject(Metadata.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getWorkflowMetadata", args));
	}

	public void pauseWorkflow(String instanceId)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_pauseWorkflow", args);
	}

	public void resumeWorkflow(String instanceId)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_resumeWorkflow", args);
	}

	public void setWorkflowState(String instanceId,
			String modelId, WorkflowState state) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		args.add(modelId);
		args.add(this.serializer.serializeObject(state));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setWorkflowState", args);
	}
	
	public void setWorkflowPriority(String instanceId,
			String modelId, Priority priority) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		args.add(modelId);
		args.add(this.serializer.serializeObject(priority));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setWorkflowPriority", args);
	}

	public String startWorkflow(WorkflowGraph workflow, Metadata metadata)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(workflow));
		args.add(this.serializer.serializeObject(metadata));
		return (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_startWorkflow", args);
	}

    public String startWorkflow(WorkflowGraph workflow, Metadata metadata, Priority priority) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(workflow));
		args.add(this.serializer.serializeObject(metadata));
		args.add(this.serializer.serializeObject(priority));
		return (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_startWorkflow", args);
    }
    
	public String startWorkflow(String modelId, Metadata inputMetadata)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(modelId);
		args.add(this.serializer.serializeObject(inputMetadata));
		return (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_startWorkflow2", args);
	}
	
	public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(modelId);
		args.add(this.serializer.serializeObject(inputMetadata));
		args.add(this.serializer.serializeObject(priority));
		return (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_startWorkflow2", args);
	}

	public void stopWorkflow(String instanceId)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_stopWorkflow", args);
	}

	public void updateInstanceMetadata(String jobId, Metadata metadata)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(jobId);
		args.add(this.serializer.serializeObject(metadata));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_updateInstanceMetadata", args);
	}

	public void updateWorkflowMetadata(String instanceId,
			String modelId, Metadata metadata) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		args.add(modelId);
		args.add(this.serializer.serializeObject(metadata));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_updateWorkflowMetadata", args);
	}
	
	public void updateWorkflowAndInstance(String instanceId,
			String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		args.add(modelId);
		args.add(this.serializer.serializeObject(state));
		args.add(this.serializer.serializeObject(metadata));
		args.add(jobId);
		args.add(this.serializer.serializeObject(instanceMetadata));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_updateWorkflowAndInstance", args);
	}
    
    public void registerEvent(WorkflowEngineEvent event) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(event));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_registerEvent", args);
    }
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(eventId);
		args.add(this.serializer.serializeObject(inputMetadata));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_triggerEvent", args);
    }
    
    public List<WorkflowEngineEvent> getRegisteredEvents() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getRegisteredEvents", args));
    }
    
    public List<WorkflowState> getSupportedStates() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getSupportedStates", args));
    }
    
    public int getNumOfLoadedProcessors() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(Integer.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNumOfLoadedProcessors", args));
    }
    
    public int getNumOfWorkflows() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(Integer.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNumOfWorkflows", args));
    }
    
    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		return this.serializer.deserializeObject(RunnablesPage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getExecutingPage", args));
    }
    
    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		return this.serializer.deserializeObject(RunnablesPage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getRunnablesPage", args));
    }
    
    public QueuePage getPage(PageInfo pageInfo) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage", args));
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(filter));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage_filtered", args));
    }

    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(comparator));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage2", args));
    }
     
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(state));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage3", args));
    }

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(category));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage4", args));    	
    }

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(modelId);
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage5", args));  
    }
    
    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(keyValPairs));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage7", args));  
    }

    public QueuePage getNextPage(QueuePage page) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
		return this.serializer.deserializeObject(QueuePage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNextPage", args));  
    }
	
    public ProcessorSkeleton getWorkflow(String instanceId) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(instanceId);
		return this.serializer.deserializeObject(ProcessorSkeleton.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getWorkflow", args));
    }

	public List<Metadata> getMetadata(QueryPage page) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadata", args));
	}

	public QueryPage getNextPage(QueryPage page) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
		return this.serializer.deserializeObject(QueryPage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNextPage2", args));
	}

	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(queryExpression));
		return this.serializer.deserializeObject(QueryPage.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage6", args));
	}
	
}
