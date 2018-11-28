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

//JDK imports
import java.io.FileNotFoundException;
import java.io.PrintStream;
//OODT imports
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.NameValueJobInput;
import org.apache.oodt.cas.resource.structs.SparkInstance;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 *
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * A job that searches the supplied file for palindromes.  Outputs timing information
 * to another file for benchmarking purposes. References standard palindrom calculation.
 *
 * Uses spark processing for the computations.
 * </p>
 */
public class SparkFilePalindromeExample implements SparkInstance {

    JavaSparkContext sc;
    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.cas.resource.structs.JobInstance#execute(org.apache.oodt.cas.resource.structs.JobInput)
     */
    public boolean execute(JobInput in) throws JobInputException {
        NameValueJobInput input = (NameValueJobInput) in;
        PrintStream output = null;
        try {
            //Setup output and timing
            output = PalindromeUtils.getPrintStream(input.getValue("output"));
            final long start = System.currentTimeMillis();
            //Read file and process
            JavaRDD<String> rdd = sc.textFile( input.getValue("file"));
            JavaRDD<String> filtered = rdd.filter(new PalindromeUtils.FilterPalindrome());
            long count = filtered.count();
            //Output timing and results
            final long end = System.currentTimeMillis();
            double timing = ((double)(end - start))/1000.0;
            output.println("Found "+ count+" palindromes in "+timing+" seconds.");
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            try {
                output.close();
            } catch (Exception e) {}

        }
        return true;
    }

    @Override
    public void setSparkContext(SparkContext context) {
        this.sc = new JavaSparkContext(context);
    }

}
