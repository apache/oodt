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
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.conv.VersionConverter;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.structs.query.filter.TimeEvent;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link SqlQueryCliAction}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestSqlQueryCliAction extends TestCase {

   private static final String QUERY = "SELECT * FROM *";
   private static final String SORT_BY = "Filename";
   private static final String OUTPUT_FORMAT = "Filename = $Filename";
   private static final String DELIMITER = ",";
   private static final FilterAlgor FILTER_ALGOR = new MockFilterAlgor();
   private static final String START_DATE_TIME_MET_KEY = "StartDateTime";
   private static final String END_DATE_TIME_MET_KEY = "EndDateTime";
   private static final String PRIORITY_DATE_TIME_MET_KEY = "PriorityDateTime";
   private static final VersionConverter VERSION_CONV = new MockVersionConverter();

   private static final String TEST_FILENAME = "data.dat";

   private ComplexQuery clientSetComplexQuery;

   public void testValidateErrors() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockSqlQueryCliAction cliAction = new MockSqlQueryCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setQuery(QUERY);
      cliAction.execute(printer); // Should not throw exception.
      cliAction.setFilterAlgor(FILTER_ALGOR);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setStartDateTimeMetKey(START_DATE_TIME_MET_KEY);
      cliAction.setEndDateTimeMetKey(END_DATE_TIME_MET_KEY);
      cliAction.setPriorityMetKey(PRIORITY_DATE_TIME_MET_KEY);
      cliAction.execute(printer); // Should not throw exception.
      cliAction.setVersionConverter(VERSION_CONV);
      cliAction.execute(printer); // Should not throw exception.
   }

   public void testClientTransTrueAndFlatProduct() throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockSqlQueryCliAction cliAction = new MockSqlQueryCliAction();
      cliAction.setQuery(QUERY);
      cliAction.setSortBy(SORT_BY);
      cliAction.setOutputFormat(OUTPUT_FORMAT);
      cliAction.setDelimiter(DELIMITER);
      cliAction.setFilterAlgor(FILTER_ALGOR);
      cliAction.setStartDateTimeMetKey(START_DATE_TIME_MET_KEY);
      cliAction.setEndDateTimeMetKey(END_DATE_TIME_MET_KEY);
      cliAction.setPriorityMetKey(PRIORITY_DATE_TIME_MET_KEY);
      cliAction.setVersionConverter(VERSION_CONV);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("Filename = data.dat", printer.getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(SORT_BY, clientSetComplexQuery.getSortByMetKey());
      assertEquals(OUTPUT_FORMAT, clientSetComplexQuery.getToStringResultFormat());
      assertNull(clientSetComplexQuery.getReducedProductTypeNames());
      assertNull(clientSetComplexQuery.getReducedMetadata());
      assertEquals(FILTER_ALGOR, clientSetComplexQuery.getQueryFilter().getFilterAlgor());
      assertEquals(START_DATE_TIME_MET_KEY, clientSetComplexQuery.getQueryFilter().getStartDateTimeMetKey());
      assertEquals(END_DATE_TIME_MET_KEY, clientSetComplexQuery.getQueryFilter().getEndDateTimeMetKey());
      assertEquals(PRIORITY_DATE_TIME_MET_KEY, clientSetComplexQuery.getQueryFilter().getPriorityMetKey());
      assertEquals(VERSION_CONV, clientSetComplexQuery.getQueryFilter().getConverter());
   }

   public void testClientTransTrueAndFlatProductAndNoOutputFormat() throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockSqlQueryCliAction cliAction = new MockSqlQueryCliAction();
      cliAction.setQuery(QUERY);
      cliAction.setSortBy(SORT_BY);
      cliAction.setDelimiter(DELIMITER);
      cliAction.setFilterAlgor(FILTER_ALGOR);
      cliAction.setStartDateTimeMetKey(START_DATE_TIME_MET_KEY);
      cliAction.setEndDateTimeMetKey(END_DATE_TIME_MET_KEY);
      cliAction.setPriorityMetKey(PRIORITY_DATE_TIME_MET_KEY);
      cliAction.setVersionConverter(VERSION_CONV);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      String msg = printer.getPrintedMessages().get(0);
      assertTrue(msg.contains("data.dat"));
      assertTrue(msg.contains("Bob,Billy"));
      assertEquals(",", msg.replace("data.dat","").replace("Bob,Billy",""));
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(SORT_BY, clientSetComplexQuery.getSortByMetKey());
      assertNull(clientSetComplexQuery.getToStringResultFormat());
      assertNull(clientSetComplexQuery.getReducedProductTypeNames());
      assertNull(clientSetComplexQuery.getReducedMetadata());
      assertEquals(FILTER_ALGOR, clientSetComplexQuery.getQueryFilter().getFilterAlgor());
      assertEquals(START_DATE_TIME_MET_KEY, clientSetComplexQuery.getQueryFilter().getStartDateTimeMetKey());
      assertEquals(END_DATE_TIME_MET_KEY, clientSetComplexQuery.getQueryFilter().getEndDateTimeMetKey());
      assertEquals(PRIORITY_DATE_TIME_MET_KEY, clientSetComplexQuery.getQueryFilter().getPriorityMetKey());
      assertEquals(VERSION_CONV, clientSetComplexQuery.getQueryFilter().getConverter());
   }

   public class MockSqlQueryCliAction extends SqlQueryCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public List<QueryResult> complexQuery(ComplexQuery complexQuery) {
               clientSetComplexQuery = complexQuery;
               Product p = new Product();
               p.setProductId("TestProductId");
               Metadata m = new Metadata();
               m.addMetadata("Filename", TEST_FILENAME);
               m.addMetadata("Owners", Lists.newArrayList("Bob", "Billy"));
               QueryResult qr = new QueryResult(p, m);
               qr.setToStringFormat(complexQuery.getToStringResultFormat());
               return Lists.newArrayList(qr);
            }
         };
      }
   }

   public static class MockFilterAlgor extends FilterAlgor {
      @Override
      public List<TimeEvent> filterEvents(List<TimeEvent> events) {
         return events;
      }
   }

   public static class MockVersionConverter implements VersionConverter {
      @Override
      public double convertToPriority(String version) throws Exception {
         return 0;
      }
   }
}
