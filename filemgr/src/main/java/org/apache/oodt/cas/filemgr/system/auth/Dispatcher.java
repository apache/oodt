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

//JDK imports
import java.util.List;

/**
 * A guard to ensure that any XML-RPC method called for the filemgr server is
 * properly authenticated with a username and password.
 * 
 * 
 * @author kelly
 */
public interface Dispatcher {

    /**
     * Handles an XML-RPC request, provided that the given <code>user</code>name
     * and <code>password</code> are correct.
     * 
     * @param methodSpecifier
     *            The name of the XML-RPC method to call.
     * @param params
     *            The parameters to the method.
     * @param user
     *            The user name who is calling the method.
     * @param password
     *            The user's password.
     * @return The result of the method call.
     * @throws Exception
     *             If any error occurs.
     */
    Result handleRequest(String methodSpecifier, List params,
                         String user, String password);
}
