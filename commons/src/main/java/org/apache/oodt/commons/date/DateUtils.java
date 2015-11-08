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


package org.apache.oodt.commons.date;

//JDK imports

import org.apache.oodt.commons.exceptions.CommonsException;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public class DateUtils {

    public enum FormatType { UTC_FORMAT, LOCAL_FORMAT, TAI_FORMAT }
    
    public static Calendar tai93epoch = new GregorianCalendar(1993, GregorianCalendar.JANUARY, 1);
    
    public static Calendar julianEpoch = new GregorianCalendar(1970, GregorianCalendar.JANUARY, 1);
    
    private static SimpleDateFormat utcFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static SimpleDateFormat taiFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static SimpleDateFormat localFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ");    
    
    static {
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        taiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    enum IndexType {
        DATE(0),
        LEAP_SECS(1);
        
        public int index;
        
        IndexType(int index) {
            this.index = index;
        }
    }
    
    //Info taken from ftp://oceans.gsfc.nasa.gov/COMMON/leapsec.dat
    static long[][] dateAndLeapSecs = {
        { 0 , 10 },
        { new GregorianCalendar(1972, GregorianCalendar.JULY,    1).getTimeInMillis() , 11 },
        { new GregorianCalendar(1973, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 12 },
        { new GregorianCalendar(1974, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 13 },
        { new GregorianCalendar(1975, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 14 },
        { new GregorianCalendar(1976, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 15 },
        { new GregorianCalendar(1977, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 16 },
        { new GregorianCalendar(1978, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 17 },
        { new GregorianCalendar(1979, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 18 },
        { new GregorianCalendar(1980, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 19 },
        { new GregorianCalendar(1981, GregorianCalendar.JULY,    1).getTimeInMillis() , 20 },
        { new GregorianCalendar(1982, GregorianCalendar.JULY,    1).getTimeInMillis() , 21 },
        { new GregorianCalendar(1983, GregorianCalendar.JULY,    1).getTimeInMillis() , 22 },
        { new GregorianCalendar(1985, GregorianCalendar.JULY,    1).getTimeInMillis() , 23 },
        { new GregorianCalendar(1988, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 24 },
        { new GregorianCalendar(1990, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 25 },
        { new GregorianCalendar(1991, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 26 },
        { new GregorianCalendar(1992, GregorianCalendar.JULY,    1).getTimeInMillis() , 27 },
        { new GregorianCalendar(1993, GregorianCalendar.JULY,    1).getTimeInMillis() , 28 },
        { new GregorianCalendar(1994, GregorianCalendar.JULY,    1).getTimeInMillis() , 29 },
        { new GregorianCalendar(1996, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 30 },
        { new GregorianCalendar(1997, GregorianCalendar.JULY,    1).getTimeInMillis() , 31 },
        { new GregorianCalendar(1999, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 32 },
        { new GregorianCalendar(2006, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 33 },
        { new GregorianCalendar(2009, GregorianCalendar.JANUARY, 1).getTimeInMillis() , 34 },
    };
    
    private DateUtils() {}
    
    public static int getLeapSecsForDate(Calendar utcCal) throws CommonsException {
        long timeInMillis = utcCal.getTimeInMillis();
        for (int i = dateAndLeapSecs.length - 1; i >= 0; i--) {
            if (dateAndLeapSecs[i][IndexType.DATE.index] < timeInMillis) {
                return (int) dateAndLeapSecs[i][IndexType.LEAP_SECS.index];
            }
        }
        throw new CommonsException("No Leap Second found for given date!");
    }
    
    public static synchronized Calendar toTai(Calendar cal) throws CommonsException {
        Calendar taiCal = Calendar.getInstance(createTaiTimeZone(getLeapSecsForDate(cal)));
        taiCal.setTimeInMillis(cal.getTimeInMillis() + getLeapSecsForDate(cal) * 1000);
        return taiCal;
    }
    
    private static synchronized Calendar taiToUtc(Calendar taiCal) {
        Calendar calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calUtc.setTimeInMillis(taiCal.getTimeInMillis() - taiCal.getTimeZone().getRawOffset());
        return calUtc;
    }
    
    private static Calendar taiToLocal(Calendar taiCal)  {
        return toLocal(taiToUtc(taiCal));
    }

    public static synchronized Calendar toLocal(Calendar cal) {
        if (cal.getTimeZone().getID().equals("TAI")) {
            return taiToLocal(cal);
        } else {
            Calendar calLocal = Calendar.getInstance();
            calLocal.setTimeInMillis(cal.getTimeInMillis());
            return calLocal;
        }
    }

    public static synchronized Calendar toUtc(Calendar cal) {
        if (cal.getTimeZone().getID().equals("TAI")) {
            return taiToUtc(cal);
        } else {
            Calendar calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calUtc.setTimeInMillis(cal.getTimeInMillis());
            return calUtc;   
        }
    }
    
    public static Calendar getCurrentUtcTime() {
        return toUtc(getCurrentLocalTime());
    }
    
    public static Calendar getCurrentLocalTime() {
        return Calendar.getInstance();
    }
    
    public static Calendar getCurrentTaiTime() throws CommonsException {
        return toTai(getCurrentUtcTime());
    }
    
    public static String toCustomLocalFormat(Calendar cal, String format) {
        return new SimpleDateFormat(format).format(cal.getTime());
    }
    
    public static String toString(Calendar cal) {
        String timeZoneId = cal.getTimeZone().getID();
        if (timeZoneId.equals("UTC")) {
            return utcFormat.format(cal.getTime());
        }else if (timeZoneId.equals("TAI")) {
            return taiFormat.format(cal.getTime()) + "-0000" 
                + (cal.getTimeZone().getRawOffset() / 1000);
        }else {
            return localFormat.format(cal.getTime());
        }
    }
    
    public static synchronized Calendar toLocalCustomFormatCalendar(String calString, String format) throws ParseException {
        Calendar localCal = Calendar.getInstance();
        localCal.setTime(new SimpleDateFormat(format).parse(calString));
        return localCal;
    }
    
    public static synchronized Calendar toCalendar(String calString, FormatType formatType)
            throws ParseException {
        Calendar cal = Calendar.getInstance();
        switch (formatType) {
            case LOCAL_FORMAT:
                cal.setTimeInMillis(localFormat.parse(calString).getTime());
                break;
            case TAI_FORMAT:
                cal.setTimeZone(createTaiTimeZone(Integer.parseInt(calString
                    .substring(calString.length() - 2))));
                calString = calString.substring(0, calString.length() - 5);
                cal.setTimeInMillis(taiFormat.parse(calString).getTime());
                break;
            case UTC_FORMAT:
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(utcFormat.parse(calString).getTime());
                break;
            default:
                cal.setTimeInMillis(localFormat.parse(calString).getTime());
        }
        return cal;
    }
    
    public static double getTimeInSecs(Calendar cal, Calendar epoch) throws CommonsException {
        return getTimeInMillis(cal, epoch) / 1000.0;
    }
    
    public static String toString(double seconds) {
        return new DecimalFormat("#.000").format(seconds);
    }
    
    public static long getTimeInMillis(Calendar cal, Calendar epoch) throws CommonsException {
        long epochDiffInMilli;
        /**
         * Fixes date conversion issues preventing tests passing in the UK but working elsewhere in the world.
         */
        if(julianEpoch.getTimeZone().getID().equals("Europe/London")){
            epochDiffInMilli = epoch.getTimeInMillis() - (julianEpoch.getTimeInMillis()+julianEpoch.getTimeZone().getOffset(julianEpoch.getTimeInMillis())) ;
        }else {
            epochDiffInMilli = epoch.getTimeInMillis() - julianEpoch.getTimeInMillis() ;
        }
        if (cal.getTimeZone().getID().equals("TAI")) {
            epochDiffInMilli += getLeapSecsForDate(epoch) * 1000;
        }
        long milliseconds = cal.getTimeInMillis();
        return milliseconds - epochDiffInMilli;
    }
    
    private static TimeZone createTaiTimeZone(int leapSecs) {
        return new SimpleTimeZone(leapSecs * 1000, "TAI");
    }
    
    public static void main(String[] args) throws ParseException, CommonsException {
        Calendar curTime = getCurrentLocalTime();
        System.out.println("Test Time: " + toString(toCalendar(toString(toTai(toCalendar("2008-01-20T16:29:55.000Z", 
                FormatType.UTC_FORMAT))), FormatType.TAI_FORMAT)));
        System.out.println("Current Local Time: " + toString(curTime) + " " + curTime.getTimeInMillis());
        System.out.println("Current UTC Time: " + toString((curTime = toCalendar("2008-01-20T16:29:55.000Z", 
                FormatType.UTC_FORMAT))) + " " + toString(getTimeInSecs(curTime, tai93epoch)));
        System.out.println("Current TAI Time: " + toString((curTime = toTai(toCalendar("2008-01-20T16:29:55.000Z", 
                FormatType.UTC_FORMAT)))) + " " + toString(getTimeInSecs(curTime, tai93epoch)));
        System.out.println("Current UTC Time: " + toString(taiToUtc(curTime)));
    }
    
}
