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

package org.apache.oodt.cas.workflow.gui.model.repo;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.model.ModelNode;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Model Repository which stores models in xml files
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class XmlWorkflowModelRepository {

  private File workspace;
  private List<File> files;
  private Set<ModelGraph> graphs;
  private Map<String, ConfigGroup> globalConfigGroups;

  public XmlWorkflowModelRepository(File workspace) {
    this.files = new Vector<File>();
    for (File file : (this.workspace = workspace).listFiles())
      if (!file.isDirectory())
        this.files.add(file);
  }

  public void loadGraphs(Set<String> supportedProcessorIds) throws Exception {
    this.graphs = new HashSet<ModelGraph>();
    HashMap<String, ConfigGroup> globalConfGroups = new HashMap<String, ConfigGroup>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder parser = factory.newDocumentBuilder();
    List<FileBasedElement> rootElements = new Vector<FileBasedElement>();
    for (File file : files) {
      System.out.println("Loading: " + file);
      rootElements.add(new FileBasedElement(file, parser.parse(file)
          .getDocumentElement()));
    }
    for (FileBasedElement root : rootElements) {
      loadConfiguration(rootElements, root, null, globalConfGroups);
      NodeList rootChildren = root.getElement().getChildNodes();
      for (int i = 0; i < rootChildren.getLength(); i++)
        if (rootChildren.item(i).getNodeType() == Node.ELEMENT_NODE
            && !rootChildren.item(i).getNodeName().equals("configuration")) {
          ModelGraph graph = this.loadGraph(rootElements, new FileBasedElement(
              root.getFile(), (Element) rootChildren.item(i)), new Metadata(),
              globalConfGroups, supportedProcessorIds);
          this.graphs.add(graph);
        }
    }
    insureUniqueIds(graphs);
    this.globalConfigGroups = globalConfGroups;
    System.out.println(this.globalConfigGroups.keySet());
  }

  public Set<ModelGraph> getGraphs() {
    return this.graphs;
  }

  public Map<String, ConfigGroup> getGlobalConfigGroups() {
    return this.globalConfigGroups;
  }

  public List<File> getFiles() {
    return this.files;
  }

  public void save(List<ModelGraph> graphs,
      Map<String, Metadata> globalConfigGroups) throws Exception {
    this.backupCurrentFiles();
  }

  private void backupCurrentFiles() throws Exception {
    File backupDir = new File(this.workspace, ".backup");
    for (File file : this.files) {
      FileUtils.copyFile(file, new File(backupDir, file.getName()));
      file.delete();
    }
    this.files.clear();
  }

  private void insureUniqueIds(Set<ModelGraph> graphs) {
    for (ModelGraph graph : graphs) {
      HashSet<String> names = new HashSet<String>();
      Vector<ModelGraph> stack = new Vector<ModelGraph>();
      stack.add(graph);
      while (!stack.isEmpty()) {
        ModelGraph currentGraph = stack.remove(0);
        String currentId = currentGraph.getId();
        for (int i = 1; names.contains(currentId); i++)
          currentId = currentGraph.getId() + "-" + i;
        names.add(currentId);
        if (!currentId.equals(currentGraph.getId()))
          currentGraph.getModel().setModelId(currentId);
        stack.addAll(currentGraph.getChildren());
      }
    }
  }

  private ModelGraph loadGraph(List<FileBasedElement> rootElements,
      FileBasedElement workflowNode, Metadata staticMetadata,
      HashMap<String, ConfigGroup> globalConfGroups,
      Set<String> supportedProcessorIds) throws Exception {
    String modelIdRef = null;
    String modelId = null;
    String modelName = null;
    String alias = null;
    String executionType = null;
    List<String> excused = new Vector<String>();
    String clazz = null;
    boolean entryPoint = false;

    NamedNodeMap attributes = workflowNode.getElement().getAttributes();
    for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
      Node node = workflowNode.getElement().getAttributes().item(i);
      if (node.getNodeName().equals("id")) {
        modelId = node.getNodeValue();
      } else if (node.getNodeName().equals("name")) {
        modelName = node.getNodeValue();
      } else if (node.getNodeName().equals("class")) {
        clazz = node.getNodeValue();
      } else if (node.getNodeName().equals("id-ref")) {
        modelIdRef = node.getNodeValue();
      } else if (node.getNodeName().equals("excused")) {
        excused.addAll(Arrays.asList(node.getNodeValue().split(",")));
      } else if (node.getNodeName().equals("entryPoint")) {
        entryPoint = Boolean.parseBoolean(node.getNodeValue());
      } else if (node.getNodeName().equals("alias")) {
        alias = node.getNodeValue();
      } else if (node.getNodeName().equals("execution")) {
        executionType = node.getNodeValue();
      } else if (node.getNodeName().startsWith("p:")) {
        staticMetadata.replaceMetadata(node.getNodeName().substring(2),
            node.getNodeValue());
      }
    }

    if (modelId == null && modelIdRef == null)
      modelId = UUID.randomUUID().toString();

    ModelGraph graph = null;
    if (modelId != null) {

      if (workflowNode.getElement().getNodeName().equals("workflow")
          || workflowNode.getElement().getNodeName().equals("conditions")) {
        if (executionType == null)
          throw new Exception("workflow model '"
              + workflowNode.getElement().getNodeName()
              + "' missing execution type");
      } else {
        executionType = workflowNode.getElement().getNodeName();
      }

      if (!supportedProcessorIds.contains(executionType))
        throw new Exception("Unsupported execution type id '" + executionType
            + "'");

      ModelNode modelNode = new ModelNode(workflowNode.getFile());
      modelNode.setModelId(modelId);
      modelNode.setModelName(modelName);
      modelNode.setExecutionType(executionType);
      modelNode.setStaticMetadata(staticMetadata);
      modelNode.setExcusedSubProcessorIds(excused);
      modelNode.setInstanceClass(clazz);
      modelNode.setEntryPoint(entryPoint);

      loadConfiguration(rootElements, workflowNode, modelNode, globalConfGroups);

      graph = new ModelGraph(modelNode);

      boolean loadedPreConditions = false;
      NodeList children = workflowNode.getElement().getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node curChild = children.item(i);
        if (curChild.getNodeType() == Node.ELEMENT_NODE) {
          if (curChild.getNodeName().equals("conditions")) {
            boolean isPreCondition = !loadedPreConditions;
            String type = ((Element) curChild).getAttribute("type");
            if (type.length() > 0)
              isPreCondition = type.toLowerCase().equals("pre");
            if (isPreCondition)
              graph.setPreConditions(this.loadGraph(rootElements,
                  new FileBasedElement(workflowNode.getFile(),
                      (Element) curChild), new Metadata(staticMetadata),
                  globalConfGroups, supportedProcessorIds));
            else
              graph.setPostConditions(this.loadGraph(rootElements,
                  new FileBasedElement(workflowNode.getFile(),
                      (Element) curChild), new Metadata(staticMetadata),
                  globalConfGroups, supportedProcessorIds));
            loadedPreConditions = true;
          } else if (!curChild.getNodeName().equals("configuration")) {
            graph.addChild(this.loadGraph(rootElements, new FileBasedElement(
                workflowNode.getFile(), (Element) curChild), new Metadata(
                staticMetadata), globalConfGroups, supportedProcessorIds));
          }
        }
      }

    } else if (modelIdRef != null) {
      graph = this.findGraph(rootElements, modelIdRef, new Metadata(
          staticMetadata), globalConfGroups, supportedProcessorIds);
      if (graph == null)
        throw new Exception("Workflow '" + modelIdRef
            + "' has not been defined in this context");
      graph.setIsRef(true);
      graph.getModel().setStaticMetadata(new Metadata());
      loadConfiguration(rootElements, workflowNode, graph.getModel(),
          globalConfGroups);
      if (alias != null)
        graph.getModel().setModelId(alias);
    }

    if (entryPoint && graph.getParent() != null) {
      this.graphs.add(graph);
    }

    return graph;
  }

  protected ModelGraph findGraph(List<FileBasedElement> rootElements,
      String modelIdRef, Metadata staticMetadata,
      HashMap<String, ConfigGroup> globalConfGroups,
      Set<String> supportedProcessorIds) throws Exception {
    XPath xpath = XPathFactory.newInstance().newXPath();
    XPathExpression expr = xpath.compile("//*[@id = '" + modelIdRef + "']");
    for (FileBasedElement rootElement : rootElements) {
      Node node = (Node) expr.evaluate(rootElement.getElement(),
          XPathConstants.NODE);
      if (node != null) {
        return this.loadGraph(rootElements,
            new FileBasedElement(rootElement.getFile(), (Element) node),
            staticMetadata, globalConfGroups, supportedProcessorIds);
      }
    }
    return null;
  }

  private void loadConfiguration(List<FileBasedElement> rootElements,
      FileBasedElement workflowNode, ModelNode modelNode,
      HashMap<String, ConfigGroup> globalConfGroups) throws Exception {
    NodeList children = workflowNode.getElement().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curChild = children.item(i);
      if (curChild.getNodeName().equals("configuration")) {
        Metadata curMetadata = new Metadata();
        if (modelNode != null
            && !((Element) curChild).getAttribute("extends").equals(""))
          modelNode.setExtendsConfig(Arrays.asList(((Element) curChild)
              .getAttribute("extends").split(",")));
        curMetadata.replaceMetadata(this.loadConfiguration(rootElements,
            curChild, globalConfGroups));
        if (!((Element) curChild).getAttribute("name").equals("")) {
          ConfigGroup configGroup = new ConfigGroup(
              ((Element) curChild).getAttribute("name"), curMetadata);
          if (modelNode != null) {
            List<String> extendsConfig = new Vector<String>(
                modelNode.getExtendsConfig());
            configGroup.addAllExtends(extendsConfig);
            extendsConfig.add(configGroup.getName());
            modelNode.setExtendsConfig(extendsConfig);
          }
          globalConfGroups.put(((Element) curChild).getAttribute("name"),
              configGroup);
        } else if (modelNode != null) {
          modelNode.setStaticMetadata(curMetadata);
        }
      }
    }
  }

  private Metadata loadConfiguration(List<FileBasedElement> rootElements,
      Node configNode, HashMap<String, ConfigGroup> globalConfGroups)
      throws Exception {
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
          curMetadata.replaceMetadata(name + "/envReplace", "true");
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

  private class FileBasedElement {

    private File file;
    private Element element;

    public FileBasedElement(File file, Element element) {
      this.file = file;
      this.element = element;
    }

    public File getFile() {
      return this.file;
    }

    public Element getElement() {
      return this.element;
    }

  }

  public class ConfigGroup {

    private String name;
    private Metadata metadata;
    private List<String> extendsConfig;

    public ConfigGroup(String name, Metadata metadata) {
      this.name = name;
      this.metadata = metadata;
      this.extendsConfig = new Vector<String>();
    }

    public String getName() {
      return this.name;
    }

    public Metadata getMetadata() {
      return this.metadata;
    }

    public void addExtends(String child) {
      this.extendsConfig.add(child);
    }

    public void addAllExtends(List<String> children) {
      this.extendsConfig.addAll(children);
    }

    public void removeExtends(String child) {
      this.extendsConfig.remove(child);
    }

    public List<String> getExtends() {
      return this.extendsConfig;
    }

    public int hashCode() {
      return this.name.hashCode();
    }

    public boolean equals(Object obj) {
      if (obj instanceof ConfigGroup) {
        ConfigGroup comp = (ConfigGroup) obj;
        return comp.name.equals(this.name);
      } else
        return false;
    }

    public String toString() {
      return this.name;
    }

  }

}
