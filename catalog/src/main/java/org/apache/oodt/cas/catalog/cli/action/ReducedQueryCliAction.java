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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.parser.QueryParser;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * A {@link CmdLineAction} which queries the Catalog Service and limits the
 * results to a subset of given terms.
 *
 * @author bfoster (Brian Foster)
 */
public class ReducedQueryCliAction extends CatalogServiceCliAction {

   protected String query;
   protected Set<String> catalogIds;
   protected List<String> termNames;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(query, "Must specify query");
         Validate.notNull(termNames, "Must specify termNames");

         QueryExpression queryExpression = QueryParser
               .parseQueryExpression(query);
         QueryPager queryPager = null;
         if (catalogIds == null) {
            queryPager = getClient().query(queryExpression);
         } else {
            queryPager = getClient().query(queryExpression, catalogIds);
         }
         List<TransactionalMetadata> transactionMetadatas = getClient()
               .getAllPages(queryPager);
         for (TransactionalMetadata tMet : transactionMetadatas) {
            StringBuffer sb = new StringBuffer("");
            for (String termName : this.termNames) {
               List<String> values = tMet.getMetadata().getAllMetadata(
                     (String) termName);
               sb.append(termName
                     + " = '"
                     + (values == null ? "null" : StringUtils.join(
                           values.iterator(), ",")) + "', ");
            }
            printer.println(sb.substring(0, sb.length() - 2));
         }
      } catch (Exception e) {
         throw new CmdLineActionException("", e);
      }
   }

   public void setQuery(String query) {
      this.query = query;
   }

   public void setCatalogIds(List<String> catalogIds) {
      this.catalogIds = new HashSet<String>(catalogIds);
   }

   public void setReducedTerms(List<String> termNames) {
      this.termNames = termNames;
   }
}
