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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.pushpull.config.DataFilesInfo;
import org.apache.oodt.cas.pushpull.config.DownloadInfo;
import org.apache.oodt.cas.pushpull.exceptions.AlreadyInDatabaseException;
import org.apache.oodt.cas.pushpull.exceptions.RetrievalMethodException;
import org.apache.oodt.cas.pushpull.exceptions.ToManyFailedDownloadsException;
import org.apache.oodt.cas.pushpull.exceptions.UndefinedTypeException;
import org.apache.oodt.cas.pushpull.filerestrictions.FileRestrictions;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;
import org.apache.oodt.cas.pushpull.retrievalsystem.DataFileToPropFileLinker;
import org.apache.oodt.cas.pushpull.retrievalsystem.FileRetrievalSystem;


//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
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
public class ListRetriever implements RetrievalMethod {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(ListRetriever.class
            .getName());

    public void processPropFile(FileRetrievalSystem frs, Parser propFileParser,
            File propFile, DataFilesInfo dfi, DataFileToPropFileLinker linker)
            throws Exception {
        RemoteSite remoteSite = null;

        // parse property file
        Metadata fileMetadata = new Metadata();
        VirtualFileStructure vfs = propFileParser.parse(new FileInputStream(
                propFile), fileMetadata);
        DownloadInfo di = dfi.getDownloadInfo();
        if (!di.isAllowAliasOverride()
                || (remoteSite = vfs.getRemoteSite()) == null)
            remoteSite = di.getRemoteSite();
        LinkedList<String> fileList = FileRestrictions.toStringList(vfs
                .getRootVirtualFile());

        // download data files specified in property file
        for (String file : fileList) {
            try {
                linker.addPropFileToDataFileLink(propFile, file);
                if (!frs.addToDownloadQueue(remoteSite, file, di
                        .getRenamingConv(), di.getStagingArea(), dfi
                        .getQueryMetadataElementName(), di.deleteFromServer(), fileMetadata))
                    linker.eraseLinks(propFile);
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
                e.printStackTrace();
                linker.markAsFailed(propFile, "Failed to download " + file
                        + " from " + remoteSite + " : " + e.getMessage());
                throw new Exception("Uknown error accured while downloading "
                        + file + " from " + remoteSite + " -- bailing out : "
                        + e.getMessage());
            }
        }
    }

}
