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
package org.apache.oodt.cas.catalog.query;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A QueryExpression which groups other QueryExpressions
 * <p>
 */
public class QueryLogicalGroup extends QueryExpression {

	public enum Operator { AND, OR }
	
    protected Operator operator;

    protected List<QueryExpression> queryExpressions;

    public QueryLogicalGroup() {
    	this(new Vector<QueryExpression>(), Operator.AND);
    }
    
    public QueryLogicalGroup(List<QueryExpression> queryExpressions) {
    	this(queryExpressions, Operator.AND);
    }

    public QueryLogicalGroup(List<QueryExpression> queryExpressions, Operator operator) {
    	this.setExpressions(queryExpressions);
    	this.setOperator(operator);
    }
    
    public void setExpressions(List<QueryExpression> queryExpressions) {
        this.queryExpressions = new Vector<QueryExpression>(queryExpressions);
    }

    /**
     * 
     * @param queryCriteria
     */
    public void addExpression(QueryExpression queryExpression) {
    	this.queryExpressions.add(queryExpression);
    }
    
    /**
     * 
     * @param queryCriterias
     */
    public void addExpressions(List<QueryExpression> queryExpressions) {
    	this.queryExpressions.addAll(queryExpressions);
    }

    /**
     * 
     * @return
     */
    public List<QueryExpression> getExpressions() {
        return Collections.unmodifiableList(this.queryExpressions);
    }

    /**
     * 
     * @param operator
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * 
     * @return
     */
    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public String toString() {
        String query = "({" + this.bucketNames + "} " + this.operator + " : ";
        for (QueryExpression queryExpression : this.queryExpressions)
            query += queryExpression.toString() + ",";
        return query.substring(0, query.length() - 1) + ")";
    }
    
    public QueryLogicalGroup clone() {
    	QueryLogicalGroup qlGroup = new QueryLogicalGroup();
    	qlGroup.setBucketNames(this.getBucketNames());
    	qlGroup.setOperator(this.operator);
    	for (QueryExpression qe : this.queryExpressions)
    		qlGroup.addExpression(qe.clone());
    	return qlGroup;
    }
    
}