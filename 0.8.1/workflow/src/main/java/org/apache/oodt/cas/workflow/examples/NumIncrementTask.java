/*
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


package org.apache.oodt.cas.workflow.examples;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;

/**
 * @author mattmann
 * @version $Revision$
 * @since OODT-53
 * 
 * <p>
 * This task illustrates OODT-53 by taking a <code>num</code> {@link Metadata}
 * parameter and then incrementing it. Subsequent executions of this same
 * {@link WorkflowTaskInstance} within a {@link Workflow} should yield
 * incremented versions of the initially provided <code>num</code> parameter.
 * </p>.
 */
public class NumIncrementTask implements WorkflowTaskInstance {

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(org.apache.oodt.cas.metadata.Metadata,
   *      org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
   */
  public void run(Metadata metadata, WorkflowTaskConfiguration config) {
    // read the num from the metadata, and then increment it
    // and update the metadata
    if (metadata.getMetadata("num") == null
        || (metadata.getMetadata("num") != null && metadata.getMetadata("num")
            .equals(""))) {
      return;
    }

    int num = Integer.parseInt(metadata.getMetadata("num"));
    System.out.println("Num pre increment: ["+num+"]");
    num++;
    System.out.println("Num post increment: ["+num+"]");
    metadata.replaceMetadata("num", String.valueOf(num));
  }

}
