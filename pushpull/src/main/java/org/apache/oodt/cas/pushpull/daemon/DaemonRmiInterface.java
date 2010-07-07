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


package org.apache.oodt.cas.pushpull.daemon;

//JDK imports
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface DaemonRmiInterface extends Remote {

    /**
     * Wakes up the CrawlDaemon if it is sleeping
     * 
     * @return Always true -- done because of XmlRpc communication
     */
    public void resume() throws RemoteException;

    /**
     * Will terminate the CrawlDaemon. If its Crawler is crawling a site when
     * this method is called, the terminate won't take place until after the
     * Crawler has complete crawling that site.
     * 
     * @return
     */
    public void quit() throws RemoteException;

    /**
     * Can be used to determine if Crawler is presently running
     * 
     * @return true if Crawler is runnning
     * @uml.property name="isRunning"
     */
    public boolean isRunning() throws RemoteException;

    /**
     * Average runtime for the Crawler
     * 
     * @return average runtime for the Crawler
     */
    public long getAverageRunTime() throws RemoteException;

    /**
     * Gets the total crawling time of the Crawler
     * 
     * @return Total crawling time of Crawler
     */
    public long getMillisCrawling() throws RemoteException;

    /**
     * Gets the time between the start of Crawler executions
     * 
     * @return Time interval between Crawler start times
     */
    public long getTimeInterval() throws RemoteException;

    /**
     * Gets the total number of times the Crawler has been run
     * 
     * @return The number of times Crawler has run
     */
    public int getNumCrawls() throws RemoteException;

    /**
     * Gets the time in milliseconds for when the CrawlDaemon constructor was
     * invoked.
     * 
     * @return
     * @uml.property name="daemonCreationTime"
     */
    public long getDaemonCreationTime() throws RemoteException;

    public boolean getHasBeenToldToQuit() throws RemoteException;

}
