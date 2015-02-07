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
import org.apache.oodt.cas.pushpull.exceptions.ConfigException;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.renamingconventions.RenamingConvention;
import org.apache.oodt.cas.pushpull.objectfactory.PushPullObjectFactory;
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

//DOM imports
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Google imports
import com.google.common.base.Strings;

/**
 * Remote Site Crawling specifications.
 *
 * @author bfoster (Brian Foster)
 */
public class RemoteSpecs implements ConfigParserMetKeys {

    LinkedList<Parser> parsers;

    LinkedList<RenamingConvention> renamingConvs;

    LinkedList<DaemonInfo> daemonInfoList;

    SiteInfo siteInfo;

    public RemoteSpecs() {
        this.parsers = new LinkedList<Parser>();
        this.renamingConvs = new LinkedList<RenamingConvention>();
        daemonInfoList = new LinkedList<DaemonInfo>();
        siteInfo = new SiteInfo();
    }

    public void loadRemoteSpecs(File remoteSpecsFile) throws ConfigException {
        try {

            Element root = XMLUtils.getDocumentRoot(
                    new FileInputStream(remoteSpecsFile)).getDocumentElement();
            NodeList aliasSpecList = root.getElementsByTagName(ALIAS_SPEC_TAG);
            for (int i = 0; i < aliasSpecList.getLength(); i++) {
                this.parseAndStoreLoginInfo(new File(PathUtils
                        .replaceEnvVariables(((Element) aliasSpecList.item(i))
                                .getAttribute(FILE_ATTR))));
            }

            // get DAEMON elements
            NodeList daemonList = root.getElementsByTagName(DAEMON_TAG);
            for (int i = 0; i < daemonList.getLength(); i++) {
                Node daemonNode = daemonList.item(i);

                // check if set to active (skip otherwise)
                if (PathUtils.replaceEnvVariables(
                        ((Element) daemonNode).getAttribute(ACTIVE_ATTR))
                        .equals("no"))
                    continue;

                DaemonInfo di = null;

                // get site alias
                String siteAlias = PathUtils
                        .replaceEnvVariables(((Element) daemonNode)
                                .getAttribute(ALIAS_ATTR));
                RemoteSite dataFilesRemoteSite = this.siteInfo
                        .getSiteByAlias(siteAlias);
                if (dataFilesRemoteSite == null)
                    throw new ConfigException("Alias '" + siteAlias
                            + "' in SiteInfo file '"
                            + remoteSpecsFile.getAbsolutePath()
                            + "' has not been defined");

                // get RUNINFO element
                NodeList runInfoList = ((Element) daemonNode)
                        .getElementsByTagName(RUN_INFO_TAG);
                String firstRunDateTimeString = null, period = null, epsilon = null;
                boolean runOnReboot = false;
                if (runInfoList.getLength() > 0) {
                    Element runInfo = (Element) runInfoList.item(0);
                    firstRunDateTimeString = runInfo
                            .getAttribute(FIRSTRUN_DATETIME_ATTR);
                    period = runInfo.getAttribute(PERIOD_ATTR);
                    runOnReboot = (runInfo.getAttribute(RUNONREBOOT_ATTR)
                            .toLowerCase().equals("yes")) ? true : false;
                    epsilon = runInfo.getAttribute(EPSILON_ATTR);
                    if (epsilon.equals(""))
                        epsilon = "0s";
                }

                // get PROPINFO elements
                NodeList propInfoList = ((Element) daemonNode)
                        .getElementsByTagName(PROP_INFO_TAG);
                LinkedList<PropFilesInfo> pfiList = new LinkedList<PropFilesInfo>();
                PropFilesInfo pfi = null;
                if (propInfoList.getLength() > 0) {
                    Node propInfoNode = propInfoList.item(0);

                    // get directory where the property files are
                    File propertyFilesDir = new File(PathUtils
                            .replaceEnvVariables(((Element) propInfoNode)
                                    .getAttribute(DIR_ATTR)));

                    pfi = new PropFilesInfo(propertyFilesDir);

                    // get PROPFILES elements
                    NodeList propFilesList = ((Element) propInfoNode)
                            .getElementsByTagName(PROP_FILES_TAG);
                    String propFilesRegExp = null;
                    if (propFilesList.getLength() > 0) {
                        for (int k = 0; k < propFilesList.getLength(); k++) {
                            Node propFilesNode = propFilesList.item(k);
                            propFilesRegExp = ((Element) propFilesNode)
                                    .getAttribute(REG_EXP_ATTR);
                            pfi
                                    .addPropFiles(
                                            propFilesRegExp,
                                            PushPullObjectFactory
                                                    .createNewInstance((Class<Parser>) Class
                                                            .forName(PathUtils
                                                                    .replaceEnvVariables(((Element) propFilesNode)
                                                                            .getAttribute(PARSER_ATTR)))));
                        }
                    } else
                        throw new ConfigException(
                                "No propFiles element specified for deamon with alias '"
                                        + siteAlias + "' in RemoteSpecs file '"
                                        + remoteSpecsFile.getAbsolutePath()
                                        + "'");

                    // get DOWNLOADINFO element if given
                    NodeList downloadInfoList = ((Element) propInfoNode)
                            .getElementsByTagName(DOWNLOAD_INFO_TAG);
                    if (downloadInfoList.getLength() > 0) {
                        Node downloadInfo = downloadInfoList.item(0);
                        String propFilesAlias = PathUtils
                                .replaceEnvVariables(((Element) downloadInfo)
                                        .getAttribute(ALIAS_ATTR));
                        String propFilesRenamingConv = ((Element) downloadInfo)
                                .getAttribute(RENAMING_CONV_ATTR);
                        boolean allowAliasOverride = PathUtils
                                .replaceEnvVariables(
                                        ((Element) downloadInfo)
                                                .getAttribute(ALLOW_ALIAS_OVERRIDE_ATTR))
                                .equals("yes");
                        boolean deleteFromServer = PathUtils
                                .replaceEnvVariables(
                                        ((Element) downloadInfo)
                                                .getAttribute(DELETE_FROM_SERVER_ATTR))
                                .equals("yes");
                        RemoteSite propFilesRemoteSite = this.siteInfo
                                .getSiteByAlias(propFilesAlias);
                        if (propFilesRemoteSite == null)
                            throw new ConfigException("Alias '"
                                    + propFilesAlias
                                    + "' in RemoteSpecs file '"
                                    + remoteSpecsFile.getAbsolutePath()
                                    + "' has not been defined");
                        String regExp = ((Element) downloadInfo)
                                .getAttribute(REG_EXP_ATTR);
                        if (regExp.equals(""))
                            regExp = propFilesRegExp;
                        NodeList propsList = ((Element) propInfoNode)
                                .getElementsByTagName(PROP_FILE_TAG);
                        HashMap<File, Parser> propFileToParserMap = new HashMap<File, Parser>();
                        for (int p = 0; p < propsList.getLength(); p++) {
                            Element propElem = (Element) propsList.item(p);
                            propFileToParserMap
                                    .put(
                                            new File(
                                                    PathUtils
                                                            .replaceEnvVariables(propElem
                                                                    .getAttribute(PATH_ATTR))),
                                            PushPullObjectFactory
                                                    .createNewInstance((Class<Parser>) Class
                                                            .forName(PathUtils
                                                                    .replaceEnvVariables(propElem
                                                                            .getAttribute(PARSER_ATTR)))));
                        }
                        pfi.setDownloadInfo(new DownloadInfo(
                                propFilesRemoteSite, propFilesRenamingConv,
                                deleteFromServer, propertyFilesDir,
                                allowAliasOverride), propFileToParserMap);
                    }

                    // get AFTERUSE element
                    NodeList afterUseList = ((Element) propInfoNode)
                            .getElementsByTagName(AFTER_USE_TAG);
                    if (afterUseList.getLength() > 0) {
                        Element afterUse = (Element) afterUseList.item(0);
                        File onSuccessDir = new File(PathUtils
                                .replaceEnvVariables(afterUse
                                        .getAttribute(MOVEON_TO_SUCCESS_ATTR)));
                        File onFailDir = new File(PathUtils
                                .replaceEnvVariables(afterUse
                                        .getAttribute(MOVEON_TO_FAIL_ATTR)));
                        pfi.setAfterUseEffects(onSuccessDir, onFailDir);
                        boolean deleteOnSuccess = Boolean.parseBoolean(PathUtils
                            .replaceEnvVariables(afterUse
                                    .getAttribute(DELETE_ON_SUCCESS_ATTR)));
                        pfi.setDeleteOnSuccess(deleteOnSuccess);
                    }

                } else
                    throw new ConfigException(
                            "No propInfo element specified for deamon with alias '"
                                    + siteAlias + "' in RemoteSpecs file '"
                                    + remoteSpecsFile.getAbsolutePath() + "'");

                // get DATAINFO elements
                NodeList dataInfoList = ((Element) daemonNode)
                        .getElementsByTagName(DATA_INFO_TAG);
                DataFilesInfo dfi = null;
                if (dataInfoList.getLength() > 0) {
                    Node dataInfo = dataInfoList.item(0);
                    String queryElement = ((Element) dataInfo)
                           .getAttribute(QUERY_ELEM_ATTR);
                    if (Strings.isNullOrEmpty(queryElement)) {
                       queryElement = null;
                    } else {
                       queryElement = PathUtils.replaceEnvVariables(queryElement);
                    }
                    String renamingConv = ((Element) dataInfo)
                            .getAttribute(RENAMING_CONV_ATTR);
                    if (Strings.isNullOrEmpty(renamingConv)) {
                       renamingConv = null;
                    }
                    boolean allowAliasOverride = PathUtils.replaceEnvVariables(
                            ((Element) dataInfo)
                                    .getAttribute(ALLOW_ALIAS_OVERRIDE_ATTR))
                            .equals("yes");
                    File stagingArea = new File(PathUtils
                            .replaceEnvVariables(((Element) dataInfo)
                                    .getAttribute(STAGING_AREA_ATTR)));
                    boolean deleteFromServer = PathUtils.replaceEnvVariables(
                            ((Element) dataInfo)
                                    .getAttribute(DELETE_FROM_SERVER_ATTR))
                            .equals("yes");
                    dfi = new DataFilesInfo(queryElement, new DownloadInfo(
                            dataFilesRemoteSite, renamingConv,
                            deleteFromServer, stagingArea, allowAliasOverride));
                } else
                    throw new ConfigException(
                            "No dataInfo element specified for deamon with alias '"
                                    + siteAlias + "' in RemoteSpecs file '"
                                    + remoteSpecsFile.getAbsolutePath() + "'");

                daemonInfoList.add(new DaemonInfo(firstRunDateTimeString,
                        period, epsilon, runOnReboot, pfi, dfi));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigException("Failed to load crawl elements : "
                    + e.getMessage());
        }
    }

    void parseAndStoreLoginInfo(File loginInfoFile) throws ConfigException {
        try {
            NodeList sourceList = XMLUtils.getDocumentRoot(new FileInputStream(loginInfoFile))
                    .getElementsByTagName(SOURCE_TAG);
            for (int i = 0; i < sourceList.getLength(); i++) {

                // get source element
                Node sourceNode = sourceList.item(i);

                // get host of this source
                String host = PathUtils
                        .replaceEnvVariables(((Element) sourceNode)
                                .getAttribute(HOST_ATTR));

                // get all login info for this source
                NodeList loginList = ((Element) sourceNode)
                        .getElementsByTagName(LOGIN_ATTR);
                for (int j = 0; j < loginList.getLength(); j++) {

                    // get a single login info
                    Node loginNode = loginList.item(j);
                    String type = PathUtils
                            .replaceEnvVariables(((Element) loginNode)
                                    .getAttribute(TYPE_ATTR));
                    String alias = PathUtils
                            .replaceEnvVariables(((Element) loginNode)
                                    .getAttribute(ALIAS_ATTR));
                    String username = null, password = null, cdTestDir = null;
                    int maxConnections = -1;

                    // parse this login info
                    NodeList loginInfo = loginNode.getChildNodes();
                    for (int k = 0; k < loginInfo.getLength(); k++) {

                        // get a single login info element
                        Node node = loginInfo.item(k);

                        // determine what element type it is
                        if (node.getNodeName().equals(USERNAME_TAG)) {
                            username = PathUtils.replaceEnvVariables(
                                    XMLUtils.getSimpleElementText((Element) node, true));
                        } else if (node.getNodeName().equals(PASSWORD_TAG)) {
                            password = PathUtils.replaceEnvVariables(
                                    XMLUtils.getSimpleElementText((Element) node, true));
                        } else if (node.getNodeName().equals(CD_TEST_DIR_TAG)) {
                            cdTestDir = PathUtils.replaceEnvVariables(
                                    XMLUtils.getSimpleElementText((Element) node, true));
                        } else if (node.getNodeName().equals(MAX_CONN_TAG)) {
                            maxConnections = Integer.parseInt(PathUtils.replaceEnvVariables(
                                    XMLUtils.getSimpleElementText((Element) node, true)));
                        }
                    }

                    this.siteInfo.addSite(new RemoteSite(alias, new URL(type
                            + "://" + host), username, password, cdTestDir, maxConnections));
                }
            }
        } catch (Exception e) {
            throw new ConfigException("Failed to load external source info : "
                    + e.getMessage(), e);
        }
    }

    public LinkedList<DaemonInfo> getDaemonInfoList() {
        return this.daemonInfoList;
    }

    public SiteInfo getSiteInfo() {
        return this.siteInfo;
    }

}
