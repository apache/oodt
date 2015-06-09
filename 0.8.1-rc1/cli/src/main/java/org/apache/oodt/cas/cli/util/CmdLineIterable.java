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
package org.apache.oodt.cas.cli.util;

//JDK imports
import java.util.Iterator;
import java.util.List;

//Apache imports
import org.apache.commons.lang.Validate;

/**
 * An {@link Iterable} which allows multiple concurrent iterators which affect
 * each other, also allows you to increment iterators index manually. All
 * iterators handle termination safely. However, the catch being that you can
 * only iterate through this {@link Iterable} once, then you must create a new
 * object of it to iterate over it again.
 * 
 * @author bfoster (Brian Foster)
 */
public class CmdLineIterable<T> implements Iterable<T> {
   private int curIndex;
   private List<T> args;

   public CmdLineIterable(List<T> args) {
      Validate.notNull(args);

      curIndex = -1;
      this.args = args;
   }

   public List<T> getArgs() {
      return args;
   }

   public List<T> getArgsLeft() {
      return args.subList(curIndex, args.size());
   }

   public int getCurrentIndex() {
      return curIndex;
   }

   public void incrementIndex() {
      if (curIndex < args.size()) {
         curIndex++;
      }
   }

   public void descrementIndex() {
      if (curIndex > 0) {
         curIndex--;
      }
   }

   public T incrementAndGet() {
      incrementIndex();
      return getCurrentArg();
   }

   public T getAndIncrement() {
      T next = getCurrentArg();
      incrementIndex();
      return next;
   }

   public int numArgs() {
      return args.size();
   }

   public T getArg(int index) {
      return args.get(index);
   }

   public boolean hasNext() {
      return curIndex + 1 < args.size();
   }

   public T getCurrentArg() {
      if (curIndex == -1) {
         return incrementAndGet();
      } else if (curIndex > -1 && curIndex < args.size()) {
         return args.get(curIndex);
      } else {
         return null;
      }
   }

   public Iterator<T> iterator() {
      return new Iterator<T>() {

         public boolean hasNext() {
            return CmdLineIterable.this.hasNext();
         }

         public T next() {
            if (!hasNext()) {
               throw new IndexOutOfBoundsException((curIndex + 1) + "");
            }
            return incrementAndGet();
         }

         public void remove() {
            // do nothing
         }

      };
   }
}
