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

package org.apache.oodt.cas.pge.writers;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

public class VelocityConfigFileWriterTest extends TestCase {

  private static final Logger LOG = Logger
      .getLogger(VelocityConfigFileWriterTest.class.getName());

  public void testCreateConfigFile() throws IOException {
    URL url = this.getClass().getResource("/test-config.vm");
    VelocityConfigFileWriter vcfw = new VelocityConfigFileWriter();
    VelocityMetadata metadata = new VelocityMetadata(new Metadata());
    metadata.addMetadata("name", "Chris");
    metadata.addMetadata("name", "Paul");
    metadata.addMetadata("conference", "ApacheCon");
    File config = File.createTempFile("config", ".out");
    try {
      vcfw.generateFile(config.toString(), metadata, LOG, url.getFile());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }
    String output = FileUtils.readFileToString(config);
    assertEquals(System.getenv().get("USER") + " Welcomes to ApacheCon Chris Paul!", output);
    config.delete();
  }

}
