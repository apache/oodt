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

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.metadata.PGETaskMetKeys;
import org.apache.oodt.commons.xml.XMLUtils;

//JPL OODT imports
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.util.QueryUtils;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.*;

//DOM imports
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An implementation of the {@link PgeConfigBuilder} that reads an XML file
 * representation of the {@link PgeConfig}
 * </p>.
 */
public class XmlFilePgeConfigBuilder extends FileBasedPgeConfigBuilder {
    
    private static Logger LOG = Logger.getLogger(XmlFilePgeConfigBuilder.class.getName());
    
    public XmlFilePgeConfigBuilder() {
    	super();
    }
    
    protected PgeConfig _build(ControlMetadata ctrlMetadata) throws Exception {
        try {
            PgeConfig pgeConfig = new PgeConfig();
            this.buildImports(this.fillIn(ctrlMetadata
                    .getMetadata(CONFIG_FILE_PATH),
                    ctrlMetadata.asMetadata()), null, pgeConfig, ctrlMetadata);
            return pgeConfig;
        } catch (Exception e) {
            throw new Exception("Failed to build PgeConfig : "
                    + e.getMessage(), e);
        }
        
    }
    
    private void buildImports(String configFile, String namespace, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws MalformedURLException, Exception {
        
        Element root = this.getRootElement(configFile);
        
         // load parent files
        NodeList nodeList = root.getElementsByTagName(IMPORT_TAG);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String curImportNS = this.fillIn(((Element) nodeList.item(i))
                    .getAttribute(NAMESPACE_ATTR), ctrlMetadata
                    .asMetadata());
            String file = this.fillIn(((Element) nodeList.item(i))
                    .getAttribute(FILE_ATTR), ctrlMetadata.asMetadata());
            if (!file.startsWith(File.separator))
                file = new File(configFile).getParent()
                        + File.separator + file;
            this.buildImports(file, curImportNS.equals("") ? null : curImportNS, pgeConfig, ctrlMetadata);
        }

        // load base config file
        LOG.log(Level.INFO, "Loading PgeConfig file '" + configFile + "'");
        this.build(root, namespace, pgeConfig, ctrlMetadata);

    }

    private void build(Element root, String namespace, PgeConfig pgeConfig,
    		ControlMetadata ctrlMetadata) throws MalformedURLException, Exception {

        // load custom metadata
    	ControlMetadata localPgeMetadata = null;
        if (root.getElementsByTagName(CUSTOM_METADATA_TAG).getLength() > 0) {
        	localPgeMetadata = this.getCustomMetadata((Element) root
                .getElementsByTagName(CUSTOM_METADATA_TAG).item(0), ctrlMetadata);
        }
        
        // update metadata
    	ControlMetadata curPgeMetadata = new ControlMetadata();
        curPgeMetadata.replaceControlMetadata(ctrlMetadata);
        if (localPgeMetadata != null)
        	curPgeMetadata.replaceControlMetadata(localPgeMetadata);
        Metadata curMetadata = curPgeMetadata.asMetadata();
        
        // load file staging info
        if (root.getElementsByTagName(FILE_STAGING_TAG).getLength() > 0) {
        	pgeConfig.setFileStagingInfo(this.getFileStagingInfo((Element) root
					.getElementsByTagName(FILE_STAGING_TAG).item(0), ctrlMetadata,
					localPgeMetadata, curMetadata));
        }
        
        // add local pge metadata to global pge metadata with given namespace
        curPgeMetadata.replaceControlMetadata(ctrlMetadata);
        if (localPgeMetadata != null)
        	curPgeMetadata.replaceControlMetadata(localPgeMetadata);
        curMetadata = curPgeMetadata.asMetadata();
        
        // load dynamic config file info
        if (root.getElementsByTagName(DYN_INPUT_FILES_TAG).getLength() > 0) {
        	List<DynamicConfigFile> configFileList = this.getDynConfigFile(
                (Element) root.getElementsByTagName(DYN_INPUT_FILES_TAG)
                        .item(0), curMetadata);
        	for (DynamicConfigFile dcf : configFileList)
        		pgeConfig.addDynamicConfigFile(dcf);
        }
        
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
        if (root.getElementsByTagName(OUTPUT_TAG).getLength() > 0) {
	        List<OutputDir> outputDirs = this.getOuputDirs((Element) root
	                .getElementsByTagName(OUTPUT_TAG).item(0), curMetadata);
	        for (OutputDir outputDir : outputDirs)
	            pgeConfig.addOuputDirAndExpressions(outputDir);
        }

        // add local pge metadata to global pge metadata with given namespace
        if (localPgeMetadata != null)
        	ctrlMetadata.replaceControlMetadata(localPgeMetadata, namespace);
    }

    private ControlMetadata getCustomMetadata(Element customMetadataElem, ControlMetadata ctrlMetadata)
            throws MalformedURLException, Exception {
    	ControlMetadata localPgeMetadata = new ControlMetadata();
    	Metadata currentMetadata = ctrlMetadata.asMetadata();
        if (customMetadataElem != null) {
            NodeList customMetadataList = customMetadataElem
                    .getElementsByTagName(METADATA_TAG);
        	String key = null;
            try {
	            for (int i = 0; i < customMetadataList.getLength(); i++) {
	                Element metadataElement = (Element) customMetadataList.item(i);
	                key = metadataElement.getAttribute(KEY_ATTR);
	                if (key.equals(""))
	                	key = this.fillIn(metadataElement.getAttribute(KEY_GEN_ATTR), currentMetadata);
	                if (!metadataElement.getAttribute(KEYREF_ATTR).equals("")) {
	                	String val = metadataElement.getAttribute(KEYREF_ATTR);
	                    if (metadataElement.getAttribute(ENV_REPLACE_NO_RECUR_ATTR)
	                            .toLowerCase().equals("true"))
	                        val = this.fillIn(val, currentMetadata, false);
	                    else if (!metadataElement.getAttribute(ENV_REPLACE_ATTR)
	                            .toLowerCase().equals("false"))
	                        val = this.fillIn(val, currentMetadata);
	                	localPgeMetadata.linkKey(key, val);
	                    currentMetadata.replaceMetadata(key, currentMetadata.getAllMetadata(val));
	                }else {
	                	String val = metadataElement.getAttribute(VAL_ATTR);
	                	if (val.equals("")) 
	                		val = metadataElement.getTextContent();
	                    if (metadataElement.getAttribute(ENV_REPLACE_NO_RECUR_ATTR)
	                            .toLowerCase().equals("true"))
	                        val = this.fillIn(val, currentMetadata, false);
	                    else if (!metadataElement.getAttribute(ENV_REPLACE_ATTR)
	                            .toLowerCase().equals("false"))
	                        val = this.fillIn(val, currentMetadata);
	                    List<String> valList = new Vector<String>();
	                    if (!metadataElement.getAttribute(SPLIT_ATTR)
	                            .toLowerCase().equals("false")) 
	                    	valList.addAll(Arrays.asList((val + ",").split(",")));
	                    else 
	                    	valList.add(val);
	                    if (localPgeMetadata.isLink(key) && localPgeMetadata.getMetadata(key) == null) {
	            			String keyref = localPgeMetadata.getReferenceKey(key);
	                    	if (ctrlMetadata.getMetadata(keyref) != null) {
		                    	ctrlMetadata.replaceLocalMetadata(keyref, valList);
		                    	if (ctrlMetadata.getMetadata(keyref, ControlMetadata.DYN) != null) {
		                    		ctrlMetadata.setAsWorkflowMetadataKey(keyref);
		                    		ctrlMetadata.commitWorkflowMetadataKeys(keyref);
		                    	}
	                		}else {
	                        	throw new Exception("Dangling key-ref '" + key + "'");
	                		}
	                    	for (String parentKey : localPgeMetadata.getReferenceKeyPath(key)) 
	                            currentMetadata.replaceMetadata(parentKey, valList);
	                    }else {
	                    	localPgeMetadata.replaceLocalMetadata(key, valList);
	                    }
	                    currentMetadata.replaceMetadata(key, valList);
	                }
	                if (metadataElement.getAttribute(WORKFLOW_MET_ATTR)
	                        .toLowerCase().equals("true")) {
	                	localPgeMetadata.setAsWorkflowMetadataKey(key);
	                	localPgeMetadata.commitWorkflowMetadataKeys(key);
	                }
//                currentMetadata.replaceMetadata(key, localPgeMetadata.getAllMetadata(key));
	            }
            }catch (Exception e) {
            	throw new Exception("Failed to load custom metadata [key='"
						+ key
						+ "',val='"
						+ (key != null ? currentMetadata.getMetadata(key)
								: null) + "'] : " + e.getMessage(), e);
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
    
    private FileStagingInfo getFileStagingInfo(Element fileStagingElement, ControlMetadata ctrlMetadata, ControlMetadata localPgeMetadata, Metadata curMetadata) throws Exception {    	
    	String stagingDir = this.fillIn(fileStagingElement.getAttribute(DIR_ATTR), curMetadata);
    	boolean forceStaging = Boolean.parseBoolean(this.fillIn(fileStagingElement.getAttribute(FORCE_ATTR), curMetadata));
    	FileStagingInfo fileStagingInfo = new FileStagingInfo(stagingDir, forceStaging);
    
    	if (fileStagingElement.getElementsByTagName(STAGE_FILES_TAG).getLength() > 0) {
    		NodeList stageFiles = fileStagingElement.getElementsByTagName(STAGE_FILES_TAG);
    		for (int i = 0; i < stageFiles.getLength(); i++) {
    			String fileKey = ((Element) stageFiles.item(i)).getAttribute(METADATA_KEY_ATTR);
        		List<String> files = curMetadata.getAllMetadata(fileKey);
        		fileStagingInfo.addFilePaths(files);
        		List<String> newPaths = new Vector<String>();
        		for (String file : files) {
        			File fileHandle = new File(file);
        			if (fileStagingInfo.isForceStaging() || !fileHandle.exists()) {
        				newPaths.add(fileStagingInfo.getStagingDir() + "/" + fileHandle.getName());
        			}else {
        				newPaths.add(file);
        			}
        		}
        		curMetadata.replaceMetadata(fileKey, newPaths);
        		if (localPgeMetadata != null && localPgeMetadata.getMetadata(fileKey) != null) {
        			localPgeMetadata.replaceLocalMetadata(fileKey, newPaths);
        			if (localPgeMetadata.getMetadata(fileKey, ControlMetadata.DYN) != null) 
        				localPgeMetadata.commitWorkflowMetadataKeys(fileKey);
        		}
    			if (ctrlMetadata.getMetadata(fileKey = localPgeMetadata.getReferenceKey(fileKey)) != null) {
        			ctrlMetadata.replaceLocalMetadata(fileKey, newPaths);
        			if (ctrlMetadata.getMetadata(fileKey, ControlMetadata.DYN) != null) 
        				ctrlMetadata.commitWorkflowMetadataKeys(fileKey);
        		}
    		}
    	}
    	
    	return fileStagingInfo;
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
                String dirPath = this.fillIn(outputDirElement
                        .getAttribute(PATH_ATTR), curMetadata);
                OutputDir outputDir = new OutputDir(dirPath, this.fillIn(
                        outputDirElement.getAttribute(CREATE_BEFORE_EXEC_ATTR),
                        curMetadata).equals("true"));
                NodeList fileList = outputDirElement
                        .getElementsByTagName(FILES_TAG);
                for (int j = 0; j < fileList.getLength(); j++) {
                    Element fileElement = (Element) fileList.item(j);
                    String outputFile = fileElement.getAttribute(REGEX_ATTR);
                    if (outputFile.equals(""))
                        outputFile = this.fillIn(fileElement
                                .getAttribute(NAME_ATTR), curMetadata);
                    NodeList renamingConvNodes = fileElement.getElementsByTagName(RENAMING_CONV_TAG);
                    RenamingConv renamingConv = null;
                    if (renamingConvNodes.getLength() > 0) {
                    	Element renamingElement = (Element) renamingConvNodes.item(0);
                    	String namingExpr = renamingElement.getAttribute(NAMING_EXPR_ATTR);
                        if (renamingElement.getAttribute(ENV_REPLACE_NO_RECUR_ATTR)
                                .toLowerCase().equals("true"))
                        	namingExpr = this.fillIn(namingExpr, curMetadata, false);
                        else if (!renamingElement.getAttribute(ENV_REPLACE_ATTR)
                                .toLowerCase().equals("false"))
                        	namingExpr = this.fillIn(namingExpr, curMetadata);
                        renamingConv = new RenamingConv(namingExpr);
                    	NodeList metadataNodes = renamingElement.getElementsByTagName(METADATA_TAG);
                        for (int k = 0; k < metadataNodes.getLength(); k++) 
                        	renamingConv.addTmpReplaceMet(
									((Element) metadataNodes.item(k))
											.getAttribute(KEY_ATTR), Arrays
											.asList(((Element) metadataNodes
													.item(k)).getAttribute(
													VAL_ATTR).split(",")));
                    }
                    outputDir.addRegExprOutputFiles(new RegExprOutputFiles(
                            outputFile, this.fillIn(fileElement
                                    .getAttribute(MET_FILE_WRITER_CLASS_ATTR),
                                    curMetadata), renamingConv, (Object[]) this.fillIn(
                                    fileElement.getAttribute(ARGS_ATTR),
                                    curMetadata).split(",")));
                }
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
	                    .getMetadata(PGETaskMetKeys.QUERY_FILE_MANAGER_URL))).complexQuery(SqlParser.parseSqlQueryMethod(value)));
	        return value;
    	}catch (Exception e) {
    		throw new Exception("Failed to parse value: " + value, e);
    	}
    }

}
