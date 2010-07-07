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


package org.apache.oodt.cas.filemgr.browser.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.oodt.cas.filemgr.browser.model.CasDB;
import org.apache.oodt.cas.filemgr.browser.view.MainWindow;
import org.apache.oodt.cas.filemgr.browser.view.prompts.ConnectPrompt;
import org.apache.oodt.cas.filemgr.browser.view.prompts.QueryBuilderPrompt;
import org.apache.oodt.cas.filemgr.browser.view.prompts.SortPrompt;

public class WindowListener implements ActionListener{

	private MainWindow window;
	private ConnectPrompt prompt;
	private SortPrompt sort;
	private QueryBuilderPrompt query;
	
	private CasDB db;
	
	public WindowListener(MainWindow m){
		window = m;
		db = new CasDB();
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals("About")){
			String aboutInfo = 
				"OODT Catalog and Archive Server File Manger Browser.\n" +
				"Copyright (c) 2007, California Institute of Technology.\n" +
				"ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.";
			JOptionPane.showMessageDialog(window,aboutInfo);
		} else if(arg0.getActionCommand().equals("Query Language")){
			String aboutQueryLanguage =
				"The CAS File Manager Browser uses the Lucene Query Language.\n" +
				"More Information can be found at:\n" +
				"http://lucene.apache.org/java/docs/queryparsersyntax.html";
			JOptionPane.showMessageDialog(window,aboutQueryLanguage);
		} else if(arg0.getActionCommand().equals("Exit")){
			String exitWarning = 
				"Are you sure you want to exit the CAS File Manager Browser?\n"+
				"All unexported queries will be lost.";
			int returnVal = JOptionPane.showConfirmDialog(window, exitWarning, "Exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(returnVal == JOptionPane.OK_OPTION){
				System.exit(0);
			}
		} else if(arg0.getActionCommand().equals("Connect...")){
			//this is from the main menu
			window.bar.changeConnectStatus();
			prompt = new ConnectPrompt(this);
			prompt.pack();
            prompt.setVisible(true);
		} else if(arg0.getActionCommand().equals("Disconnect")){
			String disconnectWarning = 
				"Are you sure you want to disconnect from this File Manager?\n"+
				"All unexported queries will be lost.";
			int returnVal = JOptionPane.showConfirmDialog(window, disconnectWarning, "Disconnect", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(returnVal == JOptionPane.OK_OPTION){
				window.bar.changeConnectStatus();
				db.disconnect();
				window.mPane.tPane.setBlank();
				window.qPane.updateTypes(new String[]{""});
				window.bPane.changeStatus("Disconnected");
			}
		} else if(arg0.getActionCommand().equals("Cancel")){
			if(((JButton)arg0.getSource()).getName().equals("ConnectCancel")){
				prompt.dispose();
				window.bar.changeConnectStatus();
			} else if(((JButton)arg0.getSource()).getName().equals("SortCancel")){
				sort.dispose();
			}
		} else if(arg0.getActionCommand().equals("Connect")){
			//this is from the connect prompt
			boolean connected = db.connect(prompt.getCASUrl());
			if(connected){
				window.qPane.updateTypes(db.getAvailableTypes());
				window.bPane.changeStatus("Connected to "+prompt.getCASUrl());
			} else {
				window.bar.changeConnectStatus();
				String errorConnectingString =
					"Error Connecting to CAS File Manager at this following address:"+
					prompt.getCASUrl();
				JOptionPane.showMessageDialog(window, errorConnectingString, "Error", JOptionPane.ERROR_MESSAGE);
			}
			prompt.dispose();
		} else if(arg0.getActionCommand().equals("Sort")){
			sort = new SortPrompt(window, this);
			sort.pack();
			sort.setVisible(true);
		} else if(arg0.getActionCommand().equals("Query Builder")){
			if(db.isConnected()){
				query = new QueryBuilderPrompt(db, this);
				query.pack();
				query.setVisible(true);
			} else {
				String errorQueryString =
					"You must connect to a CAS File Manager before using the Query Builder.";
				JOptionPane.showMessageDialog(window, errorQueryString, "Disconnected", JOptionPane.WARNING_MESSAGE);
			}
		} else if(arg0.getActionCommand().equals("OK")){
			window.mPane.tPane.sortRows(sort.getSortIndex(),sort.getSortType());
			sort.dispose();
		} else if(arg0.getActionCommand().equals("Search")){
			if(((JButton)arg0.getSource()).getName().equals("AdvancedQuery")){
				if(db.isConnected()){
					window.bPane.changeStatus("Querying the CAS...");
					boolean result = db.issueQuery(query.getQuery(), query.getProductType());
					if(result){
						window.mPane.tPane.newTable(db.results.getData());
						window.bPane.changeStatus("Query: "+query.getQueryString()+ " returned "+db.results.getNumRecords()+" records.");
					} else {
						window.mPane.tPane.setBlank();
						window.bPane.changeStatus("Query failed.");
						window.qPane.clearQuery();
					}
				}
				query.dispose();
			} else {
				if(db.isConnected()){
					window.bPane.changeStatus("Querying the CAS...");
					String query = window.qPane.getQuery();
					boolean result = db.createQuery(query, window.qPane.getType());
					if(result){
						window.mPane.tPane.newTable(db.results.getData());
						window.bPane.changeStatus("Query: "+query+ " returned "+db.results.getNumRecords()+" records.");
					} else {
						window.mPane.tPane.setBlank();
						window.bPane.changeStatus("Query failed.");
						window.qPane.clearQuery();
					}
				}
			}
		} else if(arg0.getActionCommand().equals("Clear Query")){
			String clearWarning = 
				"Are you sure you want to clear the current Query?\n"+
				"All unexported queries will be lost.";
			int returnVal = JOptionPane.showConfirmDialog(window, clearWarning, "Clear", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(returnVal == JOptionPane.OK_OPTION){
				window.mPane.tPane.setBlank();
				window.bPane.changeStatus("Query cleared.");
			}
		}
	}
	
}
