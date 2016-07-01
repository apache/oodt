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
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;

/**
 * 
 * Interface specifying a method to sort and prioritize
 * {@link WorkflowProcessor}s.
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface PrioritySorter {

  /**
   * Sorts the {@link List} of {@link WorkflowProcessor}s that are ready to run
   * in a particular order specified by the sub-class implementing this method.
   * 
   * @param candidates
   *          The {@link List} of {@link WorkflowProcessor}s to sort in priority
   *          order.
   */
  void sort(List<WorkflowProcessor> candidates);

}
