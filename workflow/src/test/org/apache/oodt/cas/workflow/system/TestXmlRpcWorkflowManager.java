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

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Vector;

//Junit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

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

  public void testGetWorkflowInstances() {
    Vector workflowInsts = null;
    try {
      workflowInsts = wmgr.getWorkflowInstances();
    } catch (Exception e) {
      e.printStackTrace();
    }

    assertNotNull(workflowInsts);
    assertEquals(1, workflowInsts.size());
  }

  protected void setUp() throws Exception {
    startXmlRpcWorkflowManager();
    startWorkflow();
  }

  protected void tearDown() throws Exception {
    // FIXME: wmgr.shutdown(); // Define a method and shutdown the webserver.
    // This is
    // FIXME: not implemented in XmlRpcWorkflowManager.

    // blow away lucene cat
    deleteAllFiles(luceneCatLoc);

  }

  private void deleteAllFiles(String startDir) {
    File startDirFile = new File(startDir);
    File[] delFiles = startDirFile.listFiles();

    if (delFiles != null && delFiles.length > 0) {
      for (int i = 0; i < delFiles.length; i++) {
        delFiles[i].delete();
      }
    }

    startDirFile.delete();

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
      e.printStackTrace();
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
      luceneCatLoc = File.createTempFile("blah", "txt").getParentFile().getCanonicalPath();
      luceneCatLoc = !luceneCatLoc.endsWith("/") ? luceneCatLoc+"/":luceneCatLoc;
      luceneCatLoc += "repo";
    } catch (Exception e) {
      fail(e.getMessage());
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
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      wmgr = new XmlRpcWorkflowManager(WM_PORT);
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

}
