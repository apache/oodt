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


package org.apache.oodt.cas.pushpull.retrievalsystem;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.pushpull.config.Config;
import org.apache.oodt.cas.pushpull.config.DataFilesInfo;
import org.apache.oodt.cas.pushpull.config.SiteInfo;
import org.apache.oodt.cas.pushpull.config.PropFilesInfo;
import org.apache.oodt.cas.pushpull.config.ProtocolInfo;
import org.apache.oodt.cas.pushpull.exceptions.ParserException;
import org.apache.oodt.cas.pushpull.exceptions.RetrievalMethodException;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.objectfactory.PushPullObjectFactory;
import org.apache.oodt.cas.pushpull.retrievalmethod.RetrievalMethod;
import org.apache.oodt.cas.pushpull.retrievalsystem.FileRetrievalSystem;

/**
 *
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Describe your class here
 * </p>.
 */
public class RetrievalSetup {

    private final Config config;

    private final HashSet<File> alreadyProcessedPropFiles;

    private final HashMap<Class<RetrievalMethod>, RetrievalMethod> classToRmMap;

    private boolean downloadingProps;

    private final SiteInfo siteInfo;

    private final DataFileToPropFileLinker linker;

    private final static Logger LOG = Logger.getLogger(RetrievalSetup.class
            .getName());

    public RetrievalSetup(Config config, SiteInfo siteInfo) {
        this.downloadingProps = false;
        this.config = config;
        this.siteInfo = siteInfo;
        alreadyProcessedPropFiles = new HashSet<File>();
        classToRmMap = new HashMap<Class<RetrievalMethod>, RetrievalMethod>();
        linker = new DataFileToPropFileLinker();
    }

    public void retrieveFiles(PropFilesInfo pfi, final DataFilesInfo dfi)
            throws RetrievalMethodException {

        FileRetrievalSystem dataFilesFRS = null;
        try {
            this.startPropFileDownload(pfi);

            (dataFilesFRS = new FileRetrievalSystem(config, siteInfo))
                    .initialize();
            dataFilesFRS.registerDownloadListener(linker);

            File[] propFiles = null;
            while ((propFiles = getCurrentlyDownloadedPropFiles(pfi)).length > 0
                    || downloadingProps) {
                for (File propFile : propFiles) {
                    try {
                        Parser parser = pfi.getParserForFile(propFile);
                        Class<RetrievalMethod> rmClass = config.getParserInfo()
                                .getRetrievalMethod(parser);
                        RetrievalMethod rm = null;
                        if ((rm = this.classToRmMap.get(rmClass)) == null) {
                            LOG.log(Level.INFO, "Creating '"
                                    + rmClass.getCanonicalName()
                                    + "' to download data files");
                            rm = PushPullObjectFactory
                                    .createNewInstance(rmClass);
                            this.classToRmMap.put(rmClass, rm);
                        }
                        rm.processPropFile(dataFilesFRS, parser, propFile, dfi,
                                linker);
                    } catch (ParserException e) {
                        LOG.log(Level.SEVERE, "Failed to parse property file "
                                + propFile + " : " + e.getMessage(), e);
                        linker.markAsFailed(propFile,
                                "Failed to parse property file " + propFile
                                        + " : " + e.getMessage());
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE,
                                "Failed to finish downloading per property files "
                                        + propFile.getAbsolutePath() + " : "
                                        + e.getMessage(), e);
                        linker.markAsFailed(propFile,
                                "Error while downloading per property file "
                                        + propFile.getAbsolutePath() + " : "
                                        + e.getMessage());
                    }
                }

                dataFilesFRS.waitUntilAllCurrentDownloadsAreComplete();

                for (File propFile : propFiles) {
                    try {
                        if (pfi.getLocalDir().equals(pfi.getOnSuccessDir())
                                || pfi.getLocalDir().equals(pfi.getOnFailDir()))
                            alreadyProcessedPropFiles.add(propFile);
                        this.movePropsFileToFinalDestination(pfi, propFile,
                                linker.getErrorsAndEraseLinks(propFile));
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.log(Level.SEVERE,
                                "Error occurred while writing errors to error dir for file '"
                                        + propFile + "' : " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dataFilesFRS != null)
                dataFilesFRS.shutdown();
            alreadyProcessedPropFiles.clear();
            linker.clear();
        }
    }

    private void startPropFileDownload(final PropFilesInfo pfi)
            throws IOException {
        if (pfi.needsToBeDownloaded()) {
            this.downloadingProps = true;
            new Thread(new Runnable() {

                @Override
               public void run() {
                    FileRetrievalSystem frs = null;
                    try {
                        (frs = new FileRetrievalSystem(
                                RetrievalSetup.this
                                        .createPropFilesConfig(config
                                                .getProtocolInfo()), siteInfo))
                                .initialize();

                        LinkedList<File> propDirStructFiles = pfi
                                .getDownloadInfoPropFiles();
                        for (File dirStructFile : propDirStructFiles) {
                            Parser parser = pfi.getParserForFile(dirStructFile);
                            Class<RetrievalMethod> rmClass = config
                                    .getParserInfo().getRetrievalMethod(parser);
                            RetrievalMethod rm = null;
                            if ((rm = RetrievalSetup.this.classToRmMap
                                    .get(rmClass)) == null) {
                                LOG.log(Level.INFO, "Creating '"
                                        + rmClass.getCanonicalName()
                                        + "' to download property files");
                                rm = PushPullObjectFactory
                                        .createNewInstance(rmClass);
                                RetrievalSetup.this.classToRmMap.put(rmClass,
                                        rm);
                            }
                            rm.processPropFile(frs, parser, dirStructFile,
                                    new DataFilesInfo(null, pfi
                                            .getDownloadInfo()), linker);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (frs != null)
                            frs.shutdown();
                        RetrievalSetup.this.downloadingProps = false;
                    }
                }

            }).start();

            // give property file download a 5 sec head start
            LOG
                    .log(
                            Level.INFO,
                            "Waiting data download thread for 5 secs to give the property files download thread a head start");
            synchronized (this) {
                try {
                    this.wait(5000);
                } catch (Exception e) {
                }
            }
        }
    }

    private File[] getCurrentlyDownloadedPropFiles(final PropFilesInfo pfi) {
        File[] files = pfi.getLocalDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pfi.getParserForFile(pathname) != null
                        && !(pathname.getName().startsWith("Downloading_")
                                || pathname.getName().endsWith(
                                        RetrievalSetup.this.config
                                                .getMetFileExtension()) || alreadyProcessedPropFiles
                                .contains(pathname));
            }
        });
        return files == null ? new File[0] : files;
    }

    private Config createPropFilesConfig(ProtocolInfo pi) {
        Config propConfig = this.config.clone();
        ProtocolInfo propPI = pi.clone();
        propPI.setPageSize(-1);
        propConfig.setProtocolInfo(propPI);
        propConfig.setUseTracker(false);
        propConfig.setIngester(null);
        propConfig.setOnlyDownloadDefinedTypes(false);
        return propConfig;
    }

    private void movePropsFileToFinalDestination(PropFilesInfo pfi,
            File dirstructFile, String errorMsgs) throws IOException {
        File moveToDir = pfi.getFinalDestination(errorMsgs == null);
        moveToDir.mkdirs();
        File newLoc = new File(moveToDir, dirstructFile.getName());
        dirstructFile.renameTo(newLoc);
        new File(dirstructFile.getAbsolutePath() + "." + config.getMetFileExtension())
                .renameTo(new File(newLoc.getAbsolutePath() + "."
                		+ config.getMetFileExtension()));
        if (errorMsgs != null) {
            File errorFile = new File(newLoc.getParentFile(), dirstructFile
                    .getName()
                    + ".errors");
            errorFile.createNewFile();
            PrintStream ps = new PrintStream(new FileOutputStream(errorFile));
            ps.print(errorMsgs);
            ps.println();
            ps.close();
        }
    }

}
