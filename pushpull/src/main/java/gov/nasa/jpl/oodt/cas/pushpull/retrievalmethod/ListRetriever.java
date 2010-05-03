//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalmethod;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.pushpull.config.DataFilesInfo;
import gov.nasa.jpl.oodt.cas.pushpull.config.DownloadInfo;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.AlreadyInDatabaseException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.RetrievalMethodException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ToManyFailedDownloadsException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.UndefinedTypeException;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.FileRestrictions;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.Parser;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.RemoteSite;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem.DataFileToPropFileLinker;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem.FileRetrievalSystem;

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
        VirtualFileStructure vfs = propFileParser.parse(new FileInputStream(
                propFile));
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
                        .getQueryMetadataElementName(), di.deleteFromServer()))
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
