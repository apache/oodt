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


package org.apache.oodt.cas.curation.util;

//JDK imports
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * Sample ISO 8601 date utility methods taken from:
 * 
 * <a href="http://www.dynamicobjects.com/d2r/archives/003057.html">http://www.
 * dynamicobjects.com/d2r/archives/003057.html</a>
 * 
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class DateUtils {

  public static String getDateAsRFC822String(Date date) {
    SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
        "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
    return RFC822DATEFORMAT.format(date);
  }

  public static String getDateAsISO8601String(Date date) {
    SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssZ");
    String result = ISO8601FORMAT.format(date);
    // convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
    // - note the added colon for the Timezone
    result = result.substring(0, result.length() - 2) + ":"
        + result.substring(result.length() - 2);
    return result;
  }

}
