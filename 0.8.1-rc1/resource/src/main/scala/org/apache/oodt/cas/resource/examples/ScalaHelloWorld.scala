package org.apache.oodt.cas.resource.examples;

import org.apache.oodt.cas.resource.structs.SparkInstance;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.NameValueJobInput;
import org.apache.spark.SparkContext;
/**
* @author starchmd
*
* An example class for use with the Spark backend to the resource manager.
*/
class ScalaHelloWorld extends SparkInstance {
    var sc : SparkContext = null;
    /**
     * Execute this job.
     */
    def execute(input: JobInput) : Boolean = {
        val name = input.asInstanceOf[NameValueJobInput].getValue("file");
        val textFile = sc.textFile(name);
        val linesWithSpark = textFile.filter(line => line.contains("fantastic"));
        var ln = linesWithSpark.count();
        println("Line count: "+ln);
        textFile.foreach(line => println(line));
        return true;
    }
    /**
     * Set the spark context.
     */
    def setSparkContext(context: SparkContext) {
        this.sc = context;
    }
}
