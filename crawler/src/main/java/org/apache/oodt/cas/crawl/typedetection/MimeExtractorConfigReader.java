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
package org.apache.oodt.cas.crawl.typedetection;

import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

import com.google.common.base.Strings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports
//W3C imports
//Google imports

/**
 * Static reader class for {@link MimeExtractor}s.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public final class MimeExtractorConfigReader implements
        MimeExtractorConfigMetKeys {
  private static Logger LOG = Logger.getLogger(MimeExtractorConfigReader.class.getName());
    private MimeExtractorConfigReader() throws InstantiationException {
        throw new InstantiationException("Don't construct reader classes!");
    }

    public static MimeExtractorRepo read(String mapFilePath)
        throws ClassNotFoundException, FileNotFoundException, MetExtractionException, InstantiationException,
        IllegalAccessException, CrawlerActionException {
        try {
            Document doc = XMLUtils.getDocumentRoot(new FileInputStream(
                    mapFilePath));
            Element root = doc.getDocumentElement();

            MimeExtractorRepo extractorRepo = new MimeExtractorRepo();
            extractorRepo.setMagic(Boolean.valueOf(
                root.getAttribute(MAGIC_ATTR)));
            String mimeTypeFile = PathUtils.replaceEnvVariables(root
                  .getAttribute(MIME_REPO_ATTR));
            if (!mimeTypeFile.startsWith("/")) {
               mimeTypeFile = new File(new File(mapFilePath).getParentFile(),
                     mimeTypeFile).getAbsolutePath();
            }
            extractorRepo.setMimeRepoFile(mimeTypeFile);

            Element defaultExtractorElem = XMLUtils.getFirstElement(
                    DEFAULT_EXTRACTOR_TAG, root);
            if (defaultExtractorElem != null) {
                NodeList defaultExtractorElems = defaultExtractorElem
                        .getElementsByTagName(EXTRACTOR_TAG);
                LinkedList<MetExtractorSpec> defaultExtractorSpecs = new LinkedList<MetExtractorSpec>();
                for (int i = 0; i < defaultExtractorElems.getLength(); i++) {
                    Element extractorElem = (Element) defaultExtractorElems
                            .item(i);
                    Element preCondsElem = XMLUtils.getFirstElement(
                          PRECONDITION_COMPARATORS_TAG, extractorElem);
                    LinkedList<String> preCondComparatorIds = new LinkedList<String>();
                    if (preCondsElem != null) {
                       NodeList preCondComparators = 
                          preCondsElem.getElementsByTagName(PRECONDITION_COMPARATOR_TAG);
                       for (int k = 0; k < preCondComparators.getLength(); k++) {
                         preCondComparatorIds.add(((Element) preCondComparators
                             .item(k)).getAttribute(ID_ATTR));
                       }
                    }
                    // This seems wrong, so added support for CLASS_ATTR while still
                    //  supporting EXTRACTOR_CLASS_TAG as an attribute for specifying
                    //  extractor class.
                    String extractorClass = extractorElem.getAttribute(CLASS_ATTR);
                    if (Strings.isNullOrEmpty(extractorClass)) {
                       extractorClass = extractorElem.getAttribute(EXTRACTOR_CLASS_TAG);
                    }
                    String extractorConfigFile = getFilePathFromElement(
                          extractorElem, EXTRACTOR_CONFIG_TAG);
                    if (extractorConfigFile != null && !extractorConfigFile.startsWith("/")) {
                       extractorConfigFile = new File(new File(mapFilePath).getParentFile(),
                             extractorConfigFile).getAbsolutePath();
                    }
                    defaultExtractorSpecs
                            .add(new MetExtractorSpec(extractorClass,
                                    extractorConfigFile,
                                    preCondComparatorIds));
                }
                extractorRepo
                        .setDefaultMetExtractorSpecs(defaultExtractorSpecs);
                extractorRepo.setDefaultNamingConventionId(
                      getNamingConventionId(defaultExtractorElem));
            }

            NodeList mimeElems = root.getElementsByTagName(MIME_TAG);
            for (int i = 0; i < mimeElems.getLength(); i++) {
                Element mimeElem = (Element) mimeElems.item(i);
                String mimeType = mimeElem.getAttribute(MIME_TYPE_ATTR);
                LinkedList<MetExtractorSpec> specs = new LinkedList<MetExtractorSpec>();

                // Load naming convention class.
                extractorRepo.setNamingConventionId(mimeType,
                      getNamingConventionId(mimeElem));

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
                        if (configFilePath != null) {
                           if (!configFilePath.startsWith("/")) {
                              configFilePath = new File(new File(mapFilePath).getParentFile(),
                                    configFilePath).getAbsolutePath();
                           }
                           spec.setExtractorConfigFile(configFilePath);
                        }

                        // get preconditions file if specified
                        Element preCondsElem = XMLUtils.getFirstElement(
                              PRECONDITION_COMPARATORS_TAG, extractorSpecElem);
                        if (preCondsElem != null) {
                           NodeList preCondComparators = preCondsElem
                                 .getElementsByTagName(PRECONDITION_COMPARATOR_TAG);
                           LinkedList<String> preCondComparatorIds = new LinkedList<String>();
                           for (int k = 0; k < preCondComparators.getLength(); k++) {
                             preCondComparatorIds
                                 .add(((Element) preCondComparators.item(k))
                                     .getAttribute(ID_ATTR));
                           }
                           spec.setPreConditionComparatorIds(preCondComparatorIds);
                        }

                        specs.add(spec);
                    }
                }
                extractorRepo.addMetExtractorSpecs(mimeType, specs);
            }
            return extractorRepo;
        } catch (IllegalAccessException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw e;
        } catch (InstantiationException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw e;
        } catch (MetExtractionException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw e;
        } catch (FileNotFoundException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw e;
        } catch (ClassNotFoundException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw e;
        } catch (CrawlerActionException e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw e;
        }
    }

    private static String getNamingConventionId(Element parent) throws CrawlerActionException {
       NodeList namingConventions = parent
             .getElementsByTagName(NAMING_CONVENTION_TAG);
       if (namingConventions != null && namingConventions.getLength() > 0) {
          if (namingConventions.getLength() > 1) {
             throw new CrawlerActionException("Can only have 1 '"
                   + NAMING_CONVENTION_TAG + "' tag per mimetype");
          }
          Element namingConvention = (Element) namingConventions.item(0);
          return namingConvention.getAttribute(ID_ATTR);
       }
       return null;
    }

    private static String getFilePathFromElement(Element root, String elemName) {
        String filePath = null;
        Element elem = XMLUtils.getFirstElement(elemName, root);
        if (elem != null) {
            filePath = elem.getAttribute(FILE_ATTR);
            if (Boolean.valueOf(elem.getAttribute(ENV_REPLACE_ATTR))) {
              filePath = PathUtils.replaceEnvVariables(filePath);
            }
        }
        return filePath;
    }
}
