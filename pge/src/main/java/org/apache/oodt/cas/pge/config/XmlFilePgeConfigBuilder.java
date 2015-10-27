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
package org.apache.oodt.cas.pge.config;

//OODT static imports
import com.google.common.collect.Lists;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.pge.exceptions.PGEException;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;
import org.apache.oodt.cas.pge.util.Pair;
import org.apache.oodt.cas.pge.util.XmlHelper;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CONFIG_FILE_PATH;
import static org.apache.oodt.cas.pge.util.XmlHelper.*;


/**
 * An implementation of the {@link PgeConfigBuilder} that reads an XML file
 * representation of the {@link PgeConfig}.
 *
 * @author bfoster (Brian Foster)
 */
public class XmlFilePgeConfigBuilder implements PgeConfigBuilder {

   private final List<String> missingMetadataKeys;

   public XmlFilePgeConfigBuilder() {
      missingMetadataKeys = Lists.newArrayList();
   }

   @Override
   public PgeConfig build(PgeMetadata pgeMetadata) throws IOException {
      try {
         missingMetadataKeys.clear();

         PgeConfig pgeConfig = new PgeConfig();
         String configFile = fillIn(pgeMetadata.getMetadata(CONFIG_FILE_PATH),
               pgeMetadata.asMetadata());
         if (configFile == null) {
            throw new Exception("Must specify metadata field '"
                  + CONFIG_FILE_PATH + "'");
         }
         pgeMetadata.replaceMetadata(loadConfigFile(configFile, pgeConfig,
               pgeMetadata));
         return pgeConfig;
      } catch (Exception e) {
         throw new IOException("Failed to build PgeConfig : " + e.getMessage(),
               e);
      }
   }

   private PgeMetadata loadConfigFile(String configFile, PgeConfig pgeConfig,
         PgeMetadata parentPgeMetadata) throws FileNotFoundException, PGEException {
      PgeMetadata pgeMetadata = new PgeMetadata(parentPgeMetadata);
      Element root = getRootElement(configFile);

      // Read in imports
      List<Pair<String, String>> imports = getImports(root, pgeMetadata.asMetadata());
      for (Pair<String, String> imp : imports) {
         String namespace = imp.getFirst();
         String file = imp.getSecond();

         // If relative path, then make path relative to configFile.
         if (!file.startsWith(File.separator)) {
            file = new File(configFile).getParent() + File.separator + file;
         }

         // Add metadata generated from import.
         if (namespace != null) {
            pgeMetadata.replaceMetadata(
                  loadConfigFile(file, pgeConfig, parentPgeMetadata),
                  namespace);
         } else {
            pgeMetadata.replaceMetadata(loadConfigFile(file, pgeConfig,
                  parentPgeMetadata));
         }
      }

      // load custom metadata
      loadCustomMetadata(root, pgeMetadata);
      Metadata metadata = pgeMetadata.asMetadata();

      // load dynamic config file info
      for (DynamicConfigFile dcf : getDynamicConfigFiles(root, metadata)) {
         pgeConfig.addDynamicConfigFile(dcf);
      }

      // load file staging info.
      loadFileStagingInfo(root, pgeConfig, pgeMetadata);

      // load exe info
      Element exeElem = XmlHelper.getExe(root);
      if (exeElem != null) {
         pgeConfig.setExeDir(getDir(exeElem, metadata));
         pgeConfig.setShellType(getShellType(exeElem, metadata));
         pgeConfig.setExeCmds(getExeCmds(exeElem, metadata));
      }

      // load output dirs
      Element outputElem = getOutput(root);
      if (outputElem != null) {
         for (OutputDir outputDir : getOuputDirs(outputElem, metadata)) {
            pgeConfig.addOuputDirAndExpressions(outputDir);
         }
      }

      return pgeMetadata;
   }

   private void loadCustomMetadata(Element root, PgeMetadata pgeMetadata)
       throws PGEException {

      // Check if there is a 'customMetadata' elem and load it.
      Element customMetadataElem = getCustomMetadataElement(root);
      if (customMetadataElem == null) {
         return;
      }

      // Iterate through metadata elements.
      for (Element metElem : getMetadataElements(customMetadataElem)) {
         Metadata curMetadata = pgeMetadata.asMetadata();

         // Load supported metadata element attributes.
         String key = getMetadataKey(metElem, curMetadata);
         List<String> values = getMetadataValues(metElem, curMetadata);
         String keyRef = getMetadataKeyRef(metElem, curMetadata);

         // Check that either val or key-ref is given.
         if (!values.isEmpty() && keyRef != null) {
            throw new PGEException(
                  "Cannot specify both values and keyref for metadata key '"
                        + key + "'");

            // If val is given then set metadata with key and val.
         } else if (!values.isEmpty()) {
            pgeMetadata.replaceMetadata(key, values);

            // Otherwise key-ref was given, so set the link.
         } else {
            pgeMetadata.linkKey(key, keyRef);
         }

         // Check if current key should be marked as workflow metadata.
         if (isWorkflowMetKey(metElem, curMetadata)) {
            pgeMetadata.markAsDynamicMetadataKey(key);
         }
      }
   }

   private void loadFileStagingInfo(Element root, PgeConfig pgeConfig,
         PgeMetadata pgeMetadata) throws PGEException {
      Metadata metadata = pgeMetadata.asMetadata();
      Element fileStagingElem = getFileStaging(root);

      // Check if there is file staging info specified.
      if (fileStagingElem != null) {
         FileStagingInfo fileStagingInfo = new FileStagingInfo(getDir(
               fileStagingElem, metadata), isForceStage(fileStagingElem,
               metadata));

         // Iterate through list of metadata keys which have list of files as
         // their values which should be staged.
         for (String metKey : getStageFilesMetKeys(fileStagingElem, metadata)) {
            List<String> files = metadata.getAllMetadata(metKey);
            fileStagingInfo.addFilePaths(files);

            // Generate paths which the files will be staged to.
            List<String> newPaths = Lists.newArrayList();
            for (String file : files) {
               File fileHandle = new File(file);
               if (fileStagingInfo.isForceStaging() || !fileHandle.exists()) {
                  newPaths.add(fileStagingInfo.getStagingDir() + File.separator
                        + fileHandle.getName());
               } else {
                  newPaths.add(file);
               }
            }

            // Update metadata key with what will be the new paths of the files.
            pgeMetadata.replaceMetadata(metKey, newPaths);
         }

         // Add staging info to PgeConfig.
         pgeConfig.setFileStagingInfo(fileStagingInfo);
      }
   }
}
