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


package org.apache.oodt.cas.filemgr.system.rpc;

import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

import java.net.URL;
@Deprecated
public class XmlRpcFileManagerClientFactory implements FileManagerClientFactory {

    private URL url;

    private boolean testConnection = true;

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public void setTestConnection(boolean testConnection) {
        this.testConnection = testConnection;
    }

    @Override
    public boolean getTestConnection() {
        return this.testConnection;
    }

    @Override
    public FileManagerClient createFileManagerClient() throws ConnectionException {
        return new XmlRpcFileManagerClient(this.url,this.testConnection);
    }

}
