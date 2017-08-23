/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.config.distributed.cli;

import org.apache.oodt.cas.cli.CmdLineUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with main method which gets invoked by the CLI.
 * <p>
 * Basic usage:
 * <pre>
 *     ./config-publisher -connectString localhost:2181 </> -a {publish|verify|clear}
 * </pre>
 * <p>
 * Optionally, users can give <pre>-notify</pre> option to notify the listening OODT components on the configuration
 * change.
 *
 * @author Imesha Sudasingha
 */
public class ConfigPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ConfigPublisher.class);

    public static void main(String[] args) throws Exception {
        CmdLineUtility cmdLineUtility = new CmdLineUtility();
        cmdLineUtility.run(args);
    }
}
