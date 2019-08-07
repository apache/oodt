/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.pcs.health;

import java.io.Serializable;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A simple data structure to capture the status of a PCS daemon (either the
 * File Manager, the Workflow Manager, or the Resource Manager)
 * </p>.
 */
public class PCSDaemonStatus implements Serializable {

    private String daemonName;

    private String urlStr;

    private String status;

    /**
     * Default Constructor.
     * 
     */
    public PCSDaemonStatus() {
    }

    /**
     * Constructs a new PCSDaemonStatus with the default parameters.
     * 
     * @param daemonName
     *            The name of the PCS Daemon.
     * @param urlStr
     *            A Stirng representation of a {@link URL} that this daemon runs
     *            on.
     * @param status
     *            One of {@link PCSHealthMonitorMetKeys#STATUS_UP} , or
     *            {@link PCSHealthMonitorMetKeys#STATUS_DOWN}.
     */
    public PCSDaemonStatus(String daemonName, String urlStr, String status) {
        super();
        this.daemonName = daemonName;
        this.urlStr = urlStr;
        this.status = status;
    }

    /**
     * @return the daemonName
     */
    public String getDaemonName() {
        return daemonName;
    }

    /**
     * @param daemonName
     *            the daemonName to set
     */
    public void setDaemonName(String daemonName) {
        this.daemonName = daemonName;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the urlStr
     */
    public String getUrlStr() {
        return urlStr;
    }

    /**
     * @param urlStr
     *            the urlStr to set
     */
    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
    }

}
