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


package gov.nasa.jpl.oodt.cas.filemgr.browser.model;

import gov.nasa.jpl.oodt.cas.metadata.Metadata;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



public class Results{
	
	public Vector<Metadata> products;
	
	public Results(){
		products = new Vector<Metadata>();	
	}
	
	public void addProduct(Metadata m){
		products.add(m);
	}
	
	public int getNumRecords(){
		return products.size();
	}
	
	public String[][] getData(){
		
		String[][] data = null;
		if(products.size()>0){
			data = new String[products.size()+1][];
			
			Hashtable hash = products.firstElement().getHashtable();
			int numCols = hash.size();
			data[0] = new String[numCols];
			int i = 0;
			for (Enumeration e = hash.keys(); e.hasMoreElements();) {
			      data[0][i] = e.nextElement().toString();
			      System.out.println(data[0][i]);
			      i++;
			}
			
			for(int j=0;j<products.size();j++){
				data[j+1] = new String[i];
				for(int k=0;k<i;k++){
					data[j+1][k] = products.get(j).getMetadata(data[0][k]);
				}
			}
			
		}
		return data;
	}
}
