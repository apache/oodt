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
package org.apache.oodt.cas.filemgr.cli.action;

//JDK imports
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link LuceneQueryCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestLuceneQueryCliAction extends TestCase {

   private static final String TEST_FILENAME = "data.dat";

   private ComplexQuery clientSetQuery;

   public void testValidation() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockLuceneQueryCliAction cliAction = new MockLuceneQueryCliAction();
      cliAction.setQuery("ProductId=TestProductId");
      try {
         cliAction.execute(printer);
         fail("Should have throw exception");
      } catch (Exception ignore) {
      }
      cliAction.setQuery("");
      try {
         cliAction.execute(printer);
         fail("Should have throw exception");
      } catch (Exception ignore) {
      }
   }

   public void testClientTransTrueAndFlatProduct()
         throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockLuceneQueryCliAction cliAction = new MockLuceneQueryCliAction();

      cliAction.setQuery("ProductId:TestProductId");
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      assertEquals("ProductId", ((TermQueryCriteria) clientSetQuery
            .getCriteria().get(0)).getElementName());
      assertEquals("TestProductId", ((TermQueryCriteria) clientSetQuery
            .getCriteria().get(0)).getValue());

      cliAction.setQuery("ProductId:TestProductId ProductName:TestProductName");
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      BooleanQueryCriteria bqc = (BooleanQueryCriteria) clientSetQuery
            .getCriteria().get(0);
      assertEquals(2, bqc.getTerms().size());
      assertEquals(BooleanQueryCriteria.OR, bqc.getOperator());
      assertEquals("ProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getValue());
      assertEquals("ProductName",
            ((TermQueryCriteria) bqc.getTerms().get(1)).getElementName());
      assertEquals("TestProductName", ((TermQueryCriteria) bqc.getTerms()
            .get(1)).getValue());

      cliAction
            .setQuery("ProductId:TestProductId NominalDate:[20020101 TO 20030101]");
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      bqc = (BooleanQueryCriteria) clientSetQuery.getCriteria().get(0);
      assertEquals(2, bqc.getTerms().size());
      assertEquals(BooleanQueryCriteria.OR, bqc.getOperator());
      assertEquals("ProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getValue());
      assertEquals("NominalDate",
            ((RangeQueryCriteria) bqc.getTerms().get(1)).getElementName());
      assertEquals("20020101",
            ((RangeQueryCriteria) bqc.getTerms().get(1)).getStartValue());
      assertEquals("20030101",
            ((RangeQueryCriteria) bqc.getTerms().get(1)).getEndValue());
      assertTrue(((RangeQueryCriteria) bqc.getTerms().get(1)).getInclusive());

      cliAction
            .setQuery("ProductId:TestProductId NominalDate:{20020101 TO 20030101}");
      cliAction.execute(printer);
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      bqc = (BooleanQueryCriteria) clientSetQuery.getCriteria().get(0);
      assertEquals(2, bqc.getTerms().size());
      assertEquals(BooleanQueryCriteria.OR, bqc.getOperator());
      assertEquals("ProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getValue());
      assertEquals("NominalDate",
            ((RangeQueryCriteria) bqc.getTerms().get(1)).getElementName());
      assertEquals("20020101",
            ((RangeQueryCriteria) bqc.getTerms().get(1)).getStartValue());
      assertEquals("20030101",
            ((RangeQueryCriteria) bqc.getTerms().get(1)).getEndValue());
      assertFalse(((RangeQueryCriteria) bqc.getTerms().get(1)).getInclusive());

      cliAction
            .setQuery("ProductId:TestProductId AND ProductName:TestProductName");
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      bqc = (BooleanQueryCriteria) clientSetQuery.getCriteria().get(0);
      assertEquals(2, bqc.getTerms().size());
      assertEquals(BooleanQueryCriteria.AND, bqc.getOperator());
      assertEquals("ProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getValue());
      assertEquals("ProductName",
            ((TermQueryCriteria) bqc.getTerms().get(1)).getElementName());
      assertEquals("TestProductName", ((TermQueryCriteria) bqc.getTerms()
            .get(1)).getValue());

      cliAction
            .setQuery("ProductId:TestProductId OR ProductName:TestProductName");
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      bqc = (BooleanQueryCriteria) clientSetQuery.getCriteria().get(0);
      assertEquals(2, bqc.getTerms().size());
      assertEquals(BooleanQueryCriteria.OR, bqc.getOperator());
      assertEquals("ProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) bqc.getTerms().get(0)).getValue());
      assertEquals("ProductName",
            ((TermQueryCriteria) bqc.getTerms().get(1)).getElementName());
      assertEquals("TestProductName", ((TermQueryCriteria) bqc.getTerms()
            .get(1)).getValue());

      cliAction
            .setQuery("(ProductId:TestProductId OR ProductName:TestProductName) AND NominalDate:20110120");
      cliAction.execute(printer);
      assertEquals(1, clientSetQuery.getCriteria().size());
      bqc = (BooleanQueryCriteria) clientSetQuery.getCriteria().get(0);
      assertEquals(2, bqc.getTerms().size());
      assertEquals(BooleanQueryCriteria.AND, bqc.getOperator());
      BooleanQueryCriteria subBqc = (BooleanQueryCriteria) bqc.getTerms()
            .get(0);
      assertEquals("ProductId",
            ((TermQueryCriteria) subBqc.getTerms().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) subBqc.getTerms().get(0)).getValue());
      assertEquals("ProductName",
            ((TermQueryCriteria) subBqc.getTerms().get(1)).getElementName());
      assertEquals("TestProductName", ((TermQueryCriteria) subBqc.getTerms()
            .get(1)).getValue());
      assertEquals("NominalDate",
            ((TermQueryCriteria) bqc.getTerms().get(1)).getElementName());
      assertEquals("20110120",
            ((TermQueryCriteria) bqc.getTerms().get(1)).getValue());
   }

   public class MockLuceneQueryCliAction extends LuceneQueryCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public List<QueryResult> complexQuery(ComplexQuery complexQuery) {
               clientSetQuery = complexQuery;
               Product p = new Product();
               p.setProductId("TestProductId");
               Metadata m = new Metadata();
               m.addMetadata("Filename", TEST_FILENAME);
               QueryResult qr = new QueryResult(p, m);
               qr.setToStringFormat(complexQuery.getToStringResultFormat());
               return Lists.newArrayList(qr);
            }
         };
      }
   }
}
