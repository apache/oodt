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
package org.apache.oodt.cas.workflow.model.repo;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.model.WorkflowGraph;
import org.apache.oodt.cas.workflow.model.WorkflowModelFactory;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.processor.ConditionProcessor;
import org.apache.oodt.cas.workflow.processor.ParallelProcessor;
import org.apache.oodt.cas.workflow.processor.SequentialProcessor;
import org.apache.oodt.cas.workflow.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

//JDK imports
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

//JAVAX imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

//DOM imports
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Model Repository which stores models in xml files
 * </p>.
 */
public class XmlWorkflowModelRepository implements WorkflowModelRepository {

	private List<File> files;
	
	public XmlWorkflowModelRepository(List<File> files) throws InstantiationException {
		try {
			this.files = files;
		}catch (Exception e) {
			e.printStackTrace();
			throw new InstantiationException();
		}
	}
	
	public Map<String, WorkflowGraph> loadGraphs(Set<String> supportedProcessorIds) throws Exception {
		HashMap<String, WorkflowGraph> graphs = new HashMap<String, WorkflowGraph>();
		HashMap<String, Metadata> globalConfGroups = new HashMap<String, Metadata>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder parser = factory.newDocumentBuilder();
		List<Element> rootElements = new Vector<Element>();
		for (File file : files) 
			rootElements.add(parser.parse(file).getDocumentElement());
		for (Element root : rootElements) {
			loadConfiguration(rootElements, root, new Metadata(), globalConfGroups);
			NodeList rootChildren = root.getChildNodes();
			for (int i = 0; i < rootChildren.getLength(); i++)
				if (rootChildren.item(i).getNodeType() == Node.ELEMENT_NODE && !rootChildren.item(i).getNodeName().equals("configuration")) {
					WorkflowGraph graph = this.loadGraph(rootElements, rootChildren.item(i), new Metadata(), globalConfGroups, graphs, Priority.getDefault(), supportedProcessorIds);
					graphs.put(graph.getId(), graph);
				}
		}
		insureUniqueIds(graphs);
		return graphs;
	}
	
	private void insureUniqueIds(HashMap<String, WorkflowGraph> graphs) {
		for (WorkflowGraph graph : graphs.values()) {
			HashSet<String> names = new HashSet<String>();
			Vector<WorkflowGraph> stack = new Vector<WorkflowGraph>();
			stack.add(graph);
			while(!stack.isEmpty()) {
				WorkflowGraph currentGraph = stack.remove(0);
				String currentId = currentGraph.getId();
				for (int i = 1; names.contains(currentId); i++)
					currentId = currentGraph.getId() + "-" + i;
				names.add(currentId);
				if (!currentId.equals(currentGraph.getId())) 
					this.changeModelId(currentGraph, currentId);
				stack.addAll(currentGraph.getChildren());
			}
		}
	}
	
	private void changeModelId(WorkflowGraph graph, String newModelId) {
		WorkflowModelFactory factory = new WorkflowModelFactory(graph.getModel());
		factory.setModelId(newModelId);
		graph.setModel(factory.createModel());
	}

	private WorkflowGraph loadGraph(List<Element> rootElements, Node workflowNode, Metadata staticMetadata, HashMap<String, Metadata> globalConfGroups, HashMap<String, WorkflowGraph> graphs, Priority priority, Set<String> supportedProcessorIds) throws Exception {
		String modelIdRef = null;
		String modelId = null;
		String modelName = null;
		String alias = null;
		String executionType = null;
		String minReqSuccessfulSubProcessors = null;
		List<String> excused = new Vector<String>();
		String clazz = null;
		boolean entryPoint = false;
		
		NamedNodeMap attributes = workflowNode.getAttributes();
		for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
			Node node = workflowNode.getAttributes().item(i);
			if (node.getNodeName().equals("id")) {
				modelId = node.getNodeValue();
			}else if (node.getNodeName().equals("name")) {
				modelName = node.getNodeValue();
			}else if (node.getNodeName().equals("class")) {
				clazz = node.getNodeValue();
			}else if (node.getNodeName().equals("id-ref")) {
				modelIdRef = node.getNodeValue();
			}else if (node.getNodeName().equals("excused")) {
				excused.addAll(Arrays.asList(node.getNodeValue().split(",")));
			}else if (node.getNodeName().equals("entryPoint")) {
				entryPoint = Boolean.parseBoolean(node.getNodeValue());
			}else if (node.getNodeName().equals("alias")) {
				alias = node.getNodeValue();
			}else if (node.getNodeName().equals("min")) {
				minReqSuccessfulSubProcessors = node.getNodeValue();		
			}else if (node.getNodeName().equals("execution")) {
				executionType = node.getNodeValue();
			}else if (node.getNodeName().equals("priority")) {
				priority = Priority.getPriority(Double.parseDouble(node.getNodeValue()));
			}else if (node.getNodeName().startsWith("p:")) {
				staticMetadata.replaceMetadata(node.getNodeName().substring(2), node.getNodeValue());
			}
		}
		
		if (modelId == null && modelIdRef == null) 
			modelId = UUID.randomUUID().toString();
				
		WorkflowGraph graph = null;
		if (modelId != null) {
			
			if (workflowNode.getNodeName().equals("workflow") || workflowNode.getNodeName().equals("conditions")) {
				if (executionType == null)
					throw new Exception("workflow model '" + workflowNode.getNodeName() + "' missing execution type");
			}else {
				executionType = workflowNode.getNodeName();
			}
			
			if (!supportedProcessorIds.contains(executionType))
				throw new Exception("Unsupported execution type id '" + executionType + "'");

			loadConfiguration(rootElements, workflowNode, staticMetadata, globalConfGroups);
			
			WorkflowModelFactory modelFactory = new WorkflowModelFactory();
			modelFactory.setModelId(modelId);
			modelFactory.setModelName(modelName);
			modelFactory.setExecutionType(executionType);				
			modelFactory.setPriority(priority);
			if (minReqSuccessfulSubProcessors != null)
				modelFactory.setMinReqSuccessfulSubProcessors(Integer.parseInt(minReqSuccessfulSubProcessors));
			modelFactory.setStaticMetadata(staticMetadata);
			modelFactory.setExcusedSubProcessorIds(excused);
			modelFactory.setInstanceClass(clazz);
			
			graph = new WorkflowGraph(modelFactory.createModel());
			
			boolean loadedPreConditions = false;
			NodeList children = workflowNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node curChild = children.item(i);
				if (curChild.getNodeType() == Node.ELEMENT_NODE) {
					if (curChild.getNodeName().equals("conditions")) {
						boolean isPreCondition = !loadedPreConditions;
						String type = ((Element) curChild).getAttribute("type");
						if (type.length() > 0)
							isPreCondition = type.toLowerCase().equals("pre");
						if (isPreCondition) 
							graph.setPreConditions(this.loadGraph(rootElements, curChild, new Metadata(staticMetadata), globalConfGroups, graphs, priority, supportedProcessorIds));
						else 
							graph.setPostConditions(this.loadGraph(rootElements, curChild, new Metadata(staticMetadata), globalConfGroups, graphs, priority, supportedProcessorIds));
						loadedPreConditions = true;
					}else if (!curChild.getNodeName().equals("configuration")){
						graph.addChild(this.loadGraph(rootElements, curChild, new Metadata(staticMetadata), globalConfGroups, graphs, priority, supportedProcessorIds));
					}
				}
			}
			
		}else if (modelIdRef != null) {
			loadConfiguration(rootElements, workflowNode, staticMetadata, globalConfGroups);
			graph = this.findGraph(rootElements, modelIdRef, new Metadata(staticMetadata), globalConfGroups, graphs, priority, supportedProcessorIds);
			if (graph == null)
				throw new Exception("Workflow '" + modelIdRef + "' has not been defined in this context");
			if (alias != null)
				this.changeModelId(graph, alias);
		}

		if (entryPoint) {
			if (graphs.containsKey(graph.getId()))
				throw new Exception("Entry points must have globally unique ModelIds: '" + graph.getId() + "' is used more than once");
			graphs.put(graph.getId(), graph);
		}
		
		return graph;
	}
	
	protected WorkflowGraph findGraph(List<Element> rootElements, String modelIdRef, Metadata staticMetadata, HashMap<String, Metadata> globalConfGroups, HashMap<String, WorkflowGraph> graphs, Priority priority, Set<String> supportedProcessorIds) throws Exception {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//*[@id = '" + modelIdRef + "']");
		for (Element rootElement : rootElements) { 
			Node node = (Node) expr.evaluate(rootElement, XPathConstants.NODE);
			if (node != null)
				return this.loadGraph(rootElements, node, staticMetadata, globalConfGroups, graphs, priority, supportedProcessorIds);
		}
		return null;
	}
	
	private void loadConfiguration(List<Element> rootElements, Node workflowNode, Metadata staticMetadata, HashMap<String, Metadata> globalConfGroups) throws Exception {
		NodeList children = workflowNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node curChild = children.item(i);
			if (curChild.getNodeName().equals("configuration")) {
				Metadata curMetadata = new Metadata();
				if (!((Element) curChild).getAttribute("extends").equals("")) 
					for (String extension : ((Element) curChild).getAttribute("extends").split(","))
						curMetadata.replaceMetadata(globalConfGroups.containsKey(extension) ? globalConfGroups.get(extension) :	this.tempLoadConfGroup(rootElements, extension, globalConfGroups));
				curMetadata.replaceMetadata(this.loadConfiguration(rootElements, curChild, globalConfGroups));
				if (curChild.hasAttributes() && !((Element) curChild).getAttribute("name").equals(""))
					globalConfGroups.put(((Element) curChild).getAttribute("name"), curMetadata);
				staticMetadata.replaceMetadata(curMetadata);
			}
		}
	}
	
	private Metadata loadConfiguration(List<Element> rootElements, Node configNode, HashMap<String, Metadata> globalConfGroups) throws Exception {
		Metadata curMetadata = new Metadata();					
		NodeList curGrandChildren = configNode.getChildNodes();
		for (int k = 0; k < curGrandChildren.getLength(); k++) {
			if (curGrandChildren.item(k).getNodeName().equals("property")) {
				Element property = (Element) curGrandChildren.item(k);
				String delim = property.getAttribute("delim");
				String envReplace = property.getAttribute("envReplace");
				String name = property.getAttribute("name");
				String value = property.getAttribute("value");
				if (Boolean.parseBoolean(envReplace))
					value = PathUtils.doDynamicReplacement(value);
				List<String> values = new Vector<String>();
				if (delim.length() > 0)
					values.addAll(Arrays.asList(value.split("\\" + delim)));
				else
					values.add(value);
				curMetadata.replaceMetadata(name, values);
			}
		}
		return curMetadata;
	}
	
	private Metadata tempLoadConfGroup(List<Element> rootElements, String group, HashMap<String, Metadata> globalConfGroups) throws Exception {
		for (final Element rootElement : rootElements) {
			NodeList nodes = rootElement.getElementsByTagName("configuration");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String name = ((Element) node).getAttribute("name");
				if (name.equals(group))
					return this.loadConfiguration(rootElements, node, globalConfGroups);
			}
		}
		throw new Exception("Configuration group '" + group + "' not defined!");
	}
	
	public static void main(String[] args) throws Exception {
		XmlWorkflowModelRepositoryFactory factory = new XmlWorkflowModelRepositoryFactory();
		factory.setModelFiles(Arrays.asList("src/main/resources/policy/workflows/GranuleMaps.xml", "src/main/resources/policy/workflows/properties.xml"));
		HashMap<String, Class<? extends WorkflowProcessor>> modelToProcessorMap = new HashMap<String, Class<? extends WorkflowProcessor>>();
		modelToProcessorMap.put("sequential", SequentialProcessor.class);
		modelToProcessorMap.put("parallel", ParallelProcessor.class);
		modelToProcessorMap.put("task", TaskProcessor.class);
		modelToProcessorMap.put("condition", ConditionProcessor.class);
		Map<String, WorkflowGraph> graphs = factory.createModelRepository().loadGraphs(modelToProcessorMap.keySet());
		System.out.println(graphs.keySet());
		WorkflowGraph graph = graphs.get("urn:npp:GranuleMaps");
		System.out.println(WorkflowUtils.toString(graph));
		System.out.println(graph.getModel().getStaticMetadata().getMetadata("BlockTimeElapse"));
	}
	
}
