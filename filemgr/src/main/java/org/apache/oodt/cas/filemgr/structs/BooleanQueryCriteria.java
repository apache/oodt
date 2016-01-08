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
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;

import java.util.List;
import java.util.Vector;

//OODT imports

/**
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Boolean Query Citeria that allows combination of a number of terms into a
 * query with a boolean operator (AND, OR, NOT). The NOT operator can only be
 * applied to one term.
 * </p>
 * 
 */

public class BooleanQueryCriteria extends QueryCriteria {

    /**
     * Constants
     */
    public static final int AND = 0;

    public static final int OR = 1;

    public static final int NOT = 2;

    private static final long serialVersionUID = 4718948237682772671L;

    private int operator;

    private List<QueryCriteria> terms;

    /**
     * Default constructor. Uses the AND operator.
     */
    public BooleanQueryCriteria() {
        operator = AND;
        terms = new Vector<QueryCriteria>();
    }

    /**
     * Boolean query constructor. This query is a boolean combination of term,
     * range, and other boolean queries. The supported operators are AND, OR and
     * NOT. Note that the NOT operator can only be used with one (1) term. This
     * method throws the QueryFormulationException if more than one term is used
     * with NOT.
     * 
     * @param terms
     *            The criteria onto which to apply the boolean operator
     * @param op
     *            The boolean operator to be applied
     */
    public BooleanQueryCriteria(List<QueryCriteria> terms, int op)
            throws QueryFormulationException {
        operator = op;
        if (op == NOT && terms.size() > 1) {
            throw new QueryFormulationException(
                    "BooleanQueryCriteria: NOT operator "
                            + "cannot be applied to multiple terms");
        } else {
            this.terms = terms;
        }
    }

    /**
     * Method to add a term to the boolean query. Note that a NOT operator can
     * only be applied to one term. Method throws the QueryFormulationException
     * if this rule is violated.
     * 
     * @param t
     *            Term to be added to the query
     */
    public void addTerm(QueryCriteria t) throws QueryFormulationException {
        if (operator == NOT && !terms.isEmpty()) {
            throw new QueryFormulationException(
                    "BooleanQueryCriteria: NOT operator "
                            + "cannot be applied to multiple terms");
        } else {
            terms.add(t);
        }
    }

    /**
     * Accessor method for the list of terms in the query.
     * 
     * @return The list of terms
     */
    public List<QueryCriteria> getTerms() {
        return terms;
    }

    /**
     * Mutator method for the boolean operator. Note that this method throws the
     * QueryFormulationException if the operator is set to NOT and multiple
     * terms are already defined.
     * 
     * @param op
     *            Boolean operator
     */
    public void setOperator(int op) throws QueryFormulationException {
        if (op == NOT && terms.size() > 1) {
            throw new QueryFormulationException(
                    "BooleanQueryCriteria: NOT operator "
                            + "cannot be applied to multiple terms");
        } else {
            operator = op;
        }
    }

    /**
     * Accessor method for the boolean operator.
     * 
     * @return the boolean operator
     */
    public int getOperator() {
        return operator;
    }

    /**
     * Method is not used in this class...
     */
    public String getElementName() {
        return null;
    }

    /**
     * Method is not used in this class...
     */
    public void setElementName(String elementName) {
    }

    /**
     * Method to convert the query to a string.
     * 
     * @return string equivement of the query
     */
    public String toString() {
        StringBuilder query = new StringBuilder();
        if (operator == AND) {
            query.append("AND(");
        } else if (operator == OR) {
            query.append("OR(");
        } else {
            query.append("NOT(");
        }

        for (int i = 0; i < terms.size(); i++) {
            query.append(terms.get(i).toString());
            if (i < (terms.size() - 1)) {
                query.append(", ");
            }
        }
        query.append(")");
        return query.toString();
    }
}
