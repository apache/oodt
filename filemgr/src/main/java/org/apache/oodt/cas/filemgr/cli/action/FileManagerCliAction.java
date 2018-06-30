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
package org.apache.oodt.cas.filemgr.cli.action;


import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base {@link CmdLineAction} for File Manager.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class FileManagerCliAction extends CmdLineAction {

    private FileManagerClient fmc;

    public String getUrl() {
        return System.getProperty("org.apache.oodt.cas.filemgr.url");
    }

    /**
     * Returns a new {@link FileManagerClient}. The client should be closed by calling {@link FileManagerClient#close()}
     * after using.
     *
     * TODO remove reference to {@link #fmc} which cannot be removed atm due to mock client used in tests.
     *
     * @return client
     * @throws MalformedURLException
     * @throws ConnectionException
     */
    protected FileManagerClient getClient() throws MalformedURLException, ConnectionException {
        Validate.notNull(getUrl(), "Must specify url");

        if (this.fmc != null) {
            return fmc;
        }

        return RpcCommunicationFactory.createClient(new URL(getUrl()), false);
    }

    public void setClient(FileManagerClient client) {
        this.fmc = client;
    }
}
