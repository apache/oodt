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
import org.apache.oodt.cas.pushpull.config.ProtocolInfo;
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.exceptions.RemoteConnectionException;
import org.apache.oodt.cas.pushpull.protocol.Protocol;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFactory;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for creating the appropriate Protocol for the given
 * RemoteSites. The boolean argument 'allowReuse' allows for one unique protocol
 * for each URL. That is, if allowReuse is set to true, then if no Protocol has
 * been created for the current site, the Protocol created will be saved and
 * then returned for any later called with allowReuse equals true. This is to
 * allow for the same Protocol object to be used by several classes. The
 * Protocol class has been synchronized so this is thread-safe. If you set
 * 'allowReuse' to false then a new Protocol object will be created and
 * returned.<br>
 * <br>
 * 
 * @author bfoster
 */
public class ProtocolHandler {

    private HashMap<URL, ProtocolFactory> urlAndProtocolFactory;

    private HashMap<URL, Protocol> reuseProtocols;

    private HashMap<ProtocolFile, PagingInfo> pageInfos;

    private HashMap<ProtocolFile, List<ProtocolFile>> pathAndFileListMap;

    private ProtocolInfo pi;

    private static final Logger LOG = Logger.getLogger(ProtocolHandler.class
            .getName());

    /**
     * Creates a new ProtocolHandler for the given Config object
     * 
     * @param config
     *            The Config object that guides this ProtocolHandler in making
     *            class instanciations
     */
    public ProtocolHandler(ProtocolInfo pi) {
        this.pi = pi;
        urlAndProtocolFactory = new HashMap<URL, ProtocolFactory>();
        reuseProtocols = new HashMap<URL, Protocol>();
        pageInfos = new HashMap<ProtocolFile, PagingInfo>();
        pathAndFileListMap = new HashMap<ProtocolFile, List<ProtocolFile>>();
    }

    /**
     * Returns the appropriate protocol for the given Path
     * 
     * @param ProtocolPath
     *            Used to determine the appropriate Protocol to be returned and
     *            the path to navigate on if navigateToPathLoc is set to true.
     * @param allowReuse
     *            Set to true if you would like ProtocolHandler to take care of
     *            the protocol returned (i.e. reuseable protocols may be
     *            returned by this method again, if it is the appropriate
     *            protocol type for a given Path. Also ProtocolHandler will take
     *            care of disconnecting the reuseable protocols)
     * @param navigateToPathLoc
     *            If true, will navigate the to the end of the Path location
     *            specified
     * @return Protocol for the given Path
     * @throws RemoteCommunicationException
     *             If there is an error creating the protocol
     */
    public Protocol getAppropriateProtocol(ProtocolFile pFile,
            boolean allowReuse, boolean navigateToPathLoc)
            throws RemoteConnectionException {
        try {
            Protocol protocol = getAppropriateProtocol(pFile, allowReuse);
            if (protocol != null && navigateToPathLoc) {
                if (pFile.isDirectory())
                    this.cd(protocol, pFile);
                else
                    this.cd(protocol, pFile.getParentFile());
            }
            return protocol;
        } catch (Exception e) {
            throw new RemoteConnectionException(
                    "Failed to get appropriate protocol for " + pFile + " : "
                            + e.getMessage());
        }
    }

    private Protocol getAppropriateProtocol(ProtocolFile pFile,
            boolean allowReuse) throws ProtocolException, MalformedURLException {
        return this.getAppropriateProtocolBySite(pFile.getRemoteSite(),
                allowReuse);
    }

    public Protocol getAppropriateProtocolBySite(RemoteSite remoteSite,
            boolean allowReuse) throws ProtocolException {
        Protocol protocol = null;
        if ((allowReuse && ((protocol = reuseProtocols.get(remoteSite.getURL())) == null))
                || !allowReuse) {
            ProtocolFactory protocolFactory = this.urlAndProtocolFactory
                    .get(remoteSite.getURL());
            if (protocolFactory == null) {
                LinkedList<Class<ProtocolFactory>> protocolClasses = pi
                        .getProtocolClassesForProtocolType(remoteSite.getURL()
                                .getProtocol());
                for (Class<ProtocolFactory> clazz : protocolClasses) {
                    try {
                        if ((protocol = (protocolFactory = clazz.newInstance())
                                .newInstance()) != null) {
                            if (!connect(protocol, remoteSite, true)) {
                                LOG.log(Level.WARNING, "ProtocolFactory "
                                        + protocolFactory.getClass()
                                                .getCanonicalName()
                                        + " is not compatible with server at "
                                        + remoteSite.getURL());
                                protocol = null;
                            } else {
                                this.urlAndProtocolFactory.put(remoteSite
                                        .getURL(), protocolFactory);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING,
                                "Failed to instanciate protocol " + clazz
                                        + " for " + remoteSite.getURL());
                    }
                }
                if (protocol == null)
                    throw new ProtocolException(
                            "Failed to get appropriate protocol for "
                                    + remoteSite);
            } else {
                connect(protocol = protocolFactory.newInstance(), remoteSite,
                        false);
            }
            if (allowReuse)
                this.reuseProtocols.put(remoteSite.getURL(), protocol);
        }
        return protocol;
    }

    public synchronized List<ProtocolFile> nextPage(Protocol protocol)
            throws RemoteConnectionException, ProtocolException {
        return nextPage(protocol, null);
    }

    /**
     * @param protocol
     * @return
     * @throws RemoteConnectionException
     * @throws ProtocolException
     */
    public synchronized List<ProtocolFile> nextPage(Protocol protocol,
            ProtocolFileFilter filter) throws RemoteConnectionException,
            ProtocolException {

        PagingInfo pgInfo = this.getPagingInfo(this.pwd(protocol));
        try {
            System.out.println("PageSize: " + pi.getPageSize() + " PageLoc: "
                    + pgInfo.getPageLoc());
            List<ProtocolFile> fileList = this.ls(protocol);
            System.out.println("FileList size: " + fileList.size());

            if (this.getDynamicFileList(protocol) == null
                    && !this.passesDynamicDetection(pgInfo, fileList)) {
                LOG
                        .log(
                                Level.SEVERE,
                                "Remote directory '"
                                        + this.pwd(protocol)
                                        + "' file list size has changed -- setting directory as dynamic and resetting page location");
                this.putDynamicFileList(protocol, fileList);
                pgInfo.updatePageInfo(0, fileList);
            }

            List<ProtocolFile> page = new LinkedList<ProtocolFile>();
            int curLoc = pgInfo.getPageLoc();
            for (; page.size() < pi.getPageSize() && curLoc < fileList.size(); curLoc++) {
                if (filter == null || filter.accept(fileList.get(curLoc)))
                    page.add(fileList.get(curLoc));
            }
            pgInfo.updatePageInfo(curLoc, fileList);

            return page;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteConnectionException(
                    "Failed getting next page for protocol " + protocol
                            + "-- pgStart = " + pgInfo.getPageLoc()
                            + " pgSize = " + pi.getPageSize() + " : "
                            + e.getMessage());
        }

    }

    private boolean passesDynamicDetection(PagingInfo pgInfo,
            List<ProtocolFile> newLS) throws MalformedURLException,
            ProtocolException {
        if (pgInfo.getSizeOfLastLS() != -1
                && (pgInfo.getSizeOfLastLS() != newLS.size() || (newLS.size() != 0
                        && pgInfo.getPageLoc() < newLS.size() && (newLS
                        .get(pgInfo.getPageLoc()) == null || !newLS.get(
                        pgInfo.getPageLoc()).equals(
                        pgInfo.getProtocolFileAtPageLoc()))))) {
            return false;
        } else {
            return true;
        }
    }

    public void download(Protocol protocol, ProtocolFile fromFile, File toFile,
            boolean delete) throws RemoteConnectionException {

        // rename file for download
        File downloadFile = new File(toFile.getParent() + "/Downloading_"
                + toFile.getName());
        toFile.renameTo(downloadFile);

        LOG.log(Level.INFO, "Starting to download " + fromFile);
        try {
            // try to download the file
            protocol.download(fromFile, downloadFile);

            // delete file is specified
            if (delete) {
                if (!this.delete(protocol, fromFile))
                    LOG.log(Level.WARNING, "Failed to delete file '" + fromFile
                            + "' from server '" + protocol.getRemoteSite()
                            + "'");
                else
                    LOG.log(Level.INFO, "Successfully deleted file '"
                            + fromFile + "' from server '"
                            + protocol.getRemoteSite() + "'");
            }

            LOG.log(Level.INFO, "Finished downloading " + fromFile + " to "
                    + toFile);

            // rename file back to original name
            downloadFile.renameTo(toFile);

        } catch (Exception e) {
            downloadFile.delete();
            throw new RemoteConnectionException("Failed to download file "
                    + fromFile + " : " + e.getMessage());
        }
    }

    /**
     * Connects the given Protocol to the given URL
     * 
     * @param protocol
     *            The Protocol that will be connected
     * @param url
     *            The server to which the Protocol will connect
     * @throws RemoteConnectionException
     *             If connection fails
     * @throws RemoteLoginException
     *             If login fails
     */
    public boolean connect(Protocol protocol, RemoteSite remoteSite,
            boolean test) {
        for (int tries = 0; tries < 3; tries++) {

            // wait for 5 secs before next retry
            if (tries > 0) {
                LOG.log(Level.INFO, "Will retry connecting to " + remoteSite
                        + " in 5 seconds");
                synchronized (this) {
                    try {
                        System.out.print("Waiting");
                        for (int i = 0; i < 5; i++) {
                            System.out.print(" .");
                            wait(1000);
                        }
                        System.out.println();
                    } catch (Exception e) {
                    }
                }
            }

            try {
                // make sure protocol is disconnected
                try {
                    protocol.disconnect();
                } catch (Exception e) {
                }

                // try connecting Protocol
                protocol.connect(remoteSite);

                // check connection
                if (protocol.isConnected() && (!test || isOkProtocol(protocol, remoteSite))) {
                    LOG.log(Level.INFO, "Successfully connected to "
                            + remoteSite.getURL() + " with protocol '"
                            + protocol.getClass().getCanonicalName()
                            + "' and username '" + remoteSite.getUsername()
                            + "'");
                    return true;
                } else
                    return false;

            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error occurred while connecting to "
                        + remoteSite + " : " + e.getMessage());
            }

        }
        return false;
    }

    private boolean isOkProtocol(Protocol protocol, RemoteSite remoteSite) {
        try {
            LOG.log(Level.INFO, "Testing protocol "
                    + protocol.getClass().getCanonicalName()
                    + " . . . this may take a few minutes . . .");
            // test ls, cd, and pwd
            this.cdToHOME(protocol);
            ProtocolFile home = this.pwd(protocol);
            this.ls(protocol);
            if (remoteSite.getCdTestDir() != null)
                this.cd(protocol, new ProtocolFile(remoteSite, 
                        new ProtocolPath(remoteSite.getCdTestDir(), true)));
            else
                this.cdToROOT(protocol);
            this.cdToHOME(protocol);
            if (home == null || !home.equals(protocol.pwd()))
                throw new ProtocolException(
                        "Home directory not the same after cd");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Protocol "
                    + protocol.getClass().getCanonicalName()
                    + " failed compatibility test : " + e.getMessage());
            return false;
        }
        return true;
    }

    public void cdToROOT(Protocol protocol) throws ProtocolException {
        protocol.cdToROOT();
    }

    public void cdToHOME(Protocol protocol) throws ProtocolException {
        protocol.cdToHOME();
    }

    public boolean isProtocolConnected(Protocol protocol)
            throws ProtocolException {
        return protocol.isProtocolConnected();
    }

    public void cd(Protocol protocol, ProtocolFile file)
            throws ProtocolException {
        protocol.cd(file);
    }

    public ProtocolFile getProtocolFileFor(Protocol protocol, String file,
            boolean isDir) throws ProtocolException {
        return protocol.getProtocolFileFor(file, isDir);
    }

    public synchronized boolean delete(Protocol protocol, ProtocolFile file)
            throws MalformedURLException, ProtocolException {
        try {
            PagingInfo pgInfo = this.getPagingInfo(file.getParentFile());
            List<ProtocolFile> fileList = this.ls(protocol, file
                    .getParentFile());
            int indexOfFile = fileList.indexOf(file);
            if (indexOfFile != -1 && protocol.delete(file)) {
                fileList.remove(indexOfFile);
                System.out.println("IndexOfFile: " + indexOfFile
                        + " PageIndex: " + pgInfo.getPageLoc());
                if (indexOfFile < pgInfo.getPageLoc()
                        || indexOfFile == fileList.size() - 1)
                    pgInfo.updatePageInfo(pgInfo.getPageLoc() - 1, fileList);
                else
                    pgInfo.updatePageInfo(pgInfo.getPageLoc(), fileList);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private synchronized void putPgInfo(PagingInfo pgInfo, ProtocolFile pFile) {
        this.pageInfos.put(pFile, pgInfo);
    }

    private synchronized PagingInfo getPagingInfo(ProtocolFile pFile) {
        PagingInfo pgInfo = this.pageInfos.get(pFile);
        if (pgInfo == null)
            this.putPgInfo(pgInfo = new PagingInfo(), pFile);
        return pgInfo;
    }

    public ProtocolFile pwd(Protocol protocol) throws ProtocolException {
        return protocol.pwd();
    }

    public List<ProtocolFile> ls(Protocol protocol, ProtocolFile dir)
            throws ProtocolException {
        List<ProtocolFile> fileList = this.getDynamicFileList(protocol);
        if (fileList == null)
            fileList = protocol.ls(dir);
        return fileList;
    }

    public List<ProtocolFile> ls(Protocol protocol) throws ProtocolException {
        List<ProtocolFile> fileList = this.getDynamicFileList(protocol);
        if (fileList == null)
            fileList = protocol.ls();
        return fileList;
    }

    public List<ProtocolFile> ls(Protocol protocol, ProtocolFileFilter filter)
            throws ProtocolException {
        List<ProtocolFile> fileList = this.getDynamicFileList(protocol);
        if (fileList == null)
            fileList = protocol.ls(filter);
        return fileList;
    }

    private synchronized List<ProtocolFile> getDynamicFileList(Protocol protocol)
            throws ProtocolException {
        return this.pathAndFileListMap.get(this.pwd(protocol));
    }

    private synchronized void putDynamicFileList(Protocol protocol,
            List<ProtocolFile> fileList) throws ProtocolException {
        this.pathAndFileListMap.put(this.pwd(protocol), fileList);
    }

    public String getProtocolType(Protocol protocol) {
        return protocol.getProtocolType();
    }

    public synchronized RemoteSite getRemoteSite(Protocol protocol) {
        return protocol.getRemoteSite();
    }

    public synchronized ProtocolFile getHomeDir(Protocol protocol) {
        return protocol.getHomeDir();
    }

    public String getAbsPathFor(Protocol protocol, String path) {
        return protocol.getAbsPathFor(path);
    }

    /**
     * Disconnects and logs out the given Protocol
     * 
     * @param protocol
     *            The Protocol to be logout out and disconnected
     * @throws RemoteConnectionException
     */
    public void disconnect(Protocol protocol) throws RemoteConnectionException {
        URL url = null;
        try {
            url = protocol.getRemoteSite().getURL();
            LOG.log(Level.INFO, "Disconnecting protocol from " + url);
            protocol.disconnect();
        } catch (Exception e) {
            throw new RemoteConnectionException("Error disconnecting from "
                    + url + " : " + e.getMessage());
        }
    }

    /**
     * Disconnects all waiting Protocols and clears the waiting lists. Also
     * clears the current Protocol
     * 
     * @throws RemoteConnectionException
     */
    public void close() throws RemoteConnectionException {
        Set<Entry<URL, Protocol>> entries = reuseProtocols.entrySet();
        for (Entry<URL, Protocol> entry : entries) {
            disconnect(entry.getValue());
        }
        this.reuseProtocols.clear();
        this.urlAndProtocolFactory.clear();
        this.pageInfos.clear();
        this.pathAndFileListMap.clear();
    }

    class PagingInfo {

        private int pageLoc;

        private int sizeOfLastLS;

        private ProtocolFile pFileAtPageLoc;

        PagingInfo() {
            this.pageLoc = 0;
            this.sizeOfLastLS = -1;
            this.pFileAtPageLoc = null;
        }

        synchronized void updatePageInfo(int newPageLoc, List<ProtocolFile> ls)
                throws MalformedURLException, ProtocolException {
            this.sizeOfLastLS = ls.size();
            this.pageLoc = newPageLoc < 0 ? 0 : newPageLoc;
            this.pFileAtPageLoc = (this.sizeOfLastLS > 0 && newPageLoc < ls
                    .size()) ? ls.get(newPageLoc) : null;
        }

        synchronized int getPageLoc() {
            return this.pageLoc;
        }

        synchronized int getSizeOfLastLS() {
            return this.sizeOfLastLS;
        }

        synchronized ProtocolFile getProtocolFileAtPageLoc() {
            return this.pFileAtPageLoc;
        }

    }

}
