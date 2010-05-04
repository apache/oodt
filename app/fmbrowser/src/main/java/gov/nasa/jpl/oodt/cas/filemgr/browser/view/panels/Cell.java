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


package gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import gov.nasa.jpl.oodt.cas.filemgr.browser.view.GuiParams;

public class Cell extends JPanel{
	
	protected JLabel text;
	
	public Cell(){
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
		Dimension size = new Dimension(GuiParams.DEFAULT_CELL_WIDTH,GuiParams.DEFAULT_CELL_HEIGHT);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		setBorder(new LineBorder(Color.LIGHT_GRAY,1));	
		
		text = new JLabel("");
		Font f = new Font("san-sarif", Font.PLAIN, 10);
		text.setFont(f);
		this.add(text,BorderLayout.CENTER);
	}
	
	public void setText(String newText){
		text.setText(newText);
	}
	
	public String getText(){
		return text.getText();
	}
	
	public void setWidth(int newWidth){
		Dimension size = this.getSize();
		size.width = newWidth;
		this.setSize(size);
		text.setSize(size);
	}
	
	public void setHeight(int newHeight){
		Dimension size = this.getSize();
		size.height = newHeight;
		this.setSize(size);
		text.setSize(size);
	}
}
