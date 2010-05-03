//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalmethod;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.pushpull.config.DataFilesInfo;
import gov.nasa.jpl.oodt.cas.pushpull.config.DownloadInfo;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.AlreadyInDatabaseException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ProtocolFileException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.RetrievalMethodException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ToManyFailedDownloadsException;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.UndefinedTypeException;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.FileRestrictions;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.Parser;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFile;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFile;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFileFilter;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.RemoteSite;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem.DataFileToPropFileLinker;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem.FileRetrievalSystem;

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
