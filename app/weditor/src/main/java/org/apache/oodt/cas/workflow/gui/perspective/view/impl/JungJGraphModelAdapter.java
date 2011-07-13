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
import javax.swing.BorderFactory;

//OODT imports
import org.apache.oodt.cas.workflow.gui.model.ModelNode;

//JGraph imports
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;

//Jung imports
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEvent.Edge;
import edu.uci.ics.jung.graph.event.GraphEvent.Vertex;
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

  private Map<GraphCell, Vertex<ModelNode, IdentifiableEdge>> jgraphVertexToJungVertexMap;

  private Map<Vertex<ModelNode, IdentifiableEdge>, GraphCell> jungVertexToJgraphVertexMap;
  
  private Map<GraphCell, Edge<ModelNode, IdentifiableEdge>> jgraphEdgeToJungEdgeMap;
  
  private Map<Edge<ModelNode, IdentifiableEdge>, GraphCell> jungEdgeToJGraphEdgeMap;
  
  public JungJGraphModelAdapter(
      ObservableGraph<ModelNode, IdentifiableEdge> jungGraph) {
    this.jungGraph = jungGraph;
    this.jgraphVertexToJungVertexMap = new HashMap<GraphCell, GraphEvent.Vertex<ModelNode,IdentifiableEdge>>();
    this.jungVertexToJgraphVertexMap = new HashMap<GraphEvent.Vertex<ModelNode,IdentifiableEdge>, GraphCell>();
    this.jgraphEdgeToJungEdgeMap = new HashMap<GraphCell, GraphEvent.Edge<ModelNode,IdentifiableEdge>>();
    this.jungEdgeToJGraphEdgeMap = new HashMap<GraphEvent.Edge<ModelNode,IdentifiableEdge>, GraphCell>();
    this.jungGraph.addGraphEventListener(new WorkflowChangeListener(this));    
    this.addGraphModelListener(new GraphModelListener() {
      
      @Override
      public void graphChanged(GraphModelEvent e) {
        Object[] added = e.getChange().getInserted();
        //Object[] removed = 
        
      }
    });
    
  }

  public DefaultGraphCell getVertexCell(ModelNode node) {
    for (ModelNode v : this.jungGraph.getVertices()) {
      if (v.getId().equals(node.getId())) {
        DefaultGraphCell cell = new DefaultGraphCell();
        cell.setUserObject(v);
        return cell;
      }
    }

    return new DefaultGraphCell(node);
  }

  private AttributeMap getEdgeAttributes() {
    AttributeMap eMap = new AttributeMap();
    GraphConstants.setLineEnd(eMap, GraphConstants.ARROW_TECHNICAL);
    GraphConstants.setEndFill(eMap, true);
    GraphConstants.setEndSize(eMap, 10);
    GraphConstants.setForeground(eMap, Color.decode("#25507C"));
    GraphConstants.setFont(eMap,
        GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
    GraphConstants.setLineColor(eMap, Color.decode("#7AA1E6"));
    return eMap;
  }

  private AttributeMap getVertexAttributes() {
    AttributeMap vMap = new AttributeMap();

    Color c = Color.decode("#FF9900");
    GraphConstants.setBounds(vMap, new Rectangle2D.Double(50, 50, 90, 30));
    GraphConstants.setBorder(vMap, BorderFactory.createRaisedBevelBorder());
    GraphConstants.setBackground(vMap, c);
    GraphConstants.setForeground(vMap, Color.white);
    GraphConstants.setFont(vMap,
        GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
    GraphConstants.setOpaque(vMap, true);
    return vMap;
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
        System.out.println("EDGE ADDED!");
        GraphEvent.Edge<ModelNode, IdentifiableEdge> event = (GraphEvent.Edge<ModelNode, IdentifiableEdge>) e;
        ConnectionSet set = new ConnectionSet();
        DefaultEdge theEdge = new DefaultEdge(event.getEdge());
        DefaultGraphCell from = new DefaultGraphCell((Object) event.getEdge()
            .getFrom());
        from.add(new DefaultPort((Object) event.getEdge().getFrom()));
        DefaultGraphCell to = new DefaultGraphCell((Object) event.getEdge()
            .getTo());
        to.add(new DefaultPort((Object) event.getEdge().getTo()));
        set.connect(theEdge, (DefaultPort) from.getChildAt(0),
            (DefaultPort) to.getChildAt(0));
        insert(new Object[] { theEdge }, getEdgeAttributes(), set, null, null);
      } else if (e.getType().equals(GraphEvent.Type.EDGE_REMOVED)) {
        System.out.println("EDGE REMOVED!");
      } else if (e.getType().equals(GraphEvent.Type.VERTEX_ADDED)) {
        System.out.println("VERTEX ADDED!");
        GraphEvent.Vertex<ModelNode, IdentifiableEdge> event = (GraphEvent.Vertex<ModelNode, IdentifiableEdge>) e;
        DefaultGraphCell cell = new DefaultGraphCell(event.getVertex());
        cell.add(new DefaultPort());
        insert(new Object[] { cell }, getVertexAttributes(), null, null, null);

      } else if (e.getType().equals(GraphEvent.Type.VERTEX_REMOVED)) {
        System.out.println("VERTEX REMOVED!");
      }

    }

  }

}
