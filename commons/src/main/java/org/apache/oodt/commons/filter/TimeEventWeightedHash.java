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


package org.apache.oodt.commons.filter;

//JDK imports
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
* 
* @author bfoster
* @version $Revision$
*
* <p>
* Builds a time sorted hash where the top value has the earliest time.  This hash
* is a directional graph.  It builds all possible paths from top node to leaf
* nodes.  At any time you can ask for the greatest weighted path in the hash -- that
* is, the path from root node to a leaf node which fills the most time. Epsilon is 
* TimeEvent objects allowed time overlap (it is of the same unit of measurement that 
* is used in you TimeEvent objects added to this hash).
* </p>.
*/
public class TimeEventWeightedHash {

  private TimeEventNode root;
  
  protected long epsilon;
  
  private MyLinkedHashSet<TimeEventNode> leafNodes;
      
  private TimeEventWeightedHash() {
      root = new TimeEventNode(new TimeEvent(-1, -1));
      leafNodes = new MyLinkedHashSet<TimeEventNode>();
      leafNodes.add(this.root);
  }
  
  public static TimeEventWeightedHash buildHash(List<? extends TimeEvent> events) {
	  return buildHash(events, 0);
  }
  
  /**
   * Returns the Greatest Weighted Path of TimeEvents
   */
  public static TimeEventWeightedHash buildHash(List<? extends TimeEvent> events, long epsilon) {
	  TimeEventWeightedHash hash = new TimeEventWeightedHash();
	  hash.epsilon = epsilon;
      events = TimeEvent.getTimeOrderedEvents(events);
      for (TimeEvent event : events) {
          hash.addEvent(event);
      }
      return hash;
  }
  
  public List<? extends TimeEvent> getGreatestWeightedPathAsOrderedList() {
      WeightedNode wn = this.getGreatestWeightedPath();
      List<TimeEvent> gwpEvents = new LinkedList<TimeEvent>();
      while(wn != null) {
          gwpEvents.add(wn.getTimeEvent());
          wn = wn.getChild();
      }
      
      return gwpEvents;
  }
 
  private void addEvent(TimeEvent newEvent) {
      TimeEventNode newEventNode = new TimeEventNode(newEvent);
      MyLinkedHashSet<TimeEventNode> parentNodes = this.findParents(newEvent);
      MyLinkedHashSet<TimeEventNode> childrenNodes = this.findChildren(newEvent, parentNodes);
      
      if (childrenNodes.size() == 0) {
          leafNodes.add(newEventNode);
      }
      
      newEventNode.addParents(parentNodes);
      newEventNode.addChildren(childrenNodes);
      
  }
  
  private MyLinkedHashSet<TimeEventNode> findParents(TimeEvent newEvent) {
      MyLinkedHashSet<TimeEventNode> parentNodes = new MyLinkedHashSet<TimeEventNode>();
      MyLinkedHashSet<TimeEventNode> possibleParentNodes = new MyLinkedHashSet<TimeEventNode>();
      MyLinkedHashSet<TimeEventNode> possibleParentNodesAlreadyChecked = new MyLinkedHashSet<TimeEventNode>();
      
      //iterate through possible parent nodes add possible parent child as
      // possible parents if they do not conflict
      TimeEventNode curPPN = this.root;
      while (curPPN != null) {
          boolean ppnListChanged = false;
          for (TimeEventNode curChild : curPPN.getChildren()) {
              //if curChild does not conficts with te, then it is a possible parent
              if (happensBefore(curChild.getTimeEvent(), newEvent) 
                      && !possibleParentNodesAlreadyChecked.contains(curChild)) {
                  possibleParentNodes.add(curChild);
                  ppnListChanged = true;
              }
          }
          //if all children where not possible parents, then curPPN must be parent
          if (!ppnListChanged) {
              parentNodes.add(curPPN);
          }
          
          //get next curPPN
          possibleParentNodes.remove(curPPN);
          possibleParentNodesAlreadyChecked.add(curPPN);
          while((curPPN = possibleParentNodes.get(0)) != null 
                  && possibleParentNodesAlreadyChecked.contains(curPPN)) {
              possibleParentNodes.remove(curPPN);
          }
      }
      
      return parentNodes;
  }
  
  private MyLinkedHashSet<TimeEventNode> findChildren(TimeEvent newEvent, MyLinkedHashSet<TimeEventNode> parentNodes) {
      MyLinkedHashSet<TimeEventNode> childrenNodes = new MyLinkedHashSet<TimeEventNode>();
      MyLinkedHashSet<TimeEventNode> possibleChildrenNodes = new MyLinkedHashSet<TimeEventNode>();
      for (TimeEventNode parent : parentNodes) {
          possibleChildrenNodes.addAll(parent.getChildren());
          TimeEventNode curPCN = possibleChildrenNodes.get(0);
          while (curPCN != null && !parentNodes.contains(curPCN)) {
              if (happensBefore(newEvent, curPCN.getTimeEvent())) {
                  childrenNodes.add(curPCN);
              }else {
                  possibleChildrenNodes.addAll(curPCN.getChildren());
              }
              possibleChildrenNodes.remove(curPCN);
              curPCN = possibleChildrenNodes.get(0);
          }
      }
      return childrenNodes;
  }
  
  private boolean happensBefore(TimeEvent t1, TimeEvent t2) {
      long boundaryCheck = t2.getStartTime() - t1.getEndTime();
      return t1.getStartTime() < t2.getStartTime() && (boundaryCheck + epsilon) > 0;
  }
  
  private WeightedNode getGreatestWeightedPath() {
      TimeEventNode max = this.leafNodes.get(0);
      for (TimeEventNode ten : this.leafNodes) {
         if (ten.getPathWeight() > max.getPathWeight() 
                 || (ten.getPathWeight() == max.getPathWeight() 
                         && ten.getPathPriorityWeight() > max.getPathPriorityWeight())) {
             max = ten;
         }
      }
      WeightedNode root = new WeightedNode(max.getTimeEvent());
      TimeEventNode curTEN = max.greatestWieghtedParent;
      while (curTEN != null) {
          WeightedNode temp = new WeightedNode(curTEN.getTimeEvent());
          temp.setChild(root);
          root = temp;
          curTEN = curTEN.greatestWieghtedParent;
      }
      return root.getChild();
  }
  
  private class MyLinkedHashSet<E> extends HashSet<E> {

      private static final long serialVersionUID = -7319154087430025841L;
      
      private LinkedList<E> listSet;
      
      public MyLinkedHashSet() {
          this.listSet = new LinkedList<E>();
      }
      
      @Override
      public boolean add(E ten) {
          boolean wasAdded;
          if (wasAdded = super.add(ten)) {
              listSet.add(ten);
          }
          return wasAdded;
      }
      
      @Override
      public boolean addAll(Collection<? extends E> collection) {
          boolean setChanged = false;
          for (E ten : collection) {
              if (this.add(ten)) {
                  setChanged = true;
              }
          }
          return setChanged;
      }
      
      @Override
      public boolean remove(Object ten) {
          if (super.remove(ten)) {
              this.listSet.remove(ten);
              return true;
          }else {
              return false;
          }
      }
      
      @Override
      public boolean removeAll(Collection<?> collection) {
          boolean setChanged = false;
          for (Object obj : collection) {
              if (this.remove(obj)) {
                  setChanged = true;
              }
          }
          return setChanged;  
      }
      
      public E get(int index) {
          if (this.listSet.size() > index) {
              return this.listSet.get(index);
          } else {
              return null;
          }
      }
      
      public List<E> getList() {
          return this.listSet;
      }
      
  }

  private class TimeEventNode {

      private TimeEvent te;
      
      private MyLinkedHashSet<TimeEventNode> children;

      private MyLinkedHashSet<TimeEventNode> parents;
      
      private TimeEventNode greatestWieghtedParent;
              
      public TimeEventNode(TimeEvent te) {
          this.te = te;
          children = new MyLinkedHashSet<TimeEventNode>();
          parents = new MyLinkedHashSet<TimeEventNode>();
      }
      
      public long getPathWeight() {
          if (this.greatestWieghtedParent != null) {
              return te.getDuration()
                     + this.greatestWieghtedParent.getPathWeight();
          } else {
              return te.getDuration();
          }
      }

      public double getPathPriorityWeight() {
          if (this.greatestWieghtedParent != null) {
              return te.getPriority()
                     + this.greatestWieghtedParent.getPathPriorityWeight();
          } else {
              return te.getPriority();
          }
      }

      public TimeEvent getTimeEvent() {
          return this.te;
      }

      public void setTimeEvent(TimeEvent te) {
          this.te = te;
      }
      
      public void addChild(TimeEventNode child) {            
          //remove parent to grandchild links
          for (TimeEventNode parent : this.parents) {
              parent.children.remove(child);
              child.parents.remove(parent);
          }
          
          //link to child
          this.children.add(child);
          child.parents.add(this);
          leafNodes.remove(this);
          
          //determine child's greatest weighted parent
          child.greatestWieghtedParent = this;
          for (TimeEventNode parent : child.parents) {
              long gwpPaW = child.greatestWieghtedParent.getPathWeight();
              long pPaW = parent.getPathWeight();
              double gwpPiW = child.greatestWieghtedParent.getPathPriorityWeight();
              double pPiW = parent.getPathPriorityWeight();
              if (pPaW > gwpPaW || (pPaW == gwpPaW && pPiW > gwpPiW)) {
                  child.greatestWieghtedParent = parent;
              }
          }
      }
      
      public void addParent(TimeEventNode parent) {
          parent.addChild(this);
      }

      public void addChildren(Collection<TimeEventNode> children) {
          for (TimeEventNode child : children) {
              this.addChild(child);
          }
      }
      
      public void addParents(Collection<TimeEventNode> parents) {
          for (TimeEventNode parent : parents) {
              this.addParent(parent);
          }
      }

      public MyLinkedHashSet<TimeEventNode> getChildren() {
          return this.children;
      }
      
      public MyLinkedHashSet<TimeEventNode> getParents() {
          return this.parents;
      }

      public boolean equals(Object obj) {
          if (obj instanceof TimeEventNode) {
              TimeEventNode ten = (TimeEventNode) obj;
              return this.te.equals(ten.te);
          } else {
              return false;
          }
      }

      public String toString() {
          return this.te.toString() + " -- " + this.getPathWeight();
      }
      
  }

  private class WeightedNode {

      private long pathWeight;

      private WeightedNode child;

      private TimeEvent te;

      private WeightedNode(TimeEvent te) {
          this.te = te;
          pathWeight = te.getDuration();
      }

      private void setChild(WeightedNode child) {
          this.child = child;
          if (child != null) {
              this.pathWeight = this.te.getDuration() + child.getPathWeight();
          }
      }

      public TimeEvent getTimeEvent() {
          return this.te;
      }

      public WeightedNode getChild() {
          return this.child;
      }

      public long getPathWeight() {
          return this.pathWeight;
      }

      public String toString() {
          return te.toString() + "\n" 
              + (child != null ? child.toString() : "");
      }

  }

  public String toString() {
      StringBuilder sb = new StringBuilder("");
      LinkedList<TimeEventNode> printNodes = new LinkedList<TimeEventNode>();
      printNodes.add(this.root);
      sb.append(printNodes(printNodes, "-", 0));
      return sb.toString();
  }

  private StringBuffer printNodes(List<TimeEventNode> list,
          String spacer, long curPathWeight) {
      StringBuffer output = new StringBuffer("");
      for (TimeEventNode node : list) {
          output.append(spacer);
          output.append(node.te).append(" -- ").append(curPathWeight + node.te.getDuration()).append("\n");
          output.append(printNodes(node.getChildren().getList(), " " + spacer, curPathWeight + node.te.getDuration()));
      }
      return output;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeEventWeightedHash that = (TimeEventWeightedHash) o;

        if (epsilon != that.epsilon) {
            return false;
        }
        if (root != null ? !root.equals(that.root) : that.root != null) {
            return false;
        }
        return !(leafNodes != null ? !leafNodes.equals(that.leafNodes) : that.leafNodes != null);

    }

    @Override
    public int hashCode() {
        int result = root != null ? root.hashCode() : 0;
        result = 31 * result + (int) (epsilon ^ (epsilon >>> 32));
        result = 31 * result + (leafNodes != null ? leafNodes.hashCode() : 0);
        return result;
    }
}
