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
package org.apache.oodt.commons.exec;

//OODT imports
import org.apache.oodt.commons.io.LoggerOutputStream;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for executing programs.
 * 
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public final class ExecUtils {
   private static Logger LOG = Logger.getLogger(ExecUtils.class.getName());
   private ExecUtils() throws InstantiationException {
      throw new InstantiationException("Don't construct utility classes!");
   }

   public static String printCommandLine(String[] args) {
      StringBuilder cmdLine = new StringBuilder();

      if (args != null && args.length > 0) {
         for (String arg : args) {
            cmdLine.append(arg);
            cmdLine.append(" ");
         }
      }

      return cmdLine.toString();
   }

   public static int callProgram(String commandLine, Logger logger)
         throws IOException {
      return callProgram(commandLine, logger, null);
   }

   public static int callProgram(String commandLine, OutputStream stdOutStream,
         OutputStream stdErrStream) throws IOException {
      return callProgram(commandLine, stdOutStream, stdErrStream, null);
   }

   public static int callProgram(String commandLine, Logger logger, File workDir)
         throws IOException {
      LoggerOutputStream loggerInfoStream = null;
      LoggerOutputStream loggerSevereStream = null;
      try {
         return callProgram(
               commandLine,
               loggerInfoStream = new LoggerOutputStream(logger, Level.INFO),
               loggerSevereStream = new LoggerOutputStream(logger, Level.SEVERE),
               workDir);
      } catch (Exception e) {
         throw new IOException(e);
      } finally {
         try {
            if (loggerInfoStream != null) {
               loggerInfoStream.close();
            }
         } catch (Exception ignored) {}
         try {
            if (loggerSevereStream != null) {
               loggerSevereStream.close();
            }
         } catch (Exception ignored) {}
      }
   }

   public static int callProgram(String commandLine, OutputStream stdOutStream,
         OutputStream stdErrStream, File workDir) throws IOException {
      Process progProcess = null;
      StreamGobbler errorGobbler = null, outputGobbler = null;
      int returnVal = -1;
      try {
         progProcess = (workDir == null) ? Runtime.getRuntime().exec(
               commandLine) : Runtime.getRuntime().exec(commandLine, null,
               workDir);
         errorGobbler = new StreamGobbler(progProcess.getErrorStream(),
               "ERROR", stdErrStream);
         outputGobbler = new StreamGobbler(progProcess.getInputStream(),
               "OUTPUT", stdOutStream);
         errorGobbler.start();
         outputGobbler.start();
         returnVal = progProcess.waitFor();
         return returnVal;
      } catch (Exception e) {
         LOG.log(Level.SEVERE, e.getMessage());
         throw new IOException("Failed to run '" + commandLine
               + "' -- return val = " + returnVal + " : " + e.getMessage());
      } finally {
         if (errorGobbler != null) {
            errorGobbler.stopGobblingAndDie();
         }
         if (outputGobbler != null) {
            outputGobbler.stopGobblingAndDie();
         }
         try {
            if (progProcess != null) {
               progProcess.getErrorStream().close();
            }
         } catch (Exception ignored) {}
         try {
            if (progProcess != null) {
               progProcess.getInputStream().close();
            }
         } catch (Exception ignored) {}
         try {
            if (progProcess != null) {
               progProcess.getOutputStream().close();
            }
         } catch (Exception ignored) {}
      }
   }

   public static int callProgram(String commandLine, File workDir)
         throws IOException {
      Process p = Runtime.getRuntime().exec(commandLine, null, workDir);
      return processProgram(p);
   }

   public static int callProgram(String[] args, File workDir)
         throws IOException {
      Process p = Runtime.getRuntime().exec(args, null, workDir);
      return processProgram(p);
   }

   private static int processProgram(Process p) {

      StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
            "ERROR", System.err);

      // any output?
      StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),
            "OUTPUT", System.out);

      errorGobbler.start();
      outputGobbler.start();
      int retVal = -1;
      try {
         retVal = p.waitFor();
      } catch (InterruptedException ignore) {
      } finally {
         // first stop the threads
         if (outputGobbler.isAlive()) {
            outputGobbler.stopGobblingAndDie();
         }

         if (errorGobbler.isAlive()) {
            errorGobbler.stopGobblingAndDie();
         }

         try { p.getErrorStream().close(); } catch (Exception ignore) {}
         try { p.getOutputStream().close(); } catch (Exception ignore) {}
         try { p.getInputStream().close(); } catch (Exception ignore) {}
      }
      return retVal;
   }
}
