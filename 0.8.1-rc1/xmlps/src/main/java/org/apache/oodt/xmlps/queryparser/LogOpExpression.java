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
 * A logical expression with a left-hand side 
 * and right hind-side {@link Expression}.
 * 
 */
public class LogOpExpression implements Expression {

    private String logop;

    private Expression lhs;

    private Expression rhs;
    
    

    public LogOpExpression(String logop, Expression lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.logop = logop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.xmlps.queryparser.Expression#evaluate()
     */
    public String evaluate() {
        return "(" + lhs.evaluate() + " " + logop + " " + rhs.evaluate() + ")";
    }

    /**
     * @return the lhs
     */
    public Expression getLhs() {
        return lhs;
    }

    /**
     * @param lhs the lhs to set
     */
    public void setLhs(Expression lhs) {
        this.lhs = lhs;
    }

    /**
     * @return the rhs
     */
    public Expression getRhs() {
        return rhs;
    }

    /**
     * @param rhs the rhs to set
     */
    public void setRhs(Expression rhs) {
        this.rhs = rhs;
    }

}
