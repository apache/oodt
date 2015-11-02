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

import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An RMI client to the {@link RmiCacheServer}, implementing an RMI front end
 * to a {@link LocalCache}.
 * </p>.
 */
public class RmiCache implements Cache {
    private static Logger LOG = Logger.getLogger(RmiCache.class.getName());
    private RemoteableCache rmiCacheServer;

    public RmiCache(String rmiCacheServerUrn) throws InstantiationException {
        try {
            rmiCacheServer = (RemoteableCache) Naming.lookup(rmiCacheServerUrn);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new InstantiationException(
                    "Unable to connect to Rmi Cache Server at: ["
                            + rmiCacheServerUrn + "]");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#clear()
     */
    public void clear() {
        try {
            rmiCacheServer.clear();
        } catch (RemoteException ignored) {
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#contains(java.lang.String)
     */
    public boolean contains(String productName) {
        try {
            return rmiCacheServer.contains(productName);
        } catch (RemoteException e) {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#setFileManager(java.net.URL)
     */
    public void setFileManager(URL fmUrl) {
        try {
            rmiCacheServer.setFileManager(fmUrl);
        } catch (RemoteException ignored) {
            
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#size()
     */
    public int size() {
        try {
            return rmiCacheServer.size();
        } catch (RemoteException e) {
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#sync(java.util.List)
     */
    public void sync(List<String> uniqueElementProductTypeNames) throws CacheException {
        try {
            rmiCacheServer.sync(uniqueElementProductTypeNames);
        } catch (RemoteException e) {
            throw new CacheException(e.getMessage(), e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#sync(java.lang.String,
     *      java.util.List)
     */
    public void sync(String uniqueElementName,
            List<String> uniqueElementProductTypeNames) throws CacheException {
        try {
            rmiCacheServer.sync(uniqueElementName, uniqueElementProductTypeNames);
        } catch (RemoteException e) {
           throw new CacheException(e.getMessage(), e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#setUniqueElementProductTypeName(java.util.List)
     */
    public void setUniqueElementProductTypeNames(
            List<String> uniqueElementProductTypeNames) {
        try {
            rmiCacheServer
                    .setUniqueElementProductTypeNames(uniqueElementProductTypeNames);
        } catch (RemoteException ignored) {
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#sync()
     */
    public void sync() throws CacheException {
        try {
            rmiCacheServer.sync();
        } catch (RemoteException e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#setUniqueElementName(java.lang.String)
     */
    public void setUniqueElementName(String uniqueElementName) {
        try {
            rmiCacheServer.setUniqueElementName(uniqueElementName);
        } catch (RemoteException ignored) {
        }
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#getFileManagerUrl()
     */
    public URL getFileManagerUrl() {
        try {
            return rmiCacheServer.getFileManagerUrl();
        } catch (RemoteException e) {
            return null;
        }
    }

}
