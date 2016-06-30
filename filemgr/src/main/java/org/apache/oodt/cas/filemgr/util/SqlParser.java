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

package org.apache.oodt.cas.filemgr.util;

//OODT imports

import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//JDK imports

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          A fairly robust SQL parser, based on the Shunting yard
 *          algorithm
 *          </p>
 *          
 *          <p>
 *          Evaluates SQL like string statements contained in a string. The SQL
 *          statement should be enclosed within: SQL ({@literal <sql_arguments>}
 *          ) { {@literal <sql_statement>} . the {@literal <sql_arguments>} can
 *          be either FORMAT, SORT_BY, or FILTER. Syntax: SQL (FORMAT='<metadata
 *          formated output>',SORT_BY='<metadata name>', FILTER='<start_time
 *          metadata element>, <end_time metadata element>, <priority metadata
 *          element>, <filter type>') { SELECT
 *          <list-of-comma-segregated-metadata-elements-to-query-on> FROM
 *          <productTypes-comma-segregated> WHERE <metadata-boolean-expressions>
 *          } Here is an example SQL statement: SQL (FORMAT='FileLocation/Filename',
 *          SORT_BY='FileSize',FILTER=<StartDateTime,EndDateTime,DataVersion,
 *          TakeHighestPriority) { SELECT FileLocation,Filename,FileSize FROM IASI_L1C 
 *          WHERE ProductionDateTime >= '2007-12-01T00:00:00.000000Z' } This example
 *          would query the cas-filemgr for metadata values:
 *          FileLocation,Filename,FileSize for any data file where the
 *          ProductType == IASI_L1C and the ProductionDateTime >=
 *          2007-12-01T00:00:00.000000Z. It would then combine the return data
 *          files metadata via the specified FORMAT. Each data file's metadata
 *          will be formated to a string representation of (with the actual
 *          values replaced in the location of the metadata keys):
 *          FileLocation/Filename. They will be concatenated together, in
 *          FileSize order.
 *          </p>
 */
public class SqlParser {

    private static Logger LOG = Logger.getLogger(SqlParser.class.getName());
    private SqlParser() {
    }
    
    public static ComplexQuery parseSqlQueryMethod(String sqlStringQueryMethod)
            throws QueryFormulationException {
        if (!Pattern.matches("((?:SQL)|(?:sql))\\s*(.*)\\s*\\{\\s*SELECT.*FROM.*(?:WHERE.*){0,1}\\}", sqlStringQueryMethod)) {
            throw new QueryFormulationException("Malformed SQL method");
        }
        
        try {
            ComplexQuery complexQuery = parseSqlQuery(stripOutSqlDefinition(sqlStringQueryMethod));
            
            for (Expression expr : getSqlStatementArgs(sqlStringQueryMethod)) {
                if (expr.getKey().toUpperCase().equals("FORMAT")) {
                    complexQuery.setToStringResultFormat(expr.getValue());
                } else if (expr.getKey().toUpperCase().equals("SORT_BY")) {
                    complexQuery.setSortByMetKey(expr.getValue());
                } else if (expr.getKey().toUpperCase().equals("FILTER")) {
                    complexQuery.setQueryFilter(createFilter(expr));
                }
            }
            
            return complexQuery;
        }catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new QueryFormulationException("Failed to parse SQL method : " + e.getMessage());
        }
    }
    
    public static ComplexQuery parseSqlQuery(String sqlStringQuery)
            throws QueryFormulationException {
        String[] splitSqlStatement = sqlStringQuery
                .split("((?:SELECT)|(?:FROM)|(?:WHERE))");
        String[] selectValues = (splitSqlStatement[1].trim() + ",").split(",");
        String[] fromValues = (splitSqlStatement[2].trim() + ",").split(",");
        ComplexQuery sq = new ComplexQuery();
        List<String> selectValuesList = Arrays.asList(selectValues);
        if (!selectValuesList.contains("*")) {
            sq.setReducedMetadata(Arrays.asList(selectValues));
        }
        List<String> fromValuesList = Arrays.asList(fromValues);
        if (!fromValuesList.contains("*")) {
            sq.setReducedProductTypeNames(fromValuesList);
        }
        
        if (splitSqlStatement.length > 3) {
            sq.addCriterion(parseStatement(toPostFix(splitSqlStatement[3]
                .trim())));
        }
        return sq;
    }
    
    public static QueryCriteria parseSqlWhereClause(String sqlWhereClause) 
            throws QueryFormulationException {
        return parseStatement(toPostFix(sqlWhereClause.trim()));
    }
    
    public static String unparseSqlQuery(ComplexQuery complexQuery) throws QueryFormulationException {
        LinkedList<String> outputArgs = new LinkedList<String>();
        if (complexQuery.getToStringResultFormat() != null) {
            outputArgs.add("FORMAT = '" + complexQuery.getToStringResultFormat() + "'");
        }
        if (complexQuery.getSortByMetKey() != null) {
            outputArgs.add("SORT_BY = '" + complexQuery.getSortByMetKey() + "'");
        }
        if (complexQuery.getQueryFilter() != null) {
            String filterString = "FILTER = '"
                    + complexQuery.getQueryFilter().getStartDateTimeMetKey() + ","
                    + complexQuery.getQueryFilter().getEndDateTimeMetKey() + ","
                    + complexQuery.getQueryFilter().getPriorityMetKey() + ","
                    + complexQuery.getQueryFilter().getFilterAlgor().getClass().getCanonicalName() + ","
                    + complexQuery.getQueryFilter().getFilterAlgor().getEpsilon();
            outputArgs.add(filterString + "'");
        }
        String sqlQueryString = getInfixCriteriaString(complexQuery.getCriteria());
        if (sqlQueryString != null && sqlQueryString.startsWith("(") && sqlQueryString.endsWith(")")) {
            sqlQueryString = sqlQueryString.substring(1, sqlQueryString.length() - 1);
        }
        return "SQL ("
                + listToString(outputArgs)
                + ") { SELECT " + listToString(complexQuery.getReducedMetadata())
                + " FROM " + (complexQuery.getReducedProductTypeNames() != null ? listToString(complexQuery.getReducedProductTypeNames()) : "*")
                + (sqlQueryString != null ? " WHERE " + sqlQueryString : "") + " }";
    }

    public static String getInfixCriteriaString(List<QueryCriteria> criteriaList) throws QueryFormulationException {
        if (criteriaList.size() > 1) {
            return getInfixCriteriaString(new BooleanQueryCriteria(criteriaList, BooleanQueryCriteria.AND));
        } else if (criteriaList.size() == 1) {
            return getInfixCriteriaString(criteriaList.get(0));
        } else {
            return null;
        }
    }
    
    public static String getInfixCriteriaString(QueryCriteria criteria) {
        StringBuilder returnString = new StringBuilder();
        if (criteria instanceof BooleanQueryCriteria) {
            BooleanQueryCriteria bqc = (BooleanQueryCriteria) criteria;
            List<QueryCriteria> terms = bqc.getTerms();
            switch(bqc.getOperator()){
            case 0:
                returnString.append("(").append(getInfixCriteriaString(terms.get(0)));
                for (int i = 1; i < terms.size(); i++) {
                    returnString.append(" AND ").append(getInfixCriteriaString(terms.get(i)));
                }
                returnString.append(")");
                break;
            case 1:
                returnString.append("(").append(getInfixCriteriaString(terms.get(0)));
                for (int i = 1; i < terms.size(); i++) {
                    returnString.append(" OR ").append(getInfixCriteriaString(terms.get(i)));
                }
                returnString.append(")");
                break;
            case 2:
                QueryCriteria qc = bqc.getTerms().get(0);
                if (qc instanceof TermQueryCriteria) {
                    TermQueryCriteria tqc = (TermQueryCriteria) qc;
                    returnString.append(tqc.getElementName()).append(" != '").append(tqc.getValue()).append("'");
                }else {
                    returnString.append("NOT(").append(getInfixCriteriaString(qc)).append(")");
                }
                break;
            }
        }else if (criteria instanceof RangeQueryCriteria) {
            RangeQueryCriteria rqc = (RangeQueryCriteria) criteria;
            String opString = rqc.getInclusive() ? "=" : "";
            if (rqc.getStartValue() != null) {
                opString = ">" + opString + " '" + rqc.getStartValue() + "'";
            }else {
                opString = "<" + opString + " '" + rqc.getEndValue() + "'";
            }
            returnString.append(rqc.getElementName()).append(" ").append(opString);
        }else if (criteria instanceof TermQueryCriteria) {
            TermQueryCriteria tqc = (TermQueryCriteria) criteria;
            returnString.append(tqc.getElementName()).append(" == '").append(tqc.getValue()).append("'");
        }
        return returnString.toString();
    }
    
    private static String stripOutSqlDefinition(String sqlStringQueryMethod) {
        return sqlStringQueryMethod.trim().replaceAll("((?:SQL)|(?:sql))\\s*(.*)\\s*\\{", "").replaceAll("}$", "").trim();
    }
    
    private static List<Expression> getSqlStatementArgs(String sqlStringQueryMethod) throws QueryFormulationException {
        boolean inExpr = false;
        int startArgs = 0;
        for (int i = 0; i < sqlStringQueryMethod.length(); i++) {
            char curChar = sqlStringQueryMethod.charAt(i);
            switch (curChar) {
            case '(':
                startArgs = i + 1;
                break;
            case ')':
                if (!inExpr) {
                    String[] args = sqlStringQueryMethod.substring(startArgs, i).trim().split("'\\s*,");
                    LinkedList<Expression> argsList = new LinkedList<Expression>();
                    for (String arg : args) {
                        argsList.add(new Expression((arg = arg.trim()).endsWith("'") ? arg : (arg + "'")));
                    }
                    return argsList;
                } else {
                    break;
                }
            case '\'':
                inExpr = !inExpr;
                break;
            }
        }
        throw new QueryFormulationException("Failed to read in args");
    }
    
    private static QueryFilter createFilter(Expression expr) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String[] filterArgs = expr.getValue().split(",");
        FilterAlgor filterAlgor = (FilterAlgor) Class.forName(filterArgs[3]).newInstance();
        QueryFilter qf = new QueryFilter(filterArgs[0], filterArgs[1], filterArgs[2], filterAlgor);
        filterAlgor.setEpsilon(Integer.parseInt(filterArgs[4]));
        return qf;
    }

    /**
     * Uses "Shunting yard algorithm" (see:
     * http://en.wikipedia.org/wiki/Shunting_yard_algorithm)
     */
    private static LinkedList<String> toPostFix(String statement) {
        LinkedList<String> postFix = new LinkedList<String>();
        Stack<String> stack = new Stack<String>();

        for (int i = 0; i < statement.length(); i++) {
            char curChar = statement.charAt(i);
            switch (curChar) {
            case '(':
                stack.push("(");
                break;
            case ')':
                String value;
                while (!(value = stack.pop()).equals("(")) {
                    postFix.add(value);
                }
                if (stack.peek().equals("NOT")) {
                    postFix.add(stack.pop());
                }
                break;
            case ' ':
                break;
            default:
                if (statement.substring(i, i + 3).equals("AND")) {
                    while (!stack.isEmpty()
                            && (stack.peek().equals("AND"))) {
                        postFix.add(stack.pop());
                    }
                    stack.push("AND");
                    i += 2;
                } else if (statement.substring(i, i + 2).equals("OR")) {
                    while (!stack.isEmpty()
                            && (stack.peek().equals("AND") || stack.peek()
                                    .equals("OR"))) {
                        postFix.add(stack.pop());
                    }
                    stack.push("OR");
                    i += 1;
                } else if (statement.substring(i, i + 3).equals("NOT")) {
                    stack.push("NOT");
                    i += 2;
                } else {
                    int endIndex = statement.indexOf('\'', statement.indexOf(
                            '\'', i) + 1) + 1;
                    postFix.add(statement.substring(i, endIndex));
                    i = endIndex - 1;
                }
            }
        }

        while (!stack.isEmpty()) {
            postFix.add(stack.pop());
        }

        return postFix;
    }

    private static QueryCriteria parseStatement(LinkedList<String> postFixStatement)
            throws QueryFormulationException {
        Stack<QueryCriteria> stack = new Stack<QueryCriteria>();
        for (String item : postFixStatement) {
            if (item.equals("AND")) {
                BooleanQueryCriteria bQC = new BooleanQueryCriteria();
                bQC.addTerm(stack.pop());
                bQC.addTerm(stack.pop());
                stack.push(bQC);
            } else if (item.equals("OR")) {
                BooleanQueryCriteria bQC = new BooleanQueryCriteria();
                bQC.setOperator(BooleanQueryCriteria.OR);
                bQC.addTerm(stack.pop());
                bQC.addTerm(stack.pop());
                stack.push(bQC);
            } else if (item.equals("NOT")) {
                BooleanQueryCriteria bQC = new BooleanQueryCriteria();
                bQC.setOperator(BooleanQueryCriteria.NOT);
                bQC.addTerm(stack.pop());
                stack.push(bQC);
            } else {
                stack.push(new Expression(item).convertToQueryCriteria());
            }
        }
        return stack.pop();
    }

    private static String listToString(List<String> list) {
        StringBuilder arrayString = new StringBuilder();
        if (list.size() > 0) {
            arrayString.append(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                arrayString.append(",").append(list.get(i));
            }
        }
        return arrayString.toString();
    }


    private static class Expression {

        public static final short GREATER_THAN = 12;

        public static final short LESS_THAN = 3;

        public static final short EQUAL_TO = 9;

        public static final short NOT_EQUAL_TO = 15;

        public static final short GREATER_THAN_OR_EQUAL_TO = 13;

        public static final short LESS_THAN_OR_EQUAL_TO = 11;

        public static final short NOT = 6;

        private String[] stringValues = new String[] { "`", "`", "`", "<", "`",
                "`", "!", "`", "`", "=", "`", "<=", ">", ">=", "`", "!=" };

        private String expression;

        private String key;

        private String val;

        private int op;

        public Expression(String expression) {
            this.parseExpression(this.expression = expression);
        }

        public Expression(String key, int op, String val) {
            this.key = key.trim();
            this.op = op;
            this.val = this.removeTickBounds(val.trim());
        }

        private void parseExpression(String expression) {
            Matcher matcher = Pattern.compile("((?:>=)|(?:<=)|(?:==)|(?:!=)|(?:=)|(?:>)|(?:<))").matcher(expression);
            matcher.find();
            this.key = expression.substring(0, matcher.start()).trim();
            this.val = this.removeTickBounds(expression.substring(matcher.end()).trim());
            String opString = matcher.group();
            for (char c : opString.toCharArray()) {
                this.op = this.op | this.getShortValueForOp(c);
            }
        }

        private String removeTickBounds(String value) {
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }

        private int getShortValueForOp(char op) {
            switch (op) {
            case '>':
                return GREATER_THAN;
            case '<':
                return LESS_THAN;
            case '=':
                return EQUAL_TO;
            case '!':
                return NOT;
            default:
                return 0;
            }
        }

        public QueryCriteria convertToQueryCriteria()
                throws QueryFormulationException {
            switch (this.op) {
            case GREATER_THAN:
                return new RangeQueryCriteria(this.key, this.val, null, false);
            case LESS_THAN:
                return new RangeQueryCriteria(this.key, null, this.val, false);
            case EQUAL_TO:
                return new TermQueryCriteria(this.key, this.val);
            case NOT_EQUAL_TO:
                BooleanQueryCriteria notEqBQC = new BooleanQueryCriteria();
                notEqBQC.setOperator(BooleanQueryCriteria.NOT);
                notEqBQC.addTerm(new TermQueryCriteria(this.key, this.val));
                return notEqBQC;
            case GREATER_THAN_OR_EQUAL_TO:
                return new RangeQueryCriteria(this.key, this.val, null, true);
            case LESS_THAN_OR_EQUAL_TO:
                return new RangeQueryCriteria(this.key, null, this.val, true);
            }
            throw new QueryFormulationException(
                    "Was not able to form query . . . probably an invalid operator -- "
                            + this.toString());
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.val;
        }

        public int getOp() {
            return this.op;
        }

        public String getExpression() {
            return this.expression;
        }

        public String toString() {
            return this.key + " " + this.stringValues[this.op] + " " + this.val;
        }

    }
    
    public static void main(String[] args) throws QueryFormulationException {
        String query = "SELECT * FROM IASI_L1C WHERE one == '1' AND two == '2' OR NOT(five == '5') OR three == '3' AND four == '4'";
        System.out.println("query: " + query);
        System.out.println("query after : " + unparseSqlQuery(parseSqlQuery(query)));
        query = "SELECT * FROM IASI_L1C";
        System.out.println("query: " + query);
        System.out.println("query after : " + unparseSqlQuery(parseSqlQuery(query)));
        query = "SELECT * FROM *";
        System.out.println("query: " + query);
        System.out.println("query after : " + unparseSqlQuery(parseSqlQuery(query)));
    }
}
