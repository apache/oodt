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
package org.apache.oodt.cas.resource.mux;

import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to load BackendManager from XML file.
 * @author starchmd
 */
public class XmlBackendRepository implements BackendRepository {

    private static final Logger LOG = Logger.getLogger(XmlBackendRepository.class.getName());
    private String uri;

    //Constants
    private static final String SCHEDULER = "scheduler";
    private static final String BATCHMGR = "batchmgr";
    private static final String MONITOR = "monitor";

    private static final String MONITOR_PROPERTY = "resource.monitor.factory";
    private static final String BATCHMGR_PROPERTY = "resource.batchmgr.factory";

    /**
     * Ctor
     * @param uri - uri of XML file containing mapping
     */
    public XmlBackendRepository(String uri) {
        if (uri == null) {
            throw new NullPointerException("URI for queue-to-backend xml file cannot be null");
        }
        this.uri = uri;
    }
    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.mux.BackendRepository#load()
     */
    @Override
    public BackendManager load() throws RepositoryException {
        LOG.log(Level.INFO,"Reading backend set manager from: "+this.uri);
        BackendManager bm = new StandardBackendManager();
        String origMon = System.getProperty(MONITOR_PROPERTY);
        String origBat = System.getProperty(BATCHMGR_PROPERTY);
        try {
            File file = new File(new URI(this.uri));
            Document root = XMLUtils.getDocumentRoot(new FileInputStream(file));
            NodeList list = root.getElementsByTagName("queue");
            if (list != null && list.getLength() > 0) {
                for (int k = 0; k < list.getLength(); k++) {
                    Element node = (Element)list.item(k);
                    String queue = node.getAttribute("name");
                    //Set properties for batch and monitor factories
                    //So scheduler builds as repository specifies
                    try {
                        String mfact = getMonitor(queue,node);
                        LOG.log(Level.INFO,"Setting monitor factory property to: "+mfact);
                        System.setProperty(MONITOR_PROPERTY, mfact);
                    } catch (RepositoryException e) {
                        LOG.log(Level.INFO, "No monitor factory for queue "+queue+", using system property.");
                    }
                    try {
                        String bfact = getBatchmgr(queue,node);
                        LOG.log(Level.INFO,"Setting batchmgr factory property to: "+bfact);
                        System.setProperty(BATCHMGR_PROPERTY, bfact);
                    } catch (RepositoryException e) {
                        LOG.log(Level.INFO, "No batchmgr factory for queue "+queue+", using system property.");
                    }
                    //Build scheduler
                    Scheduler sch = getScheduler(queue,node);
                    bm.addSet(queue, sch.getMonitor(), sch.getBatchmgr(), sch);
                    //Reset Properties for next item
                    resetAlteredProperty(MONITOR_PROPERTY,origMon);
                    resetAlteredProperty(BATCHMGR_PROPERTY,origBat);
                }
            }
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE,"Malformed URI: "+this.uri);
            throw new RepositoryException(e);
        } catch(FileNotFoundException e) {
            LOG.log(Level.SEVERE,"File not found: "+this.uri+" from working dir: "+new File(".").getAbsolutePath());
            throw new RepositoryException(e);
        } catch (ClassCastException e) {
            LOG.log(Level.SEVERE,"Queue tag must represent XML element.");
            throw new RepositoryException(e);
        } finally {
            resetAlteredProperty(MONITOR_PROPERTY,origMon);
            resetAlteredProperty(BATCHMGR_PROPERTY,origBat);
        }

        return bm;
    }
    /**
     * Resets a property. Allows nulls
     * @param prop - property name to reset
     * @param value - value to reset to, can be null
     */
    private static void resetAlteredProperty(String prop,String value) {
        if (value == null) {
            System.clearProperty(prop);
            return;
        }
        System.setProperty(prop,value);
    }

    /**
     * Get monitor factory from XML
     * @param queue - current queue, for error reporting
     * @param node - node that is being read
     * @return monitor factory string
     * @throws RepositoryException
     */
    private static String getMonitor(String queue,Element node) throws RepositoryException {
        return getFactoryAttribute(queue, node, MONITOR);
    }
    /**
     * Get scheduler from XML
     * @param queue - current queue, for error reporting
     * @param node - node that is being read
     * @return newly constructed Scheduler
     * @throws RepositoryException
     */
    private static Scheduler getScheduler(String queue,Element node) throws RepositoryException {
        String factory = getFactoryAttribute(queue, node, SCHEDULER);
        LOG.log(Level.INFO,"Loading monitor from: "+factory);
        Scheduler sch = GenericResourceManagerObjectFactory.getSchedulerServiceFromFactory(factory);
        if (sch != null) {
            return sch;
        }
        throw new RepositoryException("Could instantiate from: "+factory);
    }
    /**
     * Get batchmgr factory from XML
     * @param queue - current queue, for error reporting
     * @param node - node that is being read
     * @return batch manager factory name
     * @throws RepositoryException
     */
    private static String getBatchmgr(String queue,Element node) throws RepositoryException {
        return getFactoryAttribute(queue, node, BATCHMGR);
    }
    /**
     * Pull out the factory attribute from tag with given name.
     * @param queue - current queue, for error reporting
     * @param elem - element that contains tags as children
     * @param tag - string name of tag looked for. i.e. "monitor"
     * @return name of factory class
     * @throws RepositoryException - thrown if more than one child matches, no children match, or other error
     */
    private static String getFactoryAttribute(String queue,Element elem, String tag) throws RepositoryException {
        NodeList children = elem.getElementsByTagName(tag);
        try {
            String attr;
            if (children.getLength() != 1 || (attr = ((Element) children.item(0)).getAttribute("factory")).equals("")) {
                throw new RepositoryException("Could not find exactly one "+tag+", with factory set, in queue: "+queue);
            }
            return attr;
        } catch (ClassCastException e) {
            throw new RepositoryException("Tag "+tag+" does not represent XML element in queue: "+queue,e);
        }
    }
}
