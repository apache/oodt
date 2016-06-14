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

//CAS imports
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An interface for pagination of {@link org.apache.oodt.cas.filemgr.structs.Product} {@link java.util.List}s..
 * </p>
 * 
 */
public interface Pagination {

    /**
     * @param type
     *            The ProductType to obtain the first {@link ProductPage} for.
     * @return The first page of products for a particular {@link ProductType}.
     */
    ProductPage getFirstPage(ProductType type);

    /**
     * 
     * @param type
     *            The ProductType to obtain the last {@link ProductPage} for.
     * @return The last page of products for a particular {@link ProductType}.
     */
    ProductPage getLastProductPage(ProductType type);

    /**
     * 
     * @param type
     *            The ProductType to obtain the next page for, given the
     *            <code>currentPage</code>.
     * @param currentPage
     *            The current page that tells the function what the next page to
     *            obtain is.
     * @return The next page in the ProductType product list, given the
     *         currentPage.
     */
    ProductPage getNextPage(ProductType type, ProductPage currentPage);

    /**
     * 
     * @param type
     *            The ProductType to obtain the previous page for, given the
     *            <code>currentPage</code>.
     * @param currentPage
     *            The currentPage that tells the function what the previous page
     *            to obtain is.
     * @return The previous page in the ProductType product list, given the
     *         currentPage.
     */
    ProductPage getPrevPage(ProductType type, ProductPage currentPage);
}
