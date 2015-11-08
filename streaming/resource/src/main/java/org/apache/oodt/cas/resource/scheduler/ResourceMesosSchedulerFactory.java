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
package org.apache.oodt.cas.resource.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.SchedulerDriver;
import org.apache.oodt.cas.resource.batchmgr.MesosBatchManager;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

/**
 * A class to setup the resource manager's mesos framework.
 * @author starchmd
 * @version $Revision$
 *
 */
public class ResourceMesosSchedulerFactory implements SchedulerFactory {

    private static final Logger LOG = Logger.getLogger(ResourceMesosSchedulerFactory.class.getName());

    private Monitor mon = null;
    private MesosBatchManager batch = null;
    private JobQueue queue = null;

    public ResourceMesosSchedulerFactory() {}

    public Scheduler construct() {
        try {
            String uri = System.getProperty("org.apache.oodt.cas.resource.mesos.executor.uri","./oodt-executor.in");
            //Framework info
            FrameworkInfo.Builder frameworkBuilder = FrameworkInfo.newBuilder()
                        .setName("OODT Resource Manager Mesos Framework").setUser("")
                        .setId(FrameworkID.newBuilder().setValue("OODT-Resource Framework").build());
            FrameworkInfo framework = frameworkBuilder.build();
            ExecutorInfo executor = ExecutorInfo.newBuilder().setExecutorId(ExecutorID.newBuilder().setValue("OODT-Resource").build())
                    .setCommand(CommandInfo.newBuilder().setValue(new File(uri).getCanonicalPath()).build())
                    .setName("OODT Resource Manager Executor").build();
            SchedulerDriver driver = null;

            //Resource manager properties
            String batchmgrClassStr = "org.apache.oodt.cas.resource.batchmgr.MesosBatchManagerFactory";
            String monitorClassStr = "org.apache.oodt.cas.resource.monitor.MesosMonitorFactory";
            String jobQueueClassStr = System.getProperty("resource.jobqueue.factory","org.apache.oodt.cas.resource.jobqueue.JobStackJobQueueFactory");
            String ip = System.getProperty("resource.mesos.master.ip","127.0.0.1:5050");

            batch = (MesosBatchManager)GenericResourceManagerObjectFactory.getBatchmgrServiceFromFactory(batchmgrClassStr);
            mon = GenericResourceManagerObjectFactory.getMonitorServiceFromFactory(monitorClassStr);
            queue = GenericResourceManagerObjectFactory.getJobQueueServiceFromFactory(jobQueueClassStr);
            batch.setMonitor(mon);
            batch.setDriver(driver);
            batch.setJobRepository(queue.getJobRepository());

            LOG.log(Level.INFO,"Connecting to Mesos Master at: "+ip);
            System.out.println("Connecting to Mesos Master at: "+ip);
            ResourceMesosScheduler scheduler = new ResourceMesosScheduler(batch, executor, queue, mon);

            final MesosSchedulerDriver mesos = new MesosSchedulerDriver(scheduler, framework, ip);
            //Anonymous thread to run
            new Thread(new Runnable() {
                public void run() {
                    int status = mesos.run() == Status.DRIVER_STOPPED ? 0 : 1;
                    mesos.stop();
                }
            }).start();
            return scheduler;
        } catch(IOException ioe) {
            LOG.log(Level.SEVERE,"Exception detected: "+ioe.getMessage());
            ioLOG.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public Scheduler createScheduler() {
        return construct();
    }
}
