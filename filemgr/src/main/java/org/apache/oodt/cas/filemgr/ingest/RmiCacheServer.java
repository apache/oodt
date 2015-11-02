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

package org.apache.oodt.cas.filemgr.ingest;

//OODT imports

import org.apache.oodt.cas.filemgr.structs.exceptions.CacheException;

import java.io.Serializable;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports

/**
 * 
 * @author bfoster
 * @author mattmann
 * 
 * A Java RMI based interface to a backend {@link LocalCache}.
 * 
 */
public class RmiCacheServer extends UnicastRemoteObject implements RemoteableCache, Serializable{

    private static Logger LOG = Logger.getLogger(RmiCacheServer.class.getName());
    private static final long serialVersionUID = -538329403363156379L;

    private LocalCache cache;

    private String uniqueElementName;

    private List<String> uniqueElementProductTypeNames;
    
    private Registry reg;

    public RmiCacheServer(URL fmUrl, String rangeQueryElementName,
            String rangeStartDateTime, String rangeEndDateTime,
            String uniqueElementName, List<String> productTypeNames)
            throws RemoteException {
        // initialize the cache
        cache = new LocalCache(fmUrl, rangeQueryElementName,
                rangeStartDateTime, rangeEndDateTime);
        this.uniqueElementName = uniqueElementName;
        this.uniqueElementProductTypeNames = productTypeNames;

    }

    public void launchServer(int rmiPort) throws RemoteException {
        launchServer(this.cache.getFileManagerUrl(), rmiPort);
    }

    public void launchServer(URL filemgrUrl, int rmiPort)
            throws RemoteException {
        syncWith(filemgrUrl);
        launchRmiServer(rmiPort);
    }

    public void stopServer(int port) throws RemoteException {
        try {
            Naming.unbind("rmi://localhost:" + port + "/RmiDatabaseServer");
            UnicastRemoteObject.unexportObject(reg,true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new RemoteException(
                    "Unable to unbind Database Server: reason: "
                            + e.getMessage());
        }
    }

    public void clear() throws RemoteException {
        cache.clear();

    }

    public boolean contains(String productName) throws RemoteException {
        return cache.contains(productName);
    }

    public void setFileManager(URL fmUrl) throws RemoteException {
        cache.setFileManager(fmUrl);
    }

    public int size() throws RemoteException {
        return cache.size();
    }

    public void sync(List<String> uniqueElementProductTypeNames) throws RemoteException {
        try {
            cache.sync(uniqueElementProductTypeNames);
        } catch (CacheException e) {
            throw new RemoteException(e.getMessage());
        }

    }

    public void sync(String uniqueElementName,
            List<String> uniqueElementProductTypeNames) throws RemoteException {
        try {
            cache.sync(uniqueElementName, uniqueElementProductTypeNames);
        } catch (CacheException e) {
            throw new RemoteException(e.getMessage());
        }

    }

    public void sync() throws RemoteException {
        if (this.uniqueElementName == null || (this.uniqueElementProductTypeNames == null || (
            this.uniqueElementProductTypeNames
                .size() == 0))) {
            throw new RemoteException(
                    "Both uniqueElementName and uniqueElementProductTypeNames must "
                            + "be defined in order to use this form of the sync operation!");
        }

        sync(this.uniqueElementName, this.uniqueElementProductTypeNames);

    }

    public URL getFileManagerUrl() throws RemoteException {
        return cache.getFileManagerUrl();
    }

    /**
     * @return the uniqueElementProductTypeNames
     */
    public List<String> getUniqueElementProductTypeNames() throws RemoteException {
        return uniqueElementProductTypeNames;
    }

    /**
     * @param uniqueElementProductTypeNames
     *            the uniqueElementProductTypeNames to set
     */
    public void setUniqueElementProductTypeNames(
            List<String> uniqueElementProductTypeNames) throws RemoteException {
        this.uniqueElementProductTypeNames = uniqueElementProductTypeNames;
    }

    /**
     * @return the uniqueElementName
     */
    public String getUniqueElementName() throws RemoteException {
        return uniqueElementName;
    }

    /**
     * @param uniqueElementName
     *            the uniqueElementName to set
     */
    public void setUniqueElementName(String uniqueElementName)
            throws RemoteException {
        this.uniqueElementName = uniqueElementName;
    }

    private void syncWith(URL url) throws RemoteException {
        cache.setFileManager(url);
        try {
            cache.sync(this.uniqueElementName,
                    this.uniqueElementProductTypeNames);
        } catch (CacheException e) {
            throw new RemoteException(
                    "Unable to sync cache with file manager: [" + url
                            + "]: Message: " + e.getMessage());
        }
    }

    private void launchRmiServer(int port) throws RemoteException {
        try {
            reg = LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://localhost:" + port + "/RmiDatabaseServer", this);
            System.out.println("RMI server created at rmi://localhost:" + port
                    + "/RmiDatabaseServer");
        } catch (Exception e) {
            throw new RemoteException("Failed to create RMI Server : "
                    + e.getMessage());
        }

        
    }

}
