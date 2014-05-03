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

// JUnit static imports
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

// JDK imports
import java.util.Properties;

// OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

// JUnit imports
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

// Mockito imports
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link DateTimeExpandMetExtractor}.
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
@RunWith(JUnit4.class)
public class TestDateTimeExpandMetExtractor {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Mock private Properties configuration;

  private Metadata metadata;
  private DateTimeExpandMetExtractor extractor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    when(configuration.getProperty("FullDateTimeKey")).thenReturn("StartDateTime");
    when(configuration.getProperty("FullDateTimeFormat")).thenReturn("yyyy-MM-dd HH:mm:ss");
    when(configuration.getProperty("YearKey")).thenReturn("StartYear");
    when(configuration.getProperty("MonthKey")).thenReturn("StartMonth");
    when(configuration.getProperty("DayKey")).thenReturn("StartDay");
    when(configuration.getProperty("HourKey")).thenReturn("StartHour");
    when(configuration.getProperty("MinuteKey")).thenReturn("StartMinute");
    when(configuration.getProperty("SecondKey")).thenReturn("StartSecond");

    metadata = new Metadata();
    metadata.addMetadata("StartDateTime", "2013-05-23 03:02:01");

    extractor = new DateTimeExpandMetExtractor();
  }

  @Test
  public void testExpandAll() throws MetExtractionException {
    when(configuration.containsKey("FullDateTimeKey")).thenReturn(true);
    when(configuration.containsKey("FullDateTimeFormat")).thenReturn(true);
    when(configuration.containsKey("YearKey")).thenReturn(true);
    when(configuration.containsKey("MonthKey")).thenReturn(true);
    when(configuration.containsKey("DayKey")).thenReturn(true);
    when(configuration.containsKey("HourKey")).thenReturn(true);
    when(configuration.containsKey("MinuteKey")).thenReturn(true);
    when(configuration.containsKey("SecondKey")).thenReturn(true);

    extractor.configure(configuration);
    Metadata extractedMetadata = extractor.doExtract(null, metadata);

    assertThat(extractedMetadata.getMetadata("StartYear"), is("2013"));
    assertThat(extractedMetadata.getMetadata("StartMonth"), is("05"));
    assertThat(extractedMetadata.getMetadata("StartDay"), is("23"));
    assertThat(extractedMetadata.getMetadata("StartHour"), is("03"));
    assertThat(extractedMetadata.getMetadata("StartMinute"), is("02"));
    assertThat(extractedMetadata.getMetadata("StartSecond"), is("01"));
  }

  @Test
  public void testExpandSome() throws MetExtractionException {
    when(configuration.containsKey("FullDateTimeKey")).thenReturn(true);
    when(configuration.containsKey("FullDateTimeFormat")).thenReturn(true);
    when(configuration.containsKey("YearKey")).thenReturn(true);
    when(configuration.containsKey("MonthKey")).thenReturn(true);
    when(configuration.containsKey("DayKey")).thenReturn(true);

    extractor.configure(configuration);
    Metadata extractedMetadata = extractor.doExtract(null, metadata);

    assertThat(extractedMetadata.getMetadata("StartYear"), is("2013"));
    assertThat(extractedMetadata.getMetadata("StartMonth"), is("05"));
    assertThat(extractedMetadata.getMetadata("StartDay"), is("23"));
    assertThat(extractedMetadata.containsKey("StartHour"), is(false));
    assertThat(extractedMetadata.containsKey("StartMinute"), is(false));
    assertThat(extractedMetadata.containsKey("StartSecond"), is(false));
  }

  @Test
  public void testFailure() throws MetExtractionException {
    when(configuration.containsKey("FullDateTimeKey")).thenReturn(false);

    extractor.configure(configuration);
    expectedException.expect(MetExtractionException.class);
    extractor.doExtract(null, metadata);
  }
}
