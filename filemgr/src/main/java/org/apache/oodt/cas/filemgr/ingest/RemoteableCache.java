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

//JDK imports
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 *  A Java RMI {@link Remote}-able implementation of the {@link Cache}
 *  interface.
 * </p>.
 */
public interface RemoteableCache extends Remote{

    String DEFAULT_UNIQUE_MET_KEY = "CAS.ProductName";

    /**
     * Clears the current cache. This should be used before a re-
     * occurs.
     * 
     */
    void clear() throws RemoteException;

    /**
     * Checks the cache to determine if a {@link Product} with the given
     * <code>productName</code> exists.
     * 
     * @param productName
     *            The name of the {@link Product} to check for in the Cache.
     * @return True if the {@link Product} is foudn in the Cache, False,
     *         otherwise.
     */
    boolean contains(String productName) throws RemoteException;

    /**
     * Specifies the {@link URL} to the File Manager to connect this Cache to.
     * 
     * @param fmUrl
     *            The {@link URL} of the File Manager to cache {@link Product}s
     *            from.
     */
    void setFileManager(URL fmUrl) throws RemoteException;

    /**
     * 
     * @return The size of the current {@link Product} cache.
     */
    int size() throws RemoteException;

    /**
     * Synchronizes this Cache with the File Manager specified by the
     * {@link #setFileManager(URL)} method.
     * 
     * @param uniqueElementProductTypeNames
     *            The {@link List} {@link ProductType}s to cache
     *            {@link Product}s from.
     * @throws RemoteException
     *             If there is any error.
     */
    void sync(List<String> uniqueElementProductTypeNames) throws RemoteException;

    /**
     * Synchronizes this Cache with the File Manager specified by the
     * {@link #setFileManager(URL)} method.
     * 
     * @param uniqueElementName
     *            The met key identifier to use for determining {@link Product}
     *            existance (if different than {@value #DEFAULT_UNIQUE_MET_KEY}).
     * @param uniqueElementProductTypeNames
     *            The {@link List} of {@link ProductType}s to cache
     *            {@link Product}s from.
     * @throws RemoteException
     *             If there is any error.
     */
    void sync(String uniqueElementName,
              List<String> uniqueElementProductTypeNames) throws RemoteException;

    /**
     * Synchronizes this Cache with the File Manager specified by the
     * {@link #setFileManager(URL)} method. This method also assumes that the
     * unique element (identified by {@link #setUniqueElementName(String)} and
     * the {@link List} of {@link ProductType}s have also been set appropriate
     * (e.g., by using {@link #setUniqueElementProductTypeNames(List)}.
     * 
     * @throws RemoteException
     *             If any error occurs.
     */
    void sync() throws RemoteException;

    /**
     * Sets the names of the {@link ProductType}s to cache.
     * 
     * @param uniqueElementProductTypeNames
     *            A {@link List} of java.util.String names of
     *            {@link ProductType}s.
     */
    void setUniqueElementProductTypeNames(
        List<String> uniqueElementProductTypeNames) throws RemoteException;

    /**
     * Sets the name of the met element to use as the identifier of a
     * {@link Product} for use in determining whether the Product is in the
     * Cache.
     * 
     * @param uniqueElementName
     *            The name of the met element used to uniquely identify
     *            {@link Product}s.
     */
    void setUniqueElementName(String uniqueElementName)
            throws RemoteException;

    /**
     * Gets the {@link URL} of the File Manager that this Cache communicates
     * with.
     * 
     * @return The {@link URL} of the File Manager that this Cache communicates
     *         with.
     */
    URL getFileManagerUrl() throws RemoteException;
}
