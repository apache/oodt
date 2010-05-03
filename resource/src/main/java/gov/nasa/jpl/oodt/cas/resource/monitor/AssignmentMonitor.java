//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.monitor;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.cas.resource.util.XmlStructFactory;
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.MonitorException;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An implementation of the {@link Monitor} interface that loads its information
 * about the underlying nodes from an XML file called <code>nodes.xml</code>.
 * This implementation additionally uses an in-memory hash map to monitor the
 * load on a given set of {@link ResourceNode}s.
 * </p>
 */
public class AssignmentMonitor implements Monitor {

    private List nodesHomeUris = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(AssignmentMonitor.class
            .getName());

    /* our nodes map */
    private static HashMap nodesMap = new HashMap();

    /* our load map */
    private static HashMap loadMap = new HashMap();

    private static FileFilter nodesXmlFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isFile()
                    && pathname.toString().endsWith("nodes.xml");
        }
    };

    public AssignmentMonitor(List uris) {
        nodesHomeUris = uris;
        nodesMap = loadNodeInfo(nodesHomeUris);

        loadMap = new HashMap();

        Set nodeIds = nodesMap.keySet();
        Iterator It = nodeIds.iterator();
        while (It.hasNext()) {
            String node = (String) It.next();
            loadMap.put(node, new Integer(0));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#assignLoad(
     *      gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode, int)
     */
    public boolean assignLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        // ResourceNode resource = (ResourceNode)
        // nodesMap.get(node.getNodeId());
        int loadCap = node.getCapacity();
        int curLoad = ((Integer) loadMap.get(node.getNodeId())).intValue();

        if (loadValue <= (loadCap - curLoad)) {
            loadMap.remove(node.getNodeId());
            loadMap.put(node.getNodeId(), new Integer(curLoad + loadValue));
            return true;
        } else {
            return false;
        }

    }

    public boolean reduceLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        Integer load = (Integer) loadMap.get(node.getNodeId());
        int newVal = load.intValue() - loadValue;
        if (newVal < 0)
            newVal = 0; // should not happen but just in case
        loadMap.remove(node.getNodeId());
        loadMap.put(node.getNodeId(), new Integer(newVal));
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getLoad(gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode)
     */
    public int getLoad(ResourceNode node) throws MonitorException {
        ResourceNode resource = (ResourceNode) nodesMap.get(node.getNodeId());
        Integer i = (Integer) loadMap.get(node.getNodeId());
        return (resource.getCapacity() - i.intValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodes()
     */
    public List getNodes() throws MonitorException {
        return Arrays.asList(nodesMap.values().toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodeById(java.lang.String)
     */
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        return (ResourceNode) nodesMap.get(nodeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodeByURL(java.net.URL)
     */
    public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
        ResourceNode targetResource = null;
        Vector nodes = (Vector) this.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (((ResourceNode) nodes.get(i)).getIpAddr() == ipAddr) {
                targetResource = (ResourceNode) nodes.get(i);
                break;
            }
        }
        return targetResource;
    }

    private HashMap loadNodeInfo(List dirUris) {

        HashMap resources = new HashMap();

        if (dirUris != null && dirUris.size() > 0) {
            for (Iterator i = dirUris.iterator(); i.hasNext();) {
                String dirUri = (String) i.next();

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

                            String nodesXmlFile = nodesFiles[j]
                                    .getAbsolutePath();
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

                            if (nodeList != null && nodeList.getLength() > 0) {
                                for (int k = 0; k < nodeList.getLength(); k++) {
                                    ResourceNode resource = XmlStructFactory
                                            .getNodes((Element) nodeList
                                                    .item(k));
                                    resources.put(resource.getNodeId(),
                                            resource);
                                }
                            }
                        }
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOG
                            .log(
                                    Level.WARNING,
                                    "DirUri: "
                                            + dirUri
                                            + " is not a directory: skipping node loading for it.");
                }
            }
        }

        return resources;
    }

}