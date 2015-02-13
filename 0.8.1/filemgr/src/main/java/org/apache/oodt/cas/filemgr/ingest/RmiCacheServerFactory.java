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

package org.apache.oodt.cas.filemgr.ingest;

//JDK imports
import java.io.FileInputStream;
import java.net.URL;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Constructs new {@link RmiCacheServer}s.
 * </p>.
 */
public class RmiCacheServerFactory extends AbstractCacheServerFactory{

    /**
     * @throws InstantiationException
     */
    public RmiCacheServerFactory() throws InstantiationException {
        super();
    }
    
    public Cache createCache() throws InstantiationException{
        throw new InstantiationException("Don't call this method for the RmiCacheServer!");
    }

    public RmiCacheServer createRemoteCache() throws InstantiationException {
        try {
            return new RmiCacheServer(fmUrl, rangeQueryElementName,
                    rangeStartDateTime, rangeEndDateTime, uniqueElementName,
                    productTypeNames);

        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String propFilePath = null, fileManagerUrl = null;
        int rmiPort = -1;

        String usage = "RmiCacheServer [options] \n"
                + "--fileManagerUrl <url> \n" + "--rmiPort <port number> \n"
                + "--propFile <path>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--fileManagerUrl")) {
                fileManagerUrl = args[++i];
            } else if (args[i].equals("--propFile")) {
                propFilePath = args[++i];
            } else if (args[i].equals("--rmiPort")) {
                rmiPort = Integer.parseInt(args[++i]);
            }
        }

        if (propFilePath == null || fileManagerUrl == null || rmiPort == -1) {
            System.out.println(usage);
            return;
        }

        try {
            System.getProperties().load(new FileInputStream(propFilePath));
            RmiCacheServerFactory svrFactory = new RmiCacheServerFactory();
            RmiCacheServer cache = (RmiCacheServer)svrFactory.createRemoteCache();
            cache.launchServer(new URL(fileManagerUrl), rmiPort);
        } catch (Exception e) {
            System.err.println("Failed to launch RmiCacheServer : "
                    + e.getMessage());
        }
    }

}
