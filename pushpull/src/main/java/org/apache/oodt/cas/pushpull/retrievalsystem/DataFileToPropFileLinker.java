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

//JDK imports
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

//OODT imports
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DataFileToPropFileLinker implements DownloadListener {

    private HashMap<String, File> protocolFilePathAndPropFileMap;

    private HashMap<File, String> propFileToErrorsMap;

    private LinkedList<ProtocolFile> downloadingDataFiles;

    private LinkedList<ProtocolFile> failedDataFiles;

    private LinkedList<ProtocolFile> successDataFiles;

    public DataFileToPropFileLinker() {
        this.protocolFilePathAndPropFileMap = new HashMap<String, File>();
        this.propFileToErrorsMap = new HashMap<File, String>();
        downloadingDataFiles = new LinkedList<ProtocolFile>();
        failedDataFiles = new LinkedList<ProtocolFile>();
        successDataFiles = new LinkedList<ProtocolFile>();
    }

    public synchronized void addPropFileToDataFileLink(File propFile,
            ProtocolFile pFile) {
        this.addPropFileToDataFileLink(propFile, pFile.getProtocolPath()
                .getPathString());
    }

    public synchronized void addPropFileToDataFileLink(File propFile,
            String remoteDataFilePath) {
        this.protocolFilePathAndPropFileMap.put(remoteDataFilePath, propFile);
    }

    public synchronized void markAsFailed(File propFile, String errorMsg) {
        String errors = this.propFileToErrorsMap.get(propFile);
        if (errors == null)
            this.propFileToErrorsMap.put(propFile, errorMsg);
        else
            this.propFileToErrorsMap.put(propFile, errors + "," + errorMsg);
    }

    public synchronized void markAsFailed(ProtocolFile pFile, String errorMsg) {
        this.markAsFailed(pFile.getProtocolPath().getPathString(), errorMsg);
    }

    public synchronized void markAsFailed(String pFilePath, String errorMsg) {
        File propFile = this.protocolFilePathAndPropFileMap.get(pFilePath);
        if (propFile != null) {
            String errors = this.propFileToErrorsMap.get(propFile);
            if (errors == null)
                this.propFileToErrorsMap.put(propFile, errorMsg);
            else
                this.propFileToErrorsMap.put(propFile, errors + "," + errorMsg);
        }
    }

    public synchronized String getErrorsAndEraseLinks(File propFile) {
        this.eraseLinks(propFile);
        return this.getErrors(propFile);
    }

    public synchronized void eraseLinks(File propFile) {
        LinkedList<String> keysToRemove = new LinkedList<String>();
        for (Entry<String, File> entry : this.protocolFilePathAndPropFileMap
                .entrySet())
            if (entry.getValue().equals(propFile))
                keysToRemove.add(entry.getKey());
        for (String key : keysToRemove)
            this.protocolFilePathAndPropFileMap.remove(key);
    }

    public synchronized String getErrors(File propFile) {
        return this.propFileToErrorsMap.remove(propFile);
    }

    public synchronized String getStatusOf(File propFile) {
        return "Status for " + propFile.getAbsolutePath() + ":" + " Errors: \n"
                + "   " + this.propFileToErrorsMap.get(propFile) + "\n"
                + " Data files specified which are currently downloading:\n"
                + "   " + this.getDownloadingFilesLinkedToPropFile(propFile)
                + "\n"
                + " Data files specified which successfully downloaded:\n"
                + "   "
                + this.getSuccessfullyDownloadedFilesLinkedToPropFile(propFile)
                + "\n" + " Data files specified which failed to download:\n"
                + "   "
                + this.getFailedToDownloadFilesLinkedToPropFile(propFile)
                + "\n";
    }

    public synchronized LinkedList<ProtocolFile> getDownloadingFilesLinkedToPropFile(
            File propFile) {
        return this.getFilesLinkedToPropFileInList(propFile,
                this.downloadingDataFiles);
    }

    public synchronized LinkedList<ProtocolFile> getSuccessfullyDownloadedFilesLinkedToPropFile(
            File propFile) {
        return this.getFilesLinkedToPropFileInList(propFile,
                this.successDataFiles);
    }

    public synchronized LinkedList<ProtocolFile> getFailedToDownloadFilesLinkedToPropFile(
            File propFile) {
        return this.getFilesLinkedToPropFileInList(propFile,
                this.failedDataFiles);
    }

    private LinkedList<ProtocolFile> getFilesLinkedToPropFileInList(
            File propFile, LinkedList<ProtocolFile> list) {
        LinkedList<ProtocolFile> returnList = new LinkedList<ProtocolFile>();
        for (ProtocolFile pFile : list) {
            if (this.protocolFilePathAndPropFileMap.get(pFile.getProtocolPath()
                    .getPathString()) != null)
                returnList.add(pFile);
        }
        return returnList;
    }

    public synchronized void downloadFailed(ProtocolFile file, String errorMsg) {
        this.markAsFailed(file, "Failed to download '" + file
                + "' -- Logged Error msg: " + errorMsg);
        this.failedDataFiles.add(file);
        this.downloadingDataFiles.remove(file);
    }

    public synchronized void downloadFinished(ProtocolFile file) {
        this.successDataFiles.add(file);
        this.downloadingDataFiles.remove(file);
    }

    public synchronized void downloadStarted(ProtocolFile file) {
        this.downloadingDataFiles.add(file);
    }

    public synchronized void clear() {
        this.propFileToErrorsMap.clear();
        this.protocolFilePathAndPropFileMap.clear();
        this.downloadingDataFiles.clear();
        this.failedDataFiles.clear();
        this.successDataFiles.clear();
    }
}
