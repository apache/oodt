//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs;

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