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
package org.apache.oodt.cas.cl.util;

//JDK imports
import java.util.Arrays;
import java.util.Iterator;

//Apache imports
import org.apache.commons.lang.Validate;

/**
 * Wrapper class around command line arguments that allows multiple iterators
 * which affect each other, also allows you to increment iterators index
 * manually. all iterators handle termination safely.
 * 
 * @author bfoster (Brian Foster)
 */
public class Args implements Iterable<String> {
   private int curIndex;
   private String[] args;

   public Args(String[] args) {
      Validate.notNull(args);

      curIndex = 0;
      this.args = args;
   }

   public String[] getArgs() {
      return args;
   }

   public String[] getArgsLeft() {
      return Arrays.copyOfRange(args, curIndex, args.length);
   }

   public int getCurrentIndex() {
      return curIndex;
   }

   public void incrementIndex() {
      curIndex++;
   }

   public void descrementIndex() {
      curIndex--;
   }

   public String incrementAndGet() {
      incrementIndex();
      return getCurrentArg();
   }

   public String getAndIncrement() {
      String next = getCurrentArg();
      incrementIndex();
      return next;
   }

   public int numArgs() {
      return args.length;
   }

   public String getArg(int index) {
      return args[index];
   }

   public boolean hasNext() {
      return curIndex < args.length;
   }

   public String getCurrentArg() {
      if (hasNext()) {
         return args[curIndex];
      } else {
         return null;
      }
   }

   public Iterator<String> iterator() {
      return new Iterator<String>() {

         public boolean hasNext() {
            return Args.this.hasNext();
         }

         public String next() {
            if (!hasNext()) {
               throw new IndexOutOfBoundsException(curIndex + "");
            }
            return getAndIncrement();
         }

         public void remove() {
            // do nothing
         }

      };
   }
}
