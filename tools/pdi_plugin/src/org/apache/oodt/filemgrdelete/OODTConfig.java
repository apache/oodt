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


package org.apache.oodt.filemgrdelete;


import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bugg on 07/03/14.
 */
public class OODTConfig {

    private static final Logger LOG = Logger.getLogger(OODTConfig.class.getName());

    private URL fmUrl;

    public URL getFmUrl(){
        return this.fmUrl;
    }

    private static XmlRpcFileManagerClient client = null;

    public boolean loadXMLRpcClient(String fmUrlStr){
        try {
            client = new XmlRpcFileManagerClient(new URL(fmUrlStr));
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Unable to create file manager client: Message: "
                            + e.getMessage() + ": errors to follow");
        }
        return true;
    }

    public XmlRpcFileManagerClient getXMLRpcClient(){
        return client;
    }
}
