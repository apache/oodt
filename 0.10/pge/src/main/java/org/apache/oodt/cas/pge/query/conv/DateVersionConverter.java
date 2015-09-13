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


package org.apache.oodt.cas.pge.query.conv;

//OODT imports
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.cas.filemgr.structs.query.conv.VersionConverter;

//JDK imports
import java.text.ParseException;
import java.util.Calendar;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 */
public class DateVersionConverter implements VersionConverter {

    public double convertToPriority(String version) throws ParseException {
        Calendar cal = DateUtils.toCalendar(version,
                DateUtils.FormatType.UTC_FORMAT);
        return (cal.get(Calendar.YEAR) * 10000)
                + ((cal.get(Calendar.MONTH) + 1) * 100)
                + (cal.get(Calendar.DAY_OF_MONTH));
    }

}
