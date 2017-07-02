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

package org.apache.oodt.config.distributed.cli;

import org.kohsuke.args4j.Option;

/**
 * Bean class used to map CLI options and arguments provided by the user when publishing configuration to zookeeper
 *
 * @author Imesha Sudaingha
 */
public class CmdLineOptions {

    @Option(name = "-connectString", usage = "Zookeeper connect string", required = true)
    private String connectString;

    @Option(name = "-publish", usage = "Publishes configuration specified in the spring config file to zookeeper. " +
            "Any current similar config in zookeeper will be overwritten. If not specified, command will be assumed as a publish")
    private boolean publish = true;

    @Option(name = "-verify", usage = "Verifies the content in the local files and the published ones. Results will be printed.")
    private boolean verify = true;

    @Option(name = "-clear", usage = "Unpublish any configuration which has been published earlier using the same spring config file")
    private boolean clear = false;

    public String getConnectString() {
        return connectString;
    }

    public boolean isPublish() {
        return publish;
    }

    public boolean isVerify() {
        return verify;
    }

    public boolean isClear() {
        return clear;
    }
}
