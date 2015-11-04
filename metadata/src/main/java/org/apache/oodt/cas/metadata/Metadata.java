// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.cas.metadata;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * 
 * Metadata is a {@link Map} of <code>String</code> keys mapped to Object
 * values. So, each key can map to potentially many values, but also can map to
 * null, or to a single value.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class Metadata {

  private Group root;

  public Metadata() {
    this.root = this.createNewRoot();
  }

  public Metadata(Metadata metadata) {
    this();
    this.addMetadata(metadata);
  }

  /**
   * Adds (Appends if key exists) from given metadata into this metadata
   * 
   * @param metadata
   *          Metadata to add metadata from
   */
  public void addMetadata(Metadata metadata) {
    for (String key : metadata.getAllKeys()) {
      this.addMetadata(key, metadata.getAllMetadata(key));
    }
  }

  public void addMetadata(String group, Metadata metadata) {
    if (group == null) {
      this.addMetadata(metadata);
    } else {
      for (String key : metadata.getAllKeys()) {
        this.addMetadata(group + "/" + key, metadata.getAllMetadata(key));
      }
    }
  }

  /**
   * Adds (Replaces if key exists) from given Metadata into this Metadata
   * 
   * @param metadata
   */
  public void replaceMetadata(Metadata metadata) {
    for (String key : metadata.getAllKeys()) {
      this.replaceMetadata(key, metadata.getAllMetadata(key));
    }
  }

  public void replaceMetadata(String group, Metadata metadata) {
    if (group == null) {
      this.replaceMetadata(metadata);
    } else {
      for (String key : metadata.getAllKeys()) {
        this.replaceMetadata(group + "/" + key, metadata.getAllMetadata(key));
      }
    }
  }

  /**
   * Adds key (Appends if key exists)
   * 
   * @param key
   *          Key to be added
   * @param value
   *          Value of key to be added
   */
  public void addMetadata(String key, String value) {
    this.getGroup(key).addValue(value);
  }

  /**
   * Adds key (Replaces if key exists)
   * 
   * @param key
   *          Key to be added
   * @param value
   *          Value of key to be added
   */
  public void replaceMetadata(String key, String value) {
    this.getGroup(key).setValue(value);
  }

  /**
   * Adds key (Appends if key exists)
   * 
   * @param key
   *          Key to be added
   * @param values
   *          Values of key to be added
   */
  public void addMetadata(String key, List<String> values) {
    this.getGroup(key).addValues(values);
  }

  /**
   * Adds key (Replaces if key exists)
   * 
   * @param key
   *          Key to be added
   * @param values
   *          Values of key to be added
   */
  public void replaceMetadata(String key, List<String> values) {
    this.getGroup(key).setValues(values);
  }

  /**
   * Removes key
   * 
   * @param key
   *          Key to remove
   */
  public void removeMetadata(String key) {
    Group removeGroup = this.getGroup(key, false);
    if (removeGroup != null && removeGroup.hasValues()) {
      if (removeGroup.getChildren().size() > 0) {
        removeGroup.clearValues();
      } else {
        removeGroup.getParent().removeChild(removeGroup.getName());
      }
    }
  }

  /**
   * Removes key
   *
   */
  public void removeMetadataGroup(String group) {
    Group removeGroup = this.getGroup(group, false);
    if (removeGroup != null && removeGroup.getChildren().size() > 0) {
      removeGroup.getParent().removeChild(removeGroup.getName());
    }
  }

  /**
   * Checks if keys exists
   * 
   * @param key
   *          Key to check for
   * @return True if key exists, false otherwise
   */
  public boolean containsKey(String key) {
    Group group = this.getGroup(key, false);
    return group != null && group.hasValues();
  }

  /**
   * Checks if key has more than one value
   * 
   * @param key
   *          Key to check for
   * @return True is key exists and has more than one value, false otherwise
   */
  public boolean isMultiValued(String key) {
    Group group = this.getGroup(key, false);
    return group != null && group.getValues().size() > 1;
  }

  /**
   * Creates a Metadata from the given group
   * 
   * @param group
   *          The Group to grab
   * @return Metadata containing group and all keys below it
   */
  public Metadata getSubMetadata(String group) {
    Metadata m = new Metadata();
    Group newRoot = this.getGroup(group, false);
    if (newRoot != null) {
      m.root.addChildren(newRoot.clone().getChildren());
    }
    return m;
  }

  /**
   * Gets the first value for the given key
   * 
   * @param key
   *          The key for which the first value will be returned
   * @return First value for given key, or null if key does not exist
   */
  public String getMetadata(String key) {
    Group group = this.getGroup(key, false);
    if (group != null) {
      return group.getValue();
    } else {
      return null;
    }
  }

  /**
   * Gets all values for give key
   * 
   * @param key
   *          The key for which all values will be return
   * @return All values for given key, or null if key does not exist
   */
  public List<String> getAllMetadata(String key) {
    Group group = this.getGroup(key, false);
    if (group != null) {
      return new Vector<String>(group.getValues());
    } else {
      return null;
    }
  }

  /**
   * Gets All key in and below given group
   * 
   * @param group
   *          The group in question
   * @return All keys for the given group and below
   */
  public List<String> getKeys(String group) {
    Group foundGroup = this.getGroup(group);
    if (foundGroup != null) {
      return this.getKeys(foundGroup);
    } else {
      return new Vector<String>();
    }
  }

  /**
   * Gets all keys in this Metadata
   * 
   * @return All keys in this Metadata
   */
  public List<String> getKeys() {
    return this.getKeys(this.root);
  }

  protected List<String> getKeys(Group group) {
    Vector<String> keys = new Vector<String>();
    for (Group child : group.getChildren()) {
      if (child.hasValues()) {
        keys.add(child.getFullPath());
      }
    }
    return keys;
  }

  /**
   * Gets All key in and below given group
   * 
   * @param group
   *          The group in question
   * @return All keys for the given group and below
   */
  public List<String> getAllKeys(String group) {
    Group foundGroup = this.getGroup(group);
    if (foundGroup != null) {
      return this.getAllKeys(foundGroup);
    } else {
      return new Vector<String>();
    }
  }

  /**
   * Gets all keys in this Metadata
   * 
   * @return All keys in this Metadata
   */
  public List<String> getAllKeys() {
    return this.getAllKeys(this.root);
  }

  protected List<String> getAllKeys(Group group) {
    Vector<String> keys = new Vector<String>();
    for (Group child : group.getChildren()) {
      if (child.hasValues()) {
        keys.add(child.getFullPath());
      }
      keys.addAll(this.getAllKeys(child));
    }
    return keys;
  }
  
  /**
   * Get all keys whose leaf key name is equal to the given arg
   * @param key leaf key name
   * @return list of keys with the given leaf key name
   */
  public List<String> getAllKeysWithName(String key) {
	  List<String> keys = new Vector<String>();
	  Stack<Group> stack = new Stack<Group>();
	  stack.add(this.root);
	  while (!stack.empty()) {
		  Group curGroup = stack.pop();
		  if (curGroup.getName().equals(key) && curGroup.hasValues()) {
            keys.add(curGroup.getFullPath());
          }
		  stack.addAll(curGroup.getChildren());
	  }
	  return keys;
  }

  /**
   * Gets Values in root group
   * 
   * @return All Values in root group
   */
  public List<String> getValues() {
    Vector<String> values = new Vector<String>();
    for (String key : this.getKeys()) {
      values.addAll(this.getAllMetadata(key));
    }
    return values;
  }

  /**
   * Gets values in given group
   * 
   * @param group
   *          Group in question
   * @return Values in given group
   */
  public List<String> getValues(String group) {
    Vector<String> values = new Vector<String>();
    for (String key : this.getKeys(group)) {
      values.addAll(this.getAllMetadata(key));
    }
    return values;
  }

  /**
   * Gets all values in this Metadata
   * 
   * @return All values in this Metadata
   */
  public List<String> getAllValues() {
    Vector<String> values = new Vector<String>();
    for (String key : this.getAllKeys()) {
      values.addAll(this.getAllMetadata(key));
    }
    return values;
  }

  /**
   * Gets All values in and below given group
   * 
   * @param group
   *          Group in question
   * @return All values in and below given group
   */
  public List<String> getAllValues(String group) {
    Vector<String> values = new Vector<String>();
    for (String key : this.getAllKeys(group)) {
      values.addAll(this.getAllMetadata(key));
    }
    return values;
  }

  @Deprecated
  public void addMetadata(Hashtable<String, Object> metadata) {
    addMetadata(metadata, false);
  }

  public void addMetadata(Map<String, Object> metadata) {
    addMetadata(metadata, false);
  }

  /**
   * Takes a Map of String keys and Object values.  Values of type List
   * must be a List of Strings; all other values will have its toString() method
   * invoked.
   * @param metadata Map based metadata to add
   * @param replace If true, existing keys will be replaced, other values will be
   * combined.
   */
  @Deprecated
  public void addMetadata(Map<String, Object> metadata, boolean replace) {
    // for back compat: the old method allowed us to give it a
    // Map<String,String> and it still worked
	for (Map.Entry<String, Object> key : metadata.entrySet()) {
	  List<String> vals = (key.getValue() instanceof List) ? (List<String>) key.getValue()
        : Collections.singletonList(key.getValue().toString());
      if (replace) {
        this.replaceMetadata(key.getKey(), vals);
      } else {
        this.addMetadata(key.getKey(), vals);
      }
    }
  }


  public void replaceMetadata(Map<String, Object> metadata) {
    this.root = this.createNewRoot();
    this.addMetadata(metadata);
  }

  public boolean containsGroup(String group) {
    return this.getGroup(group, false) != null;
  }

  public List<String> getGroups() {
    return this.getGroups(this.root);
  }

  public List<String> getGroups(String group) {
    return this.getGroups(this.getGroup(group));
  }

  protected List<String> getGroups(Group group) {
    Vector<String> groupNames = new Vector<String>();
    for (Group child : group.getChildren()) {
      groupNames.add(child.getName());
    }
    return groupNames;
  }

  protected Group getGroup(String key) {
    return getGroup(key, true);
  }

  protected Group getGroup(String key, boolean create) {
    if (key == null) {
      return this.root;
    }
    StringTokenizer tokenizer = new StringTokenizer(key, "/");
    Group curGroup = this.root;
    while (tokenizer.hasMoreTokens()) {
      String groupName = tokenizer.nextToken();
      Group childGroup = curGroup.getChild(groupName);
      if (childGroup == null) {
        if (!create) {
          return null;
        }
        childGroup = new Group(groupName);
        curGroup.addChild(childGroup);
      }
      curGroup = childGroup;
    }
    return curGroup;
  }

  protected Group createNewRoot() {
    return new Group(Group.ROOT_GROUP_NAME);
  }

  protected class Group {

    private static final String ROOT_GROUP_NAME = "root";

    private String name;
    private List<String> values;
    private Group parent;
    private Map<String, Group> children;

    public Group(String name) {
      this.name = name;
      this.values = new Vector<String>();
      this.children = new Hashtable<String, Group>();
    }

    /**
     * Create Metadata Group.
     * By default we create a group using a Hashtable for XMLRPC support. Once this has been superceeded by the Avro
     * Impl we should make the HashMap implementation the default.
     * @param name
     * @param legacy
     */
    public Group(String name, boolean legacy) {
      this.name = name;
      this.values = new Vector<String>();
      this.children = legacy ? new Hashtable<String, Group>() : new ConcurrentHashMap<String, Group>();

    }

    public String getName() {
      return this.name;
    }

    public String getFullPath() {
      if (this.parent != null && !this.parent.getName().equals(ROOT_GROUP_NAME)) {
        return this.parent.getFullPath() + "/" + this.name;
      } else {
        return this.name;
      }
    }

    public Group getParent() {
      return this.parent;
    }

    public void setValue(String value) {
      this.values.clear();
      this.values.add(value);
    }

    public void setValues(List<String> values) {
      this.values.clear();
      this.values.addAll(values);
    }

    public void clearValues() {
      this.values.clear();
    }

    public void addValue(String value) {
      this.values.add(value);
    }

    public void addValues(List<String> value) {
      this.values.addAll(value);
    }

    public boolean hasValues() {
      return this.values.size() > 0;
    }

    public String getValue() {
      if (this.hasValues()) {
        return this.values.get(0);
      } else {
        return null;
      }
    }

    public List<String> getValues() {
      return this.values;
    }

    public void addChild(Group child) {
      this.children.put(child.getName(), child);
      child.parent = this;
    }

    public void addChildren(List<Group> children) {
      for (Group child : children) {
        this.addChild(child);
      }
    }

    public List<Group> getChildren() {
      return new Vector<Group>(this.children.values());
    }

    public void removeChild(String name) {
      this.children.remove(name);
    }

    public Group getChild(String name) {
      return this.children.get(name);
    }

    @Override
    public Group clone() {
      Group clone = new Group(this.name);
      clone.setValues(this.values);
      for (Group child : this.children.values()) {
        clone.addChild(child.clone());
      }
      return clone;
    }

    @Override
    public String toString() {
      return this.getFullPath();
    }

  }


  @Deprecated
  public Hashtable<String, Object> getHashTable() {
    Hashtable<String, Object> table = new Hashtable<String, Object>();
    for (String key : this.getAllKeys()) {
      table.put(key, this.getAllMetadata(key));
    }
    return table;
  }

  public Map<String, Object> getMap() {
    Map<String, Object> table = new ConcurrentHashMap<String, Object>();
    for (String key : this.getAllKeys()) {
      table.put(key, this.getAllMetadata(key));
    }
    return table;
  }

  public boolean equals(Object obj) {
    if (obj instanceof Metadata) {
      Metadata compMet = (Metadata) obj;
      if (this.getKeys().equals(compMet.getKeys())) {
        for (String key : this.getKeys()) {
          if (!this.getAllMetadata(key).equals(compMet.getAllMetadata(key))) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }

    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return root != null ? root.hashCode() : 0;
  }
}
