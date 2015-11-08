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

package org.apache.oodt.cas.workflow.engine;

//JDK imports

import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessorQueue;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * A mock {@link WorkflowProcessorQueue} object for use in testing.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MockProcessorQueue extends WorkflowProcessorQueue {

  private static Logger LOG = Logger.getLogger(MockProcessorQueue.class.getName());

  private QuerierAndRunnerUtils utils;

  private boolean consumed;

  public MockProcessorQueue() {
    super(null,null,null);
    this.utils = new QuerierAndRunnerUtils();
    this.consumed = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowProcessorQueue#getProcessors ()
   */
  @Override
  public synchronized List<WorkflowProcessor> getProcessors() {
    List<WorkflowProcessor> processors = new Vector<WorkflowProcessor>();
    try {
      if (!consumed) {
        processors.add(utils.getProcessor(10.0, "Success", "done"));
        processors.add(utils.getProcessor(2.0, "Loaded", "initial"));
        processors.add(utils.getProcessor(7.0, "Loaded", "initial"));
        this.consumed = true;
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RuntimeException(e);
    }

    return processors;
  }

}