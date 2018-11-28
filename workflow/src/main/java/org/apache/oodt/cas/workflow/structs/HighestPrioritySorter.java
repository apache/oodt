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

package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowProcessor;

/**
 * 
 * Sorts strictly based on associated {@link WorkflowProcessor#getPriority()} in
 * reverse natural order.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class HighestPrioritySorter implements PrioritySorter {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.structs.PrioritySorter#sort(java.util.List)
   */
  @Override
  public void sort(List<WorkflowProcessor> candidates) {
    Collections.sort(candidates, new Comparator<WorkflowProcessor>() {

      @Override
      public int compare(WorkflowProcessor o1, WorkflowProcessor o2) {
        return o2.getWorkflowInstance().getPriority()
            .compareTo(o1.getWorkflowInstance().getPriority());
      }
    });
  }

}
