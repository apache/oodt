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


package org.apache.oodt.cas.pushpull.filerestrictions;

//OODT imports
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class VirtualFileStructure {

    private String pathToRoot;

    private VirtualFile root;

    private RemoteSite remoteSite;

    public VirtualFileStructure(String pathToRoot, VirtualFile root) {
        this.pathToRoot = pathToRoot;
        this.root = root;
    }

    public VirtualFileStructure(RemoteSite remoteSite, String pathToRoot,
            VirtualFile root) {
        this(pathToRoot, root);
        this.remoteSite = remoteSite;
    }

    public String getPathToRoot() {
        return this.pathToRoot;
    }

    public VirtualFile getRootVirtualFile() {
        return this.root;
    }

    public RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public boolean isRootBased() {
        return this.pathToRoot.startsWith("/");
    }

}
