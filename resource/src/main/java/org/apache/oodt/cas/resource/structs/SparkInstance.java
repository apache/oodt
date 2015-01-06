package org.apache.oodt.cas.resource.structs;

import org.apache.spark.SparkContext;

public interface SparkInstance extends JobInstance {

    /**
     * Set the context to run by.
     * @param context
     */
    public void setSparkContext(SparkContext context);

}
