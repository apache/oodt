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


package org.apache.oodt.cas.pushpull.protocol.http;

//OODT imports
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class HttpPath extends ProtocolPath {

    private static final long serialVersionUID = -7780059889413081800L;

    private URL link;

    private HttpPath parent;

    /**
     * Constructor
     * 
     * @param url
     *            The URL for this Path
     * @param isDir
     *            Tells whether this Path is a directory
     * @throws MalformedURLException
     */
    protected HttpPath(String virtualPath, boolean isDir, URL link,
            HttpPath parent) throws MalformedURLException {
        super(virtualPath, isDir);
        this.link = link;
        this.parent = parent;
    }

    protected URL getLink() {
        return this.link;
    }

    public ProtocolPath getParentPath() throws MalformedURLException {
        return this.parent;
    }
}
