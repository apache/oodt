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

package org.apache.oodt.cas.workflow.tools;

//JDK imports
import java.io.File;
import java.util.List;

//APACHE imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the {@link InstanceRepoCleaner}.
 * 
 * @author mattmann
 * @version $Revision$
 * @since 
 * 
 */
public class TestInstanceRepoCleaner extends TestCase {

  private String instRepoPath;

  public void testClean() {
    InstanceRepoCleaner cleaner = new InstanceRepoCleaner();
    cleaner.setInstanceRepo(instRepoPath);
    try {
      cleaner.cleanRepository();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    WorkflowInstanceRepository repo = new LuceneWorkflowInstanceRepository(
        instRepoPath, 20);
    try {
      assertEquals(1, repo.getNumWorkflowInstances());
      for (WorkflowInstance inst : (List<WorkflowInstance>) repo
          .getWorkflowInstances()) {
        if (!inst.getStatus().equals(WorkflowStatus.FINISHED)) {
          fail("Workflow Instance: [" + inst.getId()
              + "] does was not marked as finished by the cleaner: status: ["
              + inst.getStatus() + "]");
        }
      }

    } catch (InstanceRepositoryException e) {
      fail(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // get a temp directory path
    File tempDir = File.createTempFile("bogus", "txt").getParentFile();
    FileUtils.copyDirectory(new File("./src/test/resources/testinstrepo"), new File(
        tempDir.getAbsolutePath() + "/" + "testinstrepo"));
    instRepoPath = tempDir.getAbsolutePath().endsWith("/") ? (tempDir
        .getAbsolutePath() + "testinstrepo")
        : (tempDir.getAbsolutePath() + "/" + "testinstrepo");

  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    FileUtils.deleteDirectory(new File(instRepoPath));

  }

}
