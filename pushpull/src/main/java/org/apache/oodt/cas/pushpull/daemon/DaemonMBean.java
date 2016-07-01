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


package org.apache.oodt.cas.pushpull.daemon;

//JDK imports
import java.io.File;
import java.util.Date;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface DaemonMBean {

    /**
     * Will terminate the CrawlDaemon. If its Crawler is crawling a site when
     * this method is called, the terminate won't take place until after the
     * Crawler has complete crawling that site.
     * 
     * @return
     */
    void quit();

    /**
     * Can be used to determine if Crawler is presently running
     * 
     * @return true if Crawler is runnning
     * @uml.property name="isRunning"
     */
    boolean isRunning();

    boolean getHasBeenToldToQuit();

    long getTimeIntervalInMilliseconds();

    long getEpsilonInMilliseconds();

    boolean getRunOnReboot();

    Date getFirstRunDateTime();

    String[] downloadedFilesInStagingArea();

    String[] downloadingFilesInStagingArea();

    int numberOfFilesDownloadedInStagingArea();

    int numberOfFilesDownloadingInStagingArea();

    String getDataFilesRemoteSite();

    String getDataFilesRenamingConv();

    boolean getDeleteDataFilesFromServer();

    String getQueryMetadataElementName();

    File getDataFilesStagingArea();

    boolean getAllowAliasOverride();

    String getPropertyFilesRemoteSite();

    String getPropertyFilesRenamingConv();

    boolean getDeletePropertyFilesFromServer();

    String getPropertyFilesOnSuccessDir();

    String getPropertyFilesOnFailDir();

    File getPropertyFilesLocalDir();

}
