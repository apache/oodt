/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.product.jaxrs.services;


import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.product.exceptions.CasProductException;
import org.apache.oodt.cas.product.jaxrs.enums.ErrorTypes;
import org.apache.oodt.cas.product.jaxrs.exceptions.BadRequestException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.resources.ProductPageResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for Proposing Apache OODT-2.0 FileManager REST-APIs
 * This handles HTTP requests and returns file manager entities
 * JAX-RS resources converted to different formats.
 * @author ngimhana (Nadeeshan Gimhana)
 */
public class FileManagerJaxrsServiceV2 {

    private static final Logger LOGGER = Logger.getLogger(FileManagerJaxrsServiceV2
            .class.getName());


    // The servlet context, which is used to retrieve context parameters.
    @Context
    private ServletContext context;


    /**
     * Gets an HTTP request that represents a {@link ProductPage} from the file
     * manager.
     * @param productTypeName the Name of a productType
     * @return an HTTP response that represents a {@link ProductPage} from the file
     * manager
     */
    @GET
    @Path("products")
    @Produces({"application/xml", "application/json", "application/atom+xml",
            "application/rdf+xml", "application/rss+xml"})
    public ProductPageResource getFirstPage(@QueryParam("productTypeName")  String productTypeName) throws WebApplicationException {

        try {
            FileManagerClient client = getContextClient();

            // Get the first ProductPage
            ProductPage genericFile = client.getFirstPage(client.getProductTypeByName(productTypeName));

            return getProductPageResource(client,genericFile);

        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }


    /**
     * Gets an HTTP request that represents a {@link ProductPage} from the file
     * manager.
     * @param productTypeName the Name of a productType
     * @param currentProductPage the current productPage
     * @return an HTTP response that represents a {@link ProductPage} from the file
     * manager
     */
    @GET
    @Path("products")
    @Produces({"application/xml", "application/json", "application/atom+xml",
            "application/rdf+xml", "application/rss+xml"})
    public ProductPageResource getNextPage(
            @QueryParam("productTypeName")  String productTypeName ,
            @QueryParam("currentProductPage") int currentProductPage

    ) throws WebApplicationException {

        try {
            FileManagerClient client = getContextClient();


            // Get the first ProductPage
            ProductPage firstpage = client.getFirstPage(client.getProductTypeByName(productTypeName));

            // Get the next ProductPage
            ProductPage nextPage = client.getNextPage(client.getProductTypeByName(productTypeName),firstpage);

            // Searching for the current page
            while(nextPage.getPageNum() != currentProductPage-1){
                nextPage = client.getNextPage(client.getProductTypeByName(productTypeName),nextPage);
            }

            // Get the next page from the current page
            ProductPage genericFile = client.getNextPage(client.getProductTypeByName(productTypeName),nextPage);

            // Return ProductPage resource
            return getProductPageResource(client,genericFile);

        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }


    /**
     * This method is for creating a ProductPageResource Response and return it
     * @param client FileManager client
     * @param genericFile First/next/prev ProductPage
     */
    private ProductPageResource getProductPageResource(FileManagerClient client,ProductPage genericFile) throws CatalogException, CasProductException {

        // List for storing Metadata of the products in the ProductPage
        List<Metadata> proMetaDataList = new ArrayList<>();

        // List for storing References of the products in the ProductPage
        List<List<Reference>> proReferencesList = new ArrayList<>();

        for(Product pro : genericFile.getPageProducts()){
            Metadata metadata = client.getMetadata(pro);
            List<Reference> productReferences = pro.getProductReferences();
            proMetaDataList.add(metadata);
            proReferencesList.add(productReferences);
        }

        return new ProductPageResource(genericFile,proMetaDataList, proReferencesList,getContextWorkingDir());
    }


    /**
     * Gets the file manager's working directory from the servlet context.
     *
     * @return the file manager working directory
     * @throws Exception if an object cannot be retrieved from the context
     *                   attribute
     */
    public File getContextWorkingDir() throws CasProductException {
        Object workingDirObject = context.getAttribute("workingDir");
        if (workingDirObject != null && workingDirObject instanceof File) {
            return (File) workingDirObject;
        }

        String message = ErrorTypes.CAS_PRODUCT_EXCEPTION_FILEMGR_WORKING_DIR_UNAVILABLE.getErrorType();
        LOGGER.log(Level.WARNING, message);
        throw new CasProductException(message);
    }


    /**
     * Gets the file manager client instance from the servlet context.
     *
     * @return the file manager client instance from the servlet context attribute
     * @throws Exception if an object cannot be retrieved from the context
     *                   attribute
     */
    public FileManagerClient getContextClient()
            throws CasProductException {
        // Get the file manager client from the servlet context.
        Object clientObject = context.getAttribute("client");
        if (clientObject != null &&
                clientObject instanceof FileManagerClient) {
            return (FileManagerClient) clientObject;
        }

        String message = ErrorTypes.CAS_PRODUCT_EXCEPTION_FILEMGR_CLIENT_UNAVILABLE.getErrorType();
        LOGGER.log(Level.WARNING, message);
        throw new CasProductException(message);
    }





    @GET
    @Path("product")
    @Produces({"application/xml", "application/json", "application/atom+xml",
            "application/rdf+xml", "application/rss+xml", "application/zip"})
    public ProductResource getProduct(@QueryParam("productId") String productId) throws WebApplicationException
    {
        if (productId == null || productId.trim().equals(""))
        {
            throw new BadRequestException(ErrorTypes.BAD_REQUEST_EXCEPTION_PRODUCT_RESOURCE.getErrorType());
        }

        try
        {
            FileManagerClient client = getContextClient();

            // Find the product.
            Product product = client.getProductById(productId);
            product.setProductReferences(client.getProductReferences(product));

            // Create the product resource, add the product data and return the
            // resource as the HTTP response.
            return new ProductResource(product, client.getMetadata(product),
                    product.getProductReferences(), getContextWorkingDir());
        }
        catch (Exception e)
        {
            // Just for Logging Purposes
            String message = "Unable to find the requested resource.";
            LOGGER.log(Level.FINE, message, e);

            throw new NotFoundException(e.getMessage());
        }
    }


}
