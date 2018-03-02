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

package org.apache.oodt.cas.resource.system;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.resource.structs.NameValueJobInput;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

//Junit imports
import junit.framework.TestCase;

/**
 *
 * Test harness for the {@link XmlRpcResourceManager}.
 *
 * @author mattmann
 * @author rajith
 * @version $Revision$
 *
 */
public class TestXmlRpcResourceManager extends TestCase {

  private File tmpPolicyDir;

  private XmlRpcResourceManager rm;

  private static final int RM_PORT = 50001;

  /**
   * @since OODT-182
   */
  public void testDynSetNodeCapacity() {
    XmlRpcResourceManagerClient rmc = null;
    try {
      rmc = new XmlRpcResourceManagerClient(new URL("http://localhost:"
          + RM_PORT));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertNotNull(rmc);
    try {
      rmc.setNodeCapacity("localhost", 8);
    } catch (MonitorException e) {
      fail(e.getMessage());
    }

    int setCapacity = -1;
    try {
      setCapacity = rmc.getNodeById("localhost").getCapacity();
    } catch (Exception e) {
      fail(e.getMessage());
    }
    assertEquals(8, setCapacity);
  }

  /*
   * (non-Javadoc)
   *
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    System.out.println(NameValueJobInput.class.getCanonicalName());
    generateTestConfiguration();
    this.rm = new XmlRpcResourceManager(RM_PORT);
    rm.startUp();
  }

  /*
   * (non-Javadoc)
   *
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    this.rm.shutdown();
    deleteAllFiles(this.tmpPolicyDir.getAbsolutePath());
  }

  private void deleteAllFiles(String startDir) {
    File startDirFile = new File(startDir);
    File[] delFiles = startDirFile.listFiles();

    if (delFiles != null && delFiles.length > 0) {
      for (File delFile : delFiles) {
        delFile.delete();
      }
    }

    startDirFile.delete();

  }

  private void generateTestConfiguration() throws IOException {
    Properties config = new Properties();

    String propertiesFile = "." + File.separator + "src" + File.separator +
            "test" + File.separator + "resources" + File.separator + "test.resource.properties";
    System.getProperties().load(new FileInputStream(new File(propertiesFile)));

    // stage policy
    File tmpPolicyDir = null;
    try {
      tmpPolicyDir = File.createTempFile("test", "ignore").getParentFile();
    } catch (Exception e) {
      fail(e.getMessage());
    }
    for (File policyFile : new File("./src/test/resources/policy")
            .listFiles(new FileFilter() {

              @Override
              public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".xml");
              }
            })) {
      try {
        FileUtils.copyFileToDirectory(policyFile, tmpPolicyDir);
      } catch (Exception e) {
        fail(e.getMessage());
      }
    }

    config.setProperty("org.apache.oodt.cas.resource.nodes.dirs", tmpPolicyDir
            .toURI().toString());
    config.setProperty("org.apache.oodt.cas.resource.nodetoqueues.dirs",
            tmpPolicyDir.toURI().toString());

    System.getProperties().putAll(config);
    this.tmpPolicyDir = tmpPolicyDir;
  }

}
