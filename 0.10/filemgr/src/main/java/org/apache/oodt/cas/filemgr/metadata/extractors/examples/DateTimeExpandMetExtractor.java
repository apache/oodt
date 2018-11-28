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
package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

// OODT imports
import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.util.PathUtils;

// Google imports
import com.google.common.base.Strings;

/**
 * Expands a given metadata date time field out into multiple metadata fields (i.e. year, month,
 * day, hour, minute, and second).
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
public class DateTimeExpandMetExtractor extends AbstractFilemgrMetExtractor {

  private static final String FULL_DATE_TIME_KEY = "FullDateTimeKey";
  private static final String FULL_DATE_TIME_FORMAT = "FullDateTimeFormat";

  private static final String YEAR_KEY = "YearKey";
  private static final String MONTH_KEY = "MonthKey";
  private static final String DAY_KEY = "DayKey";
  private static final String HOUR_KEY = "HourKey";
  private static final String MINUTE_KEY = "MinuteKey";
  private static final String SECOND_KEY = "SecondKey";

  private String fullDateTimeKey;
  private String fullDateTimeFormat;
  private String yearKey;
  private String monthKey;
  private String dayKey;
  private String hourKey;
  private String minuteKey;
  private String secondKey;

  @Override
  public void doConfigure() {
    fullDateTimeKey = getKey(FULL_DATE_TIME_KEY);
    fullDateTimeFormat = getKey(FULL_DATE_TIME_FORMAT);
    yearKey = getKey(YEAR_KEY);
    monthKey = getKey(MONTH_KEY);
    dayKey = getKey(DAY_KEY);
    hourKey = getKey(HOUR_KEY);
    minuteKey = getKey(MINUTE_KEY);
    secondKey = getKey(SECOND_KEY);
  }

  @Override
  public Metadata doExtract(Product product, Metadata metadata) throws MetExtractionException {
    String fullDateTime = getFullDateTime(metadata);

    createDateField(metadata, yearKey, fullDateTime, fullDateTimeFormat, "yyyy");
    createDateField(metadata, monthKey, fullDateTime, fullDateTimeFormat, "MM");
    createDateField(metadata, dayKey, fullDateTime, fullDateTimeFormat, "dd");
    createDateField(metadata, hourKey, fullDateTime, fullDateTimeFormat, "HH");
    createDateField(metadata, minuteKey, fullDateTime, fullDateTimeFormat, "mm");
    createDateField(metadata, secondKey, fullDateTime, fullDateTimeFormat, "ss");
    return metadata;
  }

  private String getKey(String key) {
    if (configuration.containsKey(key)) {
      return configuration.getProperty(key);
    } else {
      return null;
    }
  }

  private String getFullDateTime(Metadata metadata)
       throws MetExtractionException {
    if (!Strings.isNullOrEmpty(fullDateTimeKey)) {
      return metadata.getMetadata(fullDateTimeKey);
    } else {
      throw new MetExtractionException("Failed to find DateTimeKey " + fullDateTimeKey);
    }
  }

  private void createDateField(Metadata metadata, String fieldKey, String fullDateTime,
      String fullDateTimeFormat, String fieldFormat) throws MetExtractionException {
    if (!Strings.isNullOrEmpty(fieldKey)) {
      try {
        metadata.addMetadata(fieldKey, PathUtils.doDynamicReplacement(String.format(
            "[FORMAT(%s,%s,%s)]", fullDateTimeFormat, fullDateTime, fieldFormat)));
      } catch (Exception e) {
        throw new MetExtractionException(String.format(
            "Failed to create field for key %s from fullDateTime %s and fullDateTimeFormat %s",
            fieldKey, fullDateTime, fullDateTimeFormat), e);
      }
    }
  }
}
