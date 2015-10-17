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

//JDK imports
import org.apache.oodt.xmlps.mapping.Mapping;
import org.apache.oodt.xmlps.mapping.MappingField;
import org.apache.oodt.xmlquery.QueryElement;

import java.util.List;
import java.util.Stack;

//OODT imports

/**
 * 
 * Parsers the {@link org.apache.oodt.xmlquery.XMLQuery} and its @link XMLQuery#getWhereElementSet()}
 * into an {@link Expression} tree.
 */
public class HandlerQueryParser implements ParseConstants {

  /**
   * Calls {@link #parse(Stack, Mapping)} with a null mapping.
   * 
   * @param queryStack The {@link org.apache.oodt.xmlquery.XMLQuery#getWhereElementSet()}.
   * @return The parsed {@link Expression} tree.
   */
  public static Expression parse(Stack<QueryElement> queryStack) {
    return parse(queryStack, null);
  }

  /**
   * 
   * Parses the {@link org.apache.oodt.xmlquery.XMLQuery#getWhereElementSet()} using the provided
   * <param>map</param>.
   * 
   * @param queryStack The {@link org.apache.oodt.xmlquery.XMLQuery#getWhereElementSet()}
   * @param map The provided ontological mapping.
   * @return The parsed {@link Expression} tree.
   */
  public static Expression parse(Stack<QueryElement> queryStack, Mapping map) {

    QueryElement qe = null;

    if (!queryStack.empty()) {
      qe = (QueryElement) queryStack.pop();
    } else
      return null;

    if (qe.getRole().equalsIgnoreCase(XMLQUERY_LOGOP)) {

      String logOpType = qe.getValue();
      if (logOpType.equalsIgnoreCase(XMLQUERY_AND)) {
        return new AndExpression(parse(queryStack, map), parse(queryStack, map));
      } else if (logOpType.equalsIgnoreCase(XMLQUERY_OR)) {
        return new OrExpression(parse(queryStack, map), parse(queryStack, map));
      } else
        return null;

    } else if (qe.getRole().equalsIgnoreCase(XMLQUERY_RELOP)) {
      String relOpType = qe.getValue();
      QueryElement rhsQE = (QueryElement) queryStack.pop();
      QueryElement lhsQE = (QueryElement) queryStack.pop();

      String rhsVal = (String) rhsQE.getValue();
      String lhsVal = (String) lhsQE.getValue();

      if (map != null) {
        // convert the right hand side, using
        // the local name
        MappingField fld = map.getFieldByLocalName(lhsVal);
        if (fld != null) {
          if (fld.isString()) {
            rhsVal = "'" + rhsVal + "'";
          }
        }
      }

      if (relOpType.equalsIgnoreCase(XMLQUERY_EQUAL)) {
        return new EqualsExpression(lhsVal, new Literal(rhsVal));
      } else if (relOpType.equalsIgnoreCase(XMLQUERY_LIKE)) {
        return new ContainsExpression(lhsVal, new WildcardLiteral(rhsVal));
      } else if (relOpType.equalsIgnoreCase(XMLQUERY_GREATER_THAN)) {
        return new GreaterThanExpression(lhsVal, new Literal(rhsVal));
      } else if (relOpType.equalsIgnoreCase(XMLQUERY_GREATER_THAN_OR_EQUAL_TO)) {
        return new GreaterThanEqualsExpression(lhsVal, new Literal(rhsVal));
      } else if (relOpType.equalsIgnoreCase(XMLQUERY_LESS_THAN)) {
        return new LessThanExpression(lhsVal, new Literal(rhsVal));
      } else if (relOpType.equalsIgnoreCase(XMLQUERY_LESS_THAN_OR_EQUAL_TO)) {
        return new LessThanEqualsExpression(lhsVal, new Literal(rhsVal));
      } else
        return null;

    } else if (qe.getRole().equalsIgnoreCase(XMLQUERY_LITERAL)) {
      return new Literal(qe.getValue());
    } else
      return null;

  }

  public static Stack<QueryElement> createQueryStack(List<QueryElement> l) {

    Stack<QueryElement> ret = new Stack<QueryElement>();

    for (int i = 0; i < l.size(); i++) {
      ret.push(l.get(i));
    }

    return ret;

  }

}
