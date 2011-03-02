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
package org.apache.oodt.cas.workflow.server.channel.rmi;

//JDK imports
import java.lang.management.ManagementFactory;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;

//JAVAX imports
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

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
 * 
 * @author bfoster
 *
 */
public class RmiCommunicationChannelServer extends AbstractCommunicationChannelServer implements RmiCommunicationChannelServerInterface, RmiCommunicationChannelServerMBean {

	private String name;
    private MBeanServer mbs;
    private ObjectName objName;
    
	public RmiCommunicationChannelServer(int port, String name) throws MalformedObjectNameException, NullPointerException {
		this.port = port;
		this.name = name;
	}

	public void startup() throws Exception {

		//register JMX
        try {
        	this.objName = new ObjectName(
                    "org.apache.oodt.cas.workflow.server.channel.rmi:type=RmiCommunicationChannelServer");
            LocateRegistry.createRegistry(this.port);
            mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(this, objName);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Failed to register DaemonLauncher as a MBean Object : "
                            + e.getMessage(), e);
        }
		
        //bind RMI
		try {
			Naming.bind("//localhost:" + this.port + "/" + name, UnicastRemoteObject.exportObject(this, this.port));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("Failed to bind Workflow Engine to RMI registry at port "
					+ this.port);
		}		

	}
	
	public int getPort() {
		return this.port;
	}

	public void rmi_shutdown() throws RemoteException {
		try {
			this.shutdown();
			Naming.unbind("//localhost:" + this.port + "/" + this.name);
			this.mbs.unregisterMBean(this.objName);
	        UnicastRemoteObject.unexportObject(this, true);
			this.mbs = null;
			this.objName = null;
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	public void rmi_pauseRunner() throws RemoteException {
		try {
			this.pauseRunner();
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	public void rmi_resumeRunner() throws RemoteException {
		try {
			this.resumeRunner();
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	public String rmi_getLaunchDate() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getLaunchDate());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
    public String rmi_startWorkflow_WithModel(String workflow, String metadata) throws RemoteException {
		try {
			return this.startWorkflow(this.serializer.deserializeObject(WorkflowGraph.class, workflow), this.serializer.deserializeObject(Metadata.class, metadata));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_startWorkflow_WithModel(String workflow, String metadata, String priority) throws RemoteException {
		try {
			return this.startWorkflow(this.serializer.deserializeObject(WorkflowGraph.class, workflow), this.serializer.deserializeObject(Metadata.class, metadata), this.serializer.deserializeObject(Priority.class, priority));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_startWorkflow_WithModelId(String modelId, String inputMetadata) throws RemoteException {
		try {
			return this.startWorkflow(modelId, this.serializer.deserializeObject(Metadata.class, inputMetadata));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_startWorkflow_WithModelId(String modelId, String inputMetadata, String priority) throws RemoteException {
		try {
			return this.startWorkflow(modelId, this.serializer.deserializeObject(Metadata.class, inputMetadata), this.serializer.deserializeObject(Priority.class, priority));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public void rmi_deleteWorkflow(String instanceId) throws RemoteException {
		try {
			this.deleteWorkflow(instanceId);
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public void rmi_stopWorkflow(String instanceId) throws RemoteException {
		try {
			this.stopWorkflow(instanceId);
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public void rmi_pauseWorkflow(String instanceId) throws RemoteException {
		try {
			this.pauseWorkflow(instanceId);
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public void rmi_resumeWorkflow(String instanceId) throws RemoteException {
		try {
			this.resumeWorkflow(instanceId);
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getModel(String modelId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getModel(modelId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getWorkflowGraph(String modelId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflowGraph(modelId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getSupportedProcessorIds() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getSupportedProcessorIds());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getModels() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getModels());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getWorkflowGraphs() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflowGraphs());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getWorkflowStub(String instanceId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflowStub(instanceId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getWorkflowStub(String instanceId, String modelId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflowStub(instanceId, modelId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getProcessorInfo(String instanceId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getProcessorInfo(instanceId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getProcessorInfo(String instanceId, String modelId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getProcessorInfo(instanceId, modelId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getWorkflowState(String instanceId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.rmi_getWorkflowState(instanceId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getInstanceRepository() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getInstanceRepository());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public void rmi_updateWorkflowMetadata(String instanceId, String modelId, String metadata) throws RemoteException {
		try {
			this.updateWorkflowMetadata(instanceId, modelId, this.serializer.deserializeObject(Metadata.class, metadata));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public void rmi_updateInstanceMetadata(String jobId, String metadata) throws RemoteException {
		try {
			this.updateInstanceMetadata(jobId, this.serializer.deserializeObject(Metadata.class, metadata));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public void rmi_updateWorkflowAndInstance(String instanceId, String modelId, String state, String metadata, String jobId, String instanceMetadata) throws RemoteException {
		try {
			this.updateWorkflowAndInstance(instanceId, modelId, this.serializer.deserializeObject(WorkflowState.class, state), this.serializer.deserializeObject(Metadata.class, metadata), jobId, this.serializer.deserializeObject(Metadata.class, instanceMetadata));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public void rmi_setWorkflowState(String instanceId, String modelId, String state) throws RemoteException {
		try {
			this.setWorkflowState(instanceId, modelId, this.serializer.deserializeObject(WorkflowState.class, state));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public void rmi_setWorkflowPriority(String instanceId, String modelId, String priority) throws RemoteException {
		try {
			this.setWorkflowPriority(instanceId, modelId, this.serializer.deserializeObject(Priority.class, priority));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getWorkflowMetadata(String instanceId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflowMetadata(instanceId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getWorkflowMetadata(String instanceId, String modelId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflowMetadata(instanceId, modelId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getInstanceMetadata(String jobId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getInstanceMetadata(jobId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
            
    public void rmi_registerEvent(String event) throws RemoteException {
		try {
			this.registerEvent(this.serializer.deserializeObject(WorkflowEngineEvent.class, event));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public void rmi_triggerEvent(String eventId, String inputMetadata) throws RemoteException {
		try {
			this.triggerEvent(eventId, this.serializer.deserializeObject(Metadata.class, inputMetadata));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getRegisteredEvents() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getRegisteredEvents());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getSupportedStates() throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getSupportedStates());
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getNumOfLoadedProcessors() throws RemoteException {
		try {
			return this.serializer.serializeObject(new Integer(this.getNumOfLoadedProcessors()));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getNumOfWorkflows() throws RemoteException {
		try {
			return this.serializer.serializeObject(new Integer(this.getNumOfWorkflows()));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getExecutingPage(String pageInfo) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getExecutingPage(this.serializer.deserializeObject(PageInfo.class, pageInfo)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getRunnablesPage(String pageInfo) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getRunnablesPage(this.serializer.deserializeObject(PageInfo.class, pageInfo)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getPage(String pageInfo) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getPage_WithFilter(String pageInfo, String filter) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(PageFilter.class, filter)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getPage_WithComparator(String pageInfo, String comparator) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(Comparator.class, comparator)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getPage_WithFilterAndComparator(String pageInfo, String filter, String comparator) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(PageFilter.class, filter), this.serializer.deserializeObject(Comparator.class, comparator)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
     
    public String rmi_getPage_WithState(String pageInfo, String state) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(WorkflowState.class, state)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getPage_WithCategory(String pageInfo, String category) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(WorkflowState.Category.class, category)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getPage_WithModelId(String pageInfo, String modelId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), modelId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }

    public String rmi_getPage_WithMap(String pageInfo, String keyValPairs) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(Map.class, keyValPairs)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getNextQueuePage(String page) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueuePage.class, page)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
    public String rmi_getWorkflow(String instanceId) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getWorkflow(instanceId));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
    }
    
	public String rmi_getNextQueryPage(String page) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueryPage.class, page)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	public String rmi_getQueryPage(String pageInfo, String queryExpression) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfo), this.serializer.deserializeObject(QueryExpression.class, queryExpression)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	public String rmi_getMetadata(String page) throws RemoteException {
		try {
			return this.serializer.serializeObject(this.getMetadata(this.serializer.deserializeObject(QueryPage.class, page)));
		}catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
}
