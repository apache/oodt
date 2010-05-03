package jpl.eda.profile.gui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


public class profileTree extends JTree{
	
	public profileTree(){
		setEditable(true);
		DefaultMutableTreeNode dtmRoot = new DefaultMutableTreeNode("Profile");
		DefaultMutableTreeNode resAttrRoot = new DefaultMutableTreeNode("Resource Attributes");
		DefaultMutableTreeNode profAttrRoot = new DefaultMutableTreeNode("Profile Attributes");
		DefaultMutableTreeNode profElemRoot = new DefaultMutableTreeNode("Profile Elements");
		dtmRoot.add(resAttrRoot);
		dtmRoot.add(profAttrRoot);
		dtmRoot.add(profElemRoot);
		DefaultTreeModel theModel = new DefaultTreeModel(dtmRoot);
		setModel(theModel);
	}
	
	
}