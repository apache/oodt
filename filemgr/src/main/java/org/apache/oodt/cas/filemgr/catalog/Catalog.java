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

package org.apache.oodt.cas.filemgr.catalog;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.util.Pagination;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Catalog is the front end object interface for a back end peristance layer
 * for storing product references and metadata. Classes implementing this
 * interface implement methods to retrieve and store product references and
 * metadata from a particular backend repository.
 * </p>
 * 
 */
public interface Catalog extends Pagination {

    String X_POINT_ID = Catalog.class.getName();

    /**
     * <p>
     * Ingests {@link Metadata} into the metadata store, and associates it with
     * the given <code>productId</code>.
     * </p>
     * 
     * @param m
     *            The {@link Metadata} to ingest.
     * @param product
     *            The product to add the metadata for.
     * @throws CatalogException
     *             If any general error occurs.
     */
    void addMetadata(Metadata m, Product product)
            throws CatalogException;

    /**
     * <p>
     * Removes {@link Metadata} from the metadata store, and disassociates it
     * from the given <code>productId</code>.
     * </p>
     * 
     * @param m
     *            The {@link Metadata} to remove.
     * @param product
     *            The product for which the metadata is to be removed.
     * @throws CatalogException
     *             If any general error occurs.
     */
    void removeMetadata(Metadata m, Product product)
            throws CatalogException;

    /**
     * <p>
     * Adds a Product to the Catalog.
     * </p>
     * 
     * @param product
     *            The {@link Product} to add.
     * @throws CatalogException
     *             If any error occurs during the add.
     */
    void addProduct(Product product) throws CatalogException;

    /**
     * <p>
     * Modifies an existing Product within the Catalog.
     * </p>
     * 
     * @param product
     *            The new {@link Product} information to modify the existing
     *            Product with.
     * @throws CatalogException
     *             If any error occurs.
     */
    void modifyProduct(Product product) throws CatalogException;

    /**
     * <p>
     * Removes a {@link Product} from the Catalog.
     * </p>
     * 
     * @param product
     *            The product to remove.
     * @throws CatalogException
     *             If any error occurs.
     */
    void removeProduct(Product product) throws CatalogException;

    /**
     * <p>
     * Persists the <code>transferStatus</code> attribute of the given
     * {@link Product} to the Catalog.
     * 
     * @param product
     *            The Product whose transfer status will be persisted. The
     *            caller should make sure that the product ID field is set.
     * @throws CatalogException
     */
    void setProductTransferStatus(Product product)
            throws CatalogException;

    /**
     * <p>
     * Adds the specified {@link List} of {@link Reference}s to the
     * {@link Catalog}, and associates them with this {@link Product} specified
     * by its <code>productId</code>.
     * </p>
     * 
     * @param product
     *            The product to add references for. The references are read
     *            from the Products list of References.
     * @throws CatalogException
     *             If anything goes wrong.
     */
    void addProductReferences(Product product) throws CatalogException;

    /**
     * <p>
     * Gets a {@link Product}, with the specified <code>productId</code>.
     * </p>
     * 
     * @param productId
     *            The unique ID of the Product to retrieve.
     * @return A {@link Product}, with the given ID. The implementer of this
     *         method should ensure that the product {@link Reference}s are
     *         populated as well.
     * @throws CatalogException
     *             If any error occurs.
     */
    Product getProductById(String productId) throws CatalogException;

    /**
     * <p>
     * Gets a {@link Product} with the specified <code>productName</code>.
     * </p>
     * 
     * @param productName
     *            The name of the Product to retrieve.
     * @return A {@link Product} with the given name. The implementer of this
     *         method should ensure that the product {@link Reference}s are
     *         populated as well.
     * @throws CatalogException
     */
    Product getProductByName(String productName) throws CatalogException;

    /**
     * <p>
     * Gets the {@link Reference}s associated with this Product.
     * </p>
     * 
     * @param product
     *            The {@link Product} to obtain the References for.
     * @return A {@link List} of {@link Reference}s, associated with the
     *         specified Product.
     * @throws CatalogException
     */
    List getProductReferences(Product product) throws CatalogException;

    /**
     * <p>
     * Gets all the {@link Product}s in the {@link Catalog}.
     * </p>
     * 
     * @return A {@link List} of {@link Product}s in the {@link Catalog}.
     * @throws CatalogException
     *             If any error occurs.
     */
    List<Product> getProducts() throws CatalogException;

    /**
     * <p>
     * Gets the {@link Product}s associated with the specified ProductType in
     * the {@link Catalog}.
     * </p>
     * 
     * @param type
     *            The {@link ProductType} to obtain the {@link Product}s for.
     * @return A {@link List} of {@link Product}s that are associated with the
     *         specified productType in the {@link Catalog}.
     * @throws CatalogException
     *             If any error occurs.
     */
    List<Product> getProductsByProductType(ProductType type)
            throws CatalogException;

    /**
     * <p>
     * Gets the Metadata for a given Product.
     * </p>
     * 
     * @param product
     *            The {@link Product} to obtain the Metadata for.
     * @return The {@link Metadata} for the given <code>productId</code>.
     *         </p>
     * @throws CatalogException
     *             If any error occurs.
     */
    Metadata getMetadata(Product product) throws CatalogException;

    /**
     * <p>
     * Gets a reduced set of metadata for a give Product.
     * </p>
     * 
     * @param product
     *            The {@link Product} to obtain the Metadata for.
     * @param elements
     *            The set of metadata elements of interest.
     * @return The reduced {@link Metadata} for the given <code>productId</code>.
     * @throws CatalogException
     *             If any error occurs.
     */
    Metadata getReducedMetadata(Product product, List<String> elements)
            throws CatalogException;

    /**
     * <p>
     * Queries the Catalog with the specified {@link Query}
     * </p>.
     * 
     * @param query
     *            The set of criteria by which to query the Catalog.
     * @param type
     *            The {@link ProductType} that should be queried.
     * @return A {@link List} of String product IDs that can be used to retrieve
     *         products that match the query.
     * @throws CatalogException
     *             If any error occurs.
     */
    List<String> query(Query query, ProductType type) throws CatalogException;

    /**
     * <p>
     * Performs a query against the underlying {@link Catalog}, and then
     * properly formulates a page of results to send back to the user. This
     * method is useful when you would like to conserve memory and not send back
     * the entire list of results, nor load them into memory. Of course, this
     * method assumes that queries are deterministic, i.e., the same query
     * issued 2x within a paginating session will produce the same set of
     * results to paginate.
     * </p>
     * 
     * @param query
     *            The query to perform against the underlying Catalog.
     * @param type
     *            The {@link ProductType} that you are querying for.
     * @param pageNum
     *            The number of the {@link ProductPage} to return back to the
     *            user.
     * @return The requested {@link ProductPage} of results.
     * @throws CatalogException
     *             If any error occurs.
     */
    ProductPage pagedQuery(Query query, ProductType type, int pageNum)
            throws CatalogException;

    /**
     * <p>
     * Gets the top <code>N</code> most recent products that have been
     * cataloged.
     * </p>
     * 
     * @param n
     *            The amount of recent products to return.
     * @return A {@link List} of {@link Product}s that have been cataloged
     *         recently.
     * @throws CatalogException
     *             If any error occurs.
     */
    List<Product> getTopNProducts(int n) throws CatalogException;

    /**
     * <p>
     * Gets the top <code>N</code> most recent products that have been
     * cataloged for the given {@link ProductType}.
     * </p>
     * 
     * @param n
     *            The amount of recent products to return.
     * @param type
     *            The ProductType to limit the query to.
     * @return A {@link List} of {@link Product}s that have been cataloged
     *         recently.
     * @throws CatalogException
     *             If any error occurs.
     */
    List<Product> getTopNProducts(int n, ProductType type)
            throws CatalogException;

    /**
     * 
     * @return The {@link ValidationLayer} that is used by this Catalog.
     */
    ValidationLayer getValidationLayer();

    /**
     * 
     * @param type
     *            The ProductType to count the number of products for.
     * @return An integer count of the number of {@link Product}s for the
     *         specified {@link ProductType}.
     * @throws CatalogException
     *             If any error occurs.
     */
    int getNumProducts(ProductType type) throws CatalogException;

}
