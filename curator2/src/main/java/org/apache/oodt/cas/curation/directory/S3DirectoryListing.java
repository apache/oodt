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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.commons.validation.DirectoryValidator;
import org.apache.oodt.commons.validation.ValidationOutput;

public class S3DirectoryListing implements DirectoryListing {
  //Types of directory objects
  public enum Type {
    DIRECTORY,
    OBJECT
  }
  public static final String ROOT_NAME = "Root";
  //Attributes of node
  Type type;
  String name;
  String path;
  ValidationOutput validation;

  //Children listings (only valid for directory types)
  List<S3DirectoryListing> children = new LinkedList<S3DirectoryListing>();
  /**
   * Get a directory listing
   * @param type - type of listing
   * @param name - name of object
   * @param path - path of the file/directory
   * @param validation - validation object associated with this listing
   */
  public S3DirectoryListing(Type type,String name,String path, ValidationOutput validation) {
    this.name = name;
    this.type = type;
    this.path = path;
    this.children = (type == Type.DIRECTORY) ? new LinkedList<S3DirectoryListing>() : null;
    this.validation = validation;
  }

  /**
   * Create a directory listing
   * @param paths - list of file paths
   * @param validator
   * @return top-level directory listing object
   */
  public static DirectoryListing lisingFromFileObjects(Collection<String> paths,
      DirectoryValidator validator) {

    Map<String, S3DirectoryListing> S3DirectoryMap = new HashMap<>();
    String S3FileSeparator = "/";
    S3DirectoryListing root = new S3DirectoryListing(Type.DIRECTORY, "", "", null);
    S3DirectoryMap.put("", root);
    for (String file : paths) {
      String[] filePathElements = file.split(S3FileSeparator);
      String currentPath = "";
      String parent = "";
      for (int i=0; i<filePathElements.length; i++) {
        currentPath += filePathElements[i];
        if (!S3DirectoryMap.containsKey(currentPath)) {
          Type type = i == filePathElements.length - 1 ? Type.OBJECT : Type.DIRECTORY;
          S3DirectoryListing sdl = new S3DirectoryListing(type,
              filePathElements[i],
              currentPath,
              validator != null ? validator.validate(file, Configuration.getAllProperties())
                  : null);

          if (S3DirectoryMap.containsKey(parent)) {
            S3DirectoryMap.get(parent).children.add(sdl);
          }
          S3DirectoryMap.put(currentPath, sdl);
        }
        parent = currentPath;
        currentPath += "/";
      }
    }
    return S3DirectoryMap.get(root.name);
  }
}