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

package org.apache.oodt.cas.filemgr.datatransfer;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory for constructing {@link RemoteDataTransferer} objects.
 * </p>
 * 
 */
public class RemoteDataTransferFactory implements DataTransferFactory {

    /* the chunk size for sending files remotely */
    private int chunkSize = 0;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(RemoteDataTransferFactory.class.getName());

    /**
     * 
     */
    public RemoteDataTransferFactory() {
        chunkSize = Integer.getInteger(
            "org.apache.oodt.cas.filemgr.datatransfer.remote.chunkSize",
            1024);

        LOG.log(Level.INFO, "RemoteDataTransfer enabled: using chunk size: ["
                + chunkSize + "]");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.datatransfer.DataTransferFactory#createDataTransfer()
     */
    public DataTransfer createDataTransfer() {
        return new RemoteDataTransferer(chunkSize);
    }

}
