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

package org.apache.oodt.commons.activity;

/**
   This class is intended to be thrown as an exception in the org.apache.oodt.commons.activity package.

   @author S. Hardman
   @version $Revision: 1.1 $
*/
public class ActivityException extends Exception {

   /**
      This constructor calls the {@link Exception} constructor with the same signature.
   */
   public ActivityException() {
      super();
   }


   /**
      This constructor calls the {@link Exception} constructor with the same signature.

      @param message The message associated with the exception.
   */
   public ActivityException(String message) {
      super(message);
   }


   /**
      This constructor calls the {@link Exception} constructor with the same signature.

      @param cause The exception that caused this exception.
   */
   public ActivityException(Throwable cause) {
      super(cause);
   }


   /**
      This constructor calls the {@link Exception} constructor with the same signature.

      @param message The message associated with the exception.
      @param cause The exception that caused this exception.
   */
   public ActivityException(String message, Throwable cause) {
      super(message, cause);
   }
}

