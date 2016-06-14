/**
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

package org.apache.oodt.cas.workflow.system;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;


/**
 * 
 * 
 * Test harness for the {@link XmlRpcWorkflowManager}.
 * 
 * @author sherylj
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestXmlRpcWorkflowManager extends TestCase {

  private static final int WM_PORT = 50002;

  private XmlRpcWorkflowManager wmgr;

  private String luceneCatLoc;

  private static final Logger LOG = Logger
      .getLogger(TestXmlRpcWorkflowManager.class.getName());

  public void testGetWorkflowInstances() {

    List workflowInsts = null;

    int numInsts = -1;
    while (numInsts != 2) {
      try {
        workflowInsts = wmgr.getWorkflowInstances();
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
      }

      assertNotNull(workflowInsts);
      numInsts = workflowInsts.size();
    }

    assertEquals(2, workflowInsts.size());
  }

  protected void setUp() throws Exception {
    startXmlRpcWorkflowManager();
    startWorkflow();
  }

  protected void tearDown() throws Exception {

  }

  private void startWorkflow() {
    XmlRpcWorkflowManagerClient client = null;
    try {
      client = new XmlRpcWorkflowManagerClient(new URL("http://localhost:"
          + WM_PORT));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      client.sendEvent("long", new Metadata());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RuntimeException(e);
    }

  }

  private void startXmlRpcWorkflowManager() {
    System.setProperty("java.util.logging.config.file", new File(
        "./src/main/resources/logging.properties").getAbsolutePath());

    try {
      System.getProperties().load(
          new FileInputStream("./src/main/resources/workflow.properties"));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      luceneCatLoc = File.createTempFile("blah", "txt").getParentFile()
          .getCanonicalPath();
      luceneCatLoc = !luceneCatLoc.endsWith("/") ? luceneCatLoc + "/"
          : luceneCatLoc;
      luceneCatLoc += "repo";
      LOG.log(Level.INFO, "Lucene instance repository: [" + luceneCatLoc + "]");
    } catch (Exception e) {
      fail(e.getMessage());
    }

    if (new File(luceneCatLoc).exists()) {
      // blow away lucene cat
      LOG.log(Level.INFO, "Removing workflow instance repository: ["
          + luceneCatLoc + "]");
      try {
        FileUtils.deleteDirectory(new File(luceneCatLoc));
      } catch (IOException e) {
        fail(e.getMessage());
      }
    }

    System
        .setProperty("workflow.engine.instanceRep.factory",
            "org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepositoryFactory");
    System
        .setProperty("org.apache.oodt.cas.workflow.instanceRep.lucene.idxPath",
            luceneCatLoc);

    try {
      System.setProperty("org.apache.oodt.cas.workflow.repo.dirs", "file://"
          + new File("./src/main/resources/examples").getCanonicalPath());
      System.setProperty("org.apache.oodt.cas.workflow.lifecycle.filePath",
          new File("./src/main/resources/examples/workflow-lifecycle.xml")
              .getCanonicalPath());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      wmgr = new XmlRpcWorkflowManager(WM_PORT);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }

  }

}
