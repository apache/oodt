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


package org.apache.oodt.cas.pushpull.config;

//OODT imports
import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.apache.oodt.cas.pushpull.exceptions.ConfigException;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class ProtocolInfo implements ConfigParserMetKeys {

    private long timeout;

    private int pgSize;

    private static final int DEFAULT_PG_SIZE = 8;

    private HashMap<String, LinkedList<Class<ProtocolFactory>>> protocolClassReference;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(ProtocolInfo.class
            .getName());

    public ProtocolInfo() {
        timeout = 0;
        pgSize = DEFAULT_PG_SIZE;
        protocolClassReference = new HashMap<String, LinkedList<Class<ProtocolFactory>>>();
    }

    public void loadProtocolFactoryInfoFromFile(File protocolFactoryInfo)
            throws ConfigException {
        try {
            NodeList sourceList = XMLUtils.getDocumentRoot(new FileInputStream(protocolFactoryInfo))
                    .getElementsByTagName(PROTOCOL_TAG);
            for (int i = 0; i < sourceList.getLength(); i++) {

                // get source element
                Node sourceNode = sourceList.item(i);

                // get host of this source
                String type = PathUtils
                        .replaceEnvVariables(((Element) sourceNode)
                                .getAttribute(TYPE_ATTR));

                // get all login info for this source
                NodeList loginList = ((Element) sourceNode)
                        .getElementsByTagName(PROTOCOL_FACTORY_TAG);
                for (int j = 0; j < loginList.getLength(); j++) {

                    // get a single login info
                    Node loginNode = loginList.item(j);
                    String protocolFactoryClass = PathUtils
                            .replaceEnvVariables(((Element) loginNode)
                                    .getAttribute(CLASS_ATTR));

                    this.addClassForType(type, protocolFactoryClass);
                }
            }
        } catch (Exception e) {
            throw new ConfigException(
                    "Failed to load ProtocolFactory info for protocol types : "
                            + e.getMessage());
        }
    }

    public void addClassForType(String type,
            Class<ProtocolFactory> protocolFactoryClass) {
        LinkedList<Class<ProtocolFactory>> protocolClasses;
        if ((protocolClasses = protocolClassReference.get(type)) == null) {
            protocolClasses = new LinkedList<Class<ProtocolFactory>>();
            protocolClassReference.put(type.toLowerCase(), protocolClasses);
        }
        protocolClasses.add(protocolFactoryClass);
        LOG.log(Level.INFO, "Assiging protocol '" + type.toLowerCase()
                + "' with class '" + protocolFactoryClass + "'");
    }

    public void addClassForType(String type, String protocolFactoryClass)
            throws ClassNotFoundException {
        this.addClassForType(type, (Class<ProtocolFactory>) Class
                .forName(protocolFactoryClass));
    }

    public void addClassForType(String type,
            Class<ProtocolFactory>[] protocolFactoryClasses) {
        for (Class<ProtocolFactory> clazz : protocolFactoryClasses) {
            this.addClassForType(type, clazz);
        }
    }

    public void addClassForType(String type, String[] protocolFactoryClasses)
            throws ClassNotFoundException {
        for (String clazz : protocolFactoryClasses) {
            this.addClassForType(type, (Class<ProtocolFactory>) Class
                    .forName(clazz));
        }
    }

    public void setDownloadTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setPageSize(int pgSize) {
        if (pgSize != -1)
            this.pgSize = pgSize;
        else
            this.pgSize = Integer.MAX_VALUE;
    }

    public LinkedList<Class<ProtocolFactory>> getProtocolClassesForProtocolType(
            String type) {
        return protocolClassReference.get(type.toLowerCase());
    }

    public long getDownloadTimeout() {
        return timeout;
    }

    public int getPageSize() {
        return pgSize;
    }

    public ProtocolInfo clone() {
        ProtocolInfo pi = new ProtocolInfo();
        pi.protocolClassReference = this.protocolClassReference;
        pi.timeout = this.timeout;
        pi.pgSize = this.pgSize;
        return pi;
    }
}
