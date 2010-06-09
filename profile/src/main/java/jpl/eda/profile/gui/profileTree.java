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
