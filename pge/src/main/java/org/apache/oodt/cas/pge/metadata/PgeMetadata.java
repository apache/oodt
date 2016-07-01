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
package org.apache.oodt.cas.pge.metadata;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Vector;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;


//Google imports
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A wrapper class to act as a facade interface to all the different
 * {@link Metadata} sources given to a PGE.
 * 
 * NOTE: 2 ways to update DYNAMIC metadata: 1) Create a key link to a DYNAMIC metadata
 * key, then change the value of the key link or 2) add metadata then mark the
 * key as dynamic and commit it.
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class PgeMetadata {

   public enum Type {
      STATIC, DYNAMIC, LOCAL
   }
   public static final List<Type> DEFAULT_COMBINE_ORDER = Lists
         .newArrayList(Type.LOCAL, Type.DYNAMIC, Type.STATIC);
   
   public static final List<Type> DEFAULT_QUERY_ORDER = Lists
		   .newArrayList(Type.STATIC, Type.DYNAMIC, Type.LOCAL);

   private final Metadata staticMetadata;
   private final Metadata dynamicMetadata;
   private final Metadata localMetadata;

   private final Map<String, String> keyLinkMap;
   private final Set<String> markedAsDynamicMetKeys;

   public PgeMetadata() {
      keyLinkMap = Maps.newHashMap();
      markedAsDynamicMetKeys = Sets.newHashSet();
      staticMetadata = new Metadata();
      dynamicMetadata = new Metadata();
      localMetadata = new Metadata();
   }

   public PgeMetadata(PgeMetadata pgeMetadata) {
      this();

      Validate.notNull(pgeMetadata, "pgeMetadata cannot be null");

      replaceMetadata(pgeMetadata);
   }

   public PgeMetadata(Metadata staticMetadata, Metadata dynamicMetadata) {
      this();

      Validate.notNull(staticMetadata, "staticMetadata cannot be null");
      Validate.notNull(dynamicMetadata, "dynamicMetadata cannot be null");

      this.staticMetadata.replaceMetadata(staticMetadata);
      this.dynamicMetadata.replaceMetadata(dynamicMetadata);
   }

   /**
    * Replaces or creates this {@link PgeMetadata}'s metadata with given
    * {@link PgeMetadata}'s metadata. Also adds in the list of given
    * {@link PgeMetadata}'s LOCAL metadata marked for promotion DYNAMIC
    * metadata and list of key links.
    * 
    * @param pgeMetadata
    *           A {@link PgeMetadata} whose metadata and key links will be added
    *           to this {@link PgeMetadata}'s metadata and key links.
    */
   public void replaceMetadata(PgeMetadata pgeMetadata) {
      Validate.notNull(pgeMetadata, "pgeMetadata cannot be null");

      staticMetadata.replaceMetadata(pgeMetadata.staticMetadata);
      dynamicMetadata.replaceMetadata(pgeMetadata.dynamicMetadata);
      localMetadata.replaceMetadata(pgeMetadata.localMetadata);

      keyLinkMap.putAll(pgeMetadata.keyLinkMap);
      markedAsDynamicMetKeys.addAll(pgeMetadata.markedAsDynamicMetKeys);
   }

   /**
    * Replaces or creates this {@link PgeMetadata}'s metadata with given
    * {@link PgeMetadata}'s metadata. The provided "group" will be used to
    * namespace the given {@link PgeMetadata}'s LOCAL metadata when add to this
    * {@link PgeMetadata}'s LOCAL metadata. It will also namespace given
    * {@link PgeMetadata}'s key links before adding then to this
    * {@link PgeMetadata}'s key links. Also add in the list of given
    * {@link PgeMetadata}'s LOCAL metadata marked for promotion DYNAMIC
    * metadata.
    * 
    * @param pgeMetadata
    *           A {@link PgeMetadata} whose metadata and key links will be added
    *           to this {@link PgeMetadata}'s metadata and key links.
    * @param group
    *           The namespace which will be used to namespace given
    *           {@link PgeMetadata}'s LOCAL metadata and key links before being
    *           added to this {@link PgeMetadata}'s LOCAL metadata and key
    *           links.
    */
   public void replaceMetadata(PgeMetadata pgeMetadata, String group) {
      Validate.notNull(pgeMetadata, "pgeMetadata cannot be null");
      Validate.notNull(group, "group cannot be null");

      staticMetadata.replaceMetadata(pgeMetadata.staticMetadata);
      dynamicMetadata.replaceMetadata(pgeMetadata.dynamicMetadata);
      localMetadata.replaceMetadata(group, pgeMetadata.localMetadata);

      // Namespace link keys that point to either importing
      // metadata's local key or link key.
      for (String keyLink : pgeMetadata.keyLinkMap.keySet()) {
         String key = pgeMetadata.keyLinkMap.get(keyLink);
         // Check if key is was local key or a link key
         if (pgeMetadata.localMetadata.containsKey(key)
               || pgeMetadata.keyLinkMap.containsKey(key)) {
            key = group + "/" + key;
         }
         linkKey(group + "/" + keyLink, key);
      }

      // Namespace workflow keys that point to either importing
      // metadata's local key or link key.
      for (String key : pgeMetadata.markedAsDynamicMetKeys) {
         if (pgeMetadata.localMetadata.containsKey(key)
               || pgeMetadata.keyLinkMap.containsKey(key)) {
            key = group + "/" + key;
         }
         markAsDynamicMetadataKey(key);
      }
   }

   /**
    * Use to mark LOCAL keys which should be moved into DYNAMIC metadata when
    * {@link #commitMarkedDynamicMetadataKeys(String...)} is invoked. If no 
    * args are specified then all LOCAL metadata is marked for move to
    * DYNAMIC metadata.
    * 
    * @param keys
    *           Keys to mark as to be made DYNAMIC, otherwise if no keys then
    *           all LOCAL metadata keys are mark for move to DYNAMIC.
    */
   public void markAsDynamicMetadataKey(String... keys) {
      List<String> markedKeys = Lists.newArrayList(keys);
      if (markedKeys.isEmpty()) {
         markedKeys.addAll(localMetadata.getAllKeys());
      }
      markedAsDynamicMetKeys.addAll(markedKeys);
   }

   /**
    * Use to commit marked LOCAL keys to DYNAMIC keys. Specify a list of keys
    * only if you want to limit the keys which get committed, otherwise all
    * marked keys will be moved into DYNAMIC metadata.
    * 
    * @param keys
    *           The list of marked LOCAL metadata keys which should be moved
    *           into DYNAMIC metadata. If no keys are specified then all marked
    *           keys are moved.
    */
   public void commitMarkedDynamicMetadataKeys(String... keys) {
      Set<String> commitKeys = Sets.newHashSet(keys);
      if (commitKeys.isEmpty()) {
         commitKeys.addAll(markedAsDynamicMetKeys);
      } else {
         commitKeys.retainAll(markedAsDynamicMetKeys);
      }
      for (String key : commitKeys) {
         dynamicMetadata.replaceMetadata(key,
               localMetadata.getAllMetadata(resolveKey(key)));
         localMetadata.removeMetadata(key);
         markedAsDynamicMetKeys.remove(key);
      }
   }

   @VisibleForTesting
   protected Set<String> getMarkedAsDynamicMetadataKeys() {
      return Collections.unmodifiableSet(markedAsDynamicMetKeys);
   }

   /**
    * Create a key which is a link to another key, such that if you get the
    * metadata values for the created link it will return the current metadata
    * values of the key it was linked to. NOTE: if the key's metadata values
    * change, then the metadata values for the link key will also be the changed
    * values. If you want to create a key which holds the current value of a
    * key, then create a new metadata key.
    * 
    * @param keyLink
    *           The name of the link key you wish to create.
    * @param key
    *           The key you which to link to (may also be a key link)
    */
   public void linkKey(String keyLink, String key) {
      Validate.notNull(keyLink, "keyLink cannot be null");
      Validate.notNull(key, "key cannot be null");

      localMetadata.removeMetadata(keyLink);
      keyLinkMap.put(keyLink, key);
   }

   /**
    * Removes a key link reference. The key which the key link was linked to
    * remains unchanged.
    * 
    * @param keyLink
    *           The key link which you wish to destroy.
    */
   public void unlinkKey(String keyLink) {
      Validate.notNull(keyLink, "keyLink cannot be null");

      keyLinkMap.remove(keyLink);
   }

   /**
    * Check if the given key name is a key link.
    * 
    * @param key
    *           The key name in question.
    * @return True is the given key name is a key link, false if key name is an
    *         actual key.
    */
   public boolean isLink(String key) {
      Validate.notNull(key, "key cannot be null");

      return keyLinkMap.containsKey(key);
   }

   /**
    * Find the actual key whose value will be returned for the given key. If the
    * given key is a key (not a key link) then the given key will just be
    * returned, otherwise it will trace through key link mapping to find the key
    * which the given key link points to.
    * 
    * @param key
    *           The name of a key or key link.
    * @return The key whose value will be returned for the given key or key
    *         link.
    */
   public String resolveKey(String key) {
      Validate.notNull(key, "key cannot be null");

      while (keyLinkMap.containsKey(key)) {
         key = keyLinkMap.get(key);
      }
      return key;
   }

   /**
    * Determines the path by which the given key (if it is a key link) links to
    * the key whose value it will return. If the given key is a key link and
    * points to a key then the returning {@link List} will be of size 1 and will
    * contain just that key. However, if the given key is a key link which
    * points to another key link then the returning {@link List} will be greater
    * than 1 (will depend on how many key links are connected before they actual
    * point to a key. If the given key is a key, then the returning {@link List}
    * will be empty.
    * 
    * @param key
    *           The path to the key whose value will be returned for the give
    *           key.
    * @return A key path {@link List}.
    */
   public List<String> getReferenceKeyPath(String key) {
      Validate.notNull(key, "key cannot be null");

      List<String> keyPath = Lists.newArrayList();
      while (keyLinkMap.containsKey(key)) {
         keyPath.add(key = keyLinkMap.get(key));
      }
      return keyPath;
   }

   public void replaceMetadata(PgeTaskMetKeys key, String value) {
      Validate.notNull(key, "key cannot be null");

      replaceMetadata(key.getName(), value);
   }

   /**
    * Replace the given key's value with the given value. If the given key is a
    * key link, then it will update the value of the key it is linked to if that
    * key is DYNAMIC or LOCAL. If given key is a key link and it links to a
    * STATIC key, then a new LOCAL key will be create.
    * 
    * @param key
    *           The key or key link for whose value should be replaced.
    * @param value
    *           The value to give the given key. Will replace any existing value
    *           or will be the value of a newly created LOCAL key.
    */
   public void replaceMetadata(String key, String value) {
      Validate.notNull(key, "key cannot be null");
      Validate.notNull(value, "value cannot be null");

      String resolveKey = resolveKey(key);
      // If key is a key link which points to a DYNAMIC key then update the
      // DYNAMIC key's value.
      if (keyLinkMap.containsKey(key)
            && dynamicMetadata.containsKey(resolveKey)) {
         dynamicMetadata.replaceMetadata(resolveKey, value);
      } else {
         localMetadata.replaceMetadata(resolveKey, value);
      }
   }

   /**
    * Replace all key values with the given key values in the provided
    * {@link Metadata}. If the key does not exist it will be created.
    * 
    * @param metadata
    *           {@link Metadata} to replace or create.
    */
   public void replaceMetadata(Metadata metadata) {
      Validate.notNull(metadata, "metadata cannot be null");

      for (String key : metadata.getAllKeys()) {
         replaceMetadata(key, metadata.getAllMetadata(key));
      }
   }

   public void replaceMetadata(PgeTaskMetKeys key, List<String> values) {
      Validate.notNull(key, "key cannot be null");

      replaceMetadata(key.getName(), values);
   }

   /**
    * Replace the given key's values with the given values. If the given key is
    * a key link, then it will update the values of the key it is linked to if
    * that key is DYNAMIC or LOCAL. If given key is a key link and it links to a
    * STATIC key, then a new LOCAL key will be create.
    * 
    * @param key
    *           The key or key link for whose values should be replaced.
    * @param values
    *           The values to give the given key. Will replace any existing
    *           values or will be the values of a newly created LOCAL key.
    */
   public void replaceMetadata(String key, List<String> values) {
      Validate.notNull(key, "key cannot be null");
      Validate.notNull(values, "values cannot be null");

      String resolveKey = resolveKey(key);
      if (keyLinkMap.containsKey(key) && dynamicMetadata.containsKey(resolveKey)) {
         dynamicMetadata.replaceMetadata(resolveKey, values);
      } else {
         localMetadata.replaceMetadata(resolveKey, values);
      }
   }

   /**
    * Combines STATIC, DYNAMIC, and LOCAL metadata into one metadata object. You
    * can restrict which metadata you want combined and change the order in
    * which combining takes place by specifying Type arguments in the order you
    * which precedence to be observed. For example, if you perform the
    * following: pgeMetadata.asMetadata(LOCAL, STATIC) then only LOCAL and
    * STATIC metadata will be combined and LOCAL metadata will trump STATIC
    * metadata if they both contain the same key. If no arguments are specified
    * then DEFAULT_COMBINE_ORDER is used.
    * 
    * @param types
    *           The Type hierarchy you which to use when metadata is combined,
    *           if no args then DEFAULT_COMBINE_ORDER is used.
    * @return Combined metadata.
    */
   public Metadata asMetadata(Type... types) {
      List<Type> combineOrder = Lists.newArrayList(types);
      if (combineOrder.isEmpty()) {
         combineOrder.addAll(DEFAULT_COMBINE_ORDER);
      }

      Metadata combinedMetadata = new Metadata();
      for (Type type : combineOrder) {
         switch (type) {
            case DYNAMIC:
               combinedMetadata.replaceMetadata(dynamicMetadata);
               break;
            case STATIC:
               combinedMetadata.replaceMetadata(staticMetadata);
               break;
            case LOCAL:
               combinedMetadata.replaceMetadata(localMetadata);
               for (String key : keyLinkMap.keySet()) {
                  List<String> values = getAllMetadata(key);
                  if (values != null) {
                     combinedMetadata.replaceMetadata(key, values);
                  }
               }
               break;
         }
      }
      return combinedMetadata;
   }

   public List<String> getAllMetadata(PgeTaskMetKeys key, Type... types) {
      return getAllMetadata(key.getName(), types);
   }

   /**
    * Get metadata values for given key. If Types are specified then it provides
    * the precedence order in which to search for the key. If no Type args are
    * specified then DEFAULT_QUERY_ORDER will be used. For example if
    * you pass in Type args: STATIC, LOCAL then STATIC metadata will first be
    * checked for the key and if it contains it, then it will return the found
    * value, otherwise it will then check LOCAL metadata for the key and if it
    * finds the value it will return it, otherwise null.
    * 
    * @param key
    *           The key for whose metadata values should be returned.
    * @param types
    *           The type hierarchy which should be used, if no Types specified
    *           DEFAULT_QUERY_ORDER will be used.
    * @return Metadata values for given key.
    */
   public List<String> getAllMetadata(String key, Type... types) {
      List<Type> queryOrder = Lists.newArrayList(types);
      if (queryOrder.isEmpty()) {
         queryOrder.addAll(DEFAULT_QUERY_ORDER);
      }

      String useKey = resolveKey(key);
      for (Type type : queryOrder) {
         switch (type) {
            case DYNAMIC:
               if (dynamicMetadata.containsKey(useKey)) {
                  return dynamicMetadata.getAllMetadata(useKey);
               }
               break;
            case STATIC:
               if (staticMetadata.containsKey(useKey)) {
                  return staticMetadata.getAllMetadata(useKey);
               }
               break;
            case LOCAL:
               if (localMetadata.containsKey(useKey)) {
                  return localMetadata.getAllMetadata(useKey);
               }
               break;
         }
      }
      return new Vector<String>();
   }

   public String getMetadata(PgeTaskMetKeys key, Type... types) {
      return getMetadata(key.getName(), types);
   }

   /**
    * Returns the first value returned by {@link #getAllMetadata(String, Type...)}, if it returns
    * null then this method will also return null.
    */
   public String getMetadata(String key, Type... types) {
      List<String> values = getAllMetadata(key, types);
      return values != null && values.size() > 0 ? values.get(0) : null;
   }
}
