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


package org.apache.oodt.cas.filemgr.browser.view.menus;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class RightClickMenu extends JPopupMenu{
	
	private JMenuItem hideItem;
	private JMenuItem unhideItem;
	//private JMenuItem resizeItem;
	
	
	public RightClickMenu(ActionListener listener){
		
		hideItem = new JMenuItem("Hide");
		hideItem.addActionListener(listener);
		unhideItem = new JMenuItem("Unhide");
		unhideItem.addActionListener(listener);
		//resizeItem = new JMenuItem("Resize");
		//resizeItem.addActionListener(listener);
		
		this.add(hideItem);
		this.add(unhideItem);
		//this.addSeparator();
		//this.add(resizeItem);
	}
	
	public void setUnhideMode(){
		hideItem.setEnabled(false);
		unhideItem.setEnabled(true);
		//resizeItem.setEnabled(false);
	}
	
	public void setHideMode(){
		hideItem.setEnabled(true);
		unhideItem.setEnabled(false);
		//resizeItem.setEnabled(true);
	}
	
}
