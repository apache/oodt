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


package org.apache.oodt.cas.metadata.extractors;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

//JDK imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Reader that reads {@link ExternalMetExtractorConfig}s from an XML file.
 * </p>.
 */
public final class ExternConfigReader implements MetExtractorConfigReader,
        ExternMetExtractorMetKeys, ExternConfigReaderMetKeys {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractorConfigReader#parseConfigFile(java.io.File)
     */
    public MetExtractorConfig parseConfigFile(File file)
            throws MetExtractorConfigReaderException {
        try {
            Document doc = XMLUtils.getDocumentRoot(new FileInputStream(file));

            ExternalMetExtractorConfig config = new ExternalMetExtractorConfig();
            Element docElem = doc.getDocumentElement();
            Element execElement = XMLUtils.getFirstElement(EXEC_TAG, docElem);
            config.setWorkingDirPath(PathUtils.replaceEnvVariables(execElement
                    .getAttribute(WORKING_DIR_ATTR)));
            String metFileExt = PathUtils.replaceEnvVariables(execElement
                    .getAttribute(MET_FILE_EXT_ATTR));
            if (!metFileExt.equals("")) {
              config.setMetFileExt(metFileExt);
            }
            Element binPathElem = XMLUtils.getFirstElement(
                    EXTRACTOR_BIN_PATH_TAG, execElement);
            String binPath = XMLUtils.getSimpleElementText(binPathElem);
            if (Boolean.valueOf(binPathElem.getAttribute(ENV_REPLACE_ATTR))) {
                binPath = PathUtils.replaceEnvVariables(binPath);
            }

            // make sure to do path replacement on binPath because it's always
            // going
            // to be
            // a path
            binPath = binPath.replaceAll("^\\s+", "").replaceAll("\\s+$", "").replaceAll("\\s", "\\\\ ");

            config.setExtractorBinPath(binPath);

            Element argsElem = XMLUtils.getFirstElement(ARGS_TAG, execElement);
            if (argsElem != null) {
                NodeList argNodes = argsElem.getElementsByTagName(ARG_TAG);

                if (argNodes != null && argNodes.getLength() > 0) {
                    Vector argVector = new Vector();
                    for (int i = 0; i < argNodes.getLength(); i++) {
                        Element argElem = (Element) argNodes.item(i);
                        String argStr;
                        if (Boolean.valueOf(
                            argElem.getAttribute(IS_DATA_FILE_ATTR)
                                   .toLowerCase())) {
                          argStr = DATA_FILE_PLACE_HOLDER;
                        } else if (Boolean.valueOf(
                            argElem.getAttribute(IS_MET_FILE_ATTR)
                                   .toLowerCase())) {
                          argStr = MET_FILE_PLACE_HOLDER;
                        } else {
                          argStr = XMLUtils.getSimpleElementText(argElem);
                        }

                        String appendExt;
                        if (!(appendExt = argElem.getAttribute(APPEND_EXT_ATTR))
                                .equals("")) {
                          argStr += "." + appendExt;
                        }

                        if (Boolean.valueOf(
                            argElem.getAttribute(ENV_REPLACE_ATTR))) {
                            argStr = PathUtils.replaceEnvVariables(argStr);
                        }

                        if (Boolean.valueOf(argElem.getAttribute(IS_PATH_ATTR))) {
                            argStr = argStr.replaceAll("\\s", "\\\\ ");
                        }

                        argVector.add(argStr);
                    }

                    config.setArgList((String[]) argVector
                        .toArray(new String[argVector.size()]));
                }
            }

            return config;
        } catch (Exception e) {
            throw new MetExtractorConfigReaderException("Failed to parser '"
                    + file + "' : " + e.getMessage());
        }

    }

}
