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


package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Workflow is an abstract representation of a set of interconnected
 * processes. Processes, or jobs, may have dependencies upon one another, may
 * provide each other input, and/or output, or may be completely independent of
 * one another.
 * 
 * <br>
 * See <a href="http://www.gridbus.org/reports/GridWorkflowTaxonomy.pdf">Buyya
 * et al.</a> for a great description in detail of what exactly a Workflow is.
 * </p>
 * 
 */
public class Workflow {

    private String name;

    private String id;

    private List tasks;

    /**
     * Default Constructor
     * 
     */
    public Workflow() {
        tasks = new Vector();

    }

    /**
     * Constructs a new Workflow with the given parameters.
     * 
     * @param name
     *            The name of this workflow.
     * @param id
     *            The identifier for this workflow.
     * @param tasks
     *            The {@link List} of {@link WorkflowTask}s associated with
     *            this workflow.
     */
    public Workflow(String name, String id, List tasks) {
        name = name;
        id = id;
        tasks = tasks;

    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the tasks
     */
    public List getTasks() {
        return tasks;
    }

    /**
     * @param tasks
     *            the tasks to set
     */
    public void setTasks(List tasks) {
        this.tasks = tasks;
    }

}
