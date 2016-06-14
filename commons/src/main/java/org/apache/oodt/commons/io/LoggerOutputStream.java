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
package org.apache.oodt.commons.io;

//JDK imports
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link OutputStream} wrapper around a java {@link Logger}.
 * 
 * @author bfoster (Brian Foster)
 */
public class LoggerOutputStream extends OutputStream {

   public static final String NUM_BYTES_PER_WRITE_PROPERTY =
         "org.apache.oodt.commons.io.logger.os.bytes.per.write";
   public static final int VAL = 512;
   private static final int NUM_BYTES_PER_WRITE = Integer.getInteger(
         NUM_BYTES_PER_WRITE_PROPERTY, VAL);

   private Logger logger;
   private CharBuffer buffer;
   private Level logLevel;

   public LoggerOutputStream(Logger logger) throws InstantiationException {
      this(logger, Level.INFO);
   }

   public LoggerOutputStream(Logger logger, Level logLevel) {
      this(logger, NUM_BYTES_PER_WRITE, logLevel);
   }

   public LoggerOutputStream(Logger logger, int numOfBytesPerWrite) {
      this(logger, numOfBytesPerWrite, Level.INFO);
   }

   public LoggerOutputStream(Logger logger, int numOfBytesPerWrite,
         Level logLevel) {
      this.logger = logger;
      this.buffer = CharBuffer.wrap(new char[numOfBytesPerWrite]);
      this.logLevel = logLevel;
   }

   @Override
   public void write(int b) throws IOException {
      if (!buffer.hasRemaining()) {
         flush();
      }
      buffer.put((char) b);
   }

   @Override
   public void flush() {
      if (buffer.position() > 0) {
         char[] flushContext = new char[buffer.position()];
         System.arraycopy(buffer.array(), 0, flushContext, 0, buffer.position());
         logger.log(logLevel, new String(flushContext));
         buffer.clear();
      }
   }

   @Override
   public void close() throws IOException {
      flush();
      super.close();
   }
}
