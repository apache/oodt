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
import org.apache.xmlrpc.AuthenticatedXmlRpcHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//XML-RPC imports

/**
 * An XML-RPC Web Server that requires authentication and authorization.
 * 
 * @author kelly
 */
public final class SecureWebServer extends org.apache.xmlrpc.WebServer
        implements AuthenticatedXmlRpcHandler {

    /**
     * Initializes the XML-RPC web server on the given <code>port</code> and
     * adds a default handler that traps all calls and makes them authenticated.
     * 
     * @param port
     *            The port to run the XML-RPC webserver on.
     * @throws IOException
     *             If any error occurs.
     */
    public SecureWebServer(int port) {
        super(port);
        addHandler("$default", this);
    }

    /**
     * Executes the XML-RPC method, first authenticating the given user and
     * authorizing him based on his roles.
     * 
     * @param methodSpecifier
     *            the name of the XML-RPC method to call.
     * @param params
     *            the parameters passed to the method.
     * @user The username to authenticate to call the method.
     * @pass The password to use for the user.
     */
    public Object execute(String methodSpecifier, Vector params, String user,
            String password) {
        for (Object dispatcher : dispatchers) {
            Result rc = ((Dispatcher) dispatcher).handleRequest(methodSpecifier,
                params, user, password);
            if (rc != null) {
                return rc.getValue();
            }
        }
        throw new IllegalStateException(
                "No request dispatcher was able to return a non-null value to the XML-RPC caller");
    }

    /**
     * Adds an authenticated XML-RPC dispatcher.
     * 
     * @param dispatcher
     *            The dispatcher to add which will service the authenticated
     *            XML-RPC method calls
     * 
     */
    public void addDispatcher(Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException(
                "Non-null dispatchers are illegal");
        }
        dispatchers.add(dispatcher);
    }

    private List dispatchers = new ArrayList();
}
