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

package org.apache.oodt.cas.filemgr.system;

import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
/**
 * @author radu
 *
 * <p>Runs the {@link FileManagerServer} interface</p>
 *
 */
public class FileManagerServerMain {

    public static void main(String[] args) throws Exception {

        int portNum = -1;

        String usage = "FileManager --portNum <port number for rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }

        @SuppressWarnings("unused")

        FileManagerServer manager = RpcCommunicationFactory.createServer(portNum);
        manager.startUp();


        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }
}
