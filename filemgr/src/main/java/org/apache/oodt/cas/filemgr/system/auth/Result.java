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

package org.apache.oodt.cas.filemgr.system.auth;

/**
 * A Python-friendly wrapper that wraps an XML-RPC result.
 * 
 * @author kelly
 */
public class Result {

    /**
     * Default Constructor.
     * 
     * @param kind
     *            The class name of the return result.
     * @param value
     *            The result value.
     * @throws Throwable
     *             If any error occurs.
     */
    public Result(Class kind, Object value) throws Throwable {
        if (kind != null) {
            java.lang.reflect.Constructor ctor = kind.getConstructor(ARGS);
            this.value = ctor.newInstance(new Object[] { value });
        } else {
            this.value = value;
        }
    }

    /**
     * Gets the result value.
     * 
     * @return The result value.
     */
    public Object getValue() {
        return value;
    }

    private Object value;

    private static Class[] ARGS = { String.class };
}
