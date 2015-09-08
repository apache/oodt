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

import junit.framework.TestCase;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class TestServerClientFactories extends TestCase {

    FileManagerServer fm;


    public void setProprieties(){

        //get all properties
        Properties properties = new Properties(System.getProperties());

        // first load the example configuration
        try {
            URL filemgrPropertiesUrl = this.getClass().getResource(
                    "/filemgr.properties");

            properties.load(new FileInputStream(new File(filemgrPropertiesUrl.getFile())));
            //set Properties so when the server will be instantiated from the right class.
            System.setProperties(properties);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void setUp(){
        setProprieties();
    }

    public void testServerInitialization(){
        try {
            fm = RpcCommunicationFactory.createServer(60001);
            assertNotNull(fm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
