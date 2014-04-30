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
package org.apache.oodt.cas.metadata.extractors;

// JUnit static imports
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

// JDK imports
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

// JAVAX imports
import javax.sql.DataSource;

// OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

// JUnit imports
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

// Mockito imports
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link DataSourceMetExtractor}.
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
@RunWith(JUnit4.class)
public class TestDataSourceMetExtractor {

  private static final String DB_COL_NAME_1 = "NAME";
  private static final String DB_COL_VALUE_1 = "SomeName";
  private static final String DB_COL_NAME_2 = "SIZE";
  private static final String DB_COL_VALUE_2 = "20";

  @Mock private DataSource dataSource;
  @Mock private Connection conn;
  @Mock private Statement statement;
  @Mock private ResultSet rs;
  @Mock private ResultSetMetaData rsMet;

  private DataSourceMetExtractor extractor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(dataSource.getConnection()).thenReturn(conn);
    when(conn.createStatement()).thenReturn(statement);
    when(statement.executeQuery(Mockito.<String>any())).thenReturn(rs);
    when(rs.getMetaData()).thenReturn(rsMet);
    when(rs.next()).thenReturn(true);
    when(rs.getString(1)).thenReturn(DB_COL_VALUE_1);
    when(rs.getString(2)).thenReturn(DB_COL_VALUE_2);
    when(rsMet.getColumnCount()).thenReturn(2);
    when(rsMet.getColumnName(1)).thenReturn(DB_COL_NAME_1);
    when(rsMet.getColumnName(2)).thenReturn(DB_COL_NAME_2);

    extractor = new DataSourceMetExtractor();
  }
 
  @Test
  public void testGetKey() {
    assertThat(extractor.getKey(new File("Test.csv")), is("Test"));
    assertThat(extractor.getKey(new File("123_sdfwegd_g334g.dat")), is("123_sdfwegd_g334g"));
    assertThat(extractor.getKey(new File("123qweJDKJF-3")), is("123qweJDKJF-3"));
  }

  @Test
  public void testGetMetadata() throws MetExtractionException {
    Metadata metadata = extractor.getMetadata(
        dataSource, "select * from SomeTable where key = '%s'", "SomeKey");

    assertThat(metadata.getAllKeys().size(), is(2));
    assertThat(metadata.getMetadata(DB_COL_NAME_1), is(DB_COL_VALUE_1));
    assertThat(metadata.getMetadata(DB_COL_NAME_2), is(DB_COL_VALUE_2));
  }
}
