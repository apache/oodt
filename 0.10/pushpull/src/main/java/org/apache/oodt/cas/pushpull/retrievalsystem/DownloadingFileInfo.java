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
import java.util.LinkedList;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DownloadingFileInfo {

    private File downloadingFile;

    private long startTimeInMillis;

    private LinkedList<TimeAndThreadCount> timeAndThreadCountList;

    public DownloadingFileInfo(File downloadingFile, long startTimeInMillis,
            int currentThreadCount) {
        timeAndThreadCountList = new LinkedList<TimeAndThreadCount>();
        this.downloadingFile = downloadingFile;
        this.startTimeInMillis = startTimeInMillis;
        this.timeAndThreadCountList.add(new TimeAndThreadCount(
                startTimeInMillis, currentThreadCount));
    }

    public void updateThreadCount(long timeInMillis, int threadCount) {
        this.timeAndThreadCountList.add(new TimeAndThreadCount(timeInMillis,
                threadCount));
    }

    public File getDownloadingFile() {
        return this.downloadingFile;
    }

    public long getStartTimeInMillis() {
        return this.startTimeInMillis;
    }

    public LinkedList<TimeAndThreadCount> getTimeAndThreadInfo() {
        return this.timeAndThreadCountList;
    }
}
