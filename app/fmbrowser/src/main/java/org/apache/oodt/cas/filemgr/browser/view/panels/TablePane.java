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


package org.apache.oodt.cas.filemgr.browser.view.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.oodt.cas.filemgr.browser.controller.TableListener;
import org.apache.oodt.cas.filemgr.browser.view.menus.RightClickMenu;

public class TablePane extends JPanel{ 
	
	public Vector<Integer> hiddenCols;
	public HeaderRow header;
	
	private TableListener listener;
	
	public TablePane(){
		listener = new TableListener(this);
		
		hiddenCols = new Vector<Integer>();
		setBackground(Color.WHITE);
		LineBorder line1 = new LineBorder(Color.WHITE,10);
		setBorder(line1);
		
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		
		this.setBlank();
	}
	
	public TableListener getListener(){
		return listener;
	}
	
	public void setBlank(){
		this.removeAll();
		int numColumns = 10;
		int numRows = 20;
		header = new HeaderRow(listener,numColumns);
		add(header);
		
		for(int i=0;i<numRows;i++){
			Row r = new Row(numColumns);
			add(r);
		}
		
		this.validate();
		this.repaint();
	}
	
	public void newTable(String[][] data){
		this.removeAll();
		if(data.length>1){
			header = new HeaderRow(listener,data[0].length);
			for(int j=0;j<data[0].length;j++){
				header.setText(j, data[0][j]);
			}
			this.add(header);
			
			for(int j=1;j<data.length;j++){
				Row r = new Row(data[0].length);
				for(int k=0;k<data[0].length;k++){
					r.setText(k, data[j][k]);
				}
				this.add(r);
			}
		}
		this.validate();
		this.repaint();
	}
	
	public HeaderRow getHeader(){
		return header;
	}

	public void sortRows(int col, String sortType){
		int length = this.getComponentCount();
		int i,j;

	    for (i=length; --i >=1;) {
	       for (j=1; j<i;j++) {
	    	   Row jThRow = (Row)this.getComponent(j);
	    	   Row iThRow = (Row)this.getComponent(i);
	    	   if(sortType.equals("Accending")){
	    		   if(jThRow.getText(col).compareTo(iThRow.getText(col))>0){
	    			   swapRows(j,j+1);
	    		   }
	    	   } else {
	    		   if(jThRow.getText(col).compareTo(iThRow.getText(col))<0){
	    			   swapRows(j,j+1);
	    		   }
	    	   }
	       }
	    }
	    this.validate();
	    this.repaint();
	    
	}
	
	public void swapRows(int index1, int index2){
		Component c1 = this.getComponent(index1);
		Component c2 = this.getComponent(index2);
		this.add(c1, index2);
		this.add(c2, index1);
	}
	
	public Row getRow(int num){
		Row r = null;
		if(num<this.getComponentCount()-1){
			r = (Row)this.getComponent(num+1);
		}
		return r;
	}
	
	public void hideColumn(int colNum){
		header.hideCol(colNum);
		for(int i=1;i<this.getComponentCount();i++){
			((Row)this.getComponent(i)).hideCol(colNum);
		}
		hiddenCols.add(new Integer(colNum));
	}
	
	public void unhideColumn(int colNum){
		header.unhideCol(colNum);
		for(int i=1;i<this.getComponentCount();i++){
			((Row)this.getComponent(i)).unhideCol(colNum);
		}
		hiddenCols.remove(new Integer(colNum));
		
	}

}
