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
package org.apache.oodt.cas.filemgr.structs;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Page of {@link Product}s returned from the <code>File Manager</code>.
 * </p>
 * 
 */
public class ProductPage {

    /* the number of this page */
    private int pageNum = -1;

    /* the size of the number of products on this page */
    private int pageSize = -1;
    
    private int numOfHits = -1;

    /* the list of produdcts associated with this page */
    private List<Product> pageProducts = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public ProductPage() {
        pageProducts = new Vector<Product>();
    }

    /**
     * @param pageNum
     *            The number of this page.
     * @param totalPages
     *            The total number of pages in the set.
     * @param pageSize
     *            The size of this page.
     * @param pageProducts
     *            The products associated with this page.
     */
    public ProductPage(int pageNum, int pageSize, int numOfHits,
            List<Product> pageProducts) {
        this.pageNum = pageNum;
        this.numOfHits = numOfHits;
        this.pageSize = pageSize;
        this.pageProducts = pageProducts;
    }

    /**
     * @return Returns the pageNum.
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * @param pageNum
     *            The pageNum to set.
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setNumOfHits(int numOfHits) {
    	this.numOfHits = numOfHits;
    }
    
    public int getNumOfHits() {
    	return this.numOfHits;
    }
    
    /**
     * @return Returns the pageProducts.
     */
    public List<Product> getPageProducts() {
        return pageProducts;
    }

    /**
     * @param pageProducts
     *            The pageProducts to set.
     */
    public void setPageProducts(List<Product> pageProducts) {
        this.pageProducts = pageProducts;
    }

    /**
     * @return Returns the pageSize.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize
     *            The pageSize to set.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return Returns the totalPages.
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) this.numOfHits / (double) this.pageSize);
    }
//
//    /**
//     * @param totalPages
//     *            The totalPages to set.
//     */
//    public void setTotalPages(int totalPages) {
//        this.totalPages = totalPages;
//    }

    /**
     * 
     * @return True if this is the last page in the set, false otherwise.
     */
    public boolean isLastPage() {
        return pageNum == this.getTotalPages();
    }

    /**
     * 
     * @return True if this is the fist page of the set, false otherwise.
     */
    public boolean isFirstPage() {
        return pageNum == 1;
    }

    /**
     * 
     * @return A blank, unpopulated {@link ProductPage}.
     */
    public static ProductPage blankPage() {
        ProductPage blank = new ProductPage();
        blank.setPageNum(0);
        blank.setPageSize(0);
        blank.setNumOfHits(0);
        return blank;
    }

}
