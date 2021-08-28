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

package org.apache.oodt.cas.filemgr.util;

import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.system.rpc.FileManagerClientFactory;
import org.apache.oodt.cas.filemgr.system.rpc.FileManagerServerFactory;
import org.apache.oodt.cas.filemgr.system.rpc.XmlRpcFileManagerClientFactory;
import org.apache.oodt.cas.filemgr.system.rpc.XmlRpcFileManagerServerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class that manage the initialization of {@link FileManagerServer} and {@link FileManagerClient}
 * ether with AvroRpc or XmlRpc logic. Default is AvroRpc.
 *
 * @author radu
 *
 */
public class RpcCommunicationFactory {

    private static Logger LOG = LoggerFactory.getLogger(RpcCommunicationFactory.class);

    private static String getClientFactoryName() {
        return System.getProperty(FileManagerServer.FILEMGR_CLIENT_SYSTEM_PROPERTY,
                XmlRpcFileManagerClientFactory.class.getName());
    }

    /**
     * Set properties from filemgr.properties file
     * to get name of RPC initialization class.
     */
    private static void setPror(){
        // set up the configuration, if there is any
        if (System.getProperty(FileManagerServer.FILEMGR_PROPERTIES_FILE_SYSTEM_PROPERTY) != null) {
            String configFile = System.getProperty(FileManagerServer.FILEMGR_PROPERTIES_FILE_SYSTEM_PROPERTY);

            LOG.info("Loading File Manager Configuration Properties from: [{}]", configFile);

            try {
                System.getProperties().load(new FileInputStream(new File(configFile)));
            } catch (Exception e) {
                LOG.warn("Error loading configuration properties from: [{}]: {}", configFile, e.getMessage(), e);
            }
        }
    }

    /**
     * Initialization of RPC client.
     * @param filemgrUrl
     * @return instance of ether AvroRpc of XMLRPC of FileManagerClient default is AvroRpc.
     * @throws ConnectionException
     */
    public static FileManagerClient createClient(URL filemgrUrl) throws ConnectionException {
        setPror();

        try {
            FileManagerClientFactory fmcf= (FileManagerClientFactory) Class.forName(getClientFactoryName()).newInstance();
            fmcf.setTestConnection(true);
            fmcf.setUrl(filemgrUrl);
            return fmcf.createFileManagerClient();
        } catch (InstantiationException e) {
            throw new ConnectionException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ConnectionException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    /**
     * Initialization of RPC client
     * @param filemgrUrl
     * @param testConnection
     * @return instance of ether AvroRpc of XMLRPC of FileManagerClient.
     * @throws ConnectionException
     */
    public static FileManagerClient createClient(URL filemgrUrl, boolean testConnection) throws ConnectionException {
        setPror();

        try {
            FileManagerClientFactory fmcf= (FileManagerClientFactory) Class.forName(getClientFactoryName()).newInstance();
            fmcf.setTestConnection(testConnection);
            fmcf.setUrl(filemgrUrl);
            return fmcf.createFileManagerClient();

        } catch (InstantiationException e) {
            throw new ConnectionException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ConnectionException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    /**
     *
     * Initialization of RPC server.
     *
     * @param port
     * @return instance of ether AvroRpc of XMLRPC of FileManagerServer.
     * @throws IOException
     */
    public static FileManagerServer createServer(int port) throws IOException {
        setPror();

        String serverFactory = System.getProperty(FileManagerServer.FILEMGR_SERVER_SYSTEM_PROPERTY,
                XmlRpcFileManagerServerFactory.class.getName());

        LOG.info("Init. server's factory class: " + serverFactory);

        try {
            FileManagerServerFactory fmsf= (FileManagerServerFactory) Class.forName(serverFactory).newInstance();
            fmsf.setPort(port);
            return fmsf.createFileManagerServer();
        } catch (InstantiationException e) {
            LOG.error("InstantiationException: Could not start FileManager server: {}", e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOG.error("IllegalAccessException: Could not start FileManager server: {}", e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            LOG.error("ClassNotFoundException: Could not start FileManager server: {}", e.getMessage(), e);
        }
        return null;
    }

}
