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

package org.apache.oodt.cas.filemgr.repository;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;

//JDK imports
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * A Repository Manager is an extension point that is responsible for managing
 * {@link ProductType} information which boils down to policy information about
 * {@link org.apache.oodt.cas.filemgr.structs.Product}s that are ingested into the File Manager. This includes
 * information such as the root repository path for a product type, what type of
 * URI generation scheme to apply, etc.
 * </p>
 * 
 */
public interface RepositoryManager {

    /* extension point ID */
    String X_POINT_ID = RepositoryManager.class.getName();

    /**
     * <p>
     * Adds a ProductType to the RepositoryManager.
     * </p>
     * 
     * @param productType
     *            The {@link ProductType} to add.
     * @throws RepositoryManagerException
     */
    void addProductType(ProductType productType)
            throws RepositoryManagerException;

    /**
     * <p>
     * Modifies a ProductType in the RepositoryManager with the specified ID
     * field of the <code>productType</code>.
     * </p>
     * 
     * @param productType
     *            The new {@link ProductType} information.
     * @throws RepositoryManagerException
     *             If any error occurs.
     */
    void modifyProductType(ProductType productType)
            throws RepositoryManagerException;

    /**
     * <p>
     * Removes a ProductType from the RepositoryManager
     * </p>.
     * 
     * @param productType
     *            The productType to remove.
     * @throws RepositoryManagerException
     *             If any error occurs during the removal.
     */
    void removeProductType(ProductType productType)
            throws RepositoryManagerException;

    /**
     * <p>
     * Gets a {link ProductType} from the RepositoryManager identified by its
     * <code>productTypeId</code>.
     * </p>
     * 
     * @param productTypeId
     *            The ID of the ProductType to retrieve.
     * @return The {@link ProductType} corresponding to the specified
     *         <code>productTypeId</code>.
     * @throws RepositoryManagerException
     *             If any error occurs.
     */
    ProductType getProductTypeById(String productTypeId)
            throws RepositoryManagerException;

    /**
     * <p>
     * Gets a {@link ProductType} specified by its <code>productTypeName</code>,
     * from the RepositoryManager.
     * </p>
     * 
     * @param productTypeName
     *            The name of the ProductType to get.
     * @return A {@link ProductType}, with the specified name.
     * @throws RepositoryManagerException
     *             If any error occurs.
     */
    ProductType getProductTypeByName(String productTypeName)
            throws RepositoryManagerException;

    /**
     * <p>
     * Gets all the {@link ProductType}s from the repository.
     * </p>
     * 
     * @return A {@link List} of {@link ProductType}s from the repository.
     * @throws RepositoryManagerException
     *             If any error occurs.
     */
    List<ProductType> getProductTypes() throws RepositoryManagerException;
}
