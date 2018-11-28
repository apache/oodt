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
import org.apache.oodt.cas.resource.structs.StreamingInstance;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

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
public class StreamingPalindromeExample implements StreamingInstance {

    JavaSparkContext sc;
    JavaStreamingContext ssc;
    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.cas.resource.structs.JobInstance#execute(org.apache.oodt.cas.resource.structs.JobInput)
     */
    //Will not serialize a class that has no members
    @SuppressWarnings("serial")
    public boolean execute(JobInput in) throws JobInputException {
        NameValueJobInput input = (NameValueJobInput) in;
        //Get time for watchdog
        final int time = Integer.parseInt(input.getValue("time"));
        try {
            //Output to put data into
            final PrintStream output = PalindromeUtils.getPrintStream(input.getValue("output"));
            //Filter stream and count
            JavaReceiverInputDStream<String> stream = ssc.socketTextStream(input.getValue("host"),Integer.parseInt(input.getValue("port")));
            JavaDStream<String> filtered = stream.filter(new PalindromeUtils.FilterPalindrome());
            final JavaDStream<Long> count = filtered.count();
            //For each packet-ized count: output
            count.foreachRDD(new Function<JavaRDD<Long>,Void>(){
                @Override
                public Void call(JavaRDD<Long> jrdd) throws Exception {
                    synchronized(output)
                    {
                        Long[] collected = (Long[])jrdd.rdd().collect();
                        for (Long item : collected)
                        output.println("Found "+item.longValue()+ " palindromes.");
                    }
                    return null;
                }});
            ssc.start();
            //Stop in <time> seconds
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        //Don't cast this exception into the void
                        Thread.currentThread().interrupt();
                    } finally {
                        ssc.stop();
                        output.println("Stopping after "+time/1000+" seconds.");
                    }
                }
            }).start();
            //Wait for streaming to terminate
            ssc.awaitTermination();
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }
    @Override
    public void setStreamingContext(StreamingContext context) {
        this.ssc = new JavaStreamingContext(context);
    }
    @Override
    public void setSparkContext(SparkContext context) {
        this.sc = new JavaSparkContext(context);
    }

}
