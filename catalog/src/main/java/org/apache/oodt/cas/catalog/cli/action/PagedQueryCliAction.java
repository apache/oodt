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
package org.apache.oodt.cas.catalog.cli.action;

//JDK imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.parser.QueryParser;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;


/**
 * @author bfoster (Brian Foster)
 */
public class PagedQueryCliAction extends CatalogServiceCliAction {

   private int pageNum = -1;
   private int pageSize = -1;
   private String query;
   private Set<String> catalogIds;

   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.isTrue(pageNum != -1, "Must specify pageNum");
         Validate.isTrue(pageSize != -1, "Must specify pageSize");
         Validate.notNull(query, "Must specify query");

         QueryExpression queryExpression = QueryParser
               .parseQueryExpression(query);
         Page page;
         if (catalogIds == null) {
            page = getClient().getPage(new PageInfo(pageSize, pageNum),
                  queryExpression);
         } else {
            page = getClient().getPage(new PageInfo(pageSize, pageNum),
                  queryExpression, catalogIds);
         }
         List<TransactionalMetadata> transactionMetadatas = getClient()
               .getMetadata(page);
         for (TransactionalMetadata tMet : transactionMetadatas) {
            printer.print("ID: " + tMet.getTransactionId() + " ; CatalogIDs: "
                  + tMet.getCatalogIds() + " ; Metadata: (");
            StringBuilder sb = new StringBuilder("");
            for (Object metKey : tMet.getMetadata().getMap().keySet()) {
               sb.append(metKey).append("=").append(tMet.getMetadata().getAllMetadata((String) metKey)
                                                        .toString().replaceAll("[\\[\\]]", "'")).append(", ");
            }
            printer.println(sb.substring(0, sb.length() - 2) + ")");
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to perform query '" + query
               + "' : " + e.getMessage(), e);
      }
   }

   public void setPageSize(int pageSize) {
      this.pageSize = pageSize;
   }

   public void setPageNum(int pageNum) {
      this.pageNum = pageNum;
   }

   public void setQuery(String query) {
      this.query = query;
   }

   public void setCatalogIds(List<String> catalogIds) {
      this.catalogIds = new HashSet<String>(catalogIds);
   }

   public int getPageNum() {
      return pageNum;
   }

   public int getPageSize() {
      return pageSize;
   }

   public String getQuery() {
      return query;
   }

   public Set<String> getCatalogIds() {
      return catalogIds;
   }

   public void setCatalogIds(Set<String> catalogIds) {
      this.catalogIds = catalogIds;
   }
}
