//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.config;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.RemoteSite;

//JDK imports
import java.io.File;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DownloadInfo {

    private RemoteSite remoteSite;

    private String renamingConv;

    private boolean deleteFromServer;

    private File stagingArea;

    private boolean allowAliasOverride;

    public DownloadInfo(RemoteSite remoteSite, String renamingConv,
            boolean deleteFromServer, File stagingArea,
            boolean allowAliasOverride) {
        this.remoteSite = remoteSite;
        this.renamingConv = renamingConv;
        this.deleteFromServer = deleteFromServer;
        this.stagingArea = stagingArea;
        this.allowAliasOverride = allowAliasOverride;
    }

    public RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public String getRenamingConv() {
        return this.renamingConv;
    }

    public boolean deleteFromServer() {
        return this.deleteFromServer;
    }

    public File getStagingArea() {
        return this.stagingArea;
    }

    public boolean isAllowAliasOverride() {
        return this.allowAliasOverride;
    }

    public String toString() {
        return "DataFileInfo:\n" + "  " + this.remoteSite + "\n" + "  "
                + "RenamingConvension: " + this.renamingConv + "\n" + "  "
                + "StagingArea: " + this.getStagingArea().getAbsolutePath()
                + "\n" + "  " + "Delete files from server: "
                + this.deleteFromServer + "\n" + "  "
                + "Allow alias override: " + this.allowAliasOverride + "\n";
    }
}
