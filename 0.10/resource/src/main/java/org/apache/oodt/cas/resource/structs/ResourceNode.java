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


package org.apache.oodt.cas.resource.structs;

//JAVA imports
import java.net.URL;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A node struct to capture information about each resource.
 * </p>
 * 
 */
public class ResourceNode {
	
	private String nodeId = null;
	private URL ipAddr = null;
	private int capacity = 0;
	
	public ResourceNode(){}
	
	public ResourceNode(String nodeId, URL ipAddr, int capacity){
		this.nodeId=nodeId;
		this.ipAddr=ipAddr;
		this.capacity=capacity;
	}
	
	public String getNodeId(){
		return nodeId;
	}
	
	public URL getIpAddr(){
		return ipAddr;
	}
	
	public int getCapacity(){
		return capacity;
	}
	
	public void setId(String nodeId){
		this.nodeId = nodeId;
	}
	
	public void setIpAddr(URL ipAddr){
		this.ipAddr = ipAddr;
	}
	
	public void setCapacity(int capacity){
		this.capacity = capacity;
	}
}
