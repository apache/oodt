//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.ftp.jvftp;

//JDK imports
import java.io.File;
import java.util.LinkedList;
import java.util.List;

//Jvftp imports
import cz.dhl.ftp.Ftp;
import cz.dhl.ftp.FtpFile;
import cz.dhl.io.CoFile;
import cz.dhl.io.CoLoad;
import cz.dhl.io.LocalFile;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ProtocolException;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.Protocol;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFile;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolPath;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class FtpClient_jvftp extends Protocol {

    private Ftp ftp;

    protected FtpClient_jvftp() {
        super("ftp");
        ftp = new Ftp();
        ftp.getContext().setConsole(null);
    }

    @Override
    public void abortCurFileTransfer() throws ProtocolException {
        // do nothing
    }

    @Override
    protected void chDir(ProtocolPath path) throws ProtocolException {
        if (!ftp.cd(path.getPathString()))
            throw new ProtocolException("Failed to cd to " + path);
    }

    public void cdToRoot() throws ProtocolException {
        try {
            chDir(new ProtocolPath("/", true));
        } catch (Exception e) {
            throw new ProtocolException("Failed to cd to root : "
                    + e.getMessage());
        }
    }

    @Override
    protected void connect(String host, String username, String password)
            throws ProtocolException {
        try {
            if (ftp.connect(host, 21)) {
                if (!ftp.login(username, password)) {
                    throw new ProtocolException("Failed to login as user "
                            + username);
                }
            } else {
                throw new ProtocolException("Failed to connect at port 21");
            }
        } catch (Exception e) {
            throw new ProtocolException("Unsuccessful connect to " + host
                    + " : " + e.getMessage());
        }
    }

    @Override
    public void disconnectFromServer() throws ProtocolException {
        ftp.disconnect();
    }

    @Override
    protected ProtocolFile getCurrentWorkingDir() throws ProtocolException {
        try {
            return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(ftp
                    .pwd(), true));
        } catch (Exception e) {
            throw new ProtocolException(
                    "Failed to get current working directory : "
                            + e.getMessage());
        }
    }

    @Override
    public void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException {
        try {
            CoFile downloadFile = new FtpFile(file.getProtocolPath()
                    .getPathString(), ftp);
            CoFile to = new LocalFile(toLocalFile.getParentFile()
                    .getAbsolutePath(), toLocalFile.getName());
            if (!CoLoad.copy(to, downloadFile))
                throw new ProtocolException("Download returned false");
        } catch (Exception e) {
            throw new ProtocolException("Failed to download file " + file
                    + " : " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() throws ProtocolException {
        return ftp.isConnected();
    }

    @Override
    public List<ProtocolFile> listFiles() throws ProtocolException {
        LinkedList<ProtocolFile> returnList = new LinkedList<ProtocolFile>();
        try {
            CoFile dir = new FtpFile(ftp.pwd(), ftp);
            CoFile fls[] = dir.listCoFiles();
            for (CoFile file : fls) {
                returnList.add(new ProtocolFile(this.getRemoteSite(),
                        new ProtocolPath(file.getAbsolutePath(), file
                                .isDirectory())));
            }
            return returnList;
        } catch (Exception e) {
            throw new ProtocolException("Failed to ls : " + e.getMessage());
        }
    }

    @Override
    protected boolean deleteFile(ProtocolFile file) {
        try {
            return ftp.rm(file.getProtocolPath().getPathString());
        } catch (Exception e) {
            return false;
        }
    }

}
