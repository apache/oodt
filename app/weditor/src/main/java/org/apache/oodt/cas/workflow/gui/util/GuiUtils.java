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

package org.apache.oodt.cas.workflow.gui.util;

//OODT imports
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.model.ModelNode;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * Generic utility functions helpful for the Workflow Editor GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class GuiUtils {

  protected static AtomicInteger untitledIter = new AtomicInteger(0);
  protected static AtomicInteger dummyUntitledIter = new AtomicInteger(0);

  public static boolean isSubGraph(ModelGraph graph, ModelGraph subGraph) {
    if (graph.equals(subGraph)) {
      return true;
    }
    for (ModelGraph child : graph.getChildren()) {
      if (isSubGraph(child, subGraph)) {
        return true;
      }
    }
    if (graph.getPreConditions() != null) {
      if (isSubGraph(graph.getPreConditions(), subGraph)) {
        return true;
      }
    }
    if (graph.getPostConditions() != null) {
      if (isSubGraph(graph.getPostConditions(), subGraph)) {
        return true;
      }
    }
    return false;
  }

  public static void updateGraphModelId(ViewState state, String id,
      String newModelId) {
    ModelGraph graph = find(state.getGraphs(), id);
    if (graph.getParent() != null
        && graph.getParent().getModel().getExcusedSubProcessorIds()
            .contains(graph.getModel().getModelId())) {
      graph.getParent().getModel().getExcusedSubProcessorIds()
          .remove(graph.getModel().getModelId());
      graph.getParent().getModel().getExcusedSubProcessorIds().add(newModelId);
    }
    graph.getModel().setModelId(newModelId);
  }

  public static void addChild(List<ModelGraph> graphs, String parentId,
      ModelGraph child) {
    ModelGraph parent = find(graphs, parentId);
    if (parent != null) {
      parent.addChild(child);
    }
  }

  public static List<ModelGraph> findRootGraphs(List<ModelGraph> graphs) {
    List<ModelGraph> rootGraphs = new Vector<ModelGraph>();
    for (ModelGraph graph : graphs) {
      if (rootGraphs.size() == 0) {
        rootGraphs.add(graph);
      } else {
        if (find(rootGraphs, graph.getModel().getModelId()) == null) {
          rootGraphs.add(graph);
        }
      }
    }
    for (int i = 0; i < rootGraphs.size(); i++) {
      ModelGraph rootGraph = rootGraphs.get(i);
      for (int j = 0; j < rootGraphs.size(); j++) {
        if (i != j
            && rootGraphs.get(j).recursiveFind(
                rootGraph.getModel().getModelId()) != null) {
          rootGraphs.remove(i--);
          break;
        }
      }

    }
    return rootGraphs;
  }

  public static ModelGraph findRoot(List<ModelGraph> rootGraphs,
      ModelGraph graph) {
    for (ModelGraph rootGraph : rootGraphs) {
      if (graph.equals(rootGraph)) {
        return rootGraph;
      } else if (graph.getParent() != null) {
        ModelGraph root = findRoot(rootGraphs, graph.getParent());
        if (root != null) {
          return root;
        }
      }
    }
    return null;
  }

  public static List<ModelGraph> find(List<ModelGraph> graphs, Set<String> ids) {
    Vector<ModelGraph> foundGraphs = new Vector<ModelGraph>();
    for (String id : ids) {
      ModelGraph graph = find(graphs, id);
      if (graph != null) {
        foundGraphs.add(graph);
      }
    }
    return foundGraphs;
  }

  public static ModelGraph find(List<ModelGraph> graphs, String id) {
    for (ModelGraph graph : graphs) {
      ModelGraph found = graph.recursiveFind(id);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  public static ModelGraph removeNode(List<ModelGraph> graphs, ModelNode node) {
    for (int i = 0; i < graphs.size(); i++) {
      if (graphs.get(i).getModel().equals(node)) {
        return graphs.remove(i);
      } else {
        ModelGraph graph = removeNode(graphs.get(i), node);
        if (graph != null) {
          return graph;
        }
      }
    }
    return null;
  }

  public static ModelGraph removeNode(ModelGraph graph, ModelNode node) {
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(graph);
    while (!stack.empty()) {
      ModelGraph curGraph = stack.pop();
      if (curGraph.getModel().equals(node)) {
        curGraph.setParent(null);
        return curGraph;
      } else {
        stack.addAll(curGraph.getChildren());
        if (curGraph.getPreConditions() != null) {
          stack.add(curGraph.getPreConditions());
        }
        if (curGraph.getPostConditions() != null) {
          stack.add(curGraph.getPostConditions());
        }
      }
    }
    return null;
  }

  public static List<Line> findSequentialLines(List<ModelGraph> graphs) {
    Vector<Line> lines = new Vector<Line>();
    for (ModelGraph graph : graphs) {
      lines.addAll(findSequentialLines(graph));
    }
    return lines;
  }

  public static List<Line> findSequentialLines(final ModelGraph graph) {
    Vector<Line> lines = new Vector<Line>();
    if (graph.getChildren().size() > 0) {
      Stack<ModelGraph> stack = new Stack<ModelGraph>();
      stack.add(graph);
      while (!stack.empty()) {
        ModelGraph curGraph = stack.pop();
        if (curGraph.getModel().getExecutionType().equals("sequential")) {
          for (int i = 0; i < curGraph.getChildren().size() - 1; i++) {
            lines.add(new Line(curGraph.getChildren().get(i).getModel(),
                curGraph.getChildren().get(i + 1).getModel()));
          }
        }
        stack.addAll(curGraph.getChildren());
      }
    }
    return lines;
  }

  public static List<Line> findLines(final List<ModelGraph> graphs) {
    Vector<Line> lines = new Vector<Line>();
    for (ModelGraph graph : graphs) {
      lines.addAll(findLines(graph));
    }
    return lines;
  }

  public static List<Line> findLines(final ModelGraph graph) {
    Vector<Line> lines = new Vector<Line>();
    if (graph.getChildren().size() > 0) {
      Stack<ModelGraph> graphs = new Stack<ModelGraph>();
      graphs.add(graph);
      while (!graphs.empty()) {
        ModelGraph curGraph = graphs.pop();
        if (curGraph.getModel().isParentType()) {

          if (curGraph.getChildren().size() == 0) {
            curGraph.addChild(new ModelGraph(createDummyNode()));
          }

          List<Line> relaventLines = getRelaventLines(lines, curGraph
              .getModel().getId());
          for (Line relaventLine : relaventLines) {
            int index = lines.indexOf(relaventLine);
            if (curGraph.getModel().getExecutionType().toLowerCase()
                .equals("sequential")) {
              lines.remove(index);
              if (curGraph.getChildren().size() > 0) {
                if (relaventLine.getFromModel().equals(curGraph.getModel())) {
                  lines.add(new Line(curGraph.getChildren()
                                             .get(curGraph.getChildren().size() - 1).getModel(),
                      relaventLine.getToModel()));
                } else {
                  lines.add(new Line(relaventLine.getFromModel(), curGraph
                      .getChildren().get(0).getModel()));
                }
              }
            } else if (curGraph.getModel().getExecutionType().toLowerCase()
                .equals("parallel")) {
              lines.remove(index);
              if (relaventLine.getFromModel().equals(curGraph.getModel())) {
                for (ModelGraph child : curGraph.getChildren()) {
                  lines.add(new Line(child.getModel(), relaventLine
                      .getToModel()));
                }
              } else {
                for (ModelGraph child : curGraph.getChildren()) {
                  lines.add(new Line(relaventLine.getFromModel(), child
                      .getModel()));
                }
              }
            }
          }

          if (curGraph.getModel().getExecutionType().toLowerCase()
              .equals("sequential")) {
            for (int i = 0; i < curGraph.getChildren().size(); i++) {
              if (i == curGraph.getChildren().size() - 1) {
                lines.add(new Line(curGraph.getChildren().get(i).getModel(),
                    null));
              } else {
                lines.add(new Line(curGraph.getChildren().get(i).getModel(),
                    curGraph.getChildren().get(i + 1).getModel()));
              }
            }
          } else if (curGraph.getModel().getExecutionType().toLowerCase()
              .equals("parallel")) {
            for (int i = 0; i < curGraph.getChildren().size(); i++) {
              lines
                  .add(new Line(curGraph.getChildren().get(i).getModel(), null));
            }
          }
          graphs.addAll(curGraph.getChildren());
        }
      }
    } else {
      lines.add(new Line(graph.getModel(), null));
    }
    return lines;
  }

  public static boolean isDummyNode(ModelNode node) {
    return node.getModelId().startsWith("DUMMY-");
  }

  public static ModelNode createDummyNode() {
    ModelNode dummy = new ModelNode(null, "DUMMY-"
        + dummyUntitledIter.getAndIncrement());
    dummy.setTextVisible(false);
    return dummy;
  }

  public static List<ModelGraph> getGraphsInFile(File file,
      List<ModelGraph> graphs) {
    List<ModelGraph> graphsInFile = new Vector<ModelGraph>();
    for (ModelGraph graph : graphs) {
      if (graph.getModel().getFile().equals(file)) {
        graphsInFile.add(graph);
      } else {
        graphsInFile.addAll(getGraphsInFile(file, graph.getChildren()));
      }
    }
    return graphsInFile;
  }

  public static String createUniqueName() {
    return "Untitled-" + untitledIter.getAndIncrement();
  }

  public static Line getLine(List<Line> lines, ModelNode fromModel,
      ModelNode toModel) {
    for (Line line : lines) {
      if (line.getFromModel().equals(fromModel)
          && line.getToModel().equals(toModel)) {
        return line;
      }
    }
    return null;
  }

  public static List<Line> getRelaventLines(List<Line> lines, String id) {
    List<Line> relaventLines = new Vector<Line>();
    for (Line line : lines) {
      if ((line.getFromModel() != null && line.getFromModel().getId()
                                              .equals(id))
          || (line.getToModel() != null && line.getToModel().getId().equals(id))) {
        relaventLines.add(line);
      }
    }
    return relaventLines;
  }

  public static List<Line> getChildrenLines(List<Line> lines, String id) {
    List<Line> relaventLines = new Vector<Line>();
    for (Line line : lines) {
      if (line.getFromModel().getId().equals(id)) {
        relaventLines.add(line);
      }
    }
    return relaventLines;
  }

  public static List<Line> getParentLines(List<Line> lines, String id) {
    List<Line> relaventLines = new Vector<Line>();
    for (Line line : lines) {
      if (line.getToModel().getId().equals(id)) {
        relaventLines.add(line);
      }
    }
    return relaventLines;
  }

  public static List<Line> getStartingLines(List<Line> lines) {
    Vector<Line> startingLines = new Vector<Line>();
    for (Line line : lines) {
      if (getParentLines(lines, line.getFromModel().getId()).size() == 0) {
        startingLines.add(line);
      }
    }
    return startingLines;
  }

}
