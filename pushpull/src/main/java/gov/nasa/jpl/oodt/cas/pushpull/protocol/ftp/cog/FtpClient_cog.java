//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.ftp.cog;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.Vector;

//Globus imports
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;

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
public class FtpClient_cog extends Protocol {

    private FTPClient ftp;

    private boolean isConnected;

    public FtpClient_cog() {
        super("ftp");
    }

    protected void chDir(ProtocolPath path) throws ProtocolException {
        try {
            ftp.changeDir(path.getPathString());
        } catch (Exception e) {
            throw new ProtocolException("Failed to cd to " + path + " : "
                    + e.getMessage());
        }
    }

    @Override
    public void cdToRoot() throws ProtocolException {
        try {
            chDir(new ProtocolPath("/", true));
        } catch (Exception e) {
            throw new ProtocolException("Failed to cd to root : "
                    + e.getMessage());
        }
    }

    public void connect(String host, String username, String password)
            throws ProtocolException {
        try {
            ftp = new FTPClient(host, 21);
        } catch (Exception e) {
            throw new ProtocolException("Failed to connect to: " + host + " : "
                    + e.getMessage());
        }
        isConnected = true;

        try {
            ftp.authorize(username, password);
            ftp.setActive(ftp.setLocalPassive());
        } catch (Exception e) {
            throw new ProtocolException("Failed to login to: " + host + " : "
                    + e.getMessage());
        }
    }

    public void disconnectFromServer() throws ProtocolException {
        try {
            ftp.close();
            isConnected = false;
        } catch (Exception e) {
            throw new ProtocolException("Error disconnecting from "
                    + this.getRemoteSite().getURL() + " : " + e.getMessage());
        }
    }

    public void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException {
        try {
            ftp.setActive(ftp.setLocalPassive());
            ftp.get(file.getProtocolPath().getPathString(), toLocalFile);
        } catch (Exception e) {
            throw new ProtocolException("Failed to download: " + file.getName()
                    + " : " + e.getMessage());
        }
    }

    public void abortCurFileTransfer() throws ProtocolException {
        try {
            ftp.abort();
        } catch (Exception e) {
            throw new ProtocolException("Failed to abort file transfer : "
                    + e.getMessage());
        }
    }

    public List<ProtocolFile> listFiles() throws ProtocolException {
        try {
            ftp.setActive(ftp.setLocalPassive());
            Vector<FileInfo> fileList = (Vector<FileInfo>) ftp.list("*", null);
            Vector<ProtocolFile> returnList = new Vector<ProtocolFile>();
            String path = this.pwd().getProtocolPath().getPathString();
            for (FileInfo file : fileList) {
                returnList.add(new ProtocolFile(this.getRemoteSite(),
                        new ProtocolPath(path + "/" + file.getName(), file
                                .isDirectory())));
            }
            return returnList;
        } catch (Exception e) {
            throw new ProtocolException("Failed to get list of files : "
                    + e.getMessage());
        }
    }

    public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
        try {
            return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(ftp
                    .getCurrentDir(), true));
        } catch (Exception e) {
            throw new ProtocolException("Failed to pwd : " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void sendNoOP() {
        try {
            // ftp.getCurrentDir();
        } catch (Exception e) {
        }
    }

    @Override
    protected boolean deleteFile(ProtocolFile file) {
        try {
            ftp.deleteFile(file.getProtocolPath().getPathString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
