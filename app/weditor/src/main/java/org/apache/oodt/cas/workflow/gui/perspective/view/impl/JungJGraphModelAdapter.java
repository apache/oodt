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
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;

//OODT imports
import org.apache.oodt.cas.workflow.gui.model.ModelNode;

//JGraph imports
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

//Jung imports
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;

/**
 * 
 * This class integrates OODT, Jung and JGraph.
 * 
 * Not necessarily in that order. It acts as a facade, wrapping a Jung graph
 * (and its modifications) and then using that to update the current state of
 * the associated JGraph model.
 * 
 * @author mattmann
 * 
 */
public class JungJGraphModelAdapter extends DefaultGraphModel {

  private static final long serialVersionUID = 205647349848965651L;

  private ObservableGraph<ModelNode, IdentifiableEdge> jungGraph;

  private Map<String, DefaultGraphCell> cellMap;
  
  private static final Logger LOG = Logger.getLogger(JungJGraphModelAdapter.class.getName());

  public JungJGraphModelAdapter(
      final ObservableGraph<ModelNode, IdentifiableEdge> jungGraph) {
    this.jungGraph = jungGraph;
    this.jungGraph.addGraphEventListener(new WorkflowChangeListener(this));
    this.cellMap = new HashMap<String, DefaultGraphCell>();
    this.addGraphModelListener(new GraphModelListener() {

      @Override
      public void graphChanged(GraphModelEvent e) {
        Object[] added = e.getChange().getInserted();
        Object[] removed = e.getChange().getRemoved();

        if (added != null && added.length > 0) {
          for (Object a : added) {
            LOG.log(Level.FINE, "Jgraph notification of object added: ["
                + a.getClass().getName() + "]");

            if (a instanceof org.jgraph.graph.Edge) {
              LOG.log(Level.FINE, "Edge added to jgraph");
              org.jgraph.graph.DefaultEdge edge = (org.jgraph.graph.DefaultEdge) a;
              if (!jungGraph.getEdges().contains(edge.getUserObject())) {
                jungGraph.addEdge(
                    new IdentifiableEdge((ModelNode) edge.getSource(),
                        (ModelNode) edge.getTarget()), (ModelNode) edge
                        .getSource(), (ModelNode) edge.getTarget());
              }
            } else if (a instanceof org.jgraph.graph.DefaultGraphCell) {
              LOG.log(Level.FINE, "Vertex added to jgraph");
              org.jgraph.graph.DefaultGraphCell cell = (org.jgraph.graph.DefaultGraphCell) a;

              if (!jungGraph.getVertices().contains(cell.getUserObject())) {
                jungGraph.addVertex((ModelNode) cell.getUserObject());
              }
            }
          }
        }

        if (removed != null && removed.length > 0) {
          for (Object r : removed) {
            LOG.log(Level.FINE, "Jgraph notification of object removed: ["
                + r.getClass().getName() + "]");
          }
        }

      }
    });

  }

  public DefaultGraphCell getVertexCell(ModelNode node) {
    if (cellMap.get(node.getId()) != null) {
      return cellMap.get(node.getId());
    }

    DefaultGraphCell cell = new DefaultGraphCell(node);
    cell.add(new DefaultPort());
    return cell;
  }

  private AttributeMap getEdgeAttributes(DefaultEdge edge) {
    AttributeMap eMap = new AttributeMap();
    GraphConstants.setLineEnd(eMap, GraphConstants.ARROW_TECHNICAL);
    GraphConstants.setEndFill(eMap, true);
    GraphConstants.setEndSize(eMap, 10);
    GraphConstants.setForeground(eMap, Color.decode("#25507C"));
    GraphConstants.setFont(eMap,
        GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
    GraphConstants.setLineColor(eMap, Color.decode("#7AA1E6"));
    AttributeMap map = new AttributeMap();
    map.put(edge, eMap);
    return map;
  }

  private AttributeMap getVertexAttributes(DefaultGraphCell cell) {
    AttributeMap vMap = new AttributeMap();

    Color c = Color.decode("#FF9900");
    GraphConstants.setBounds(vMap, new Rectangle2D.Double(50, 50, 90, 30));
    GraphConstants.setBorder(vMap, BorderFactory.createRaisedBevelBorder());
    GraphConstants.setBackground(vMap, c);
    GraphConstants.setForeground(vMap, Color.white);
    GraphConstants.setFont(vMap,
        GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
    GraphConstants.setOpaque(vMap, true);

    AttributeMap map = new AttributeMap();
    map.put(cell, vMap);
    return map;
  }

  private class WorkflowChangeListener implements
      GraphEventListener<ModelNode, IdentifiableEdge> {

    private JungJGraphModelAdapter adapter;

    public WorkflowChangeListener(JungJGraphModelAdapter adapter) {
      this.adapter = adapter;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.uci.ics.jung.graph.event.GraphEventListener#handleGraphEvent(edu.
     * uci.ics.jung.graph.event.GraphEvent)
     */
    @Override
    public void handleGraphEvent(GraphEvent<ModelNode, IdentifiableEdge> e) {
      if (e.getType().equals(GraphEvent.Type.EDGE_ADDED)) {
        LOG.log(Level.FINE, "EDGE ADDED!");
        GraphEvent.Edge<ModelNode, IdentifiableEdge> event = (GraphEvent.Edge<ModelNode, IdentifiableEdge>) e;
        addJGraphEdge(event.getEdge());
      } else if (e.getType().equals(GraphEvent.Type.EDGE_REMOVED)) {
        LOG.log(Level.FINE, "EDGE REMOVED!");
      } else if (e.getType().equals(GraphEvent.Type.VERTEX_ADDED)) {
        LOG.log(Level.FINE, "VERTEX ADDED!");
        GraphEvent.Vertex<ModelNode, IdentifiableEdge> event = (GraphEvent.Vertex<ModelNode, IdentifiableEdge>) e;
        addJGraphVertex(event.getVertex());
      } else if (e.getType().equals(GraphEvent.Type.VERTEX_REMOVED)) {
        LOG.log(Level.FINE, "VERTEX REMOVED!");
      }

    }

  }

  private void addJGraphVertex(ModelNode node) {
    DefaultGraphCell cell = new DefaultGraphCell(node);
    cell.add(new DefaultPort());
    insert(new Object[] { cell }, getVertexAttributes(cell), null, null, null);
    cellMap.put(node.getId(), cell);
  }

  private void addJGraphEdge(IdentifiableEdge e) {
    ConnectionSet set = new ConnectionSet();
    DefaultEdge theEdge = new DefaultEdge(e);
    DefaultGraphCell from = null;
    DefaultGraphCell to = null;
    String fromVertexId = e.getFrom().getId();
    String toVertexId = e.getTo().getId();
    if (!cellMap.containsKey(fromVertexId)) {
      addJGraphVertex(e.getFrom());
    }
    from = cellMap.get(fromVertexId);

    if (!cellMap.containsKey(toVertexId)) {
      addJGraphVertex(e.getTo());
    }

    to = cellMap.get(toVertexId);

    set.connect(theEdge, (DefaultPort) from.getChildAt(0),
        (DefaultPort) to.getChildAt(0));
    insert(new Object[] { theEdge }, getEdgeAttributes(theEdge), set, null,
        null);
  }

}
