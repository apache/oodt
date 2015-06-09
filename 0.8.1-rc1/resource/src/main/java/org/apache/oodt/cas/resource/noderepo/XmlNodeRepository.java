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

package org.apache.oodt.cas.resource.noderepo;

//OODT imports
import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.util.XmlStructFactory;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//DOM imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The XML Node Repository interface.
 * </p>
 */
public class XmlNodeRepository implements NodeRepository {

	private static final Logger LOG = Logger.getLogger(XmlNodeRepository.class
			.getName());

	private static FileFilter nodesXmlFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isFile()
					&& pathname.toString().endsWith("nodes.xml");
		}
	};

	private List<String> nodesHomeUris = null;

	public XmlNodeRepository(List<String> uris) {
		this.nodesHomeUris = uris;
	}

	public List<ResourceNode> loadNodes() {
		Vector<ResourceNode> nodes = new Vector<ResourceNode>();
		for (String dirUri : this.nodesHomeUris) {
			try {
				File nodesDir = new File(new URI(dirUri));
				if (nodesDir.isDirectory()) {

					String nodesDirStr = nodesDir.getAbsolutePath();

					if (!nodesDirStr.endsWith("/")) {
						nodesDirStr += "/";
					}

					// get all the workflow xml files
					File[] nodesFiles = nodesDir.listFiles(nodesXmlFilter);

					for (int j = 0; j < nodesFiles.length; j++) {

						String nodesXmlFile = nodesFiles[j].getAbsolutePath();
						Document nodesRoot = null;
						try {
							nodesRoot = XMLUtils
									.getDocumentRoot(new FileInputStream(
											nodesFiles[j]));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							return null;
						}

						NodeList nodeList = nodesRoot
								.getElementsByTagName("node");
						if (nodeList != null)
							for (int k = 0; k < nodeList.getLength(); k++)
								nodes.add(XmlStructFactory
										.getNodes((Element) nodeList.item(k)));
					}
				}
			} catch (URISyntaxException e) {
				LOG.log(Level.WARNING, "DirUri: " + dirUri
						+ " is not a directory: skipping node loading for it.",
						e);
			}
		}
		return nodes;
	}

}
