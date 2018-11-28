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
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.ARGS_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.CMD_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.CREATE_BEFORE_EXEC_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.CUSTOM_METADATA_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.DIR_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.DIR_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.DYN_INPUT_FILES_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.ENV_REPLACE_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.ENV_REPLACE_NO_RECUR_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.EXE_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.FILE_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.FILE_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.IMPORT_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.KEYREF_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.KEY_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.KEY_GEN_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.METADATA_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.NAMESPACE_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.OUTPUT_TAG;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.PATH_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.SHELL_TYPE_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.SPLIT_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.VAL_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.WORKFLOW_MET_ATTR;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.WRITER_CLASS_ATTR;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CONFIG_FILE_PATH;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.QUERY_FILE_MANAGER_URL;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.util.QueryUtils;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;
import org.apache.oodt.commons.xml.XMLUtils;

//W3C imports
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An implementation of the {@link PgeConfigBuilder} that reads an XML file
 * representation of the {@link PgeConfig}.
 *
 * @author bfoster (Brian Foster)
 */
public class XmlFilePgeConfigBuilder implements PgeConfigBuilder {
    
    private static Logger LOG = Logger.getLogger(XmlFilePgeConfigBuilder.class.getName());
    
    public PgeConfig build(PgeMetadata pgeMetadata) throws IOException {
        try {
            PgeConfig pgeConfig = new PgeConfig();
            buildImports(
               fillIn(pgeMetadata.getMetadata(CONFIG_FILE_PATH),
                     pgeMetadata.asMetadata()), null, pgeConfig, pgeMetadata);
            return pgeConfig;
        } catch (Exception e) {
            throw new IOException("Failed to build PgeConfig : "
                    + e.getMessage(), e);
        }
    }
    
    private void buildImports(String configFile, String namespace, PgeConfig pgeConfig, PgeMetadata pgeMetadata) throws MalformedURLException, Exception {
        
        Element root = this.getRootElement(configFile);
        
         // load parent files
        NodeList nodeList = root.getElementsByTagName(IMPORT_TAG);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String curImportNS = this.fillIn(((Element) nodeList.item(i))
                    .getAttribute(NAMESPACE_ATTR), pgeMetadata
                    .asMetadata());
            String file = this.fillIn(((Element) nodeList.item(i))
                    .getAttribute(FILE_ATTR), pgeMetadata.asMetadata());
            if (!file.startsWith(File.separator))
                file = new File(configFile).getParent()
                        + File.separator + file;
            this.buildImports(file, curImportNS.equals("") ? null : curImportNS, pgeConfig, pgeMetadata);
        }

        // load base config file
        LOG.log(Level.INFO, "Loading PgeConfig file '" + configFile + "'");
        this.build(root, namespace, pgeConfig, pgeMetadata);
    }

    private void build(Element root, String namespace, PgeConfig pgeConfig,
            PgeMetadata pgeMetadata) throws MalformedURLException, Exception {

        // load custom metadata
        PgeMetadata localPgeMetadata = this.getCustomMetadata((Element) root
                .getElementsByTagName(CUSTOM_METADATA_TAG).item(0), pgeMetadata);
        PgeMetadata curPgeMetadata = new PgeMetadata();
        curPgeMetadata.replaceMetadata(pgeMetadata);
        curPgeMetadata.replaceMetadata(localPgeMetadata);
        Metadata curMetadata = curPgeMetadata.asMetadata();
        
        // load dynamic config file info
        List<DynamicConfigFile> configFileList = this.getDynConfigFile(
                (Element) root.getElementsByTagName(DYN_INPUT_FILES_TAG)
                        .item(0), curMetadata);
        for (DynamicConfigFile dcf : configFileList)
            pgeConfig.addDynamicConfigFile(dcf);

        // load exe info
        if (root.getElementsByTagName(EXE_TAG).getLength() > 0) {
            pgeConfig.setExeDir(this.getExeDir((Element) root.getElementsByTagName(
                EXE_TAG).item(0), curMetadata));        
            pgeConfig.setShellType(this.getShellType((Element) root
                .getElementsByTagName(EXE_TAG).item(0), curMetadata));
            List<String> exeCmds = pgeConfig.getExeCmds();
            exeCmds.addAll(this.getExeCmds((Element) root.getElementsByTagName(
                    EXE_TAG).item(0), curMetadata));
            pgeConfig.setExeCmds(exeCmds);
        }

        // load output dirs
        List<OutputDir> outputDirs = this.getOuputDirs((Element) root
                .getElementsByTagName(OUTPUT_TAG).item(0), curMetadata);
        for (OutputDir outputDir : outputDirs)
            pgeConfig.addOuputDirAndExpressions(outputDir);
        
        // add local pge metadata to global pge metadata with given namespace
        if (namespace != null) {
           pgeMetadata.replaceMetadata(localPgeMetadata, namespace);
        } else {
           pgeMetadata.replaceMetadata(localPgeMetadata);  
        }
    }

    private PgeMetadata getCustomMetadata(Element customMetadataElem, PgeMetadata pgeMetadata)
            throws MalformedURLException, Exception {
    	PgeMetadata localPgeMetadata = new PgeMetadata();
    	PgeMetadata curPgeMetadata = new PgeMetadata();
    	curPgeMetadata.replaceMetadata(pgeMetadata);
    	Metadata curPlusLocalMetadata = new Metadata();
    	curPlusLocalMetadata.addMetadata(curPgeMetadata.asMetadata().getHashtable());
    	
        if (customMetadataElem != null) {
            NodeList customMetadataList = customMetadataElem
                    .getElementsByTagName(METADATA_TAG);
            for (int i = 0; i < customMetadataList.getLength(); i++) {
                Element metadataElement = (Element) customMetadataList.item(i);
                String key = metadataElement.getAttribute(KEY_ATTR);
                if (key.equals(""))
                	key = this.fillIn(metadataElement.getAttribute(KEY_GEN_ATTR), curPlusLocalMetadata);
                if (!metadataElement.getAttribute(KEYREF_ATTR).equals("")) {
                	String val = metadataElement.getAttribute(KEYREF_ATTR);
                    if (metadataElement.getAttribute(ENV_REPLACE_NO_RECUR_ATTR)
                            .toLowerCase().equals("true"))
                        val = this.fillIn(val, curPlusLocalMetadata, false);
                    else if (!metadataElement.getAttribute(ENV_REPLACE_ATTR)
                            .toLowerCase().equals("false"))
                        val = this.fillIn(val, curPlusLocalMetadata);
                	localPgeMetadata.linkKey(key, val);
                	curPgeMetadata.linkKey(key, val);
                }else {
                	String val = metadataElement.getAttribute(VAL_ATTR);
                	if (val.equals("")) 
                		val = metadataElement.getTextContent();
                    if (metadataElement.getAttribute(ENV_REPLACE_NO_RECUR_ATTR)
                            .toLowerCase().equals("true"))
                        val = this.fillIn(val, curPlusLocalMetadata, false);
                    else if (!metadataElement.getAttribute(ENV_REPLACE_ATTR)
                            .toLowerCase().equals("false"))
                        val = this.fillIn(val, curPlusLocalMetadata);
                    List<String> valList = new Vector<String>();
                    if (!metadataElement.getAttribute(SPLIT_ATTR)
                            .toLowerCase().equals("false")) 
                    	valList.addAll(Arrays.asList((val + ",").split(",")));
                    else 
                    	valList.add(val);
                    localPgeMetadata.replaceMetadata(key, valList);
                    curPgeMetadata.replaceMetadata(key, valList);
                }
                if (metadataElement.getAttribute(WORKFLOW_MET_ATTR)
                        .toLowerCase().equals("true"))
                	localPgeMetadata.markAsDynamicMetadataKey(key);
                
                List<String> values = curPgeMetadata.getAllMetadata(key);
                if (values != null) {
                   curPlusLocalMetadata.replaceMetadata(key, values);
                }
            }
        }
        return localPgeMetadata;
    }

    private Element getRootElement(String xmlFilePath)
            throws FileNotFoundException {
        return XMLUtils.getDocumentRoot(
                new FileInputStream(new File(xmlFilePath)))
                .getDocumentElement();
    }

    private List<DynamicConfigFile> getDynConfigFile(
            Element dynConfigFileElement, Metadata curMetadata)
            throws MalformedURLException, Exception {
        List<DynamicConfigFile> configFileList = new LinkedList<DynamicConfigFile>();
        if (dynConfigFileElement != null) {
            NodeList dynConfigFilesList = dynConfigFileElement
                    .getElementsByTagName(FILE_TAG);
            for (int i = 0; i < dynConfigFilesList.getLength(); i++) {
                Element fileElement = (Element) dynConfigFilesList.item(i);
                configFileList.add(new DynamicConfigFile(this.fillIn(
                        fileElement.getAttribute(PATH_ATTR), curMetadata), this
                        .fillIn(fileElement.getAttribute(WRITER_CLASS_ATTR),
                                curMetadata), (Object[]) this.fillIn(
                        fileElement.getAttribute(ARGS_ATTR), curMetadata)
                        .split(",")));
            }
        }
        return configFileList;
    }

    private String getExeDir(Element exeElement, Metadata curMetadata)
            throws MalformedURLException, Exception {
        if (exeElement != null)
            return this.fillIn(exeElement.getAttribute(DIR_ATTR), curMetadata);
        else
            return null;
    }

    private String getShellType(Element exeElement, Metadata curMetadata)
            throws MalformedURLException, Exception {
        if (exeElement != null)
            return this.fillIn(exeElement.getAttribute(SHELL_TYPE_ATTR),
                    curMetadata);
        else
            return null;
    }

    private List<String> getExeCmds(Element exeElement, Metadata curMetadata)
            throws MalformedURLException, DOMException, Exception {
        LinkedList<String> exeCmds = new LinkedList<String>();
        if (exeElement != null) {
            NodeList cmds = exeElement.getElementsByTagName(CMD_TAG);
            for (int i = 0; i < cmds.getLength(); i++) {
                Element cmd = (Element) cmds.item(i);
                exeCmds.add(this.fillIn(cmd.getTextContent(), curMetadata));
            }
        }
        return exeCmds;
    }

    private List<OutputDir> getOuputDirs(Element ouputDirElement,
            Metadata curMetadata) throws MalformedURLException, Exception {
        List<OutputDir> outputDirs = new LinkedList<OutputDir>();
        if (ouputDirElement != null) {
            NodeList outputDirsList = ouputDirElement
                    .getElementsByTagName(DIR_TAG);
            for (int i = 0; i < outputDirsList.getLength(); i++) {
                Element outputDirElement = (Element) outputDirsList.item(i);
                String dirPath = fillIn(outputDirElement
                      .getAttribute(PATH_ATTR), curMetadata);
                OutputDir outputDir = new OutputDir(dirPath,
                      Boolean.parseBoolean(fillIn(outputDirElement
                            .getAttribute(CREATE_BEFORE_EXEC_ATTR),
                            curMetadata)));
                outputDirs.add(outputDir);
            }
        }
        return outputDirs;
    }

    private String fillIn(String value, Metadata inputMetadata) throws Exception {
    	return this.fillIn(value, inputMetadata, true);
    }
    
    private String fillIn(String value, Metadata inputMetadata, boolean envReplaceRecur)
            throws Exception {
    	try {
	        while ((value = PathUtils.doDynamicReplacement(value, inputMetadata)).contains("[") && envReplaceRecur);
	        if (value.toUpperCase()
	                .matches("^\\s*SQL\\s*\\(.*\\)\\s*\\{.*\\}\\s*$"))
	            value = QueryUtils.getQueryResultsAsString(new XmlRpcFileManagerClient(new URL(inputMetadata
	                    .getMetadata(QUERY_FILE_MANAGER_URL.getName()))).complexQuery(SqlParser.parseSqlQueryMethod(value)));
	        return value;
    	}catch (Exception e) {
    		throw new Exception("Failed to parse value: " + value, e);
    	}
    }
}
