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


package org.apache.oodt.cas.pushpull.protocol.ftp.ftp4che;

//JDK imports
import java.io.File;
import java.util.LinkedList;
import java.util.List;

//FTP4CHE imports
import org.ftp4che.FTPConnection;
import org.ftp4che.FTPConnectionFactory;
import org.ftp4che.util.ftpfile.FTPFile;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.protocol.Protocol;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class FtpClient_ftp4che extends Protocol {

    FTPConnection ftp;

    protected FtpClient_ftp4che() {
        super("ftp");
    }

    @Override
    protected void chDir(ProtocolPath path) throws ProtocolException {
        try {
            ftp.changeDirectory(path.getPathString());
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

    @Override
    protected void connect(String host, String username, String password)
            throws ProtocolException {
        try {
            ftp = FTPConnectionFactory.getInstance(host, 21, username,
                    password, null, 10000, FTPConnection.FTP_CONNECTION, true);
            ftp.connect();
        } catch (Exception e) {
            throw new ProtocolException("Failed to connect to " + host + " : "
                    + e.getMessage());
        }
    }

    @Override
    public void disconnectFromServer() throws ProtocolException {
        ftp.disconnect();
    }

    @Override
    public void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException {
        try {
            ftp.downloadFile(
                    new FTPFile(file.getProtocolPath().getPathString(), file
                            .isDirectory()), new FTPFile(toLocalFile));
        } catch (Exception e) {
            throw new ProtocolException("Failed to download " + file + " : "
                    + e.getMessage());
        }
    }

    public void abortCurFileTransfer() {
        ftp.disconnect();
    }

    public List<ProtocolFile> listFiles() throws ProtocolException {
        try {
            List<FTPFile> fileList = ftp.getDirectoryListing();
            List<ProtocolFile> returnList = new LinkedList<ProtocolFile>();
            for (int i = 0; i < fileList.size(); i++) {
                FTPFile file = fileList.get(i);
                String path = this.pwd().getProtocolPath().getPathString();
                returnList.add(new ProtocolFile(this.getRemoteSite(),
                        new ProtocolPath(path + "/" + file.getName(), file
                                .isDirectory())));
            }
            return returnList;
        } catch (Exception e) {
            throw new ProtocolException("Failed to get file list : "
                    + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() throws ProtocolException {
        return ftp.getConnectionStatus() == ftp.CONNECTED;
    }

    @Override
    public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
        try {
            return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(ftp
                    .getWorkDirectory(), true));
        } catch (Exception e) {
            throw new ProtocolException("pwd command failed : "
                    + e.getMessage());
        }
    }

    @Override
    protected boolean deleteFile(ProtocolFile file) {
        try {
            ftp.deleteFile(new FTPFile(file.getProtocolPath().getPathString(),
                    false));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
