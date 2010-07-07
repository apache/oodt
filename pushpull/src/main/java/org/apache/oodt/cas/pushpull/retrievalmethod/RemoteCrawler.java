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


package org.apache.oodt.cas.pushpull.retrievalmethod;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.pushpull.config.DataFilesInfo;
import org.apache.oodt.cas.pushpull.config.DownloadInfo;
import org.apache.oodt.cas.pushpull.exceptions.AlreadyInDatabaseException;
import org.apache.oodt.cas.pushpull.exceptions.ProtocolFileException;
import org.apache.oodt.cas.pushpull.exceptions.RetrievalMethodException;
import org.apache.oodt.cas.pushpull.exceptions.ToManyFailedDownloadsException;
import org.apache.oodt.cas.pushpull.exceptions.UndefinedTypeException;
import org.apache.oodt.cas.pushpull.filerestrictions.FileRestrictions;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFile;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFileFilter;
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;
import org.apache.oodt.cas.pushpull.retrievalsystem.DataFileToPropFileLinker;
import org.apache.oodt.cas.pushpull.retrievalsystem.FileRetrievalSystem;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class RemoteCrawler implements RetrievalMethod {

    private static final Logger LOG = Logger.getLogger(RemoteCrawler.class
            .getName());

    /**
     * Starts the crawler and creates a default DirStruct if null was supplied
     * in constructor
     * 
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws ProtocolFileException
     */
    public void processPropFile(FileRetrievalSystem frs, Parser propFileParser,
            File propFile, DataFilesInfo dfi, DataFileToPropFileLinker linker)
            throws Exception {
        RemoteSite remoteSite;

        // parse property file
        VirtualFileStructure vfs = propFileParser.parse(new FileInputStream(
                propFile));

        // determine RemoteSite
        DownloadInfo di = dfi.getDownloadInfo();
        if (!di.isAllowAliasOverride()
                || (remoteSite = vfs.getRemoteSite()) == null)
            remoteSite = di.getRemoteSite();

        // modify vfs to be root based if HOME directory based
        if (!vfs.isRootBased()) {
            String homeDirPath = frs.getHomeDir(remoteSite).getProtocolPath()
                    .getPathString();
            VirtualFile root = new VirtualFile(homeDirPath, true);
            root.addChild(vfs.getRootVirtualFile());
            vfs = new VirtualFileStructure(homeDirPath + "/"
                    + vfs.getPathToRoot(), root.getRootDir());
            frs.changeToHOME(remoteSite);
        }

        // initialize variables
        final String initialCdPath = vfs.getPathToRoot();
        final VirtualFile vf = vfs.getRootVirtualFile();

        // change to initial directory (takes care of Linux auto-mounting)
        frs.changeToDir(initialCdPath, remoteSite);

        // add starting directory to stack
        Stack<ProtocolFile> files = new Stack<ProtocolFile>();
        files.add(frs.getCurrentFile(remoteSite));

        // start crawling
        while (!files.isEmpty()) {
            ProtocolFile file = files.peek();
            try {
                // if directory, then add its children to the crawl list
                if (file.isDirectory()) {

                    // get next page worth of children
                    List<ProtocolFile> children = frs.getNextPage(file,
                            new ProtocolFileFilter() {
                                public boolean accept(ProtocolFile pFile) {
                                    return FileRestrictions.isAllowed(pFile
                                            .getProtocolPath(), vf);
                                }
                            });

                    // if directory had more children then add them
                    if (children.size() > 0)
                        files.addAll(children);
                    // otherwise remove the directory from the crawl list
                    else
                        files.pop();

                    // if file, then download it
                } else {
                    linker.addPropFileToDataFileLink(propFile, file);
                    if (!frs.addToDownloadQueue(files.pop(), di
                            .getRenamingConv(), di.getStagingArea(), dfi
                            .getQueryMetadataElementName(), di
                            .deleteFromServer()))
                        linker.eraseLinks(propFile);
                }

            } catch (ToManyFailedDownloadsException e) {
                throw new RetrievalMethodException(
                        "Connection appears to be down. . .unusual number of download failures. . .stopping : "
                                + e.getMessage());
            } catch (CatalogException e) {
                throw new RetrievalMethodException(
                        "Failed to communicate with database : "
                                + e.getMessage());
            } catch (AlreadyInDatabaseException e) {
                LOG.log(Level.WARNING, "Skipping file : " + e.getMessage());
            } catch (UndefinedTypeException e) {
                LOG.log(Level.WARNING, "Skipping file : " + e.getMessage());
            } catch (Exception e) {
                linker.markAsFailed(propFile, e.getMessage());
                throw new Exception("Uknown error accured while downloading "
                        + file + " from " + remoteSite + " -- bailing out : "
                        + e.getMessage());
            }
        }
    }
}
