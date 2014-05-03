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


package org.apache.oodt.cas.pushpull.config;

//OODT imports
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;

//JDK imports
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class PropFilesInfo {

    private File localDir;

    private boolean deleteOnSuccess;

    private File successLoc;

    private File failLoc;

    private LinkedList<RegExpAndParser> patterns;

    private HashMap<File, Parser> fileToParserMap;

    private DownloadInfo di;

    public PropFilesInfo(File localDir) {
        this.localDir = localDir;
        patterns = new LinkedList<RegExpAndParser>();
    }

    public void setDownloadInfo(DownloadInfo di,
            HashMap<File, Parser> fileToParserMap) {
        this.di = di;
        this.fileToParserMap = fileToParserMap;
    }

    public LinkedList<File> getDownloadInfoPropFiles() {
        LinkedList<File> returnList = new LinkedList<File>();
        for (Entry<File, Parser> entry : this.fileToParserMap.entrySet())
            returnList.add(entry.getKey());
        return returnList;
    }

    public void setAfterUseEffects(File successLoc, File failLoc) {
        this.successLoc = successLoc;
        this.failLoc = failLoc;
    }

    public void addPropFiles(String regExp, Parser parser) {
        patterns.add(new RegExpAndParser(regExp, parser));
    }

    public Parser getParserForFile(File propFile) {
        Parser parser = this.fileToParserMap == null ? null
                : this.fileToParserMap.get(propFile);
        if (parser == null)
            parser = this.getParserForFilename(propFile.getName());
        return parser;
    }

    public Parser getParserForFilename(String propFilename) {
        for (RegExpAndParser pattern : patterns) {
            if (pattern.isAcceptedByPattern(propFilename))
                return pattern.getParser();
        }
        return null;
    }

    public boolean needsToBeDownloaded() {
        return di != null;
    }

    public DownloadInfo getDownloadInfo() {
        return this.di;
    }

    public File getFinalDestination(boolean success) {
        return success ? this.getOnSuccessDir() : this.getOnFailDir();
    }

    public File getOnSuccessDir() {
        return (this.successLoc == null) ? this.localDir : this.successLoc;
    }

    public File getOnFailDir() {
        return (this.failLoc == null) ? this.localDir : this.failLoc;
    }

    public File getLocalDir() {
        return this.localDir;
    }

    public void setDeleteOnSuccess(boolean deleteOnSuccess) {
        this.deleteOnSuccess = deleteOnSuccess;
    }

    public boolean getDeleteOnSuccess() {
      return deleteOnSuccess;
    }

    public String toString() {
        return "PropFilesInfo\n" + "   " + "Local directory: " + this.localDir
                + "\n" + "   " + "Patterns: " + this.patterns + "\n" + "   "
                + "Delete on success: " + this.deleteOnSuccess + "\n" + "   "
                + "Move to directory on success: " + this.successLoc + "\n"
                + "   " + "Move to directory on fail: " + this.failLoc + "\n"
                + "   " + this.di + "\n";
    }

    private class RegExpAndParser {
        private String pattern;

        private Parser parser;

        public RegExpAndParser(String pattern, Parser parser) {
            this.pattern = pattern;
            this.parser = parser;
        }

        public boolean isAcceptedByPattern(String filename) {
            return Pattern.matches(pattern, filename);
        }

        public Parser getParser() {
            return this.parser;
        }

        public String getPattern() {
            return this.pattern;
        }
    }

}
