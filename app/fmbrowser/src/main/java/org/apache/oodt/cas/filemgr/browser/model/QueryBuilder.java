/**
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

package org.apache.oodt.cas.filemgr.browser.model;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.tools.CASAnalyzer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryBuilder {

  private CasDB database;
  private static Logger LOG = Logger.getLogger(QueryBuilder.class.getName());
  public QueryBuilder(CasDB db) {
    database = db;
  }

  public Query ParseQuery(String query) {
    // note that "__FREE__" is a control work for free text searching
    QueryParser parser = new QueryParser("__FREE__", new CASAnalyzer());

    org.apache.lucene.search.Query luceneQ = null;
    org.apache.oodt.cas.filemgr.structs.Query casQ = new org.apache.oodt.cas.filemgr.structs.Query();

    try {
      luceneQ = parser.parse(query);
    } catch (org.apache.lucene.queryparser.classic.ParseException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.SEVERE, e.getMessage());
    }

    System.out.println(luceneQ != null ? luceneQ.toString() : null);
    GenerateCASQuery(casQ, luceneQ);

    return casQ;
  }

  public void GenerateCASQuery(org.apache.oodt.cas.filemgr.structs.Query casQ,
      org.apache.lucene.search.Query luceneQ) {
    if (luceneQ instanceof TermQuery) {
      Term t = ((TermQuery) luceneQ).getTerm();
      if (!t.field().equals("__FREE__")) {
        String element = database.getElementID(t.field());
        if (!element.equals("") && !t.text().equals("")) {

          casQ.addCriterion(new TermQueryCriteria(element, t.text()));
        }
      }
    } else if (luceneQ instanceof PhraseQuery) {
      Term[] t = ((PhraseQuery) luceneQ).getTerms();
      if (!t[0].field().equals("__FREE__")) {
        for (Term aT : t) {
          String element = database.getElementID(aT.field());
          if (!element.equals("") && !aT.text().equals("")) {
            casQ.addCriterion(new TermQueryCriteria(element, aT.text()));
          }
        }
      }
    } else if (luceneQ instanceof TermRangeQuery) {
      BytesRef startT = ((TermRangeQuery) luceneQ).getLowerTerm();
      BytesRef endT = ((TermRangeQuery) luceneQ).getUpperTerm();
      String element = database.getElementID(((TermRangeQuery) luceneQ).getField());
      if (!element.equals("") && !startT.utf8ToString().equals("")
          && !endT.utf8ToString().equals("")) {
        casQ.addCriterion(new RangeQueryCriteria(element, startT.utf8ToString(), endT
            .utf8ToString()));
      }
    } else if (luceneQ instanceof BooleanQuery) {
      List<BooleanClause> clauses = ((BooleanQuery) luceneQ).clauses();
      for (BooleanClause clause : clauses) {
        GenerateCASQuery(casQ, (clause).getQuery());
      }
    } else {
      System.out.println("Error Parsing Query");
      System.exit(-1);
    }
  }

}
