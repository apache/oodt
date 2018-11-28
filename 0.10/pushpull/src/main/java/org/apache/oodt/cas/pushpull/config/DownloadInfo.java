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
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;

//JDK imports
import java.io.File;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DownloadInfo {

    private RemoteSite remoteSite;

    private String renamingConv;

    private boolean deleteFromServer;

    private File stagingArea;

    private boolean allowAliasOverride;

    public DownloadInfo(RemoteSite remoteSite, String renamingConv,
            boolean deleteFromServer, File stagingArea,
            boolean allowAliasOverride) {
        this.remoteSite = remoteSite;
        this.renamingConv = renamingConv;
        this.deleteFromServer = deleteFromServer;
        this.stagingArea = stagingArea;
        this.allowAliasOverride = allowAliasOverride;
    }

    public RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public String getRenamingConv() {
        return this.renamingConv;
    }

    public boolean deleteFromServer() {
        return this.deleteFromServer;
    }

    public File getStagingArea() {
        return this.stagingArea;
    }

    public boolean isAllowAliasOverride() {
        return this.allowAliasOverride;
    }

    public String toString() {
        return "DataFileInfo:\n" + "  " + this.remoteSite + "\n" + "  "
                + "RenamingConvension: " + this.renamingConv + "\n" + "  "
                + "StagingArea: " + this.getStagingArea().getAbsolutePath()
                + "\n" + "  " + "Delete files from server: "
                + this.deleteFromServer + "\n" + "  "
                + "Allow alias override: " + this.allowAliasOverride + "\n";
    }
}
