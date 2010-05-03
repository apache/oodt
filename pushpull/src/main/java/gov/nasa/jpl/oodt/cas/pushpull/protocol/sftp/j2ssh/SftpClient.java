//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.sftp.j2ssh;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ProtocolException;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolPath;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.Protocol;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFile;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

//SSH tools imports
import com.sshtools.j2ssh.FileTransferProgress;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class SftpClient extends Protocol {

    SshClient ssh;

    com.sshtools.j2ssh.SftpClient sftp;
    
    boolean abort;

    public SftpClient() {
    	super("sftp");
        ssh = new SshClient();
    }

    protected void chDir(ProtocolPath path)
            throws ProtocolException {
        try {
            sftp.cd(path.getPathString());
        } catch (Exception e) {
            throw new ProtocolException("Failed to cd to " + path + " : " + e.getMessage());
        }
    }
    
    public void cdToRoot() throws ProtocolException {
    	try {
    		chDir(new ProtocolPath("/", true));
    	}catch (Exception e) {
    		throw new ProtocolException("Failed to change to root directory : " + e.getMessage());
    	}
    }

    public void connect(String host, String username, String password) throws ProtocolException {
        try {
            System.out.println("CONNECTING SFTP!!!!");
            ssh.connect(host, new IgnoreHostKeyVerification());
        } catch (Exception e) {
            throw new ProtocolException("Failed to connect to " + host + " : " + e.getMessage());
        }
        try {
            PasswordAuthenticationClient pac = new PasswordAuthenticationClient();
            pac.setUsername(username);
            pac.setPassword(password);

            if (ssh.authenticate(pac) != AuthenticationProtocolState.COMPLETE)
                throw new Exception();

            sftp = ssh.openSftpClient();
        } catch (Exception e) {
            throw new ProtocolException("Failed to login to " + host + " : " + e.getMessage());
        }
    }

    public void disconnectFromServer() throws ProtocolException {
        try {
            System.out.println("DISCONNECTING SFTP!!!!");
			sftp.quit();
	        ssh.disconnect();
		} catch (Exception e) {
			throw new ProtocolException("Failed to disconnect from " + this.getRemoteSite().getURL() + " : " + e.getMessage());
		}
    }

    public void getFile(final ProtocolFile file, File toLocalFile) throws ProtocolException {
        try {
        	this.abort = false;
            final FileOutputStream fos = new FileOutputStream(toLocalFile);
            FileTransferProgress progress = new FileTransferProgress() {

                public void completed() {
                    System.out.println("Finishing Download of " + file);
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        System.out.println("Failed to close " + file);
                    }
                }

                public boolean isCancelled() {
                    return abort;
                }

                public void progressed(long arg0) {

                }

                public void started(long arg0, String arg1) {
                    System.out.println("Starting download of " + file);
                }

            };
            FileAttributes fa = sftp.get(file.getProtocolPath().getPathString(),
            		fos, progress);// toLocalFile.getAbsolutePath(),
                                                            // progress);
        } catch (Exception e) {
            toLocalFile.delete();
            throw new ProtocolException("Failed to download " + file.getName() + " : " + e.getMessage());
        }
    }
    
    public void abortCurFileTransfer() {
    	this.abort = true;
    }

    public List<ProtocolFile> listFiles()
            throws ProtocolException {
        try {
            Vector<com.sshtools.j2ssh.sftp.SftpFile> files = (Vector<com.sshtools.j2ssh.sftp.SftpFile>) sftp
                    .ls();
            Vector<ProtocolFile> returnFiles = new Vector<ProtocolFile>();
            for (com.sshtools.j2ssh.sftp.SftpFile file : files) {
            	String path = this.pwd().getProtocolPath().getPathString();
				returnFiles.add(new ProtocolFile(this.getRemoteSite(), new ProtocolPath(
						path + "/" + file.getFilename(), file.isDirectory())));
            }
            return returnFiles;
        } catch (Exception e) {
            throw new ProtocolException("Failed to get file list : " + e.getMessage());
        }   
    }

    public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
    	try {
    		return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(sftp.pwd(), true));
    	}catch (Exception e) {
    		throw new ProtocolException("Failed to pwd : " + e.getMessage());
    	}
    }

    public boolean isConnected() throws ProtocolException {
        return ssh.isConnected();
    }

	@Override
	protected boolean deleteFile(ProtocolFile file) {
		try {
			sftp.rm(file.getProtocolPath().getPathString());
			return true;
		}catch (Exception e) {
			return false;
		}
	}

}
