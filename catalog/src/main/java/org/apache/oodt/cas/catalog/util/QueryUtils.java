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

package org.apache.oodt.cas.catalog.util;

//JDK imports
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.QueryLogicalGroup;
import org.apache.oodt.cas.catalog.query.WrapperQueryExpression;
import org.apache.oodt.cas.catalog.query.filter.QueryFilter;
import org.apache.oodt.cas.catalog.query.parser.ParseException;
import org.apache.oodt.cas.catalog.query.parser.QueryParser;
import org.apache.oodt.cas.catalog.query.parser.TokenMgrError;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Utilities for helping filter query results
 * <p>
 */
public class QueryUtils {

	public static List<TransactionalMetadata> filterMetadata(QueryFilter<?> queryFilter, List<TransactionalMetadata> metadataList) {
		return queryFilter.filterMetadataList(metadataList);
	}
	
	public static List<TransactionalMetadata> sort(List<TransactionalMetadata> metadataList, String sortByMetadataKey) {
		return null;
	}
	
	public static QueryExpression simplifyQuery(QueryExpression queryExpression) {
		return _simplifyQuery(queryExpression.clone());
	}
	
	/**
	 * Might later be factored out into a Query Normalizer Interface . . . this method currently compacts AND(s).
	 * @param queryExpression Query to be simplified
	 * @return simplified query
	 */
	private static QueryExpression _simplifyQuery(QueryExpression queryExpression) {
		if (queryExpression instanceof QueryLogicalGroup) {
			QueryLogicalGroup.Operator operator = ((QueryLogicalGroup) queryExpression).getOperator();
			boolean changed;
			do {
				changed = false;
				Vector<QueryExpression> children = new Vector<QueryExpression>();
				for (QueryExpression qe : ((QueryLogicalGroup) queryExpression).getExpressions()) {
					if (qe instanceof QueryLogicalGroup && ((QueryLogicalGroup) qe).getOperator().equals(operator)) {
						children.addAll(((QueryLogicalGroup) qe).getExpressions());
						changed = true;
					}else {
						children.add(qe);
					}
				}
				((QueryLogicalGroup) queryExpression).setExpressions(children);
			}while(changed);
		}else if (queryExpression instanceof WrapperQueryExpression) {
			((WrapperQueryExpression) queryExpression).setQueryExpression(((WrapperQueryExpression) queryExpression).getQueryExpression());
		}
		return queryExpression;
	}
	
	public static void main(String[] args) throws ParseException, TokenMgrError {
		QueryExpression qe = QueryParser.parseQueryExpression("{bucketNames = 'joe,tim' ; Name == 'Tim,Joe' AND City == 'Upland' AND State == 'CA'}");
		System.out.println(qe.toString());
		System.out.println(simplifyQuery(qe).toString());
		System.out.println("");
		qe = QueryParser.parseQueryExpression("Name == 'Tim,Joe' AND (City == 'Upland' AND State == 'CA')");
		System.out.println(qe.toString());
		System.out.println(simplifyQuery(qe).toString());
	}
	
//	public static QueryExpression convertAndsToOrs(QueryExpression queryExpression) {
//		if (queryExpression instanceof QueryLogicalGroup) {
//			if (((QueryLogicalGroup) queryExpression).getOperator().equals(QueryLogicalGroup.Operator.AND)) {
//				QueryLogicalGroup convertedQueryExpression = new QueryLogicalGroup();
//				convertedQueryExpression.setOperator(QueryLogicalGroup.Operator.OR);
//				for (QueryExpression subQueryExpression : ((QueryLogicalGroup) queryExpression).getExpressions())
//					convertedQueryExpression.addExpression(new NotQueryExpression(convertAndsToOrs(subQueryExpression)));
//				return new NotQueryExpression(convertedQueryExpression);
//			}else {
//				QueryLogicalGroup convertedQueryExpression = new QueryLogicalGroup();
//				convertedQueryExpression.setOperator(QueryLogicalGroup.Operator.OR);
//				for (QueryExpression subQueryExpression : ((QueryLogicalGroup) queryExpression).getExpressions()) 
//					convertedQueryExpression.addExpression(convertAndsToOrs(subQueryExpression));
//				return convertedQueryExpression;
//			}
//		}else if (queryExpression instanceof NotQueryExpression) {
//			return new NotQueryExpression(convertAndsToOrs(((NotQueryExpression) queryExpression).getQueryExpression()));
//		}else {
//			return queryExpression;
//		}
//	}
//	
//	public static QueryExpression reduceNots(QueryExpression queryExpression) {
//		if (queryExpression instanceof NotQueryExpression) {
//			NotQueryExpression notQueryExpression = (NotQueryExpression) queryExpression;
//			if (notQueryExpression.getQueryExpression() instanceof QueryLogicalGroup && ((QueryLogicalGroup) notQueryExpression.getQueryExpression()).getOperator().equals(QueryLogicalGroup.Operator.OR)) {
//				QueryLogicalGroup queryLogicalGroup = (QueryLogicalGroup) notQueryExpression.getQueryExpression();
//				QueryLogicalGroup newQueryLogicalGroup = new QueryLogicalGroup();
//				newQueryLogicalGroup.setOperator(QueryLogicalGroup.Operator.AND);
//				for (QueryExpression subQueryExpression : queryLogicalGroup.getExpressions()) {
//					if (subQueryExpression instanceof NotQueryExpression) {
//						newQueryLogicalGroup.addExpression(reduceNots(((NotQueryExpression) subQueryExpression).getQueryExpression()));
//					}else {
//						newQueryLogicalGroup.addExpression(new NotQueryExpression(reduceNots(subQueryExpression)));
//					}
//				}
//				return newQueryLogicalGroup;
//			}else {
//				return new NotQueryExpression(reduceNots(notQueryExpression.getQueryExpression()));
//			}
//		}else if (queryExpression instanceof QueryLogicalGroup){
//			QueryLogicalGroup queryLogicalGroup = (QueryLogicalGroup) queryExpression;
//			QueryLogicalGroup newQueryLogicalGroup = new QueryLogicalGroup();
//			newQueryLogicalGroup.setOperator(queryLogicalGroup.getOperator());
//			for (QueryExpression subQueryExpression : queryLogicalGroup.getExpressions())
//				newQueryLogicalGroup.addExpression(reduceNots(subQueryExpression));
//			return newQueryLogicalGroup;
//		}else {
//			return queryExpression;
//		}
//	}
	
}
