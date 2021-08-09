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

package org.apache.oodt.cas.filemgr.system;

//OODT imports
import org.apache.oodt.cas.filemgr.catalog.Catalog;

/**
 * @author radu
 *
 * <p>Interface of server for FileManager RPC logic.</p>
 *
 */
public interface FileManagerServer {
    
    String FILEMGR_PROPERTIES_FILE_SYSTEM_PROPERTY = "org.apache.oodt.cas.filemgr.properties";
    String FILEMGR_SERVER_SYSTEM_PROPERTY = "filemgr.server";
    String FILEMGR_CLIENT_SYSTEM_PROPERTY = "filemgr.client";
    
    /**
     *
     * <p>Preparing and starting up the rpc server.</p>
     *
     * @return
     * @throws Exception
     *              If any error occurs while starting up the server.
     */
    public boolean startUp() throws Exception;

    /**
     *
     * <p>Shutting down the server.</p>
     *
     *
     * @return
     */
    public boolean shutdown();

    /**
     *
     * <p>Verifying if the server is alive.</p>
     *
     * @return
     */
    public boolean isAlive();

    /**
     *
     * <p>Passing the {@link Catalog} to {@link FileManager} implementation.</p>
     *
     * @param catalog
     *                {@link Catalog} for {@link FileManager}
     */
    public void setCatalog(Catalog catalog);
}
