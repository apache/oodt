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

package org.apache.oodt.cas.pushpull.protocol;

//OODT imports
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.RemoteSiteFile;
import org.apache.oodt.cas.pushpull.config.ProtocolInfo;
import org.apache.oodt.cas.protocol.auth.BasicAuthentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;
import org.apache.oodt.cas.pushpull.exceptions.RemoteConnectionException;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Vector;
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

  private final HashMap<URL, ProtocolFactory> urlAndProtocolFactory;

  private final HashMap<URL, Protocol> reuseProtocols;

  private final HashMap<RemoteSiteFile, PagingInfo> pageInfos;

  private final HashMap<RemoteSiteFile, List<RemoteSiteFile>> pathAndFileListMap;

  private final ProtocolInfo pi;

  private static final Logger LOG = Logger.getLogger(ProtocolHandler.class
      .getName());

  /**
   * Creates a new ProtocolHandler for the given Config object
   *
   * @param config
   *          The Config object that guides this ProtocolHandler in making class
   *          instanciations
   */
  public ProtocolHandler(ProtocolInfo pi) {
    this.pi = pi;
    urlAndProtocolFactory = new HashMap<URL, ProtocolFactory>();
    reuseProtocols = new HashMap<URL, Protocol>();
    pageInfos = new HashMap<RemoteSiteFile, PagingInfo>();
    pathAndFileListMap = new HashMap<RemoteSiteFile, List<RemoteSiteFile>>();
  }

  /**
   * Returns the appropriate protocol for the given Path
   *
   * @param ProtocolPath
   *          Used to determine the appropriate Protocol to be returned and the
   *          path to navigate on if navigateToPathLoc is set to true.
   * @param allowReuse
   *          Set to true if you would like ProtocolHandler to take care of the
   *          protocol returned (i.e. reuseable protocols may be returned by
   *          this method again, if it is the appropriate protocol type for a
   *          given Path. Also ProtocolHandler will take care of disconnecting
   *          the reuseable protocols)
   * @param navigateToPathLoc
   *          If true, will navigate the to the end of the Path location
   *          specified
   * @return Protocol for the given Path
   * @throws RemoteCommunicationException
   *           If there is an error creating the protocol
   */
  public Protocol getAppropriateProtocol(RemoteSiteFile pFile,
      boolean allowReuse, boolean navigateToPathLoc)
      throws RemoteConnectionException {
    try {
      Protocol protocol = getAppropriateProtocol(pFile, allowReuse);
      if (protocol != null && navigateToPathLoc) {
        if (pFile.isDir())
          this.cd(protocol, pFile);
        else
          this.cd(protocol, new RemoteSiteFile(pFile.getParent(), pFile.getSite()));
      }
      return protocol;
    } catch (Exception e) {
      throw new RemoteConnectionException(
          "Failed to get appropriate protocol for " + pFile + " : "
              + e.getMessage());
    }
  }

  private Protocol getAppropriateProtocol(RemoteSiteFile pFile,
      boolean allowReuse) throws ProtocolException, MalformedURLException {
    return this.getAppropriateProtocolBySite(pFile.getSite(), allowReuse);
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
                LOG.log(
                    Level.WARNING,
                    "ProtocolFactory "
                        + protocolFactory.getClass().getCanonicalName()
                        + " is not compatible with server at "
                        + remoteSite.getURL());
                protocol = null;
              } else {
                this.urlAndProtocolFactory.put(remoteSite.getURL(),
                    protocolFactory);
                break;
              }
            }
          } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to instanciate protocol " + clazz
                + " for " + remoteSite.getURL());
          }
        }
        if (protocol == null)
          throw new ProtocolException("Failed to get appropriate protocol for "
              + remoteSite);
      } else {
        connect(protocol = protocolFactory.newInstance(), remoteSite, false);
      }
      if (allowReuse)
        this.reuseProtocols.put(remoteSite.getURL(), protocol);
    }
    return protocol;
  }

  public synchronized List<RemoteSiteFile> nextPage(RemoteSite site, Protocol protocol)
      throws RemoteConnectionException, ProtocolException {
    return nextPage(site, protocol, null);
  }

  /**
   * @param protocol
   * @return
   * @throws RemoteConnectionException
   * @throws ProtocolException
   */
  public synchronized List<RemoteSiteFile> nextPage(RemoteSite site, Protocol protocol,
      ProtocolFileFilter filter) throws RemoteConnectionException,
      ProtocolException {

    PagingInfo pgInfo = this.getPagingInfo(this.pwd(site, protocol));
    try {
      System.out.println("PageSize: " + pi.getPageSize() + " PageLoc: "
          + pgInfo.getPageLoc());
      List<RemoteSiteFile> fileList = this.ls(site, protocol);
      System.out.println("FileList size: " + fileList.size());

      if (this.getDynamicFileList(site, protocol) == null
          && !this.passesDynamicDetection(pgInfo, fileList)) {
        LOG.log(
            Level.SEVERE,
            "Remote directory '"
                + this.pwd(site, protocol)
                + "' file list size has changed -- setting directory as dynamic and resetting page location");
        this.putDynamicFileList(site, protocol, fileList);
        pgInfo.updatePageInfo(0, fileList);
      }

      List<RemoteSiteFile> page = new LinkedList<RemoteSiteFile>();
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
          "Failed getting next page for protocol " + protocol + "-- pgStart = "
              + pgInfo.getPageLoc() + " pgSize = " + pi.getPageSize() + " : "
              + e.getMessage());
    }

  }

  private boolean passesDynamicDetection(PagingInfo pgInfo,
      List<RemoteSiteFile> newLS) throws MalformedURLException,
      ProtocolException {
    if (pgInfo.getSizeOfLastLS() != -1
        && (pgInfo.getSizeOfLastLS() != newLS.size() || (newLS.size() != 0
            && pgInfo.getPageLoc() < newLS.size() && (newLS.get(pgInfo
            .getPageLoc()) == null || !newLS.get(pgInfo.getPageLoc()).equals(
            pgInfo.getRemoteSiteFileAtPageLoc()))))) {
      return false;
    } else {
      return true;
    }
  }

  public void download(Protocol protocol, RemoteSiteFile fromFile, File toFile,
      boolean delete) throws RemoteConnectionException {

    // rename file for download
    File downloadFile = new File(toFile.getParent() + "/Downloading_"
        + toFile.getName());
    toFile.renameTo(downloadFile);

    LOG.log(Level.INFO, "Starting to download " + fromFile);
    try {
      // try to download the file
      protocol.get(fromFile, downloadFile);

      // delete file is specified
      if (delete) {
        if (!this.delete(protocol, fromFile))
          LOG.log(Level.WARNING, "Failed to delete file '" + fromFile
              + "' from server '" + fromFile.getSite() + "'");
        else
          LOG.log(Level.INFO, "Successfully deleted file '" + fromFile
              + "' from server '" + fromFile.getSite() + "'");
      }

      LOG.log(Level.INFO, "Finished downloading " + fromFile + " to " + toFile);

      // rename file back to original name
      downloadFile.renameTo(toFile);

    } catch (Exception e) {
      downloadFile.delete();
      throw new RemoteConnectionException("Failed to download file " + fromFile
          + " : " + e.getMessage(), e);
    }
  }

  /**
   * Connects the given Protocol to the given URL
   *
   * @param protocol
   *          The Protocol that will be connected
   * @param url
   *          The server to which the Protocol will connect
   * @throws RemoteConnectionException
   *           If connection fails
   * @throws RemoteLoginException
   *           If login fails
   */
  public boolean connect(Protocol protocol, RemoteSite remoteSite, boolean test) {
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
          protocol.close();
        } catch (Exception e) {
        }

        // try connecting Protocol
        protocol.connect(
            remoteSite.getURL().getHost(),
            new BasicAuthentication(remoteSite.getUsername(), remoteSite
                .getPassword()));

        // check connection
        if (protocol.connected()
            && (!test || isOkProtocol(protocol, remoteSite))) {
          LOG.log(Level.INFO,
              "Successfully connected to " + remoteSite.getURL()
                  + " with protocol '" + protocol.getClass().getCanonicalName()
                  + "' and username '" + remoteSite.getUsername() + "'");
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
      RemoteSiteFile home = this.pwd(remoteSite, protocol);
      this.ls(remoteSite, protocol);
      if (remoteSite.getCdTestDir() != null)
        this.cd(protocol, new RemoteSiteFile(home, remoteSite.getCdTestDir(),
            true, remoteSite));
      else
        this.cdToROOT(protocol);
      this.cdToHOME(protocol);
      if (home == null || !home.equals(protocol.pwd()))
        throw new ProtocolException("Home directory not the same after cd");
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Protocol "
          + protocol.getClass().getCanonicalName()
          + " failed compatibility test : " + e.getMessage());
      return false;
    }
    return true;
  }

  public void cdToROOT(Protocol protocol) throws ProtocolException {
    protocol.cdRoot();
  }

  public void cdToHOME(Protocol protocol) throws ProtocolException {
    protocol.cdHome();
  }

  public boolean isProtocolConnected(Protocol protocol)
      throws ProtocolException {
    return protocol.connected();
  }

  public void cd(Protocol protocol, RemoteSiteFile file)
      throws ProtocolException {
    protocol.cd(file);
  }

  public RemoteSiteFile getProtocolFileFor(RemoteSite site, Protocol protocol, String file,
      boolean isDir) throws ProtocolException {
    return this.getProtocolFileByProtocol(site, protocol, file, isDir);
  }

  public synchronized boolean delete(Protocol protocol, RemoteSiteFile file)
      throws MalformedURLException, ProtocolException {
    try {
      PagingInfo pgInfo = this.getPagingInfo(file.getRemoteParent());
      List<RemoteSiteFile> fileList = this.ls(protocol, file.getRemoteParent());
      int indexOfFile = fileList.indexOf(file);
      if (indexOfFile != -1) {
        protocol.delete(file);
        fileList.remove(indexOfFile);
        System.out.println("IndexOfFile: " + indexOfFile + " PageIndex: "
            + pgInfo.getPageLoc());
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

  private synchronized void putPgInfo(PagingInfo pgInfo, RemoteSiteFile pFile) {
    this.pageInfos.put(pFile, pgInfo);
  }

  private synchronized PagingInfo getPagingInfo(RemoteSiteFile pFile) {
    PagingInfo pgInfo = this.pageInfos.get(pFile);
    if (pgInfo == null)
      this.putPgInfo(pgInfo = new PagingInfo(), pFile);
    return pgInfo;
  }

  public RemoteSiteFile pwd(RemoteSite site, Protocol protocol) throws ProtocolException {
    return new RemoteSiteFile(protocol.pwd(), site);
  }

  public List<RemoteSiteFile> ls(Protocol protocol, RemoteSiteFile dir)
      throws ProtocolException {
    List<RemoteSiteFile> fileList = this.getDynamicFileList(dir.getSite(), protocol);
    if (fileList == null) {
      protocol.cd(dir);
      fileList = toRemoteSiteFiles(protocol.ls(), dir.getSite());
    }
    return fileList;
  }

  public List<RemoteSiteFile> ls(RemoteSite site, Protocol protocol) throws ProtocolException {
    List<RemoteSiteFile> fileList = this.getDynamicFileList(site, protocol);
    if (fileList == null)
      fileList = toRemoteSiteFiles(protocol.ls(), site);
    return fileList;
  }

  public List<RemoteSiteFile> ls(RemoteSite site, Protocol protocol, ProtocolFileFilter filter)
      throws ProtocolException {
    List<RemoteSiteFile> fileList = this.getDynamicFileList(site, protocol);
    if (fileList == null)
      fileList = toRemoteSiteFiles(protocol.ls(filter), site);
    return fileList;
  }

  private synchronized List<RemoteSiteFile> getDynamicFileList(RemoteSite site, Protocol protocol)
      throws ProtocolException {
    return (List<RemoteSiteFile>) (List<?>) this.pathAndFileListMap.get(this
        .pwd(site, protocol));
  }

  private synchronized void putDynamicFileList(RemoteSite site, Protocol protocol,
      List<RemoteSiteFile> fileList) throws ProtocolException {
    this.pathAndFileListMap.put(this.pwd(site, protocol), fileList);
  }

  public synchronized RemoteSiteFile getHomeDir(RemoteSite site, Protocol protocol) {
    try {
      protocol.cdHome();
      return new RemoteSiteFile(protocol.pwd(), site);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public String getAbsPathFor(Protocol protocol, String path, boolean isDir) {
    try {
      protocol.cd(new ProtocolFile(path, isDir));
      return protocol.pwd().getAbsoluteFile().getPath();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Disconnects and logs out the given Protocol
   *
   * @param protocol
   *          The Protocol to be logout out and disconnected
   * @throws RemoteConnectionException
   */
  public void disconnect(Protocol protocol) throws RemoteConnectionException {
    try {
      LOG.log(Level.INFO, "Disconnecting protocol " + protocol.getClass().getName());
      protocol.close();
    } catch (Exception e) {
      throw new RemoteConnectionException("Error disconnecting " + protocol.getClass().getName()
          + " : " + e.getMessage());
    }
  }

  /**
   * Disconnects all waiting Protocols and clears the waiting lists. Also clears
   * the current Protocol
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

  private synchronized RemoteSiteFile getProtocolFileByProtocol(
      RemoteSite site, Protocol protocol, String file, boolean isDir) throws ProtocolException {
    try {
      if (!file.startsWith("/")) {
        protocol.cdHome();
        file = protocol.pwd().getPath() + "/" + file;
      }
      return new RemoteSiteFile(file, isDir, site);
    } catch (Exception e) {
      throw new ProtocolException("Failed to create protocol for " + file
          + " : " + e.getMessage());
    }
  }

  private List<RemoteSiteFile> toRemoteSiteFiles(List<ProtocolFile> files, RemoteSite site) {
    List<RemoteSiteFile> newFiles = new Vector<RemoteSiteFile>();
    if (files != null) {
      for (ProtocolFile file : files) {
        newFiles.add(new RemoteSiteFile(file, site));
      }
    }
    return newFiles;
  }

  class PagingInfo {

    private int pageLoc;

    private int sizeOfLastLS;

    private RemoteSiteFile pFileAtPageLoc;

    PagingInfo() {
      this.pageLoc = 0;
      this.sizeOfLastLS = -1;
      this.pFileAtPageLoc = null;
    }

    synchronized void updatePageInfo(int newPageLoc, List<RemoteSiteFile> ls)
        throws MalformedURLException, ProtocolException {
      this.sizeOfLastLS = ls.size();
      this.pageLoc = newPageLoc < 0 ? 0 : newPageLoc;
      this.pFileAtPageLoc = (this.sizeOfLastLS > 0 && newPageLoc < ls.size()) ? ls
          .get(newPageLoc) : null;
    }

    synchronized int getPageLoc() {
      return this.pageLoc;
    }

    synchronized int getSizeOfLastLS() {
      return this.sizeOfLastLS;
    }

    synchronized RemoteSiteFile getRemoteSiteFileAtPageLoc() {
      return this.pFileAtPageLoc;
    }

  }

}
