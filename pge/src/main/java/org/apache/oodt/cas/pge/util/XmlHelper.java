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
package org.apache.oodt.cas.pge.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.QueryUtils;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.filenaming.PathUtilsNamingConvention;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.config.DynamicConfigFile;
import org.apache.oodt.cas.pge.config.OutputDir;
import org.apache.oodt.cas.pge.config.RegExprOutputFiles;
import org.apache.oodt.cas.pge.exceptions.PGEException;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.*;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.QUERY_FILE_MANAGER_URL;


/**
 * Help class with method for parsing XMLFilePgeConfigBuilder config XML file.
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class XmlHelper {

	private XmlHelper() {
	}

	public static Element getRootElement(String xmlFilePath)
			throws FileNotFoundException {
		return XMLUtils.getDocumentRoot(
				new FileInputStream(new File(xmlFilePath)))
				.getDocumentElement();
	}

	public static List<Pair<String, String>> getImports(Element elem,
			Metadata metadata) throws PGEException {
		List<Pair<String, String>> imports = Lists.newArrayList();
		NodeList nodeList = elem.getElementsByTagName(IMPORT_TAG);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element importElem = (Element) nodeList.item(i);
			String namespace = getNamespace(importElem, metadata);
			String file = getFile(importElem, metadata);
			imports.add(new Pair<String, String>(namespace, file));
		}
		return imports;
	}

	public static String getNamespace(Element elem, Metadata metadata)
		throws PGEException {
		String namespace = elem.getAttribute(NAMESPACE_ATTR);
		if (!Strings.isNullOrEmpty(namespace)) {
			return fillIn(namespace, metadata, false);
		} else {
			return null;
		}
	}

	public static String getFile(Element elem, Metadata metadata)
		throws PGEException {
		String file = elem.getAttribute(FILE_ATTR);
		if (!Strings.isNullOrEmpty(file)) {
			return fillIn(file, metadata, false);
		} else {
			return null;
		}
	}

	public static Element getCustomMetadataElement(Element root)
		throws PGEException {
		NodeList nodes = root.getElementsByTagName(CUSTOM_METADATA_TAG);
		if (nodes.getLength() == 0) {
			return null;
		} else if (nodes.getLength() == 1) {
			return (Element) nodes.item(0);
		} else {
			throw new PGEException("Found more than one '" + CUSTOM_METADATA_TAG
					+ "' element");
		}
	}

	public static List<Element> getMetadataElements(Element customMetadataElem) {
		NodeList metElemsNodeList = customMetadataElem
				.getElementsByTagName(METADATA_TAG);
		List<Element> metElems = Lists.newArrayList();
		for (int i = 0; i < metElemsNodeList.getLength(); i++) {
			metElems.add((Element) metElemsNodeList.item(i));
		}
		return metElems;
	}

	public static String getMetadataKey(Element metElem, Metadata metadata)
		throws PGEException {
		String key = metElem.getAttribute(KEY_ATTR);

		// no key attr, so check for key_gen attr.
		if (Strings.isNullOrEmpty(key)) {
			key = fillIn(metElem.getAttribute(KEY_GEN_ATTR), metadata);
		}

		// if still no key value, then fail.
		if (Strings.isNullOrEmpty(key)) {
			throw new PGEException("Must specify either metadata attr '"
					+ KEY_ATTR + "' or '" + KEY_GEN_ATTR + "'");
		}

		// else success!
		return key;
	}

	public static boolean isEnvReplaceNoRecur(Element elem, Metadata metadata) {
		String isEnvReplaceNoRecur = elem
				.getAttribute(ENV_REPLACE_NO_RECUR_ATTR);
		if (Strings.isNullOrEmpty(isEnvReplaceNoRecur)) {
			return false;
		} else {
			return isEnvReplaceNoRecur.trim().toLowerCase().equals("true");
		}
	}

	public static boolean isEnvReplace(Element elem, Metadata metadata) {
		String isEnvReplace = elem.getAttribute(ENV_REPLACE_ATTR);
		if (Strings.isNullOrEmpty(isEnvReplace)) {
			return true;
		} else {
			return !isEnvReplace.trim().toLowerCase().equals("false");
		}
	}

	public static boolean isMultiValue(Element elem, Metadata metadata)
		throws PGEException {
		return Boolean.parseBoolean(fillIn(elem.getAttribute(SPLIT_ATTR),
				metadata));
	}

	public static List<String> getMetadataValues(Element elem, Metadata metadata)
		throws PGEException {
		List<String> values = Lists.newArrayList();

		// Read val attr.
		String value = elem.getAttribute(VAL_ATTR);

		// Check if val tag was not specified see if value was given as element
		// text.
		if (Strings.isNullOrEmpty(value)) {
			value = elem.getTextContent();
		}

		// If value was found.
		if (!Strings.isNullOrEmpty(value)) {

			// Is multi-value so split up value.
			if (isMultiValue(elem, metadata)) {
				for (String v : Splitter.on(",").split(value)) {

					// Check for envReplace and perform met replacement on value
					// if set.
					if (isEnvReplaceNoRecur(elem, metadata)) {
						values.add(fillIn(v, metadata, false));
					} else if (isEnvReplace(elem, metadata)) {
						values.add(fillIn(v, metadata));
					}
				}

				// Is scalar
			} else {

				// Check for envReplace and perform met replacement on value if
				// set.
				if (isEnvReplaceNoRecur(elem, metadata)) {
					value = fillIn(value, metadata, false);
				} else if (isEnvReplace(elem, metadata)) {
					value = fillIn(value, metadata);
				}
				values.add(value);
			}

		}
		return values;
	}

	public static String getMetadataKeyRef(Element elem, Metadata metadata)
		throws PGEException {
		String keyRef = elem.getAttribute(KEYREF_ATTR);
		if (!Strings.isNullOrEmpty(keyRef)) {

			// Check for envReplace and perform met replacement on value if set.
			if (isEnvReplaceNoRecur(elem, metadata)) {
				keyRef = fillIn(keyRef, metadata, false);
			} else if (isEnvReplace(elem, metadata)) {
				keyRef = fillIn(keyRef, metadata);
			}

			return keyRef;
		} else {
			return null;
		}
	}

	public static boolean isWorkflowMetKey(Element elem, Metadata metadata)
		throws PGEException {
		return Boolean.parseBoolean(fillIn(
				elem.getAttribute(WORKFLOW_MET_ATTR), metadata, false));
	}

	public static String getPath(Element elem, Metadata metadata)
		throws PGEException {
		return fillIn(elem.getAttribute(PATH_ATTR), metadata, false);
	}

	public static String getWriter(Element elem, Metadata metadata)
		throws PGEException {
		return fillIn(elem.getAttribute(WRITER_CLASS_ATTR), metadata, false);
	}

	public static List<String> getArgs(Element elem, Metadata metadata)
		throws PGEException {
		List<String> args = Lists.newArrayList();
		for (String arg : Splitter.on(",").split(elem.getAttribute(ARGS_ATTR))) {
			args.add(fillIn(arg, metadata, false));
		}
		if (args.size() == 1 && Strings.isNullOrEmpty(args.get(0))) {
			return Lists.newArrayList();
		} else {
			return args;
		}
	}

	public static List<DynamicConfigFile> getDynamicConfigFiles(Element elem,
			Metadata metadata) throws PGEException {
		List<DynamicConfigFile> dynamicConfigFiles = Lists.newArrayList();
		NodeList nodeList = elem.getElementsByTagName(DYN_INPUT_FILES_TAG);

		// Check if dynInput element exists.
		if (nodeList.getLength() > 0) {
			Element dynamicConfigFilesElem = (Element) nodeList.item(0);
			nodeList = dynamicConfigFilesElem.getElementsByTagName(FILE_TAG);

			// Load each dynamic input file information.
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element fileElem = (Element) nodeList.item(i);
				String path = getPath(fileElem, metadata);
				String writer = getWriter(fileElem, metadata);
				List<String> args = getArgs(fileElem, metadata);
				dynamicConfigFiles.add(new DynamicConfigFile(path, writer, args
					.toArray(new String[args.size()])));
			}
		}
		return dynamicConfigFiles;
	}

	public static Element getExe(Element elem) throws PGEException {
		NodeList nodeList = elem.getElementsByTagName(EXE_TAG);
		if (nodeList.getLength() > 1) {
			throw new PGEException("Can only specify '" + EXE_TAG + "' once!");
		} else if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			return null;
		}
	}

	public static String getDir(Element elem, Metadata metadata)
		throws PGEException {
		return fillIn(elem.getAttribute(DIR_ATTR), metadata);
	}

	public static String getShellType(Element elem, Metadata metadata)
		throws PGEException {
		return fillIn(elem.getAttribute(SHELL_TYPE_ATTR), metadata);
	}

	public static List<String> getExeCmds(Element elem, Metadata metadata)
		throws PGEException {
		List<String> exeCmds = Lists.newArrayList();
		NodeList nodeList = elem.getElementsByTagName(CMD_TAG);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element cmdElem = (Element) nodeList.item(i);
			String exeCmd = cmdElem.getTextContent();
			if (isEnvReplaceNoRecur(cmdElem, metadata)) {
				exeCmd = fillIn(exeCmd, metadata, false);
			} else if (isEnvReplace(cmdElem, metadata)) {
				exeCmd = fillIn(exeCmd, metadata);
			}
			exeCmds.add(exeCmd);
		}
		return exeCmds;
	}

	public static Element getFileStaging(Element elem) throws PGEException {
		NodeList nodeList = elem.getElementsByTagName(FILE_STAGING_TAG);
		if (nodeList.getLength() > 1) {
			throw new PGEException("Can only specify '" + FILE_STAGING_TAG
					+ "' once!");
		} else if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			return null;
		}
	}

	public static boolean isForceStage(Element elem, Metadata metadata)
		throws PGEException {
		return Boolean.parseBoolean(fillIn(elem.getAttribute(FORCE_ATTR),
				metadata));
	}

	public static String getFileStagingMetadataKey(Element elem,
			Metadata metadata) throws PGEException {
		return fillIn(elem.getAttribute(METADATA_KEY_ATTR), metadata);
	}

	public static List<String> getStageFilesMetKeys(Element elem,
			Metadata metadata) throws PGEException {
		List<String> metKeys = Lists.newArrayList();
		NodeList nodeList = elem.getElementsByTagName(STAGE_FILES_TAG);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element stageFilesElem = (Element) nodeList.item(i);
			metKeys.add(getFileStagingMetadataKey(stageFilesElem, metadata));
		}
		return metKeys;
	}

	public static Element getOutput(Element elem) throws PGEException {
		NodeList nodeList = elem.getElementsByTagName(OUTPUT_TAG);
		if (nodeList.getLength() > 1) {
			throw new PGEException("Can only specify '" + OUTPUT_TAG + "' once!");
		} else if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			return null;
		}
	}

	public static boolean isCreateBeforeExe(Element elem, Metadata metadata)
		throws PGEException {
		return Boolean.parseBoolean(fillIn(
				elem.getAttribute(CREATE_BEFORE_EXEC_ATTR), metadata));
	}

	public static List<OutputDir> getOuputDirs(Element elem, Metadata metadata)
		throws PGEException {
		List<OutputDir> outputDirs = Lists.newArrayList();
		NodeList nodeList = elem.getElementsByTagName(DIR_TAG);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element outputDirElem = (Element) nodeList.item(i);
			String path = getPath(outputDirElem, metadata);
			boolean createBeforeExe = isCreateBeforeExe(outputDirElem, metadata);
			OutputDir outputDir = new OutputDir(path, createBeforeExe);
			getRegExpOutputFiles(outputDirElem, metadata, outputDir);
			outputDirs.add(outputDir);
		}
		return outputDirs;

	}

	public static void getRegExpOutputFiles(Element elem, Metadata metadata,
			OutputDir outputDir) throws PGEException {
		NodeList fileList = elem.getElementsByTagName(FILES_TAG);
		for (int j = 0; j < fileList.getLength(); j++) {
			Element fileElement = (Element) fileList.item(j);
			String outputFile = fileElement.getAttribute(REGEX_ATTR);
			if (outputFile.equals("")){
				outputFile = fillIn(fileElement.getAttribute(NAME_ATTR),
						metadata);
			}
			PathUtilsNamingConvention renamingConvention = null;
			NodeList renamingConvNodes = fileElement
					.getElementsByTagName(RENAMING_CONV_TAG);

			if (renamingConvNodes.getLength() > 0) {
				Element renamingElement = (Element) renamingConvNodes.item(0);
				String namingExpr = renamingElement
						.getAttribute(NAMING_EXPR_ATTR);
				if (renamingElement.getAttribute(ENV_REPLACE_ATTR)
						.toLowerCase().equals("true")){
					namingExpr = fillIn(namingExpr, metadata, false);
				}
				else if (!renamingElement.getAttribute(ENV_REPLACE_ATTR)
						.toLowerCase().equals("false")){
					namingExpr = fillIn(namingExpr, metadata);
				}
				renamingConvention = new PathUtilsNamingConvention();
				renamingConvention.setNamingConv(namingExpr);
				NodeList metadataNodes = renamingElement
						.getElementsByTagName(METADATA_TAG);
				for (int k = 0; k < metadataNodes.getLength(); k++) {
					renamingConvention.addTmpReplaceMet(((Element) metadataNodes
							.item(k)).getAttribute(KEY_ATTR), Arrays
							.asList(((Element) metadataNodes.item(k))
									.getAttribute(VAL_ATTR).split(",")));
				}
			}

			outputDir
					.addRegExprOutputFiles(new RegExprOutputFiles(outputFile,
							fillIn(fileElement
									.getAttribute(MET_FILE_WRITER_CLASS_ATTR),
									metadata), renamingConvention, (Object[]) fillIn(
									fileElement.getAttribute(ARGS_ATTR),
									metadata).split(",")));
		}

	}

	public static String fillIn(String value, Metadata inputMetadata)
		throws PGEException {
		return fillIn(value, inputMetadata, true);
	}

	public static String fillIn(String value, Metadata inputMetadata, boolean envReplaceRecur) throws PGEException {
		FileManagerClient fmClient=null;
		try {
			while ((value = PathUtils.doDynamicReplacement(value, inputMetadata)).contains("[") && envReplaceRecur) {
			}

			if (value.toUpperCase().matches("^\\s*SQL\\s*\\(.*\\)\\s*\\{.*\\}\\s*$")) {
				fmClient = RpcCommunicationFactory
						.createClient(new URL(inputMetadata.getMetadata(QUERY_FILE_MANAGER_URL.getName())));
				value = QueryUtils.getQueryResultsAsString(fmClient.complexQuery(SqlParser.parseSqlQueryMethod(value)));
			}
			return value;
		} catch (Exception e) {
			throw new PGEException("Failed to parse value: " + value, e);
		} finally {
			if (fmClient != null) {
				try {
					fmClient.close();
				} catch (IOException ignored) { }
			}
		}
	}
}
