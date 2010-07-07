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


package org.apache.oodt.cas.pushpull.protocol;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Protocol Interface Class
 * </p>.
 */
public abstract class Protocol {

    protected String protocolType;

    protected RemoteSite remoteSite;

    private ProtocolFile homeDir;

    private ProtocolFile cwd;

    private static final Logger LOG = Logger
            .getLogger(Protocol.class.getName());

    protected Protocol(String protocolType) {
        this.protocolType = protocolType.toLowerCase();
    }

    protected abstract void chDir(ProtocolPath path) throws ProtocolException;

    protected abstract void cdToRoot() throws ProtocolException;

    protected abstract void connect(String host, String username,
            String password) throws ProtocolException;

    /**
     * Disconnects the Protocol for the server
     * 
     * @throws IOException
     *             If error is encountered while disconnecting
     */
    protected abstract void disconnectFromServer() throws ProtocolException;

    /**
     * Can be used to determine if Protocol is connected
     * 
     * @return true if Protocol is connected, otherwise false
     */
    protected abstract boolean isConnected() throws ProtocolException;

    /**
     * Downloads a file from Protocol server to the location specified by the
     * java.io.File passed in. Only a name can be passed in for {@code fileName},
     * can't be a path. User must first navigate to the directory which contains
     * the file and then can call this method.
     * 
     * @param fileName
     *            The name of the file at Protocol server
     * @param localFileLocation
     *            The name and location that file should be stored
     * @return true if successful, otherwise false
     * @throws RemoteCommunicationException
     *             If an error is encountered while downloading file
     */
    protected abstract void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException;

    public abstract void abortCurFileTransfer() throws ProtocolException;

    /**
     * Returns a string value of current directory -- similar to U*ix pwd()
     * 
     * @return The path of current directory
     * @throws RemoteCommunicationException
     *             If an error is encountered while retreiving path
     */
    protected abstract ProtocolFile getCurrentWorkingDir()
            throws ProtocolException;

    protected abstract List<ProtocolFile> listFiles() throws ProtocolException;

    protected abstract boolean deleteFile(ProtocolFile file);

    /**
     * Connects the Protocol to the given server
     * 
     * @param server
     *            The URL to which the Protocol will be connected
     * @throws RemoteConnectionException
     *             If error is encountered while connecting to server
     */
    public synchronized void connect(RemoteSite remoteSite)
            throws ProtocolException {
        this.remoteSite = remoteSite;
        this.connect(remoteSite.getURL().getHost(), remoteSite.getUsername(),
                remoteSite.getPassword());
        try {
            homeDir = this.getCurrentWorkingDir();
        } catch (Exception e) {
            throw new ProtocolException(
                    "Failed to pwd after connect to store cwd : "
                            + e.getMessage());
        }
        this.cwd = this.homeDir;
    }

    protected synchronized void reconnect() throws ProtocolException {
        if (this.remoteSite == null)
            throw new ProtocolException(
                    "Can't reconnect a Protocol which has never been initially connected");

        try {
            this.disconnect();
        } catch (Exception e) {
        }

        this.connect(this.remoteSite);
    }

    public synchronized boolean isProtocolConnected() throws ProtocolException {
        return this.isConnected();
    }

    public synchronized void disconnect() throws ProtocolException {
        this.disconnectFromServer();
    }

    /**
     * Changes directories to the given directory passed in (insures that the
     * correct path goes to the cd(String) method implemented by inheriting
     * classes
     * 
     * @param dir
     *            The path of directory to change to, can be relative or
     *            absolute
     * @return true if successful, otherwise false
     * @throws RemoteCommunicationException
     */
    public synchronized void cd(ProtocolFile file) throws ProtocolException {
        if (!this.cwd.equals(file)) {
            LOG.log(Level.INFO, "Changing to directory '" + file + "'");
            if (file.isRelativeToHOME())
                file = new ProtocolFile(file.getRemoteSite(), new ProtocolPath(
                        this.homeDir.getProtocolPath().getPathString() + "/"
                                + file.getProtocolPath().getPathString(), file
                                .isDirectory()));
            chDir(file.getProtocolPath());
            this.cwd = file;
        }
    }

    public synchronized void cdToHOME() throws ProtocolException {
        LOG
                .log(Level.INFO, "Changing to HOME directory '" + this.homeDir
                        + "'");
        chDir(this.homeDir.getProtocolPath());
        this.cwd = this.homeDir;
        // if
        // (pathAndFileListMap.get(this.cwd.getProtocolPath().getPathString())
        // == null)
        // pathAndFileListMap.put(this.cwd.getProtocolPath().getPathString(),
        // this.listFiles());
    }

    public synchronized void cdToROOT() throws ProtocolException {
        try {
            LOG.log(Level.INFO, "Changing to ROOT directory '/'");
            this.cdToRoot();
            this.cwd = new ProtocolFile(this.getRemoteSite(), new ProtocolPath(
                    "/", true));
        } catch (Exception e) {
            throw new ProtocolException("Failed to cd to Root directory : "
                    + e.getMessage());
        }
    }

    public synchronized ProtocolFile getProtocolFileFor(String file,
            boolean isDir) throws ProtocolException {
        try {
            if (!file.startsWith("/"))
                file = this.homeDir.getProtocolPath().getPathString() + "/"
                        + file;
            return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(
                    file, isDir));
        } catch (Exception e) {
            throw new ProtocolException("Failed to create protocol for " + file
                    + " : " + e.getMessage());
        }
    }

    /**
     * Overrides the Object class equals method.
     * 
     * @param protocol
     *            The comparing protocol
     * @return true if the two protocols are equal
     */
    public synchronized boolean equals(Object protocol) {
        if (protocol instanceof Protocol) {
            Protocol p = (Protocol) protocol;
            try {
                if (p.getProtocolType().equals(this.getProtocolType())
                        && p.getRemoteSite().equals(this.getRemoteSite())
                        && p.cwd.getProtocolPath().equals(
                                this.cwd.getProtocolPath()))
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public synchronized void download(ProtocolFile file, File toLocalFile)
            throws ProtocolException {
        this.getFile(file, toLocalFile);
    }

    public synchronized void download(ProtocolFile file, File toLocalFile,
            final long timeout) throws ProtocolException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    synchronized (this) {
                        this.wait(timeout);
                    }
                } catch (Exception e) {
                }
                try {
                    Protocol.this.abortCurFileTransfer();
                } catch (Exception e) {
                }
            }
        }).start();
        this.download(file, toLocalFile);
    }

    public synchronized boolean delete(ProtocolFile file) {
        return this.deleteFile(file);
    }

    public synchronized ProtocolFile pwd() throws ProtocolException {
        return this.cwd;
    }

    public synchronized List<ProtocolFile> ls(ProtocolFile dir)
            throws ProtocolException {
        if (!this.cwd.equals(dir)) {
            this.chDir(dir.getProtocolPath());
            List<ProtocolFile> fileList = this.listFiles();
            this.chDir(this.cwd.getProtocolPath());
            return fileList;
        } else
            return this.listFiles();
    }

    public synchronized List<ProtocolFile> ls() throws ProtocolException {
        return this.listFiles();
    }

    public synchronized List<ProtocolFile> ls(ProtocolFileFilter filter)
            throws ProtocolException {
        try {
            List<ProtocolFile> fileList = this.ls();
            LinkedList<ProtocolFile> returnList = new LinkedList<ProtocolFile>();
            for (ProtocolFile file : fileList) {
                if (filter == null || filter.accept(file))
                    returnList.add(file);
            }
            return returnList;
        } catch (Exception e) {
            throw new ProtocolException(
                    "Failed to get file list using filter : " + e.getMessage());
        }
    }

    /**
     * 
     * @return String The type of protocol
     */
    public synchronized String getProtocolType() {
        return protocolType;
    }

    /**
     * The name of the Protocol server host name
     * 
     * @return The name of server host
     */
    public synchronized RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public synchronized ProtocolFile getHomeDir() {
        return this.homeDir;
    }

    protected String getAbsPathFor(String path) {
        if (!path.startsWith("/")) {
            String curPath = this.getHomeDir().getProtocolPath()
                    .getPathString();
            if (curPath.endsWith("/"))
                curPath = curPath.substring(0, curPath.length() - 1);
            path = curPath + "/" + path;
        }
        return path;
    }

}
