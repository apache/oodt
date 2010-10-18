/**
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

package org.apache.oodt.cas.pushpull.protocol.ftp;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;
import org.apache.oodt.cas.pushpull.protocol.Protocol;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

//APACHE imports
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * This class is responsible for FTP transfers. It is built as a wrapper around
 * Apache's FTPClient class in order to connect it into the Crawler's Protocol
 * infrastructure.
 * 
 * @author bfoster
 * 
 */
public class CommonsNetFtpClient extends Protocol {

    private FTPClient ftp;

    /**
     * Creates a new FtpClient
     */
    public CommonsNetFtpClient() {
        super("ftp");
        ftp = new FTPClient();
    }

    /**
     * {@inheritDoc}
     */
    public void connect(String host, String username, String password)
            throws ProtocolException {
        // server cannot be null
        if (host == null) {
            throw new ProtocolException("Tried to connect to server == NULL");
        }

        try {
            ftp.connect(host);
            ftp.enterLocalPassiveMode();
        } catch (Exception e) {
            throw new ProtocolException("Failed to connect to server : "
                    + e.getMessage());
        }

        try {
            // try logging in
            if (!ftp.login(username, password)) {
                throw new ProtocolException("Failed logging into host " + host
                        + " as user " + username);
            }

            // set file type to binary
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

        } catch (Exception e) {
            // login failed
            throw new ProtocolException(
                    "Exception thrown while logging into host " + host
                            + " as user " + username);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
        try {
            return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(ftp
                    .printWorkingDirectory(), true));
        } catch (Exception e) {
            throw new ProtocolException("Failed to pwd : " + e.getMessage());
        }
    }

    public List<ProtocolFile> listFiles() throws ProtocolException {
        try {
            FTPFile[] files = ftp.listFiles();
            List<ProtocolFile> returnFiles = new LinkedList<ProtocolFile>();
            for (int i = 0; i < files.length; i++) {
                FTPFile file = files[i];
                if (file == null)
                    continue;
                String path = this.pwd().getURL().getPath();
                returnFiles.add(new ProtocolFile(this.getRemoteSite(),
                        new ProtocolPath(path + "/" + file.getName(), file
                                .isDirectory())));
            }
            // System.out.println("RETURN FILES: " + returnFiles);
            return returnFiles;
        } catch (Exception e) {
            throw new ProtocolException("Failed to get file list : "
                    + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException {
        // file or toLocalFile cannot be null
        if (file == null || toLocalFile == null) {
            throw new ProtocolException(
                    "Can't download file -> ProtocolFile == null || toLocalFile == null");
        }

        // download file
        OutputStream os = null;
        try {
            os = new FileOutputStream(toLocalFile);
            if (ftp.retrieveFile(file.getName(), os))// {
                throw new ProtocolException("Failed to download file "
                        + file.getName());
            // }
        } catch (Exception e) {
            // download failed
            toLocalFile.delete();
            throw new ProtocolException("FAILED to download: " + file.getName()
                    + " : " + e.getMessage());
        } finally {
            // close output stream
            if (os != null)
                try {
                    os.close();
                } catch (Exception e) {
                    toLocalFile.delete();
                    throw new ProtocolException(
                            "Failed to close outputstream : " + e.getMessage());
                }
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

    /**
     * {@inheritDoc}
     */
    protected void chDir(ProtocolPath path) throws ProtocolException {
        try {
            if (!ftp.changeWorkingDirectory(path.getPathString()))
                throw new Exception("Directory change method returned false");
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

    /**
     * {@inheritDoc}
     */
    public void sendNoOP() throws ProtocolException {
        try {
            ftp.noop();
        } catch (Exception e) {
            throw new ProtocolException("Failed to send NoOP command");
        }
    }

    @Override
    public void disconnectFromServer() throws ProtocolException {
        try {
            ftp.disconnect();
        } catch (IOException e) {
            throw new ProtocolException("Failed to disconnect from "
                    + this.getRemoteSite().getURL());
        }
    }

    @Override
    public boolean isConnected() throws ProtocolException {
        return ftp.isConnected();
    }

    @Override
    protected boolean deleteFile(ProtocolFile file) {
        try {
            return ftp.deleteFile(file.getProtocolPath().getPathString());
        } catch (Exception e) {
            return false;
        }
    }

}