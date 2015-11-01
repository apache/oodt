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

package org.apache.oodt.cas.workflow.gui.model.repo;

//JDK imports
import java.io.File;

/**
 * 
 * Factory for creating xml model repositories
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class XmlWorkflowModelRepositoryFactory {

  private String workspace;

  public XmlWorkflowModelRepository createModelRepository() {
    if (workspace == null) {
      return null;
    }
    if (!new File(workspace).exists()) {
      new File(workspace).mkdirs();
    }
    return new XmlWorkflowModelRepository(new File(workspace));
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

}
