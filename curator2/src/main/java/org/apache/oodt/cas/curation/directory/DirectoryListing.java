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
package org.apache.oodt.cas.curation.directory;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.commons.validation.DirectoryValidator;
import org.apache.oodt.commons.validation.ValidationOutput;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A directory listing object
 *
 * @author starchmd
 */
public interface DirectoryListing {
    //Types of directory objects
    public enum Type {
        DIRECTORY,
        OBJECT
    }
    //Attributes of node
    DirectoryListing.Type type = null;
    String name = null;
    String path = null;
    ValidationOutput validation = null;

    //Children listings (only valid for directory types)
    List<DirectoryListing> children = new LinkedList<DirectoryListing>();
}