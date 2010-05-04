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


package gov.nasa.jpl.oodt.cas.filemgr.browser.controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import gov.nasa.jpl.oodt.cas.filemgr.browser.view.menus.RightClickMenu;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.HeaderCell;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.HeaderSpacer;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.Row;
import gov.nasa.jpl.oodt.cas.filemgr.browser.view.panels.TablePane;

public class TableListener implements MouseListener,ActionListener{
	
	private Component caller;
	private TablePane table;
	private int mousePos;
	private RightClickMenu rcMenu;
	
	public TableListener(TablePane t){
		table = t;
		mousePos = -1;
		caller = null;
		rcMenu = new RightClickMenu(this);
		
	}

	public void mouseClicked(MouseEvent arg0) {}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
			caller = e.getComponent();
			mousePos = e.getX()+caller.getX();
	}

	public void mouseReleased(MouseEvent e) {
		if(caller!=null){	
			if(caller instanceof HeaderSpacer){
				HeaderSpacer hsCaller = (HeaderSpacer)caller;
				if(e.getModifiers()==18 && table.hiddenCols.contains(new Integer(hsCaller.getColNum()+1))){
					rcMenu.setUnhideMode();
					rcMenu.show(caller, e.getX(), e.getY());
										
				} else {
					int change = e.getX()+caller.getX() - mousePos;
					int curWidth = table.header.getWidth(hsCaller.getColNum());
					table.header.changeWidth(hsCaller.getColNum(), curWidth+change);
			
					for(int i=1;i<table.getComponentCount();i++){
						((Row)table.getComponent(i)).changeWidth(((HeaderSpacer)caller).getColNum(), curWidth+change+2);
					}
					caller = null;
				}
			} else {
				if(caller instanceof HeaderCell && e.getModifiers()==18){
					rcMenu.setHideMode();
					rcMenu.show(caller, e.getX(), e.getY());
					
				}
			}
		}
		
	}

	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals("Hide")){
			if(caller!=null){
				int colNum = ((HeaderCell)caller).getColNum();
				table.hideColumn(colNum);
				caller=null;
			}
		} else if(arg0.getActionCommand().equals("Unhide")){
			if(caller!=null){
				int colNum = ((HeaderSpacer)caller).getColNum()+1;
				table.unhideColumn(colNum);
				caller=null;
			}
		}else if(arg0.getActionCommand().equals("Unhide Columns")){
			while(!table.hiddenCols.isEmpty()){
				table.unhideColumn((table.hiddenCols.firstElement()).intValue());
			}
		} else if(arg0.getActionCommand().equals("Export Table")){
			
			final JFileChooser fc = new JFileChooser();						
			int returnVal = fc.showSaveDialog(table);
			if(returnVal== JFileChooser.APPROVE_OPTION){
				
				//write out excel file
				String fullFileName = (fc.getSelectedFile()).getAbsolutePath();
				if(!fullFileName.endsWith(".xls")) fullFileName+=".xls";
				
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = wb.createSheet("results");
				HSSFRow headerRow = sheet.createRow((short)0);

				int i=0;
				for(int j=0;j<table.getRow(0).getComponentCount();j++){
					if(!table.hiddenCols.contains(new Integer(j))){
						headerRow.createCell((short)i).setCellValue(table.header.getText(j));
						i++;
					}
				}

				for(int k=0;k<table.getComponentCount()-1;k++){
					HSSFRow row = sheet.createRow((short)k+1);
					i=0;
					for(int j=0;j<table.getRow(0).getComponentCount();j++){
						if(!table.hiddenCols.contains(new Integer(j))){
							row.createCell((short)i).setCellValue((table.getRow(k)).getText(j));
							i++;
						}
					}
				}
			
				FileOutputStream fileOut;
				try {
				
					fileOut = new FileOutputStream(fullFileName);
					wb.write(fileOut);
					fileOut.close();
			    
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		    
		}
		
	}
	
}
