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
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.tools.CASAnalyzer;

//Google imports
import com.google.common.collect.Lists;

/**
 * A {@link CmdLineAction} which converts a Lucene-like query into a File
 * Manager {@link Query} and returns the results.
 * 
 * @author bfoster (Brian Foster)
 */
public class LuceneQueryCliAction extends AbstractQueryCliAction {

   private static final String FREE_TEXT_BLOCK = "__FREE__";

   private String query;
   private List<String> reducedProductTypes;
   private List<String> reducedMetadataKeys;

   public LuceneQueryCliAction() {
      super();
   }

   @Override
   public ComplexQuery getQuery() throws Exception {
      Validate.notNull(query, "Must specify query");

      ComplexQuery complexQuery = new ComplexQuery();
      complexQuery.setCriteria(Lists.newArrayList(generateCASQuery(parseQuery(query))));
      complexQuery.setReducedProductTypeNames(reducedProductTypes);
      complexQuery.setReducedMetadata(reducedMetadataKeys);
      return complexQuery;
   }

   public void setQuery(String query) {
      this.query = query;
   }

   public void setReducedProductTypes(List<String> reducedProductTypes) {
      this.reducedProductTypes = reducedProductTypes;
   }

   public void setReducedMetadataKeys(List<String> reducedMetadataKeys) {
      this.reducedMetadataKeys = reducedMetadataKeys;
   }

   private Query parseQuery(String query) throws ParseException {
      // note that "__FREE__" is a control work for free text searching
      return (Query) new QueryParser(FREE_TEXT_BLOCK, new CASAnalyzer())
            .parse(query);
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
