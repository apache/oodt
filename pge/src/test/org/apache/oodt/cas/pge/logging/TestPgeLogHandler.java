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
package org.apache.oodt.cas.pge.logging;

//JDK imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//Google imports
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link PgeLogHandler}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestPgeLogHandler extends TestCase {

   public void testSeparatesMultipleCasPgeLogWritesByPgeName() throws SecurityException, FileNotFoundException {
      final String instanceId1 = "1234";
      final String instanceId2 = "4321";
      
      final StringBuffer pge1LogMessages = new StringBuffer("");
      PgeLogHandler handler1 = new PgeLogHandler(instanceId1, new OutputStream() {
         @Override
         public void write(int character) throws IOException {
            pge1LogMessages.append((char) character);
         }
      });
      final StringBuffer pge2LogMessages = new StringBuffer("");
      PgeLogHandler handler2 = new PgeLogHandler(instanceId2, new OutputStream() {
         @Override
         public void write(int character) throws IOException {
            pge2LogMessages.append((char) character);
         }
      });

      Logger logger = Logger.getLogger(TestPgeLogHandler.class.getName());
      logger.addHandler(handler1);
      logger.addHandler(handler2);

      logger.log(new PgeLogRecord(instanceId1, Level.INFO, "pge1 message1"));
      logger.log(new PgeLogRecord(instanceId1, Level.INFO, "pge1 message2"));
      logger.log(new PgeLogRecord(instanceId1, Level.INFO, "pge1 message3"));

      logger.log(new PgeLogRecord(instanceId2, Level.INFO, "pge2 message1"));

      handler1.close();
      handler2.close();

      assertFalse(pge1LogMessages.toString().isEmpty());
      List<String> messages = Lists.newArrayList(Splitter.on("\n")
            .omitEmptyStrings().split(pge1LogMessages.toString()));
      assertEquals(6, messages.size());
      assertEquals("INFO: pge1 message1", messages.get(1));
      assertEquals("INFO: pge1 message2", messages.get(3));
      assertEquals("INFO: pge1 message3", messages.get(5));

      assertFalse(pge2LogMessages.toString().isEmpty());
      messages = Lists.newArrayList(Splitter.on("\n")
            .omitEmptyStrings().split(pge2LogMessages.toString()));
      assertEquals(2, messages.size());
      assertEquals("INFO: pge2 message1", messages.get(1));
   }
}
