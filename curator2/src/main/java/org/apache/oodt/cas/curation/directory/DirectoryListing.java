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
public class DirectoryListing {
    //Types of directory objects
    public enum Type {
        DIRECTORY,
        OBJECT
    }
    public static final String ROOT_NAME = "Root";
    //Attributes of node
    DirectoryListing.Type type;
    String name;
    String path;
    ValidationOutput validation;

    //Children listings (only valid for directory types)
    List<DirectoryListing> children = new LinkedList<DirectoryListing>();
    /**
     * Get a directory listing
     * @param type - type of listing
     * @param name - name of object
     */
    public DirectoryListing(DirectoryListing.Type type,String name,String path, ValidationOutput validation) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.children = (type == DirectoryListing.Type.DIRECTORY) ? new LinkedList<DirectoryListing>() : null;
        this.validation = validation;
    }
    /**
     * Create a directory listing 
     * @param paths - list of file paths
     * @param validator
     * @return top-level directory listing object
     */
    public static DirectoryListing lisingFromFileObjects(Collection<File> paths, File root,
                                                         DirectoryValidator validator) {
        //Shallow copy and sort
        List<File> copy = new LinkedList<File>(paths);
        Collections.sort(copy);
        //Create a stack to hold directories (implementation details)
        LinkedList<DirectoryListing> stack = new LinkedList<DirectoryListing>();
        stack.addLast( (root != null && root.isDirectory()) ?
                       new DirectoryListing(DirectoryListing.Type.DIRECTORY,root.getPath(),root.getPath(),
                           validator != null ? validator.validate(root, Configuration.getAllProperties()):null) :
                       new DirectoryListing(DirectoryListing.Type.DIRECTORY,ROOT_NAME,"", null));
        for (File file : paths) {
            if (file.equals(root))
                continue;
            //Remove all directories off stack until file starts with last's path
            while (!file.getPath().startsWith(stack.peekLast().path))
                stack.removeLast();
            //Get type and name of this file path and create dl object
            DirectoryListing.Type type =  file.isDirectory() ? DirectoryListing.Type.DIRECTORY : DirectoryListing.Type.OBJECT;
            DirectoryListing dl = new DirectoryListing(type,file.getName(),file.getPath(),
                validator != null ? validator.validate(file, Configuration.getAllProperties()) :
                null);

            //Add to last's children
            stack.peekLast().children.add(dl);
            if (type == DirectoryListing.Type.DIRECTORY) {
                stack.addLast(dl);
            }
        }
        return stack.peekFirst();
    }
}