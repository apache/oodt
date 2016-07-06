// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
	The <code>DateConvert</code> class is intended to provide date/time
	conversion and parse routines. For a description of the syntax of the
	format strings see {@link SimpleDateFormat}.

	@author S. Hardman
	@version $Revision: 1.1.1.1 $
 */
public class DateConvert {

  /**
		The number of milliseconds in a minute.
	*/
	private final static long MS_IN_MINUTE = 60000;

	/**
		The number of milliseconds in an hour.
	*/
	private final static long MS_IN_HOUR = 3600000;

	/**
		The number of milliseconds in a day.
	*/
	private final static long MS_IN_DAY = 86400000;

	/**
		The format string representing the ISO 8601 format. The format
		is close to CCSDS ASCII Time Code A. 
	*/
	private final static String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	/**
		The format string representing the CCSDS ASCII Time Code B format
		excluding the trailing "Z".
	*/
	private final static String DOY_FORMAT = "yyyy-DDD'T'HH:mm:ss.SSS";

	/**
		The format string representing the EDA time stamp format.
	*/
	private final static String TS_FORMAT = "yyyyMMddHHmmssSSS";

	/**
		The format string representing the DBMS format.
	*/
	private final static String DBMS_FORMAT = "dd-MMM-yyyy HH:mm:ss";

	/**
		The format string representing the Year-Month-Day format.
	*/
	private final static String YMD_FORMAT = "yyyy-MM-dd";
  public static final int INT = 24;
  public static final int BEGIN_INDEX = 23;
  public static final int ERROR_OFFSET = 24;
  public static final int ERROR_OFFSET1 = 25;


  /**
		Constructor given no arguments.

		This is a static-only class that may not be instantiated.

		@throws IllegalStateException If the class is instantiated.
	*/
	public DateConvert() throws IllegalStateException {
		throw new IllegalStateException("Instantiation of this class is not allowed.");
	}


	/**
		Format the given date and return the resulting string in ISO 8601 format.

		The format is as follows: "yyyy-MM-dd'T'HH:mm:ss.SSS[Z|[+|-]HH:mm]".

		@param inputDate The date to be converted into string format.
		@return The formatted date/time string.
	*/
	public static String isoFormat(Date inputDate) {

		// Setup the date format and convert the given date.
		SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_FORMAT);
		String dateString = dateFormat.format(inputDate);

		// Determine the time zone and concatenate the time zone designator
		// onto the formatted date/time string.
		TimeZone tz = dateFormat.getTimeZone();
		String tzName = tz.getDisplayName();
		if (tzName.equals("Greenwich Mean Time") && !TimeZone.getDefault().inDaylightTime( inputDate )) {
			dateString = dateString.concat("Z");
		}
		else {
			// Determine the hour offset. Add an hour if daylight savings
			// is in effect.
			long tzOffsetMS = tz.getRawOffset();
			long tzOffsetHH = tzOffsetMS / MS_IN_HOUR;
			if (tz.inDaylightTime(inputDate)) {
				tzOffsetHH = tzOffsetHH + 1;
			}
			String hourString = String.valueOf(Math.abs(tzOffsetHH));
			if (hourString.length() == 1) {
				hourString = "0" + hourString;
			}

			// Determine the minute offset.
			long tzOffsetMMMS = tzOffsetMS % MS_IN_HOUR;
			long tzOffsetMM = 0;
			if (tzOffsetMMMS != 0) {
				tzOffsetMM = tzOffsetMMMS / MS_IN_MINUTE;
			}
			String minuteString = String.valueOf(tzOffsetMM);
			if (minuteString.length() == 1) {
				minuteString = "0" + minuteString;
			}

			// Determine the sign of the offset.
			String sign = "+";
			if (String.valueOf(tzOffsetMS).contains("-")) {
				sign = "-";
			}

			dateString = dateString.concat(sign + hourString + ":" + minuteString);
		}

		return(dateString);
	}


	/**
		Parse the given date/time string in ISO 8601 format and return the
		resulting <code>Date</code> object.

		The format is as follows: "yyyy-MM-dd'T'HH:mm:ss.SSS[Z|[+|-]HH:mm]".

		@param inputString The string to be parsed.
		@return The resulting Date object.
		@throws ParseException If the string is null or does not match the date/time
		format.
	*/
	public static Date isoParse(String inputString) throws ParseException {

		// Setup the date format.
		SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_FORMAT);
		dateFormat.setLenient(false);

		// The length of the input string should be at least 24 characters.
		if (inputString == null || inputString.length() < INT) {
			throw new ParseException("An exception occurred because the input date/time string was null or under 24 characters in length.", inputString.length());
		}

		// Evaluate the the specified offset and set the time zone.
		String offsetString = inputString.substring(BEGIN_INDEX);
		if (offsetString.equals("Z")) {
			dateFormat.setTimeZone(TimeZone.getTimeZone("Greenwich Mean Time"));
		}
		else if (offsetString.startsWith("-") || offsetString.startsWith("+")) {
			SimpleDateFormat offsetFormat = new SimpleDateFormat();
			if (offsetString.length() == 3) {
				offsetFormat.applyPattern("HH");
			}
			else if (offsetString.length() == 6) {
				offsetFormat.applyPattern("HH:mm");
			}
			else {
				throw new ParseException("An exception occurred because the offset portion was not the valid length of 3 or 6 characters.",
					ERROR_OFFSET1);
			}

			// Validate the given offset.
			offsetFormat.setLenient(false);

			// Set the time zone with the validated offset.
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT" + offsetString));
		}
		else {
			throw new ParseException("An exception occurred because the offset portion of the input date/time string was not 'Z' or did not start with '+' or '-'.",
				ERROR_OFFSET);
		}

		// Parse the given string.

	  return(dateFormat.parse(inputString));
	}


	/**
		Format the given date and return the resulting string in CCSDS
		ASCII Time Code B format.

		The format is as follows: "yyyy-DDD'T'HH:mm:ss.SSS".

		@param inputDate The date to be converted into string format.
		@return The formatted date/time string.
	*/
	public static String doyFormat(Date inputDate) {

		// Setup the date format and convert the given date.
		SimpleDateFormat dateFormat = new SimpleDateFormat(DOY_FORMAT);

	  return(dateFormat.format(inputDate));
	}


	/**
		Parse the given date/time string in CCSDS ASCII Time Code B format
		and return the resulting <code>Date</code> object.

		The format is as follows: "yyyy-DDD'T'HH:mm:ss.SSS".

		@param inputString The string to be parsed.
		@return The resulting Date object.
		@throws ParseException If the string does not match the date/time
		format.
	*/
	public static Date doyParse(String inputString) throws ParseException {

		// Setup the date format and parse the given string.
		SimpleDateFormat dateFormat = new SimpleDateFormat(DOY_FORMAT);
		dateFormat.setLenient(false);

	  return(dateFormat.parse(inputString));
	}


	/**
		Format the given date and return the resulting string in a timestamp
		format.

		The format is as follows: "yyyyMMddHHmmssSSS".

		@param inputDate The date to be converted into string format.
		@return The formatted date/time string.
	*/
	public static String tsFormat(Date inputDate) {

		// Setup the date format and convert the given date.
		SimpleDateFormat dateFormat = new SimpleDateFormat(TS_FORMAT);

	  return(dateFormat.format(inputDate));
	}


	/**
		Parse the given date/time string in timestamp format
		and return the resulting <code>Date</code> object.

		The format is as follows: "yyyyMMddHHmmssSSS".

		@param inputString The string to be parsed.
		@return The resulting Date object.
		@throws ParseException If the string does not match the date/time
		format.
	*/
	public static Date tsParse(String inputString) throws ParseException {

		// Setup the date format and parse the given string.
		SimpleDateFormat dateFormat = new SimpleDateFormat(TS_FORMAT);
		dateFormat.setLenient(false);

	  return(dateFormat.parse(inputString));
	}


	/**
		Format the given date and return the resulting string in a DBMS
		format.

		The format is as follows: "dd-MMM-yyyy HH:mm:ss".

		@param inputDate The date to be converted into string format.
		@return The formatted date/time string.
	*/
	public static String dbmsFormat(Date inputDate) {

		// Setup the date format and convert the given date.
		SimpleDateFormat dateFormat = new SimpleDateFormat(DBMS_FORMAT);

	  return(dateFormat.format(inputDate));
	}


	/**
		Parse the given date/time string in DBMS format
		and return the resulting <code>Date</code> object.

		The format is as follows: "dd-MMM-yyyy HH:mm:ss".

		@param inputString The string to be parsed.
		@return The resulting Date object.
		@throws ParseException If the string does not match the date/time
		format.
	*/
	public static Date dbmsParse(String inputString) throws ParseException {

		// Setup the date format and parse the given string.
		SimpleDateFormat dateFormat = new SimpleDateFormat(DBMS_FORMAT);
		dateFormat.setLenient(false);

	  return(dateFormat.parse(inputString));
	}


	/**
		Format the given date and return the resulting string in a
		year-month-day format.

		The format is as follows: "yyyy-MM-dd".

		@param inputDate The date to be converted into string format.
		@return The formatted date/time string.
	*/
	public static String ymdFormat(Date inputDate) {

		// Setup the date format and convert the given date.
		SimpleDateFormat dateFormat = new SimpleDateFormat(YMD_FORMAT);

	  return(dateFormat.format(inputDate));
	}


	/**
		Parse the given date/time string in year-month-day format
		and return the resulting <code>Date</code> object.

		The format is as follows: "yyyy-MM-dd".

		@param inputString The string to be parsed.
		@return The resulting Date object.
		@throws ParseException If the string does not match the date/time
		format.
	*/
	public static Date ymdParse(String inputString) throws ParseException {

		// Setup the date format and parse the given string.
		SimpleDateFormat dateFormat = new SimpleDateFormat(YMD_FORMAT);
		dateFormat.setLenient(false);

	  return(dateFormat.parse(inputString));
	}


	/**
		Get the number of milliseconds in a minute.

		@return The number of milliseconds in a minute.
	*/
	public static long getMsecsInMinute() {
		return(MS_IN_MINUTE);
	}


	/**
		Get the number of milliseconds in an hour.

		@return The number of milliseconds in an hour.
	*/
	public static long getMsecsInHour() {
		return(MS_IN_HOUR);
	}


	/**
		Get the number of milliseconds in a day.

		@return The number of milliseconds in a day.
	*/
	public static long getMsecsInDay() {
		return(MS_IN_DAY);
	}
}

