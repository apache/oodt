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

package org.apache.oodt.cas.resource.monitor.ganglia.loadcalc;

import java.util.HashMap;

/**
 * @author rajith
 * @version $Revision$
 */
public interface LoadCalculator {

    public static int MAXIMUM_FRACTION_DIGITS = 3;

    /*Ganglia metric keys*/
    public static String LOAD_ONE = "load_one";
    public static String LOAD_FIVE = "load_five";
    public static String LOAD_FIFTEEN = "load_fifteen";
    public static String CPU_NUM = "cpu_num";

    /**
     * Calculate the load and normalize it within the given node's capacity
     * @param nodeCapacity node's {@link org.apache.oodt.cas.resource.structs.ResourceNode}
     *                     capacity
     * @param metrics status metrics of the resource node
     * @return An integer representation of the load within 0 and node's capacity
     */
    public float calculateLoad(float nodeCapacity, HashMap<String, String> metrics);

}
