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
package org.apache.oodt.cas.resource.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.spark.api.java.function.Function;

/**
 * @author starchmd
 *
 * Tests if a line is a palindrome.
 *
 */
public class PalindromeUtils {
    /**
     * Super simple palindrome test.
     * @param line - line to test
     * @return true if it is a palindrome, false otherwise.
     */
    public static boolean isPalindrome(String line) {
        line = line.replaceAll("\\s","").toLowerCase();
        return line.equals(new StringBuilder(line).reverse().toString());
    }
    /**
     * Get a PrintStream for printing to give file
     * @param file - file to open as PrintStream
     * @return stream for file to print to.
     * @throws FileNotFoundException
     */
    public static PrintStream getPrintStream(String file) throws FileNotFoundException {
        return new PrintStream(new File(file));
    }
    /**
     * Functor class for spark.  Really should do this in Scala....
     * Note: serial id is disabled because this class has no instance variables.
     * @author starchmd
     */
    @SuppressWarnings("serial")
    static class FilterPalindrome implements Function<String, Boolean> {
        public Boolean call(String s) { return isPalindrome(s); }
      }
}
