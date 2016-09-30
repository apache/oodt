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

package org.apache.oodt.cas.workflow.system.rpc;

import org.apache.oodt.cas.workflow.system.WorkflowManager;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManager;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;

import java.net.URL;

/**
 * @deprecated replaced by avro-rpc
 */
@Deprecated
public class XmlRpcWorkflowManagerFactory implements WorkflowManagerFactory {

    private int port;

    private URL url;

    public void setPort(int port){
        this.port = port;
    }

    public void setUrl(URL url){
        this.url = url;
    }

    public WorkflowManager createServer(){
        return new XmlRpcWorkflowManager(port);
    }

    public WorkflowManagerClient createClient(){
        return new XmlRpcWorkflowManagerClient(url);
    }
}
