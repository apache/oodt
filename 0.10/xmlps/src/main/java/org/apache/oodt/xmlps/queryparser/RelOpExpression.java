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

package org.apache.oodt.xmlps.queryparser;

/**
 * 
 * A relational operator.
 */
public class RelOpExpression implements Expression {

    private String relop;

    private Expression literal;

    private String lhs;

    public RelOpExpression(String relop, String lhs, Expression literal) {
        this.relop = relop;
        this.lhs = lhs;
        this.literal = literal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.xmlps.queryparser.Expression#evaluate()
     */
    public String evaluate() {
        return lhs + " " + relop + " " + literal.evaluate();
    }

    /**
     * @return the lhs
     */
    public String getLhs() {
        return lhs;
    }

    /**
     * @param lhs the lhs to set
     */
    public void setLhs(String lhs) {
        this.lhs = lhs;
    }

    /**
     * @return the literal
     */
    public Expression getLiteral() {
        return literal;
    }

    /**
     * @param literal the literal to set
     */
    public void setLiteral(Expression literal) {
        this.literal = literal;
    }

}
