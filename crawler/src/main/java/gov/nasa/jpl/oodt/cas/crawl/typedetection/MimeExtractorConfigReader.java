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


package gov.nasa.jpl.oodt.cas.crawl.typedetection;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;

//JDK imports
import java.io.FileInputStream;
import java.util.LinkedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Static reader class for {@link MimeExtractor}s.
 * </p>.
 */
public final class MimeExtractorConfigReader implements
        MimeExtractorConfigMetKeys {

    private MimeExtractorConfigReader() throws InstantiationException {
        throw new InstantiationException("Don't construct reader classes!");
    }

    public static MimeExtractorRepo read(String mapFilePath) throws Exception {
        try {
            Document doc = XMLUtils.getDocumentRoot(new FileInputStream(
                    mapFilePath));
            Element root = doc.getDocumentElement();

            MimeExtractorRepo extractorRepo = new MimeExtractorRepo();
            extractorRepo.setMagic(Boolean.valueOf(
                    root.getAttribute(MAGIC_ATTR)).booleanValue());
            extractorRepo.setMimeRepoFile(PathUtils.replaceEnvVariables(root
                    .getAttribute(MIME_REPO_ATTR)));

            Element defaultExtractorElem = XMLUtils.getFirstElement(
                    DEFAULT_EXTRACTOR_TAG, root);
            if (defaultExtractorElem != null) {
                NodeList defaultExtractorElems = defaultExtractorElem
                        .getElementsByTagName(EXTRACTOR_TAG);
                LinkedList<MetExtractorSpec> defaultExtractorSpecs = new LinkedList<MetExtractorSpec>();
                for (int i = 0; i < defaultExtractorElems.getLength(); i++) {
                    Element extractorElem = (Element) defaultExtractorElems
                            .item(i);
                    NodeList preCondComparators = XMLUtils.getFirstElement(
                            PRECONDITION_COMPARATORS_TAG, extractorElem)
                            .getElementsByTagName(PRECONDITION_COMPARATOR_TAG);
                    LinkedList<String> preCondComparatorIds = new LinkedList<String>();
                    for (int k = 0; k < preCondComparators.getLength(); k++)
                        preCondComparatorIds.add(((Element) preCondComparators
                                .item(k)).getAttribute(ID_ATTR));
                    defaultExtractorSpecs
                            .add(new MetExtractorSpec(extractorElem
                                    .getAttribute(EXTRACTOR_CLASS_TAG),
                                    getFilePathFromElement(extractorElem,
                                            EXTRACTOR_CONFIG_TAG),
                                    preCondComparatorIds));
                }
                extractorRepo
                        .setDefaultMetExtractorSpecs(defaultExtractorSpecs);
            }

            NodeList mimeElems = root.getElementsByTagName(MIME_TAG);
            for (int i = 0; i < mimeElems.getLength(); i++) {
                Element mimeElem = (Element) mimeElems.item(i);
                String mimeType = mimeElem.getAttribute(MIME_TYPE_ATTR);
                LinkedList<MetExtractorSpec> specs = new LinkedList<MetExtractorSpec>();

                NodeList extractorSpecElems = mimeElem
                        .getElementsByTagName(EXTRACTOR_TAG);
                if (extractorSpecElems != null
                        && extractorSpecElems.getLength() > 0) {
                    for (int j = 0; j < extractorSpecElems.getLength(); j++) {
                        Element extractorSpecElem = (Element) extractorSpecElems
                                .item(j);
                        MetExtractorSpec spec = new MetExtractorSpec();
                        spec.setMetExtractor(extractorSpecElem
                                .getAttribute(CLASS_ATTR));

                        // get config file if specified
                        String configFilePath = getFilePathFromElement(
                                extractorSpecElem, EXTRACTOR_CONFIG_TAG);
                        if (configFilePath != null)
                            spec.setExtractorConfigFile(configFilePath);

                        // get preconditions file if specified
                        NodeList preCondComparators = XMLUtils
                                .getFirstElement(PRECONDITION_COMPARATORS_TAG,
                                        extractorSpecElem)
                                .getElementsByTagName(
                                        PRECONDITION_COMPARATOR_TAG);
                        LinkedList<String> preCondComparatorIds = new LinkedList<String>();
                        for (int k = 0; k < preCondComparators.getLength(); k++)
                            preCondComparatorIds
                                    .add(((Element) preCondComparators.item(k))
                                            .getAttribute(ID_ATTR));
                        spec.setPreConditionComparatorIds(preCondComparatorIds);

                        specs.add(spec);
                    }
                }
                extractorRepo.addMetExtractorSpecs(mimeType, specs);
            }
            return extractorRepo;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String getFilePathFromElement(Element root, String elemName) {
        String filePath = null;
        Element elem = XMLUtils.getFirstElement(elemName, root);
        if (elem != null) {
            filePath = elem.getAttribute(FILE_ATTR);
            if (Boolean.valueOf(elem.getAttribute(ENV_REPLACE_ATTR))
                    .booleanValue())
                filePath = PathUtils.replaceEnvVariables(filePath);
        }
        return filePath;
    }
}
