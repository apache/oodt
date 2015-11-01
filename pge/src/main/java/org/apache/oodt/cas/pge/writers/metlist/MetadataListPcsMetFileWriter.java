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


package org.apache.oodt.cas.pge.writers.metlist;

//JDK imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.CasMetadataException;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.writers.PcsMetFileWriter;
import org.apache.oodt.commons.exceptions.CommonsException;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Arrays;

import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.*;

//OODT imports

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A {@link PcsMetFileWriter} that generates PCS met files based on a MetList
 * XML document
 * </p>.
 */
public class MetadataListPcsMetFileWriter extends PcsMetFileWriter {

    @Override
    protected Metadata getSciPgeSpecificMetadata(File sciPgeCreatedDataFile,
            Metadata inputMetadata, Object... customArgs)
        throws FileNotFoundException, ParseException, CommonsException, CasMetadataException {
        Metadata metadata = new Metadata();
        for (Object arg : customArgs) {
            Element root = XMLUtils.getDocumentRoot(
                    new FileInputStream(new File((String) arg)))
                    .getDocumentElement();
            NodeList metadataNodeList = root.getElementsByTagName(METADATA_TAG);
            for (int i = 0; i < metadataNodeList.getLength(); i++) {
                Element metadataElement = (Element) metadataNodeList.item(i);
                String key = metadataElement.getAttribute(KEY_ATTR);
                if (key.equals("")) {
                  key = PathUtils.doDynamicReplacement(metadataElement.getAttribute(KEY_GEN_ATTR), inputMetadata);
                }
                String val = metadataElement.getAttribute(VAL_ATTR);
            	if (val.equals("")) {
                  val = metadataElement.getTextContent();
                }
                if (val != null && !val.equals("")) {
                    if (!metadataElement.getAttribute(ENV_REPLACE_ATTR).toLowerCase().equals("false")) {
                      val = PathUtils.doDynamicReplacement(val, inputMetadata);
                    }
                    String[] vals;
                    if (metadataElement.getAttribute(SPLIT_ATTR).toLowerCase().equals("false")) {
                        vals = new String[] { val };
                    } else {
                        String delimiter = metadataElement.getAttribute("delimiter");
                        if (delimiter == null || delimiter.equals("")) {
                          delimiter = ",";
                        }
                        vals = (val + delimiter).split(delimiter);
                    }
                    metadata.replaceMetadata(key, Arrays.asList(vals));
                    inputMetadata.replaceMetadata(key, Arrays.asList(vals));
                } else if (inputMetadata.getMetadata(key) != null
                        && !inputMetadata.getMetadata(key).equals("")) {
                    metadata.replaceMetadata(key, inputMetadata
                            .getAllMetadata(key));
                }
            }
        }
        return metadata;
    }

}
