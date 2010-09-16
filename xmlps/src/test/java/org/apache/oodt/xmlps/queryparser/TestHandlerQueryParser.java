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

//APACHE imports
import org.apache.oodt.xmlps.queryparser.Expression;
import org.apache.oodt.xmlps.queryparser.HandlerQueryParser;
import org.apache.oodt.xmlps.util.XMLQueryHelper;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.XMLQuery;

//JDK imports
import java.util.Stack;

//Junit imports
import junit.framework.TestCase;

/**
 * Tests the XMLPS query handler parser.
 */
public class TestHandlerQueryParser extends TestCase {

  public TestHandlerQueryParser() {
  }

  public void testParseQuery() {
    String queryStr = "A = B AND C = D";
    String expected = "(C = D AND A = B)";

    XMLQuery query = XMLQueryHelper.getDefaultQueryFromQueryString(queryStr);
    assertNotNull(query);
    Stack<QueryElement> queryStack = HandlerQueryParser.createQueryStack(query
        .getWhereElementSet());
    assertNotNull(queryStack);
    Expression parsedQuery = HandlerQueryParser.parse(queryStack);
    assertNotNull(parsedQuery);
    assertEquals(expected, parsedQuery.evaluate());
  }

}
