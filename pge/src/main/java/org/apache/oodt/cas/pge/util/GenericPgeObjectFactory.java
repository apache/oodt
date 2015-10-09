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
package org.apache.oodt.cas.pge.util;


//OODT imports
import org.apache.oodt.cas.pge.ConfigFilePropertyAdder;
import org.apache.oodt.cas.pge.PGETaskInstance;
import org.apache.oodt.cas.pge.config.PgeConfigBuilder;
import org.apache.oodt.cas.pge.staging.FileStager;
import org.apache.oodt.cas.pge.writers.SciPgeConfigFileWriter;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Factory for creating {@link Object}s.
 *
 * @author bfoster (Brian Foster)
 */
public class GenericPgeObjectFactory {

   private GenericPgeObjectFactory() {}

   public static PGETaskInstance createPGETaskInstance(
         String clazz, Logger logger) {
      try {
         return (PGETaskInstance) Class.forName(clazz).newInstance();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Failed to create PGETaskInstance ["
               + clazz + "] : " + e.getMessage(), e);
         return null;
      }
   }

   public static PgeConfigBuilder createPgeConfigBuilder(
         String clazz, Logger logger) {
      try {
         return (PgeConfigBuilder) Class.forName(clazz).newInstance();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Failed to create PgeConfigBuilder ["
               + clazz + "] : " + e.getMessage(), e);
         return null;
      }
   }

   public static ConfigFilePropertyAdder createConfigFilePropertyAdder(
         String clazz, Logger logger) {
      try {
         return (ConfigFilePropertyAdder) Class.forName(clazz).newInstance();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Failed to create ConfigFilePropertyAdder ["
               + clazz + "] : " + e.getMessage(), e);
         return null;
      }
   }

   public static FileStager createFileStager(
         String clazz, Logger logger) {
      try {
         return (FileStager) Class.forName(clazz).newInstance();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Failed to create FileStager ["
               + clazz + "] : " + e.getMessage(), e);
         return null;
      }
   }

   public static SciPgeConfigFileWriter createSciPgeConfigFileWriter(
         String clazz, Logger logger) {
      try {
         return (SciPgeConfigFileWriter) Class.forName(clazz).newInstance();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "Failed to create SciPgeConfigFileWriter ["
               + clazz + "] : " + e.getMessage(), e);
         return null;
      }
   }
}
