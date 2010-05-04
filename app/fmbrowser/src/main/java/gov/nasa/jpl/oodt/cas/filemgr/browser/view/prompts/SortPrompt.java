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


package gov.nasa.jpl.oodt.cas.filemgr.browser.view.prompts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import gov.nasa.jpl.oodt.cas.filemgr.browser.view.MainWindow;

public class SortPrompt extends JFrame{
	
	private ColumnPanel cPanel;
	private ConnectButton cButtons;
	
	public SortPrompt(MainWindow window, ActionListener listener){
		this.setName("Sort");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Dimension d = new Dimension(350,150);
		this.setMinimumSize(d);
		this.setMaximumSize(d);
		this.setPreferredSize(d);
	
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
		this.getContentPane().setBackground(Color.WHITE);
		
		cPanel = new ColumnPanel(window);
		cButtons = new ConnectButton(listener);
		this.add(cPanel);
		this.add(cButtons);
	}
	
	public int getSortIndex(){
		return cPanel.getSortIndex();
	}
	
	public String getSortType(){
		return cPanel.getSortType();
	}
	
	protected class ColumnPanel extends JPanel{
		
		protected JComboBox ColChoices;
		protected JComboBox SortChoices;
		protected JButton cancel;
		protected JButton ok;
		
		public ColumnPanel(MainWindow window){
			ColChoices = new JComboBox(window.getColHeaders());
			ColChoices.setSelectedIndex(0);
			ColChoices.setBackground(Color.WHITE);
			String[] sortPatterns = {"Accending","Descending"};
			SortChoices = new JComboBox(sortPatterns);
			SortChoices.setSelectedIndex(0);
			SortChoices.setBackground(Color.WHITE);
			
			Dimension choicesDim = new Dimension(200,20);
			ColChoices.setMinimumSize(choicesDim);
			ColChoices.setMaximumSize(choicesDim);
			ColChoices.setPreferredSize(choicesDim);
			SortChoices.setMinimumSize(choicesDim);
			SortChoices.setMaximumSize(choicesDim);
			SortChoices.setPreferredSize(choicesDim);
			
			this.setBackground(Color.WHITE);
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			
			EmptyBorder line1 = new EmptyBorder(5,10,5,5);
			this.setBorder(line1);
			
			c.gridx = 0;
			c.gridy = 0;
			c.ipadx = 10;
			c.ipady = 10;
			this.add(new JLabel("Sort By:  "), c);
			
			c.gridx = 1;
			c.gridy = 0;
			this.add(ColChoices, c);
			
			c.gridx = 0;
			c.gridy = 1;
			this.add(new JLabel("In Order:  "), c);
			
			c.gridx = 1;
			c.gridy = 1;
			this.add(SortChoices, c);
			
		}
		
		public int getSortIndex(){
			return ColChoices.getSelectedIndex();
		}
		
		public String getSortType(){
			return SortChoices.getSelectedItem().toString();
		}
		
	}
	
	private class ConnectButton extends JPanel{
		
		protected JButton connect;
		protected JButton cancel;
		
		protected ConnectButton(ActionListener listener){
			this.setBackground(Color.WHITE);
			this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
			
			EmptyBorder line1 = new EmptyBorder(5,20,5,5);
			this.setBorder(line1);
			
			this.add(new JLabel("                          "));
			connect = new JButton("OK");
			connect.setBackground(Color.WHITE);
			connect.addActionListener(listener);
			cancel = new JButton("Cancel");
			cancel.setBackground(Color.WHITE);
			cancel.addActionListener(listener);
			cancel.setName("SortCancel");
			this.add(cancel);
			this.add(connect);
		}
	}
	
}
