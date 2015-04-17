/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.oodt.cas.resource.scheduler;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobInstance;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.SparkInstance;
import org.apache.oodt.cas.resource.structs.StreamingInstance;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.StreamingContext;

/**
 * A scheduler that runs spark jobs on a spark cluster.
 *
 * @author starchmd
 *
 */
public class SparkScheduler implements Scheduler {

    SparkContext sc;
    StreamingContext ssc;
    JobQueue queue;

    private static final Logger LOG = Logger.getLogger(SparkScheduler.class.getName());

    public SparkScheduler(JobQueue queue) {
        SparkConf conf = new SparkConf();
        conf.setMaster(System.getProperty("resource.runner.spark.host","local"));
        conf.setAppName("OODT Spark Job");

        URL location = SparkScheduler.class.getResource('/'+SparkScheduler.class.getName().replace('.', '/')+".class");
        conf.setJars(new String[]{"../lib/cas-resource-0.8-SNAPSHOT.jar"});
        sc = new SparkContext(conf);
        ssc = new StreamingContext(sc,new Duration(10000));
        this.queue = queue;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (queue.isEmpty())
                    continue;
                JobSpec spec = queue.getNextJob();

                Class<?> clazz = Class.forName(spec.getJob().getJobInstanceClassName());
                if (!(clazz.newInstance() instanceof SparkInstance)) {
                    LOG.log(Level.WARNING,"Non-Spark job found ("+spec.getJob().getId()+") ignoring.");
                    continue;
                }
                this.schedule(spec);
            } catch(SchedulerException e) {
                LOG.log(Level.WARNING,"Scheduler exception detected: "+e.getMessage());
            } catch (JobQueueException e) {
                LOG.log(Level.WARNING,"Could not get next job from job-queue.");
            } catch (ClassNotFoundException e) {
                LOG.log(Level.WARNING,"Class not found: "+e.getMessage());
            } catch (InstantiationException e) {
                LOG.log(Level.WARNING,"Could not instantiate: "+e.getMessage());
            } catch (IllegalAccessException e) {
                LOG.log(Level.WARNING,"Could not access: "+e.getMessage());
            }

        }
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.scheduler.Scheduler#schedule(org.apache.oodt.cas.resource.structs.JobSpec)
     */
    @Override
    public boolean schedule(JobSpec spec) throws SchedulerException {
        try {
            JobInstance instance = GenericResourceManagerObjectFactory.getJobInstanceFromClassName(spec.getJob().getJobInstanceClassName());
            //spec.getIn().
            SparkInstance sparkInstance = (SparkInstance) instance;
            LOG.log(Level.INFO,"Setting SparkContext");
            sparkInstance.setSparkContext(this.sc);
            //Handle spark streaming
            if (sparkInstance instanceof StreamingInstance) {
                LOG.log(Level.INFO,"Found streaming instance, setting StreamingContext");
                ((StreamingInstance)sparkInstance).setStreamingContext(this.ssc);
            }
            sparkInstance.execute(spec.getIn());

        } catch (JobInputException e) {
            LOG.log(Level.WARNING,"Job input exception detected.");
            throw new SchedulerException(e);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.scheduler.Scheduler#nodeAvailable(org.apache.oodt.cas.resource.structs.JobSpec)
     */
    @Override
    public ResourceNode nodeAvailable(JobSpec spec) throws SchedulerException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.scheduler.Scheduler#getMonitor()
     */
    @Override
    public Monitor getMonitor() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.scheduler.Scheduler#getBatchmgr()
     */
    @Override
    public Batchmgr getBatchmgr() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.scheduler.Scheduler#getJobQueue()
     */
    @Override
    public JobQueue getJobQueue() {
        return queue;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.scheduler.Scheduler#getQueueManager()
     */
    @Override
    public QueueManager getQueueManager() {
        // TODO Auto-generated method stub
        return null;
    }

}
