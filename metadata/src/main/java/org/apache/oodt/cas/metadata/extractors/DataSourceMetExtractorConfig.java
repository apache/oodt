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

// JDK imports
import java.util.Properties;

// OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;

/**
 * MetExtractorConfig which loads configuration from a Java Property file.  Expects given keys
 * to be set and provides easy read methods for those keys.
 *
 * @author bfoster@apache.org (Brian Foster)
 */
public class DataSourceMetExtractorConfig extends Properties implements MetExtractorConfig {

  private static final long serialVersionUID = -437948882353764454L;

  private static final String QUERY_KEY =
      "org.apache.oodt.cas.metadata.extractors.datasource.query";
  private static final String DATABASE_URL_KEY =
      "org.apache.oodt.cas.metadata.extractors.datasource.db.url";
  private static final String DRIVER_KEY =
      "org.apache.oodt.cas.metadata.extractors.datasource.driver";
  private static final String USER_NAME_KEY =
      "org.apache.oodt.cas.metadata.extractors.datasource.username";
  private static final String PASSWORD_KEY =
      "org.apache.oodt.cas.metadata.extractors.datasource.password";

  public String getQuery() {
    return getProperty(QUERY_KEY);
  }

  public String getDatabaseUrl() {
    return getProperty(DATABASE_URL_KEY);
  }

  public String getDriver() {
    return getProperty(DRIVER_KEY);
  }

  public String getUserName() {
    return getProperty(USER_NAME_KEY);
  }

  public String getPassword() {
    return getProperty(PASSWORD_KEY);
  }
}
