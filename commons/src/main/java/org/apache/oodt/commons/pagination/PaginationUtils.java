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


package org.apache.oodt.commons.pagination;

import java.util.List;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A set of utility methods to do pagination.
 * </p>
 * 
 */
public final class PaginationUtils {

    private PaginationUtils() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }

    public static int hasRemainder(int divisor, int quotient) {
        if (divisor % quotient != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int computeCurrentPage(int startIdx, int productsPerPage) {
        return ((startIdx + 1) / productsPerPage)
                + hasRemainder(startIdx + 1, productsPerPage);
    }

    public static int computeCurrentWindow(int currPage, int pagesPerPage) {
        return (currPage / pagesPerPage) + hasRemainder(currPage, pagesPerPage);
    }

    public static int computeMinPage(int pagesPerPage, int currWindow) {
        return (pagesPerPage * (currWindow - 1)) + 1;
    }

    public static int computeMaxPage(int minPage, int pagesPerPage,
            int lastIdx, int productsPerPage) {
        return Math.min(minPage + (pagesPerPage - 1), computeCurrentPage(
                lastIdx, productsPerPage));
    }

    public static int computeEndIdx(int currPage, int productsPerPage,
            int totalProducts) {
        return Math.min((currPage * productsPerPage) - 1, totalProducts - 1);
    }

    public static int computePrevStartIdx(int minPage, int productsPerPage) {
        int prevPage = Math.max(minPage - 2, 1);
        return (prevPage * productsPerPage);
    }

    public static int computeNextStartIdx(int maxPage, int productsPerPage) {
        return (maxPage * productsPerPage);

    }

    public static List iterateFrom(final int startIndex,
            final List originalList, int pageSize) {
        final int totalSize = originalList.size();

        int endIndex = startIndex + pageSize;
        if (endIndex > totalSize) {
            endIndex = totalSize;
        }

        return originalList.subList(startIndex, endIndex);
    }

    public static int getTotalPage(List originalList, int pageSize) {
        if (originalList == null || originalList.size() <= 0) {
            return 0;
        }
        final int totalSize = originalList.size();
        return ((totalSize - 1) / pageSize) + 1;
    }

    public static int getTotalPage(int numTotal, int pageSize) {
        if (numTotal <= 0) {
            return 0;
        }
        return ((numTotal - 1) / pageSize) + 1;
    }

}
