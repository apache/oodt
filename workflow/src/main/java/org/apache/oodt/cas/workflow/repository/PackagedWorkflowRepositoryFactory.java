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

package org.apache.oodt.cas.workflow.repository;

//JDK imports
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Constructs {@link PackagedWorkflowRepository}s.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PackagedWorkflowRepositoryFactory implements
    WorkflowRepositoryFactory {

  private String wDirPath;

  private static final Logger LOG = Logger
      .getLogger(PackagedWorkflowRepositoryFactory.class.getName());

  public PackagedWorkflowRepositoryFactory() throws InstantiationException {
    this.wDirPath = System
        .getProperty("org.apache.oodt.cas.workflow.wengine.packagedRepo.dir.path");
    if (this.wDirPath == null || (!new File(wDirPath).isDirectory())) {
      throw new InstantiationException("Must specify valid directory path "
          + "containing wengine-style workflow xml files! path specified: ["
          + this.wDirPath + "]");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepositoryFactory#
   * createRepository()
   */
  @Override
  public WorkflowRepository createRepository() {
    try {
      if(this.wDirPath!=null) {
        return new PackagedWorkflowRepository(
            Arrays.asList(new File(this.wDirPath).listFiles()));
      }
      else {
        LOG.log(
            Level.SEVERE,
            "Unable to create packaged workflow repository! Reason: empty wDirPath");
        return null;
      }
    } catch (Exception e) {
      LOG.log(
          Level.SEVERE,
          "Unable to create packaged workflow repository! Reason: "
              + e.getMessage());
      return null;
    }
  }

}
