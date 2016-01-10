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

package org.apache.oodt.profile.gui;

import org.apache.oodt.profile.EnumeratedProfileElement;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileElement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;



public class LeafListener extends MouseAdapter{
	  profileTree tree;
	  
	  public LeafListener(profileTree jtr){

	    tree = jtr;

	  }

	  private boolean isValidMultiTerm(String t){
	  	if(t==null){return false;}
	  	
	  	if(t.equals("Children")){
	  		return true;
	  	}
	  	else if(t.equals("Revision Notes")){
	  		return true;
	  	}
	  	else if(t.equals("Contexts")){
	  		return true;
	  	}
	  	else if(t.equals("Contributors")){
	  		return true;
	  	}
	  	else if(t.equals("Coverages")){
	  		return true;
	  	}
	  	else if(t.equals("Creators")){
	  		return true;
	  	}
	  	else if(t.equals("Dates")){
	  		return true;
	  	}
	  	else if(t.equals("Formats")){
	  		return true;
	  	}
	  	else if(t.equals("Languages")){
	  		return true;
	  	}
	  	else if(t.equals("Resource Locations")){
	  		return true;
	  	}
	  	else if(t.equals("Publishers")){
	  		return true;
	  	}
	  	else if(t.equals("Relations")){
	  		return true;
	  	}
	  	else if(t.equals("Rights")){
	  		return true;
	  	}
	  	else if(t.equals("Sources")){
	  		return true;
	  	}
	  	else if(t.equals("Subjects")){
	  		return true;
	  	}
	  	else if(t.equals("Types")){
	  		return true;
	  	}
	  	else if(t.equals("Profile Elements")){
	  		return true;
	  	}
	  	else if(t.equals("Synonyms")){
	  		return true;
	  	}

	  	return false;
	    
	  }
	  
	  public DefaultMutableTreeNode generateNewProfileElementTree(String peName){
	  	 DefaultMutableTreeNode peRoot;
	  	 
	  	 ProfileElement theProfileElement = new EnumeratedProfileElement(new Profile());
          
	    	
	    	peRoot = new DefaultMutableTreeNode(peName);
	    	DefaultMutableTreeNode theCommentsRoot = new DefaultMutableTreeNode("Comments");
	    	DefaultMutableTreeNode theComments = new DefaultMutableTreeNode(theProfileElement.getComments());
	    	
	    	theCommentsRoot.add(theComments);
	    	
	    	DefaultMutableTreeNode theDesc = new DefaultMutableTreeNode(theProfileElement.getDescription());
	    	DefaultMutableTreeNode theDescRoot = new DefaultMutableTreeNode("Description");
	    	
	    	theDescRoot.add(theDesc);
	    	
	    	
	    	
	    	DefaultMutableTreeNode theID = new DefaultMutableTreeNode(theProfileElement.getID());
	    	DefaultMutableTreeNode theIDRoot = new DefaultMutableTreeNode("ID");
	    	
	    	theIDRoot.add(theID);
	    	
	    	DefaultMutableTreeNode theMO = new DefaultMutableTreeNode(
				Integer.toString(theProfileElement.getMaxOccurrence()));
	    	DefaultMutableTreeNode theMORoot = new DefaultMutableTreeNode("Max Occurence");
	    	theMORoot.add(theMO);
	    	
	    	DefaultMutableTreeNode theSynonyms = new DefaultMutableTreeNode("Synonyms");

		for (Object o : theProfileElement.getSynonyms()) {
		  String theSynonym = (String) o;
		  DefaultMutableTreeNode sNode = new DefaultMutableTreeNode(theSynonym);
		  theSynonyms.add(sNode);
		}
	
	    	DefaultMutableTreeNode theType = new DefaultMutableTreeNode(theProfileElement.getType());
	    	DefaultMutableTreeNode theTypeRoot = new DefaultMutableTreeNode("Type");
	    	theTypeRoot.add(theType);
	    	
	    	
	    	DefaultMutableTreeNode theUnit = new DefaultMutableTreeNode(theProfileElement.getUnit());
	    	DefaultMutableTreeNode theUnitRoot = new DefaultMutableTreeNode("Unit");
	    	theUnitRoot.add(theUnit);
    	
    	peRoot.add(theCommentsRoot);
    	peRoot.add(theDescRoot);
    	peRoot.add(theIDRoot);
    	peRoot.add(theMORoot);
    	peRoot.add(theSynonyms);
    	peRoot.add(theTypeRoot);
    	peRoot.add(theUnitRoot);
    	
    	
	  	 return peRoot;
	  	
	  }

	  protected void menuItem2ActionPerformed(ActionEvent evt,DefaultMutableTreeNode theTreeNode,DefaultTreeModel theModel){
	  	
	  	//only add child if it's one of the things that should have children
	  	String treeNodeName = (String)theTreeNode.getUserObject();
	  	
	  	if(isValidMultiTerm(treeNodeName)){
	  		return; //only edit non multi-term fields
	  	}
	
	  	
	  	String newNodeName = (String)JOptionPane.showInputDialog(null,"Edit","Enter New Node Value", JOptionPane.PLAIN_MESSAGE,null,null,"New Value");
	  	
	  	if(newNodeName == null){return; } //they didn't specify name or hit cancel
	  	theTreeNode.setUserObject(newNodeName);
	  	theModel.reload();
	  }
	  
	  
	  protected void menuItem1ActionPerformed(ActionEvent evt,DefaultMutableTreeNode theTreeNode,DefaultTreeModel theModel){
	  	
	  	//only add child if it's one of the things that should have children
	  	String treeNodeName = (String)theTreeNode.getUserObject();
	  	
	  	if(!isValidMultiTerm(treeNodeName)){
	  		return;
	  	}
	
	  	DefaultMutableTreeNode theAddNode;
	  	String childNodeName = (String)JOptionPane.showInputDialog(null,"Add","Enter Node Name", JOptionPane.PLAIN_MESSAGE,null,null,"Child Value");
	  	
	  	if(childNodeName == null){return; } //they didn't specify name or hit cancel
	  	
	  	if(treeNodeName.equals("Profile Elements")){
	  		theAddNode = generateNewProfileElementTree(childNodeName);
	  	}
	  	else{
	  		theAddNode = new DefaultMutableTreeNode(childNodeName);
	  	}
	  	
	  	 
           	
	  	theTreeNode.add(theAddNode);
	  	theModel.reload();
	  }
	  
	  
	  public void mousePressed(MouseEvent e) {

	    int selectedRow = tree.getRowForLocation(e.getX(), e.getY());
	    TreePath selectedPath = tree.getPathForLocation(e.getX(), e.getY());
	    
	    if(selectedRow != -1) {
	       DefaultMutableTreeNode tn               //get end of current path
	        = (DefaultMutableTreeNode)(selectedPath.getLastPathComponent());

	       	JMenuItem menuItem1 = new JMenuItem("Add");
	       	JMenuItem menuItem2 = new JMenuItem("Edit");
	       	
	       	menuItem2.addActionListener(new TreeNodeActionListener(tn,this,(DefaultTreeModel)tree.getModel(),"EDIT"));
	       	menuItem1.addActionListener(new TreeNodeActionListener(tn,this,(DefaultTreeModel)tree.getModel(),"ADD"));       	
	       	JPopupMenu popup = new JPopupMenu();
	       	popup.add(menuItem1);
	       	popup.add(menuItem2);
	       	
	       	if(e.isPopupTrigger()){
	       		popup.show(tree,e.getX(),e.getY());
	       	}

	    }
	  }
	  
	  public void mouseReleased(MouseEvent e) {

	    int selectedRow = tree.getRowForLocation(e.getX(), e.getY());
	    TreePath selectedPath = tree.getPathForLocation(e.getX(), e.getY());
	    
	    if(selectedRow != -1) {
	       DefaultMutableTreeNode tn               //get end of current path
	        = (DefaultMutableTreeNode)(selectedPath.getLastPathComponent());

	       	JMenuItem menuItem1 = new JMenuItem("Add");
	       	JMenuItem menuItem2 = new JMenuItem("Edit");
	       	
	       	menuItem2.addActionListener(new TreeNodeActionListener(tn,this,(DefaultTreeModel)tree.getModel(),"EDIT"));
	       	menuItem1.addActionListener(new TreeNodeActionListener(tn,this,(DefaultTreeModel)tree.getModel(),"ADD"));  
	       	JPopupMenu popup = new JPopupMenu();
	       	popup.add(menuItem1);
	       	popup.add(menuItem2);
	       	
	       	if(e.isPopupTrigger()){
	       		popup.show(tree,e.getX(),e.getY());
	       	}

	    }
	  }
	  
	  public class TreeNodeActionListener implements ActionListener{
	  	 private DefaultMutableTreeNode myTreeNode = null;
	  	 private DefaultTreeModel myTreeModel = null;
	  	 private LeafListener theLeafListener=null;
	  	 private String type=null;
	  	 
	  	  public TreeNodeActionListener(DefaultMutableTreeNode t,LeafListener l,DefaultTreeModel tm,String theType){
	  	  	myTreeNode = t;
	  	  	theLeafListener = l;
	  	  	myTreeModel = tm;
	  	  	type = theType;
	  	  }
	  	  
			public void actionPerformed(ActionEvent evt) {
				if(type.equals("ADD")){
					theLeafListener.menuItem1ActionPerformed(evt,myTreeNode,myTreeModel);				
				}
				else if(type.equals("EDIT")){
					theLeafListener.menuItem2ActionPerformed(evt,myTreeNode,myTreeModel);
				}
			}
	  	  
	  }
	  
	}
