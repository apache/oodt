/**
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

package org.apache.oodt.cas.pge.writers;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Wrapper around CAS {@link Metadata} object to provide Velocity template
 * semantics, e.g., a {@link #get(String)} method that allows for
 * ${metadata.author} (the '.' is a call that Velocity then makes from the
 * template to the {@link #get(String)} method in the class.
 * 
 * @author pramirez
 * 
 */
public class VelocityMetadata extends Metadata {
  private Metadata _metadata = null;

  public VelocityMetadata(Metadata metadata) {
    this._metadata = metadata;
  }

  public String get(String key) {
    return _metadata.getMetadata(key);
  }

  public List<String> getValues(String key) {
    return (List<String>) _metadata.getAllMetadata(key);
  }
}
