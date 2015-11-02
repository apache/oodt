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
package org.apache.oodt.cas.resource.batchmgr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobInstance;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.util.MesosUtilities;

/**
 * @author starchmd
 *
 * This "Executor" is run by mesos to actually run the job.
 */
public class ResourceExecutor implements Executor {

    PrintStream str = null;
    String id = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+" ";
    public ResourceExecutor() {
        try {
            File tmp = new File("./executor-log.notalog");
            //tmp.delete(); //With NIO then something must be caught
            str = new PrintStream(new FileOutputStream(tmp));
            str.println(id+"Starting up new<<<<<");
        } catch (FileNotFoundException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
    }
    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#disconnected(org.apache.mesos.ExecutorDriver)
     */
    @Override
    public void disconnected(ExecutorDriver driver) {
        str.println(id+"Disconnected!");
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#error(org.apache.mesos.ExecutorDriver, java.lang.String)
     */
    @Override
    public void error(ExecutorDriver driver, String error) {
        str.println(id+"Error: "+error);
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#frameworkMessage(org.apache.mesos.ExecutorDriver, byte[])
     */
    @Override
    public void frameworkMessage(ExecutorDriver arg0, byte[] arg1) {
        str.println(id+"Message: "+new String(arg1));
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#killTask(org.apache.mesos.ExecutorDriver, org.apache.mesos.Protos.TaskID)
     */
    @Override
    public void killTask(ExecutorDriver arg0, TaskID arg1) {
        str.println(id+"Kill");
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#launchTask(org.apache.mesos.ExecutorDriver, org.apache.mesos.Protos.TaskInfo)
     */
    @Override
    public void launchTask(final ExecutorDriver driver, final TaskInfo info) {
        str.println(id+"Launch task!");
        try {
            JobSpec spec = MesosUtilities.byteStringToJobSpec(info.getData());
            final JobInstance exec = GenericResourceManagerObjectFactory
                    .getJobInstanceFromClassName(spec.getJob().getJobInstanceClassName());
            final JobInput in = spec.getIn();
            Thread tmp = new Thread(new Runnable(){
                    public void run() {
                        TaskStatus status = null;
                        try {
                            exec.execute(in);
                            status = TaskStatus.newBuilder().setTaskId(info.getTaskId())
                                    .setState(TaskState.TASK_FINISHED).build();
                        } catch (JobInputException e) {
                            LOG.log(Level.SEVERE, e.getMessage());
                            status = TaskStatus.newBuilder().setTaskId(info.getTaskId())
                                    .setState(TaskState.TASK_FAILED).build();
                        }
                        driver.sendStatusUpdate(status);
                    }
            });
            driver.sendStatusUpdate(TaskStatus.newBuilder().setTaskId(info.getTaskId())
                  .setState(TaskState.TASK_STARTING).build());
            tmp.start();
        } catch (ClassNotFoundException e1) {
            System.out.println("BAD DATA: ");
            e1.printStackTrace();
            driver.sendStatusUpdate(TaskStatus.newBuilder().setTaskId(info.getTaskId())
                    .setState(TaskState.TASK_FAILED).build());
        } catch (InstantiationException e2) {
            System.out.println("BAD DATA: ");
            e2.printStackTrace();
            driver.sendStatusUpdate(TaskStatus.newBuilder().setTaskId(info.getTaskId())
                    .setState(TaskState.TASK_FAILED).build());
        } catch (IllegalAccessException e3) {
            System.out.println("BAD DATA: ");
            e3.printStackTrace();
            driver.sendStatusUpdate(TaskStatus.newBuilder().setTaskId(info.getTaskId())
                    .setState(TaskState.TASK_FAILED).build());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#registered(org.apache.mesos.ExecutorDriver, org.apache.mesos.Protos.ExecutorInfo, org.apache.mesos.Protos.FrameworkInfo, org.apache.mesos.Protos.SlaveInfo)
     */
    @Override
    public void registered(ExecutorDriver arg0, ExecutorInfo arg1,
            FrameworkInfo arg2, SlaveInfo arg3) {
        System.out.println("Do-Wah-Do-Wah");
        str.println(id+"Registered, Huzzah!");

    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#reregistered(org.apache.mesos.ExecutorDriver, org.apache.mesos.Protos.SlaveInfo)
     */
    @Override
    public void reregistered(ExecutorDriver arg0, SlaveInfo arg1) {
        System.out.println("Do-Wah-Do-Wah GO GO GO!!!!");
        str.println(id+"Re-regged");
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Executor#shutdown(org.apache.mesos.ExecutorDriver)
     */
    @Override
    public void shutdown(ExecutorDriver arg0) {
        System.out.println("Down down down.");
        str.println(id+"Shutdown");
    }

    public static void main(String[] args) throws Exception {
        MesosExecutorDriver driver = new MesosExecutorDriver(new ResourceExecutor());
        System.exit(driver.run() == Status.DRIVER_STOPPED ? 0 : 1);
    }
}
