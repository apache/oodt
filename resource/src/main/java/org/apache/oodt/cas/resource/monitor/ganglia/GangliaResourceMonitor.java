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

package org.apache.oodt.cas.resource.monitor.ganglia;

//OODT imports
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.monitor.ganglia.loadcalc.LoadCalculator;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.NAME;

//JDK imports

/**
 * @author rajith
 * @author mattmann
 * @version $Revision$
 */
public class GangliaResourceMonitor implements Monitor {

	private static final Logger LOG = Logger
			.getLogger(GangliaResourceMonitor.class.getName());
	private LoadCalculator loadCalculator;
	private Map<String, Integer> loadMap;
	private Map<String, Map<String, String>> gmetaNodes;
	private Map<String, GangliaAdapter> gmetaAdapters;
	private static final int DEFAULT_PORT = 8649;

	/**
	 * Make a new GangliaResourceMonitor that reads information from a ganglia
	 * meta daemon.
	 * 
	 * @param loadCalculator
	 *            LoadCalculator
	 *            {@link org.apache.oodt.cas.resource.monitor.ganglia.loadcalc.LoadCalculator}
	 *            to calculate load
	 */
	public GangliaResourceMonitor(LoadCalculator loadCalculator,
			String gmetadHost, int gmetadPort) {
		this.loadCalculator = loadCalculator;
		this.loadMap = new ConcurrentHashMap<String, Integer>();
		this.gmetaNodes = new ConcurrentHashMap<String, Map<String, String>>();
		this.gmetaAdapters = new ConcurrentHashMap<String, GangliaAdapter>();
		try {
			this.initGmetaNodes(gmetadHost, gmetadPort);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			LOG.log(Level.WARNING,
					"URL exception initializing gmetad nodes: [" + gmetadHost
							+ ":" + gmetadPort + "]: Message: "
							+ e.getMessage());
		}

	}

	@Override
	public int getLoad(ResourceNode node) throws MonitorException {
		Map<String, String> nodeProperties;
		String nodeId = node.getNodeId();
		nodeProperties = this.locateNode(nodeId);
		if (nodeProperties == null) {
			throw new MonitorException(
					"GangliaMonitor: not tracking requested node: [" + nodeId
							+ "]");
		}

		// calculate load
		double calcLoad = this.loadCalculator.calculateLoad(nodeProperties);
		System.out.println(calcLoad);
		int load = Long.valueOf(Math.round(calcLoad)).intValue();
		System.out.println("LOAD is: "+load);
		return load;
	}

	@Override
	public boolean assignLoad(ResourceNode node, int loadValue)
			throws MonitorException {
		// technically this method should simply do nothing, since
		// putting a job onto a node should cause Ganglia to detect
		// for now we'll simply track what the current perceived load
		// on a node is - we may want to factor this into the weighting
		// in load calculator later
		String nodeId = node.getNodeId();
		if (loadMap.containsKey(nodeId)) {
			int currLoad = loadMap.get(nodeId);
			currLoad += loadValue;
			loadMap.put(nodeId, currLoad);
		} else {
			loadMap.put(nodeId, loadValue);
		}
		return true;
	}

	@Override
	public void addNode(ResourceNode node) {
		this.addGmetadNode(node.getIpAddr().getHost(), node.getIpAddr()
				.getPort());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNodeById(String nodeId) {
		this.removeGmetadNode(nodeId);
	}

	@Override
	public List getNodes() throws MonitorException {
		List<ResourceNode> nodes = new Vector<ResourceNode>();
		if (this.gmetaAdapters != null) {
			for (GangliaAdapter adapter : this.gmetaAdapters.values()) {
				Map<String, Map<String, String>> aNodes = adapter
						.getResourceNodeStatus();
				for (Map<String, String> map : aNodes.values()) {
					try {
						nodes.add(this.nodeFromMap(map));
					} catch (MalformedURLException e) {
						LOG.log(Level.SEVERE, e.getMessage());
						throw new MonitorException(e.getMessage());
					}
				}
			}
		}

		return nodes;
	}

	@Override
	public ResourceNode getNodeById(String nodeId) throws MonitorException {
		try {
			return this.nodeFromMap(this.locateNode(nodeId));
		} catch (MalformedURLException e) {
			LOG.log(Level.SEVERE, e.getMessage());
			throw new MonitorException(e.getMessage());
		}
	}

	@Override
	public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
		if (this.gmetaAdapters != null) {
			for (GangliaAdapter adapter : this.gmetaAdapters.values()) {
				Map<String, Map<String, String>> aNodes = adapter
						.getResourceNodeStatus();
				for (Map.Entry<String, Map<String, String>> aNodeId : aNodes.entrySet()) {
					String host = ipAddr.getHost();
					int port = ipAddr.getPort();
					Map<String, String> nodeProps = aNodeId.getValue();
					if (aNodeId.getKey().equals(host)
							&& nodeProps.get(DEFAULT_PORT).equals(
									String.valueOf(port))) {
						try {
							return this.nodeFromMap(aNodeId.getValue());
						} catch (MalformedURLException e) {
							LOG.log(Level.SEVERE, e.getMessage());
							throw new MonitorException(e.getMessage());
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public boolean reduceLoad(ResourceNode node, int loadValue)
			throws MonitorException {
		String nodeId = node.getNodeId();
		if (this.loadMap.containsKey(nodeId)) {
			int currLoad = loadMap.get(nodeId);
			currLoad = Math.min(0, currLoad - loadValue);
			this.loadMap.put(nodeId, currLoad);
		} else {
			this.loadMap.put(nodeId, 0);
		}
		
		return true;
	}

	private Map<String, String> locateNode(String nodeId) {
		if (this.gmetaAdapters != null && this.gmetaAdapters.size() > 0) {
			for (Map.Entry<String, GangliaAdapter> nId : this.gmetaAdapters.entrySet()) {
				GangliaAdapter adapter = nId.getValue();
				try {
					System.out.println("Querying gmetad: ["+adapter.getUrlString()+"]");
					Map<String, Map<String, String>> nodeStatus = adapter
							.getResourceNodeStatus();
					System.out.println("Looking for nodeid: ["+nodeId+"]");
					if (nodeStatus.containsKey(nodeId)) {
						System.out.println("NODE met: "+nodeStatus.get(nodeId));
						return nodeStatus.get(nodeId);
					}
				} catch (MonitorException e) {
					LOG.log(Level.WARNING,
							"MonitorException contacting Ganglia: ["
									+ adapter.getUrlString() + "]");
					LOG.log(Level.SEVERE, e.getMessage());
				}
			}

		}

		return null;
	}

	private ResourceNode nodeFromMap(Map<String, String> map)
			throws MalformedURLException {
		if (map == null) {
		  return null;
		}
		ResourceNode node = new ResourceNode();
		System.out.println("MAP IS "+map);
		System.out.println("Setting hostname to "+map.get(NAME));
		node.setId(map.get(NAME));
		node.setIpAddr(new URL("http://" + map.get(NAME) + ":" + DEFAULT_PORT));
		return node;
	}

	private void initGmetaNodes(String host, int port) {
		this.addGmetadNode(host, port);
	}

	private GangliaAdapter createAdapter(Map<String, String> node) {
		return new GangliaAdapter(node.get("host"), Integer.valueOf(node
				.get("port")));
	}

	private void addGmetadNode(String host, int port) {
		Map<String, String> rootNode = new ConcurrentHashMap<String, String>();
		rootNode.put("host", host);
		rootNode.put("port", String.valueOf(port));
		this.gmetaNodes.put(host, rootNode);
		this.gmetaAdapters.put(host, this.createAdapter(rootNode));
	}

	private void removeGmetadNode(String host) {
		if (this.gmetaNodes.containsKey(host)
				&& this.gmetaAdapters.containsKey(host)) {
			LOG.log(Level.FINE,
					"Removing gmetad node: ["
							+ gmetaAdapters.get(host).getUrlString() + "]");
			this.gmetaAdapters.remove(host);
			this.gmetaNodes.remove(host);
		}
	}

}
