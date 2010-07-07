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


package org.apache.oodt.cas.pushpull.protocol;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class ProtocolFile {

    protected RemoteSite remoteSite;

    protected ProtocolPath path;

    public ProtocolFile(RemoteSite remoteSite, ProtocolPath path) {
        this.path = path;
        this.remoteSite = remoteSite;
    }

    public ProtocolFile getParentFile() throws MalformedURLException {
        return new ProtocolFile(this.remoteSite, path.getParentPath());
    }

    public String getName() {
        return path.getFileName();
    }

    public boolean isDirectory() {
        return path.isDirectory();
    }

    public boolean isRelativeToHOME() {
        return this.path.isRelativeToHOME();
    }

    public String getHostName() {
        return remoteSite.getURL().getHost();
    }

    public URL getURL() {
        return this.remoteSite.getURL();
    }

    public ProtocolPath getProtocolPath() {
        return path;
    }

    public void setPath(ProtocolPath path) {
        this.path = path;
    }

    public RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public boolean equals(Object protocolFile) {
        if (protocolFile instanceof ProtocolFile) {
            ProtocolFile pf = (ProtocolFile) protocolFile;
            return pf.remoteSite.equals(this.remoteSite)
                    && (pf.getProtocolPath().equals(this.getProtocolPath()));
        }
        return false;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public String toString() {
        return remoteSite.getURL() + path.toString();
    }

}
