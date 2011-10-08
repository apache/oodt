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
package org.apache.oodt.cas.protocol;

//OODT imports
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;

//JDK imports
import java.io.File;
import java.util.List;

/**
 * Interface for communication over different transfer protocols
 * 
 * @author bfoster
 * @version $Revision$
 */
public interface Protocol {

    public void connect(String host, Authentication authentication) throws ProtocolException;
    
    public void close() throws ProtocolException;

    public boolean connected();
    
    public void cd(ProtocolFile file) throws ProtocolException;

    public void cdRoot() throws ProtocolException;

    public void cdHome() throws ProtocolException;

    public void get(ProtocolFile fromFile, File toFile) throws ProtocolException;

    public void put(File fromFile, ProtocolFile toFile) throws ProtocolException;
    
    public ProtocolFile pwd() throws ProtocolException;

    public List<ProtocolFile> ls() throws ProtocolException;

    public List<ProtocolFile> ls(ProtocolFileFilter filter) throws ProtocolException;

    public void delete(ProtocolFile file) throws ProtocolException;

}
