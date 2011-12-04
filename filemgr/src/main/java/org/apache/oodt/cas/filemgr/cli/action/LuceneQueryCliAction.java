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
package org.apache.oodt.cas.filemgr.cli.action;

//JDK imports
import java.util.List;

//Apache imports
import org.apache.commons.lang.Validate;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.tools.CASAnalyzer;

//Google imports
import com.google.common.collect.Lists;

/**
 * A {@link CmdLineAction} which converts a Lucene-like query into a File
 * Manager {@link Query} and returns the results.
 * 
 * @author bfoster (Brian Foster)
 */
public class LuceneQueryCliAction extends FileManagerCliAction {

   private static final String FREE_TEXT_BLOCK = "__FREE__";

   private String query;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(query, "Must specify query");

         XmlRpcFileManagerClient client = getClient();
         List<ProductType> productTypes = client.getProductTypes();
         if (productTypes == null) {
            throw new Exception("FileManager return null list of product types");
         }
         org.apache.oodt.cas.filemgr.structs.Query casQuery = new org.apache.oodt.cas.filemgr.structs.Query();
         casQuery.setCriteria(Lists.newArrayList(generateCASQuery(parseQuery(query))));

         List<String> productIds = query(client, casQuery, productTypes);
         if (productIds == null) {
            throw new Exception("FileManager returned null list of Product IDs");
         }
         for (String productId : productIds) {
            printer.println(productId);
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to perform lucene query '"
               + query + "' : " + e.getMessage(), e);
      }
   }

   public void setQuery(String query) {
      this.query = query;
   }

   private Query parseQuery(String query) throws ParseException {
      // note that "__FREE__" is a control work for free text searching
      return (Query) new QueryParser(FREE_TEXT_BLOCK, new CASAnalyzer())
            .parse(query);
   }

   private List<String> query(XmlRpcFileManagerClient client,
         org.apache.oodt.cas.filemgr.structs.Query fmQuery,
         List<ProductType> productTypes) throws Exception {
      List<String> productIds = Lists.newArrayList();
      for (ProductType type : productTypes) {
         List<Product> products = client.query(fmQuery, type);
         if (products == null) {
            throw new Exception(
                  "FileManager returned null products for query '" + query
                        + "' and product type '" + type.getName() + "'");
         }
         for (Product product : products) {
            productIds.add(product.getProductId());
         }
      }
      return productIds;
   }

   private QueryCriteria generateCASQuery(Query luceneQuery)
         throws Exception {
      if (luceneQuery instanceof TermQuery) {
         Term t = ((TermQuery) luceneQuery).getTerm();
         if (t.field().equals(FREE_TEXT_BLOCK)) {
            throw new Exception("Free text blocks not supported!");
         } else {
            return new TermQueryCriteria(t.field(), t.text());
         }
      } else if (luceneQuery instanceof PhraseQuery) {
         Term[] t = ((PhraseQuery) luceneQuery).getTerms();
         if (t[0].field().equals(FREE_TEXT_BLOCK)) {
            throw new Exception("Free text blocks not supported!");
         } else {
            BooleanQueryCriteria bqc = new BooleanQueryCriteria();
            bqc.setOperator(BooleanQueryCriteria.AND);
            for (int i = 0; i < t.length; i++) {
               bqc.addTerm(new TermQueryCriteria(t[i].field(), t[i]
                     .text()));
            }
            return bqc;
         }
      } else if (luceneQuery instanceof RangeQuery) {
         Term startT = ((RangeQuery) luceneQuery).getLowerTerm();
         Term endT = ((RangeQuery) luceneQuery).getUpperTerm();
         return new RangeQueryCriteria(startT.field(), startT
               .text(), endT.text(), ((RangeQuery) luceneQuery).isInclusive());
      } else if (luceneQuery instanceof BooleanQuery) {
         BooleanClause[] clauses = ((BooleanQuery) luceneQuery).getClauses();
         BooleanQueryCriteria bqc = new BooleanQueryCriteria();
         bqc.setOperator(BooleanQueryCriteria.AND);
         for (int i = 0; i < clauses.length; i++) {
            if (clauses[i].getOccur().equals(BooleanClause.Occur.SHOULD)) {
               bqc.setOperator(BooleanQueryCriteria.OR);
            }
            bqc.addTerm(generateCASQuery(clauses[i].getQuery()));
         }
         return bqc;
      } else {
         throw new Exception(
               "Error parsing query! Cannot determine clause type: ["
                     + luceneQuery.getClass().getName() + "] !");
      }
   }
}
