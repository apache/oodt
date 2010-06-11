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


package gov.nasa.jpl.oodt.cas.resource.util;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Metadata keys to describe the output
 * of the ulimit call.
 * </p>.
 */
public interface UlimitMetKeys {

    public static final String CORE_FILE_SIZE = "core file size";

    public static final String DATA_SEGMENT_SIZE = "data seg size";
    
    public static final String FILE_SIZE = "file size";

    public static final String MAX_LOCKED_MEMORY = "max locked memory";

    public static final String MAX_MEMORY_SIZE = "max memory size";

    public static final String MAX_OPEN_FILES = "open files";

    public static final String MAX_PIPE_SIZE = "pipe size";

    public static final String MAX_STACK_SIZE = "stack size";

    public static final String MAX_CPU_TIME = "cpu time";

    public static final String MAX_USER_PROCESSES = "max user processes";

    public static final String MAX_VIRTUAL_MEMORY = "virtual memory";

}
