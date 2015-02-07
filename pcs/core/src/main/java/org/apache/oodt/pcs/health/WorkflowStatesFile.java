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

package org.apache.oodt.pcs.health;

//OODT imports
import org.apache.oodt.pcs.input.PGEConfigurationFile;
import org.apache.oodt.pcs.input.PGEGroup;
import org.apache.oodt.pcs.input.PGEConfigFileReader;

//JDK imports
import java.io.FileInputStream;
import java.util.List;

/**
 * 
 * A file that manages the {@link Workflow} states that the
 * {@link PCSHealthMonitor} tool will look up when obtaining status in the PCS.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class WorkflowStatesFile implements WorkflowStatesMetKeys {

  private PGEConfigurationFile file;

  /**
   * Constructs a new WorkflowStatesFile with the specified parameters.
   * 
   * @param filePath
   *          The {@link File} path to the WorkflowStatesFile.
   * 
   * @throws InstantiationException
   *           If there is any error reading and parsing the WorkflowStates
   *           file.
   */
  public WorkflowStatesFile(String filePath) throws InstantiationException {
    try {
      this.file = new PGEConfigFileReader().read(new FileInputStream(filePath));
    } catch (Exception e) {
      throw new InstantiationException(e.getMessage());
    }
  }

  /**
   * 
   * @return The {@link List} of String Workflow States defined in the Workflow
   *         States file.
   */
  public List getStates() {
    return ((PGEGroup) this.file.getPgeSpecificGroups().get(
        WORKFLOW_STATES_GROUP)).getVector(WORKFLOW_STATES_VECTOR).getElements();
  }

}
