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

package org.apache.oodt.cas.workflow.gui.perspective.view.impl;

//JDK imports
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.model.ModelNode;
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewChange;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.util.GuiUtils;
import org.apache.oodt.cas.workflow.gui.util.IconLoader;
import org.apache.oodt.cas.workflow.gui.util.Line;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.ObservableGraph;

//JGraph imports
//Jung imports
//OODT imports

/**
 * 
 * This is where the money happens. The Graph visualization of OODT CAS
 * workflows is displayed via this view, which magically integrates JGraph,
 * Jung, and OODT.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class GraphView extends DefaultTreeView {

  private static final long serialVersionUID = 5935578385254884387L;

  private JungJGraphModelAdapter m_jgAdapter;
  private JGraph jgraph;
  private ObservableGraph<ModelNode, IdentifiableEdge> directedGraph;

  private MyGraphListener myGraphListener;

  private static final String VIEW_REF_WORKFLOW = "View Referrenced";
  private static final String ACTIONS_POP_MENU_NAME = "Actions";
  private static final String NEW_SUB_POP_MENU_NAME = "New";
  private static final String NEW_TASK_ITEM_NAME = "Task";
  private static final String NEW_CONDITION_ITEM_NAME = "Condition";
  private static final String NEW_PARALLEL_ITEM_NAME = "Parallel";
  private static final String NEW_SEQUENTIAL_ITEM_NAME = "Sequential";
  private static final String EDGES_SUB_POP_MENU_NAME = "Edges";
  private static final String TASK_LEVEL = "Task Level";
  private static final String WORKFLOW_LEVEL = "Workflow Level";
  private static final String DELETE_ITEM_NAME = "Delete";
  private static final String FORMAT_ITEM_NAME = "Format";
  private static final String ORDER_SUB_POP_MENU_NAME = "Order";
  private static final String TO_FRONT_ITEM_NAME = "Move To Front";
  private static final String TO_BACK_ITEM_NAME = "Move To Back";
  private static final String FORWARD_ITEM_NAME = "Move Forward";
  private static final String BACKWARDS_ITEM_NAME = "Move Backwards";

  private HashMap<String, Pair> edgeMap;

  private static final String SCALE = "GraphView/scale";
  private static final String EDGE_DISPLAY_MODE = "GraphView/EdgeDisplay/Mode";
  private static final String TASK_MODE = "Task";
  private static final String WORKFLOW_MODE = "Workflow";

  private static boolean scrollSelectedToVisible = false;

  public GraphView(String name) {
    super(name);
  }

  @Override
  public void refreshView(final ViewState state) {
    this.removeAll();
    this.myGraphListener = new MyGraphListener(state);

    Rectangle visible = null;
    if (jgraph != null)
      visible = jgraph.getVisibleRect();

    Cursor cursor = null;
    if (jgraph != null)
      cursor = jgraph.getCursor();

    this.edgeMap = new HashMap<String, Pair>();

    directedGraph = new ObservableGraph<ModelNode, IdentifiableEdge>(
        new DirectedSparseGraph<ModelNode, IdentifiableEdge>());
    m_jgAdapter = new JungJGraphModelAdapter(directedGraph);

    jgraph = new JGraph(m_jgAdapter);
    for (MouseListener ml : jgraph.getMouseListeners())
      jgraph.removeMouseListener(ml);
    for (MouseMotionListener ml : jgraph.getMouseMotionListeners())
      jgraph.removeMouseMotionListener(ml);
    for (MouseWheelListener ml : jgraph.getMouseWheelListeners())
      jgraph.removeMouseWheelListener(ml);
    jgraph.setBackground(Color.white);
    jgraph.setAntiAliased(true);
    jgraph.setMoveable(false);
    String scale = state.getFirstPropertyValue(SCALE);
    if (scale == null)
      state.setProperty(SCALE, scale = "1.0");
    jgraph.setScale(Double.parseDouble(scale));

    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this.jgraph,
        DnDConstants.ACTION_MOVE, new DragGestureListener() {

          DefaultGraphCell moveCell = null;
          ModelGraph moveGraph = null;

          public void dragGestureRecognized(final DragGestureEvent dge) {
            if (state.getMode() == View.Mode.MOVE
                || state.getMode() == View.Mode.EDIT) {
              Object moveOverCell = jgraph.getFirstCellForLocation(dge
                  .getDragOrigin().getX(), dge.getDragOrigin().getY());
              if (moveOverCell != null) {
                if (moveOverCell instanceof DefaultEdge) {
                  moveCell = null;
                } else if (moveOverCell instanceof DefaultGraphCell) {
                  moveCell = (DefaultGraphCell) moveOverCell;
                  moveGraph = GuiUtils.find(state.getGraphs(),
                      ((ModelNode) ((DefaultGraphCell) moveCell)
                          .getUserObject()).getId());

                  if (state.getMode() == View.Mode.MOVE)
                    moveCell = GraphView.this.m_jgAdapter
                        .getVertexCell((moveGraph = moveGraph.getRootParent())
                            .getModel());
                  else if (GuiUtils.isDummyNode(moveGraph.getModel()))
                    moveCell = GraphView.this.m_jgAdapter
                        .getVertexCell((moveGraph = moveGraph.getParent())
                            .getModel());
                  else if (moveGraph.getModel().isRef()) {
                    while (moveGraph.getParent() != null
                        && moveGraph.getParent().getModel().isRef())
                      moveGraph = moveGraph.getParent();
                    moveCell = GraphView.this.m_jgAdapter
                        .getVertexCell(moveGraph.getModel());
                  }
                  final double scale = Double.parseDouble(state
                      .getFirstPropertyValue(SCALE));
                  final Rectangle2D bounds = (Rectangle2D) GraphView.this.jgraph
                      .getAttributes(moveCell).get(GraphConstants.BOUNDS);
                  Point offset = new Point(
                      (int) (bounds.getX() * scale - dge.getDragOrigin().getX()),
                      (int) (bounds.getY() * scale - dge.getDragOrigin().getY()));
                  BufferedImage image = new BufferedImage((int) (bounds
                      .getWidth() * scale), (int) (bounds.getHeight() * scale),
                      BufferedImage.TYPE_INT_ARGB);
                  Graphics2D g2d = image.createGraphics();
                  g2d.setColor(Color.black);
                  g2d.drawRect((int) (2 * scale), (int) (2 * scale),
                      (int) ((bounds.getWidth() - 4) * scale),
                      (int) ((bounds.getHeight() - 4) * scale));
                  dge.startDrag(GraphView.this.getCursor(), image, offset,
                      new Transferable() {

                        public Object getTransferData(DataFlavor flavor)
                            throws UnsupportedFlavorException, IOException {
                          if (flavor.getHumanPresentableName().equals(
                              DefaultGraphCell.class.getSimpleName()))
                            return this;
                          else
                            throw new UnsupportedFlavorException(flavor);
                        }

                        public DataFlavor[] getTransferDataFlavors() {
                          return new DataFlavor[] { new DataFlavor(
                              DefaultGraphCell.class, DefaultGraphCell.class
                                  .getSimpleName()) };
                        }

                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                          return flavor.getHumanPresentableName().equals(
                              DefaultGraphCell.class.getSimpleName());
                        }

                      }, new DragSourceListener() {

                        private ModelGraph mouseOverGraph;
                        private DefaultGraphCell mouseOverCell;

                        public void dragDropEnd(DragSourceDropEvent dsde) {
                          System.out.println("DRAG END!!!!");
                          if (moveCell == null)
                            return;
                          Point dropPoint = new Point(dsde.getX()
                              - jgraph.getLocationOnScreen().x, dsde.getY()
                              - jgraph.getLocationOnScreen().y);
                          DefaultGraphCell endCell = (DefaultGraphCell) GraphView.this.jgraph
                              .getSelectionCell();
                          if (endCell != null) {
                            ModelGraph endGraph = GuiUtils.find(
                                state.getGraphs(),
                                ((ModelNode) endCell.getUserObject()).getId());
                            if (!endGraph.getModel().isParentType()
                                || GuiUtils.isSubGraph(moveGraph, endGraph))
                              return;
                            if (moveGraph.getParent() == null)
                              state.removeGraph(moveGraph);
                            else
                              GuiUtils.removeNode(state.getGraphs(),
                                  moveGraph.getModel());
                            GraphView.this.removeShift(state, moveGraph);
                            GuiUtils.addChild(state.getGraphs(), endGraph
                                .getModel().getId(), moveGraph);
                            GraphView.this.notifyListeners();
                          } else {
                            if (moveGraph.getParent() != null) {
                              GuiUtils.removeNode(state.getGraphs(),
                                  moveGraph.getModel());
                              state.addGraph(moveGraph);
                            }
                            Point shiftPoint = new Point(
                                (int) ((dropPoint.x - (dge.getDragOrigin().x - (bounds
                                    .getX() * scale))) / scale),
                                (int) ((dropPoint.y - (dge.getDragOrigin().y - (bounds
                                    .getY() * scale))) / scale));
                            GraphView.this.setShift(state, moveGraph,
                                shiftPoint);
                            GraphView.this.notifyListeners();
                          }
                        }

                        public void dragEnter(DragSourceDragEvent dsde) {
                          mouseOverCell = (DefaultGraphCell) GraphView.this.jgraph
                              .getFirstCellForLocation(
                                  dsde.getX() - jgraph.getLocationOnScreen().x,
                                  dsde.getY() - jgraph.getLocationOnScreen().y);
                          mouseOverGraph = GuiUtils.find(state.getGraphs(),
                              ((ModelNode) mouseOverCell.getUserObject())
                                  .getId());
                        }

                        public void dragExit(DragSourceEvent dse) {
                          System.out.println("DRAG EXIT!!!!");
                        }

                        public void dragOver(DragSourceDragEvent dsde) {
                          if (state.getMode().equals(Mode.EDIT)) {
                            if (mouseOverCell != null) {
                              Rectangle2D currentBounds = (Rectangle2D) mouseOverCell
                                  .getAttributes().get(GraphConstants.BOUNDS);
                              Point currentPoint = new Point(dsde.getX()
                                  - jgraph.getLocationOnScreen().x, dsde.getY()
                                  - jgraph.getLocationOnScreen().y);
                              if (currentBounds.contains(currentPoint)) {
                                for (ModelGraph child : mouseOverGraph
                                    .getChildren()) {
                                  DefaultGraphCell mouseOverCellLoc = GraphView.this.m_jgAdapter
                                      .getVertexCell(child.getModel());
                                  currentBounds = (Rectangle2D) mouseOverCellLoc
                                      .getAttributes().get(
                                          GraphConstants.BOUNDS);
                                  if (currentBounds.contains(currentPoint)) {
                                    mouseOverCell = mouseOverCellLoc;
                                    mouseOverGraph = child;
                                    break;
                                  }
                                }
                              } else {
                                if (mouseOverGraph.getParent() != null
                                    && (!mouseOverGraph.isCondition() || (mouseOverGraph
                                        .isCondition() && mouseOverGraph
                                        .getParent().isCondition()))) {
                                  mouseOverCell = GraphView.this.m_jgAdapter
                                      .getVertexCell((mouseOverGraph = mouseOverGraph
                                          .getParent()).getModel());
                                  currentBounds = (Rectangle2D) mouseOverCell
                                      .getAttributes().get(
                                          GraphConstants.BOUNDS);
                                } else {
                                  mouseOverCell = null;
                                  mouseOverGraph = null;
                                }
                              }
                            } else {
                              mouseOverCell = (DefaultGraphCell) GraphView.this.jgraph
                                  .getFirstCellForLocation(
                                      dsde.getX()
                                          - jgraph.getLocationOnScreen().x,
                                      dsde.getY()
                                          - jgraph.getLocationOnScreen().y);
                              if (mouseOverCell != null)
                                mouseOverGraph = GuiUtils.find(state
                                    .getGraphs(), ((ModelNode) mouseOverCell
                                    .getUserObject()).getId());
                              else
                                mouseOverGraph = null;
                            }
                            if (mouseOverGraph != null) {
                              if (GuiUtils.isDummyNode(mouseOverGraph
                                  .getModel())) {
                                mouseOverGraph = mouseOverGraph.getParent();
                              } else {
                                while (mouseOverGraph != null
                                    && mouseOverGraph.getModel().isRef())
                                  mouseOverGraph = mouseOverGraph.getParent();
                              }
                              if (mouseOverGraph != null)
                                mouseOverCell = GraphView.this.m_jgAdapter
                                    .getVertexCell(mouseOverGraph.getModel());
                              else
                                mouseOverCell = null;
                            }
                            GraphView.this.jgraph
                                .setSelectionCells(new Object[] { mouseOverCell });
                          }
                        }

                        public void dropActionChanged(DragSourceDragEvent dsde) {
                          System.out.println("DRAG CHANGE!!!!");
                        }

                      });
                }
              }
            }
          }

        });

    List<Line> lines = GuiUtils.findLines(state.getGraphs());
    for (Line line : lines) {
      if (!this.directedGraph.containsVertex(line.getFromModel()))
        this.directedGraph.addVertex(line.getFromModel());

      if (line.getToModel() != null) {
        if (!this.directedGraph.containsVertex(line.getToModel()))
          this.directedGraph.addVertex(line.getToModel());
        IdentifiableEdge edge = new IdentifiableEdge(line.getFromModel(), line.getToModel());
        directedGraph.addEdge(edge, line.getFromModel(), line.getToModel());
        this.edgeMap.put(edge.id, new Pair(line.getFromModel() != null ? line
            .getFromModel().getId() : null, line.getToModel().getId()));
      }
    }

    JGraphFacade facade = new JGraphFacade(jgraph);
    facade.setIgnoresUnconnectedCells(false);
    JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
    layout.setOrientation(SwingConstants.WEST);
    layout.setIntraCellSpacing(70.0);
    layout.setLayoutFromSinks(false);
    layout.run(facade);
    Map nested = facade.createNestedMap(true, true);
    if (nested != null) {
      this.hideDummyNodes(nested);
      this.addGroups(state.getGraphs(), nested, state);
      this.ensureNoOverlap(nested, state);
      nested = this.shiftMap(nested, state);
      jgraph.getGraphLayoutCache().edit(nested);
    }

    String edgeDisplayMode = state.getFirstPropertyValue(EDGE_DISPLAY_MODE);
    if (edgeDisplayMode == null)
      state.setProperty(EDGE_DISPLAY_MODE, edgeDisplayMode = WORKFLOW_MODE);
    if (edgeDisplayMode.equals(WORKFLOW_MODE)) {
      this.edgeMap = new HashMap<String, Pair>();
      removeAllEdges(this.directedGraph);
      lines = GuiUtils.findSequentialLines(state.getGraphs());
      for (Line line : lines) {
        IdentifiableEdge edge = new IdentifiableEdge(line.getFromModel(), line.getToModel());
        directedGraph.addEdge(edge, line.getFromModel(), line.getToModel());
        this.edgeMap.put(edge.id, new Pair(line.getFromModel() != null ? line
            .getFromModel().getId() : null, line.getToModel().getId()));
      }
    }

    if (state.getSelected() != null) {
      ModelGraph graph = GuiUtils.find(state.getGraphs(), state.getSelected()
          .getModel().getId());
      if (graph != null) {
        DefaultGraphCell cell = this.m_jgAdapter
            .getVertexCell(graph.getModel());
        if (cell != null)
          this.jgraph.setSelectionCells(new Object[] { cell });
        else
          this.jgraph.setSelectionCells(new Object[] {});
      } else
        this.jgraph.setSelectionCells(new Object[] {});
    } else {
      this.jgraph.setSelectionCells(new Object[] {});
    }

    jgraph.addMouseListener(this.myGraphListener);

    this.setLayout(new BorderLayout());
    this.add(new JScrollPane(jgraph, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);

    if (scrollSelectedToVisible && state.getSelected() != null) {
      this.jgraph.scrollCellToVisible(GraphView.this.m_jgAdapter
          .getVertexCell(state.getSelected().getModel()));
      scrollSelectedToVisible = false;
    } else if (visible != null) {
      this.jgraph.scrollRectToVisible(visible);
    }

    if (cursor != null)
      this.jgraph.setCursor(cursor);

    this.revalidate();
  }

  private void hideDummyNodes(Map nested) {
    for (Object cell : nested.keySet()) {
      if (cell instanceof DefaultEdge) {
        // do nothing
      } else if (cell instanceof DefaultGraphCell) {
        if (GuiUtils.isDummyNode((ModelNode) ((DefaultGraphCell) cell)
            .getUserObject())) {
          ((Map<Object, Object>) nested.get(cell)).put("opaque", false);
          ((Map<Object, Object>) nested.get(cell)).put("backgroundColor",
              Color.white);
        }
      }
    }
  }

  private Map shiftMap(Map nested, ViewState state) {
    Map shiftedNested = new Hashtable(nested);
    for (Object obj : shiftedNested.entrySet()) {
      Entry entry = (Entry) obj;
      if (entry.getKey() instanceof DefaultEdge) {
        ArrayList<Point2D.Double> points = (ArrayList<Point2D.Double>) ((Map<Object, Object>) entry
            .getValue()).get("points");
        ArrayList<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
        Point shift = this.getShift(state, (DefaultGraphCell) entry.getKey(),
            nested);
        for (Point2D.Double point : points)
          newPoints
              .add(new Point2D.Double(point.x + shift.x, point.y + shift.y));
        ((Map<Object, Object>) entry.getValue()).put("points", newPoints);
      } else if (entry.getKey() instanceof DefaultGraphCell) {
        DefaultGraphCell cell = (DefaultGraphCell) entry.getKey();
        Rectangle2D bounds = (Rectangle2D) ((Map<Object, Object>) entry
            .getValue()).get("bounds");
        Point shift = this.getShift(state, cell, nested);
        AttributeMap.SerializableRectangle2D newBounds = new AttributeMap.SerializableRectangle2D(
            bounds.getX() + shift.x, bounds.getY() + shift.y,
            bounds.getWidth(), bounds.getHeight());
        Map<Object, Object> newMap = new Hashtable<Object, Object>(
            (Map<Object, Object>) entry.getValue());
        newMap.put("bounds", newBounds);
        shiftedNested.put(cell, newMap);
      }
    }
    return shiftedNested;
  }

  private void ensureNoOverlap(Map nested, ViewState state) {
    boolean changed;
    do {
      changed = false;
      for (int i = 0; i < state.getGraphs().size(); i++) {
        ModelGraph currentGraph = state.getGraphs().get(i);
        if (this.ensureNoInternalOverlap(currentGraph, nested))
          changed = true;
        DefaultGraphCell currentCell = this.m_jgAdapter
            .getVertexCell(currentGraph.getModel());
        Rectangle2D currentBounds = (Rectangle2D) ((Map) nested
            .get(currentCell)).get(GraphConstants.BOUNDS);
        Point currentShift = this.getShift(state, currentCell, nested);
        Rectangle currentShiftBounds = new Rectangle(
            (int) (currentBounds.getX() + currentShift.getX()),
            (int) (currentBounds.getY() + currentShift.getY()),
            (int) currentBounds.getWidth(), (int) currentBounds.getHeight());
        for (int j = 0; j < state.getGraphs().size(); j++) {
          if (i == j)
            continue;
          ModelGraph graph = state.getGraphs().get(j);
          DefaultGraphCell cell = this.m_jgAdapter.getVertexCell(graph
              .getModel());
          Rectangle2D bounds = (Rectangle2D) ((Map) nested.get(cell))
              .get(GraphConstants.BOUNDS);
          Point shift = this.getShift(state, cell, nested);
          Rectangle shiftBounds = new Rectangle(
              (int) (bounds.getX() + shift.getX()),
              (int) (bounds.getY() + shift.getY()), (int) bounds.getWidth(),
              (int) bounds.getHeight());
          if (currentShiftBounds.intersects(shiftBounds)) {
            changed = true;
            if (currentShiftBounds.getY() < shiftBounds.getY()) {
              Rectangle intersection = currentShiftBounds
                  .intersection(shiftBounds);
              if (currentShiftBounds.getY() + currentShiftBounds.getHeight() > shiftBounds
                  .getY() + shiftBounds.getHeight()) {
                this.setShift(state, graph,
                    new Point((int) (currentShiftBounds.getX()
                        + currentShiftBounds.getWidth() + 20.0),
                        (int) shiftBounds.getY()));
              } else {
                this.setShift(
                    state,
                    graph,
                    new Point((int) shiftBounds.getX(), (int) (shiftBounds
                        .getY() + intersection.getHeight() + 20.0)));
              }
            } else {
              Rectangle intersection = shiftBounds
                  .intersection(currentShiftBounds);
              if (shiftBounds.getY() + shiftBounds.getHeight() > currentShiftBounds
                  .getY() + currentShiftBounds.getHeight()) {
                this.setShift(
                    state,
                    currentGraph,
                    new Point((int) (shiftBounds.getX()
                        + shiftBounds.getWidth() + 20.0),
                        (int) currentShiftBounds.getY()));
              } else {
                this.setShift(
                    state,
                    currentGraph,
                    new Point((int) currentShiftBounds.getX(),
                        (int) (currentShiftBounds.getY()
                            + intersection.getHeight() + 20.0)));
              }

              currentShift = this.getShift(state, currentCell, nested);
              currentShiftBounds = new Rectangle(
                  (int) (currentBounds.getX() + currentShift.getX()),
                  (int) (currentBounds.getY() + currentShift.getY()),
                  (int) currentBounds.getWidth(),
                  (int) currentBounds.getHeight());
            }
          }
        }
      }
    } while (changed);
  }

  private boolean ensureNoInternalOverlap(final ModelGraph graph,
      final Map nested) {
    boolean changed = false;
    if (graph.getChildren().size() > 1) {
      List<ModelGraph> sortedChildren = new Vector<ModelGraph>(
          graph.getChildren());
      Collections.sort(sortedChildren, new Comparator<ModelGraph>() {

        public int compare(ModelGraph o1, ModelGraph o2) {
          DefaultGraphCell child1Cell = GraphView.this.m_jgAdapter
              .getVertexCell(o1.getModel());
          DefaultGraphCell child2Cell = GraphView.this.m_jgAdapter
              .getVertexCell(o2.getModel());
          Rectangle2D child1Bounds = (Rectangle2D) ((Map) nested
              .get(child1Cell)).get(GraphConstants.BOUNDS);
          Rectangle2D child2Bounds = (Rectangle2D) ((Map) nested
              .get(child2Cell)).get(GraphConstants.BOUNDS);
          if (graph.getModel().getExecutionType().equals("parallel"))
            return Double.compare(child1Bounds.getMaxY(),
                child2Bounds.getMaxY());
          else
            return Double.compare(child1Bounds.getX(), child2Bounds.getX());
        }

      });
      changed = ensureNoInternalOverlap(sortedChildren.get(0), nested);
      Rectangle2D graphRectangle = (Rectangle2D) ((Map) nested
          .get(this.m_jgAdapter.getVertexCell(sortedChildren.get(0).getModel())))
          .get(GraphConstants.BOUNDS);
      for (int i = 1; i < sortedChildren.size(); i++) {
        ModelGraph child2 = sortedChildren.get(i);
        if (ensureNoInternalOverlap(child2, nested))
          changed = true;
        DefaultGraphCell child2Cell = this.m_jgAdapter.getVertexCell(child2
            .getModel());
        for (int j = i - 1; j >= 0; j--) {
          ModelGraph child1 = sortedChildren.get(j);
          DefaultGraphCell child1Cell = this.m_jgAdapter.getVertexCell(child1
              .getModel());
          Rectangle2D child1Bounds = (Rectangle2D) ((Map) nested
              .get(child1Cell)).get(GraphConstants.BOUNDS);
          Rectangle2D child2Bounds = (Rectangle2D) ((Map) nested
              .get(child2Cell)).get(GraphConstants.BOUNDS);
          if (child1Bounds.intersects(child2Bounds)) {
            changed = true;
            if (graph.getModel().getExecutionType().equals("parallel")) {
              ((Map) nested.get(child2Cell)).put(GraphConstants.BOUNDS,
                  new AttributeMap.SerializableRectangle2D(child2Bounds.getX(),
                      child1Bounds.getMaxY() + 20.0, child2Bounds.getWidth(),
                      child2Bounds.getHeight()));
              this.shift(child2.getChildren(), nested, 0,
                  child1Bounds.getMaxY() + 20.0 - child2Bounds.getY());
            } else {
              ((Map) nested.get(child2Cell)).put(
                  GraphConstants.BOUNDS,
                  new AttributeMap.SerializableRectangle2D(child1Bounds
                      .getMaxX() + 20.0, child2Bounds.getY(), child2Bounds
                      .getWidth(), child2Bounds.getHeight()));
              this.shift(child2.getChildren(), nested, child1Bounds.getMaxX()
                  + 20.0 - child2Bounds.getX(), 0);
            }
          }
        }
        graphRectangle = graphRectangle.createUnion((Rectangle2D) ((Map) nested
            .get(child2Cell)).get(GraphConstants.BOUNDS));
      }
      ((Map) nested.get(this.m_jgAdapter.getVertexCell(graph.getModel()))).put(
          GraphConstants.BOUNDS, new AttributeMap.SerializableRectangle2D(
              graphRectangle.getX() - 5, graphRectangle.getY() - 20,
              graphRectangle.getWidth() + 10, graphRectangle.getHeight() + 25));
    }
    return changed;
  }

  private void shift(List<ModelGraph> graphs, Map nested, double x, double y) {
    for (ModelGraph graph : graphs) {
      DefaultGraphCell cell = this.m_jgAdapter.getVertexCell(graph.getModel());
      Rectangle2D bounds = (Rectangle2D) ((Map) nested.get(cell))
          .get(GraphConstants.BOUNDS);
      ((Map) nested.get(cell)).put(
          GraphConstants.BOUNDS,
          new AttributeMap.SerializableRectangle2D(bounds.getX() + x, bounds
                                                                          .getY() + y, bounds.getWidth(),
              bounds.getHeight()));
      this.shift(graph.getChildren(), nested, x, y);
    }
  }

  private void addGroups(List<ModelGraph> modelGraphs, Map nested,
      ViewState state) {
    for (ModelGraph modelGraph : modelGraphs)
      this.addGroups(modelGraph, nested, state);
  }

  private DefaultGraphCell addGroups(ModelGraph modelGraph, Map nested,
      ViewState state) {
    if (modelGraph.getModel().isParentType()) {
      double top_x = Double.MAX_VALUE, top_y = Double.MAX_VALUE, bottom_x = 0.0, bottom_y = 0.0;
      this.directedGraph.addVertex(modelGraph.getModel());
      DefaultGraphCell modelCell = this.m_jgAdapter.getVertexCell(modelGraph
          .getModel());
      Vector<DefaultGraphCell> group = new Vector<DefaultGraphCell>();
      group.add(modelCell);

      HashMap<Object, Object> map = new HashMap<Object, Object>();
      for (int i = 0; i < modelGraph.getChildren().size(); i++) {
        ModelGraph child = modelGraph.getChildren().get(i);
        DefaultGraphCell curCell = addGroups(child, nested, state);
        group.add(curCell);
        Rectangle2D bounds = (Rectangle2D) ((Map<Object, Object>) nested
            .get(curCell)).get("bounds");
        if (bounds.getX() < top_x)
          top_x = bounds.getX();
        if (bounds.getY() < top_y)
          top_y = bounds.getY();
        if (bounds.getMaxX() > bottom_x)
          bottom_x = bounds.getMaxX();
        if (bounds.getMaxY() > bottom_y)
          bottom_y = bounds.getMaxY();
      }

      map.put(GraphConstants.BOUNDS, new AttributeMap.SerializableRectangle2D(
          top_x - 5, top_y - 20, bottom_x - top_x + 10, bottom_y - top_y + 25));
      map.put(GraphConstants.FOREGROUND, Color.black);
      if (modelGraph.getModel().isRef())
        map.put(GraphConstants.BACKGROUND, Color.lightGray);
      else
        map.put(GraphConstants.BACKGROUND, Color.white);
      if (modelGraph.isExcused())
        map.put(GraphConstants.GRADIENTCOLOR, Color.gray);
      map.put(GraphConstants.HORIZONTAL_ALIGNMENT, SwingConstants.LEFT);
      map.put(GraphConstants.VERTICAL_ALIGNMENT, SwingConstants.TOP);
      map.put(GraphConstants.BORDER, new LineBorder(modelGraph.getModel()
          .getColor(), 1));
      jgraph.getGraphLayoutCache().toBack(new Object[] { modelCell });
      nested.put(modelCell, map);
      return modelCell;
    }
    DefaultGraphCell cell = this.m_jgAdapter.getVertexCell(modelGraph
        .getModel());
    ((Map<Object, Object>) nested.get(cell)).put(GraphConstants.FOREGROUND,
        Color.black);
    if (modelGraph.isExcused())
      ((Map<Object, Object>) nested.get(cell)).put(
          GraphConstants.GRADIENTCOLOR, Color.gray);
    else
      ((Map<Object, Object>) nested.get(cell)).put(
          GraphConstants.GRADIENTCOLOR, Color.white);
    if (!((ModelNode) ((DefaultGraphCell) cell).getUserObject()).isRef())
      ((Map<Object, Object>) nested.get(cell)).put(GraphConstants.BACKGROUND,
          modelGraph.getModel().getColor());
    else
      ((Map<Object, Object>) nested.get(cell)).put(GraphConstants.BACKGROUND,
          Color.lightGray);
    return cell;
  }
  
  private void removeAllEdges(ObservableGraph<ModelNode, IdentifiableEdge> graph){
    List<IdentifiableEdge> edges = new Vector<IdentifiableEdge>();
    
    for(IdentifiableEdge edge: graph.getEdges()){
       edges.add(edge);
     }
    
    for(IdentifiableEdge edge: edges){
       graph.removeEdge(edge);
    }
  }
  

  private class Pair {
    String first, second;

    public Pair(String first, String second) {
      this.first = first;
      this.second = second;
    }

    public String getFirst() {
      return this.first;
    }

    public String getSecond() {
      return this.second;
    }
  }

  public class MyGraphListener implements MouseListener, ActionListener {

    private Point curPoint;
    private ViewState state;

    public MyGraphListener(ViewState state) {
      this.state = state;
    }

    public void mouseClicked(MouseEvent e) {
      curPoint = e.getPoint();
      if (e.getButton() == MouseEvent.BUTTON3) {
        Object mouseOverCell = GraphView.this.jgraph.getFirstCellForLocation(
            e.getX(), e.getY());
        ModelGraph mouseOverGraph = null;
        if (mouseOverCell != null) {
          mouseOverGraph = (GuiUtils.find(state.getGraphs(),
              ((ModelNode) ((DefaultMutableTreeNode) mouseOverCell)
                  .getUserObject()).getId()));
          if (mouseOverGraph != null) {
            if (GuiUtils.isDummyNode(mouseOverGraph.getModel())) {
              mouseOverGraph = mouseOverGraph.getParent();
            } else {
              while (mouseOverGraph != null
                  && mouseOverGraph.getParent() != null
                  && mouseOverGraph.getParent().getModel().isRef())
                mouseOverGraph = mouseOverGraph.getParent();
            }
            if (mouseOverGraph != null)
              mouseOverCell = GraphView.this.m_jgAdapter
                  .getVertexCell(mouseOverGraph.getModel());
            else
              mouseOverCell = null;
          }
          state.setSelected(mouseOverGraph);
        } else {
          state.setSelected(null);
        }
        PopupMenu actionsMenu = createActionMenu(state);
        GraphView.this.notifyListeners();
        GraphView.this.jgraph.add(actionsMenu);
        actionsMenu.show(GraphView.this.jgraph, e.getPoint().x, e.getPoint().y);
      } else if (e.getButton() == MouseEvent.BUTTON1) {
        if (state.getMode() == View.Mode.ZOOM_IN) {
          state.setProperty(SCALE, Double.toString(Double.parseDouble(state
              .getFirstPropertyValue(SCALE)) + 0.1));
          state.setSelected(null);
          GraphView.this.notifyListeners();
        } else if (state.getMode() == View.Mode.ZOOM_OUT) {
          state.setProperty(SCALE, Double.toString(Double.parseDouble(state
              .getFirstPropertyValue(SCALE)) - 0.1));
          state.setSelected(null);
          GraphView.this.notifyListeners();
        } else if (state.getMode() == View.Mode.EDIT) {
          Object cell = GraphView.this.jgraph.getFirstCellForLocation(e.getX(),
              e.getY());
          if (cell != null) {
            if (cell instanceof DefaultEdge) {
            } else if (cell instanceof DefaultGraphCell) {
              ModelGraph graph = GuiUtils.find(state.getGraphs(),
                  ((ModelNode) ((DefaultGraphCell) cell).getUserObject())
                      .getId());
              if (graph.getModel().isRef())
                while (graph.getParent() != null
                    && graph.getParent().getModel().isRef())
                  graph = graph.getParent();
              if (GuiUtils.isDummyNode(graph.getModel()))
                graph = graph.getParent();
              state.setSelected(graph);
              GraphView.this.notifyListeners();
            }
          } else if (cell == null && state.getSelected() != null) {
            state.setSelected(null);
            GraphView.this.notifyListeners();
          }
        } else if (state.getMode() == View.Mode.DELETE
            && e.getClickCount() == 2) {
          Object cell = GraphView.this.jgraph.getFirstCellForLocation(e.getX(),
              e.getY());
          if (cell != null) {
            if (cell instanceof DefaultEdge) {
              // do nothing
            } else if (cell instanceof DefaultGraphCell) {
              ModelGraph graph = GuiUtils.removeNode(state.getGraphs(),
                  (ModelNode) ((DefaultGraphCell) cell).getUserObject());
              GraphView.this.notifyListeners();
            }
          }
        }
      }
    }

    public void mouseEntered(MouseEvent e) {
      if (state.getMode() == View.Mode.ZOOM_IN
          || state.getMode() == View.Mode.ZOOM_OUT) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
          GraphView.this.jgraph.setCursor(toolkit.createCustomCursor(
              IconLoader.getIcon(IconLoader.ZOOM_CURSOR), new Point(0, 0),
              "img"));
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      } else if (state.getMode() == Mode.MOVE) {
        GraphView.this.jgraph.setCursor(new Cursor(Cursor.HAND_CURSOR));
      } else {
        GraphView.this.jgraph.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
      this.createNewGraph(e.getActionCommand());
    }

    private void createNewGraph(String actionCommand) {
      ModelGraph newGraph = null;
      if (actionCommand.equals(NEW_TASK_ITEM_NAME)) {
        newGraph = new ModelGraph(new ModelNode(state.getFile(),
            GuiUtils.createUniqueName()));
      } else if (actionCommand.equals(NEW_PARALLEL_ITEM_NAME)) {
        ModelNode node = new ModelNode(state.getFile(),
            GuiUtils.createUniqueName());
        node.setExecutionType("parallel");
        newGraph = new ModelGraph(node);
      } else if (actionCommand.equals(NEW_SEQUENTIAL_ITEM_NAME)) {
        ModelNode node = new ModelNode(state.getFile(),
            GuiUtils.createUniqueName());
        node.setExecutionType("sequential");
        newGraph = new ModelGraph(node);
      } else {
        return;
      }
      Object cell = GraphView.this.jgraph.getSelectionCell();
      if (cell != null) {
        if (cell instanceof DefaultGraphCell) {
          ModelGraph graph = GuiUtils.find(state.getGraphs(),
              ((ModelNode) ((DefaultGraphCell) cell).getUserObject()).getId());
          if (graph != null)
            graph.addChild(newGraph);
        }
      } else {
        state.addGraph(newGraph);
        GraphView.this.setShift(state, newGraph, curPoint);
      }
      GraphView.this.notifyListeners();
    }

  }

  public void setShift(ViewState state, ModelGraph modelGraph, Point point) {
    state.setProperty(modelGraph.getModel().getId() + "/Shift/x",
        Integer.toString(point.x));
    state.setProperty(modelGraph.getModel().getId() + "/Shift/y",
        Integer.toString(point.y));
  }

  public void removeShift(ViewState state, ModelGraph modelGraph) {
    state.removeProperty(modelGraph.getModel().getId() + "/Shift");
  }

  public Point getShift(ViewState state, DefaultGraphCell cell, Map nested) {
    ModelGraph graph = null;
    if (cell instanceof DefaultEdge) {
      IdentifiableEdge edge = (IdentifiableEdge) cell.getUserObject();
      Pair pair = GraphView.this.edgeMap.get(edge.id);
      graph = GuiUtils.find(state.getGraphs(), pair.getFirst());
    } else {
      graph = GuiUtils.find(state.getGraphs(),
          ((ModelNode) cell.getUserObject()).getId());
    }
    ModelGraph parent = GuiUtils.findRoot(state.getGraphs(), graph);
    Point shiftPoint = null;
    if (state.containsProperty(parent.getModel().getId() + "/Shift"))
      shiftPoint = new Point(Integer.parseInt(state
          .getFirstPropertyValue(parent.getModel().getId() + "/Shift/x")),
          Integer.parseInt(state.getFirstPropertyValue(parent.getModel()
              .getId() + "/Shift/y")));
    if (shiftPoint == null) {
      shiftPoint = new Point(100, 100);
      this.setShift(state, parent, shiftPoint);
      return shiftPoint;
    } else {
      Rectangle2D bounds = (Rectangle2D) ((Map<Object, Object>) nested
          .get(GraphView.this.m_jgAdapter.getVertexCell(parent.getModel())))
          .get(GraphConstants.BOUNDS);
      return new Point(shiftPoint.x - (int) bounds.getX(), shiftPoint.y
          - (int) bounds.getY());
    }
  }


  private PopupMenu createActionMenu(final ViewState state) {
    PopupMenu actionsMenu = new PopupMenu(ACTIONS_POP_MENU_NAME);
    PopupMenu newSubMenu = new PopupMenu(NEW_SUB_POP_MENU_NAME);
    MenuItem taskItem = new MenuItem(NEW_TASK_ITEM_NAME);
    MenuItem condItem = new MenuItem(NEW_CONDITION_ITEM_NAME);
    newSubMenu.add(taskItem);
    newSubMenu.add(condItem);
    newSubMenu.add(new MenuItem(NEW_PARALLEL_ITEM_NAME));
    newSubMenu.add(new MenuItem(NEW_SEQUENTIAL_ITEM_NAME));
    newSubMenu.addActionListener(this.myGraphListener);
    actionsMenu.add(newSubMenu);
    MenuItem viewReferrencedWorkflow = new MenuItem(VIEW_REF_WORKFLOW);
    actionsMenu.add(viewReferrencedWorkflow);
    MenuItem deleteItem = new MenuItem(DELETE_ITEM_NAME);
    actionsMenu.add(deleteItem);
    MenuItem formatItem = new MenuItem(FORMAT_ITEM_NAME);
    actionsMenu.add(formatItem);

    PopupMenu orderSubMenu = new PopupMenu(ORDER_SUB_POP_MENU_NAME);

    ModelGraph modelGraph = state.getSelected();
    newSubMenu.setEnabled(modelGraph == null
        || modelGraph.getModel().isParentType());
    deleteItem.setEnabled(modelGraph != null);
    formatItem.setEnabled(true);
    if (modelGraph != null) {
      viewReferrencedWorkflow.setEnabled(modelGraph.getModel().isRef());
      taskItem.setEnabled(!modelGraph.isCondition());
      condItem.setEnabled(modelGraph.isCondition());
      orderSubMenu.setEnabled(modelGraph.getParent() != null
          && !(modelGraph.isCondition() && !modelGraph.getParent()
              .isCondition()));
    } else {
      boolean isCondition = false;
      if (state.getGraphs().size() > 0)
        isCondition = state.getGraphs().get(0).isCondition();
      viewReferrencedWorkflow.setEnabled(false);
      taskItem.setEnabled(!isCondition);
      condItem.setEnabled(isCondition);
    }

    actionsMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(DELETE_ITEM_NAME)) {
          GuiUtils.removeNode(state.getGraphs(), state.getSelected().getModel());
          state.setSelected(null);
          GraphView.this.notifyListeners();
        } else if (e.getActionCommand().equals(FORMAT_ITEM_NAME)) {
          GraphView.this.refreshView(state);
        } else if (e.getActionCommand().equals(VIEW_REF_WORKFLOW)) {
          scrollSelectedToVisible = true;
          GraphView.this.notifyListeners(new ViewChange.VIEW_MODEL(state
              .getSelected().getModel().getModelId(), GraphView.this));
        }
      }
    });
    PopupMenu edgesSubMenu = new PopupMenu(EDGES_SUB_POP_MENU_NAME);
    edgesSubMenu.add(new MenuItem(TASK_LEVEL));
    edgesSubMenu.add(new MenuItem(WORKFLOW_LEVEL));
    actionsMenu.add(edgesSubMenu);
    edgesSubMenu.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(TASK_LEVEL)) {
          state.setProperty(EDGE_DISPLAY_MODE, TASK_MODE);
        } else if (e.getActionCommand().equals(WORKFLOW_LEVEL)) {
          state.setProperty(EDGE_DISPLAY_MODE, WORKFLOW_MODE);
        }
        GraphView.this.refreshView(state);
      }

    });
    actionsMenu.add(orderSubMenu);
    orderSubMenu.add(new MenuItem(TO_FRONT_ITEM_NAME));
    orderSubMenu.add(new MenuItem(TO_BACK_ITEM_NAME));
    orderSubMenu.add(new MenuItem(FORWARD_ITEM_NAME));
    orderSubMenu.add(new MenuItem(BACKWARDS_ITEM_NAME));
    orderSubMenu.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        ModelGraph graph = state.getSelected();
        ModelGraph parent = graph.getParent();
        if (e.getActionCommand().equals(TO_FRONT_ITEM_NAME)) {
          if (parent.getChildren().remove(graph))
            parent.getChildren().add(0, graph);
        } else if (e.getActionCommand().equals(TO_BACK_ITEM_NAME)) {
          if (parent.getChildren().remove(graph))
            parent.getChildren().add(graph);
        } else if (e.getActionCommand().equals(FORWARD_ITEM_NAME)) {
          int index = parent.getChildren().indexOf(graph);
          if (index != -1) {
            parent.getChildren().remove(index);
            parent.getChildren().add(
                Math.min(parent.getChildren().size(), index + 1), graph);
          }
        } else if (e.getActionCommand().equals(BACKWARDS_ITEM_NAME)) {
          int index = parent.getChildren().indexOf(graph);
          if (index != -1) {
            parent.getChildren().remove(index);
            parent.getChildren().add(Math.max(0, index - 1), graph);
          }
        }
        GraphView.this.notifyListeners();
      }

    });
    return actionsMenu;
  }

}