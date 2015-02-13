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


package org.apache.oodt.cas.pushpull.retrievalsystem;

//OODT imports
import org.apache.oodt.cas.pushpull.config.Config;
import org.apache.oodt.cas.pushpull.config.SiteInfo;
import org.apache.oodt.cas.pushpull.exceptions.AlreadyInDatabaseException;
import org.apache.oodt.cas.pushpull.exceptions.CrawlerException;
import org.apache.oodt.cas.pushpull.exceptions.ProtocolFileException;
import org.apache.oodt.cas.pushpull.exceptions.RemoteConnectionException;
import org.apache.oodt.cas.pushpull.exceptions.ThreadEvaluatorException;
import org.apache.oodt.cas.pushpull.exceptions.ToManyFailedDownloadsException;
import org.apache.oodt.cas.pushpull.exceptions.UndefinedTypeException;
import org.apache.oodt.cas.pushpull.filerestrictions.renamingconventions.RenamingConvention;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.ProtocolHandler;
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;
import org.apache.oodt.cas.pushpull.protocol.RemoteSiteFile;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.MimeTypeUtils;


//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * <pre>
 *    Will crawl external directory structures and will download the files within these structures.
 *
 *    This class's settings are set using a java .properties file which can be read in and parsed by Config.java.
 *    This .properties file should have the following properties set:
 *
 *   	{@literal #list of sites to crawl
 *   	protocol.external.sources=&lt;path-to-xml-file&gt;
 *
 *   	#protocol types
 *   	protocolfactory.types=&lt;list-of-protocols-separated-by-commas&gt; (e.g. ftp,http,https,sftp)
 *
 *   	#Protocol factories per types (must have one for each protocol mention in protocolfactory.types -- the property must be name
 *    	# as such: protocolfactory.&lt;name-of-protocol-type&gt;
 *   	protocolfactory.ftp=&lt;path-to-java-protocolfactory-class&gt; (e.g. org.apache.oodt.cas.protocol.ftp.FtpClientFactory)
 *   	protocolfactory.http=&lt;path-to-java-protocolfactory-class&gt;
 *   	protocolfactory.https=&lt;path-to-java-protocolfactory-class&gt;
 *   	protocolfactory.sftp=&lt;path-to-java-protocolfactory-class&gt;
 *
 *   	#configuration to make java.net.URL accept unsupported protocols -- must exist just as shown
 *   	java.protocol.handler.pkgs=org.apache.oodt.cas.url.handlers
 *    }
 *
 *    In order to specify which external sites to crawl you must create a XML file which contains the
 *    the site and necessary information needed to crawl the site, such as username and password.
 *    protocol.external.sources must contain the path to this file so the crawl knows where to find it.
 *    You can also train this class on how to crawl each given site.  This is also specified in an XML
 *    file, whose path must be given in the first mentioned XML file which contians the username and password.
 *
 *    Then schema for the external sites XML file is as such:
 *
 *    	{@literal &lt;sources&gt;
 *    	   &lt;source url=&quot;url-of-server&quot;&gt;
 *    	      &lt;username&gt;username&lt;/username&gt;
 *    	      &lt;password&gt;password&lt;/password&gt;
 *    	      &lt;dirstruct&gt;path-to-xml-file&lt;/dirstruct&gt;
 *    	      &lt;crawl&gt;yes-or-no&lt;/crawl&gt;
 *    	   &lt;/source&gt;
 *    	   ...
 *    	   ...
 *    	   ...
 *    	&lt;/sources\&gt;}
 *
 *    You may specify as many sources as you would like by specifying multiple {@literal &lt;source&gt;} tags.
 *    In the {@literal &lt;source&gt;} tag, the parameter 'url' must be specified.  This is the url of the server
 *    you want the crawler to connect to.  It should be of the following format:
 *    {@literal &lt;protocol&gt;://&lt;host&gt;} (e.g. sftp://remote.computer.gov)
 *    If no username and password exist, then these elements can be omitted (they are optional).
 *    For {@literal &lt;crawl&gt;} place yes or no here.  This is for convenience of being able to keep record of the
 *    sites and their information in this XML file even if you decide that you no longer need to crawl it
 *    anymore (just put {@literal &lt;crawl&gt;no&lt;/crawl&gt;} and the crawl will not crawl that site).
 *    {@literal &lt;dirStruct&gt;} contains a path to another XML file which is documented in DirStruct.java javadoc.  This
 *    element is optional.  If no {@literal &lt;dirStruct&gt;} is given, then every directory will be crawled on the site
 *    and every encountered file will be downloaded.
 * </pre>
 *
 * @author bfoster (Brian Foster)
 */
public class FileRetrievalSystem {

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(FileRetrievalSystem.class.getName());

    private final static int MAX_RETRIES = 3;

    private LinkedList<ProtocolFile> failedDownloadList;

    private HashSet<ProtocolFile> currentlyDownloading;

    private int max_allowed_failed_downloads;

    /**
     * The max number of threads able to run at the same time
     */
    private int max_sessions;

    private final int absMaxAllowedSessions = 50;

    /**
     * This is just for clarity purposes. . .I only create the amount of threads
     * that I will allow to be used at any given moment
     */
    private final static int EXTRA_LAZY_SESSIONS_TIMEOUT = 10;

    /**
     * A list of created protocol sessions (devoted to grabbing files from the
     * crawling directory structure) that are not presently in use.
     */
    private Vector<Protocol> avaliableSessions;

    /**
     * The number of sessions that have been created (should always be less than
     * or equal to MAX_SESSIONS).
     */
    private int numberOfSessions;

    /**
     * The thread pool that is in charge of the sessions.
     */
    private ThreadPoolExecutor threadController;

    /**
     * Manages the Protocols and always ensures that the Crawler is using the
     * appropriate protocol for any given server.
     */
    private ProtocolHandler protocolHandler;

    /**
     * max_sessions tracker
     */
    private DownloadThreadEvaluator dtEval;

    private DownloadListener dListener;

    private Config config;

    private SiteInfo siteInfo;

    private HashSet<File> stagingAreas;

    private MimeTypeUtils mimeTypeDetection;

    /**
     * Creates a Crawler based on the URL, DirStruct, and Config objects passed
     * in. If no DirStruct is needed then set it to null.
     *
     * @param url
     *            The URL for which you want this Crawler to crawl
     * @param dirStruct
     *            The specified directory structure located at the host -- use
     *            to train crawler (see DirStruct).
     * @param config
     *            The Configuration file that is passed to this objects
     *            ProtocolHandler.
     * @throws InstantiationException
     * @throws DatabaseException
     */
    public FileRetrievalSystem(Config config, SiteInfo siteInfo)
            throws InstantiationException {
        try {
            protocolHandler = new ProtocolHandler(config.getProtocolInfo());
            this.config = config;
            this.siteInfo = siteInfo;
            mimeTypeDetection = new MimeTypeUtils(config
                    .getProductTypeDetectionFile());
        } catch (Exception e) {
            e.printStackTrace();
            throw new InstantiationException(
                    "Failed to create FileRetrievalSystem : " + e.getMessage());
        }
    }

    public void registerDownloadListener(DownloadListener dListener) {
        this.dListener = dListener;
    }

    public void initialize() throws IOException {
        try {
            resetVariables();
        } catch (Exception e) {
            throw new IOException("Failed to initialize FileRetrievalSystem : "
                    + e.getMessage());
        }
    }

    /**
     * Initializes variables that must be reset when more than one crawl is done
     *
     * @throws ThreadEvaluatorException
     */
    void resetVariables() throws ThreadEvaluatorException {
        numberOfSessions = 0;
        stagingAreas = new HashSet<File>();
        avaliableSessions = new Vector<Protocol>();
        currentlyDownloading = new HashSet<ProtocolFile>();
        failedDownloadList = new LinkedList<ProtocolFile>();
        max_allowed_failed_downloads = config.getMaxFailedDownloads();
        max_sessions = config.getRecommendedThreadCount();
        threadController = new ThreadPoolExecutor(this.max_sessions,
                this.max_sessions, EXTRA_LAZY_SESSIONS_TIMEOUT,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        if (config.useTracker())
            dtEval = new DownloadThreadEvaluator(this.absMaxAllowedSessions);
    }

    /**
     * reset error flag
     */
    public void clearErrorFlag() {
        max_allowed_failed_downloads += config.getMaxFailedDownloads();
    }

    public boolean isAlreadyInDatabase(RemoteFile rf) throws CatalogException {
        return config.getIngester() != null ? config.getIngester().hasProduct(
                config.getFmUrl(), rf.getMetadata(RemoteFile.PRODUCT_NAME))
                : false;
    }

    public List<RemoteSiteFile> getNextPage(final RemoteSiteFile dir,
            final ProtocolFileFilter filter) throws RemoteConnectionException {
        for (int i = 0; i < 3; i++) {
            try {
                return protocolHandler.nextPage(dir.getSite(), protocolHandler
                        .getAppropriateProtocol(dir, true, true),
                        new ProtocolFileFilter() {
                            @Override
                           public boolean accept(ProtocolFile file) {
                                return filter.accept(file)
                                        && !FileRetrievalSystem.this
                                                .isDownloading(file);
                            }
                        });
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Retrying to get next page for " + dir
                        + " because operation failed : " + e.getMessage(), e);
            }
        }
        throw new RemoteConnectionException("Failed to get next page for "
                + dir);
    }

    public void changeToRoot(RemoteSite remoteSite) throws ProtocolException,
            MalformedURLException, org.apache.oodt.cas.protocol.exceptions.ProtocolException {
        if (validate(remoteSite))
            protocolHandler.cdToROOT(protocolHandler
                    .getAppropriateProtocolBySite(remoteSite, true));
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public void changeToHOME(RemoteSite remoteSite) throws ProtocolException,
            MalformedURLException {
        if (validate(remoteSite))
            protocolHandler.cdToHOME(protocolHandler
                    .getAppropriateProtocolBySite(remoteSite, true));
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public void changeToDir(String dir, RemoteSite remoteSite)
            throws MalformedURLException, ProtocolException {
        if (validate(remoteSite))
            this
                    .changeToDir(protocolHandler.getProtocolFileFor(remoteSite,
                            protocolHandler.getAppropriateProtocolBySite(
                                    remoteSite, true), dir, true));
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public void changeToDir(RemoteSiteFile pFile) throws ProtocolException,
            MalformedURLException {
        RemoteSite remoteSite = pFile.getSite();
        if (validate(remoteSite))
            protocolHandler.cd(protocolHandler.getAppropriateProtocolBySite(
                    remoteSite, true), pFile);
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public ProtocolFile getHomeDir(RemoteSite remoteSite)
            throws ProtocolException {
        if (validate(remoteSite))
            return protocolHandler.getHomeDir(remoteSite, protocolHandler
                    .getAppropriateProtocolBySite(remoteSite, true));
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public ProtocolFile getProtocolFile(RemoteSite remoteSite, String file,
            boolean isDir) throws ProtocolException {
        if (validate(remoteSite))
            return protocolHandler.getProtocolFileFor(remoteSite, protocolHandler
                    .getAppropriateProtocolBySite(remoteSite, true), file,
                    isDir);
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public ProtocolFile getCurrentFile(RemoteSite remoteSite)
            throws ProtocolFileException, ProtocolException,
            MalformedURLException {
        if (validate(remoteSite))
            return protocolHandler.pwd(remoteSite, protocolHandler
                    .getAppropriateProtocolBySite(remoteSite, true));
        else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    // returns true if download was added to queue. . .false otherwise
    public boolean addToDownloadQueue(RemoteSite remoteSite, String file,
            String renamingString, File downloadToDir,
            String uniqueMetadataElement, boolean deleteAfterDownload, Metadata fileMetadata)
            throws ToManyFailedDownloadsException, RemoteConnectionException,
            ProtocolFileException, ProtocolException,
            AlreadyInDatabaseException, UndefinedTypeException,
            CatalogException, IOException {
        if (validate(remoteSite)) {
            if (!file.startsWith("/"))
                file = "/" + file;
            return addToDownloadQueue(protocolHandler.getProtocolFileFor(remoteSite,
                    protocolHandler.getAppropriateProtocolBySite(remoteSite,
                            true), file, false), renamingString, downloadToDir,
                    uniqueMetadataElement, deleteAfterDownload, fileMetadata);
        } else
            throw new ProtocolException("Not a valid remote site " + remoteSite);
    }

    public boolean validate(RemoteSite remoteSite) {
        Preconditions.checkNotNull(remoteSite);
        LinkedList<RemoteSite> remoteSites = this.siteInfo
                .getPossibleRemoteSites(remoteSite.getAlias(), remoteSite
                        .getURL(), remoteSite.getUsername(), remoteSite
                        .getPassword());
        if (remoteSites.size() == 1) {
            RemoteSite rs = remoteSites.get(0);
            remoteSite.copy(rs);
            return true;
        }
        return false;
    }

    public void waitUntilAllCurrentDownloadsAreComplete()
            throws ProtocolException {
        synchronized (this) {
            for (int i = 0; i < 180; i++) {
                try {
                    if (this.avaliableSessions.size() == this.numberOfSessions)
                        return;
                    else
                        this.wait(5000);
                } catch (Exception e) {
                }
            }
            throw new ProtocolException(
                    "Downloads appear to be hanging . . . aborting wait . . . waited for 15 minutes");
        }
    }

  public boolean addToDownloadQueue(RemoteSiteFile file,
                                    String renamingString,
                                    File downloadToDir,
                                    String uniqueMetadataElement,
                                    boolean deleteAfterDownload,
                                    Metadata fileMetadata) throws ToManyFailedDownloadsException,
                                                                  RemoteConnectionException,
                                                                  AlreadyInDatabaseException,
                                                                  UndefinedTypeException,
                                                                  CatalogException,
                                                                  IOException {
        if (this.failedDownloadList.size() > max_allowed_failed_downloads)
            throw new ToManyFailedDownloadsException(
                    "Number of failed downloads exceeds "
                            + max_allowed_failed_downloads
                            + " . . . blocking all downloads from being added to queue . . . "
                            + "reset error flag in order to force allow downloads into queue");
        if (this.isDownloading(file)) {
            LOG.log(Level.WARNING, "Skipping file '" + file
                    + "' because it is already on the download queue");
            return false;
        }

        RemoteFile remoteFile = new RemoteFile(file);
        remoteFile.addMetadata(fileMetadata);
        remoteFile.addMetadata(RemoteFile.RENAMING_STRING, renamingString);
        remoteFile.addMetadata(RemoteFile.DELETE_AFTER_DOWNLOAD,
                deleteAfterDownload + "");

        if (config.onlyDownloadDefinedTypes()) {
           String mimeType = this.mimeTypeDetection.getMimeType(file.getName());
           if (mimeType != null
                   && !mimeType.equals("application/octet-stream")) {
               remoteFile.addMetadata(RemoteFile.MIME_TYPE, mimeType);
               remoteFile.addMetadata(RemoteFile.SUPER_TYPE, this.mimeTypeDetection
                       .getSuperTypeForMimeType(mimeType));
               String description = this.mimeTypeDetection
                       .getDescriptionForMimeType(mimeType);
               if (!Strings.isNullOrEmpty(description)) {
                 if(description.indexOf("&") != -1){
                   for (String field : description.split("\\&\\&")) {
                     String[] keyval = field.split("\\=");
                     remoteFile.addMetadata(keyval[0].trim(), keyval[1].trim());
                   }
                 } else{
                   // it's the ProductType
                   remoteFile.addMetadata(RemoteFile.PRODUCT_TYPE, description);
                 }
                 if (remoteFile.getMetadata(RemoteFile.UNIQUE_ELEMENT) != null) {
                    uniqueMetadataElement = remoteFile.getMetadata(RemoteFile.UNIQUE_ELEMENT);
                 }
               }
           } else {
              throw new UndefinedTypeException("File '" + file
                    + "' is not a defined type");
           }
        }

        downloadToDir = new File(downloadToDir.isAbsolute() ? downloadToDir
                .getAbsolutePath() : this.config.getBaseStagingArea() + "/"
                + downloadToDir.getPath());
        if (!this.isStagingAreaInitialized(downloadToDir))
            this.initializeStagingArea(downloadToDir);

        remoteFile.addMetadata(RemoteFile.DOWNLOAD_TO_DIR, downloadToDir.getAbsolutePath());

    	if (remoteFile.getMetadata(RemoteFile.PRODUCT_NAME_GENERATOR) != null) {
    		remoteFile.addMetadata(RemoteFile.PRODUCT_NAME, RenamingConvention.rename(remoteFile, remoteFile.getMetadata(RemoteFile.PRODUCT_NAME_GENERATOR)));
    	}else {
    		remoteFile.setUniqueMetadataElement(uniqueMetadataElement == null ? RemoteFile.FILENAME : uniqueMetadataElement);
    	}

        if (!isAlreadyInDatabase(remoteFile)) {

            // get download location
            File newFile = getSaveToLoc(remoteFile);

            // add session to thread pool
            if (!this.isInStagingArea(newFile)) {
                for (int retries = 0;; retries++) {
                    try {
                        addSessionToThreadPool(
                                getNextAvaliableSession(remoteFile
                                        .getProtocolFile()), remoteFile,
                                newFile);
                        return true;
                    } catch (Exception e) {
                        if (retries < MAX_RETRIES) {
                            LOG.log(Level.WARNING, "Failed to get session for "
                                    + file + " . . . retrying in 5 secs");
                            synchronized (this) {
                                try {
                                    wait(5000);
                                } catch (Exception e1) {
                                }
                            }
                        } else {
                            this.failedDownloadList.add(file);
                            throw new RemoteConnectionException(
                                    "Failed to get session to download " + file
                                            + " : " + e.getMessage(), e);
                        }
                    }
                }
            } else {
                if (deleteAfterDownload) {
                    try {
                        protocolHandler
                                .delete(protocolHandler.getAppropriateProtocol(
                                        file, true, true), file);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE,
                                "Failed to delete file from server : "
                                        + e.getMessage());
                    }
                }
                LOG.log(Level.WARNING, "Skipping file " + file
                        + " because it is already in staging area");
                return false;
            }
        } else
            throw new AlreadyInDatabaseException("File " + file
                    + " is already the database");
    }

    private boolean isStagingAreaInitialized(File stagingArea) {
        return this.stagingAreas.contains(stagingArea);
    }

    private boolean isInStagingArea(final File findFile) {
        return (findFile.exists() || new File(findFile.getParentFile(),
                "Downloading_" + findFile.getName()).exists());
    }

    private void initializeStagingArea(File stagingArea) throws IOException {
        LOG.log(Level.INFO, "Preparing staging area " + stagingArea);
        if (stagingArea.exists()) {
            File[] failedDownloads = stagingArea.listFiles(new FileFilter() {
                @Override
               public boolean accept(File pathname) {
                    return pathname.getName().startsWith("Downloading_");
                }
            });
            for (File file : failedDownloads) {
                LOG.log(Level.INFO, "Removing failed download file "
                        + file.getAbsolutePath());
                file.delete();
            }
        } else {
            LOG.log(Level.INFO, "Staging area " + stagingArea.getAbsolutePath()
                    + " does not exist! -- trying to create it ");
            if (!stagingArea.mkdirs())
                throw new IOException("Failed to create staging area at "
                        + stagingArea.getAbsolutePath());
        }
        this.stagingAreas.add(stagingArea);
    }

    File getSaveToLoc(RemoteFile remoteFile) {
        String renamingString = remoteFile
                .getMetadata(RemoteFile.RENAMING_STRING);
        if (renamingString == null || renamingString.equals("")) {
            return new File(remoteFile.getMetadata(RemoteFile.DOWNLOAD_TO_DIR)
                    + "/" + remoteFile.getMetadata(RemoteFile.FILENAME));
        } else {
            File newFile = new File(remoteFile
                    .getMetadata(RemoteFile.DOWNLOAD_TO_DIR)
                    + "/"
                    + RenamingConvention.rename(remoteFile, renamingString));
            if (!newFile.getParentFile().equals(
                    remoteFile.getMetadata(RemoteFile.DOWNLOAD_TO_DIR)))
                newFile.getParentFile().mkdirs();
            return newFile;
        }
    }

    Protocol getNextAvaliableSession(RemoteSiteFile file) throws CrawlerException {
        // wait for available session, then load it
        Protocol session;
        while ((session = getSession(file)) == null) {
            try {
                waitMainThread();
            } catch (InterruptedException e1) {
            }
        }
        return session;
    }

    /**
     * Sleeps the crawling thread
     *
     * @throws InterruptedException
     */
    synchronized void waitMainThread() throws InterruptedException {
        wait();
    }

    /**
     * Wakes up the crawling thread
     */
    synchronized void wakeUpMainThread() {
        notify();
    }

    /**
     * Increments the number of downloading session
     */
    synchronized void incrementSessions() {
        numberOfSessions++;
    }

    synchronized void decrementSessions() {
        this.numberOfSessions--;
    }

    /**
     * Gets an available downloading session Protocol. Returns null if none are
     * available
     *
     * @param path
     *            The session returned will be checked against the Path passed
     *            in and if not presently connected to the Path's URL, it will
     *            be disconnected from it's current server and connected to the
     *            server specified by the Path.
     * @return The found downloading session Protocol
     * @throws RemoteCommunicationException
     *             If downloading session Protocol has to be reconnected and
     *             there is an error communicating with the server
     */
    synchronized Protocol getSession(RemoteSiteFile file) throws CrawlerException {
        try {
            Protocol session = null;
			if (file.getSite().getMaxConnections() < 0
					|| file.getSite().getMaxConnections() > this.getCurrentlyDownloadingFiles().size()) {
	            if (avaliableSessions.size() > 0) {
	                session = modifyAvailableSessionForPath(file);
	            } else if (numberOfSessions < max_sessions) {
	                session = createNewSessionForPath(file);
	                incrementSessions();
	            }
            }
            return session;
        } catch (Exception e) {
            throw new CrawlerException("Failed to get new session : "
                    + e.getMessage(), e);
        }
    }

    Protocol createNewSessionForPath(RemoteSiteFile file)
            throws RemoteConnectionException {
        return protocolHandler.getAppropriateProtocol(file, /* reuse */false, /* navigate */
        true);
    }

    Protocol modifyAvailableSessionForPath(RemoteSiteFile file)
            throws ProtocolException, RemoteConnectionException {
        Protocol session = getAvailableSession();
        if (!file.getSite().getURL().getHost().equals(
                file.getSite().getURL().getHost())
                || !protocolHandler.isProtocolConnected(session)) {
            protocolHandler.disconnect(session);
            session = protocolHandler.getAppropriateProtocol(file, /* reuse */
            false, /* navigate */true);
        } else {
            try {
                if (file.isDir())
                    protocolHandler.cd(session, file);
                else
                    protocolHandler.cd(session,
                          new RemoteSiteFile(file.getParent(), file.getSite()));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    protocolHandler.disconnect(session);
                } catch (Exception exc) {
                }
                session = protocolHandler.getAppropriateProtocol(file, /* reuse */
                false, /* navigate */true);
            }
        }
        return session;
    }

    /**
     * Puts a session in the available session list
     *
     * @param session
     *            The Protocol session to be added to the available list
     */
    synchronized void addAvailableSession(Protocol session) {
        avaliableSessions.add(session);
    }

    /**
     * Removes a session from the available list and returns it
     *
     * @return An available downloading Protocol session
     */
    synchronized Protocol getAvailableSession() {
        return avaliableSessions.remove(0);
    }

    synchronized int getNumberOfUsedSessions() {
        return numberOfSessions - avaliableSessions.size();
    }

    /**
     * Registers a downloading session with the threadpoolexecutor to begin
     * downloading the specified ProtocolFile to the local File location
     *
     * @param session
     *            The downloading Protocol session to be used to download the
     *            ProtocolFile
     * @param protocolFile
     *            The file to be downloaded
     * @param newFile
     *            The location which the downloaded file will be stored
     */
    void addSessionToThreadPool(final Protocol session,
            final RemoteFile remoteFile, final File newFile) {
        this.addToDownloadingList(remoteFile.getProtocolFile());
        threadController.execute(new Runnable() {
            @Override
            public void run() {
                boolean successful = false;
                int retries = 0;
                Protocol curSession = session;

                if (FileRetrievalSystem.this.dListener != null)
                    FileRetrievalSystem.this.dListener
                            .downloadStarted(remoteFile.getProtocolFile());

                // try until successful or all retries have been used
                do {
                    try {
                        // if thread tracker is to be used
                        if (config.useTracker()) {
                            dtEval.startTrackingDownloadRuntimeForFile(newFile);
                            protocolHandler.download(curSession, remoteFile
                                    .getProtocolFile(), newFile, remoteFile
                                    .getMetadata(
                                            RemoteFile.DELETE_AFTER_DOWNLOAD)
                                    .equals("true"));
                            dtEval.fileDownloadComplete(newFile);
                            threadController
                                    .setCorePoolSize(max_sessions = dtEval
                                            .getRecommendedThreadCount());
                            threadController.setMaximumPoolSize(max_sessions);
                            // if static number of threads are to be used
                        } else {
                            protocolHandler.download(curSession, remoteFile
                                    .getProtocolFile(), newFile, remoteFile
                                    .getMetadata(
                                            RemoteFile.DELETE_AFTER_DOWNLOAD)
                                    .equals("true"));
                        }

                        successful = true;
                        if (FileRetrievalSystem.this.dListener != null)
                            FileRetrievalSystem.this.dListener
                                    .downloadFinished(remoteFile
                                            .getProtocolFile());

                        remoteFile.addMetadata(RemoteFile.FILE_SIZE, newFile
                                .length()
                                + "");

                        // try to create the metadata file
                        if (config.getWriteMetFile()) {
	                        try {
	                        	LOG.log(Level.INFO, "Writing metadata file for '" + newFile + "'");
	                            remoteFile.addMetadata(RemoteFile.FILE_SIZE,
	                                    newFile.length() + "");
	                            remoteFile.writeToPropEqValFile(newFile
	                                    .getAbsolutePath()
	                                    + "." + config.getMetFileExtension(),
	                                    config.getListOfMetadataToOutput());
	                        } catch (Exception e) {
	                            LOG.log(Level.SEVERE,
	                                    "Failed to create metadata file for "
	                                            + remoteFile.getProtocolFile());
	                        }
                        }

                    } catch (Exception e) {

                        // if tracker is being used cancel tracking
                        if (config.useTracker())
                            dtEval.cancelRuntimeTracking(newFile);

                        // delete any created file from staging area
                        newFile.delete();
                        new File(newFile.getAbsolutePath() + "."
                                + config.getMetFileExtension()).delete();

                        // check if a retry is still allowed
                        if (++retries > MAX_RETRIES) {
                            FileRetrievalSystem.this.failedDownloadList
                                    .add(remoteFile.getProtocolFile());
                            LOG.log(Level.SEVERE, "Failed to download "
                                    + remoteFile.getProtocolFile() + " : "
                                    + e.getMessage());
                            if (FileRetrievalSystem.this.dListener != null)
                                FileRetrievalSystem.this.dListener
                                        .downloadFailed(remoteFile
                                                .getProtocolFile(), e
                                                .getMessage());
                            break;
                        } else if (FileRetrievalSystem.this.failedDownloadList
                                .size() < max_allowed_failed_downloads) {
                            // discard current session and recreate a new
                            // session to try to download file with
                            LOG.log(Level.WARNING, "Retrying to download file "
                                    + remoteFile.getProtocolFile()
                                    + " because download failed : "
                                    + e.getMessage(), e);
                            try {
                                protocolHandler.disconnect(curSession);
                            } catch (Exception exc) {
                            }
                            try {
                                curSession = protocolHandler
                                        .getAppropriateProtocol(remoteFile
                                                .getProtocolFile(), false, true);
                            } catch (Exception exc) {
                                LOG.log(Level.SEVERE,
                                        "Failed to reconnect protocol to retry download of file "
                                                + remoteFile.getProtocolFile()
                                                + " -- aborting retry : "
                                                + e.getMessage(), e);
                            }
                        } else {
                            LOG
                                    .log(
                                            Level.SEVERE,
                                            "Terminating download tries for file "
                                                    + remoteFile
                                                            .getProtocolFile()
                                                    + " do to too many previous download failures : "
                                                    + e.getMessage(), e);
                            if (FileRetrievalSystem.this.dListener != null)
                                FileRetrievalSystem.this.dListener
                                        .downloadFailed(remoteFile
                                                .getProtocolFile(), e
                                                .getMessage());
                            break;
                        }

                    }
                } while (!successful);

                FileRetrievalSystem.this.removeFromDownloadingList(remoteFile
                        .getProtocolFile());
                determineSessionFate(curSession);
            }
        });
    }

    private synchronized void addToDownloadingList(ProtocolFile pFile) {
        this.currentlyDownloading.add(pFile);
    }

    private synchronized void removeFromDownloadingList(ProtocolFile pFile) {
        this.currentlyDownloading.remove(pFile);
    }

    public synchronized boolean isDownloading(ProtocolFile pFile) {
        return this.currentlyDownloading.contains(pFile);
    }

    public synchronized LinkedList<ProtocolFile> getCurrentlyDownloadingFiles() {
        LinkedList<ProtocolFile> list = new LinkedList<ProtocolFile>();
        list.addAll(this.currentlyDownloading);
        return list;
    }

    public LinkedList<ProtocolFile> getListOfFailedDownloads() {
        return this.failedDownloadList;
    }

    public void clearFailedDownloadsList() {
        this.failedDownloadList.clear();
    }

    synchronized void determineSessionFate(Protocol session) {
        // determine whether thread should be keep or should be thrown away
        if (numberOfSessions <= max_sessions) {
            giveBackSession(session);
        } else {
            disposeOfSession(session);
        }
    }

    void giveBackSession(Protocol session) {
        addAvailableSession(session);
        wakeUpMainThread();
    }

    void disposeOfSession(Protocol session) {
        try {
            protocolHandler.disconnect(session);
        } catch (Exception e) {
            // log failure
        }
        numberOfSessions--;
    }

    public void shutdown() {
        try {
            // close out threadpool
            threadController.shutdown();
            // give a max of 10 minutes to finish downloading any files
            threadController.awaitTermination(600, TimeUnit.SECONDS);
        } catch (Exception e) {
            // log failure
        }

        try {
            this.resetVariables();
        } catch (Exception e) {

        }

        try {
            closeSessions();
        } catch (Exception e) {
            // log failure!!!
        }

        try {
            protocolHandler.close();
        } catch (Exception e) {
            // log failure!!!
        }

    }

    /**
     * Disconnects all downloading Protocol sessions in the avaiableSessions
     * list. The ThreadPoolExecutor needs to be completely shutdown before this
     * method should be called. Otherwise some Protocols might not be
     * disconnected or left downloading.
     *
     * @return True if successful, false otherwise
     * @throws RemoteConnectionException
     */
    public boolean closeSessions() throws RemoteConnectionException {
        for (Protocol session : avaliableSessions) {
            protocolHandler.disconnect(session);
        }
        // sessions.clear();
        avaliableSessions.clear();
        numberOfSessions = 0;
        return true;
    }
}
