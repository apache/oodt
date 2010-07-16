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

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ConfigException;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.retrievalmethod.RetrievalMethod;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class ParserInfo implements ConfigParserMetKeys{

    private HashMap<String, String> parserToRetrievalMethodMap;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(ParserInfo.class
            .getName());

    public ParserInfo() {
        parserToRetrievalMethodMap = new HashMap<String, String>();
    }

    public void loadParserInfo(File xmlFile) throws ConfigException {
        try {
            NodeList rmList = XMLUtils.getDocumentRoot(new FileInputStream(xmlFile))
                    .getElementsByTagName(RETRIEVAL_METHOD_TAG);
            for (int i = 0; i < rmList.getLength(); i++) {

                // get rm element
                Node rmNode = rmList.item(i);

                // get classpath for this rm
                String rmClasspath = PathUtils
                        .replaceEnvVariables(((Element) rmNode)
                                .getAttribute(CLASS_ATTR));

                // get all login info for this source
                NodeList parserList = ((Element) rmNode)
                        .getElementsByTagName(PARSER_TAG);
                for (int j = 0; j < parserList.getLength(); j++) {

                    // get a single login info
                    Node parserNode = parserList.item(j);
                    String parserClasspath = PathUtils
                            .replaceEnvVariables(((Element) parserNode)
                                    .getAttribute(CLASS_ATTR));

                    LOG.log(Level.INFO, "Assiging parser '" + parserClasspath
                            + "' with retrievalmethod '" + rmClasspath + "'");
                    this.parserToRetrievalMethodMap.put(parserClasspath,
                            rmClasspath);
                }
            }
        } catch (Exception e) {
            throw new ConfigException("Failed to load Parser info : "
                    + e.getMessage());
        }
    }

    public Class<RetrievalMethod> getRetrievalMethod(Parser parser)
            throws ClassNotFoundException {
        System.out.println(parser.getClass().getCanonicalName());
        return (Class<RetrievalMethod>) Class
                .forName(this.parserToRetrievalMethodMap.get(parser.getClass()
                        .getCanonicalName()));
    }

}
