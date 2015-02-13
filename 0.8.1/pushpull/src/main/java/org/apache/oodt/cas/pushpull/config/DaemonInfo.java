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

//JDK imports
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class DaemonInfo {

    private static SimpleDateFormat utcFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'hh:mm:ss'Z'");

    private Date firstRunDateTime;

    private long timeIntervalInMilliseconds;

    private long epsilonInMilliseconds;

    private boolean runOnReboot;

    private PropFilesInfo pfi;

    private DataFilesInfo dfi;

    public DaemonInfo(String firstRunDateTimeString, String period,
            String epsilon, boolean runOnReboot, PropFilesInfo pfi,
            DataFilesInfo dfi) throws ParseException {
        this.runOnReboot = runOnReboot;
        if (firstRunDateTimeString != null
                && !firstRunDateTimeString.equals(""))
            this.firstRunDateTime = utcFormat.parse(firstRunDateTimeString);
        else
            this.firstRunDateTime = new Date();
        if (period != null && !period.equals(""))
            this.timeIntervalInMilliseconds = Long.parseLong(period.substring(
                    0, period.length() - 1))
                    * this.getMillisecondsInMetric((period.charAt(period
                            .length() - 1) + "").toLowerCase());
        else
            this.timeIntervalInMilliseconds = -1;
        if (epsilon != null && !epsilon.equals(""))
            this.epsilonInMilliseconds = Long.parseLong(epsilon.substring(0,
                    epsilon.length() - 1))
                    * this.getMillisecondsInMetric((epsilon.charAt(epsilon
                            .length() - 1) + "").toLowerCase());
        else
            this.epsilonInMilliseconds = -1;
        this.pfi = pfi;
        this.dfi = dfi;
    }

    private long getMillisecondsInMetric(String stringMetric) {
        switch (stringMetric.charAt(0)) {
        case 'w':
            return 604800000;
        case 'd':
            return 86400000;
        case 'h':
            return 3600000;
        case 'm':
            return 60000;
        case 's':
            return 1000;
        default:
            return -1;
        }
    }

    public PropFilesInfo getPropFilesInfo() {
        return this.pfi;
    }

    public DataFilesInfo getDataFilesInfo() {
        return this.dfi;
    }

    public long getTimeIntervalInMilliseconds() {
        return this.timeIntervalInMilliseconds;
    }

    public long getEpsilonInMilliseconds() {
        return this.epsilonInMilliseconds;
    }

    public boolean runOnReboot() {
        return this.runOnReboot;
    }

    public Date getFirstRunDateTime() {
        return this.firstRunDateTime;
    }

    public String toString() {
        return "--------DaemonInfo--------\n" + "   " + "First run date/time: "
                + this.firstRunDateTime + "\n" + "   "
                + "Period in milliseconds: " + this.timeIntervalInMilliseconds
                + "\n" + "   " + "Epsilon in milliseoncs: "
                + this.epsilonInMilliseconds + "\n" + "   " + "Run on reboot: "
                + this.runOnReboot + "\n" + this.dfi + this.pfi + "\n";
    }
}
