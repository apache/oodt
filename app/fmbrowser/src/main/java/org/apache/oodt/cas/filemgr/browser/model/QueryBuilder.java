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


package org.apache.oodt.cas.filemgr.browser.model;

import org.apache.lucene.analysis.standard.ParseException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.tools.CASAnalyzer;

public class QueryBuilder{
	
	private CasDB database;
	
	public QueryBuilder(CasDB db){
		database = db;
	}
	
	public Query ParseQuery(String query){
		//note that "__FREE__" is a control work for free text searching
		QueryParser parser = new QueryParser("__FREE__", new CASAnalyzer());
		
		org.apache.lucene.search.Query luceneQ = null;
		org.apache.oodt.cas.filemgr.structs.Query casQ = 
			new org.apache.oodt.cas.filemgr.structs.Query();
		
		
		try {
			luceneQ = parser.parse(query);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(luceneQ.toString());
		GenerateCASQuery(casQ,luceneQ);
		
		return casQ;	
	}
	
	public void GenerateCASQuery(org.apache.oodt.cas.filemgr.structs.Query casQ, org.apache.lucene.search.Query luceneQ){
		if (luceneQ instanceof TermQuery){
			Term t = ((TermQuery)luceneQ).getTerm();
			if (t.field().equals("__FREE__")){
				//if(casQuery.getCriteria().isEmpty()) casQuery.addCriterion(new FreeTextQueryCriteria());
				//((FreeTextQueryCriteria)casQuery.getCriteria().get(0)).addValue(t.text());
			} else {
				String element = database.getElementID(t.field());
				if(!element.equals("")&&!t.text().equals("")){
					
					casQ.addCriterion(new TermQueryCriteria(element, t.text()));
				}
			}
		} else if (luceneQ instanceof PhraseQuery){
			Term[] t = ((PhraseQuery)luceneQ).getTerms();
			if(t[0].field().equals("__FREE__")){
				//if(casQuery.getCriteria().isEmpty()) casQuery.addCriterion(new FreeTextQueryCriteria());
				//for(int i=0;i<t.length;i++)
				//	((FreeTextQueryCriteria)casQuery.getCriteria().get(0)).addValue(t[i].text());				
			} else {
				for(int i=0;i<t.length;i++){
					String element = database.getElementID(t[i].field());
					if(!element.equals("")&&!t[i].text().equals("")){
						casQ.addCriterion(new TermQueryCriteria(element, t[i].text()));
					}
				}
			}	
		} else if (luceneQ instanceof RangeQuery){
			Term startT = ((RangeQuery)luceneQ).getLowerTerm();
			Term endT = ((RangeQuery)luceneQ).getUpperTerm();
			String element = database.getElementID(startT.field());
			if(!element.equals("")&&!startT.text().equals("")&&!endT.text().equals("")){
				casQ.addCriterion(new RangeQueryCriteria(element, startT.text(), endT.text()));
			}
		} else if (luceneQ instanceof BooleanQuery){
			BooleanClause[] clauses = ((BooleanQuery)luceneQ).getClauses();
			for(int i=0;i<clauses.length;i++){
				GenerateCASQuery(casQ, (clauses[i]).getQuery());
			}
		} else {
			System.out.println("Error Parsing Query");
			System.exit(-1);
		}
	}
	
	
}
