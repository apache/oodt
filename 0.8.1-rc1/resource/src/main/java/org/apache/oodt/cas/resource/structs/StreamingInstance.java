package org.apache.oodt.cas.resource.structs;

import org.apache.spark.streaming.StreamingContext;

public interface StreamingInstance extends SparkInstance {

    /**
     * Set the context to run by.
     * @param context
     */
    public void setStreamingContext(StreamingContext context);

}
