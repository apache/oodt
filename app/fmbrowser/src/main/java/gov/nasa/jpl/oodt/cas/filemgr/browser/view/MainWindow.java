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


package gov.nasa.jpl.oodt.cas.filemgr.browser.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import gov.nasa.jpl.oodt.cas.filemgr.browser.controller.TableListener;
import gov.nasa.jpl.oodt.cas.filemgr.browser.controller.WindowListener;
import gov.nasa.jpl.oodt.cas.filemgr.browser.model.CasDB;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.BottomPane;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.HeaderRow;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.MiddlePane;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.QueryPane;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.prompts.ConnectPrompt;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.prompts.SortPrompt;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.menus.MenuBar;

public class MainWindow extends JFrame{ 
	
	public QueryPane qPane;
	public MiddlePane mPane;
	public BottomPane bPane;
	public MenuBar bar;
		
	private WindowListener wListener;
	
	public MainWindow(){
		wListener = new WindowListener(this);
		//db = new CasDB();
		setName("CAS File Manager Browser");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		qPane = new QueryPane(wListener);
		mPane = new MiddlePane();
		bPane = new BottomPane();
		Dimension d = new Dimension(GuiParams.WINDOW_WIDTH,GuiParams.WINDOW_HEIGHT);
		Container p = this.getContentPane();
        p.setPreferredSize(d);
        p.setMinimumSize(d);
        p.setMaximumSize(d);
		p.setBackground(Color.WHITE);
		p.setLayout(new BorderLayout());
		p.add(qPane,BorderLayout.NORTH);
		p.add(mPane,BorderLayout.CENTER);
		p.add(bPane,BorderLayout.SOUTH);
        
    
        bar = new MenuBar(wListener, mPane.getListener());
        this.setJMenuBar(bar);
	}
	
	public String[] getColHeaders(){
		HeaderRow h = mPane.tPane.getHeader();
		String[] heading = new String[h.getNumCols()];
		for(int i=0;i<h.getNumCols();i++){
			heading[i] = h.getText(i);
		}
		return heading;
	}
}
