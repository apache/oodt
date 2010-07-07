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
import org.apache.oodt.cas.pushpull.exceptions.ThreadEvaluatorException;

//JDK imports
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DownloadThreadEvaluator {

    private HashMap<File, DownloadingFileInfo> fileAndDownloadingFileInfo;

    private final int MAX_THREADS;

    private int currentThreadCount;

    private double[] downloadSpeedsForEachThread;

    public DownloadThreadEvaluator(int maxThreads) {
        this.MAX_THREADS = maxThreads;
        downloadSpeedsForEachThread = new double[maxThreads + 1];
        fileAndDownloadingFileInfo = new HashMap<File, DownloadingFileInfo>();
        currentThreadCount = 0;
    }

    public synchronized void startTrackingDownloadRuntimeForFile(File file)
            throws ThreadEvaluatorException {
        long curTime = System.currentTimeMillis();
        if (++this.currentThreadCount > this.MAX_THREADS)
            throw new ThreadEvaluatorException(
                    "Number of threads exceeds max allows threads");
        updateThreadCounts(curTime);
        fileAndDownloadingFileInfo.put(file, new DownloadingFileInfo(file,
                curTime, this.currentThreadCount));
    }

    private void updateThreadCounts(long curTime) {
        Set<Entry<File, DownloadingFileInfo>> entrySet = fileAndDownloadingFileInfo
                .entrySet();
        for (Entry<File, DownloadingFileInfo> entry : entrySet) {
            entry.getValue()
                    .updateThreadCount(curTime, this.currentThreadCount);
        }
    }

    public synchronized void cancelRuntimeTracking(File file) {
        fileAndDownloadingFileInfo.remove(file);
        currentThreadCount--;
        updateThreadCounts(System.currentTimeMillis());
    }

    public synchronized void fileDownloadComplete(File file)
            throws ThreadEvaluatorException {
        try {
            long finishTime = System.currentTimeMillis();
            DownloadingFileInfo dfi = fileAndDownloadingFileInfo.remove(file);
            updateThreadCounts(finishTime);
            LinkedList<TimeAndThreadCount> tatcList = dfi
                    .getTimeAndThreadInfo();
            long runtime = finishTime - dfi.getStartTimeInMillis();
            double total = 0;
            long nextTime;
            for (int i = 0; i < tatcList.size(); i++) {
                TimeAndThreadCount tatc = tatcList.get(i);
                if (i + 1 >= tatcList.size())
                    nextTime = finishTime;
                else
                    nextTime = tatcList.get(i + 1).getStartTimeInMillis();
                long threadCountTime = nextTime - tatc.getStartTimeInMillis();
                total += ((double) (tatc.getThreadCount() * threadCountTime))
                        / (double) runtime;
            }
            int avgThreadCountForFile = (int) Math.rint(total);
            System.out.println("Recorded avg: " + avgThreadCountForFile);

            double downloadSpeed = (file.length() * avgThreadCountForFile)
                    / calculateRuntime(dfi.getStartTimeInMillis());
            double currentAvgSpeed = this.downloadSpeedsForEachThread[avgThreadCountForFile];
            if (currentAvgSpeed == 0)
                this.downloadSpeedsForEachThread[avgThreadCountForFile] = downloadSpeed;
            else
                this.downloadSpeedsForEachThread[avgThreadCountForFile] = (currentAvgSpeed + downloadSpeed) / 2;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ThreadEvaluatorException("Failed to register file "
                    + file + " as downloaded : " + e.getMessage());
        } finally {
            currentThreadCount--;
        }
    }

    long calculateRuntime(final long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    public synchronized int getRecommendedThreadCount() {

        int curRecThreadCount = 1;
        double curMaxSpeed = this.downloadSpeedsForEachThread[curRecThreadCount];
        for (int i = 1; i < this.downloadSpeedsForEachThread.length; i++) {
            double curSpeed = this.downloadSpeedsForEachThread[i];
            if (curSpeed > curMaxSpeed) {
                curMaxSpeed = curSpeed;
                curRecThreadCount = i;
            }
        }

        if (curRecThreadCount != this.MAX_THREADS
                && this.downloadSpeedsForEachThread[curRecThreadCount + 1] == 0)
            curRecThreadCount++;
        else if (this.downloadSpeedsForEachThread[curRecThreadCount - 1] == 0)
            curRecThreadCount--;

        System.out.print("[ ");
        for (double time : downloadSpeedsForEachThread)
            System.out.print(time + " ");
        System.out.println("]");

        System.out.println("Recommended Threads: " + curRecThreadCount);

        return curRecThreadCount;
    }

}
